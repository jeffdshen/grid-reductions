package transform;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;
import parser.GadgetParser;
import types.Direction;
import types.Gadget;
import types.configuration.GridConfiguration;
import types.configuration.cells.Cell;
import types.configuration.cells.CellType;

import java.io.File;
import java.net.URL;

import static org.testng.Assert.*;

public class GadgetConverterTest {
    @Test
    public void testToGridConfiguration() throws Exception {
        URL resource = getClass().getResource("and.txt");
        assertNotNull(resource, "Test file missing");
        GadgetParser parser = new GadgetParser();
        Gadget gadget = parser.parseGadget(new File(resource.getFile()));
        GadgetConverter converter = new GadgetConverter();
        GridConfiguration grid = converter.toGridConfiguration(gadget, ImmutableList.of(1, 2, 3));

        assertEquals(grid.getSizeX(), 1);
        assertEquals(grid.getSizeY(), 2);

        Cell c1 = grid.getCell(0, 0);
        assertEquals(c1.getCellType(), CellType.PORT);
        assertEquals(c1.getName(), "AND");
        assertEquals(c1.inputSize(), 1);
        assertEquals(c1.outputSize(), 1);
        assertTrue(c1.isInput(Direction.WEST));
        assertTrue(c1.isOutput(Direction.EAST));
        assertEquals(c1.getId(), ImmutableList.of(1, 2, 3));

        Cell c2 = grid.getCell(0, 1);
        assertEquals(c2.getCellType(), CellType.PORT);
        assertEquals(c2.getName(), "AND");
        assertEquals(c2.inputSize(), 1);
        assertEquals(c2.outputSize(), 0);
        assertTrue(c2.isInput(Direction.WEST));
        assertEquals(c1.getId(), ImmutableList.of(1, 2, 3));
    }
}