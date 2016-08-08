import com.google.common.collect.ImmutableList;
import org.apache.commons.cli.*;
import reduction.module.*;
import reduction.ReductionRunner;
import reduction.ReductionXmlParser;
import reduction.xml.ReductionXml;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public void run(File reductionConfig) throws Exception {
        ReductionXmlParser parser = new ReductionXmlParser();

        ReductionXml reductionXml = parser.parse(reductionConfig);
        ReductionRunner runner = new ReductionRunner(ImmutableList.of(
            new SATParsing(),
            new ConfigurationSubstitution(),
            new GraphPlanarization(),
            new GadgetAlignment(),
            new GadgetPlacement(),
            new ImagePostProcessing()
        ));

        runner.run(reductionXml, reductionConfig.getParentFile());
    }

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        CommandLine cmd = parser.parse(options, args);
        String pathString = cmd.getArgs()[0];
        Path path = Paths.get(pathString).normalize().toAbsolutePath();
        File file = path.toFile();
        new Main().run(file);
    }
}
