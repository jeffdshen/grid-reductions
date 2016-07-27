package parser;

import org.testng.annotations.Test;
import types.Direction;
import types.Gadget;
import types.Side;
import utils.ResourceUtils;

import static org.testng.Assert.assertEquals;

public class GadgetParserTest {
    @Test
    public void testParseGadget() throws Exception {
        GadgetParser parser = new GadgetParser();
        Gadget gadget = parser.parse(ResourceUtils.getReader(getClass(), "and.txt"), "and.txt");

        assertEquals(gadget.getInputSize(), 2);
        assertEquals(gadget.getOutputSize(), 1);
        assertEquals(gadget.getInput(0), new Side(0, 0, Direction.WEST));
        assertEquals(gadget.getInput(1), new Side(0, 2, Direction.WEST));
        assertEquals(gadget.getOutput(0), new Side(2, 1, Direction.EAST));
        assertEquals(gadget.getCell(0,0), "1");
        assertEquals(gadget.getCell(1,0), "1");
        assertEquals(gadget.getCell(2,0), "0");
        assertEquals(gadget.getCell(0,1), "0");
        assertEquals(gadget.getCell(1,1), "2");
        assertEquals(gadget.getCell(2,1), "1");
        assertEquals(gadget.getCell(0,2), "1");
        assertEquals(gadget.getCell(1,2), "1");
        assertEquals(gadget.getCell(2,2), "0");
    }
}