package transform.wiring;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.PatternFilenameFilter;
import org.testng.Assert;
import org.testng.annotations.Test;
import transform.GadgetUtils;
import types.Direction;
import types.Gadget;
import types.Grid;
import types.Side;
import types.configuration.GadgetConfiguration;
import utils.ResourceUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static types.Direction.*;

public class TurnShifterTest {
    // tests to include: frobenius wirer length 0.
    // TODO more tests

    @Test
    public void testShift() throws Exception {
        File turnDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/turns/");
        Iterable<Gadget> turns = GadgetUtils.getGadgets(turnDir);
        File wireDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/wires/");
        Iterable<Gadget> wires = GadgetUtils.getGadgets(wireDir);
        TurnShifter shifter = new TurnShifter(turns, wires, new FrobeniusWirer(wires));
        Assert.assertEquals(shifter.minSeparation(Direction.WEST), 2);

        int thickness = shifter.minThickness(Direction.EAST, 2);
        int length = shifter.minLength(Direction.EAST, 2, thickness);
        List<Side> inputs = ImmutableList.of(new Side(0, thickness, WEST), new Side(0, thickness + 3, WEST));
        List<Side> outputs = ImmutableList.of(
                new Side(length - 1, thickness, EAST), new Side(length - 1, thickness + 3, EAST)
        );
        GadgetConfiguration shift = shifter.shift(inputs, outputs, 18, 6);
        Assert.assertEquals(shift.getInputs(), ImmutableSet.copyOf(inputs));
        Assert.assertEquals(shift.getOutputs(), ImmutableSet.copyOf(outputs));
        Grid<String> grid = shift.toGrid(".");
        Assert.assertTrue(grid.getSizeY() <= thickness + 3 + thickness);
        Assert.assertTrue(grid.getSizeX() == length);
        System.out.println(grid);
    }
}