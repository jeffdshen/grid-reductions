package transform.planar;

import types.Direction;
import types.configuration.GridConfiguration;
import types.configuration.cells.Cell;

public class GridExpander {
    private AreaCellExpander area;
    private WireCellExpander wire;
    public GridExpander() {
        area = new AreaCellExpander();
        wire = new WireCellExpander();
    }

    /**
     * Last step of expansion, only call after everything else is done.
     */
    public void expandLast(GridConfiguration grid) {
        expandX(wire, grid, true);
        expandX(wire, grid, false);
    }

    public void expand(GridConfiguration grid) {
        expandX(area, grid, true);
        expandX(area, grid, false);
    }

    /**
     * Expands one direction of the grid
     * @param grid the grid
     * @param isX is expanding the x direction
     */
    private void expandX(CellExpander ex, GridConfiguration grid, boolean isX) {
        // flip meaning of x and y if not isX
        int sizeX = isX ? grid.getSizeX() : grid.getSizeY();
        int sizeY = isX ? grid.getSizeY() : grid.getSizeX();
        Direction dir = isX ? Direction.getDirection(1, 0) : Direction.getDirection(0, 1);
        boolean[] conflicts = new boolean[sizeX - 1];
        int count = 0;

        // determine which x-rows need to be expanded
        for (int i = 0; i < sizeX - 1; i++) {
            for (int j = 0; j < sizeY; j++) {
                int x = isX ? i : j;
                int y = isX ? j : i;
                Cell c1 = grid.getCell(x, y);
                Cell c2 = grid.getCell(x + dir.getX(), y + dir.getY());
                if (ex.conflict(dir, c1, c2)) {
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
                        cells[x][j] = ex.expand(dir, grid.getCell(i, j), grid.getCell(i + dir.getX(), j + dir.getY()));
                    } else {
                        //noinspection SuspiciousNameCombination
                        cells[j][x] = ex.expand(dir, grid.getCell(j, i), grid.getCell(j + dir.getX(), i + dir.getY()));
                    }
                }
                x++;
            }
        }
        grid.set(cells, isX ? sizeX + count : sizeY, isX ? sizeY : sizeX + count);
    }

}
