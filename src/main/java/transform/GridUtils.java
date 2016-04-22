package transform;

import com.google.common.collect.ImmutableList;
import types.Direction;
import types.Grid;
import types.Location;

import java.util.List;
import java.util.Objects;

public class GridUtils {
    public static <E> int getSize(Grid<E> g, Direction dir) {
        Direction x = Direction.getDirection(1, 0);
        return dir.parallel(x) ? g.getSizeX() : g.getSizeY();
    }

    /**
     * Number of cells from this location to the edge of the grid directly in this direction
     */
    public static <E> int countCellsInDir(Grid<E> g, Location loc, Direction dir) {
        Direction x = Direction.getDirection(1, 0);
        int locX = dir.parallel(x) ? loc.getX() : loc.getY();
        int gX = dir.isPositive() ? (dir.parallel(x) ? g.getSizeX() - 1: g.getSizeY() - 1) : 0;

        return Math.abs(gX - locX);
    }

    public static <E> List<E> sliceX(Grid<E> g, int x) {
        ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (int y = 0; y < g.getSizeY(); y++) {
            builder.add(g.getCell(x, y));
        }
        return builder.build();
    }

    public static <E> List<E> sliceY(Grid<E> g, int y) {
        ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (int x = 0; x < g.getSizeX(); x++) {
            builder.add(g.getCell(x, y));
        }
        return builder.build();
    }

    public static String[][] toStringArray(Grid<String> grid) {
        String[][] cells = new String[grid.getSizeX()][grid.getSizeY()];

        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                cells[i][j] = grid.getCell(i, j);
            }
        }

        return cells;
    }

    public static <Cell> boolean equals(Grid<Cell> a, Grid<Cell> b) {
        if (a == null || b == null) {
            return a == null && b == null;
        }

        if (a.getSizeX() != b.getSizeX()) {
            return false;
        }

        if (a.getSizeY() != b.getSizeY()) {
            return false;
        }

        for (int i = 0; i < a.getSizeX(); i++) {
            for (int j = 0; j < a.getSizeY(); j++) {
                if (!Objects.equals(a.getCell(i, j), b.getCell(i, j))) {
                    return false;
                }
            }
        }

        return true;
    }
}
