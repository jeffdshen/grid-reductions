package reduction.module;

import org.apache.log4j.Logger;
import parser.ConfigurationParser;
import reduction.ReductionData;
import transform.ConfigurationResolver;
import transform.GadgetUtils;
import types.configuration.AtomicConfiguration;
import types.configuration.Configuration;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.log4j.Level.ERROR;

public class ConfigurationSubstitution implements Module<Configuration, AtomicConfiguration> {
    private ConfigurationResolver resolver;
    private ConfigurationParser parser;
    private static final Logger logger = Logger.getLogger(ConfigurationSubstitution.class.getName());

    public ConfigurationSubstitution() {
        parser = new ConfigurationParser();
    }

    @Override
    public String name() {
        return "ConfigurationSubstitution";
    }

    @Override
    public void init(ReductionData data) {
        resolver = new ConfigurationResolver(data.getConfigs(), GadgetUtils.getNames(data.getGadgets()));
    }

    @Override
    public void write(AtomicConfiguration atomicConfiguration, OutputStream stream) {
        UnsupportedOperationException e = new UnsupportedOperationException();
        logger.log(ERROR, e.getMessage(), e);
    }

    @Override
    public Configuration parse(File file) {
        return parser.parse(file);
    }

    @Override
    public Configuration parse(InputStream stream, String id) {
        return parser.parse(stream, id);
    }

    @Override
    public AtomicConfiguration process(Configuration configuration) {
        return resolver.process(configuration);
    }
}
