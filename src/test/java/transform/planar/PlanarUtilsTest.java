package transform.planar;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;
import parser.CellConfigurationParser;
import parser.GadgetParser;
import types.Direction;
import types.Gadget;
import types.Side;
import types.configuration.CellConfiguration;
import types.configuration.nodes.AtomicNode;
import types.configuration.nodes.Node;
import types.configuration.nodes.NodeType;
import utils.ResourceUtils;

import java.util.List;

import static org.testng.Assert.*;

public class PlanarUtilsTest {
    @Test
    public void testDeleteNode() throws Exception {
        CellConfigurationParser parser = new CellConfigurationParser();
        CellConfiguration grid = parser.parse(ResourceUtils.getRelativeFile(getClass(), "layouts/akari-layout.txt"));
        List<Side> sides = PlanarUtils.deleteNode(
            grid,
            new AtomicNode(
                ImmutableList.<Integer>of(),
                new Node(NodeType.LABELLED, "NOT", 4, 1, 1)
            )
        );

        assertEquals(sides, ImmutableList.of(new Side(6, 13, Direction.SOUTH), new Side(14, 11, Direction.WEST)));
    }

}