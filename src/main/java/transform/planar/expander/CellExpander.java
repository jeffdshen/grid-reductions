package transform.planar.expander;

import types.Direction;
import types.configuration.cells.Cell;

/**
 * Helps expand a grid by determining when two adjacent cells conflict,
 * and in the case of expansion, what new cell to insert in between
 */
public interface CellExpander {
    Cell expand(Direction d, Cell c1, Cell c2);
    boolean conflict(Direction d, Cell c1, Cell c2);
}
