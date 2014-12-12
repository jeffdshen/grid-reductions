import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import parser.ConfigurationParser;
import parser.GadgetParser;
import parser.SATParser;
import transform.ConfigurationResolver;
import transform.GadgetPlacer;
import transform.GridPlacer;
import types.Gadget;
import types.configuration.AtomicConfiguration;
import types.configuration.Configuration;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Main {
    public void run(
        String expr, Iterable<File> gadgetFiles, Iterable<Reader> configFiles, Iterable<File> wireFiles, File outFile
    ) throws Exception {
        SATParser s = new SATParser();
        Configuration c = s.parseSAT(expr);

        ImmutableMap<String, Gadget> gadgets = getGadgets(gadgetFiles);
        Iterable<Configuration> configs = getConfigs(configFiles);
        List<Gadget> wires = getWires(wireFiles);

        AtomicConfiguration config = new ConfigurationResolver().resolve(c, configs, gadgets.keySet());
        GridPlacer placer = new GridPlacer(config, gadgets);
        placer.place();
        GadgetPlacer gadgetPlacer = new GadgetPlacer(
            wires,
            gadgets.get("TURN"),
            gadgets.get("CROSSOVER"),
            gadgets.get("EMPTY"),
            ImmutableList.copyOf(gadgets.values())
        );

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)))) {
            printStringArray(out, gadgetPlacer.place(placer.getGrid()));
        }
    }

    public List<Gadget> getWires(Iterable<File> wires) throws IOException {
        GadgetParser parser = new GadgetParser();

        ImmutableList.Builder<Gadget> builder = ImmutableList.builder();
        for (File file : wires) {
            System.out.println(file);
            Gadget gadget = parser.parseGadget(file);
            builder.add(gadget);
        }

        return builder.build();
    }

    public ImmutableMap<String, Gadget> getGadgets(Iterable<File> gadgets) throws IOException {
        GadgetParser parser = new GadgetParser();

        ImmutableMap.Builder<String, Gadget> builder = ImmutableMap.builder();
        for (File file : gadgets) {
            System.out.println(file);
            Gadget gadget = parser.parseGadget(file);
            builder.put(gadget.getName(), gadget);
        }

        return builder.build();
    }

    public Iterable<Configuration> getConfigs(Iterable<Reader> configs) throws IOException {
        ConfigurationParser parser = new ConfigurationParser();

        ImmutableList.Builder<Configuration> builder = ImmutableList.builder();
        for (Reader reader : configs) {
            Configuration config = parser.parseConfiguration(reader);
            builder.add(config);
        }

        return builder.build();
    }

    public static Iterable<Reader> toReader(Iterable<File> files) throws IOException {
        ImmutableList.Builder<Reader> builder = ImmutableList.builder();
        for (File file : files) {
            builder.add(new FileReader(file));
        }
        return builder.build();
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("g", "gadgets", true, "directory for gadgets, defaults to ./gadgets");
        options.addOption("c", "configs", true, "directory for configs");
        options.addOption("d", true, "the default directory");
        options.addOption("w", "wires", true, "directory for wires, defaults to ./wires");
        options.addOption("o", "out", true, "output file location, defaults to ./out.txt");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);

        File curDir = new File(System.getProperty("user.dir"));
        File gadgetsDir = cmd.hasOption("g") ? new File(cmd.getOptionValue("g")) : new File(curDir, "gadgets");
        File configsDir = cmd.hasOption("c") ? new File(cmd.getOptionValue("c")): null;
        File wiresDir = cmd.hasOption("w") ? new File(cmd.getOptionValue("w")) : new File(curDir, "wires");
        File outDir = cmd.hasOption("o") ? new File(cmd.getOptionValue("o")) : new File(curDir, "out.txt");
        File defaultsDir = cmd.hasOption("d") ? new File(cmd.getOptionValue("d")) : null;
//        InputStream defaultConfigDir = Main.class.getResourceAsStream("default/configs");
//        Main.class.getResource("default/configs").getPath();
        String bool = cmd.getArgs()[0];

        Main main = new Main();
        Iterable<File> gadgets = Arrays.asList(gadgetsDir.listFiles());
        Iterable<Reader> configs = ImmutableList.of();
        if (configsDir != null) {
            configs = Iterables.concat(configs, toReader(Arrays.asList(configsDir.listFiles())));
        }

        if (defaultsDir != null) {
            configs = Iterables.concat(configs, toReader(Arrays.asList(defaultsDir.listFiles())));
        }

        Iterable<File> wires = Arrays.asList(wiresDir.listFiles());

//        if (!cmd.hasOption("d")) {
//            configs = Iterables.concat(configs, Arrays.asList(defaultConfigDir));
//        }
        main.run(bool, gadgets, configs, wires, outDir);
    }

    public void printStringArray(PrintWriter out, String[][] array){
        for(int j = 0; j < array[0].length; j++) {
            out.print(array[0][j]);
            for(int i = 1; i < array.length; i++) {
                out.print(" " + array[i][j]);
            }
            out.println();
        }
    }
}
