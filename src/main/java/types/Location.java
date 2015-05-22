package types;

public class Location {
    private final int x;
    private final int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Location add(int x, int y){
        return new Location(this.x + x, y + this.y);
    }
    public Location add(Location loc){
        return add(loc.x, loc.y);
    }
    public Location add(Direction d) {
        return add(d.getX(), d.getY());
    }

    public Location subtract(int x, int y) {
        return add(-x, -y);
    }

    public Location subtract(Location loc) {
        return subtract(loc.getX(), loc.getY());
    }

    public Location subtract(Direction dir) {
        return subtract(dir.getX(), dir.getY());
    }

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
