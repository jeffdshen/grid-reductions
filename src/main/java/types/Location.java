package types;

/**
 * Created by jdshen on 11/25/14.
 */
public class Location {
    private final int x;
    private final int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Location offset(int offsetX, int offsetY){
        return new Location(x + offsetX, y + offsetY);
    }
    public Location offset(Location offset){
        return offset(offset.x, offset.y);
    }
    public Location offset(Direction d){ return offset(d.getX(), d.getY()); }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Location) {
            Location that = (Location) o;
            return (x == that.x) && (y == that.y);
        }
        return super.equals(o);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.x;
        hash = 71 * hash + this.y;
        return hash;
    }

    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + "]";
    }
}
