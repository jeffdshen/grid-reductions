package transform.wiring;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.testng.annotations.Test;
import transform.GadgetUtils;
import types.Direction;
import types.Gadget;
import types.Grid;
import types.Side;
import types.configuration.GadgetConfiguration;
import utils.ResourceUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static types.Direction.*;

/**
 * Tests the turn shifter class. Note that some of these tests are implementation specific.
 */
public class TurnShifterTest {

    // TODO make config.properties, and make this read from there.
    @Test
    public void testMinimumShift2WiresEast() throws Exception {
        File turnDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/turns/");
        Iterable<Gadget> turns = GadgetUtils.getGadgets(turnDir);
        File wireDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/wires/");
        Iterable<Gadget> wires = GadgetUtils.getGadgets(wireDir);
        TurnShifter shifter = new TurnShifter(turns, wires, new FrobeniusWirer(wires));

        List<Boolean> isInput = ImmutableList.of(true, true);
        List<Integer> minSep = shifter.minSeparation(Direction.EAST, true, isInput);
        assertEquals(minSep.size(), 1);
        assertEquals(minSep.get(0).intValue(), 1);

        List<Boolean> isOutput = ImmutableList.of(false, false);
        List<Integer> minSep2 = shifter.minSeparation(Direction.EAST, false, isOutput);
        assertEquals(minSep2.size(), 1);
        assertEquals(minSep2.get(0).intValue(), 1);

        int thickness = shifter.minThickness(Direction.EAST, isInput);
        int length = shifter.minLength(Direction.EAST, isInput, thickness);

        List<Side> inputs = ImmutableList.of(new Side(0, thickness, WEST), new Side(0, thickness + 2, WEST));
        List<Side> outputs = ImmutableList.of(
            new Side(length - 1, thickness, EAST), new Side(length - 1, thickness + 2, EAST)
        );

        GadgetConfiguration shift = shifter.shift(inputs, outputs, isInput, thickness);
        assertEquals(shift.getInputs(), ImmutableSet.copyOf(inputs));
        assertEquals(shift.getOutputs(), ImmutableSet.copyOf(outputs));
        Grid<String> grid = shift.toGrid(".");
        assertEquals(grid.getSizeY(), thickness + 2 + 1 + 1);
        assertEquals(grid.getSizeX(), length);
//        System.out.println(grid);
    }

    @Test
    public void testMinimumShift2WiresEastMixed() throws Exception {
        File turnDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/turns/");
        Iterable<Gadget> turns = GadgetUtils.getGadgets(turnDir);
        File wireDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/wires/");
        Iterable<Gadget> wires = GadgetUtils.getGadgets(wireDir);
        TurnShifter shifter = new TurnShifter(turns, wires, new FrobeniusWirer(wires));

        List<Boolean> isInput = ImmutableList.of(true, false);
        assertEquals(shifter.minSeparation(EAST, true, isInput), ImmutableList.of(2));

        List<Boolean> isOutput = ImmutableList.of(false, true);
        assertEquals(shifter.minSeparation(EAST, false, isOutput), ImmutableList.of(2));

        int thickness = shifter.minThickness(Direction.EAST, isInput);
        int length = shifter.minLength(Direction.EAST, isInput, thickness);

        // input[1] - input[0] >= minSep[0] + 1
        List<Side> start = ImmutableList.of(new Side(0, thickness, WEST), new Side(0, thickness + 3, WEST));
        List<Side> end = ImmutableList.of(
            new Side(length - 1, thickness, EAST), new Side(length - 1, thickness + 3, EAST)
        );

        GadgetConfiguration shift = shifter.shift(start, end, isInput, thickness);
        assertEquals(shift.getInputs(), ImmutableSet.of(start.get(0), end.get(1)));
        assertEquals(shift.getOutputs(), ImmutableSet.of(start.get(1), end.get(0)));
        Grid<String> grid = shift.toGrid(".");
        assertEquals(grid.getSizeY(), thickness + 3 + 1 + 1);
        assertEquals(grid.getSizeX(), length);
//        System.out.println(grid);
    }

