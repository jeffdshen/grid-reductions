package types;

import com.google.common.collect.ImmutableMap;

public enum Direction {
    NORTH(0, -1),
    SOUTH(0, 1),
    EAST(1, 0),
    WEST(-1 , 0);

    private final int x;
    private final int y;

    private final static ImmutableMap<Direction, Direction> OPPOSITES = constructOpposites();

    private static ImmutableMap<Direction, Direction> constructOpposites() {
        return ImmutableMap.of(
                NORTH, SOUTH,
                SOUTH, NORTH,
                EAST, WEST,
                WEST, EAST
        );
    }

    private Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Direction opposite() {
        return OPPOSITES.get(this);
    }
}
