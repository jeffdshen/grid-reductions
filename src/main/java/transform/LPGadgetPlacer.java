package transform;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import transform.lp.*;
import transform.placer.LargeNode;
import transform.placer.LocationID;
import transform.planar.AbstractCostFunction;
import transform.planar.GadgetSet;
import transform.wiring.FrobeniusWirer;
import transform.wiring.Shifter;
import transform.wiring.TurnShifter;
import transform.wiring.Wirer;
import types.Direction;
import types.Gadget;
import types.Location;
import types.Side;
import types.configuration.CellConfiguration;
import types.configuration.GadgetConfiguration;
import types.configuration.cells.Cell;
import types.configuration.cells.CellType;

import java.util.*;

import static transform.lp.ConstraintFactory.*;
import static transform.placer.PlacerUtils.*;

// TODO write a test
public class LPGadgetPlacer {
    private final Gadget empty;

    // wires mapped by their output direction
    private final Multimap<Direction, Gadget> wires;

    private final Map<List<Direction>, Gadget> turns;
    private final Map<Set<Direction>, Gadget> crossovers;
    private final Map<String, Gadget> gadgets;

    private final Wirer wirer;
    private final Shifter shifter;
    private final GadgetSet gadgetSet;

    public LPGadgetPlacer(
        Iterable<Gadget> wires,
        Iterable<Gadget> turns,
        Iterable<Gadget> crossovers,
        Gadget empty,
        Iterable<Gadget> gadgets
    ) {
        this.empty = empty;
        Preconditions.checkArgument(
            empty.getSizeX() == 1 || empty.getSizeY() == 1, "Non-1x1 empty gadgets are not supported yet"
        );

        this.wires = GadgetUtils.getWireMap(wires);
        this.gadgets = GadgetUtils.getGadgetMap(gadgets);
        this.turns = GadgetUtils.getTurnMap(turns);
        this.crossovers = GadgetUtils.getCrossoverMap(crossovers);
        this.wirer = new FrobeniusWirer(wires);
        this.shifter = new TurnShifter(turns, wires, wirer);
        this.gadgetSet = new GadgetSet(wires, turns, crossovers, empty, gadgets);
    }