    @Test
    public void testMinimumShift2WiresEastMixedOffset() throws Exception {
        File turnDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/turns/");
        Iterable<Gadget> turns = GadgetUtils.getGadgets(turnDir);
        File wireDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/wires/");
        Iterable<Gadget> wires = GadgetUtils.getGadgets(wireDir);
        TurnShifter shifter = new TurnShifter(turns, wires, new FrobeniusWirer(wires));


        List<Boolean> isInput = ImmutableList.of(true, false);
        assertEquals(shifter.minSeparation(EAST, true, isInput), ImmutableList.of(2));

        List<Boolean> isOutput = ImmutableList.of(false, true);
        assertEquals(shifter.minSeparation(EAST, false, isOutput), ImmutableList.of(2));

        int thickness = shifter.minThickness(Direction.EAST, isInput);
        int length = shifter.minLength(Direction.EAST, isInput, thickness);

        // input[1] - input[0] >= minSep[0] + 1
        List<Side> start = ImmutableList.of(new Side(0, thickness, WEST), new Side(0, thickness + 3, WEST));
        List<Side> end = ImmutableList.of(
            new Side(length - 1, thickness + 1, EAST), new Side(length - 1, thickness + 5, EAST)
        );

        GadgetConfiguration shift = shifter.shift(start, end, isInput, thickness);
        assertEquals(shift.getInputs(), ImmutableSet.of(start.get(0), end.get(1)));
        assertEquals(shift.getOutputs(), ImmutableSet.of(start.get(1), end.get(0)));
        Grid<String> grid = shift.toGrid(".");
        assertEquals(grid.getSizeY(), thickness + 5 + 1 + 1);
        assertEquals(grid.getSizeX(), length);
//        System.out.println(grid);
    }

    @Test
    public void testMinimumShift2WiresSouthMixedOffset() throws Exception {
        File turnDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/turns/");
        Iterable<Gadget> turns = GadgetUtils.getGadgets(turnDir);
        File wireDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/wires/");
        Iterable<Gadget> wires = GadgetUtils.getGadgets(wireDir);
        TurnShifter shifter = new TurnShifter(turns, wires, new FrobeniusWirer(wires));

        List<Boolean> isInput = ImmutableList.of(true, false);
        assertEquals(shifter.minSeparation(SOUTH, true, isInput), ImmutableList.of(2));

        List<Boolean> isOutput = ImmutableList.of(false, true);
        assertEquals(shifter.minSeparation(SOUTH, false, isOutput), ImmutableList.of(2));

        int thickness = shifter.minThickness(SOUTH, isInput);
        int length = shifter.minLength(SOUTH, isInput, thickness);

        // input[1] - input[0] >= minSep[0] + 1
        List<Side> start = ImmutableList.of(new Side(4, 0, NORTH), new Side(1, 0, NORTH));
        List<Side> end = ImmutableList.of(new Side(6, length - 1, SOUTH), new Side(2, length - 1, SOUTH));

        GadgetConfiguration shift = shifter.shift(start, end, isInput, thickness);
        assertEquals(shift.getInputs(), ImmutableSet.of(start.get(0), end.get(1)));
        assertEquals(shift.getOutputs(), ImmutableSet.of(start.get(1), end.get(0)));
        Grid<String> grid = shift.toGrid(".");
        assertEquals(grid.getSizeX(), thickness + 6 + 1);
        assertEquals(grid.getSizeY(), length);
//        System.out.println(grid);
    }

