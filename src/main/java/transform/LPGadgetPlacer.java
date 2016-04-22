package transform;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import transform.lp.JOptimizerSolver;
import transform.lp.LinearProgram;
import transform.wiring.FrobeniusWirer;
import transform.wiring.Wirer;
import types.Direction;
import types.Gadget;
import types.Location;
import types.Side;
import types.configuration.GadgetConfiguration;
import types.configuration.GridConfiguration;
import types.configuration.cells.Cell;
import types.configuration.cells.CellType;

import java.util.*;

import static transform.lp.ConstraintFactory.*;

// TODO write a test
public class LPGadgetPlacer {
    private final Gadget empty;

    // wires mapped by their output direction
    private final Multimap<Direction, Gadget> wires;

    // wires
    private final Map<List<Direction>, Gadget> turns;
    private final Map<Set<Direction>, Gadget> crossovers;
    private final Map<String, Gadget> gadgets;

    private final Wirer wirer;

    public LPGadgetPlacer(
        Iterable<Gadget> wires,
        Iterable<Gadget> turns,
        Iterable<Gadget> crossovers,
        Gadget empty,
        Iterable<Gadget> gadgets
    ) throws Exception {
        this.empty = empty;
        if (empty.getSizeX() != 1 || empty.getSizeY() != 1) {
            throw new Exception("Non-1x1 empty gadgets are not supported yet");
        }


        this.wires = GadgetUtils.getWireMap(wires);
        this.gadgets = GadgetUtils.getGadgetMap(gadgets);
        this.turns = GadgetUtils.getTurnMap(turns);
        this.crossovers = GadgetUtils.getCrossoverMap(crossovers);
        this.wirer = new FrobeniusWirer(wires);
    }

    private void addBasicConstraints(GridConfiguration config, LinearProgram.Builder lp) throws Exception {
        // slices are increasing, slice0 is 0
        lp.addConstraint(equalTo(ImmutableMap.of(getSlice(0).x, 1), 0));
        for (int x = 0; x < config.getSizeX(); x++) {
            lp.addConstraint(atMost(ImmutableMap.of(getSlice(x).x, 1), ImmutableMap.of(getSlice(x + 1).x, 1)));
        }

        lp.addConstraint(equalTo(ImmutableMap.of(getSlice(0).y, 1), 0));
        for (int y = 0; y < config.getSizeY(); y++) {
            lp.addConstraint(atMost(ImmutableMap.of(getSlice(y).y, 1), ImmutableMap.of(getSlice(y + 1).y, 1)));
        }

        for (int x = 0; x < config.getSizeX(); x++) {
            for (int y = 0; y < config.getSizeY(); y++) {
                // ports match
                Cell cell = config.getCell(x, y);
                for (Direction d : Direction.values()) {
                    if (cell.isInput(d) || cell.isOutput(d)) {
                        Side side = new Side(x, y, d);
                        Side opp = side.opposite();

                        // skip if both wires
                        if (cell.getCellType() == CellType.WIRE
                                && config.getCell(opp.getX(), opp.getY()).getCellType() == CellType.WIRE) {
                            continue;
                        }

                        // take the smaller location of the pair
                        if (side.getX() < opp.getX() || (side.getX() == opp.getX() && side.getY() < opp.getY())) {
                            continue;
                        }

                        LocationID side1 = getSide(side);
                        LocationID side2 = getSide(opp);
                        lp.addConstraint(equalTo(ImmutableMap.of(side1.x, 1), d.getX(), ImmutableMap.of(side2.x, 1)));
                        lp.addConstraint(equalTo(ImmutableMap.of(side1.y, 1), d.getY(), ImmutableMap.of(side2.y, 1)));
                    }
                }
            }
        }
    }

