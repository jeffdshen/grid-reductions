package parser;

import org.testng.annotations.Test;
import types.Gadget;
import types.Location;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class GadgetParserTest {
    @Test
    public void testParseGadget() throws Exception {
        assertNotNull(getClass().getResource("and.txt"), "Test file missing");
        GadgetParser parser = new GadgetParser();
        Gadget gadget = parser.parseGadget(new File(getClass().getResource("and.txt").getFile()));
        assertEquals(gadget.getInputSize(), 2);
        assertEquals(gadget.getOutputSize(), 1);
        assertEquals(gadget.getInput(0), new Location(0, 0));
        assertEquals(gadget.getInput(1), new Location(0, 2));
        assertEquals(gadget.getOutput(0), new Location(2, 1));
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