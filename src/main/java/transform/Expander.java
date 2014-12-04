package transform;

import com.google.common.base.Preconditions;
import types.Direction;
import types.configuration.cells.Cell;
import types.configuration.cells.EmptyCell;
import types.configuration.cells.NodeCell;
import types.configuration.cells.WireCell;

public class Expander {
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
                    case INPUT_PORT:
                    case OUTPUT_PORT:
                        // if connection, add a wire
                        for (int i = 0; i < c1.outputSize(); i++) {
                            if (c1.getOutputDirection(i) == d) {
                                return WireCell.getWire(d);
                            }
                        }
                        for (int i = 0; i < c1.inputSize(); i++) {
                            if (c1.getInputDirection(i) == d.opposite()) {
                                return WireCell.getWire(d.opposite());
                            }
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
                    case INPUT_PORT:
                    case OUTPUT_PORT:
                        return c1;
                    default:
                        throw new IllegalArgumentException();
                }
            case INPUT_PORT:
            case OUTPUT_PORT:
                switch (c2.getCellType()) {
                    case EMPTY:
                    case WIRE:
                    case TURN:
                    case CROSSOVER:
                    case NODE:
                        return expand(d.opposite(), c2, c1);
                    case INPUT_PORT:
                    case OUTPUT_PORT:
                        Preconditions.checkArgument(c1.getName().equals(c2.getName()));
                        return new NodeCell(c1.getName());
                    default:
                        throw new IllegalArgumentException();
                }
            default:
                throw new IllegalArgumentException();
        }
    }
}
