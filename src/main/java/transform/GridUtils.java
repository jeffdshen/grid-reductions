package transform;

import com.google.common.collect.ImmutableList;
import types.Grid;

import java.util.List;

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
}
