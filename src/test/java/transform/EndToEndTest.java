package transform;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.testng.annotations.Test;
import parser.GadgetParser;
import parser.SATParser;
import types.Gadget;
import types.configuration.AtomicConfiguration;
import types.configuration.Configuration;

import java.io.File;

import static org.testng.Assert.assertNotNull;

public class EndToEndTest {
    @Test
    public void testPlace() throws Exception {
        SATParser s = new SATParser();
        Configuration c = s.parseSAT("((x && y) || x) && x");
        AtomicConfiguration config = new ConfigurationResolver()
            .resolve(c, ImmutableList.<Configuration>of(), ImmutableSet.of("AND", "OR", "VARIABLE", "SPLIT", "END"));

        assertNotNull(getClass().getResource("circuit/and.txt"), "Test file missing");
        assertNotNull(getClass().getResource("circuit/or.txt"), "Test file missing");
        assertNotNull(getClass().getResource("circuit/variable.txt"), "Test file missing");
        assertNotNull(getClass().getResource("circuit/split.txt"), "Test file missing");
        assertNotNull(getClass().getResource("circuit/end.txt"), "Test file missing");

        GadgetParser parser = new GadgetParser();
        Gadget and = parser.parseGadget(new File(getClass().getResource("circuit/and.txt").getFile()));
        Gadget or = parser.parseGadget(new File(getClass().getResource("circuit/or.txt").getFile()));
        Gadget var = parser.parseGadget(new File(getClass().getResource("circuit/variable.txt").getFile()));
        Gadget split = parser.parseGadget(new File(getClass().getResource("circuit/split.txt").getFile()));
        Gadget end = parser.parseGadget(new File(getClass().getResource("circuit/end.txt").getFile()));
        Gadget wire = parser.parseGadget(new File(getClass().getResource("circuit/wire.txt").getFile()));
        Gadget turn = parser.parseGadget(new File(getClass().getResource("circuit/turn.txt").getFile()));
        Gadget crossover = parser.parseGadget(new File(getClass().getResource("circuit/crossover.txt").getFile()));
        Gadget empty = parser.parseGadget(new File(getClass().getResource("circuit/empty.txt").getFile()));


        GridPlacer placer = new GridPlacer(
            config, ImmutableMap.of("AND", and, "OR", or, "VARIABLE", var, "SPLIT", split, "END", end)
        );
        placer.place();
        System.out.println(placer.getGrid());
        GadgetPlacer gadgetPlacer = new GadgetPlacer(
            ImmutableList.of(wire),
            turn,
            crossover,
            empty,
            ImmutableList.of(and, or, var, split, end)
        );
        System.out.println(getStringArray(gadgetPlacer.place(placer.getGrid())));
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
