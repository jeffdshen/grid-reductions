package transform.placer;

import com.google.common.base.Joiner;

public class LocationID {
    public final String x;
    public final String y;

    public LocationID(String s, Object... objects) {
        this.x = s + "x:" + Joiner.on("-").join(objects);
        this.y = s + "y:" + Joiner.on("-").join(objects);
    }
}
