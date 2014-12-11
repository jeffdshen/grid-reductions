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

import static org.testng.Assert.*;

public class GridPlacerTest {
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

        GridPlacer placer = new GridPlacer(
            config, ImmutableMap.of("AND", and,"OR", or, "VARIABLE", var, "SPLIT", split, "END", end)
        );
        placer.place();
        System.out.println(placer.getGrid());
    }
}