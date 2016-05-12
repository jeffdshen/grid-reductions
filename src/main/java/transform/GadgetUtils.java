package transform;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.io.PatternFilenameFilter;
import parser.GadgetParser;
import types.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class GadgetUtils {
    public static final Function<Gadget, Integer> WIRE_LENGTH = new Function<Gadget, Integer>() {
        @Override
        public Integer apply(Gadget input) {
            return input.getInput(0).getDirection().isY() ? input.getSizeY() : input.getSizeX();
        }
    };

    public static final Function<Gadget, Integer> WIRE_WIDTH = new Function<Gadget, Integer>() {
        @Override
        public Integer apply(Gadget input) {
            return input.getInput(0).getDirection().isY() ? input.getSizeX() : input.getSizeY();
        }
    };

    public static final Function<Gadget, Integer> WIRE_THICKNESS = new Function<Gadget, Integer>() {
        @Override
        public Integer apply(Gadget input) {
            Location loc = input.getInput(0).getLocation();

            if (input.getInput(0).getDirection().isY()) {
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

    public static Map<String, Gadget> getGadgetMap(Iterable<Gadget> gadgets) {
        return Maps.uniqueIndex(gadgets, new Function<Gadget, String>() {
            @Override
            public String apply(Gadget input) {
                return input.getName();
            }
        });
    }

    public static Map<Set<Direction>, Gadget> getCrossoverMap(Iterable<Gadget> gadgets) {
        ImmutableMap.Builder<Set<Direction>, Gadget> crossovers = ImmutableMap.builder();
        for (Gadget g : gadgets) {
            Preconditions.checkArgument(g.getInputSize() == 2, "Crossover input size must be 2");
            Preconditions.checkArgument(g.getOutputSize() == 2, "Crossover output size must be 2");
            Direction i0 = g.getInput(0).getDirection();
            Direction i1 = g.getInput(1).getDirection();
            Direction o0 = g.getOutput(0).getDirection();
            Direction o1 = g.getOutput(1).getDirection();

            Preconditions.checkArgument(i0.opposite() == o0,
                "Crossover input and output must have opposite directions");
            Preconditions.checkArgument(i1.opposite() == o1,
                "Crossover input and output must have opposite directions");
            Preconditions.checkArgument(i0.perpendicular(i1),
                "Crossover inputs must have perpendicular directions");
            crossovers.put(ImmutableSet.of(i0, i1), g);
        }

        return crossovers.build();
    }

    public static Multimap<Direction, Gadget> getWireMap(Iterable<Gadget> gadgets) {
        ImmutableMultimap.Builder<Direction, Gadget> wires = ImmutableMultimap.builder();
        for (Gadget g : gadgets) {
            Preconditions.checkArgument(g.getInputSize() == 1, "Wire input size must be 1");
            Preconditions.checkArgument(g.getOutputSize() == 1, "Wire output size must be 1");
            Preconditions.checkArgument(g.getInput(0).getDirection().opposite() == g.getOutput(0).getDirection(),
                "Wire input and output must have opposite directions");
            wires.put(g.getOutput(0).getDirection(), g);
        }
        return wires.build();
    }

    public static Map<List<Direction>, Gadget> getTurnMap(Iterable<Gadget> gadgets) {
        ImmutableMap.Builder<List<Direction>, Gadget> turns = ImmutableMap.builder();
        for (Gadget g : gadgets) {
            Preconditions.checkArgument(g.getInputSize() == 1, "Turn input size must be 1");
            Preconditions.checkArgument(g.getOutputSize() == 1, "Turn output size must be 1");
            Preconditions.checkArgument(g.getInput(0).getDirection().perpendicular(g.getOutput(0).getDirection()),
                "Turn input and output must have perpendicular directions");
            turns.put(ImmutableList.of(g.getInput(0).getDirection(), g.getOutput(0).getDirection()), g);
        }

        return turns.build();
    }

    /**
     * Returns all gadgets in the directory, where files are filtered by the file name
     */
    public static Iterable<Gadget> getGadgets(File dir, FilenameFilter filter) throws IOException {
        return getGadgets(Arrays.asList(dir.listFiles(filter)));
    }

    /**
     * Returns all gadgets in the directory, where file names are of the form *.txt
     */
    public static Iterable<Gadget> getGadgets(File dir) throws IOException {
        return getGadgets(dir, new PatternFilenameFilter(".*\\.txt"));
    }

    public static Iterable<Gadget> getGadgets(Iterable<File> gadgets) throws IOException {
        GadgetParser parser = new GadgetParser();

        ImmutableList.Builder<Gadget> builder = ImmutableList.builder();
        for (File file : gadgets) {
            Gadget gadget = parser.parseGadget(file);
            builder.add(gadget);
        }

        return builder.build();
    }

    // TODO make immutable
    public static Map<Direction, List<Side>> getPorts(Gadget g, Side start, Location end) {
        HashMap<Direction, List<Side>> gadgetSides = new HashMap<>();
        for (Direction d : Direction.values()) {
            gadgetSides.put(d, new ArrayList<Side>());
        }

        for (Side s : GridUtils.getBoundary(start, end)) {
            if (g.isInput(s) || g.isOutput(s)) {
                gadgetSides.get(s.getDirection()).add(s);
            }
        }
        return gadgetSides;
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
