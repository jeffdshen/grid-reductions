package transform;

import types.Direction;
import types.Gadget;
import types.Location;
import types.Side;
import types.configuration.CellConfiguration;
import types.configuration.cells.NodeCell;

import java.util.List;

public class GadgetConverter {
    public CellConfiguration toGridConfiguration(Gadget g, List<Integer> id) {
        int sizeX = 1;
        int sizeY = 1;
        for (Direction d : Direction.values()) {
            Direction step = d.clockwise();
            int count = 0;
            for (Location loc = GridUtils.getCorner(g, d, step.opposite()); g.isValid(loc); loc = loc.add(step)) {
                Side side = new Side(loc, d);
                if (g.isInput(side) || g.isOutput(side)) {
                    count++;
                }
            }
            sizeX = Math.max(sizeX, count * Math.abs(step.getX()));
            sizeY = Math.max(sizeY, count * Math.abs(step.getY()));
        }

        CellConfiguration grid = new CellConfiguration(new NodeCell(g.getName(), id), sizeX, sizeY);
        for (Direction d : Direction.values()) {
            Direction step = d.clockwise();
            Side side = new Side(GridUtils.getCorner(grid, d, step.opposite()), d);
            Side gadgetSide = new Side(GridUtils.getCorner(g, d, step.opposite()), d);
            for (; grid.isValid(side.getLocation()); side = side.add(step), gadgetSide = gadgetSide.add(step)) {
                while (g.isValid(gadgetSide.getLocation()) && !(g.isInput(gadgetSide) || g.isOutput(gadgetSide))) {
                    gadgetSide = gadgetSide.add(step);
                }

                if (!g.isValid(gadgetSide.getLocation())) {
                    continue;
                }

                boolean isInput = g.isInput(gadgetSide);
                grid.putPort(side, isInput, isInput ? g.getInputNumber(gadgetSide) : g.getOutputNumber(gadgetSide));
            }
        }


        return grid;
    }
}
