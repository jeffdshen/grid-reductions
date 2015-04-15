package types;

/**
 * The side of the square denoted by the location
 */
public class Side {
    private final Location loc;
    private final Direction d;

    public Side(int x, int y, Direction d) {
        this(new Location(x, y), d);
    }

    public Side(Location loc, Direction d) {
        this.loc = loc;
        this.d = d;
    }

    public Location getLocation() {
        return loc;
    }

    public Direction getDirection() {
        return d;
    }

    public Side opposite() {
        return new Side(loc.add(d), d.opposite());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + loc.hashCode();
        hash = 71 * hash + d.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Side) {
            Side that = (Side) o;
            return (this.loc.equals(that.loc)) && (this.d == that.d);
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[loc=" + loc + ",d=" + d + "]";
    }
}
