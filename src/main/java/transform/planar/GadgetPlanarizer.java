package transform.planar;

import transform.Processor;
import types.Gadget;
import types.configuration.AtomicConfiguration;
import types.configuration.CellConfiguration;

import java.util.Map;

public class GadgetPlanarizer implements Processor<AtomicConfiguration, CellConfiguration> {
    private Map<String, Gadget> gadgets;

    public GadgetPlanarizer(Map<String, Gadget> gadgets) {
        this.gadgets = gadgets;
    }

    @Override
    public CellConfiguration process(AtomicConfiguration atomicConfiguration) {
        GadgetPlanarizerInstance instance = new GadgetPlanarizerInstance(atomicConfiguration, gadgets);
        instance.place();
        return instance.getGrid();
    }
}
