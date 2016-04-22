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
     * @param input the sides desired to be the inputs of the resulting gadget
     * @param output the sides desired to be the output of the resulting gadget
     * @param length exact length in the direction of the shifter
     * @param thickness maximum length in the direction orthogonal of the wire on either end of the shifter
     */
    GadgetConfiguration shift(List<Side> input, List<Side> output, int length, int thickness);

    boolean canShift(Direction dir);

    /**
     * The minimum required distance between two adjacent wires on the same side.
     * @param dir the direction of the shift gadget

     */
    int minSeparation(Direction dir);

    /**
     * The minimum thickness for which this shifter can construct all sufficiently large lengths of shifts
     * with this many wires.
     * @param dir the direction of the shift gadget
     */
    int minThickness(Direction dir, int wires);

    /**
     * Given a direction, max thickness and number of wires, finds a min length such that for every
     * length at least the min length, it is possible to construct a shifter with that many wires,
     * that exact length, and that maximum thickness. Returns 0
     */
    int minLength(Direction dir, int wires, int thickness);
}
