package types;

import com.google.common.base.Preconditions;

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

    public void put(MutableGrid<Cell> grid, Location loc) {
        put(this.cells, grid, loc.getX(), loc.getY());
    }

    public void put(MutableGrid<Cell> grid, int x, int y) {
        put(this.cells, grid, x, y);
    }

    public void put(Cell cell, Location loc) {
        this.cells[loc.getX()][loc.getY()] = cell;
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

    private void put(Cell[][] cells, MutableGrid<Cell> grid, int x, int y) {
        for (int i = 0; i < grid.sizeX; i++) {
            System.arraycopy(grid.cells[i], 0, cells[x + i], y, grid.sizeY);
        }
    }
}
