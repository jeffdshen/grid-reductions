package parser;

import org.testng.annotations.Test;
import types.configuration.Configuration;
import types.configuration.nodes.Node;
import types.configuration.nodes.Port;

public class SATParserTest {

    // TODO make unit test
    @Test
    public void testParseSAT() throws Exception {
//        SATTokenizer t = new SATTokenizer("!(x1||!y&&z) || (!x2 || y) && y");
//        t.test();
        SATParser s = new SATParser();
        Configuration c = s.parseSAT("!(x1||!y &&z) || (!x2 || y) && y");
        System.out.println(c.toString());
        for(Node n : c.getNodes()){
            System.out.println("Node " + n.getName());
            System.out.println("In ports: ");
            for(Port p: n.getInputPorts()){
                System.out.println(p);
            }
            System.out.println("Out ports map to: ");
            for(Port p: n.getOutputPorts()){
                System.out.println(c.getPort(p));
            }
            System.out.println();
        }

    }
}