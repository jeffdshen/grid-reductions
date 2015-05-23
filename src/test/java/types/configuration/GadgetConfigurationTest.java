package types.configuration;

import org.testng.annotations.Test;
import parser.GadgetParser;
import transform.GridUtils;
import types.Direction;
import types.Gadget;
import types.Grid;
import types.Side;
import utils.ResourceUtils;

import static org.testng.Assert.*;

public class GadgetConfigurationTest {
    @Test
    public void testConnect() throws Exception {
        GadgetParser parser = new GadgetParser();
        String dir = "types/gadget/circuit/";
        Gadget and = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "and.txt"));
        Gadget wire = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "wire.txt"));
        Gadget wire2 = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "wire2.txt"));
        Gadget empty = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "empty.txt"));
        Gadget longAnd = parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "and.txt"));

        String emptyCell = empty.getCell(0, 0);
        GadgetConfiguration g = new GadgetConfiguration();

        Side start = new Side(0, 1, Direction.WEST).opposite();

        assertTrue(g.canConnectInputPort(start, wire, 0));
        int g0 = g.connectInputPort(start, wire, 0);

        assertTrue(g.canConnectInputPort(g0, 0, wire2, 0));
        int g1 = g.connectInputPort(g0, 0, wire2, 0);

        assertTrue(g.canConnectInputPort(g1, 0, and, 0));
        int g2 = g.connectInputPort(g1, 0, and, 0);

        assertTrue(g.canConnectOutputPort(g2, 1, wire, 0));
        int g3 = g.connectOutputPort(g2, 1, wire, 0);

        assertTrue(g.canConnectOutputPort(g3, 0, wire2, 0));
        int g4 = g.connectOutputPort(g3, 0, wire2, 0);

        assertTrue(g.canConnectInputPort(g2, 0, wire, 0));
        int g5 = g.connectInputPort(g2, 0, wire, 0);

        Side end = new Side(12, 3, Direction.EAST).opposite();
        assertTrue(g.canConnectOutputPort(end, wire2, 0));
        int g6 = g.connectOutputPort(end, wire2, 0);

        assertTrue(g.canConnectOutputPort(g4, 0, wire, 0));
        assertFalse(g.canConnectInputPort(g4, 0, wire, 0));
        assertTrue(g.canConnectInputPort(g6, 0, wire, 0));
        assertFalse(g.canConnectInputPort(g5, 0, wire2, 0));

        Grid<String> grid = g.toGrid(emptyCell);
        assertTrue(GridUtils.equals(grid, longAnd));
    }
}
