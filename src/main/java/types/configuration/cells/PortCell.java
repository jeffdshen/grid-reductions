package types.configuration.cells;

import com.google.common.collect.ImmutableBiMap;
import types.Direction;
import types.configuration.nodes.Port;

import java.util.Map;

public class PortCell extends AbstractCell {
    private final String name;
    private final ImmutableBiMap<Direction, Port> ports;

    public PortCell(String name, Map<Direction, Port> ports) {
        super(ports);
        this.name = name;
        this.ports = ImmutableBiMap.copyOf(ports);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPortNumber(Direction d) {
        return ports.get(d).getPortNumber();
    }

    @Override
    public CellType getCellType() {
        return CellType.PORT;
    }
}
