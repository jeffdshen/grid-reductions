package transform.planar;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import transform.GadgetConverter;
import types.Direction;
import types.Gadget;
import types.Location;
import types.Side;
import types.configuration.AtomicConfiguration;
import types.configuration.CellConfiguration;
import types.configuration.cells.*;
import types.configuration.nodes.AtomicNode;
import types.configuration.nodes.AtomicPort;
import types.configuration.nodes.Port;

import javax.annotation.Nonnull;
import java.util.*;

public class GridPlacer {
    private static final int INITIAL_SIZE = 10;

    private CellConfiguration grid;
    private AtomicConfiguration config;
    private final ImmutableMap<String, Gadget> gadgets;
    private final GridExpander expander;
    private final GadgetConverter converter;

    // weights for getting a path.
    private final int crossover = 20;
    private final int turn = 5;
    private final int wire = 3;

    public GridPlacer(AtomicConfiguration config, Map<String, Gadget> gadgets) {
        this.config = config;
        this.gadgets = ImmutableMap.copyOf(gadgets);
        this.grid = new CellConfiguration(INITIAL_SIZE, INITIAL_SIZE);
        this.expander = new GridExpander();
        this.converter = new GadgetConverter();
    }

    public CellConfiguration getGrid() {
        return this.grid;
    }

    public void place() {
        Iterable<AtomicNode> nodes = config.getNodes();
        // assume nodes are in topological order
        for (AtomicNode node : nodes) {
            CellConfiguration nodeGrid = converter.toGridConfiguration(gadgets.get(node.getName()), node.getId());
            List<Location> locs = getPlaces(nodeGrid);
            if (locs.isEmpty()) {
                grid.resize(grid.getSizeX() + nodeGrid.getSizeX() + 2, grid.getSizeY() + nodeGrid.getSizeY() + 2);
                locs = getPlaces(nodeGrid);
            }

            Location best = locs.get(0);
            grid.put(nodeGrid, best);
            connect(node);
        }

        expander.expandLast(grid);
    }

    private void connect(AtomicNode node) {
        for (int i = 0; i < node.inputSize(); i++) {
            AtomicPort port = node.getInputPort(i);
            Side end = find(port);
            Side start = find(config.getConnectingPort(port));

            if (start == null || end == null) {
                port = config.getConnectingPort(port);
                String nodeName = config.getNode(port.getContext(), port.getPort().getId()).getName();

                throw new IllegalArgumentException(
                    String.format("Configuration with node %s is not topologically sorted", nodeName)
                );
            }

            List<Side> path = getPath(start, end);
            // skip start
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

            expander.expand(grid);
        }
    }

    private Side find(AtomicPort port) {
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
                        return new Side(new Location(i, j), d);
                    }
                }
            }
        }
        return null;
    }

    private List<Location> getPlaces(CellConfiguration node) {
        // leave space for wires
        int x = node.getSizeX() + 2;
        int y = node.getSizeY() + 2;
        int[][] sizeX = new int[grid.getSizeX()][grid.getSizeY()];
        int[][] sizeY = new int[grid.getSizeX()][grid.getSizeY()];

        // use dp to calculate what size rectangle fits at each location
        for (int i = grid.getSizeX() - 1; i >= 0; i--) {
            for (int j = grid.getSizeY() - 1; j >= 0; j--) {
                if (grid.getCell(i, j).getCellType() != CellType.EMPTY) {
                    sizeX[i][j] = 0;
                    sizeY[i][j] = 0;
                } else {
                    if (grid.isValid(i + 1, j + 1)) {
                        sizeX[i][j] = Math.min(sizeX[i + 1][j] + 1, sizeX[i][j + 1]);
                        sizeY[i][j] = Math.min(sizeY[i + 1][j], sizeY[i][j + 1] + 1);
                    } else if (grid.isValid(i + 1, j)) {
                        sizeX[i][j] = sizeX[i + 1][j] + 1;
                        sizeY[i][j] = sizeY[i + 1][j];
                    } else if (grid.isValid(i, j + 1)) {
                        sizeX[i][j] = sizeX[i][j + 1];
                        sizeY[i][j] = sizeY[i][j + 1] + 1;
                    } else {
                        sizeX[i][j] = 1;
                        sizeY[i][j] = 1;
                    }
                }
            }
        }

        ArrayList<Location> locs = new ArrayList<>();
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                if (sizeX[i][j] >= x && sizeY[i][j] >= y) {
                    locs.add(new Location(i + 1, j + 1));
                }
            }
        }
        return Collections.unmodifiableList(locs);
    }

    private List<Side> getPath(Side start, Side end) {
        Map<Side, Side> prev = new HashMap<>();
        final Map<Side, Integer> dist = new HashMap<>();
        PriorityQueue<VertexDistance> queue = new PriorityQueue<>();
        Set<Side> seen = new HashSet<>();

        dist.put(start, 0);
        queue.add(new VertexDistance(start, 0));

        while (!queue.isEmpty()) {
            final VertexDistance u = queue.poll();

            if (seen.contains(u.v)) {
                continue;
            }

            seen.add(u.v);
            Iterable<VertexDistance> neighbors = getNeighbors(u);

            for (VertexDistance v : neighbors) {
                if (seen.contains(v.v)) {
                    continue;
                }

                int alt = u.dist + v.dist;
                if (!dist.containsKey(v.v) || alt < dist.get(v.v)) {
                    dist.put(v.v, alt);
                    prev.put(v.v, u.v);
                    queue.add(new VertexDistance(v.v, alt));
                }
            }
        }

        ImmutableList.Builder<Side> builder = ImmutableList.builder();
        Side cur = end;
        while (cur != null && !cur.equals(start)) {
            builder.add(cur);
            cur = prev.get(cur);
        }
        if (cur == null) {
//            System.out.println(grid);
            throw new IllegalStateException(String.format("Unexpected error, no path from %s to %s", start, end));
        }
        builder.add(cur);
        return builder.build().reverse();
    }

    private Iterable<VertexDistance> getNeighbors(final VertexDistance u) {
        Cell c = grid.getCell(u.v.getLocation());

        ImmutableList.Builder<VertexDistance> builder = ImmutableList.builder();
        if (grid.isValid(u.v.opposite().getLocation())) {
            builder.add(new VertexDistance(u.v.opposite(), 0));
        }

        if (c.getCellType() == CellType.WIRE) {
            builder.add(new VertexDistance(u.v.clockwise().clockwise(), crossover));
        } else if (c.getCellType() == CellType.EMPTY) {
            builder.add(new VertexDistance(u.v.clockwise().clockwise(), wire));
            builder.add(new VertexDistance(u.v.clockwise(), turn));
            builder.add(new VertexDistance(u.v.anticlockwise(), turn));
        }

        return builder.build();
    }

    private static class VertexDistance implements Comparable<VertexDistance> {
        public final Side v;
        public final int dist;

        public VertexDistance(Side v, int dist) {
            this.v = v;
            this.dist = dist;
        }

        @Override
        public int compareTo(@Nonnull VertexDistance o) {
            return this.dist - o.dist;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + v.hashCode();
            hash = 71 * hash + dist;
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof VertexDistance) {
                VertexDistance that = (VertexDistance) o;
                return (v == that.v) && (dist == that.dist);
            }
            return super.equals(o);
        }

        @Override
        public String toString() {
            return getClass().getName() + "[v=" + v + ",dist=" + dist + "]";
        }
    }
}
