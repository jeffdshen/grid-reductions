package types;

import java.util.List;

public class ConnectedNode {
    private final Node node;
    private final List<Port> inputs;
    private final List<Port> outputs;

    public ConnectedNode(Node node, List<Port> inputs, List<Port> outputs) {
        this.node = node;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public Node getNode() {
        return node;
    }

    public List<Port> getInputs() {
        return inputs;
    }

    public List<Port> getOutputs() {
        return outputs;
    }
}
