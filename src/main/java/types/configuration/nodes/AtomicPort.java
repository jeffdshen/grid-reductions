package types.configuration.nodes;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class AtomicPort {
    private final ImmutableList<Integer> context;
    private final Port port;

    public AtomicPort(Iterable<Integer> context, Port port) {
        this.context = ImmutableList.copyOf(context);
        this.port = port;
    }

    public List<Integer> getContext() {
        return context;
    }

    public Port getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AtomicPort) {
            AtomicPort that = (AtomicPort) o;
            return context == that.context && port == that.port;
        }
        return super.equals(o);
    }
}