    private Location findEndpoint(GridConfiguration config, Location loc, HashSet<Location> seen) {
        Cell cell = config.getCell(loc);
        switch (cell.getCellType()) {
            case EMPTY:
                return loc;
            case WIRE:
                Direction d = cell.getInputDirection(0);
                if (seen.contains(loc.add(cell.getInputDirection(0)))) {
                    d = cell.getOutputDirection(0);
                }

                for (Location wire = loc; ; wire = wire.add(d)) {
                    if (config.getCell(wire).getCellType() != CellType.WIRE || !config.isValid(wire)) {
                        return wire.subtract(d);
                    }

                    seen.add(wire);
                }
            case TURN:
                return loc;
            case CROSSOVER:
                return loc;
            case NODE:
            case PORT:
                List<Integer> id = cell.getId();
                String name = cell.getName();

                int x;
                for (Location edge = loc; ; edge = edge.add(1, 0)) {
                    Cell edgeCell = config.getCell(edge);
                    CellType type = edgeCell.getCellType();
                    if (!(config.isValid(edge) && (type == CellType.NODE || type == CellType.PORT))) {
                        x = edge.getX();
                        break;
                    }

                    if (!(edgeCell.getName().equals(name) && edgeCell.getId().equals(id))) {
                        x = edge.getX();
                        break;
                    }
                }

                int y;
                for (Location edge = loc; ; edge = edge.add(0, 1)) {
                    Cell edgeCell = config.getCell(edge);
                    CellType type = edgeCell.getCellType();
                    if (!(config.isValid(edge) && (type == CellType.NODE || type == CellType.PORT))) {
                        y = edge.getY();
                        break;
                    }

                    if (!(edgeCell.getName().equals(name) && edgeCell.getId().equals(id))) {
                        y = edge.getY();
                        break;
                    }
                }

                for (int i = loc.getX(); i < x; i++) {
                    for (int j = loc.getY(); j < y; j++) {
                        seen.add(new Location(i, j));
                    }
                }

                return new Location(x - 1, y - 1);
            default:
                throw new IllegalArgumentException();
        }
    }

    private void addWireConstraints(LinearProgram.Builder lp, Location start, Location end, Direction d, Wirer wirer) {
        LocationID startID = getSide(new Side(start, d.opposite()));
        LocationID endID = getSide(new Side(end, d));

        Preconditions.checkArgument(wirer.canWire(d), d);
        int thick = wirer.minThickness(d) * 2; // TODO replace this with better inequalities
        int minLength = wirer.minLength(d, thick);

        // ports are offset, and have min length, then wire thickness
        switch (d) {
            case NORTH:
                lp.addConstraint(equalTo(ImmutableMap.of(endID.x, 1), ImmutableMap.of(startID.x, 1)));
                lp.addConstraint(atMost(ImmutableMap.of(endID.y, 1), minLength, ImmutableMap.of(startID.y, 1)));

                lp.addConstraint(atLeast(ImmutableMap.of(endID.x, 1, getSlice(end.getX()).x, -1), thick));
                lp.addConstraint(atLeast(ImmutableMap.of(getSlice(end.getX() + 1).x, 1, endID.x, -1), thick + 1));
                break;
            case SOUTH:
                lp.addConstraint(equalTo(ImmutableMap.of(startID.x, 1), ImmutableMap.of(endID.x, 1)));
                lp.addConstraint(atMost(ImmutableMap.of(startID.y, 1), minLength, ImmutableMap.of(endID.y, 1)));

                lp.addConstraint(atLeast(ImmutableMap.of(startID.x, 1, getSlice(start.getX()).x, -1), thick));
                lp.addConstraint(atLeast(ImmutableMap.of(getSlice(start.getX() + 1).x, 1, startID.x, -1), thick + 1));
                break;
            case EAST:
                lp.addConstraint(equalTo(ImmutableMap.of(startID.y, 1), ImmutableMap.of(endID.y, 1)));
                lp.addConstraint(atMost(ImmutableMap.of(startID.x, 1), minLength, ImmutableMap.of(endID.x, 1)));

                lp.addConstraint(atLeast(ImmutableMap.of(startID.y, 1, getSlice(start.getY()).y, -1), thick));
                lp.addConstraint(atLeast(ImmutableMap.of(getSlice(start.getY() + 1).y, 1, startID.y, -1), thick + 1));
                break;
            case WEST:
                lp.addConstraint(equalTo(ImmutableMap.of(endID.y, 1), ImmutableMap.of(startID.y, 1)));
                lp.addConstraint(atMost(ImmutableMap.of(endID.x, 1), minLength, ImmutableMap.of(startID.x, 1)));

                lp.addConstraint(atLeast(ImmutableMap.of(endID.y, 1, getSlice(end.getY()).y, -1), thick));
                lp.addConstraint(atLeast(ImmutableMap.of(getSlice(end.getY() + 1).y, 1, endID.y, -1), thick + 1));
                break;
        }
    }

