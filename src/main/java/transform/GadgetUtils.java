package transform;

import com.google.common.base.Function;
import types.Direction;
import types.Gadget;
import types.Location;

public class GadgetUtils {
    public static final Function<Gadget, Integer> WIRE_LENGTH = new Function<Gadget, Integer>() {
        @Override
        public Integer apply(Gadget input) {
            return input.getInput(0).getDirection().parallel(Direction.NORTH) ? input.getSizeY() : input.getSizeX();
        }
    };

    public static final Function<Gadget, Integer> WIRE_WIDTH = new Function<Gadget, Integer>() {
        @Override
        public Integer apply(Gadget input) {
            return input.getInput(0).getDirection().parallel(Direction.NORTH) ? input.getSizeX() : input.getSizeY();
        }
    };

    public static final Function<Gadget, Integer> WIRE_THICKNESS = new Function<Gadget, Integer>() {
        @Override
        public Integer apply(Gadget input) {
            Location loc = input.getInput(0).getLocation();

            if (input.getInput(0).getDirection().parallel(Direction.NORTH)) {
                return Math.max(loc.getX(), input.getSizeX() - 1 - loc.getX());
            }

            return Math.max(loc.getY(), input.getSizeY() - 1 - loc.getY());
        }
    };

    public static final Function<Gadget, Direction> WIRE_DIRECTION = new Function<Gadget, Direction>() {
        @Override
        public Direction apply(Gadget input) {
            return input.getOutput(0).getDirection();
        }
    };
}
