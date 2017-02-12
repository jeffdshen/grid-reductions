package transform.planar;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import transform.GridUtils;
import types.Direction;
import types.Location;
import types.MutableGrid;
import types.Side;
import types.configuration.AtomicConfiguration;
import types.configuration.CellConfiguration;
import types.configuration.cells.*;
import types.configuration.nodes.AtomicNode;
import types.configuration.nodes.AtomicPort;
import types.configuration.nodes.Port;

import java.util.*;

public class PlanarUtils {
    /**
     * Given a node, returns a list of places that will fit it
     */
    public static List<Location> getPlacements(CellConfiguration grid, CellConfiguration node) {
        // leave space for wires
        int x = node.getSizeX() + 2;
        int y = node.getSizeY() + 2;

        // how many of the previous cells along the x slice are open
        int[][] sliceX = new int[grid.getSizeX()][grid.getSizeY()];

        // how many of the previous cells along the y slice have sliceX >= x
        int[][] sliceY = new int[grid.getSizeX()][grid.getSizeY()];

        // use dp to calculate sliceX
        for (int i = 1; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                if (grid.getCell(i, j).getCellType() == CellType.EMPTY) {
                    sliceX[i][j] = sliceX[i - 1][j] + 1;
                } else {
                    sliceX[i][j] = 0;
                }
            }
        }

