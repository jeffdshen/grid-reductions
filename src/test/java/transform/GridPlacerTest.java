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
import utils.ResourceUtils;

public class GridPlacerTest {
    @Test
    public void testPlace() throws Exception {
        SATParser s = new SATParser();
        Configuration c = s.parseSAT("((x && y) || x) && x");
        AtomicConfiguration config = new ConfigurationResolver()
            .resolve(c, ImmutableList.<Configuration>of(), ImmutableSet.of("AND", "OR", "VARIABLE", "SPLIT", "END"));

        String dir = "types/gadget/circuit/";
        GadgetParser parser = new GadgetParser();
        Gadget and = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "and.txt"));
        Gadget or = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "or.txt"));
        Gadget var = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "variable.txt"));
        Gadget split = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "split.txt"));
        Gadget end = parser.parseGadget(ResourceUtils.getAbsoluteFile(getClass(), dir + "end.txt"));

        GridPlacer placer = new GridPlacer(
            config, ImmutableMap.of("AND", and,"OR", or, "VARIABLE", var, "SPLIT", split, "END", end)
        );
        placer.place();
        System.out.println(placer.getGrid());
    }
}