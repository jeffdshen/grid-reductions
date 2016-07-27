package reduction.module;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import org.apache.log4j.Logger;
import parser.SATParser;
import reduction.ReductionData;
import types.configuration.Configuration;

import java.io.*;
import java.nio.charset.Charset;

import static org.apache.log4j.Level.ERROR;

public class SATParsing implements Module<String, Configuration> {
    private SATParser parser;

    private static final Logger logger = Logger.getLogger(SATParsing.class.getName());

    public SATParsing() {
        parser = new SATParser();
    }

    @Override
    public String name() {
        return "SATParsing";
    }

    @Override
    public void init(ReductionData data) {
        // do nothing
    }

    @Override
    public Configuration process(String s) {
        return parser.parseSAT(s);
    }

    @Override
    public void write(Configuration configuration, OutputStream stream) {
        UnsupportedOperationException e = new UnsupportedOperationException();
        logger.log(ERROR, e.getMessage(), e);
    }

    @Override
    public String parse(File file) {
        try {
            return Files.toString(file, Charset.forName("UTF-8"));
        } catch (IOException e) {
            logger.log(ERROR, "Error while parsing " + file.getAbsolutePath() + ": " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String parse(InputStream stream, String id) {
        try {
            return CharStreams.toString(new InputStreamReader(stream));
        } catch (IOException e) {
            logger.log(ERROR, "Error while parsing " + id + ": " + e.getMessage(), e);
        }

        return null;
    }
}
