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
import types.configuration.Configuration;
import utils.ResourceUtils;

public class GadgetPlanarizerTest {
    // todo make unit test
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

//        GadgetPlanarizer placer = new GadgetPlanarizer(
//            ImmutableMap.of("AND", and,"OR", or, "VARIABLE", var, "SPLIT", split, "END", end)
//        );
//        System.out.println(placer.process(config));
    }
}