package transform;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.testng.annotations.Test;
import parser.GadgetParser;
import parser.SATParser;
import transform.planar.GadgetPlanarizer;
import types.Gadget;
import types.configuration.AtomicConfiguration;
import types.configuration.CellConfiguration;
import types.configuration.Configuration;
import utils.ResourceUtils;

public class EndToEndTest {
    // TODO fix or delete
    @Test
    public void testPlace() throws Exception {
        SATParser s = new SATParser();
        Configuration c = s.parseSAT("((x && y) || x) && x");
        AtomicConfiguration config = new ConfigurationResolver(
            ImmutableList.<Configuration>of(), ImmutableSet.of("AND", "OR", "VARIABLE", "SPLIT", "END")
        ).process(c);

        String dir = "types/gadget/circuit/";

        GadgetParser parser = new GadgetParser();
        Gadget and = parser.parse(ResourceUtils.getAbsoluteFile(getClass(), dir + "and.txt"));
        Gadget or = parser.parse(ResourceUtils.getAbsoluteFile(getClass(), dir + "or.txt"));
        Gadget var = parser.parse(ResourceUtils.getAbsoluteFile(getClass(), dir + "variable.txt"));
        Gadget split = parser.parse(ResourceUtils.getAbsoluteFile(getClass(), dir + "split.txt"));
        Gadget end = parser.parse(ResourceUtils.getAbsoluteFile(getClass(), dir + "end.txt"));
        Gadget wire = parser.parse(ResourceUtils.getAbsoluteFile(getClass(), dir + "wire_2.txt"));
        Gadget wire2 = parser.parse(ResourceUtils.getAbsoluteFile(getClass(), dir + "wire_3.txt"));
        Gadget turn = parser.parse(ResourceUtils.getAbsoluteFile(getClass(), dir + "turn.txt"));
        Gadget crossover = parser.parse(ResourceUtils.getAbsoluteFile(getClass(), dir + "crossover.txt"));
        Gadget empty = parser.parse(ResourceUtils.getAbsoluteFile(getClass(), dir + "empty.txt"));


        GadgetPlanarizer placer = new GadgetPlanarizer(
            ImmutableMap.of("AND", and, "OR", or, "VARIABLE", var, "SPLIT", split, "END", end)
        );
        CellConfiguration grid = placer.process(config);
        System.out.println(grid);
        GadgetPlacer gadgetPlacer = new GadgetPlacer(
            ImmutableList.of(wire, wire2),
            turn,
            crossover,
            empty,
            ImmutableList.of(and, or, var, split, end)
        );
        System.out.println(getStringArray(gadgetPlacer.place(grid)));
    }


    public String getStringArray(String[][] array){
        StringBuilder str = new StringBuilder();
        for(int j = 0; j < array[0].length; j++){
            //noinspection ForLoopReplaceableByForEach
            for(int i = 0; i < array.length; i++){
                str.append(array[i][j]);
                str.append("");
            }
            str.append("\n");
        }
        return str.toString();
    }
}
