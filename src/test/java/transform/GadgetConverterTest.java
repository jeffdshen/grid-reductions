package transform;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;
import parser.CellConfigurationParser;
import parser.GadgetParser;
import parser.ParserUtils;
import types.Direction;
import types.Gadget;
import types.configuration.CellConfiguration;
import types.configuration.cells.Cell;
import types.configuration.cells.CellType;
import utils.ResourceUtils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GadgetConverterTest {
    @Test
    public void testToGridConfiguration() throws Exception {
        GadgetParser parser = new GadgetParser();
        Gadget gadget = parser.parse(ResourceUtils.getAbsoluteFile(getClass(), "types/gadget/circuit/and.txt"));
        GadgetConverter converter = new GadgetConverter();
        CellConfiguration grid = converter.toGridConfiguration(gadget, ImmutableList.of(1, 2, 3));
        assertEquals(grid.getSizeX(), 1);
        assertEquals(grid.getSizeY(), 2);

        // TODO use .equals and CellConfigurationParser to test
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