    private void addNodeConstraints(LinearProgram.Builder lp, Location start, Location end, Gadget g) {
        if (!start.equals(end)) {
            throw new IllegalArgumentException("Large nodes not yet implemented");
        }

        // ports are correctly offset from gadget offset
        LocationID id = getGadget(start.getX(), start.getY());
        for (int i = 0; i < g.getInputSize(); i++) {
            Side side = g.getInput(i);
            LocationID sideID = getSide(new Side(start, side.getDirection()));
            lp.addConstraint(equalTo(ImmutableMap.of(id.x, 1), side.getX(), ImmutableMap.of(sideID.x, 1)));
            lp.addConstraint(equalTo(ImmutableMap.of(id.y, 1), side.getY(), ImmutableMap.of(sideID.y, 1)));
        }

        for (int i = 0; i < g.getOutputSize(); i++) {
            Side side = g.getOutput(i);
            LocationID sideID = getSide(new Side(start, side.getDirection()));
            lp.addConstraint(equalTo(ImmutableMap.of(id.x, 1), side.getX(), ImmutableMap.of(sideID.x, 1)));
            lp.addConstraint(equalTo(ImmutableMap.of(id.y, 1), side.getY(), ImmutableMap.of(sideID.y, 1)));
        }


        // gadgets within slice boundaries.
        lp.addConstraint(atLeast(ImmutableMap.of(id.x, 1), ImmutableMap.of(getSlice(start.getX()).x, 1)));
        lp.addConstraint(atLeast(ImmutableMap.of(id.y, 1), ImmutableMap.of(getSlice(start.getY()).y, 1)));
        lp.addConstraint(
            atMost(ImmutableMap.of(id.x, 1), g.getSizeX(), ImmutableMap.of(getSlice(end.getX() + 1).x, 1))
        );
        lp.addConstraint(
            atMost(ImmutableMap.of(id.y, 1), g.getSizeY(), ImmutableMap.of(getSlice(end.getY() + 1).y, 1))
        );
    }

    private void addConstraints(GridConfiguration config, LinearProgram.Builder lp, Location start, Location end) {
        Cell cell = config.getCell(start);

        if (cell.getCellType() != CellType.EMPTY) {
            System.out.println(cell.getCellType() + " " +
                ImmutableList.copyOf(cell.getInputDirections()) + "," + ImmutableList.copyOf(cell.getOutputDirections()));
        }
        switch (cell.getCellType()) {
            case EMPTY:
                // do nothing
                break;
            case WIRE:
                if (start.equals(end)) {
                    addWireConstraints(lp, start, end, cell.getOutputDirection(0), wirer);
                } else if (Direction.getScalarDirection(end.subtract(start)) == cell.getOutputDirection(0)) {
                    addWireConstraints(lp, start, end, cell.getOutputDirection(0), wirer);
                } else {
                    addWireConstraints(lp, end, start, cell.getOutputDirection(0), wirer);
                }
                break;
            case TURN:
                List<Direction> dirs = ImmutableList.of(cell.getInputDirection(0), cell.getOutputDirection(0));
                addNodeConstraints(lp, start, end, turns.get(dirs));
                break;
            case CROSSOVER:
                addNodeConstraints(lp, start, end, crossovers.get(ImmutableSet.copyOf(cell.getInputDirections())));
                break;
            case NODE:
            case PORT:
                addNodeConstraints(lp, start, end, gadgets.get(cell.getName()));
                break;
        }
    }

