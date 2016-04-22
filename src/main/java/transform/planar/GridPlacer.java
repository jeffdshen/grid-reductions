package transform.planar;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import transform.GadgetConverter;
import types.Direction;
import types.Gadget;
import types.Location;
import types.configuration.AtomicConfiguration;
import types.configuration.GridConfiguration;
import types.configuration.cells.*;
import types.configuration.nodes.AtomicNode;
import types.configuration.nodes.AtomicPort;
import types.configuration.nodes.Port;

import javax.annotation.Nonnull;
import java.util.*;

public class GridPlacer {
    private static final int INITIAL_SIZE = 10;

    private GridConfiguration grid;
    private AtomicConfiguration config;
    private final ImmutableMap<String, Gadget> gadgets;
    private final GridExpander expander;
    private final GadgetConverter converter;

    public GridPlacer(AtomicConfiguration config, Map<String, Gadget> gadgets) {
        this.config = config;
        this.gadgets = ImmutableMap.copyOf(gadgets);
        this.grid = new GridConfiguration(INITIAL_SIZE, INITIAL_SIZE);
        this.expander = new GridExpander();
        this.converter = new GadgetConverter();
    }

    public GridConfiguration getGrid() {
        return this.grid;
    }

    public void place() {
        Iterable<AtomicNode> nodes = config.getNodes();
        // assume nodes are in topological order
        for (AtomicNode node : nodes) {
            GridConfiguration nodeGrid = converter.toGridConfiguration(gadgets.get(node.getName()), node.getId());
            Location loc = getPlace(nodeGrid);
            if (loc == null) {
                grid.resize(grid.getSizeX() + nodeGrid.getSizeX() + 2, grid.getSizeY() + nodeGrid.getSizeY() + 2);
                loc = getPlace(nodeGrid);
            }
            grid.put(nodeGrid, loc);
            connect(node);
        }

        expander.expandLast(grid);
    }

    private void connect(AtomicNode node) {
        for (int i = 0; i < node.inputSize(); i++) {
            AtomicPort port = node.getInputPort(i);
            SearchVertex end = find(port);
            SearchVertex start = find(config.getPort(port));

            if (start == null || end == null) {
                port = config.getPort(port);
                String nodeName = config.getNode(port.getContext(), port.getPort().getId()).getName();

                throw new IllegalArgumentException(
                    String.format("Configuration with node %s is not topologically sorted", nodeName)
                );
            }

            List<SearchVertex> path = getPath(start, end);
            // skip start
            for (int j = 1; j + 1 < path.size(); j += 2) {
                SearchVertex input = path.get(j);
                SearchVertex output = path.get(j + 1);
                assert(input.loc.equals(output.loc));
                Cell cell = grid.getCell(input.loc);
                switch(cell.getCellType()) {
                    case EMPTY:
                        if (input.d == output.d.opposite()) {
                            grid.put(WireCell.getWire(output.d), input.loc);
                        } else {
                            grid.put(new TurnCell(input.d, output.d), input.loc);
                        }
                        break;
                    case WIRE:
                        grid.put(new CrossoverCell(cell.getOutputDirection(0), output.d), input.loc);
                        break;
                    default:
                        assert false;
                        break;
                }
            }

            expander.expand(grid);
        }
    }

    private SearchVertex find(AtomicPort port) {
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                Cell c = grid.getCell(i, j);
                if (c.getCellType() == CellType.PORT && c.getId().equals(port.getId())) {
                    for (Direction d : Direction.values()) {
                        Port p = port.getPort();
                        if (((p.isInput() && c.isInput(d)) || (!p.isInput() && c.isOutput(d)))
                                && p.getPortNumber() == c.getPortNumber(d)) {
                            return new SearchVertex(new Location(i, j), d);
                        }
                    }
                }
            }
        }
        return null;
    }

    private Location getPlace(GridConfiguration node) {
        // leave space for wires
        int x = node.getSizeX() + 2;
        int y = node.getSizeY() + 2;
        int[][] sizeX = new int[grid.getSizeX()][grid.getSizeY()];
        int[][] sizeY = new int[grid.getSizeX()][grid.getSizeY()];

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

        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                if (sizeX[i][j] >= x && sizeY[i][j] >= y) {
                    return new Location(i + 1, j + 1);
                }
            }
        }
        return null;
    }

    private List<SearchVertex> getPath(SearchVertex start, SearchVertex end) {
        Map<SearchVertex, SearchVertex> prev = new HashMap<>();
        final Map<SearchVertex, Integer> dist = new HashMap<>();
        PriorityQueue<VertexDistance> queue = new PriorityQueue<>();
        Set<SearchVertex> seen = new HashSet<>();

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

        ImmutableList.Builder<SearchVertex> builder = ImmutableList.builder();
        SearchVertex cur = end;
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
        Cell c = grid.getCell(u.v.loc);
        Iterable<VertexDistance> neighbors = ImmutableList.of(
            new VertexDistance(new SearchVertex(u.v.loc.add(u.v.d), u.v.d.opposite()), 0));

        if (c.getCellType() == CellType.WIRE || c.getCellType() == CellType.EMPTY) {
            Iterable<VertexDistance> cellNeighbors = Iterables.transform(Arrays.asList(Direction.values()),
                new Function<Direction, VertexDistance>() {
                    @Override
                    public VertexDistance apply(Direction dir) {
                        Direction opp = u.v.d.opposite();
                        int x = opp.getX() - dir.getX();
                        int y = opp.getY() - dir.getY();
                        int dist = x * x + y * y + 1;
                        return new VertexDistance(new SearchVertex(u.v.loc, dir), dist);
                    }
                }
            );
            neighbors = Iterables.concat(neighbors, Iterables.filter(cellNeighbors,
                new Predicate<VertexDistance>() {
                    @Override
                    public boolean apply(VertexDistance x) {
                        Cell c = grid.getCell(x.v.loc);
                        return !c.isInput(x.v.d) && !c.isOutput(x.v.d);
                    }

                }
            ));
        }

        neighbors = Iterables.filter(neighbors,
            new Predicate<VertexDistance>() {
                @Override
                public boolean apply(VertexDistance x) {
                    return grid.isValid(x.v.loc);
                }
            }
        );
        return neighbors;
    }

    private static class VertexDistance implements Comparable<VertexDistance> {
        public final SearchVertex v;
        public final int dist;

        public VertexDistance(SearchVertex v, int dist) {
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

    private static class SearchVertex {
        public final Location loc;
        public final Direction d;

        private SearchVertex(Location loc, Direction d) {
            this.loc = loc;
            this.d = d;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + loc.hashCode();
            hash = 71 * hash + d.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SearchVertex) {
                SearchVertex that = (SearchVertex) o;
                return (this.loc.equals(that.loc)) && (this.d == that.d);
            }
            return super.equals(o);
        }

        @Override
        public String toString() {
            return getClass().getName() + "[loc=" + loc + ",d=" + d + "]";
        }
    }
}
