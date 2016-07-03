package types.configuration.nodes;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * An node holding an "atom," or a component that is not represented by any sub-configuration.
 */
public class AtomicNode {
    private final ImmutableList<Integer> id;
    private final ImmutableList<Integer> context;
    private final Node node;

    public AtomicNode(Iterable<Integer> context, Node node) {
        this.context = ImmutableList.copyOf(context);
        this.node = node;
        this.id = ImmutableList.<Integer>builder().addAll(context).add(node.getId()).build();
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

    public List<Integer> getId() {
        return id;
    }

    public String getName() {
        return node.getName();
    }

    public NodeType getType() {
        return node.getType();
    }

    @Override
    public String toString() {
        return getClass().getName() + "[context=" + context + ",node=" + node + "]";
    }

}
