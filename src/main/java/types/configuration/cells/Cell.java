package types.configuration.cells;

import types.Direction;

import java.util.List;

/**
 * A cell in a grid configuration. getName, getId are only be supported by Node and Port cells.
 * getPortNumber is only supported by Port cells. All directions refer to sides of this cell.
 */
public interface Cell {
    public int inputSize();
    public int outputSize();
    public Iterable<Direction> getInputDirections();
    public Iterable<Direction> getOutputDirections();
    public Direction getInputDirection(int i);
    public Direction getOutputDirection(int i);
    public String getName();
    public List<Integer> getId();
    public boolean isOutput(Direction d);
    public boolean isInput(Direction d);
    public int getPortNumber(Direction d);
    public CellType getCellType();
}
