package transform;

import com.google.common.collect.ImmutableList;
import types.Grid;

import java.util.List;
import java.util.Objects;

public class GridUtils {
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
