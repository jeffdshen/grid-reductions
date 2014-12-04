package types.configuration.cells;

import types.Direction;

import java.util.List;

/**
 * A cell in a grid configuration. getName is only be supported by Node and Port cells.
 * getPortNumber is only supported by Port cells. The convention with directions is that an input direction
 * is the direction from the other cell to this cell, whereas the output direction is the direction is the direction
 * from this cell to the next cell. This way, two connected cell's input and output directions match.
 */
public interface Cell {
    public int inputSize();
    public int outputSize();
    public List<Direction> getInputDirections();
    public List<Direction> getOutputDirections();
    public Direction getInputDirection(int i);
    public Direction getOutputDirection(int i);
    public String getName();
    public int getPortNumber();
    public CellType getCellType();
}