    private void addBasicConstraints(CellConfiguration config, LinearProgram.Builder lp) {
        // slices are increasing, slice0 is 0
        lp.addConstraint(equalTo(getSlice(0).x, 0));
        for (int x = 0; x < config.getSizeX(); x++) {
            lp.addConstraint(atMost(getSlice(x).x, getSlice(x + 1).x));
        }

        lp.addConstraint(equalTo(getSlice(0).y, 0));
        for (int y = 0; y < config.getSizeY(); y++) {
            lp.addConstraint(atMost(getSlice(y).y, getSlice(y + 1).y));
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
                        lp.addConstraint(equalTo(side1.x, d.getX(), side2.x));
                        lp.addConstraint(equalTo(side1.y, d.getY(), side2.y));
                    }
                }
            }
        }
    }

    private Location findEndpoint(CellConfiguration config, Location loc, HashSet<Location> seen) {
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
                    if (!config.isValid(edge)) {
                        x = edge.getX();
                        break;
                    }

                    Cell edgeCell = config.getCell(edge);
                    CellType type = edgeCell.getCellType();
                    if (!((type == CellType.NODE || type == CellType.PORT))) {
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
                    if (!config.isValid(edge)) {
                        y = edge.getY();
                        break;
                    }

                    Cell edgeCell = config.getCell(edge);
                    CellType type = edgeCell.getCellType();
                    if (!(type == CellType.NODE || type == CellType.PORT)) {
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
        // TODO replace thick with better choice, also require thick <= maxThickness, else this just adds padding
        int thick = wirer.minThickness(d) * 2;
        int minLength = wirer.minLength(d, thick);

        // ports are offset, and have min length, then wire thickness
        switch (d) {
            case NORTH:
                lp.addConstraint(equalTo(endID.x, startID.x));
                lp.addConstraint(atMost(endID.y, minLength, startID.y));

                lp.addConstraint(atLeast(ImmutableMap.of(endID.x, 1, getSlice(end.getX()).x, -1), thick));
                lp.addConstraint(atLeast(ImmutableMap.of(getSlice(end.getX() + 1).x, 1, endID.x, -1), thick + 1));
                break;
            case SOUTH:
                lp.addConstraint(equalTo(startID.x, endID.x));
                lp.addConstraint(atMost(startID.y, minLength, endID.y));

                lp.addConstraint(atLeast(ImmutableMap.of(startID.x, 1, getSlice(start.getX()).x, -1), thick));
                lp.addConstraint(atLeast(ImmutableMap.of(getSlice(start.getX() + 1).x, 1, startID.x, -1), thick + 1));
                break;
            case EAST:
                lp.addConstraint(equalTo(startID.y, endID.y));
                lp.addConstraint(atMost(startID.x, minLength, endID.x));

                lp.addConstraint(atLeast(ImmutableMap.of(startID.y, 1, getSlice(start.getY()).y, -1), thick));
                lp.addConstraint(atLeast(ImmutableMap.of(getSlice(start.getY() + 1).y, 1, startID.y, -1), thick + 1));
                break;
            case WEST:
                lp.addConstraint(equalTo(endID.y, startID.y));
                lp.addConstraint(atMost(endID.x, minLength, startID.x));

                lp.addConstraint(atLeast(ImmutableMap.of(endID.y, 1, getSlice(end.getY()).y, -1), thick));
                lp.addConstraint(atLeast(ImmutableMap.of(getSlice(end.getY() + 1).y, 1, endID.y, -1), thick + 1));
                break;
        }
    }

    private void addSmallNodeConstraints(LinearProgram.Builder lp, Location start, Location end, Gadget g) {
        // ports are correctly offset from gadget offset
        LocationID id = getGadget(start.getX(), start.getY());
        for (int i = 0; i < g.getInputSize(); i++) {
            Side side = g.getInput(i);
            LocationID sideID = getSide(new Side(start, side.getDirection()));
            lp.addConstraint(equalTo(id.x, side.getX(), sideID.x));
            lp.addConstraint(equalTo(id.y,  side.getY(), sideID.y));
        }

        for (int i = 0; i < g.getOutputSize(); i++) {
            Side side = g.getOutput(i);
            LocationID sideID = getSide(new Side(start, side.getDirection()));
            lp.addConstraint(equalTo(id.x, side.getX(), sideID.x));
            lp.addConstraint(equalTo(id.y, side.getY(), sideID.y));
        }


        // gadgets within slice boundaries.
        lp.addConstraint(atLeast(id.x, getSlice(start.getX()).x));
        lp.addConstraint(atLeast(id.y, getSlice(start.getY()).y));
        lp.addConstraint(atMost(id.x, g.getSizeX(), getSlice(end.getX() + 1).x));
        lp.addConstraint(atMost(id.y, g.getSizeY(), getSlice(end.getY() + 1).y));
    }

    private void addLargeNodeConstraints(
        CellConfiguration config, LinearProgram.Builder lp, Location start, Location end, Gadget g
    ) {
        LargeNode.addConstraint(wirer, shifter, config, lp, start, end, g);
    }

    private void addNodeConstraints(
        CellConfiguration config, LinearProgram.Builder lp, Location start, Location end, Gadget g
    ) {
        if (!start.equals(end)) {
            addLargeNodeConstraints(config, lp, start, end, g);
        } else {
            addSmallNodeConstraints(lp, start, end, g);
        }
    }

    private void addConstraints(CellConfiguration config, LinearProgram.Builder lp, Location start, Location end) {
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
                addNodeConstraints(config, lp, start, end, turns.get(dirs));
                break;
            case CROSSOVER:
                addNodeConstraints(config, lp, start, end, crossovers.get(ImmutableSet.copyOf(cell.getInputDirections())));
                break;
            case NODE:
            case PORT:
                addNodeConstraints(config, lp, start, end, gadgets.get(cell.getName()));
                break;
        }
    }

    private Map<String, Double> runLP(CellConfiguration config) throws Exception {
        LinearProgram.Builder lp = LinearProgram.builder();

//        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
//        for (int i = 1; i <= config.getSizeX(); i++) {
//            builder.put(getSlice(i).x, 1);
//        }
//
//        for (int i = 1; i <= config.getSizeY(); i++) {
//            builder.put(getSlice(i).y, 1);
//        }
//        builder.put(getSlice(0).x, -config.getSizeX());
//        builder.put(getSlice(0).y, -config.getSizeY());
//
//        lp.setObjective(builder.build());
//        System.out.println(equalTo(builder.build(), 1));

        lp.setObjective(ImmutableMap.of(
            getSlice(0).x, -1, getSlice(config.getSizeX()).x, 1,
            getSlice(0).y, -1, getSlice(config.getSizeY()).y, 1
        ));

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

    private void placeWire(
        GadgetConfiguration config, Map<String, Double> sol, Location start, Location end, Direction d, Wirer wirer
    ) {
        LocationID startID = getSide(new Side(start, d.opposite()));
        LocationID endID = getSide(new Side(end, d));

        Preconditions.checkArgument(wirer.canWire(d), d);
        int thick = wirer.minThickness(d) * 2; // TODO replace this with better inequalities
        int startX = roundAndCheck(sol.get(startID.x));
        int startY = roundAndCheck(sol.get(startID.y));
        int endX = roundAndCheck(sol.get(endID.x));
        int endY = roundAndCheck(sol.get(endID.y));

        int length = Math.abs(endX + endY - startX - startY) + 1;
        GadgetConfiguration wire = wirer.wire(new Side(startX, startY, d.opposite()), length, thick);
        Preconditions.checkState(config.canConnect(new Location(0, 0), wire),
            String.format("Could not connect wire from %s to %s", start, end));
        config.connect(new Location(0, 0), wire);
    }

    private void placeNode(
        CellConfiguration cellConfig,
        GadgetConfiguration gadgetConfig,
        Map<String, Double> sol,
        Location start,
        Location end,
        Gadget g
    ) {
        if (!start.equals(end)) {
            LargeNode.place(wirer, shifter, cellConfig, gadgetConfig, sol, start, end, g);
            return;
        }

        // ports are correctly offset from gadget offset
        LocationID id = getGadget(start.getX(), start.getY());
        int x = roundAndCheck(sol.get(id.x));
        int y = roundAndCheck(sol.get(id.y));
        gadgetConfig.connect(new Location(x, y), g);
    }

    private void placeGadget(
        CellConfiguration cellConfig,
        GadgetConfiguration gadgetConfig,
        Map<String, Double> sol,
        Location start,
        Location end
    ) {
        Cell cell = cellConfig.getCell(start);

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
                placeNode(cellConfig, gadgetConfig, sol, start, end, turns.get(dirs));
                break;
            case CROSSOVER:
                Set inputDirs = ImmutableSet.copyOf(cell.getInputDirections());
                placeNode(cellConfig, gadgetConfig, sol, start, end, crossovers.get(inputDirs));
                break;
            case NODE:
            case PORT:
                placeNode(cellConfig, gadgetConfig, sol, start, end, gadgets.get(cell.getName()));
                break;
        }
    }

    private GadgetConfiguration placeGadgets(CellConfiguration cellConfig, Map<String, Double> sol) {
        GadgetConfiguration gadgetConfig = new GadgetConfiguration();

        // ports match gadget offsets, and gadget boundaries match up to the slices
        HashSet<Location> seen = new HashSet<>();
        for (int x = 0; x < cellConfig.getSizeX(); x++) {
            for (int y = 0; y < cellConfig.getSizeY(); y++) {
                Location loc = new Location(x, y);
                if (seen.contains(loc)) {
                    continue;
                }

                seen.add(loc);
                Location end = findEndpoint(cellConfig, loc, seen);
                placeGadget(cellConfig, gadgetConfig, sol, loc, end);
            }
        }

        return gadgetConfig;
    }

    private void print(CellConfiguration config, Map<String, Double> sol) {
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

    public GadgetConfiguration place(CellConfiguration cellConfig) throws Exception {
        System.out.println("cost: " + new AbstractCostFunction(cellConfig, gadgetSet).getTotalCost());

        Map<String, Double> sol = runLP(cellConfig);
        System.out.println(sol.get(getSlice(0).x) + "," + sol.get(getSlice(cellConfig.getSizeX()).x) +
            "," +  sol.get(getSlice(0).y) + "," + sol.get(getSlice(cellConfig.getSizeY()).y));
        print(cellConfig, sol);

        return placeGadgets(cellConfig, sol);
    }
}
