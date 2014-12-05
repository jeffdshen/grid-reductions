package types.configuration.cells;


import com.google.common.collect.ImmutableBiMap;
import types.Direction;
import types.configuration.nodes.Port;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractCell implements Cell {
    private final ImmutableBiMap<Integer, Direction> inputs;
    private final ImmutableBiMap<Integer, Direction> outputs;

    public AbstractCell(List<Direction> inputs, List<Direction> outputs) {
        this.inputs = getBiMap(inputs);
        this.outputs = getBiMap(outputs);
    }

    public AbstractCell(Map<Direction, Port> ports) {
        ArrayList<Direction> inputs = new ArrayList<>();
        ArrayList<Direction> outputs = new ArrayList<>();
        for (Map.Entry<Direction, Port> entry : ports.entrySet()) {
            if (entry.getValue().isInput()) {
                inputs.add(entry.getKey());
            } else {
                outputs.add(entry.getKey());
            }
        }

        this.inputs = getBiMap(inputs);
        this.outputs = getBiMap(outputs);
    }


    private static ImmutableBiMap<Integer, Direction> getBiMap(List<Direction> dirs) {
        ImmutableBiMap.Builder<Integer, Direction> builder = ImmutableBiMap.builder();
        int index = 0;
        for (Direction dir : dirs) {
            builder.put(index, dir);
            index++;
        }
        return builder.build();
    }

    @Override
    public int inputSize() {
        return inputs.size();
    }

    @Override
    public int outputSize() {
        return outputs.size();
    }

    @Override
    public Iterable<Direction> getInputDirections() {
        return inputs.values();
    }

    @Override
    public Iterable<Direction> getOutputDirections() {
        return outputs.values();
    }

    @Override
    public Direction getInputDirection(int i) {
        if (!inputs.containsKey(i)) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        return inputs.get(i);
    }

    @Override
    public Direction getOutputDirection(int i) {
        if (!outputs.containsKey(i)) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        return outputs.get(i);
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOutput(Direction d) {
        return outputs.containsValue(d);
    }

    @Override
    public boolean isInput(Direction d) {
        return inputs.containsValue(d);
    }

    @Override
    public int getPortNumber(Direction d) {
        throw new UnsupportedOperationException();
    }
}
