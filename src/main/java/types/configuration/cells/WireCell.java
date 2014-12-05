package types.configuration.cells;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import types.Direction;

public class WireCell extends AbstractCell {
    private static final ImmutableBiMap<Direction, WireCell> WIRES = constructWires();

    private WireCell(Direction d) {
        super(ImmutableList.of(d.opposite()), ImmutableList.of(d));
    }

    public static WireCell getWire(Direction d) {
        return WIRES.get(d);
    }

    private static ImmutableBiMap<Direction, WireCell> constructWires() {
        ImmutableBiMap.Builder<Direction, WireCell> builder = ImmutableBiMap.builder();
        for (Direction d : Direction.values()) {
            builder.put(d, new WireCell(d));
        }
        return builder.build();
    }

    @Override
    public CellType getCellType() {
        return CellType.WIRE;
    }
}
