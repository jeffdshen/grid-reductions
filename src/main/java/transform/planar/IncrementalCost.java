package transform.planar;

import types.Location;
import types.configuration.CellConfiguration;
import types.configuration.cells.Cell;

public class IncrementalCost extends AbstractCostFunction implements CostFunction {
    public IncrementalCost(CellConfiguration grid, GadgetSet gadgets) {
        super(grid, gadgets);
    }

    @Override
    public int getCost(Cell c, Location loc) {
        return getCost(c, loc, 10000);
    }
}
