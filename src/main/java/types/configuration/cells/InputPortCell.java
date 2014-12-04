package types.configuration.cells;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class InputPortCell implements Cell {
    private final Direction d;
    private final String name;
    private final int port;

    public InputPortCell(Direction d, String name, int port) {
        this.d = d;
        this.name = name;
        this.port = port;
    }

    @Override
    public int inputSize() {
        return 1;
    }

    @Override
    public int outputSize() {
        return 0;
    }

    @Override
    public List<Direction> getInputDirections() {
        return ImmutableList.of(d);
    }

    @Override
    public List<Direction> getOutputDirections() {
        return ImmutableList.of();
    }

    @Override
    public Direction getInputDirection(int i) {
        return d;
    }

    @Override
    public Direction getOutputDirection(int i) {
        throw new ArrayIndexOutOfBoundsException(i);
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
        return CellType.INPUT_PORT;
    }
}
