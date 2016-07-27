package reduction.module;

import org.apache.log4j.Logger;
import reduction.ReductionData;
import transform.GadgetUtils;
import transform.planar.GadgetPlanarizer;
import types.configuration.AtomicConfiguration;
import types.configuration.CellConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.log4j.Level.ERROR;

public class GadgetPlanarization implements Module<AtomicConfiguration, CellConfiguration> {
    private static final Logger logger = Logger.getLogger(ConfigurationSubstitution.class.getName());
    private GadgetPlanarizer planarizer;

    @Override
    public String name() {
        return "GadgetPlanarization";
    }

    @Override
    public void init(ReductionData data) {
        planarizer = new GadgetPlanarizer(GadgetUtils.getGadgetMap(data.getGadgets()));
    }

    @Override
    public void write(CellConfiguration cellConfiguration, OutputStream stream) {
        UnsupportedOperationException e = new UnsupportedOperationException();
        logger.log(ERROR, e.getMessage(), e);
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