    @Test
    public void testMinimumShift3WiresSouthMixedOffset() throws Exception {
        File turnDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/turns/");
        Iterable<Gadget> turns = GadgetUtils.getGadgets(turnDir);
        File wireDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/wires/");
        Iterable<Gadget> wires = GadgetUtils.getGadgets(wireDir);
        TurnShifter shifter = new TurnShifter(turns, wires, new FrobeniusWirer(wires));

        List<Boolean> isInput = ImmutableList.of(true, false, true);
        assertEquals(shifter.minSeparation(SOUTH, true, isInput), ImmutableList.of(2, 2));

        List<Boolean> isOutput = ImmutableList.of(false, true, false);
        assertEquals(shifter.minSeparation(SOUTH, false, isOutput), ImmutableList.of(2, 2));

        int thickness = shifter.minThickness(SOUTH, isInput);
        int length = shifter.minLength(SOUTH, isInput, thickness);

        // input[1] - input[0] >= minSep[0] + 1
        List<Side> start = ImmutableList.of(new Side(10, 0, NORTH), new Side(4, 0, NORTH), new Side(1, 0, NORTH));
        List<Side> end = ImmutableList.of(
            new Side(9, length - 1, SOUTH), new Side(6, length - 1, SOUTH), new Side(2, length - 1, SOUTH)
        );

        GadgetConfiguration shift = shifter.shift(start, end, isInput, thickness);
        assertEquals(shift.getInputs(), ImmutableSet.of(start.get(0), end.get(1), start.get(2)));
        assertEquals(shift.getOutputs(), ImmutableSet.of(end.get(0), start.get(1), end.get(2)));
        Grid<String> grid = shift.toGrid(".");
        assertEquals(grid.getSizeX(), thickness + 9 + 1);
        assertEquals(grid.getSizeY(), length);
//        System.out.println(grid);
    }

    @Test
    public void testMinimumShift3WiresSouthMixedOffsetLong() throws Exception {
        File turnDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/turns/");
        Iterable<Gadget> turns = GadgetUtils.getGadgets(turnDir);
        File wireDir = ResourceUtils.getAbsoluteFile(getClass(), "transform/wiring/gadgets/wires/");
        Iterable<Gadget> wires = GadgetUtils.getGadgets(wireDir);
        // only east has wires that are long
        wires = Iterables.filter(wires, new Predicate<Gadget>() {
            @Override
            public boolean apply(Gadget g) {
                return GadgetUtils.WIRE_DIRECTION.apply(g) != EAST || GadgetUtils.WIRE_LENGTH.apply(g) >= 5;
            }
        });

        TurnShifter shifter = new TurnShifter(turns, wires, new FrobeniusWirer(wires));

        List<Boolean> isInput = ImmutableList.of(true, false, true);
        assertEquals(shifter.minSeparation(SOUTH, true, isInput), ImmutableList.of(2, 2));

        List<Boolean> isOutput = ImmutableList.of(false, true, false);
        assertEquals(shifter.minSeparation(SOUTH, false, isOutput), ImmutableList.of(2, 2));

        int thickness = shifter.minThickness(SOUTH, isInput);
        int length = shifter.minLength(SOUTH, isInput, thickness);

        // input[1] - input[0] >= minSep[0] + 1
        List<Side> start = ImmutableList.of(new Side(10, 0, NORTH), new Side(4, 0, NORTH), new Side(1, 0, NORTH));
        List<Side> end = ImmutableList.of(
            new Side(9, length - 1, SOUTH), new Side(6, length - 1, SOUTH), new Side(2, length - 1, SOUTH)
        );

        GadgetConfiguration shift = shifter.shift(start, end, isInput, thickness);
        assertEquals(shift.getInputs(), ImmutableSet.of(start.get(0), end.get(1), start.get(2)));
        assertEquals(shift.getOutputs(), ImmutableSet.of(end.get(0), start.get(1), end.get(2)));
        Grid<String> grid = shift.toGrid(".");
        assertEquals(grid.getSizeX(), thickness + 10 + 1);
        assertEquals(grid.getSizeY(), length);
//        System.out.println(grid);
    }
}