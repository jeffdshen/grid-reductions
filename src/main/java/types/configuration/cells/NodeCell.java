package types.configuration.cells;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class NodeCell implements Cell {
    private final String name;

    public NodeCell(String name) {
        this.name = name;
    }

    @Override
    public int inputSize() {
        return 0;
    }

    @Override
    public int outputSize() {
        return 0;
    }

    @Override
    public List<Direction> getInputDirections() {
        return ImmutableList.of();
    }

    @Override
    public List<Direction> getOutputDirections() {
        return ImmutableList.of();
    }

    @Override
    public Direction getInputDirection(int i) {
        throw new ArrayIndexOutOfBoundsException(i);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public CellType getCellType() {
        return CellType.NODE;
    }
}
