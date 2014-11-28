package types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Stack;

/**
 * Created by jdshen on 11/28/14.
 */
public class AtomicConfiguration {
    public ImmutableMap<String, Configuration> subs;
    public Configuration config;

    public AtomicConfiguration(Configuration config, Iterable<Configuration> subs) {
        this.config = config;
        ImmutableMap.Builder<String, Configuration> builder = ImmutableMap.builder();
        for (Configuration conf : subs) {
            builder.put(conf.getName(), conf);
        }
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

    // TODO Should be okay once config iterator is done.
    /*
    public Iterable<AtomicNode> getNodes() {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new ConfigIterator();
            }
        }
    }
    */

    public AtomicPort getPort(AtomicPort port) {
        // go down
        Stack<Configuration> configStack = new Stack<Configuration>();
        configStack.push(config);
        Stack<Node> nodeStack = new Stack<Node>();
        Stack<Integer> context = new Stack<Integer>();

        for (int node : port.getContext()) {
            nodeStack.push(configStack.peek().getNode(node));
            configStack.push(subs.get(nodeStack.peek().getName()));
            context.push(node);
        }

        // come back up if at an input or output node
        Configuration curConfig = configStack.peek();
        Port curPort = curConfig.getPort(port.getPort());

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
            nodeStack.pop();
            context.pop();
        }
        return new AtomicPort(context, curPort);
    }

    public AtomicNode getInput() {
        return new AtomicNode(ImmutableList.<Integer>of(), config.getInput());
    }

    public AtomicNode getOutput() {
        return new AtomicNode(ImmutableList.<Integer>of(), config.getOutput());
    }

    //TODO iterator that goes through atomic nodes one at a time, skipping over input and output nodes
    /*
    private class ConfigIterator implements Iterator<AtomicNode> {
        private Stack<Integer> context;
        private Stack<PeekingIterator<Node>> iterators;

        public ConfigIterator() {
            context = new Stack<Integer>();
            iterators = new Stack<PeekingIterator<Node>>();
        }

        @Override
        public boolean hasNext() {
            if (iterators.peek().peek())
        }

        @Override
        public AtomicNode next() {
            return null;
        }

        @Override
        public void remove() {

        }
    }
        */
}
