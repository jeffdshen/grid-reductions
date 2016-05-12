package types.configuration.cells;

import types.Direction;

import java.util.List;

/**
 * A cell in a grid configuration. getName, getId are only be supported by Node and Port cells.
 * getPortNumber is only supported by Port cells. All directions refer to sides of this cell.
 */
public interface Cell {
    int inputSize();
    int outputSize();
    Iterable<Direction> getInputDirections();
    Iterable<Direction> getOutputDirections();
    Direction getInputDirection(int i);
    Direction getOutputDirection(int i);
    String getName();
    List<Integer> getId();
    boolean isOutput(Direction d);
    boolean isInput(Direction d);
    int getPortNumber(Direction d);
    CellType getCellType();
}
