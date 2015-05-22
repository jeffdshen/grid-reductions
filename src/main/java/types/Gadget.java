package types;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;

import java.util.List;

/**
 * A generic gadget composed of grid cells.
 */
public class Gadget implements Grid<String> {
    private final String[][] cells;
    private final int sizeX;
    private final int sizeY;
    private final String name;
    private final ImmutableBiMap<Integer, Side> inputs;
    private final ImmutableBiMap<Integer, Side> outputs;

    public Gadget(String name, String[][] cells, List<Side> inputs, List<Side> outputs) {
        Preconditions.checkNotNull(cells);
        Preconditions.checkArgument(cells.length > 0);
        Preconditions.checkArgument(cells[0].length > 0);

        this.sizeX = cells.length;
        this.sizeY = cells[0].length;
        this.cells = cells;
        this.name = name;

        this.inputs = getLocationMap(inputs);
        this.outputs = getLocationMap(outputs);
    }

    private static ImmutableBiMap<Integer, Side> getLocationMap(List<Side> sides) {
        ImmutableBiMap.Builder<Integer, Side> builder = ImmutableBiMap.builder();
        int i = 0;
        for (Side side : sides) {
            builder.put(i, side);
            i++;
        }
        return builder.build();
    }

    @Override
    public boolean isValid(Location loc) {
        return isValid(loc.getX(), loc.getY());
    }

    @Override
    public boolean isValid(int x, int y) {
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY;
    }

    @Override
    public String getCell(Location loc) {
        return cells[loc.getX()][loc.getY()];
    }

    @Override
    public String getCell(int x, int y) {
        return cells[x][y];
    }

    public String getName() {
        return name;
    }

    @Override
    public int getSizeX() {
        return sizeX;
    }

    @Override
    public int getSizeY() {
        return sizeY;
    }

    public int getInputSize() {
        return inputs.size();
    }

    public int getOutputSize() {
        return outputs.size();
    }

    public boolean isInput(Side side) {
        return inputs.containsValue(side);
    }

    public boolean isInput(Location loc, Direction dir) {
        return inputs.containsValue(new Side(loc, dir));
    }

    public boolean isInput(int x, int y, Direction dir) {
        return isInput(new Location(x, y), dir);
    }

    public int getInputNumber(Side side) {
        return inputs.inverse().get(side);
    }

    public boolean isOutput(Side side) {
        return outputs.containsValue(side);
    }

    public boolean isOutput(Location loc, Direction dir) {
        return isOutput(new Side(loc, dir));
    }
    public boolean isOutput(int x, int y, Direction dir) {
        return isOutput(new Location(x, y), dir);
    }

    public int getOutputNumber(Side side) {
        return outputs.inverse().get(side);
    }

    public Side getInput(int index) {
        return inputs.get(index);
    }

    public Side getOutput(int index) {
        return outputs.get(index);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(
            "Gadget(sizeX=%s, sizeY=%s, inputs=%s, outputs=%s, cells=\n", sizeX, sizeY, inputs, outputs
        ));

        // print transposed
        for (int i = 0; i < sizeY; i++) {
            for (int j = 0; j < sizeX; j++) {
                builder.append(cells[j][i]);
            }
            builder.append("\n");
        }
        builder.append(")");
        return builder.toString();
    }
}
