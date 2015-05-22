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
    private final static ImmutableMap<Direction, Direction> CLOCKWISE = constructClockwise();
    private final static ImmutableMap<Direction, Direction> ANTICLOCKWISE = constructAntiClockwise();

    /**
     * Gets the closest direction
     */
    public static Direction getClosestDirection(int x, int y) {
        if (x >= y) {
            return x >= -y ? EAST : NORTH;
        } else {
            return x >= -y ? SOUTH : WEST;
        }
    }

    private static ImmutableMap<Direction, Direction> constructOpposites() {
        return ImmutableMap.of(
                NORTH, SOUTH,
                SOUTH, NORTH,
                EAST, WEST,
                WEST, EAST
        );
    }

    private static ImmutableMap<Direction, Direction> constructClockwise() {
        return ImmutableMap.of(
                NORTH, EAST,
                SOUTH, WEST,
                EAST, SOUTH,
                WEST, NORTH
        );
    }
    private static ImmutableMap<Direction, Direction> constructAntiClockwise() {
        return ImmutableMap.of(
                NORTH, WEST,
                SOUTH, EAST,
                EAST, NORTH,
                WEST, SOUTH
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

    public Direction clockwise() {
        return CLOCKWISE.get(this);
    }

    public Direction anticlockwise() {
        return ANTICLOCKWISE.get(this);
    }

    public boolean perpendicular(Direction d) {
        return x * d.x + y * d.y == 0;
    }

    public boolean parallel(Direction d) {
        return !perpendicular(d);
    }
}
