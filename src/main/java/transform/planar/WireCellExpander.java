package transform.planar;

import types.Direction;
import types.configuration.cells.Cell;
import types.configuration.cells.CellType;
import types.configuration.cells.EmptyCell;
import types.configuration.cells.WireCell;

/**
 * Ensures every connection is separated by a wire.
 */
public class WireCellExpander implements CellExpander {
    @Override
    public Cell expand(Direction d, Cell c1, Cell c2) {
        if (c1.isOutput(d)) {
            return WireCell.getWire(d);
        } else if (c1.isInput(d)) {
            return WireCell.getWire(d.opposite());
        }

        return EmptyCell.getInstance();
    }

    @Override
    public boolean conflict(Direction d, Cell c1, Cell c2) {
        // if connection, return whether or not one of the cells is a wire
        if (c1.isOutput(d)) {
            return !(c1.getCellType() == CellType.WIRE || c2.getCellType() == CellType.WIRE);
        }

        //noinspection SimplifiableIfStatement
        if (c1.isInput(d)) {
            return !(c1.getCellType() == CellType.WIRE || c2.getCellType() == CellType.WIRE);
        }

        return false;
    }
}
