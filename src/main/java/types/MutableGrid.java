package types;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import transform.GridUtils;
import types.configuration.CellConfiguration;

public class MutableGrid<Cell> implements Grid<Cell> {
    private Cell background;
    private Cell[][] cells;
    private int sizeX;
    private int sizeY;

    public MutableGrid(Cell background, int initialSizeX, int initialSizeY) {
        this.background = background;
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
        resize(background, x, y);
    }

    public void resize(Cell background, int x, int y) {
        Preconditions.checkArgument(x >= sizeX);
        Preconditions.checkArgument(y >= sizeY);
        Cell[][] newCells = getCellCopies(background, x, y);
        put(newCells, this, 0, 0);
        this.cells = newCells;
        this.sizeX = x;
        this.sizeY = y;
    }

    /**
     * Expands all sides, and recenters
     */
    public void expand(int x, int y) {
        MutableGrid<Cell> newGrid = new MutableGrid<>(background, getSizeX() + 2 * x, getSizeY() + 2 * y);
        newGrid.put(this, x + 1, y + 1);
        this.set(newGrid.cells, newGrid.getSizeX(), newGrid.getSizeY());
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

    public void put(Grid<Cell> grid, Location loc) {
        put(this.cells, grid, loc.getX(), loc.getY());
    }

    public void put(Grid<Cell> grid, int x, int y) {
        put(this.cells, grid, x, y);
    }

    public void put(Cell cell, Location loc) {
        put(cell, loc.getX(), loc.getY());
    }

    public void put(Cell cell, int x, int y) {
        this.cells[x][y] = cell;
    }

    private Cell[][] getCellCopies(Cell original, int x, int y) {
        @SuppressWarnings("unchecked")
        Cell[][] cells = (Cell[][]) new Object[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                cells[i][j] = original;
            }
        }
        return cells;
    }

    private void put(Cell[][] cells, Grid<Cell> grid, int x, int y) {
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                cells[x + i][y + j] = grid.getCell(i, j);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sizeY; i++) {
            builder.append(Joiner.on("t").join(GridUtils.sliceY(this, i)));
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
}