    private Map<String, Double> runLP(GridConfiguration config) throws Exception {
        LinearProgram.Builder lp = LinearProgram.builder();

        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        for (int i = 1; i <= config.getSizeX(); i++) {
            builder.put(getSlice(i).x, 1);
        }

        for (int i = 1; i <= config.getSizeY(); i++) {
            builder.put(getSlice(i).y, 1);
        }
        builder.put(getSlice(0).x, -config.getSizeX());
        builder.put(getSlice(0).y, -config.getSizeY());

        lp.setObjective(builder.build());
        System.out.println(equalTo(builder.build(), 1));
//        lp.setObjective(ImmutableMap.of(
//            getSlice(0).x, -1, getSlice(config.getSizeX()).x, 1,
//            getSlice(0).y, -1, getSlice(config.getSizeY()).y, 1
//        ));

        addBasicConstraints(config, lp);

        // ports match gadget offsets, and gadget boundaries match up to the slices
        HashSet<Location> seen = new HashSet<>();
        for (int x = 0; x < config.getSizeX(); x++) {
            for (int y = 0; y < config.getSizeY(); y++) {
                Location loc = new Location(x, y);
                if (seen.contains(loc)) {
                    continue;
                }

                seen.add(loc);
                Location end = findEndpoint(config, loc, seen);
                addConstraints(config, lp, loc, end);
            }
        }

        return lp.build().getSolution(new JOptimizerSolver());
    }

    private int roundAndCheck(double d) {
        long num = Math.round(d);
//        Preconditions.checkState(Math.abs(d - num) < 0.1, d);
        Preconditions.checkState(num < 5000 && num >= 0, "Offset out of bounds");
        return (int) num;
    }

    private void placeWire(
        GadgetConfiguration config, Map<String, Double> sol, Location start, Location end, Direction d, Wirer wirer
    ) {
        LocationID startID = getSide(new Side(start, d.opposite()));
        LocationID endID = getSide(new Side(end, d));

        Preconditions.checkArgument(wirer.canWire(d), d);
        System.out.println(startID.x + "," + startID.y);
        System.out.println(endID.x + "," + endID.y);
        int thick = wirer.minThickness(d) * 2; // TODO replace this with better inequalities
        int startX = roundAndCheck(sol.get(startID.x));
        int startY = roundAndCheck(sol.get(startID.y));
        int endX = roundAndCheck(sol.get(endID.x));
        int endY = roundAndCheck(sol.get(endID.y));

        int length = Math.abs(endX + endY - startX - startY) + 1;
        System.out.println(wirer.minLength(d, thick) + ", " + d + ", " + thick + ", " + length + ", " + startX + "," + startY);
        System.out.println(new Side(startX, startY, d.opposite()).opposite());
        GadgetConfiguration wire = wirer.wire(new Side(startX, startY, d.opposite()), length, thick);
        Preconditions.checkState(config.canConnect(new Location(0, 0), wire), "\n" + config.toGrid(empty) + "\n" + wire.toGrid(empty));
        config.connect(new Location(0, 0), wire);
    }

    private void placeNode(
        GadgetConfiguration config, Map<String, Double> sol, Location start, Location end, Gadget g
    ) {
        if (!start.equals(end)) {
            throw new IllegalArgumentException("Large nodes not yet implemented");
        }

        // ports are correctly offset from gadget offset
        LocationID id = getGadget(start.getX(), start.getY());
        int x = roundAndCheck(sol.get(id.x));
        int y = roundAndCheck(sol.get(id.y));
        config.connect(new Location(x, y), g);
    }

