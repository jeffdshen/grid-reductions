package transform.placer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import transform.GadgetUtils;
import transform.GridUtils;
import transform.lp.LinearProgram;
import transform.wiring.Shifter;
import transform.wiring.Wirer;
import types.Direction;
import types.Gadget;
import types.Location;
import types.Side;
import types.configuration.GadgetConfiguration;
import types.configuration.CellConfiguration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static transform.lp.ConstraintFactory.*;
import static transform.placer.PlacerUtils.*;

public class LargeNode {
    public static void addConstraint(
        Wirer wirer,
        Shifter shifter,
        CellConfiguration config,
        LinearProgram.Builder lp,
        Location start,
        Location end,
        Gadget g
    ) {
        new LargeNodeConstraint(wirer, shifter, config, lp, start, end, g).add();
    }

    public static void place(
        Wirer wirer,
        Shifter shifter,
        CellConfiguration cellConfig,
        GadgetConfiguration config,
        Map<String, Double> sol,
        Location start,
        Location end,
        Gadget g
    ) {
        new LargeNodePlacer(wirer, shifter, cellConfig, config, sol, start, end, g).place();
    }

    public static List<Boolean> getIsInput(final Gadget g, final Direction d, List<Side> sides) {
        return Lists.transform(sides, new Function<Side, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable Side s) {
                return g.isInput(s);
            }
        });
    }

    public static List<Boolean> getIsOutput(final Gadget g, final Direction d, List<Side> sides) {
        return Lists.transform(sides, new Function<Side, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable Side s) {
                return g.isOutput(s);
            }
        });
    }

    private static class LargeNodePlacer {
        private final Wirer wirer;
        private final Shifter shifter;
        private final Location start;
        private final Gadget g;
        private final Map<Direction, List<Side>> gridPorts;
        private final Map<Direction, List<Side>> gridSides;
        private final Map<Direction, List<Side>> gadgetPorts;
        private final GadgetConfiguration config;
        private final Map<String, Double> sol;

        private LargeNodePlacer (
            Wirer wirer,
            Shifter shifter,
            CellConfiguration cellConfig,
            GadgetConfiguration config,
            Map<String, Double> sol,
            Location start,
            Location end,
            Gadget g
        ) {
            this.wirer = wirer;
            this.shifter = shifter;
            this.config = config;
            this.sol = sol;
            this.start = start;
            this.g = g;

            Direction startDir = getBoundaryStartDir();
            gridPorts = GridUtils.getPorts(cellConfig, new Side(start, startDir), end);
            gridSides = GridUtils.getBoundaryAsMap(new Side(start, startDir), end);
            gadgetPorts = GadgetUtils.getPorts(g, new Side(0, 0, startDir), GridUtils.getCorner(g));
        }

        public void place() {
            // place gadget
            LocationID id = getGadget(start.getX(), start.getY());
            Location offset = new Location(roundAndCheck(sol.get(id.x)), roundAndCheck(sol.get(id.y)));

            Preconditions.checkState(config.canConnect(offset, g));
            config.connect(offset, g);

            for (Direction d : Direction.values()) {
                if (gadgetPorts.get(d).size() == 0) {
                    continue;
                }

                // place shifter
                boolean out = g.isOutput(gadgetPorts.get(d).get(0));

                int thickness = getShifterThickness(d);
                int length = getShifterLength(d, thickness);

                List<Side> outer = new ArrayList<>();
                List<Side> inner = new ArrayList<>();
                for (int i = 0; i < gridPorts.get(d).size(); i++) {
                    Side gadgetSide = gadgetPorts.get(d).get(i).add(offset);

                    LocationID sideID = getSide(gridPorts.get(d).get(i));
                    Location sideOffset = new Location(
                        roundAndCheck(sol.get(sideID.x)), roundAndCheck(sol.get(sideID.y))
                    );
                    outer.add(new Side(sideOffset, d));

                    int dist = sideOffset.dot(d) - gadgetSide.dot(d) - length;

                    inner.add(gadgetSide.add(d, dist).opposite());

                    Side wireInput = out ? gadgetSide.opposite() : gadgetSide.add(d, dist);
                    Direction wireDir = wireInput.getDirection().opposite();
                    GadgetConfiguration wire = wirer.wire(wireInput, dist, wirer.minThickness(wireDir));

                    Preconditions.checkState(config.canConnect(new Location(0, 0), wire));
                    config.connect(new Location(0, 0), wire);
                }

                List<Boolean> isInput = getIsOutput(g, d, gadgetPorts.get(d));
                GadgetConfiguration shift = shifter.shift(inner, outer, isInput, thickness);

                Preconditions.checkState(
                    config.canConnect(new Location(0, 0), shift),
                    String.format("Could not connect shifter for multinode %s", start)
                );

                config.connect(new Location(0, 0), shift);
            }
        }

        private int getShifterThickness(Direction d) {
            return shifter.minThickness(d, getIsOutput(g, d, gadgetPorts.get(d)));
        }

        private int getShifterLength(Direction d, int thickness) {
            return shifter.minLength(d, getIsOutput(g, d, gadgetPorts.get(d)), thickness);
        }
    }

    private static class LargeNodeConstraint {
        private final Wirer wirer;
        private final Shifter shifter;
        private final LinearProgram.Builder lp;
        private final Location start;
        private final Gadget g;
        private final Map<Direction, List<Side>> gridPorts;
        private final Map<Direction, List<Side>> gridSides;
        private final Map<Direction, List<Side>> gadgetPorts;

        private LargeNodeConstraint(
            Wirer wirer,
            Shifter shifter,
            CellConfiguration config,
            LinearProgram.Builder lp,
            Location start,
            Location end,
            Gadget g
        ) {
            this.wirer = wirer;
            this.shifter = shifter;
            this.lp = lp;
            this.start = start;
            this.g = g;

            Direction startDir = getBoundaryStartDir();
            gridPorts = GridUtils.getPorts(config, new Side(start, startDir), end);
            gridSides = GridUtils.getBoundaryAsMap(new Side(start, startDir), end);
            gadgetPorts = GadgetUtils.getPorts(g, new Side(0, 0, startDir), GridUtils.getCorner(g));
        }

        public void add() {
            for (Direction d : Direction.values()) {
                addSideAlignment(d);
                addSlice(d);
                addMinSeparation(d);
                addThicknessCW(d);
                addThicknessACW(d);
                addOffset(d);
            }
        }

        private void addSideAlignment(Direction d) {
            // all the sides are aligned
            List<Side> gridSideList = gridSides.get(d);

            for (int i = 1; i < gridSideList.size(); i++) {
                Side side = gridSideList.get(i);
                LocationID sideID = getSide(side);
                Side firstSide = gridSideList.get(0);
                LocationID firstSideID = getSide(firstSide);

                if (side.getDirection().isY()) {
                    lp.addConstraint(equalTo(sideID.y, firstSideID.y));
                } else {
                    lp.addConstraint(equalTo(sideID.x, firstSideID.x));
                }
            }
        }

        private void addSlice(Direction d) {
            // the primary side within slice boundaries

            Side s = gridSides.get(d).get(0);
            LocationID sideID = getSide(s);
            String slice = getSlice(s);
            if (s.getDirection().isPositive()) {
                if (s.getDirection().isY()) {
                    lp.addConstraint(atMost(sideID.y, 1, slice));
                } else {
                    lp.addConstraint(atMost(sideID.x, 1, slice));
                }
            } else {
                if (s.getDirection().isY()) {
                    lp.addConstraint(atLeast(sideID.y, slice));
                } else {
                    lp.addConstraint(atLeast(sideID.x, slice));
                }
            }
        }

        private void addMinSeparation(Direction d) {
            // min separation between outer wires
            if (gadgetPorts.get(d).size() <= 0) {
                return;
            }

            List<Integer> minSepOut = shifter.minSeparation(d, false, getIsInput(g, d, gadgetPorts.get(d)));

            List<Side> portList = gridPorts.get(d);
            for (int i = 0; i < portList.size() - 1; i++) {
                int sep = minSepOut.get(i);
                lp.addConstraint(lengthAtLeast(portList.get(i), portList.get(i + 1), d.clockwise(), sep + 1));
            }
        }

        private void addThicknessACW(Direction d) {
            if (gadgetPorts.get(d).size() == 0) {
                return;
            }

            Direction acw = d.anticlockwise();
            int thickness = getShifterThickness(d);
            int length = (gadgetPorts.get(acw).size() == 0) ? 0 : getShifterLength(acw, getShifterThickness(acw));
            lp.addConstraint(lengthAtLeast(gridSides.get(acw).get(0), gridPorts.get(d).get(0), acw, thickness + length));
        }

        private void addThicknessCW(Direction d) {
            if (gadgetPorts.get(d).size() == 0) {
                return;
            }

            Direction cw = d.clockwise();
            int thickness = getShifterThickness(d);
            int length = (gadgetPorts.get(cw).size() == 0) ? 0 : getShifterLength(cw, getShifterThickness(cw));

            List<Side> gridPortList = gridPorts.get(d);
            Side last = gridPortList.get(gridPortList.size() - 1);
            lp.addConstraint(lengthAtLeast(gridSides.get(cw).get(0), last, cw, thickness + length));
        }

        private int getShifterThickness(Direction d) {
            return shifter.minThickness(d, getIsOutput(g, d, gadgetPorts.get(d)));
        }

        private int getShifterLength(Direction d, int thickness) {
            return shifter.minLength(d, getIsOutput(g, d, gadgetPorts.get(d)), thickness);
        }

        private int getOffsetLength(Direction d) {
            int length = GridUtils.countCellsInDir(g, new Location(0, 0), d);

            if (gadgetPorts.get(d).size() == 0) {
                return length;
            }

            int thickness = getShifterThickness(d);
            // TODO wire could also be in opp dir - fix.
            length += wirer.minLength(d, wirer.minThickness(d)) + getShifterLength(d, thickness);

            return length;
        }

        private int getOffsetCWThickness(Direction d) {
            Direction cw = d.clockwise();
            int cells = GridUtils.countCellsInDir(g, new Location(0, 0), d);

            if (gadgetPorts.get(cw).size() == 0) {
                return cells;
            }

            int port = gadgetPorts.get(cw).get(0).dot(d);
            int thickness = getShifterThickness(cw);
            if (gadgetPorts.get(d).size() == 0) {
                return cells + thickness - port;
            }

            int length = getShifterLength(d, getShifterThickness(d));
            return cells + thickness + length - port;
        }

        private int getOffsetACWThickness(Direction d) {
            Direction acw = d.anticlockwise();
            int cells = GridUtils.countCellsInDir(g, new Location(0, 0), d);

            if (gadgetPorts.get(acw).size() == 0) {
                return cells;
            }

            int port = gadgetPorts.get(acw).get(gadgetPorts.get(acw).size() - 1).dot(d);
            int thickness = getShifterThickness(acw);
            if (gadgetPorts.get(d).size() == 0) {
                return cells + thickness - port;
            }

            int length = getShifterLength(d, getShifterThickness(d));
            return cells + thickness + length - port;
        }

        private void addOffset(Direction d) {
            // gadget offset far away from sides - thickness and frobenius

            LocationID id = getGadget(start.getX(), start.getY());
            LocationID sideID = getSide(gridSides.get(d).get(0));
            int length = Ordering.natural().max(getOffsetLength(d), getOffsetACWThickness(d), getOffsetCWThickness(d));

            lp.addConstraint(lengthAtLeast(id, sideID, d, length));
        }
    }

    private static Direction getBoundaryStartDir() {
        Direction a = Direction.getDirection(0, -1);
        Direction b = Direction.getDirection(-1, 0);
        return a.clockwise() == b ? b : a;
    }
}
