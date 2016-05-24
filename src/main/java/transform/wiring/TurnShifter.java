package transform.wiring;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import transform.GadgetUtils;
import transform.GridUtils;
import types.Direction;
import types.Gadget;
import types.Location;
import types.Side;
import types.configuration.GadgetConfiguration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TurnShifter implements Shifter {
    private final Multimap<Direction, Gadget> wires;
    private final Map<List<Direction>, Gadget> turns;

    private final Wirer wirer;

    public TurnShifter(Iterable<Gadget> turns, Iterable<Gadget> wires, Wirer wirer) {
        this.wirer = wirer;
        this.turns = GadgetUtils.getTurnMap(turns);
        this.wires = GadgetUtils.getWireMap(wires);
    }

    public GadgetConfiguration shift(List<Side> start, List<Side> end, List<Boolean> isInput, int thickness) {
        GadgetConfiguration config = new GadgetConfiguration();
        int n = start.size();
        Preconditions.checkArgument(n == end.size(), "start and end sizes must be the same.");
        Preconditions.checkArgument(n == isInput.size(), "start and isInput sizes must be the same");

        if (n == 0) {
            return config;
        }

        // get dir
        final Direction dir = start.get(0).opposite().getDirection();
        final Direction cw = dir.clockwise();
        final Direction opp = dir.opposite();
        final Direction acw = dir.anticlockwise();

        int minThicknessDir = wirer.minThickness(dir);
        int minThicknessOpp = wirer.minThickness(opp);
        int minLengthDir = wirer.minLength(dir, minThicknessDir);
        int minLengthOpp = wirer.minLength(opp, minThicknessOpp);

        int totalLength = 0;
        ArrayList<Integer> endpoint = new ArrayList<>();

        // do first layer
        for (int i = 0; i < n; i++) {
            boolean ii = isInput.get(i);
            Side s = start.get(i);
            int minLength = ii ? minLengthDir : minLengthOpp;
            totalLength = Math.max(totalLength, minLength);
            GadgetConfiguration wire = getWire(s, ii, totalLength);

            tryConnectWire(config, wire);

            Gadget turn = ii ? getTurn(opp, acw) : getTurn(acw, opp);

            int left = Math.max(getTurnThickness(opp, acw, opp, ii), getTurnThickness(cw, dir, opp, ii));
            int right = Math.max(getTurnThickness(opp, acw, dir, ii), getTurnThickness(cw, dir, dir, ii));
            int layer = left + right + 1;

            endpoint.add(tryConnectTurn(config, s.add(dir, totalLength).opposite(), turn, ii));
            totalLength += layer;
        }

        // do last layer
        totalLength = 0;
        ArrayList<Integer> startpoint = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            boolean ii = isInput.get(i);
            Side s = end.get(i);
            int minLength = ii ? minLengthDir : minLengthOpp;
            totalLength = Math.max(totalLength, minLength);
            GadgetConfiguration wire = getWire(s, !ii, totalLength);

            tryConnectWire(config, wire);

            Gadget turn = ii ? getTurn(acw, dir) : getTurn(dir, acw);

            int left = Math.max(getTurnThickness(opp, cw, opp, ii), getTurnThickness(acw, dir, opp, ii));
            int right = Math.max(getTurnThickness(opp, cw, dir, ii), getTurnThickness(acw, dir, dir, ii));
            int layer = left + right + 1;

            startpoint.add(tryConnectTurn(config, s.add(opp, totalLength).opposite(), turn, !ii));
            totalLength += layer;
        }

        // do middle layer
        Ordering<Side> ordering = Ordering.from(new Comparator<Side>() {
            @Override
            public int compare(Side a, Side b) {
                return a.dot(acw) - b.dot(acw);
            }
        }).nullsFirst();

        Side cur = null;
        for (int i = n - 1; i >= 0; i--) {
            boolean ii = isInput.get(i);
            Side first = ii ? config.getOutputSide(endpoint.get(i), 0) : config.getInputSide(endpoint.get(i), 0);
            Side second = ii ? config.getInputSide(startpoint.get(i), 0) : config.getOutputSide(startpoint.get(i), 0);

            int firstTurnThickness = getTurnThickness(cw, dir, cw, ii);
            int secondTurnThickness = getTurnThickness(opp, cw, cw, ii);

            int minFirst = getPerpWireMinLength(dir, true, ii) + firstTurnThickness;
            int minSecond = getPerpWireMinLength(dir, false, ii) + secondTurnThickness;
            Side firstReq = first.add(acw, minFirst);
            Side secondReq = second.add(acw, minSecond);

            if (cur != null) {
                cur = cur.add(acw, getWireMinThickness(dir, ii));
            }
            cur = ordering.max(firstReq, secondReq, cur);

            int firstLength = cur.subtract(first.getLocation()).dot(acw) - firstTurnThickness;
            tryConnectWire(config, getPerpWire(dir, first.opposite(), true, ii, firstLength));

            int secondLength = cur.subtract(second.getLocation()).dot(acw) - secondTurnThickness;
            tryConnectWire(config, getPerpWire(dir, second.opposite(), false, ii, secondLength));

            Gadget firstTurn = ii ? getTurn(cw, dir) : getTurn(dir, cw);
            int idFirst = tryConnectTurn(config, first.add(acw, firstLength), firstTurn, ii);

            Gadget secondTurn = ii ? getTurn(opp, cw) : getTurn(cw, opp);
            int idSecond = tryConnectTurn(config, second.add(acw, secondLength), secondTurn, !ii);

            Side midFirst = ii ? config.getOutputSide(idFirst, 0) : config.getInputSide(idFirst, 0);
            Side midSecond = ii ? config.getInputSide(idSecond, 0) : config.getOutputSide(idSecond, 0);
            int midLength = midSecond.subtract(midFirst.opposite().getLocation()).dot(dir);

            tryConnectWire(config, getWire(midFirst.opposite(), ii, midLength));

            cur = cur.add(acw, 1 + Math.max(getTurnThickness(cw, dir, acw, ii), getTurnThickness(opp, cw, acw, ii)));
        }

        return config;
    }

    public int minThickness(Direction dir, List<Boolean> isInput) {
        // assume that the protrusion is anticlockwise, e.g. right -> up -> right -> down -> right
        Direction cw = dir.clockwise();
        Direction opp = dir.opposite();
        Direction acw = dir.anticlockwise();

        // non protruding side
        boolean lastInput = isInput.get(isInput.size() - 1);
        int cwSize = Math.max(getTurnThickness(opp, acw, cw, lastInput), getTurnThickness(acw, dir, cw, lastInput));

        // protruding side
        List<Boolean> isOutput = Lists.transform(isInput, new Function<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean input) {
                return !input;
            }
        });
        List<Integer> minSepStart = minSeparation(dir, true, isInput);
        List<Integer> minSepEnd = minSeparation(dir, false, isOutput);

        // each step is max of minimum protrusion and some modification of the protrusion from previous input
        int totalSize = minProtrusion(dir, lastInput);

        for (int i = isInput.size() - 2; i >= 0; i--) {
            boolean ii = isInput.get(i);
            Direction out = ii ? dir : opp;

            int maxTurnThickness = Math.max(getTurnThickness(cw, dir, acw, ii), getTurnThickness(opp, cw, acw, ii));
            int per = wirer.minThickness(out) + 1 + maxTurnThickness;

            int size = minProtrusion(dir, isInput.get(i));
            totalSize = Math.max(size, totalSize + per - (Math.min(minSepStart.get(i), minSepEnd.get(i)) + 1));
        }

        return Math.max(totalSize, cwSize);
    }

    public int minLength(Direction dir, List<Boolean> isInput, int thickness) {
        // assume that the protrusion is anticlockwise, e.g. right -> up -> right -> down -> right
        Direction cw = dir.clockwise();
        Direction opp = dir.opposite();
        Direction acw = dir.anticlockwise();

        int minLengthDir = wirer.minLength(dir, wirer.minThickness(dir));
        int minLengthOpp = wirer.minLength(opp, wirer.minThickness(opp));

        int totalLength = 0;
        ArrayList<Integer> lengths = new ArrayList<>();

        // first layer
        for (int i = 0; i < isInput.size(); i++) {
            boolean ii = isInput.get(i);

            int left = Math.max(getTurnThickness(opp, acw, opp, ii), getTurnThickness(cw, dir, opp, ii));
            int right = Math.max(getTurnThickness(opp, acw, dir, ii), getTurnThickness(cw, dir, dir, ii));
            int layer = left + right + 1;
            int minLength = ii ? minLengthDir : minLengthOpp;

            totalLength = Math.max(totalLength + layer, minLength + layer);
            lengths.add(totalLength);
        }

        // middle layer
        for (int i = isInput.size() - 1; i >= 0; i--) {
            boolean ii = isInput.get(i);

            int left = Math.max(getTurnThickness(opp, cw, opp, ii), getTurnThickness(acw, dir, opp, ii));
            int right = Math.max(getTurnThickness(opp, cw, dir, ii), getTurnThickness(acw, dir, dir, ii));
            int layer = left + right + 1;

            int minLength = ii ? minLengthDir : minLengthOpp;
            int length = lengths.get(i);

            totalLength = Math.max(totalLength + layer, length + minLength + layer);
            lengths.set(i, totalLength);
        }

        // last wire
        for (int i = 0; i < isInput.size(); i++) {
            boolean ii = isInput.get(i);

            int minLength = ii ? minLengthDir : minLengthOpp;
            int length = lengths.get(i);
            totalLength = Math.max(totalLength, length + minLength);
        }

        return totalLength;
    }

    @Override
    public boolean canShift(Direction dir, List<Boolean> isInput) {
        boolean input = false;
        boolean output = false;
        for (boolean b : isInput) {
            if (b) {
                input = true;
            } else {
                output = true;
            }
        }

        if (input && !canShift(dir)) {
            return false;
        }

        if (output && !canShift(dir.opposite())) {
            return false;
        }

        return true;
    }

    /**
     * Whether it's possible to shift in this direction (assuming it's going from input to output)
     */
    private boolean canShift(Direction dir) {
        Direction cw = dir.clockwise();
        Direction opp = dir.opposite();
        Direction acw = dir.anticlockwise();

        if (!wirer.canWire(dir)) {
            return false;
        }

        if (!wirer.canWire(acw)) {
            return false;
        }

        if (!wirer.canWire(cw)) {
            return false;
        }

        if (!turns.containsKey(ImmutableList.of(opp, acw))) {
            return false;
        }

        if (!turns.containsKey(ImmutableList.of(cw, dir))) {
            return false;
        }

        if (!turns.containsKey(ImmutableList.of(opp, cw))) {
            return false;
        }

        if (!turns.containsKey(ImmutableList.of(acw, dir))) {
            return false;
        }

        return true;
    }

    @Override
    public List<Integer> minSeparation(Direction dir, boolean isStart, List<Boolean> isInput) {
        Direction cw = dir.clockwise();
        Direction opp = dir.opposite();
        Direction acw = dir.anticlockwise();

        int wireDir = wirer.canWire(dir) ? wirer.minThickness(dir) : 0;
        int wireOpp = wirer.canWire(opp) ? wirer.minThickness(opp) : 0;

        ImmutableList.Builder<Integer> builder = ImmutableList.builder();

        for (int i = 0; i < isInput.size() - 1; i++) {
            boolean first = isInput.get(i);
            boolean second = isInput.get(i + 1);
            int firstSep = isStart ? getTurnThickness(opp, acw, cw, first) : getTurnThickness(acw, dir, cw, first);
            int secondSep = isStart ^ second ? wireOpp : wireDir;

            builder.add(firstSep + secondSep);
        }

        return builder.build();
    }

    private static void tryConnectWire(GadgetConfiguration config, GadgetConfiguration wire) {
        Preconditions.checkState(
            config.canConnect(new Location(0, 0), wire),"unexpected error: could not connect wire"
        );

        config.connect(new Location(0, 0), wire);
    }

    private static int tryConnectTurn(GadgetConfiguration config, Side s, Gadget turn, boolean isInput) {
        if (isInput) {
            Preconditions.checkState(
                config.canConnectInputPort(s, turn, 0),
                "unexpected error: could not connect turn"
            );

            return config.connectInputPort(s, turn, 0);
        } else {
            Preconditions.checkState(
                config.canConnectOutputPort(s, turn, 0),
                "unexpected error: could not connect turn"
            );

            return config.connectOutputPort(s, turn, 0);
        }
    }

    private Gadget getTurn(Direction input, Direction output) {
        return turns.get(ImmutableList.of(input, output));
    }

    /**
     * Gets the corresponding turn thickness. If inDir is false, then the direction of the
     * turn is reversed, so input and output are swapped.
     */
    private int getTurnThickness(Direction input, Direction output, Direction d, boolean inDir) {
        if (inDir) {
            return getTurnThickness(input, output, d);
        } else {
            return getTurnThickness(output, input, d);
        }
    }

    private int getTurnThickness(Direction input, Direction output, Direction d) {
        return getTurnThickness(getTurn(input, output), d);
    }

    private int getTurnThickness(Gadget turn, Direction d) {
        Side input = turn.getInput(0);
        Side output = turn.getOutput(0);
        if (d.perpendicular(input.getDirection())) {
            return GridUtils.countCellsInDir(turn, input.getLocation(), d);
        } else {
            return GridUtils.countCellsInDir(turn, output.getLocation(), d);
        }
    }

    private int getTurnSize(Direction input, Direction output, Direction d) {
        return GridUtils.getSize(getTurn(input, output), d);
    }

    /**
     * Calculates the minimum thickness for a wire running perpendicular to the shifting direction
     * (assuming each side has a turn gadget). Perp is the direction of the wire.
     */
    private int getPerpWireMinThickness(Direction dir, Direction perp, boolean isInput) {
        Direction opp = dir.opposite();
        Direction in = isInput ? opp : dir;
        Direction out = isInput ? dir : opp;

        int left =  Math.min(getTurnThickness(in, perp, opp), getTurnThickness(perp.opposite(), out, opp));
        int right = Math.min(getTurnThickness(in, perp, dir), getTurnThickness(perp.opposite(), out, dir));
        int turnThickness = Math.min(left, right);

        // Note: a wire should not overhang the edges of a turn gadget,
        // so one might think the turnThickness is always bigger than the minThickness, but
        // the left/right thickness for a wire could be uneven.
        return  Math.max(wirer.minThickness(perp), turnThickness);
    }

    /**
     * Calculates the minimum length for a wire running perpendicular to the shifting direction
     * (assuming each side has a turn gadget).
     */
    private int getPerpWireMinLength(Direction dir, Direction perp, boolean isInput) {
        return wirer.minLength(perp, getPerpWireMinThickness(dir, perp, isInput));
    }

    /**
     * Here, isFirst refers to either the first leg of the shift or the second
     */
    private Direction getPerp(Direction dir, boolean isFirst, boolean isInput) {
        Direction perp = dir.anticlockwise();
        if (!isFirst) {
            perp = perp.opposite();
        }

        if (!isInput) {
            perp = perp.opposite();
        }

        return perp;
    }

    private int getPerpWireMinThickness(Direction dir, boolean isFirst, boolean isInput) {
        return getPerpWireMinThickness(dir, getPerp(dir, isFirst, isInput), isInput);
    }

    private int getPerpWireMinLength(Direction dir, boolean isFirst, boolean isInput) {
        return getPerpWireMinLength(dir, getPerp(dir, isFirst, isInput), isInput);
    }

    public int getWireMinThickness(Direction dir, boolean isInput) {
        Direction d = isInput ? dir : dir.opposite();
        return wirer.minThickness(d);
    }

    /**
     * Gets a wire with a given length and with one given endpoint, either the input or the output.
     * Uses the minimum thickness possible.
     */
    private GadgetConfiguration getWire(Side start, boolean isInput, int length) {
        Direction dir = start.getDirection().opposite();
        if (isInput) {
            return wirer.wire(start, length, wirer.minThickness(dir));
        } else {
            return wirer.wire(start.add(dir, length).opposite(), length, wirer.minThickness(dir.opposite()));
        }
    }

    /**
     * Gets a wire perpendicular to the shift direction dir, with length and endpoint.
     */
    private GadgetConfiguration getPerpWire(Direction dir, Side start, boolean isFirst, boolean isInput, int length) {
        Direction perp = getPerp(dir, isFirst, isInput);
        int thickness = getPerpWireMinThickness(dir, perp, isInput);
        if (start.getDirection() != perp) {
            return wirer.wire(start, length, thickness);
        } else {
            Side end = start.add(start.getDirection().opposite(), length).opposite();
            return wirer.wire(end, length, thickness);
        }
    }

    private int minProtrusion(Direction dir, boolean isInput) {
        Direction opp = dir.opposite();
        Direction acw = dir.anticlockwise();
        Direction cw = dir.clockwise();

        Direction in = isInput ? opp : dir;
        Direction out = isInput ? dir : opp;


        int acwLength = getPerpWireMinLength(dir, acw, isInput);
        int cwLength = getPerpWireMinLength(dir, cw, isInput);

        int initACW = getTurnThickness(in, acw, acw) + acwLength + getTurnThickness(cw, out, cw);
        int initCW = getTurnThickness(acw, out, acw) + cwLength + getTurnThickness(in, cw, cw);

        int wire = Math.max(initACW, initCW);

        return wire + Math.max(getTurnThickness(cw, out, cw), getTurnThickness(in, cw, cw)) + 1;
    }
}
