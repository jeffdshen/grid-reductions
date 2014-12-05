package transform;

import com.google.common.base.Preconditions;
import types.Direction;
import types.configuration.GridConfiguration;
import types.configuration.cells.*;

public class GridExpander {
    public void expand(GridConfiguration grid) {
        expandX(grid, true);
        expandX(grid, false);
    }

    /**
     * Expands one direction of the grid
     * @param grid
     * @param isX is expanding the x direction
     */
    private void expandX(GridConfiguration grid, boolean isX) {
        // flip meaning of x and y if not isX
        int sizeX = isX ? grid.getSizeX() : grid.getSizeY();
        int sizeY = isX ? grid.getSizeY() : grid.getSizeX();
        Direction dir = isX ? Direction.getClosestDirection(1, 0) : Direction.getClosestDirection(0, 1);
        boolean[] conflicts = new boolean[sizeX - 1];
        int count = 0;

        // determine which x-rows need to be expanded
        for (int i = 0; i < sizeX - 1; i++) {
            for (int j = 0; j < sizeY; j++) {
                Cell c1 = grid.getCell(i, j);
                Cell c2 = grid.getCell(i + dir.getX(), j + dir.getY());
                if (conflict(dir, c1, c2)) {
                    conflicts[i] = true;
                    count++;
                    break;
                }
            }
        }

        // new grid of cells
        Cell[][] cells = isX ? new Cell[sizeX + count][sizeY] : new Cell[sizeY][sizeX + count];
        int x = 0;
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                if (isX) {
                    cells[x][j] = grid.getCell(i, j);
                } else {
                    cells[j][x] = grid.getCell(j, i);
                }
            }
            x++;

            if (i < conflicts.length && conflicts[i]) {
                for (int j = 0; j < sizeY; j++) {
                    if (isX) {
                        cells[x][j] = expand(dir, grid.getCell(x, j), grid.getCell(x + dir.getX(), j + dir.getY()));
                    } else {
                        //noinspection SuspiciousNameCombination
                        cells[j][x] = expand(dir, grid.getCell(j, x), grid.getCell(j + dir.getX(), x + dir.getY()));
                    }
                }
                x++;
            }
        }
        grid.set(cells, isX ? sizeX + count : sizeY, isX ? sizeY : sizeX + count);
    }


    private boolean conflict(Direction d, Cell c1, Cell c2) {
        if (!isWireOrTurn(c1) && !isWireOrTurn(c2)) {
            return false;
        }
        switch (c1.getCellType()) {
            case EMPTY:
                return false;
            case WIRE:
            case TURN:
                // if connection, return false
                for (Direction dir : c1.getOutputDirections()) {
                    if (dir == d) {
                        return false;
                    }
                }

                for (Direction dir : c1.getInputDirections()) {
                    if (dir == d) {
                        return false;
                    }
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
    private Cell expand(Direction d, Cell c1, Cell c2) {
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
                        for (Direction dir : c1.getOutputDirections()) {
                            if (dir == d) {
                                return WireCell.getWire(d);
                            }
                        }

                        for (Direction dir : c1.getInputDirections()) {
                            if (dir == d) {
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
                        return new NodeCell(c1.getName());
                    default:
                        throw new IllegalArgumentException();
                }
            default:
                throw new IllegalArgumentException();
        }
    }
}
