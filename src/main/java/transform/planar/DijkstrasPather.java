package transform.planar;

import com.google.common.collect.ImmutableList;
import types.Direction;
import types.Location;
import types.Side;
import types.configuration.CellConfiguration;
import types.configuration.cells.*;

import java.util.*;

public class DijkstrasPather {
    private CellConfiguration grid;
    private Side start;
    private Map<Side, Side> prev;
    private Map<Side, Integer> dist;
    private Set<Side> seen;

    private CostFunction cost;
    private boolean isInput;

    public DijkstrasPather(CellConfiguration grid, Side start, CostFunction cost, boolean isInput) {
        this.grid = grid;
        this.start = start;
        this.cost = cost;
        this.isInput = isInput;
        run();
    }

    private void run() {
        Map<Side, Side> prev = new HashMap<>();
        Map<Side, Integer> dist = new HashMap<>();
        PriorityQueue<DijkstrasNode> queue = new PriorityQueue<>();
        Set<Side> seen = new HashSet<>();

        dist.put(start, 0);
        queue.add(new DijkstrasNode(start, 0));

        while (!queue.isEmpty()) {
            final DijkstrasNode u = queue.poll();

            if (seen.contains(u.v)) {
                continue;
            }

            seen.add(u.v);
            Iterable<DijkstrasNode> neighbors = getNeighbors(u);

            for (DijkstrasNode v : neighbors) {
                if (seen.contains(v.v)) {
                    continue;
                }

                int alt = u.dist + v.dist;
                if (!dist.containsKey(v.v) || alt < dist.get(v.v)) {
                    dist.put(v.v, alt);
                    prev.put(v.v, u.v);
                    queue.add(new DijkstrasNode(v.v, alt));
                }
            }
        }

        this.prev = Collections.unmodifiableMap(prev);
        this.dist = Collections.unmodifiableMap(dist);
        this.seen = Collections.unmodifiableSet(seen);
    }

    public boolean isPathable(Side s) {
        return seen.contains(s);
    }

    public int distanceTo(Side end) {
        if (!seen.contains(end.opposite())) {
            throw new IllegalArgumentException(String.format("Illegal input, no path from %s to %s", start, end));
        }

        return dist.get(end.opposite());
    }

    public List<Side> getPath(Side end) {
        if (!seen.contains(end.opposite())) {
            throw new IllegalArgumentException(String.format("Illegal input, no path from %s to %s", start, end));
        }

        ImmutableList.Builder<Side> builder = ImmutableList.builder();
        builder.add(end);
        Side cur = end.opposite();
        while (!cur.equals(start)) {
            builder.add(cur);
            cur = prev.get(cur);
            builder.add(cur.opposite());
        }

        builder.add(cur);
        return builder.build().reverse();
    }

    private Iterable<DijkstrasNode> getNeighbors(DijkstrasNode u) {
        Side v = u.v.opposite();
        if (!grid.isValid(v.getLocation())) {
            return ImmutableList.of();
        }
        Location loc = v.getLocation();
        Direction d = v.getDirection();
        Cell c = grid.getCell(loc);

        ImmutableList.Builder<DijkstrasNode> builder = ImmutableList.builder();

        if (c.getCellType() == CellType.WIRE) {
            Cell next = new CrossoverCell(c.getOutputDirection(0), isInput ? d.opposite() : d);
            builder.add(new DijkstrasNode(v.clockwise().clockwise(), cost.getCost(next, loc)));
        } else if (c.getCellType() == CellType.EMPTY) {
            Cell wire = WireCell.getWire(isInput ? d.opposite() : d);
            builder.add(new DijkstrasNode(v.clockwise().clockwise(), cost.getCost(wire, loc)));

            Cell turnCW = isInput ? new TurnCell(d.clockwise(), d) : new TurnCell(d, d.clockwise());
            builder.add(new DijkstrasNode(v.clockwise(), cost.getCost(turnCW, loc)));

            Cell turnACW = isInput ? new TurnCell(d.anticlockwise(), d) : new TurnCell(d, d.anticlockwise());
            builder.add(new DijkstrasNode(v.anticlockwise(), cost.getCost(turnACW, loc)));
        }

        return builder.build();
    }
}
