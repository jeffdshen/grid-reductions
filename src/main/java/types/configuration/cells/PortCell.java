package types.configuration.cells;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import types.Direction;
import types.configuration.nodes.Port;

import java.util.List;
import java.util.Map;

public class PortCell extends AbstractCell {
    private final String name;
    private final ImmutableList<Integer> id;
    private final ImmutableMap<Direction, Integer> ports;

    public PortCell(String name, List<Integer> id, Map<Direction, Port> ports) {
        super(ports);
        this.name = name;
        this.id = ImmutableList.copyOf(id);
        this.ports = portToNumber(ports);
    }

    public PortCell(
        String name,
        List<Integer> id,
        Iterable<Direction> inputs,
        Iterable<Direction> outputs,
        Map<Direction, Integer> ports
    ) {
        super(inputs, outputs);
        this.name = name;
        this.id = ImmutableList.copyOf(id);
        this.ports = ImmutableMap.copyOf(ports);
    }

    private static ImmutableMap<Direction, Integer> portToNumber(Map<Direction, Port> ports) {
        ImmutableMap.Builder<Direction, Integer> builder = ImmutableMap.builder();
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
    public List<Integer> getId() {
        return id;
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
