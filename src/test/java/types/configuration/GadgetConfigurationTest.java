package types.configuration;

import org.testng.annotations.Test;
import parser.GadgetParser;
import transform.GridUtils;
import types.*;
import utils.ResourceUtils;

import java.util.Map;

import static org.testng.Assert.*;

public class GadgetConfigurationTest {
    @Test
    public void testConnect() throws Exception {
        GadgetParser parser = new GadgetParser();
        String dir = "types/gadget/circuit/";
        Gadget and = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "and.txt"));
        Gadget wire = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "wire_2.txt"));
        Gadget wire2 = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "wire_3.txt"));
        Gadget empty = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "empty.txt"));
        Gadget variable = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "variable.txt"));

        Gadget longAnd = parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "and.txt"));
        Gadget twoAnds = parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "2ands.txt"));


        String emptyCell = empty.getCell(0, 0);

        // make a long AND configuration
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

        // make a variable configuration
        GadgetConfiguration v = new GadgetConfiguration();
        int v0 = v.connect(new Location(0, 0), variable);

        //  connect configurations together
        GadgetConfiguration config = new GadgetConfiguration();
        assertTrue(config.canConnect(new Location(0, 0), g));
        Map<Integer, Integer> ids = config.connect(new Location(0, 0), g);
        assertTrue(config.canConnectInputPort(ids.get(g6), 0, g0, 0, g));
        Map<Integer, Integer> ids2 = config.connectInputPort(ids.get(g6), 0, g0, 0, g);
        assertTrue(config.canConnectOutputPort(ids2.get(g4), 0, v0, 0, v));
        config.connectOutputPort(ids2.get(g4), 0, v0, 0, v);

        assertTrue(GridUtils.equals(config.toGrid(emptyCell), twoAnds));
    }
}
