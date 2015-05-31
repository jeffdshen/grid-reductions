package transform.wiring;

import types.Direction;
import types.Side;
import types.configuration.GadgetConfiguration;


/**
 * thickness refers to maximum length either left or right of the input port.
 */
public interface Wirer {
    /**
     * @param length exact length in the direction of the shifter
     * @param thickness maximum length in the direction orthogonal of the wire on either side of the wire.
     * @return a wiring that satisfies the constraints
     */
    public GadgetConfiguration wire(Side input, int length, int thickness);

    /**
     * @return whether this wirer can supply wires in this direction
     */
    public boolean canWire(Direction dir);

    /**
     * The minimum thickness for which it is possible to construct all sufficiently large wire lengths.
     * @param dir the direction of the wire
     */
    public int minThickness(Direction dir);

    /**
     * Given a direction, max thickness and number of wires, finds a min length such that for every
     * length above the min length, it is possible to construct a shifter with that many wires, that exact length,
     * and that maximum thickness.
     * @throws IllegalArgumentException if not possible
     * @param thickness maximum length in the direction orthogonal of the wire
     * @return the minimum length
     */
    public int minLength(Direction dir, int thickness);
}
