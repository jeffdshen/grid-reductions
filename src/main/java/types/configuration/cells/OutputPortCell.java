package types.configuration.cells;

import com.google.common.collect.ImmutableList;
import types.Direction;

import java.util.List;

public class OutputPortCell implements Cell{
    private final Direction d;
    private final String name;
    private final int port;

    public OutputPortCell(Direction d, String name, int port) {
        this.d = d;
        this.name = name;
        this.port = port;
    }

    @Override
    public int inputSize() {
        return 0;
    }

    @Override
    public int outputSize() {
        return 1;
    }

    @Override
    public List<Direction> getInputDirections() {
        return ImmutableList.of();
    }

    @Override
    public List<Direction> getOutputDirections() {
        return ImmutableList.of(d);
    }

    @Override
    public Direction getInputDirection(int i) {
        throw new ArrayIndexOutOfBoundsException(i);
    }

    @Override
    public Direction getOutputDirection(int i) {
        return d;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPortNumber() {
        return port;
    }

    @Override
    public CellType getCellType() {
        return CellType.OUTPUT_PORT;
    }
}