    private void placeGadget(
        GridConfiguration gridConfig, GadgetConfiguration gadgetConfig,
        Map<String, Double> sol, Location start, Location end
    ) {
        Cell cell = gridConfig.getCell(start);

        if (cell.getCellType() != CellType.EMPTY) {
            System.out.println(cell.getCellType());
        }

        switch (cell.getCellType()) {
            case EMPTY:
                // do nothing
                break;
            case WIRE:
                if (start.equals(end)) {
                    placeWire(gadgetConfig, sol, start, end, cell.getOutputDirection(0), wirer);
                } else if (Direction.getScalarDirection(end.subtract(start)) == cell.getOutputDirection(0)) {
                    placeWire(gadgetConfig, sol, start, end, cell.getOutputDirection(0), wirer);
                } else {
                    placeWire(gadgetConfig, sol, end, start, cell.getOutputDirection(0), wirer);
                }
                break;
            case TURN:
                List<Direction> dirs = ImmutableList.of(cell.getInputDirection(0), cell.getOutputDirection(0));
                placeNode(gadgetConfig, sol, start, end, turns.get(dirs));
                break;
            case CROSSOVER:
                Set inputDirs = ImmutableSet.copyOf(cell.getInputDirections());
                placeNode(gadgetConfig, sol, start, end, crossovers.get(inputDirs));
                break;
            case NODE:
            case PORT:
                placeNode(gadgetConfig, sol, start, end, gadgets.get(cell.getName()));
                break;
        }
    }

    private GadgetConfiguration placeGadgets(GridConfiguration gridConfig, Map<String, Double> sol) throws Exception {
        GadgetConfiguration gadgetConfig = new GadgetConfiguration();

        // ports match gadget offsets, and gadget boundaries match up to the slices
        HashSet<Location> seen = new HashSet<>();
        for (int x = 0; x < gridConfig.getSizeX(); x++) {
            for (int y = 0; y < gridConfig.getSizeY(); y++) {
                Location loc = new Location(x, y);
                if (seen.contains(loc)) {
                    continue;
                }

                seen.add(loc);
                Location end = findEndpoint(gridConfig, loc, seen);
                placeGadget(gridConfig, gadgetConfig, sol, loc, end);
            }
        }

        return gadgetConfig;
    }

    private void print(GridConfiguration config, Map<String, Double> sol) {
        List<Map.Entry<String, Double>> entries = ImmutableList.copyOf(sol.entrySet());

        entries = Ordering.from(new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        }).sortedCopy(entries);

        for (Map.Entry<String, Double> entry : entries) {
            if (entry.getKey().contains("x")) {
                System.out.println(entry);
            }
        }

        for (Map.Entry<String, Double> entry : entries) {
            if (entry.getKey().contains("y")) {
                System.out.println(entry);
            }
        }
    }

    public GadgetConfiguration place(GridConfiguration gridConfig) throws Exception {
        Map<String, Double> sol = runLP(gridConfig);
        System.out.println(sol.get(getSlice(0).x) + "," + sol.get(getSlice(gridConfig.getSizeX()).x) +
            "," +  sol.get(getSlice(0).y) + "," + sol.get(getSlice(gridConfig.getSizeY()).y));
        System.out.println(sol);
        print(gridConfig, sol);

        return placeGadgets(gridConfig, sol);
    }

    /**
     * formats:
     * side [x] [y] [d]
     * x [x]
     * y [y]
     * gadgetX [x] [y]
     * gadgetY [x] [y]
     */
    private static class LocationID {
        private final String x;
        private final String y;

        public LocationID(String s, Object... objects) {
            this.x = s + "x:" + Joiner.on("-").join(objects);
            this.y = s + "y:" + Joiner.on("-").join(objects);
        }
    }

    private static LocationID getSide(Side side) {
        Location loc = side.getLocation();
        Direction dir = side.getDirection();
        return new LocationID("side", loc.getX(), loc.getY(), dir.ordinal());
    }

    private static LocationID getSlice(int slice) {
        return new LocationID("slice", slice);
    }

    private static LocationID getGadget(int x, int y) {
        return new LocationID("gadget", x, y);
    }
}
