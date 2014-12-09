package types.configuration.cells;

import com.google.common.collect.ImmutableBiMap;
import types.Direction;
import types.configuration.nodes.Port;

import java.util.Map;

public class PortCell extends AbstractCell {
    private final String name;
    private final ImmutableBiMap<Direction, Integer> ports;

    public PortCell(String name, Map<Direction, Port> ports) {
        super(ports);
        this.name = name;
        this.ports = portToNumber(ports);
    }

    public PortCell(
        String name, Iterable<Direction> inputs, Iterable<Direction> outputs, Map<Direction, Integer> ports
    ) {
        super(inputs, outputs);
        this.name = name;
        this.ports = ImmutableBiMap.copyOf(ports);
    }


    private static ImmutableBiMap<Direction, Integer> portToNumber(Map<Direction, Port> ports) {
        ImmutableBiMap.Builder<Direction, Integer> builder = ImmutableBiMap.builder();
        for (Map.Entry<Direction, Port> e : ports.entrySet()) {
            builder.put(e.getKey(), e.getValue().getPortNumber());
        }
        return builder.build();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPortNumber(Direction d) {
        return ports.get(d);
    }

    @Override
    public CellType getCellType() {
        return CellType.PORT;
    }
}
