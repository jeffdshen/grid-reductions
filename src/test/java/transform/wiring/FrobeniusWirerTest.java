package transform.wiring;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;
import parser.GadgetParser;
import types.Direction;
import types.Gadget;
import types.Grid;
import types.Side;
import utils.ResourceUtils;

import static org.testng.Assert.*;

/**
 * Created by jdshen on 5/29/15.
 */
public class FrobeniusWirerTest {
    @Test
    public void testConnect() throws Exception {
        GadgetParser parser = new GadgetParser();
        String dir = "types/gadget/circuit/";
        Gadget and = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "and.txt"));
        Gadget wire2 = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "wire_2.txt"));
        Gadget wire3 = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "wire_3.txt"));
        Gadget wire5 = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "wire_5.txt"));
        Gadget wire7 = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "wire_7.txt"));
        Gadget empty = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "empty.txt"));
        Gadget variable = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "variable.txt"));
        Wirer wirer = new FrobeniusWirer(ImmutableList.of(wire2, wire3, wire5, wire7));
        assertEquals(wirer.minWidth(Direction.EAST), 1);
        assertEquals(wirer.minLength(Direction.EAST, 1), 24);
        assertEquals(wirer.minLength(Direction.EAST, 5), 2);
        assertEquals(wirer.minLength(Direction.EAST, 3), 2);
        Grid<String> grid = wirer.wire(new Side(-1, 1, Direction.EAST), 8, 3).toGrid(empty.getCell(0, 0));
        assertEquals(grid.getSizeX(), 8);
        assertEquals(grid.getSizeY(), 3);
        Grid<String> grid2 = wirer.wire(new Side(-1, 1, Direction.EAST), 24, 2).toGrid(empty.getCell(0, 0));
        assertEquals(grid2.getSizeX(), 24);
        assertEquals(grid2.getSizeY(), 2);
    }
}
