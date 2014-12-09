package types.configuration;

import com.google.common.base.Preconditions;
import types.Direction;
import types.Grid;
import types.Location;
import types.configuration.cells.Cell;
import types.configuration.cells.EmptyCell;

public class GridConfiguration implements Grid {
    private Cell[][] cells;
    private int sizeX;
    private int sizeY;

    public GridConfiguration(int initialSizeX, int initialSizeY) {
        this(EmptyCell.getInstance(), initialSizeX, initialSizeY);
    }

    public GridConfiguration(Cell background, int initialSizeX, int initialSizeY) {
        this.cells = getCellCopies(background, initialSizeX, initialSizeY);
        this.sizeX = initialSizeX;
        this.sizeY = initialSizeY;
    }

    public void set(Cell[][] cells, int sizeX, int sizeY) {
        this.cells = cells;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public void resize(int x, int y) {
        resize(EmptyCell.getInstance(), x, y);
    }

    public void resize(Cell background, int x, int y) {
        Preconditions.checkArgument(x >= sizeX);
        Preconditions.checkArgument(y >= sizeY);
        Cell[][] newCells = getCellCopies(background, x, y);
        put(newCells, this, 0, 0);
        this.cells = newCells;
    }

    @Override
    public boolean isValid(Location loc) {
        return isValid(loc.getX(), loc.getY());
    }

    @Override
    public boolean isValid(int x, int y) {
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY;
    }

    @Override
    public int getSizeX() {
        return sizeX;
    }

    @Override
    public int getSizeY() {
        return sizeY;
    }

    @Override
    public Cell getCell(Location loc) {
        return cells[loc.getX()][loc.getY()];
    }

    @Override
    public Cell getCell(int x, int y) {
        return cells[x][y];
    }

    public void put(GridConfiguration grid, Location loc) {
        put(this.cells, grid, loc.getX(), loc.getY());
    }

    public void put(GridConfiguration grid, int x, int y) {
        put(this.cells, grid, x, y);
    }

    public void put(Cell cell, Location loc) {
        this.cells[loc.getX()][loc.getY()] = cell;
    }

    private static Cell[][] getCellCopies(Cell original, int x, int y) {
        Cell[][] cells = new Cell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                cells[i][j] = original;
            }
        }
        return cells;
    }

    private static void put(Cell[][] cells, GridConfiguration grid, int x, int y) {
        for (int i = 0; i < grid.sizeX; i++) {
            System.arraycopy(grid.cells[i], 0, cells[x + i], y, grid.sizeY);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("GridConfiguration(sizeX=%s, sizeY=%s, cells=\n", sizeX, sizeY));
        char[][] grid = new char[sizeX * 3][sizeY * 3];

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                Cell cell = cells[i][j];
                int x = i * 3 + 1;
                int y = j * 3 + 1;

                // set center
                grid[x][y] = cell.getCellType().name().charAt(0);

                // set corners
                grid[x - 1][y - 1] = grid[x + 1][y + 1] = grid[x + 1][y - 1] = grid[x - 1][y + 1] = ' ';

                // set sides
                for (Direction dir : Direction.values()) {
                    char c = '-';
                    if (dir == Direction.NORTH || dir == Direction.SOUTH) {
                        c = '|';
                    }
                    grid[x + dir.getX()][y + dir.getY()] = cell.isInput(dir) || cell.isOutput(dir) ? c : ' ';
                }
            }
        }

        // print transposed
        for (int i = 0; i < sizeY * 3; i++) {
            for (int j = 0; j < sizeX * 3; j++) {
                builder.append(grid[j][i]);
            }
        }
        builder.append(")");
        return builder.toString();
    }
}
