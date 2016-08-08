package reduction.module;

import org.apache.log4j.Logger;
import parser.StringGridParser;
import reduction.ReductionData;
import types.Gadget;
import types.Grid;
import types.configuration.GadgetConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import static org.apache.log4j.Level.ERROR;

public class GadgetPlacement implements Module<GadgetConfiguration, Grid<String>> {
    private static final Logger logger = Logger.getLogger(GadgetPlacement.class.getName());

    private Gadget empty;
    private StringGridParser parser = new StringGridParser();

    @Override
    public String name() {
        return "GadgetPlacement";
    }

    @Override
    public void init(ReductionData data) {
        Map<String, Iterable<Gadget>> typed = data.getTypedGadgets();
        empty = typed.get("empty").iterator().next();
    }

    @Override
    public void write(Grid<String> stringGrid, OutputStream stream) {
        parser.write(stringGrid, stream);
    }

    @Override
    public GadgetConfiguration parse(File file) {
        UnsupportedOperationException e = new UnsupportedOperationException();
        logger.log(ERROR, e.getMessage(), e);
        return null;
    }

    @Override
    public GadgetConfiguration parse(InputStream stream, String id) {
        UnsupportedOperationException e = new UnsupportedOperationException();
        logger.log(ERROR, e.getMessage(), e);
        return null;
    }

    @Override
    public Grid<String> process(GadgetConfiguration gadgetConfiguration) {
        return gadgetConfiguration.toGrid(empty);
    }
}
