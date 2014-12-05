package types.configuration.cells;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import types.Direction;

public class CrossoverCell extends AbstractCell {
    /**
     * The output directions.
     */
    public CrossoverCell(Direction d1, Direction d2) {
        super(ImmutableList.of(d1.opposite(), d2.opposite()), ImmutableList.of(d1, d2));
        Preconditions.checkArgument(d1 != d2);
        Preconditions.checkArgument(d1.opposite() != d2);
    }

    @Override
    public CellType getCellType() {
        return CellType.CROSSOVER;
    }
}
