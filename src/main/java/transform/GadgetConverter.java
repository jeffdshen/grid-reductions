package transform;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import types.*;
import types.configuration.CellConfiguration;
import types.configuration.cells.Cell;
import types.configuration.cells.NodeCell;
import types.configuration.cells.PortCell;

import java.util.List;

public class GadgetConverter {
    public CellConfiguration toGridConfiguration(Gadget g, List<Integer> id) {
        int sizeX = 1;
        int sizeY = 1;
        for (Direction d : Direction.values()) {
            Direction step = d.clockwise();
            int count = 0;
            for (Location loc = getStart(g, d, step); g.isValid(loc); loc = loc.add(step)) {
                if (isInput(g, loc) || isOutput(g, loc)) {
                    count++;
                }
            }
            sizeX = Math.max(sizeX, count * Math.abs(step.getX()));
            sizeY = Math.max(sizeY, count * Math.abs(step.getY()));
        }

        CellConfiguration grid = new CellConfiguration(new NodeCell(g.getName(), id), sizeX, sizeY);
        for (Direction d : Direction.values()) {
            Direction step = d.clockwise();
            Location gadgetLoc = getStart(g, d, step);
            for (Location loc = getStart(grid, d, step); grid.isValid(loc); loc = loc.add(step), gadgetLoc = gadgetLoc.add(step)) {
                while (g.isValid(gadgetLoc) && !(isInput(g, gadgetLoc) || isOutput(g, gadgetLoc))) {
                    gadgetLoc = gadgetLoc.add(step);
                }

                if (!g.isValid(gadgetLoc)) {
                    continue;
                }

                Cell c = grid.getCell(loc);
                Iterable<Direction> inputs = c.getInputDirections();
                Iterable<Direction> outputs = c.getOutputDirections();

                ImmutableMap.Builder<Direction, Integer> portsBuilder = ImmutableMap.builder();
                for (Direction input : inputs) {
                    portsBuilder.put(input, c.getPortNumber(input));
                }

                for (Direction output : outputs) {
                    portsBuilder.put(output, c.getPortNumber(output));
                }

                // add direction->port
                if (isInput(g, gadgetLoc)) {
                    inputs = Iterables.concat(inputs, ImmutableList.of(d));
                    portsBuilder.put(d, getInputNumber(g, gadgetLoc));
                } else {
                    outputs = Iterables.concat(outputs, ImmutableList.of(d));
                    portsBuilder.put(d, getOutputNumber(g, gadgetLoc));
                }

                grid.put(new PortCell(c.getName(), id, inputs, outputs, portsBuilder.build()), loc);
            }
        }


        return grid;
    }

    // TODO fix?
    // Used for the hot fix for the new gadget format. Does not handle when the location is on corner properly.
    private Side getSide(Gadget g, Location loc) {
        for (Direction dir : Direction.values()) {
            if (!g.isValid(loc.add(dir))) {
                return new Side(loc, dir);
            }
        }

        return null;
    }

    // Hot fix for new gadget format. Does not handle when the input is on corner properly.
    private boolean isInput(Gadget g, Location loc) {
        return g.isInput(getSide(g, loc));
    }

    // Hot fix for new gadget format. Does not handle when the output is on corner properly.
    private boolean isOutput(Gadget g, Location loc) {
        return g.isOutput(getSide(g, loc));
    }

    // Hot fix for new gadget format. Does not handle when the input is on corner properly.
    private int getInputNumber(Gadget g, Location loc) {
        return g.getInputNumber(getSide(g, loc));
    }

    // Hot fix for new gadget format. Does not handle when the output is on corner properly.
    private int getOutputNumber(Gadget g, Location loc) {
        return g.getOutputNumber(getSide(g, loc));
    }


    private static Location getStart(Grid g, Direction side, Direction step) {
        Location[] corners = new Location[]{
                new Location(0, 0),
                new Location(0, g.getSizeY() - 1),
                new Location(g.getSizeX() - 1, g.getSizeY() - 1),
                new Location(g.getSizeX() - 1, 0),
        };

        for (Location loc : corners) {
            // on the side and either adding a step is valid or degenerate state where adding +- step are both bad
            if (!g.isValid(loc.add(side)) && (g.isValid(loc.add(step)) || !g.isValid(loc.add(step.opposite())))) {
                return loc;
            }
        }

        throw new IllegalArgumentException();
    }
}
