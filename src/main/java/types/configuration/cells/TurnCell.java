package types.configuration.cells;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import types.Direction;

public class TurnCell extends AbstractCell{
    public TurnCell(Direction in, Direction out) {
        super(ImmutableList.of(in), ImmutableList.of(out));
        Preconditions.checkArgument(in != out);
        Preconditions.checkArgument(in.opposite() != out);
    }

    @Override
    public CellType getCellType() {
        return CellType.TURN;
    }
}
