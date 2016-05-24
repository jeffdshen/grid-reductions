package transform.wiring;

import types.Direction;
import types.Side;
import types.configuration.GadgetConfiguration;

import java.util.List;

/**
 * Note: here, thickness refers to the thickness of the shifter, not of the wires.
 */
public interface Shifter {
    /**
     * Produces a gadget configuration representing a shift from the starting ports to the ending ports.
     * @param start the sides desired to be the starting ports of the resulting configuration
     * @param end the sides desired to be the ending ports of the resulting configuration
     * @param isInput whether the sides in start are inputs
     * @param thickness maximum length in the direction orthogonal of the wire on either end of the shifter
     */
    GadgetConfiguration shift(List<Side> start, List<Side> end, List<Boolean> isInput, int thickness);

    /**
     * Whether it is possible to shift in the given direction with the following ports in order.
     * @param dir the direction of the shift gadget
     * @param isInput whether the ports are inputs, on the starting side of the shifter in the clockwise direction
     *                of dir.
     * @return A boolean indicating if it is possible to shift.
     */
    boolean canShift(Direction dir, List<Boolean> isInput);

    /**
     * The minimum number of required cells between (exclusive) two adjacent wires on the same side.
     * @param dir the direction of the shift gadget
     * @param isStart whether the minimum separation is for the start of the shifter or the end.
     * @param isInput whether the ports are inputs, on the corresponding side of the shifter.
     *                For the end of the shifter, these run clockwise, and for the start, these run counterclockwise.
     * @return A list of the minimum separations, the size of this will be one less than the size of isInput.
     */
    List<Integer> minSeparation(Direction dir, boolean isStart, List<Boolean> isInput);

    /**
     * The minimum thickness for which this shifter can construct all sufficiently large lengths of shifts
     * with this many wires.
     * @param dir the direction of the shift gadget
     * @param isInput whether the ports are inputs, on the starting side of the shifter in the clockwise direction
     *                of dir.
     */
    int minThickness(Direction dir, List<Boolean> isInput);

    /**
     * Given a direction, max thickness and number of wires, finds a min length such that for every
     * length at least the min length, it is possible to construct a shifter with that many wires,
     * that exact length, and that maximum thickness. Note that this may return 0.
     * @param dir the direction of the shifter
     * @param isInput whether the ports are inputs, on the starting side of the shifter in the clockwise direction
     *                of dir.
     * @param thickness the max thickness of the shifter
     */
    int minLength(Direction dir, List<Boolean> isInput, int thickness);
}
