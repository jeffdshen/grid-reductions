package transform.placer;

import com.google.common.base.Preconditions;
import transform.lp.Constraint;
import types.Direction;
import types.Location;
import types.Side;

import static transform.lp.ConstraintFactory.atLeast;

public class PlacerUtils {
    public static int roundAndCheck(double d) {
        long num = Math.round(d);
//        Preconditions.checkState(Math.abs(d - num) < 0.1, d);
        Preconditions.checkState(num < 5000 && num >= 0, "Offset out of bounds");
        return (int) num;
    }


    /**
     * Compares the two sides along the following direction
     */
    public static int compareTo(Side a, Side b, Direction d) {
        if (a.dot(d) != b.dot(d)) {
            return a.dot(d) - b.dot(d);
        }

        return a.getDirection().dot(d) - b.getDirection().dot(d);
    }

    /**
     * Returns a constraint that specifies that the sides are separated by at least length along the given axis.
     */
    public static Constraint lengthAtLeast(Side s1, Side s2, Direction d, int length) {
        Direction dir = d.isPositive() ? d : d.opposite();
        int compare = compareTo(s1, s2, dir);
        Preconditions.checkArgument(compare != 0, "Can't constrain equivalent sides in this direction");
        Side a = compare < 0 ? s1 : s2; // smaller
        Side b = compare < 0 ? s2 : s1; // larger
        if (dir.isX()) {
            return atLeast(getSide(b).x, getSide(a).x, length);
        } else {
            return atLeast(getSide(b).y, getSide(a).y, length);
        }
    }

    /**
     * Returns a constraint that specifies that the distance from LocationID a
     * to LocationID b in the given direction is at least a certain length.
     *
     * Note: Here, the sign of the direction matters since the direction from a to b has a
     * component in the given direction.
     */
    public static Constraint lengthAtLeast(LocationID a, LocationID b, Direction d, int length) {
        if (d.isPositive()) {
            if (d.isY()) {
                return atLeast(b.y, a.y, length);
            } else {
                return atLeast(b.x, a.x, length);
            }
        } else {
            if (d.isY()) {
                return atLeast(a.y, b.y, length);
            } else {
                return atLeast(a.x, b.x, length);
            }
        }
    }

    /**
     * formats:
     * side [x] [y] [d]
     * x [x]
     * y [y]
     * gadgetX [x] [y]
     * gadgetY [x] [y]
     */
    public static LocationID getSide(Side side) {
        Location loc = side.getLocation();
        Direction dir = side.getDirection();
        return new LocationID("side", loc.getX(), loc.getY(), dir.ordinal());
    }

    public static LocationID getSlice(int slice) {
        return new LocationID("slice", slice);
    }

    public static String getSlice(Side side) {
        Side s = side;
        if (s.getDirection().isPositive()) {
            s = s.opposite();
        }

        if (s.getDirection().isY()) {
            return getSlice(s.getY()).y;
        } else {
            return getSlice(s.getX()).x;
        }
    }

    public static LocationID getGadget(int x, int y) {
        return new LocationID("gadget", x, y);
    }
}
