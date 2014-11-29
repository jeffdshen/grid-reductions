package types;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.List;

/**
 * A directed acyclic graph of labelled nodes.
 * Precondition: Nodes must be passed in topological order.
 *
 */
public class Configuration {
    private final String name;
    private final ImmutableMap<Integer, Node> nodes;
    private final Node input;
    private final Node output;
    private final ImmutableMap<Port, Port> edges;

    public Configuration(String name, List<ConnectedNode> nodes) {
        ImmutableMap.Builder<Integer, Node> nodeBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<Port, Port> edgesBuilder = ImmutableMap.builder();

        Node input = null;
        Node output = null;
        for (ConnectedNode connectedNode : nodes) {
            Node node = connectedNode.getNode();
            switch (node.getType()) {
                case INPUT:
                    Preconditions.checkArgument(input == null, "Found more than one input node");
                    input = node;
                    break;
                case OUTPUT:
                    Preconditions.checkArgument(output == null, "Found more than one output node");
                    output = node;
                    break;
                case LABELLED:
                    break;
            }

            int id = node.getId();
            nodeBuilder.put(id, node);

            int index = 0;
            for (Port port : connectedNode.getInputs()) {
                edgesBuilder.put(node.getInputPort(index), port);
                index++;
            }

            index = 0;
            for (Port port : connectedNode.getOutputs()) {
                edgesBuilder.put(node.getOutputPort(index), port);
                index++;
            }
        }

        this.name = name;
        this.nodes = nodeBuilder.build();
        this.edges = edgesBuilder.build();
        Preconditions.checkArgument(input != null, "Found no input node");
        Preconditions.checkArgument(output != null, "Found no output node");
        this.input = input;
        this.output = output;
    }

    public String getName() {
        return name;
    }

    public Node getNode(int id) {
        return nodes.get(id);
    }

    /**
     * Gets nodes in topological order.
     * @return An iterable with all nodes in topological order.
     */
    public Iterable<Node> getNodes() {
        return nodes.values();
    }

    public Port getPort(Port port) {
        return edges.get(port);
    }

    public Node getInput() {
        return input;
    }

    public Node getOutput() {
        return output;
    }
}
