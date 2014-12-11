package types.configuration.nodes;

public class Port {
    private final int id;
    private final boolean input;
    private final int port;

    public Port(int id, boolean input, int port) {
        this.id = id;
        this.input = input;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public boolean isInput() {
        return input;
    }

    public int getPortNumber() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Port) {
            Port that = (Port) o;
            return id == that.id && input == that.input && port == that.port;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.id;
        hash = 71 * hash + (this.input ? 1 : 0);
        hash = 71 * hash + this.port;
        return hash;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[id=" + id + ",input=" + input + ",port=" + port + "]";
    }
}
