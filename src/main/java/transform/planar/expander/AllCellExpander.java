package transform.planar.expander;

import types.Direction;
import types.configuration.cells.Cell;

/**
 * Doubles the grid size
 */
public class AllCellExpander implements CellExpander {
    @Override
    public Cell expand(Direction d, Cell c1, Cell c2) {
        // expanding should be the same
        return new AreaCellExpander().expand(d, c1, c2);
    }

    @Override
    public boolean conflict(Direction d, Cell c1, Cell c2) {
        return true;
    }
}
