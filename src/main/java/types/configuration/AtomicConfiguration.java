package types.configuration;

import com.google.common.collect.*;
import types.configuration.nodes.*;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * A configuration broken down into "atoms," or components that are not represented by a sub-configuration.
 */
public class AtomicConfiguration {
    private ImmutableMap<String, Configuration> subs;
    private Configuration config;
    private ImmutableSet<String> atoms;

    public AtomicConfiguration(Configuration config, Iterable<Configuration> subs, Set<String> atoms) {
        ImmutableMap.Builder<String, Configuration> builder = ImmutableMap.builder();
        for (Configuration conf : subs) {
            builder.put(conf.getName(), conf);
        }
        this.subs = builder.build();
        this.config = config;
        this.atoms = ImmutableSet.copyOf(atoms);
    }

    public String getName() {
        return config.getName();
    }

    public AtomicNode getNode(List<Integer> context, int id) {
        Configuration cur = config;
        for (int node : context) {
            cur = subs.get(cur.getNode(node).getName());
        }

        return new AtomicNode(context, cur.getNode(id));
    }

    /**
     * Returns an iterable of atomic nodes, in topological order.
     * @return an iterable of atomic nodes.
     */
    public Iterable<AtomicNode> getNodes() {
        return new Iterable<AtomicNode>() {
            @Override
            public Iterator<AtomicNode> iterator() {
                return new ConfigIterator();
            }
        };
    }

    /**
     * Gets the connecting port to this one
     * @param port the given port
     * @return A port connected to the given one.
     */
    public AtomicPort getConnectingPort(AtomicPort port) {
        // go down
        Stack<Configuration> configStack = new Stack<>();
        configStack.push(config);
        Stack<Node> nodeStack = new Stack<>();
        Stack<Integer> context = new Stack<>();

        // node, context at the top of stack always represents config at top of stack
        for (int node : port.getContext()) {
            nodeStack.push(configStack.peek().getNode(node));
            configStack.push(subs.get(nodeStack.peek().getName()));
            context.push(node);
        }

        Configuration curConfig = configStack.peek();
        Port curPort = curConfig.getPort(port.getPort());

        // come back up if at an input or output node
        while (curConfig.getNode(curPort.getId()).getType() != NodeType.LABELLED) {
            if (context.empty()) {
                // cant go any farther, just return the top level input or output node
                return new AtomicPort(context, curPort);
            }
            configStack.pop();
            curConfig = configStack.peek();
            Node curNode = nodeStack.peek();
            if (curPort.isInput()) {
                curPort = curNode.getOutputPort(curPort.getPortNumber());
            } else {
                curPort = curNode.getInputPort(curPort.getPortNumber());
            }
            curPort = curConfig.getPort(curPort);

            nodeStack.pop();
            context.pop();
        }

        // go into a substitution
        while (subs.containsKey(curConfig.getNode(curPort.getId()).getName())) {
            context.push(curPort.getId());
            nodeStack.push(curConfig.getNode(curPort.getId()));

            Node curNode = nodeStack.peek();
            curConfig = subs.get(curNode.getName());
            if (curPort.isInput()) {
                curNode = curConfig.getInput();
                curPort = curNode.getOutputPort(curPort.getPortNumber());
            } else {
                curNode = curConfig.getOutput();
                curPort = curNode.getInputPort(curPort.getPortNumber());
            }
            curPort = curConfig.getPort(curPort);
            configStack.push(curConfig);
        }

        return new AtomicPort(context, curPort);
    }

    public AtomicNode getInput() {
        return new AtomicNode(ImmutableList.<Integer>of(), config.getInput());
    }

    public AtomicNode getOutput() {
        return new AtomicNode(ImmutableList.<Integer>of(), config.getOutput());
    }

    private class ConfigIterator implements Iterator<AtomicNode> {
        private Stack<Integer> context;
        private Stack<PeekingIterator<Node>> iterators;
        private boolean hasNext;

        public ConfigIterator() {
            context = new Stack<>();
            iterators = new Stack<>();
            iterators.push(Iterators.peekingIterator(config.getNodes().iterator()));
            hasNext = load();
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public AtomicNode next() {
            AtomicNode node = new AtomicNode(context, iterators.peek().next());
            hasNext = load();
            return node;
        }

        /**
         * Advances the iterator until the next element is a labelled node. Returns true if there is another element.
         * Returns false otherwise.
         */
        public boolean load() {
            if (iterators.empty()) {
                return false;
            }

            PeekingIterator<Node> it = iterators.peek();
            while(it.peek().getType() != NodeType.LABELLED || !atoms.contains(it.peek().getName())) {
                Node next = it.peek();
                switch (next.getType()) {
                    case INPUT:
                        it.next();
                        break;
                    case OUTPUT:
                        iterators.pop();
                        if (iterators.empty()) {
                            return false;
                        }

                        context.pop();
                        it = iterators.peek();
                        it.next();
                        break;
                    case LABELLED:
                        if (!subs.containsKey(next.getName())) {
                            throw new IllegalStateException(
                                String.format("No substitution found for gadget: %s", next.getName())
                            );
                        }
                        iterators.push(Iterators.peekingIterator(subs.get(next.getName()).getNodes().iterator()));
                        context.push(next.getId());
                        it = iterators.peek();
                        break;
                }
            }

            return true;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
