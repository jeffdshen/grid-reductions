package types;

import com.google.common.base.Joiner;
import transform.GridUtils;

public class SubGrid<E> implements Grid<E> {
    private final Grid<E> grid;
    private final Location start;
    private final int sizeX;
    private final int sizeY;

    public SubGrid(Grid<E> grid, Location start, int sizeX, int sizeY) {
        this.grid = grid;
        this.start = start;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    @Override
    public boolean isValid(Location loc) {
        return isValid(loc.getX(), loc.getY());
    }

    @Override
    public boolean isValid(int x, int y) {
        return x >= 0 && y >= 0 && x < sizeX && y < sizeY;
    }

    @Override
    public E getCell(Location loc) {
        return getCell(loc.getX(), loc.getY());
    }

    @Override
    public E getCell(int x, int y) {
        int xx = x + start.getX();
        int yy = y + start.getY();
        return grid.getCell(xx, yy);
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sizeY; i++) {
            builder.append(Joiner.on(" ").join(GridUtils.sliceY(this, i)));
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
}
