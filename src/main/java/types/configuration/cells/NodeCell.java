package types.configuration.cells;

import com.google.common.collect.ImmutableList;
import types.Direction;

import java.util.List;

public class NodeCell implements Cell {
    private final String name;
    private final ImmutableList<Integer> id;

    public NodeCell(String name, List<Integer> id) {
        this.name = name;
        this.id = ImmutableList.copyOf(id);
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
    public List<Integer> getId() {
        return id;
    }

    @Override
    public boolean isOutput(Direction d) {
        return false;
    }

    @Override
    public boolean isInput(Direction d) {
        return false;
    }

    @Override
    public int getPortNumber(Direction d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellType getCellType() {
        return CellType.NODE;
    }
}
