package types.configuration.cells;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class EmptyCell implements Cell {
    private static final EmptyCell INSTANCE = new EmptyCell();

    public static EmptyCell getInstance() {
        return INSTANCE;
    }

    private EmptyCell() {}

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
        throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    public Direction getOutputDirection(int i) {
        throw new ArrayIndexOutOfBoundsException();
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
        return CellType.EMPTY;
    }
}
