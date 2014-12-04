package types.configuration.cells;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class TurnCell implements Cell{
    private final Direction in;
    private final Direction out;

    public TurnCell(Direction in, Direction out) {
        Preconditions.checkArgument(in != out);
        Preconditions.checkArgument(in.opposite() != out);

        this.in = in;
        this.out = out;
    }

    @Override
    public int inputSize() {
        return 1;
    }

    @Override
    public int outputSize() {
        return 1;
    }

    @Override
    public List<Direction> getInputDirections() {
        return ImmutableList.of(in);
    }

    @Override
    public List<Direction> getOutputDirections() {
        return ImmutableList.of(out);
    }

    @Override
    public Direction getInputDirection(int i) {
        if (i < 0 || i >= 1) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        return in;
    }

    @Override
    public Direction getOutputDirection(int i) {
        if (i < 0 || i >= 1) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        return out;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPortNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellType getCellType() {
        return CellType.TURN;
    }
}
