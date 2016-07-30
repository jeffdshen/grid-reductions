import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.cli.*;
import parser.ConfigurationParser;
import parser.GadgetParser;
import parser.SATParser;
import postprocessor.ImagePostProcessor;
import postprocessor.PostProcessorUtils;
import reduction.module.*;
import reduction.ReductionRunner;
import reduction.ReductionXmlParser;
import reduction.xml.ReductionXml;
import transform.ConfigurationResolver;
import transform.GadgetUtils;
import transform.LPGadgetPlacer;
import transform.planar.GadgetPlanarizer;
import types.Gadget;
import types.Grid;
import types.configuration.AtomicConfiguration;
import types.configuration.CellConfiguration;
import types.configuration.Configuration;
import types.configuration.GadgetConfiguration;
import utils.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Main {
    public void run(File reductionConfig) throws Exception {
        ReductionXmlParser parser = new ReductionXmlParser();

        ReductionXml reductionXml = parser.parse(reductionConfig);
        ReductionRunner runner = new ReductionRunner(ImmutableList.of(
            new SATParsing(),
            new ConfigurationSubstitution(),
            new GadgetPlanarization(),
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
