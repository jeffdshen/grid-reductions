package transform;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;
import types.Location;
import types.Side;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static types.Direction.*;

public class GridUtilsTest {
    @Test
    public void testGetBoundary() throws Exception {
        List<Side> expected = ImmutableList.of(
            new Side(-1, 3, NORTH),
            new Side(0, 3, NORTH),
            new Side(0, 3, EAST),
            new Side(0, 4, EAST),
            new Side(0, 5, EAST),
            new Side(0, 5, SOUTH),
            new Side(-1, 5, SOUTH),
            new Side(-1, 5, WEST),
            new Side(-1, 4, WEST),
            new Side(-1, 3, WEST)
        );
        List<Side> actual = GridUtils.getBoundary(new Side(-1, 3, NORTH), new Location(0, 5));
        assertEquals(actual, expected);
    }

}