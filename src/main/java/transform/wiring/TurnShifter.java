package transform.wiring;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import transform.GadgetUtils;
import transform.GridUtils;
import types.Direction;
import types.Gadget;
import types.Location;
import types.Side;
import types.configuration.GadgetConfiguration;

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

    @Override
    public GadgetConfiguration shift(List<Side> input, List<Side> output, int length, int thickness) {
        GadgetConfiguration config = new GadgetConfiguration();
        int n = input.size();
        Preconditions.checkArgument(n == output.size(), "input and output sizes must be the same.");

        if (n == 0) {
            return config;
        }

        // get dir
        final Direction dir = input.get(0).opposite().getDirection();
        final Direction cw = dir.clockwise();
        final Direction opp = dir.opposite();
        final Direction acw = dir.anticlockwise();

        int minThickness = wirer.minThickness(dir);
        int minLength = wirer.minLength(dir, minThickness);
        int curLength = minLength;

        // do first layer
        ArrayList<Integer> endpoint = new ArrayList<>();
        int firstLayer = Math.max(getTurnThickness(opp, acw, opp), getTurnThickness(cw, dir, opp))
            + Math.max(getTurnThickness(opp, acw, dir), getTurnThickness(cw, dir, dir)) + 1;

        for (Side s : input) {
            GadgetConfiguration wire = wirer.wire(s, curLength, minThickness);
            tryConnectWire(config, wire);

            Preconditions.checkState(
                config.canConnectInputPort(s.add(dir, curLength).opposite(), getTurn(opp, acw), 0),
                "unexpected error: could not connect turn"
            );
            int id = config.connectInputPort(s.add(dir, curLength).opposite(), getTurn(opp, acw), 0);
            endpoint.add(id);
            curLength += firstLayer;
        }

        // do second layer
        int secondLayer = Math.max(getTurnThickness(opp, cw, opp), getTurnThickness(acw, dir, opp))
            + Math.max(getTurnThickness(opp, cw, dir), getTurnThickness(acw, dir, dir)) + 1;

        ArrayList<Integer> startpoint = new ArrayList<>();
        curLength = minLength;

        for (Side s : output) {
            GadgetConfiguration wire = wirer.wire(s.subtract(dir, curLength).opposite(), curLength, minThickness);

            tryConnectWire(config, wire);

            Preconditions.checkState(
                    config.canConnectOutputPort(s.subtract(dir, curLength).opposite(), getTurn(acw, dir), 0),
                    "unexpected error: could not connect turn"
            );
            int id = config.connectOutputPort(s.subtract(dir, curLength).opposite(), getTurn(acw, dir), 0);
            startpoint.add(id);
            curLength += secondLayer;
        }

        // do the middle section
        int minThicknessCW = wirer.minThickness(cw);
        int minLengthCW = wirer.minLength(cw, minThicknessCW);
        int minThicknessACW = wirer.minThickness(acw);
        int minLengthACW = wirer.minLength(acw, minThicknessACW);

        int minFirst = minLengthACW + getTurnThickness(cw, dir, cw);
        int minSecond = minLengthCW + getTurnThickness(opp, cw, cw);
        int midLayer = Math.max(getTurnThickness(cw, dir, cw), getTurnThickness(opp, cw, cw))
                + Math.max(getTurnThickness(cw, dir, acw), getTurnThickness(opp, cw, acw)) + 1;

        Side cur = null;
        for (int i = n - 1; i >= 0; i--) {
            Side first = config.getOutputSide(endpoint.get(i), 0);
            Side second = config.getInputSide(startpoint.get(i), 0);

            Side firstReq = first.add(acw, minFirst);
            Side secondReq = second.add(acw, minSecond);
            Side curReq = cur != null ? cur.add(acw, midLayer) : null;

            Ordering<Side> ordering = Ordering.from(new Comparator<Side>() {
                @Override
                public int compare(Side a, Side b) {
                    return a.dot(acw) - b.dot(acw);
                }
            }).nullsFirst();

            cur = ordering.max(firstReq, secondReq, curReq);

            int firstLength = cur.subtract(first.getLocation()).dot(acw) - getTurnThickness(cw, dir, cw);
//            System.out.println(minThickness);
//            System.out.println(first.opposite().getDirection());
            tryConnectWire(config, wirer.wire(first.opposite(), firstLength, minThicknessACW));

            int secondLength = cur.subtract(second.getLocation()).dot(acw) - getTurnThickness(opp, cw, cw);
            tryConnectWire(config, wirer.wire(second.add(acw, secondLength), secondLength, minThicknessCW));

            Preconditions.checkState(
                    config.canConnectInputPort(first.add(acw, firstLength), getTurn(cw, dir), 0),
                    "unexpected error: could not connect turn"
            );
            int idFirst = config.connectInputPort(first.add(acw, firstLength), getTurn(cw, dir), 0);

            Preconditions.checkState(
                    config.canConnectOutputPort(second.add(acw, secondLength), getTurn(opp, cw), 0),
                    "unexpected error: could not connect turn"
            );
            int idSecond = config.connectOutputPort(second.add(acw, secondLength), getTurn(opp, cw), 0);
            Side midFirst = config.getOutputSide(idFirst, 0);
            Side midSecond = config.getInputSide(idSecond, 0);
            int midLength = midSecond.subtract(midFirst.opposite().getLocation()).dot(dir);
            tryConnectWire(config, wirer.wire(midFirst.opposite(), midLength, minThickness));
        }

        return config;
    }

    private static void tryConnectWire(GadgetConfiguration config, GadgetConfiguration wire) {
        Preconditions.checkState(
                config.canConnect(new Location(0, 0), wire),"unexpected error: could not connect wire"
        );

        config.connect(new Location(0, 0), wire);
    }

    @Override
    public boolean canShift(Direction dir) {
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
    public int minSeparation(Direction dir) {
        int wire = wirer.minThickness(dir);

        Direction cw = dir.clockwise();
        Direction opp = dir.opposite();
        Direction acw = dir.anticlockwise();

        return wire + Math.max(getTurnThickness(opp, cw, acw), getTurnThickness(opp, acw, cw));
    }

    private Gadget getTurn(Direction input, Direction output) {
        return turns.get(ImmutableList.of(input, output));
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

    @Override
    public int minThickness(Direction dir, int wires) {
        // assume that the protrusion is anticlockwise, e.g. right -> up -> right -> down -> right
        Direction cw = dir.clockwise();
        Direction opp = dir.opposite();
        Direction acw = dir.anticlockwise();

        // non protruding side
        int max = getTurnThickness(opp, acw, cw);

        // protruding side
        int minLengthACW;
        {
            int left = Math.min(getTurnThickness(opp, acw, opp), getTurnThickness(cw, dir, opp));
            int right = Math.min(getTurnThickness(opp, acw, dir), getTurnThickness(cw, dir, dir));
            int turnThickness = Math.min(left, right);

            minLengthACW = wirer.minLength(acw, Math.max(wirer.minThickness(acw), turnThickness));
        }

        int minLengthCW;
        {
            int left = Math.min(getTurnThickness(opp, cw, dir), getTurnThickness(acw, dir, dir));
            int right = Math.min(getTurnThickness(opp, cw, opp), getTurnThickness(acw, dir, opp));
            int turnThickness = Math.min(left, right);

            minLengthCW = wirer.minLength(cw, Math.max(wirer.minThickness(cw), turnThickness));
        }

        int minSep = minSeparation(dir);

        // calculate for one protrusion
        int initialStart = getTurnThickness(opp, acw, acw) + minLengthACW + getTurnSize(cw, dir, acw);
        int initialEnd = getTurnThickness(acw, dir, acw) + minLengthCW + getTurnSize(opp, cw, acw);
        int size = Math.max(initialStart, initialEnd);

        // each additional protrusion adds
        int maxTurnThickness = Math.max(getTurnThickness(cw, dir, acw), getTurnThickness(opp, cw, acw));
        int per = wirer.minThickness(dir) + 1 + maxTurnThickness;

        // minimum separation subtracts some number, pick the bigger one
        if (per > minSep + 1) {
            return Math.max(max, size + (per - (minSep + 1)) * (wires - 1));
        } else {
            return Math.max(max, size);
        }
    }

    @Override
    public int minLength(Direction dir, int wires, int thickness) {
        // assume that the protrusion is anticlockwise, e.g. right -> up -> right -> down -> right
        Direction cw = dir.clockwise();
        Direction opp = dir.opposite();
        Direction acw = dir.anticlockwise();

        int minLength = wirer.minLength(dir, wirer.minThickness(dir));

        // each pair of turns in the first and second legs of the protrusion
        int firstLayer = Math.max(getTurnThickness(opp, acw, opp), getTurnThickness(cw, dir, opp))
            + Math.max(getTurnThickness(opp, acw, dir), getTurnThickness(cw, dir, dir)) + 1;
        int secondLayer = Math.max(getTurnThickness(opp, cw, opp), getTurnThickness(acw, dir, opp))
            + Math.max(getTurnThickness(opp, cw, dir), getTurnThickness(acw, dir, dir)) + 1;

        int min = 0;
        min += minLength;
        min += firstLayer * wires;
        min += minLength;
        min += secondLayer * wires;
        min += minLength;
        return min;
    }
}
