package transform.planar;

import com.google.common.base.Preconditions;
import types.Direction;
import types.configuration.cells.*;

/**
 * Ensures non-zero area for regions
 */
public class AreaCellExpander implements CellExpander{
    public boolean conflict(Direction d, Cell c1, Cell c2) {
        if (!isWireOrTurn(c1) && !isWireOrTurn(c2)) {
            return false;
        }

        if (c1.getCellType() == CellType.EMPTY || c2.getCellType() == CellType.EMPTY) {
            return false;
        }

        switch (c1.getCellType()) {
            case WIRE:
            case TURN:
                // if connection, return false
                if (c1.isOutput(d)) {
                    return false;
                }

                if (c1.isInput(d)) {
                    return false;
                }

                // otherwise, they conflict
                return true;
            case CROSSOVER:
            case NODE:
            case PORT:
                return conflict(d.opposite(), c2, c1);
            default:
                throw new IllegalArgumentException();
        }
    }

    private boolean isWireOrTurn(Cell c) {
        CellType type = c.getCellType();
        return type == CellType.WIRE || type == CellType.TURN;
    }

    /**
     * When a cell is added between two cells, expand returns what cell it should be.
     * This method may or may not catch errors in the set up - e.g. a wire going into a turn on the wrong side.
     * @param d the direction from cell c1 to cell c2
     * @return the added cell
     */
    public Cell expand(Direction d, Cell c1, Cell c2) {
        switch (c1.getCellType()) {
            case EMPTY:
                return EmptyCell.getInstance();
            case WIRE:
            case TURN:
            case CROSSOVER:
                switch (c2.getCellType()) {
                    case EMPTY:
                    case NODE:
                        return EmptyCell.getInstance();
                    case WIRE:
                    case TURN:
                    case CROSSOVER:
                    case PORT:
                        // if connection, return a wire
                        if (c1.isOutput(d)) {
                            return WireCell.getWire(d);
                        }

                        if (c1.isInput(d)) {
                            return WireCell.getWire(d.opposite());
                        }

                        // otherwise, add an empty slot
                        return EmptyCell.getInstance();
                    default:
                        throw new IllegalArgumentException();
                }
            case NODE:
                switch (c2.getCellType()) {
                    case EMPTY:
                    case WIRE:
                    case TURN:
                    case CROSSOVER:
                        return expand(d.opposite(), c2, c1);
                    case NODE:
                    case PORT:
                        return c1;
                    default:
                        throw new IllegalArgumentException();
                }
            case PORT:
                switch (c2.getCellType()) {
                    case EMPTY:
                    case WIRE:
                    case TURN:
                    case CROSSOVER:
                    case NODE:
                        return expand(d.opposite(), c2, c1);
                    case PORT:
                        Preconditions.checkArgument(c1.getName().equals(c2.getName()));
                        return new NodeCell(c1.getName(), c1.getId());
                    default:
                        throw new IllegalArgumentException();
                }
            default:
                throw new IllegalArgumentException();
        }
    }
}
