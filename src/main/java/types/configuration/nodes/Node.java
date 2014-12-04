package types.configuration.nodes;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * A node in a graph with in-edges and out-edges.
 * If a node is labelled, it should have a name.
 * Each id is under its own scope.
 */
public class Node {
    private final NodeType type;
    private final String name;
    private final int id;
    private final List<Port> inputs;
    private final List<Port> outputs;

    public Node(NodeType type, String name, int id, int inputs, int outputs) {
        this.type = type;
        this.name = name;
        this.id = id;

        ImmutableList.Builder<Port> inputsBuilder = ImmutableList.builder();
        for (int i = 0; i < inputs; i++) {
            inputsBuilder.add(new Port(id, true, i));
        }
        this.inputs = inputsBuilder.build();
        ImmutableList.Builder<Port> outputsBuilder = ImmutableList.builder();
        for (int i = 0; i < outputs; i++) {
            outputsBuilder.add(new Port(id, false, i));
        }
        this.outputs = outputsBuilder.build();
    }

    public List<Port> getInputPorts() {
        return inputs;
    }

    public List<Port> getOutputPorts() {
        return outputs;
    }

    public Port getInputPort(int i) {
        return inputs.get(i);
    }

    public Port getOutputPort(int i) {
        return outputs.get(i);
    }

    public int inputSize() {
        return inputs.size();
    }
    public int outputSize() {
        return outputs.size();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public NodeType getType() {
        return type;
    }
}
