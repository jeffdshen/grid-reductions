package types;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;

import java.util.List;

/**
 * A generic gadget composed of grid cells.
 */
public class Gadget {
    private final String[][] cells;
    private final int sizeX;
    private final int sizeY;
    private final String name;
    private final ImmutableBiMap<Integer, Location> inputs;
    private final ImmutableBiMap<Integer, Location> outputs;

    public Gadget(String name, String[][] cells, List<Location> inputs, List<Location> outputs) {
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

    private static ImmutableBiMap<Integer, Location> getLocationMap(List<Location> locs) {
        ImmutableBiMap.Builder<Integer, Location> builder = ImmutableBiMap.builder();
        int i = 0;
        for (Location loc : locs) {
            builder.put(i, loc);
            i++;
        }
        return builder.build();
    }

    public boolean isValid(Location loc) {
        return loc.getX() >= 0 && loc.getX() < sizeX && loc.getY() >= 0 && loc.getY() < sizeY;
    }

    public String getCell(Location loc) {
        return cells[loc.getX()][loc.getY()];
    }

    public String getCell(int x, int y) {
        return cells[x][y];
    }

    public String getName() {
        return name;
    }

    public int getInputSize() {
        return inputs.size();
    }

    public int getOutputSize() {
        return outputs.size();
    }

    public boolean isInput(Location loc) {
        return inputs.containsValue(loc);
    }

    public boolean isInput(int x, int y) {
        return isInput(new Location(x, y));
    }

    public boolean isOutput(Location loc) {
        return outputs.containsValue(loc);
    }

    public boolean isOutput(int x, int y) {
        return isOutput(new Location(x, y));
    }

    public Location getInput(int index) {
        return inputs.get(index);
    }

    public Location getOutput(int index) {
        return outputs.get(index);
    }
}
