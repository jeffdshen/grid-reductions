package types.configuration;

import types.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A configuration of gadgets that can be converted to a grid of strings
 */
public class GadgetConfiguration {
    private final Map<Location, Integer> occupied;
    private final Map<Integer, Location> offsets;
    private final Map<Integer, Gadget> gadgets;
    private final Set<Side> inputs;
    private final Set<Side> outputs;
    private int maxId;

    public GadgetConfiguration() {
        this.inputs = new HashSet<>();
        this.outputs = new HashSet<>();
        occupied = new HashMap<>();
        offsets = new HashMap<>();
        gadgets = new HashMap<>();
        maxId = 0;
    }

    /**
     * Places and connects a gadget's output port
     * @return the gadget's id
     */
    public int connectOutputPort(int fromGadgetID, int inputPort, Gadget newGadget, int outputPort) {
        Location offset = calcOutputOffset(fromGadgetID, inputPort, newGadget, outputPort);
        return connect(offset, newGadget);
    }

    /**
     * Places and connects a gadget's input port
     * @return the gadget's id
     */
    public int connectInputPort(int fromGadgetID, int outputPort, Gadget newGadget, int inputPort) {
        Location offset = calcInputOffset(fromGadgetID, outputPort, newGadget, inputPort);
        return connect(offset, newGadget);
    }

    /**
     * Places and connects a gadget's output port to the given input side
     * @return the gadget's id
     */
    public int connectOutputPort(Side input, Gadget newGadget, int outputPort) {
        Location offset = calcOutputOffset(input, newGadget, outputPort);
        return connect(offset, newGadget);
    }

    /**
     * Places and connects a gadget's input port to the given output side
     * @return the gadget's id
     */
    public int connectInputPort(Side output, Gadget newGadget, int inputPort) {
        Location offset = calcInputOffset(output, newGadget, inputPort);
        return connect(offset, newGadget);
    }

    /**
     * Adds a gadget to the configuration and connects any ports
     * Does NOT do a safety check - use canConnect
     * @return the assigned id of the new gadget
     */
    public int connect(Location offset, Gadget gadget) {
        int id = getID();
        for (int i = 0; i < gadget.getSizeX(); i++) {
            for (int j = 0; j < gadget.getSizeY(); j++) {
                occupied.put(offset.add(i, j), id);
            }
        }

        for (int i = 0; i < gadget.getInputSize(); i++) {
            Side input = gadget.getInput(i).add(offset);
            Side output = input.opposite();
            if (outputs.contains(output)) {
                outputs.remove(output);
            } else {
                inputs.add(input);
            }
        }

        for (int i = 0; i < gadget.getOutputSize(); i++) {
            Side output = gadget.getOutput(i).add(offset);
            Side input = output.opposite();
            if (inputs.contains(input)) {
                inputs.remove(input);
            } else {
                outputs.add(output);
            }
        }

        gadgets.put(id, gadget);
        offsets.put(id, offset);
        return id;
    }

    public boolean canConnectInputPort(int fromGadgetID, int outputPort, Gadget newGadget, int inputPort) {
        Location offset = calcInputOffset(fromGadgetID, outputPort, newGadget, inputPort);
        return canConnect(offset, newGadget);
    }

    public boolean canConnectOutputPort(int fromGadgetID, int inputPort, Gadget newGadget, int outputPort) {
        Location offset = calcOutputOffset(fromGadgetID, inputPort, newGadget, outputPort);
        return canConnect(offset, newGadget);
    }

    public boolean canConnectInputPort(Side output, Gadget newGadget, int inputPort) {
        Location offset = calcInputOffset(output, newGadget, inputPort);
        return canConnect(offset, newGadget);
    }

    public boolean canConnectOutputPort(Side input, Gadget newGadget, int outputPort) {
        Location offset = calcOutputOffset(input, newGadget, outputPort);
        return canConnect(offset, newGadget);
    }

    public boolean canConnect(Location offset, Gadget gadget) {
        for (int i = 0; i < gadget.getSizeX(); i++) {
            for (int j = 0; j < gadget.getSizeY(); j++) {
                if (occupied.containsKey(offset.add(i, j))) {
                    return false;
                }
            }
        }

        for (int i = 0; i < gadget.getInputSize(); i++) {
            Side input = gadget.getInput(i).add(offset);
            Side output = input.opposite();
            if (!outputs.contains(output) && occupied.containsKey(output.getLocation())) {
                return false;
            }
        }

        for (int i = 0; i < gadget.getOutputSize(); i++) {
            Side output = gadget.getOutput(i).add(offset);
            Side input = output.opposite();
            if (!inputs.contains(input) && occupied.containsKey(input.getLocation())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns this as a grid of strings, does not perform any offsets
     */
    public Grid<String> toGrid(String empty) {
        int maxX = 0;
        int maxY = 0;
        for (Location loc : occupied.keySet()) {
            if (loc.getX() > maxX) {
                maxX = loc.getX();
            }

            if (loc.getY() > maxY) {
                maxY = loc.getY();
            }
        }

        MutableGrid<String> grid = new MutableGrid<>(empty, maxX + 1, maxY + 1);
        for (int id : gadgets.keySet()) {
            Gadget gadget = gadgets.get(id);
            Location offset = offsets.get(id);
            grid.put(gadget, offset);
        }
        return grid;
    }

    private int getID() {
        return maxId++;
    }

    private Location calcInputOffset(Side output, Gadget newGadget, int inputPort) {
        Side input = newGadget.getInput(inputPort);
        return output.opposite().subtract(input.getLocation()).getLocation();
    }

    private Location calcOutputOffset(Side input, Gadget newGadget, int outputPort) {
        Side output = newGadget.getOutput(outputPort);
        return input.opposite().subtract(output.getLocation()).getLocation();
    }


    private Location calcInputOffset(int fromGadgetID, int outputPort, Gadget newGadget, int inputPort) {
        Gadget gadget = gadgets.get(fromGadgetID);
        Location offset = offsets.get(fromGadgetID);
        Side output = gadget.getOutput(outputPort).add(offset);
        return calcInputOffset(output, newGadget, inputPort);
    }

    private Location calcOutputOffset(int fromGadgetID, int inputPort, Gadget newGadget, int outputPort) {
        Gadget gadget = gadgets.get(fromGadgetID);
        Location offset = offsets.get(fromGadgetID);
        Side input = gadget.getInput(inputPort).add(offset);
        return calcOutputOffset(input, newGadget, outputPort);
    }
}

