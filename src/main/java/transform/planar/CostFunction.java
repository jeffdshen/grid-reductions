package transform.planar;

import types.Location;
import types.configuration.cells.Cell;

public interface CostFunction {
    int getTotalCost();
    int getCost(Cell c, Location loc);
}
