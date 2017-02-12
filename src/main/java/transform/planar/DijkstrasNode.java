package transform.planar;


import types.Side;

import javax.annotation.Nonnull;

public class DijkstrasNode implements Comparable<DijkstrasNode> {
    public final Side v;
    public final int dist;

    public DijkstrasNode(Side v, int dist) {
        this.v = v;
        this.dist = dist;
    }

    @Override
    public int compareTo(@Nonnull DijkstrasNode o) {
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
        if (o instanceof DijkstrasNode) {
            DijkstrasNode that = (DijkstrasNode) o;
            return (v == that.v) && (dist == that.dist);
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[v=" + v + ",dist=" + dist + "]";
    }
}