        // use dp to calculate sliceY
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 1; j < grid.getSizeY(); j++) {
                if (sliceX[i][j] >= x) {
                    sliceY[i][j] = sliceY[i][j - 1] + 1;
                } else {
                    sliceY[i][j] = 0;
                }
            }
        }

        ImmutableList.Builder<Location> locs = ImmutableList.builder();
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                if (sliceY[i][j] >= y) {
                    // offset because of boundary to leave space for wires
                    locs.add(new Location(i - (x - 1) + 1, j - (y - 1) + 1));
                }
            }
        }
        return locs.build();
    }

    public static Side findPort(CellConfiguration grid, AtomicPort port) {
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                Cell c = grid.getCell(i, j);

                if (c.getCellType() != CellType.PORT || !c.getId().equals(port.getId())) {
                    continue;
                }

                for (Direction d : Direction.values()) {
                    Port p = port.getPort();
                    if (((p.isInput() && c.isInput(d)) || (!p.isInput() && c.isOutput(d)))
                        && p.getPortNumber() == c.getPortNumber(d)) {
                        return new Side(i, j, d);
                    }
                }
            }
        }

        throw new IllegalArgumentException("Expected port not found");
    }

    public static BiMap<AtomicPort, Side> findPorts(CellConfiguration grid, AtomicNode node) {
        List<Integer> id = node.getId();
        ImmutableBiMap.Builder<AtomicPort, Side> builder = ImmutableBiMap.builder();
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                Cell c = grid.getCell(i, j);

                if (c.getCellType() != CellType.PORT || !c.getId().equals(id)) {
                    continue;
                }

                for (Direction d : Direction.values()) {
                    if (c.isInput(d)) {
                        builder.put(node.getInputPort(c.getPortNumber(d)), new Side(i, j, d));
                    } else if (c.isOutput(d)){
                        builder.put(node.getOutputPort(c.getPortNumber(d)), new Side(i, j, d));
                    }
                }
            }
        }

        return builder.build();
    }

    public static BiMap<AtomicPort, Side> findConnectingPorts(
        CellConfiguration grid, AtomicConfiguration config, Iterable<AtomicPort> ports
    ) {
        ImmutableBiMap.Builder<AtomicPort, Side> builder = ImmutableBiMap.builder();
        for (AtomicPort port : ports) {
            AtomicPort conn = config.getConnectingPort(port);
            // TODO do these all at once, because otherwise its n^2 * ports
            Side side = findPort(grid, conn);
            builder.put(port, side);
        }

        return builder.build();
    }

    public static Location findReplacement(
        CellConfiguration grid,
        AtomicConfiguration config,
        AtomicNode node,
        CellConfiguration nodeGrid,
        CostFunction cost,
        double probIgnore
    ) {
        BiMap<AtomicPort, Side> ports = findPorts(nodeGrid, node);
        BiMap<AtomicPort, Side> conns = findConnectingPorts(grid, config, ports.keySet());
        HashMap<AtomicPort, DijkstrasPather> pathers = new HashMap<>();
        for (Map.Entry<AtomicPort, Side> entry : conns.entrySet()) {
            pathers.put(
                entry.getKey(),
                new DijkstrasPather(
                    grid,
                    entry.getValue(),
                    cost, entry.getKey().getPort().isInput()
                )
            );
        }

        int max = -1;
        Location best = null;
        List<Location> placements = getPlacements(grid, nodeGrid);
        MutableGrid<Integer> dists = new MutableGrid<>(0, grid.getSizeX(), grid.getSizeY());
        HashSet<AtomicPort> ignore = new HashSet<>();
        for (AtomicPort port : ports.keySet()) {
            if (Math.random() < probIgnore) {
                ignore.add(port);
            }
        }

        if (ignore.size() == ports.size()) {
            ignore = new HashSet<>(); // jailbreak :)
        }

        for (Location loc : placements) {
            int sum = 0;
            for (Map.Entry<AtomicPort, Side> entry : ports.entrySet()) {
                AtomicPort port = entry.getKey();
                Side end = entry.getValue();

                if (ignore.contains(port)) {
                    continue;
                }

                sum += pathers.get(port).distanceTo(end.add(loc));
            }

            for (int i = 0; i < nodeGrid.getSizeX(); i++) {
                for (int j = 0; j < nodeGrid.getSizeY(); j++) {
                    sum += cost.getCost(nodeGrid.getCell(i, j), loc.add(i, j));
                }
            }

            dists.put(sum, loc);
            if (max == -1 || sum < max) {
                max = sum;
                best = loc;
            }
        }

        return best;
    }

    public static void putOutputPath(CellConfiguration grid, List<Side> path) {
        putPath(grid, path, false);
    }

    public static void putInputPath(CellConfiguration grid, List<Side> path) {
        putPath(grid, path, true);
    }

    public static void putPath(CellConfiguration grid, List<Side> path, boolean isInput) {
        if (!isInput) {
            putPath(grid, path);
        } else {
            putPath(grid, Lists.reverse(path));
        }
    }

    private static void putPath(CellConfiguration grid, List<Side> path) {
        // skip start
        // TODO make for general lists (not just random access)
        for (int j = 1; j + 1 < path.size(); j += 2) {
            Side input = path.get(j);
            Side output = path.get(j + 1);
            Cell cell = grid.getCell(input.getLocation());
            switch(cell.getCellType()) {
                case EMPTY:
                    if (input.getDirection() == output.getDirection().opposite()) {
                        grid.put(WireCell.getWire(output.getDirection()), input.getLocation());
                    } else {
                        grid.put(new TurnCell(input.getDirection(), output.getDirection()), input.getLocation());
                    }
                    break;
                case WIRE:
                    grid.put(new CrossoverCell(cell.getOutputDirection(0), output.getDirection()), input.getLocation());
                    break;
                default:
                    Preconditions.checkState(false);
                    break;
            }
        }
    }

    /**
     * TODO make return a map from port to live wire - doesnt matter anymore
     * Deletes a node and returns all "live" wires
     */
    public static List<Side> deleteNode(CellConfiguration grid, AtomicNode node) {
        ImmutableList.Builder<Side> builder = ImmutableList.builder();
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                Cell c = grid.getCell(i, j);

                if (c.getCellType() != CellType.NODE && c.getCellType() != CellType.PORT) {
                    continue;
                }

                if (!c.getId().equals(node.getId())) {
                    continue;
                }

                grid.put(EmptyCell.getInstance(), i, j);

                if (c.getCellType() == CellType.PORT) {
                    for (Direction dir : c.getInputDirections()) {
                        builder.add(deleteWire(grid, new Side(i, j, dir), true));
                    }

                    for (Direction dir : c.getOutputDirections()) {
                        builder.add(deleteWire(grid, new Side(i, j, dir), false));
                    }
                }
            }
        }

        return builder.build();
    }

    private static Side deleteWire(CellConfiguration grid, Side start, boolean isInput) {
        Side cur = start.opposite();
        while (true) {
            Cell cell = grid.getCell(cur.getLocation());
            switch (cell.getCellType()) {
                case EMPTY:
                case NODE:
                case PORT:
                    return cur;
                case WIRE:
                    grid.put(EmptyCell.getInstance(), cur.getLocation());
                    cur = cur.add(cur.getDirection().opposite());
                    break;
                case CROSSOVER:
                    Direction cw = cur.getDirection().clockwise();
                    Cell wire = cell.isOutput(cw) ? WireCell.getWire(cw) : WireCell.getWire(cw.opposite());
                    grid.put(wire, cur.getLocation());
                    cur = cur.add(cur.getDirection().opposite());
                    break;
                case TURN:
                    grid.put(EmptyCell.getInstance(), cur.getLocation());
                    Direction dir = isInput ? cell.getInputDirection(0) : cell.getOutputDirection(0);
                    cur = new Side(cur.add(dir).getLocation(), dir.opposite());
                    break;
            }
        }
    }

    public static void deleteSlices(CellConfiguration grid) {
        Direction x = Direction.getDirection(1, 0);
        Direction y = Direction.getDirection(0, 1);
        ArrayList<List<Cell>> cells = new ArrayList<>();
        for (int i = 0; i < grid.getSizeX(); i++) {
            boolean delete = true;
            for (int j = 0; j < grid.getSizeY(); j++) {
                Cell c = grid.getCell(i, j);
                if (c.getCellType() == CellType.EMPTY) {
                    continue;
                }

                // TODO do this once area expander works for touching ports/nodes
//                if (c.getCellType() == CellType.WIRE && c.getOutputDirection(0).parallel(x)) {
//                    continue;
//                }

                delete = false;
                break;
            }

            if (!delete) {
                cells.add(GridUtils.sliceX(grid, i));
            }
        }

        Cell[][] xcell = new Cell[cells.size()][grid.getSizeY()];
        for (int i = 0; i < cells.size(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                xcell[i][j] = cells.get(i).get(j);
            }
        }

        grid.set(xcell, cells.size(), grid.getSizeY());

        cells = new ArrayList<>();
        for (int j = 0; j < grid.getSizeY(); j++) {
            boolean delete = true;
            for (int i = 0; i < grid.getSizeX(); i++) {
                Cell c = grid.getCell(i, j);
                if (c.getCellType() == CellType.EMPTY) {
                    continue;
                }

//                if (c.getCellType() == CellType.WIRE && c.getOutputDirection(0).parallel(y)) {
//                    continue;
//                }

                delete = false;
                break;
            }

            if (!delete) {
                cells.add(GridUtils.sliceY(grid, j));
            }
        }

        Cell[][] ycell = new Cell[grid.getSizeX()][cells.size()];
        for (int j = 0; j < cells.size(); j++) {
            for (int i = 0; i < grid.getSizeX(); i++) {
                ycell[i][j] = cells.get(j).get(i);
            }
        }

        grid.set(ycell, grid.getSizeX(), cells.size());
    }
}
