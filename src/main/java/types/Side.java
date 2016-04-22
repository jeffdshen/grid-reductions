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

    public int getX() {
        return loc.getX();
    }

    public int getY() {
        return loc.getY();
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

    public Side add(int x, int y){
        return new Side(loc.add(x, y), d);
    }

    public Side add(Location loc){
        return add(loc.getX(), loc.getY());
    }

    public Side add(Direction d){ return add(d.getX(), d.getY()); }

    public Side add(Direction d, int multiple) {
        return add(d.getX() * multiple, d.getY() * multiple);
    }

    public Side subtract(int x, int y) {
        return add(-x, -y);
    }

    public Side subtract(Location loc) {
        return subtract(loc.getX(), loc.getY());
    }

    public Side subtract(Direction dir) {
        return subtract(dir.getX(), dir.getY());
    }

    public Side subtract(Direction d, int multiple) {
        return subtract(d.getX() * multiple, d.getY() * multiple);
    }

    public int dot(Direction d) {
        return loc.dot(d);
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
