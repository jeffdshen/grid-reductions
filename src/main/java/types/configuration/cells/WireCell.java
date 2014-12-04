package types.configuration.cells;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import types.Direction;

import java.util.List;

public class WireCell implements Cell{
    private static final ImmutableBiMap<Direction, WireCell> WIRES = constructWires();

    private final Direction d;

    private WireCell(Direction d) {
        this.d = d;
    }

    public static WireCell getWire(Direction d) {
        return WIRES.get(d);
    }

    private static ImmutableBiMap<Direction, WireCell> constructWires() {
        ImmutableBiMap.Builder<Direction, WireCell> builder = ImmutableBiMap.builder();
        for (Direction d : Direction.values()) {
            builder.put(d, new WireCell(d));
        }
        return builder.build();
    }

    @Override
    public int inputSize() {
        return 1;
    }

    @Override
    public int outputSize() {
        return 1;
    }

    @Override
    public List<Direction> getInputDirections() {
        return ImmutableList.of(d);
    }

    @Override
    public List<Direction> getOutputDirections() {
        return ImmutableList.of(d);
    }

    @Override
    public Direction getInputDirection(int i) {
        if (i < 0 || i >= 1) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        return d;
    }

    @Override
    public Direction getOutputDirection(int i) {
        if (i < 0 || i >= 1) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        return d;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPortNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellType getCellType() {
        return CellType.WIRE;
    }
}
