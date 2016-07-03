package transform;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;
import parser.GadgetParser;
import types.Direction;
import types.Gadget;
import types.Location;
import types.configuration.CellConfiguration;
import types.configuration.cells.*;
import utils.ResourceUtils;

import java.util.*;

import static org.testng.Assert.assertEquals;

public class GadgetPlacerTest {

    // TODO delete?
    @Test
    public void testPlace() throws Exception {
        List<Gadget> fatWires = new ArrayList<>();
        List<Gadget> shortWires = new ArrayList<>();
        List<Gadget> baseGadgets = new ArrayList<>();

        GadgetParser parser = new GadgetParser();
        Gadget and = parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "bigand.txt"));
        Gadget crossover = parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "crossover.txt"));
        Gadget empty = parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "empty.txt"));
        Gadget not = parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "not.txt"));
        Gadget turn = parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "turn.txt"));

        baseGadgets.add(and);
        baseGadgets.add(not);

        fatWires.add(parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "wire-3x6.txt")));
        fatWires.add(parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "wire-6x4.txt")));
        fatWires.add(parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "wire-9x4.txt")));

        shortWires.add(parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "wire-3x2.txt")));
        shortWires.add(parser.parseGadget(ResourceUtils.getRelativeFile(getClass(), "wire-3x3.txt")));

        GadgetPlacer fatPlacer = new GadgetPlacer(fatWires, turn, crossover, empty, baseGadgets);
        GadgetPlacer shortPlacer = new GadgetPlacer(shortWires, turn, crossover, empty, baseGadgets);
        //MAKE SOME GRID CONFIGS

        CellConfiguration wireGrid = new CellConfiguration(1,1);
        wireGrid.put(WireCell.getWire(Direction.NORTH),new Location(0,0));

        //test shortPlacement of wires
        String[][] test = shortPlacer.place(wireGrid);
        wireGrid.put(WireCell.getWire(Direction.EAST),new Location(0,0));
        String[][] test2 = shortPlacer.place(wireGrid);
        int cellSize = test.length;
        for(int i = 0; i< cellSize; i++){
            String testStr = "-";
            if(i == cellSize/2){
                testStr = "*";
            }else if(i == cellSize/2 - 1 || i == cellSize/2 + 1){
                testStr = "\"";
            }
            for(int j = 0; j < cellSize; j++){
                assertEquals(test[i][j], testStr);
                assertEquals(test2[j][i], testStr);
            }
        }

//        visually check fat wire for propery rotation/reflection
//        wireGrid.put(WireCell.getWire(Direction.NORTH),new Location(0,0));
//        System.out.println("FAT WIRE:");
//        System.out.println(getStringArray(fatPlacer.place(wireGrid)));
//
//        wireGrid.put(new TurnCell(Direction.NORTH, Direction.EAST),new Location(0,0));
//        System.out.println("FAT TURN:");
//        System.out.println(getStringArray(fatPlacer.place(wireGrid)));
//
//        wireGrid.put(new CrossoverCell(Direction.SOUTH, Direction.WEST),new Location(0,0));
//        System.out.println("FAT CROSS:");
//        System.out.println(getStringArray(fatPlacer.place(wireGrid)));

        CellConfiguration biggerGrid = new CellConfiguration(2,3);
        Set<Direction> inputDirs = new HashSet<>();
        inputDirs.add(Direction.WEST);
        Set<Direction> outputDirs = new HashSet<>();
        outputDirs.add(Direction.EAST);

        Map<Direction, Integer> ports = new HashMap<>();

        ImmutableList<Integer> dummyId = ImmutableList.of();
        ports.put(Direction.WEST, 0);
        biggerGrid.put(new PortCell("AND", dummyId, inputDirs, new HashSet<Direction>(), ports), new Location(1, 0));
        biggerGrid.put(new NodeCell("AND", dummyId), new Location(1,1));
        ports.put(Direction.WEST,1);
        ports.put(Direction.EAST, 0);
//        biggerGrid.put(new PortCell("AND", new HashSet<Direction>(), outputDirs, ports), new Location(1, 1));
        biggerGrid.put(new PortCell("AND", dummyId, inputDirs, outputDirs, ports), new Location(1, 2));

        biggerGrid.put(new CrossoverCell(Direction.SOUTH, Direction.EAST), new Location(0,0));
        biggerGrid.put(new TurnCell(Direction.NORTH, Direction.EAST), new Location(0,2));
        biggerGrid.put(WireCell.getWire(Direction.SOUTH), new Location(0,1));
//        System.out.println("FAT GADGET");
//        System.out.println(getStringArray(fatPlacer.place(biggerGrid)));

//        System.out.println("SHORT GADGET");
//        System.out.println(getStringArray(shortPlacer.place(biggerGrid)));
    }

    public String getStringArray(String[][] array){
        StringBuilder str = new StringBuilder();
        for(int j = 0; j < array[0].length; j++){
            for(int i = 0; i < array.length; i++){
                str.append(array[i][j]);
                str.append("");
            }
            str.append("\n");
        }
        return str.toString();
    }
}