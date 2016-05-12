package transform;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import types.Direction;
import types.Grid;
import types.Location;
import types.Side;
import types.configuration.GridConfiguration;
import types.configuration.cells.Cell;

import java.util.*;

public class GridUtils {
    public static <E> int getSize(Grid<E> g, Direction dir) {
        return dir.isX() ? g.getSizeX() : g.getSizeY();
    }

    /**
     * Number of cells from this location to the edge of the grid directly in this direction
     * Does not include the given cell, e.g. location (0, 0) in the direction (0, 1) would give g.getSizeY() - 1.
     */
    public static <E> int countCellsInDir(Grid<E> g, Location loc, Direction dir) {
        int locX = dir.isX() ? loc.getX() : loc.getY();
        int gX = dir.isPositive() ? (dir.isX() ? g.getSizeX() - 1: g.getSizeY() - 1) : 0;

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

    /**
     * Gets the defining corner of a grid
     */
    public static <E> Location getCorner(Grid<E> g) {
        return new Location(g.getSizeX() - 1, g.getSizeY() - 1);
    }

    /**
     * Returns the boundary formed by the following rectangle in clockwise order starting from start.
     * @param start the side to start at
     * @param opp the opposite corner
     * @return the sides forming the boundary of this rectangle
     */
    public static List<Side> getBoundary(Side start, Location opp) {
        ImmutableList.Builder<Side> builder = ImmutableList.builder();

        Direction d = start.getDirection().clockwise();
        Side current = start;
        for (; opp.subtract(current.getLocation()).dot(d) >= 0; current = current.add(d)) {
            builder.add(current);
        }

        current = current.subtract(d).clockwise();
        d = d.clockwise();
        for (; opp.subtract(current.getLocation()).dot(d) >= 0; current = current.add(d)) {
            builder.add(current);
        }

        current = current.subtract(d).clockwise();
        d = d.clockwise();
        for (; start.subtract(current.getLocation()).dot(d) >= 0; current = current.add(d)) {
            builder.add(current);
        }

        current = current.subtract(d).clockwise();
        d = d.clockwise();
        for (; start.subtract(current.getLocation()).dot(d) >= 0; current = current.add(d)) {
            builder.add(current);
        }

        return builder.build();
    }

    // TODO make immutable
    public static Map<Direction, List<Side>> getBoundaryAsMap(Side start, Location opp) {
        Map<Direction, List<Side>> sides = new HashMap<>();
        for (Direction d : Direction.values()) {
            sides.put(d, new ArrayList<Side>());
        }

        List<Side> boundary = getBoundary(start, opp);
        for (Side s : boundary) {
            sides.get(s.getDirection()).add(s);
        }

        return sides;
    }

    // TODO make immutable
    public static Map<Direction, List<Side>> getPorts(GridConfiguration config, Side start, Location end) {
        HashMap<Direction, List<Side>> gridPorts = new HashMap<>();
        for (Direction d : Direction.values()) {
            gridPorts.put(d, new ArrayList<Side>());
        }

        for (Side s : GridUtils.getBoundary(start, end)) {
            Cell cell = config.getCell(s.getLocation());
            if (cell.isInput(s.getDirection()) || cell.isOutput(s.getDirection())) {
                gridPorts.get(s.getDirection()).add(s);
            }
        }

        return gridPorts;
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
