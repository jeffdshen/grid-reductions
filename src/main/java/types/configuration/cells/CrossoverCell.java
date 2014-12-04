package types.configuration.cells;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import types.Direction;

import java.util.List;

public class CrossoverCell implements Cell {
    private final List<Direction> dirs;

    public CrossoverCell(Direction d1, Direction d2) {
        Preconditions.checkArgument(d1 != d2);
        Preconditions.checkArgument(d1.opposite() != d2);
        dirs = ImmutableList.of(d1, d2);
    }

    @Override
    public int inputSize() {
        return 2;
    }

    @Override
    public int outputSize() {
        return 2;
    }

    @Override
    public List<Direction> getInputDirections() {
        return dirs;
    }

    @Override
    public List<Direction> getOutputDirections() {
        return dirs;
    }

    @Override
    public Direction getInputDirection(int i) {
        return dirs.get(i);
    }

    @Override
    public Direction getOutputDirection(int i) {
        return dirs.get(i);
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
        return CellType.CROSSOVER;
    }
}
