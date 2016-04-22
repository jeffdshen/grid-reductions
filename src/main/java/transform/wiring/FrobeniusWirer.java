package transform.wiring;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.google.common.math.IntMath;
import transform.GadgetUtils;
import types.Direction;
import types.Gadget;
import types.Side;
import types.configuration.GadgetConfiguration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * NOTE here, width means thickness
 */
// TODO swap with linear diophantine solver?
public class FrobeniusWirer implements Wirer {
    private Map<Direction, FrobeniusWirerHelper> wirers;

    public FrobeniusWirer(Iterable<Gadget> wires) {
        Map<Direction, Collection<Gadget>> wiresByDir = Multimaps.index(wires, GadgetUtils.WIRE_DIRECTION).asMap();
        ImmutableMap.Builder<Direction, FrobeniusWirerHelper> builder = ImmutableMap.builder();
        for (Direction d : Direction.values()) {
            if (wiresByDir.containsKey(d)) {
                FrobeniusWirerHelper wirer = getFrobeniusWirerHelper(wiresByDir.get(d));
                if (wirer != null) {
                    builder.put(d, wirer);
                }
            }
        }

        wirers = builder.build();
    }

    @Override
    public GadgetConfiguration wire(Side input, int length, int thickness) {
        return wirers.get(input.getDirection().opposite()).wire(input, length, thickness);
    }

    @Override
    public boolean canWire(Direction dir) {
        return wirers.containsKey(dir);
    }

    @Override
    public int minThickness(Direction dir) {
        return wirers.get(dir).minThickness();
    }

    @Override
    public int minLength(Direction dir, int thickness) {
        return wirers.get(dir).minLength(thickness);
    }

    private static FrobeniusWirerHelper getFrobeniusWirerHelper(Iterable<Gadget> allWires) {
        final Ordering<Gadget> orderByWidth = Ordering.natural().onResultOf(GadgetUtils.WIRE_THICKNESS);
        // get wires by length, select one with min width
        Map<Integer, Gadget> wires = ImmutableMap.copyOf(Maps.transformValues(
            Multimaps.index(allWires, GadgetUtils.WIRE_LENGTH).asMap(),
            new Function<Collection<Gadget>, Gadget>() {
                @Override
                public Gadget apply(Collection<Gadget> input) {
                    return orderByWidth.min(input);
                }
            }
        ));

        // sort wires by width
        List<Gadget> wiresByWidth = orderByWidth.immutableSortedCopy(wires.values());

        // find min width
        int minWidth = computeMinWidth(wiresByWidth);

        if (minWidth < 0) {
            return null;
        }

        Map<Integer, FrobeniusSolver> solvers = computeSolvers(wiresByWidth, minWidth);

        return new FrobeniusWirerHelper(wires, wiresByWidth, minWidth, solvers);
    }

    /**
     * @return min width. -1 if not possible
     */
    @SuppressWarnings("ConstantConditions")
    private static int computeMinWidth(List<Gadget> wiresByWidth) {
        if (wiresByWidth.size() == 1) {
            return -1;
        }

        int gcd = GadgetUtils.WIRE_LENGTH.apply(wiresByWidth.get(0));

        for (Gadget gadget : wiresByWidth) {
            gcd = IntMath.gcd(gcd, GadgetUtils.WIRE_LENGTH.apply(gadget));
            if (gcd == 1) {
                return GadgetUtils.WIRE_THICKNESS.apply(gadget);
            }
        }

        return -1;
    }

    /**
     * Finds the index of the first element of the list that is greater than num.
     * Returns the list size if all elements are at most num.
     */
    private static int search(List<Integer> list, int num) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) > num) {
                return i;
            }
        }
        return list.size();
    }

    private static Map<Integer, FrobeniusSolver> computeSolvers(List<Gadget> wiresByWidth, int minWidth) {
        List<Integer> widths = Lists.transform(wiresByWidth, GadgetUtils.WIRE_THICKNESS);
        List<Integer> lengths = Lists.transform(wiresByWidth, GadgetUtils.WIRE_LENGTH);
        int index = search(widths, minWidth);

        ImmutableMap.Builder<Integer, FrobeniusSolver> builder = ImmutableMap.builder();
        for (int i = index; i <= wiresByWidth.size(); i++) {
            builder.put(i, new FrobeniusSolver(lengths.subList(0, i)));

            // skip duplicates
            while (i+1 < widths.size() && widths.get(i+1).equals(widths.get(i))) {
                i++;
            }
        }
        return builder.build();
    }

    /**
     * Wirer for a given direction
     */
    private static class FrobeniusWirerHelper {
        // Direction, length to gadget
        private Map<Integer, Gadget> wires;
        private List<Gadget> wiresByWidth;

        private int minWidth;

        // map from direction, index for the wiresByWidth to a solver.
        private Map<Integer, FrobeniusSolver> solvers;

        public FrobeniusWirerHelper(
            Map<Integer, Gadget> wires, List<Gadget> wiresByWidth, int minWidth, Map<Integer, FrobeniusSolver> solvers
        ) {
            this.wires = wires;
            this.wiresByWidth = wiresByWidth;
            this.minWidth = minWidth;
            this.solvers = solvers;
        }

        private FrobeniusSolver getSolver(int width) {
            List<Integer> widths = Lists.transform(wiresByWidth, GadgetUtils.WIRE_THICKNESS);

            int index = search(widths, width);
            return solvers.get(index);
        }

        public int minThickness() {
            return minWidth;
        }

        public GadgetConfiguration wire(Side input, int length, int width) {
            FrobeniusSolver solver = getSolver(width);
            int[] coeff = solver.getCoefficients(length);
            GadgetConfiguration config = new GadgetConfiguration();

            // i = 0
            Integer lastId = null;

            for (int i = 0; i < coeff.length; i++) {
                Gadget wire = wires.get(solver.getWireLength(i));
                for (int j = 0; j < coeff[i]; j++) {
                    if (lastId != null) {
                        lastId = config.connectInputPort(lastId, 0, wire, 0);
                    } else {
                        lastId = config.connectInputPort(input.opposite(), wire, 0);
                    }
                }
            }

            return config;
        }

        public int minLength(int width) {
            return getSolver(width).getSolvableCutoff();
        }
    }
}
