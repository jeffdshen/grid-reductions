package parser;

import org.testng.annotations.Test;
import types.Configuration;
import types.Node;
import types.NodeType;

import java.io.File;

import static org.testng.Assert.*;

public class ConfigurationParserTest {

    @Test
    public void testParseConfiguration() throws Exception {
        assertNotNull(getClass().getResource("nand.txt"), "Test file missing");
        ConfigurationParser parser = new ConfigurationParser();
        Configuration cfg = parser.parseConfiguration(new File(getClass().getResource("nand.txt").getFile()));
        assertEquals(cfg.getName(), "NAND");
        
        Node in = cfg.getNode(0);
        Node andNode = cfg.getNode(1);
        Node notNode = cfg.getNode(2);
        Node out = cfg.getNode(4);
        assertEquals(in,cfg.getInput());
        assertEquals(out, cfg.getOutput());
        
        assertEquals(in.getId(), 0);      
        assertEquals(in.inputSize(), 0);
        assertEquals(in.getType(), NodeType.INPUT);
        assertEquals(in.outputSize(), 2);
        
        assertEquals(in.getOutputPort(0).getPortNumber(), 0);
        assertEquals(in.getOutputPort(0).getId(), in.getId());
        assertEquals(in.getOutputPort(0).isInput(), false);
        assertEquals(in.getOutputPort(1).getPortNumber(), 1);
        assertEquals(in.getOutputPort(1).getId(), in.getId());
        assertEquals(in.getOutputPort(1).isInput(), false);

        assertEquals(andNode.getId(), 1);
        assertEquals(andNode.inputSize(), 2);
        assertEquals(andNode.getType(), NodeType.LABELLED);
        assertEquals(andNode.outputSize(), 1);
        assertEquals(andNode.getName(), "AND");


        assertEquals(andNode.getInputPort(0).getPortNumber(), 0);
        assertEquals(andNode.getInputPort(0).getId(), andNode.getId());
        assertEquals(andNode.getInputPort(0).isInput(), true);
        assertEquals(andNode.getInputPort(1).getPortNumber(), 1);
        assertEquals(andNode.getInputPort(1).getId(), andNode.getId());
        assertEquals(andNode.getInputPort(1).isInput(), true);
        assertEquals(andNode.getOutputPort(0).getPortNumber(), 0);
        assertEquals(andNode.getOutputPort(0).getId(), andNode.getId());
        assertEquals(andNode.getOutputPort(0).isInput(), false);


        assertEquals(notNode.getId(), 2);
        assertEquals(notNode.inputSize(), 1);
        assertEquals(notNode.getType(), NodeType.LABELLED);
        assertEquals(notNode.outputSize(), 1);
        assertEquals(notNode.getName(), "NOT");


        assertEquals(notNode.getInputPort(0).getPortNumber(), 0);
        assertEquals(notNode.getInputPort(0).getId(), notNode.getId());
        assertEquals(notNode.getInputPort(0).isInput(), true);
        assertEquals(notNode.getOutputPort(0).getPortNumber(), 0);
        assertEquals(notNode.getOutputPort(0).getId(), notNode.getId());
        assertEquals(notNode.getOutputPort(0).isInput(), false);


        assertEquals(out.getId(), 4);
        assertEquals(out.inputSize(), 1);
        assertEquals(out.getType(), NodeType.OUTPUT);
        assertEquals(out.outputSize(), 0);


        assertEquals(out.getInputPort(0).getPortNumber(), 0);
        assertEquals(out.getInputPort(0).getId(), out.getId());
        assertEquals(out.getInputPort(0).isInput(), true);


        assertEquals(cfg.getPort(in.getOutputPort(0)), andNode.getInputPort(1));
        assertEquals(cfg.getPort(in.getOutputPort(1)), andNode.getInputPort(0));
        assertEquals(cfg.getPort(andNode.getInputPort(0)), in.getOutputPort(1));
        assertEquals(cfg.getPort(andNode.getInputPort(1)), in.getOutputPort(0));

        assertEquals(cfg.getPort(andNode.getOutputPort(0)), notNode.getInputPort(0));
        assertEquals(cfg.getPort(notNode.getInputPort(0)), andNode.getOutputPort(0));

        assertEquals(cfg.getPort(out.getInputPort(0)), notNode.getOutputPort(0));
        assertEquals(cfg.getPort(notNode.getOutputPort(0)), out.getInputPort(0));
    }
}