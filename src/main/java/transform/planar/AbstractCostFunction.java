package transform.planar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import transform.GridUtils;
import types.Direction;
import types.Location;
import types.configuration.CellConfiguration;
import types.configuration.cells.Cell;
import types.configuration.cells.CellType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbstractCostFunction {
    private CellConfiguration grid;

    private final GadgetSet gadgets;
    private Set<List<Integer>> largeNodes;

    private List<Integer> costX;
    private List<Integer> costY;
    private int totalCost;

    public AbstractCostFunction(CellConfiguration grid, GadgetSet gadgets) {
        this.grid = grid;
        this.gadgets = gadgets;
        largeNodes = getLargeNodes(grid);
        initCosts();
    }

    private static Set<List<Integer>> getLargeNodes(CellConfiguration grid) {
        HashSet<List<Integer>> set = new HashSet<>();
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                Cell c = grid.getCell(i, j);
                if (c.getCellType() == CellType.NODE) {
                    set.add(c.getId());
                }
            }
        }

        return ImmutableSet.copyOf(set);
    }

    /**
     * Used to determine initial cost
     */
    private int getBaseCost(Cell c, Direction dir) {
        switch (c.getCellType()) {
            case EMPTY:
                return 0;
            case WIRE:
                return dir.parallel(c.getOutputDirection(0)) ? 0 : gadgets.wirer.minThickness(c.getOutputDirection(0));
            case TURN:
                List<Direction> dirs = ImmutableList.of(c.getInputDirection(0), c.getOutputDirection(0));
                return GridUtils.getSize(gadgets.turns.get(dirs), dir);
            case CROSSOVER:
                Set inputDirs = ImmutableSet.copyOf(c.getInputDirections());
                return GridUtils.getSize(gadgets.crossovers.get(inputDirs), dir);
            case NODE:
                return 0;
            case PORT:
                if (largeNodes.contains(c.getId())) {
                    // TODO
                    return 20;
                }

                return GridUtils.getSize(gadgets.gadgets.get(c.getName()), dir);
        }

        return 0;
    }

    private int getBaseCostX(Cell c) {
        return getBaseCost(c, Direction.getDirection(1, 0));
    }

    private int getBaseCostY(Cell c) {
        return getBaseCost(c, Direction.getDirection(0, 1));
    }

    /**
     * Used to determine incremental cost
     */
    private int getIncrementalCost(Cell c, Direction dir) {
        switch (c.getCellType()) {
            case EMPTY:
                return 0;
            case WIRE:
                return 0; // assume this is less than a turn/whatever gadget it is connected to.
            case TURN:
                List<Direction> dirs = ImmutableList.of(c.getInputDirection(0), c.getOutputDirection(0));
                return GridUtils.getSize(gadgets.turns.get(dirs), dir);
            case CROSSOVER:
                Set inputDirs = ImmutableSet.copyOf(c.getInputDirections());
                return GridUtils.getSize(gadgets.crossovers.get(inputDirs), dir);
            case NODE:
                return 0;
            case PORT:
                if (largeNodes.contains(c.getId())) {
                    // TODO
                    return 20;
                }

                return GridUtils.getSize(gadgets.gadgets.get(c.getName()), dir);
        }

        return 0;
    }

    private int getIncrementalCostX(Cell c) {
        return getIncrementalCost(c, Direction.getDirection(1, 0));
    }

    private int getIncrementalCostY(Cell c) {
        return getIncrementalCost(c, Direction.getDirection(0, 1));
    }

    private void initCosts() {
        costX = new ArrayList<>();
        costY = new ArrayList<>();
        totalCost = 0;
        for (int i = 0; i < grid.getSizeX(); i++) {
            costX.add(0);
        }

        for (int i = 0; i < grid.getSizeY(); i++) {
            costY.add(0);
        }

        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                Cell c = grid.getCell(i, j);
                costX.set(i, Math.max(costX.get(i), getBaseCostX(c)));
                costY.set(j, Math.max(costY.get(j), getBaseCostY(c)));
            }
        }

        for (int c : costX) {
            totalCost += c;
        }

        for (int c : costY) {
            totalCost += c;
        }
    }

    public int getTotalCost() {
        return totalCost;
    }

    public int getCost(Cell c, Location loc, int incrWeight) {
        int x = getIncrementalCostX(c);
        int y = getIncrementalCostY(c);
        int curX = costX.get(loc.getX());
        int curY = costY.get(loc.getY());

        return (curX <= x ? x - curX : 0) * incrWeight + (curY <= y ? y - curY : 0) * incrWeight
            + getBaseCostX(c) + getBaseCostY(c) + 1;
    }
}
