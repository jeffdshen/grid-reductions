package transform.planar;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import transform.GadgetUtils;
import transform.Processor;
import transform.wiring.FrobeniusWirer;
import transform.wiring.TurnShifter;
import types.Direction;
import types.Gadget;
import types.Grid;
import types.configuration.AtomicConfiguration;
import types.configuration.CellConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GadgetPlanarizer implements Processor<AtomicConfiguration, CellConfiguration> {
    private GadgetSet gadgets;
    public GadgetPlanarizer(
        Iterable<Gadget> wires,
        Iterable<Gadget> turns,
        Iterable<Gadget> crossovers,
        Gadget empty,
        Iterable<Gadget> gadgets
    ) {
        this.gadgets = new GadgetSet(
            wires,
            turns,
            crossovers,
            empty,
            gadgets
        );
    }

    @Override
    public CellConfiguration process(AtomicConfiguration atomicConfiguration) {
        GadgetPlanarizerInstance instance = new GadgetPlanarizerInstance(atomicConfiguration, gadgets);
        instance.place();
        return instance.getGrid();
    }
}
