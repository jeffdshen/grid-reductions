package reduction.module;

import org.apache.log4j.Logger;
import parser.CellConfigurationParser;
import reduction.ReductionData;
import transform.GadgetUtils;
import transform.planar.GadgetPlanarizer;
import types.Gadget;
import types.configuration.AtomicConfiguration;
import types.configuration.CellConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import static org.apache.log4j.Level.ERROR;

public class GraphPlanarization implements Module<AtomicConfiguration, CellConfiguration> {
    private static final Logger logger = Logger.getLogger(ConfigurationSubstitution.class.getName());
    private GadgetPlanarizer planarizer;
    private CellConfigurationParser parser;

    public GraphPlanarization() {
        parser = new CellConfigurationParser();
    }

    @Override
    public String name() {
        return "GraphPlanarization";
    }

    @Override
    public void init(ReductionData data) {
        Map<String, Iterable<Gadget>> typed = data.getTypedGadgets();
        Iterable<Gadget> wires = typed.get("wire");
        Iterable<Gadget> turns = typed.get("turn");
        Iterable<Gadget> crossovers = typed.get("crossover");
        Gadget empty = typed.get("empty").iterator().next();
        Iterable<Gadget> gadgets = data.getGadgets();
        planarizer = new GadgetPlanarizer(wires, turns, crossovers, empty, gadgets);
    }

    @Override
    public void write(CellConfiguration cellConfig, OutputStream stream) {
        parser.write(cellConfig, stream);
    }

    @Override
    public AtomicConfiguration parse(File file) {
        UnsupportedOperationException e = new UnsupportedOperationException();
        logger.log(ERROR, e.getMessage(), e);
        return null;
    }

    @Override
    public AtomicConfiguration parse(InputStream stream, String id) {
        UnsupportedOperationException e = new UnsupportedOperationException();
        logger.log(ERROR, e.getMessage(), e);
        return null;
    }

    @Override
    public CellConfiguration process(AtomicConfiguration atomicConfiguration) {
        return planarizer.process(atomicConfiguration);
    }
}
