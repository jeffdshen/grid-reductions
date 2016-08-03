package reduction.module;

import org.apache.log4j.Logger;
import parser.CellConfigurationParser;
import reduction.ReductionData;
import transform.LPGadgetPlacer;
import types.Gadget;
import types.configuration.CellConfiguration;
import types.configuration.GadgetConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import static org.apache.log4j.Level.ERROR;

public class GadgetAlignment implements Module<CellConfiguration, GadgetConfiguration> {
    private static final Logger logger = Logger.getLogger(GadgetAlignment.class.getName());

    private LPGadgetPlacer placer;
    private CellConfigurationParser parser = new CellConfigurationParser();

    @Override
    public String name() {
        return "GadgetAlignment";
    }

    @Override
    public void init(ReductionData data) {
        Map<String, Iterable<Gadget>> typed = data.getTypedGadgets();
        Iterable<Gadget> wires = typed.get("wire");
        Iterable<Gadget> turns = typed.get("turn");
        Iterable<Gadget> crossovers = typed.get("crossover");
        Gadget empty = typed.get("empty").iterator().next();
        Iterable<Gadget> gadgets = data.getGadgets();

        placer = new LPGadgetPlacer(wires, turns, crossovers, empty, gadgets);
    }

    @Override
    public void write(GadgetConfiguration gadgetConfiguration, OutputStream stream) {
        UnsupportedOperationException e = new UnsupportedOperationException();
        logger.log(ERROR, e.getMessage(), e);
    }

    @Override
    public CellConfiguration parse(File file) {
        return parser.parse(file);
    }

    @Override
    public CellConfiguration parse(InputStream stream, String id) {
        return parser.parse(stream, id);
    }

    @Override
    public GadgetConfiguration process(CellConfiguration cellConfiguration) {
        try {
            return placer.place(cellConfiguration);
        } catch (Exception e) {
            logger.log(ERROR, e.getMessage(), e);
            return null;
        }
    }
}
