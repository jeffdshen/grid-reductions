package transform;

import com.google.common.base.Function;
import com.google.common.collect.*;
import types.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GadgetUtils {
    public static final Function<Gadget, Integer> WIRE_LENGTH = new Function<Gadget, Integer>() {
        @Override
        public Integer apply(Gadget input) {
            return input.getInput(0).getDirection().parallel(Direction.NORTH) ? input.getSizeY() : input.getSizeX();
        }
    };

    public static final Function<Gadget, Integer> WIRE_WIDTH = new Function<Gadget, Integer>() {
        @Override
        public Integer apply(Gadget input) {
            return input.getInput(0).getDirection().parallel(Direction.NORTH) ? input.getSizeX() : input.getSizeY();
        }
    };

    public static final Function<Gadget, Integer> WIRE_THICKNESS = new Function<Gadget, Integer>() {
        @Override
        public Integer apply(Gadget input) {
            Location loc = input.getInput(0).getLocation();

            if (input.getInput(0).getDirection().parallel(Direction.NORTH)) {
                return Math.max(loc.getX(), input.getSizeX() - 1 - loc.getX());
            }

            return Math.max(loc.getY(), input.getSizeY() - 1 - loc.getY());
        }
    };

    public static final Function<Gadget, Direction> WIRE_DIRECTION = new Function<Gadget, Direction>() {
        @Override
        public Direction apply(Gadget input) {
            return input.getOutput(0).getDirection();
        }
    };


    public static Set<Gadget> getSymmetries(Iterable<Gadget> gadgets) {
        ImmutableSet.Builder<Gadget> builder = ImmutableSet.builder();
        for (Gadget g : gadgets) {
            builder.addAll(getSymmetries(g));
        }
        return builder.build();
    }

    public static Set<Gadget> getRotations(Iterable<Gadget> gadgets) {
        ImmutableSet.Builder<Gadget> builder = ImmutableSet.builder();
        for (Gadget g : gadgets) {
            builder.addAll(getRotations(g));
        }
        return builder.build();
    }

    public static Set<Gadget> getRotations(Gadget g) {
        Gadget g2 = clockwise(g);
        Gadget g3 = clockwise(g2);
        Gadget g4 = clockwise(g3);
        return ImmutableSet.of(g, g2, g3, g4);
    }

    public static Set<Gadget> getSymmetries(Gadget g) {
        return Sets.union(getRotations(g), getRotations(transpose(g)));
    }

    public static Gadget transpose(Gadget g) {
        String[][] cells = new String[g.getSizeY()][g.getSizeX()];

        for (int x = 0; x < g.getSizeY(); x++) {
            for (int y = 0; y < g.getSizeX(); y++) {
                cells[x][y] = g.getCell(y, x);
            }
        }

        Function<Side, Side> transposeSide = new Function<Side, Side>() {
            @Nullable
            @Override
            public Side apply(Side input) {
                return new Side(input.getY(), input.getX(), input.getDirection().transpose());
            }
        };

        List<Side> inputs = Lists.transform(g.getInputs(), transposeSide);
        List<Side> outputs = Lists.transform(g.getOutputs(), transposeSide);

        return new Gadget(g.getName(), cells, inputs, outputs);
    }

    public static Gadget clockwise(Gadget g) {
        String[][] cells = new String[g.getSizeY()][g.getSizeX()];

        for (int x = 0; x < g.getSizeY(); x++) {
            for (int y = 0; y < g.getSizeX(); y++) {
                cells[x][y] = g.getCell(y, g.getSizeY() - x - 1);
            }
        }

        final int sizeY = g.getSizeY();

        Function<Side, Side> rotateSide = new Function<Side, Side>() {
            @Nullable
            @Override
            public Side apply(Side input) {
                return new Side(sizeY - input.getY() - 1, input.getX(), input.getDirection().clockwise());
            }
        };

        List<Side> inputs = Lists.transform(g.getInputs(), rotateSide);
        List<Side> outputs = Lists.transform(g.getOutputs(), rotateSide);

        return new Gadget(g.getName(), cells, inputs, outputs);
    }

    /**
     * Gets the corner in these directions. TODO: TEST
     */
    public static Location getCorner(Grid g, Direction d1, Direction d2) {
        Location[] corners = new Location[]{
            new Location(0, 0),
            new Location(0, g.getSizeY() - 1),
            new Location(g.getSizeX() - 1, g.getSizeY() - 1),
            new Location(g.getSizeX() - 1, 0),
        };

        for (Location loc : corners) {
            if (!g.isValid(loc.add(d1)) && !g.isValid(loc.add(d2))) {
                return loc;
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * Returns a profile of the gadget's inputs and outputs. TODO: TEST
     */
    public static Map<Direction, ? extends List<Boolean>> getProfile(Gadget g) {
        ImmutableMap.Builder<Direction, List<Boolean>> builder = ImmutableMap.builder();
        // TODO improve algorithm so that it runs in O(inputs/outputs) and not O(sizeX + sizeY)
        for (Direction d : Direction.values()) {
            Direction step = d.clockwise();
            int count = 0;
            ImmutableList.Builder<Boolean> side = ImmutableList.builder();
            for (Location loc = getCorner(g, d, step.opposite()); g.isValid(loc); loc = loc.add(step)) {
                if (g.isInput(loc, d)) {
                    side.add(true);
                } else if (g.isOutput(loc, d)) {
                    side.add(false);
                }
            }
            builder.put(d, side.build());
        }

        return builder.build();
    }



}
