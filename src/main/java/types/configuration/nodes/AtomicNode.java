package types.configuration.nodes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.List;

public class AtomicNode {
    private final Iterable<Integer> id;
    private final ImmutableList<Integer> context;
    private final Node node;

    public AtomicNode(Iterable<Integer> context, Node node) {
        this.context = ImmutableList.copyOf(context);
        this.node = node;
        this.id = Iterables.concat(context, ImmutableList.of(node.getId()));
    }

    public List<Integer> getContext() {
        return context;
    }

    public Node getNode() {
        return node;
    }

    public AtomicPort getInputPort(int i) {
        return new AtomicPort(context, node.getInputPort(i));
    }

    public AtomicPort getOutputPort(int i) {
        return new AtomicPort(context, node.getInputPort(i));
    }

    public int inputSize() {
        return node.inputSize();
    }

    public int outputSize() {
        return node.outputSize();
    }

    public Iterable<Integer> getId() {
        return id;
    }

    public String getName() {
        return node.getName();
    }

    public NodeType getType() {
        return node.getType();
    }
}
