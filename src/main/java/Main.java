import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import parser.ConfigurationParser;
import parser.GadgetParser;
import parser.SATParser;
import transform.ConfigurationResolver;
import transform.GadgetPlacer;
import transform.GridPlacer;
import types.Gadget;
import types.configuration.AtomicConfiguration;
import types.configuration.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {
    public void run(
        Iterable<File> gadgetFiles, Iterable<File> configFiles, Iterable<File> wireFiles, File out
    ) throws Exception {
        SATParser s = new SATParser();
        Configuration c = s.parseSAT("((x && y) || x) && x");

        ImmutableMap<String, Gadget> gadgets = getGadgets(gadgetFiles);
        Iterable<Configuration> configs = getConfigs(configFiles);
        List<Gadget> wires = getWires(wireFiles);

        AtomicConfiguration config = new ConfigurationResolver().resolve(c, configs, gadgets.keySet());
        GridPlacer placer = new GridPlacer(config, gadgets);
        placer.place();
        System.out.println(placer.getGrid());
        GadgetPlacer gadgetPlacer = new GadgetPlacer(
            wires,
            gadgets.get("TURN"),
            gadgets.get("CROSSOVER"),
            gadgets.get("EMPTY"),
            ImmutableList.copyOf(gadgets.values())
        );
        System.out.println(getStringArray(gadgetPlacer.place(placer.getGrid())));
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

    public Iterable<Configuration> getConfigs(Iterable<File> configs) throws IOException {
        ConfigurationParser parser = new ConfigurationParser();

        ImmutableList.Builder<Configuration> builder = ImmutableList.builder();
        for (File file : configs) {
            System.out.println(file);
            Configuration config = parser.parseConfiguration(file);
            builder.add(config);
        }

        return builder.build();
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        File gadgetDir = new File(Main.class.getResource("Akari/gadgets").getFile());
        File configDir = new File(Main.class.getResource("Akari/configs").getFile());
        File configDir2 = new File(Main.class.getResource("default/configs").getFile());
        File wiresDir = new File(Main.class.getResource("Akari/wires").getFile());
        main.run(
            Arrays.asList(gadgetDir.listFiles()),
            Iterables.concat(Arrays.asList(configDir.listFiles()), Arrays.asList(configDir2.listFiles())),
            Arrays.asList(wiresDir.listFiles()),
            null
        );
    }

    public String getStringArray(String[][] array){
        StringBuilder str = new StringBuilder();
        for(int j = 0; j < array[0].length; j++){
            for(int i = 0; i < array.length; i++){
                str.append(array[i][j]);
                str.append("");
            }
            str.append("\n");
        }
        return str.toString();
    }
}
