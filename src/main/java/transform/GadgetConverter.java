package transform;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import types.Direction;
import types.Gadget;
import types.Grid;
import types.Location;
import types.configuration.GridConfiguration;
import types.configuration.cells.Cell;
import types.configuration.cells.NodeCell;
import types.configuration.cells.PortCell;

public class GadgetConverter {
    public GridConfiguration toGridConfiguration(Gadget g) {
        int sizeX = 1;
        int sizeY = 1;
        for (Direction d : Direction.values()) {
            Direction step = d.clockwise();
            int count = 0;
            for (Location loc = getStart(g, d, step); g.isValid(loc); loc = loc.add(step)) {
                if (g.isInput(loc) || g.isOutput(loc)) {
                    count++;
                }
            }
            sizeX = Math.max(sizeX, count * Math.abs(step.getX()));
            sizeY = Math.max(sizeY, count * Math.abs(step.getY()));
        }

        GridConfiguration grid = new GridConfiguration(new NodeCell(g.getName()), sizeX, sizeY);
        for (Direction d : Direction.values()) {
            Direction step = d.clockwise();
            Location gadgetLoc = getStart(g, d, step);
            for (Location loc = getStart(grid, d, step); grid.isValid(loc); loc = loc.add(step), gadgetLoc = gadgetLoc.add(step)) {
                while (g.isValid(gadgetLoc) && !(g.isInput(gadgetLoc) || g.isOutput(gadgetLoc))) {
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
                if (g.isInput(gadgetLoc)) {
                    inputs = Iterables.concat(inputs, ImmutableList.of(d));
                    portsBuilder.put(d, g.getInputNumber(gadgetLoc));
                } else {
                    outputs = Iterables.concat(outputs, ImmutableList.of(d));
                    portsBuilder.put(d, g.getOutputNumber(gadgetLoc));
                }

                grid.put(new PortCell(c.getName(), inputs, outputs, portsBuilder.build()), loc);
            }
        }


        return grid;
    }

    private Location getStart(Grid g, Direction side, Direction step) {
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
