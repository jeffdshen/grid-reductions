import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import java.util.List;
import java.util.Map;

public class Main {
    //TODO remove unused code
    public void run(
        String expr, Iterable<File> gadgetFiles, Iterable<Reader> configFiles, Iterable<File> wireFiles, File outFile
    ) throws Exception {
        SATParser s = new SATParser();
        Configuration c = s.parseSAT(expr);
        ImmutableMap<String, Gadget> gadgets = getGadgets(gadgetFiles);
        Iterable<Configuration> configs = getConfigs(configFiles);
        Iterable<Gadget> wires = GadgetUtils.getSymmetries(getWires(wireFiles));

        AtomicConfiguration config = new ConfigurationResolver(configs, gadgets.keySet()).process(c);
        GadgetPlanarizer planarizer = new GadgetPlanarizer(gadgets);
        CellConfiguration grid = planarizer.process(config);

        System.out.println(grid);

        Iterable<Gadget> crossovers = GadgetUtils.getRotations(gadgets.get("CROSSOVER"));
        Iterable<Gadget> turns = GadgetUtils.getSymmetries(gadgets.get("TURN"));
        Gadget empty = gadgets.get("EMPTY");
        LPGadgetPlacer gadgetPlacer = new LPGadgetPlacer(
            wires, turns, crossovers, empty, ImmutableList.copyOf(gadgets.values())
        );
        GadgetConfiguration gadgetConfig = gadgetPlacer.place(grid);
        Grid<String> output = gadgetConfig.toGrid(empty);

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)))) {
            out.print(output);
        }

        ImagePostProcessor ipp = new ImagePostProcessor(getAkariImages(), 10, 10);
        BufferedImage image = ipp.process(output);
        File imageOutput = ResourceUtils.getAbsoluteFile(getClass(), "akari.png");
        PostProcessorUtils.showImage(image);
        ipp.write(image, imageOutput);
    }

    public Map<String, Image> getAkariImages() throws IOException {
        BufferedImage zero = ImageIO.read(ResourceUtils.getAbsoluteFile(getClass(), "Akari/images/zero10.png"));
        BufferedImage one = ImageIO.read(ResourceUtils.getAbsoluteFile(getClass(), "Akari/images/one10.png"));
        BufferedImage two = ImageIO.read(ResourceUtils.getAbsoluteFile(getClass(), "Akari/images/two10.png"));
        BufferedImage blank = ImageIO.read(ResourceUtils.getAbsoluteFile(getClass(), "Akari/images/blank10.png"));
        BufferedImage black = ImageIO.read(ResourceUtils.getAbsoluteFile(getClass(), "Akari/images/black10.png"));
        return ImmutableMap.<String, Image>of("0", zero, "1", one, "2", two, "x", black, ".", blank);
    }

    public List<Gadget> getWires(Iterable<File> wires) throws IOException {
        GadgetParser parser = new GadgetParser();

        ImmutableList.Builder<Gadget> builder = ImmutableList.builder();
        for (File file : wires) {
            System.out.println(file);
            Gadget gadget = parser.parse(file);
            if (gadget != null) {
                builder.add(gadget);
            }
        }

        return builder.build();
    }

    public ImmutableMap<String, Gadget> getGadgets(Iterable<File> gadgets) throws IOException {
        GadgetParser parser = new GadgetParser();

        ImmutableMap.Builder<String, Gadget> builder = ImmutableMap.builder();
        for (File file : gadgets) {
            System.out.println(file);
            Gadget gadget = parser.parse(file);
            builder.put(gadget.getName(), gadget);
        }

        return builder.build();
    }

    public Iterable<Configuration> getConfigs(Iterable<Reader> configs) throws IOException {
        ConfigurationParser parser = new ConfigurationParser();

        ImmutableList.Builder<Configuration> builder = ImmutableList.builder();
        for (Reader reader : configs) {
            Configuration config = parser.parse(reader, null);
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
        ReductionXmlParser parser = new ReductionXmlParser();

        File file = ResourceUtils.getRelativeFile(Main.class, "Akari/akari.xml");
        ReductionXml reductionXml = parser.parse(file);
        ReductionRunner runner = new ReductionRunner(ImmutableList.of(
            new SATParsing(),
            new ConfigurationSubstitution(),
            new GadgetPlanarization(),
            new GadgetAlignment(),
            new GadgetPlacement(),
            new ImagePostProcessing()
        ));

        runner.run(reductionXml, file.getParentFile());

//        Options options = new Options();
//        options.addOption("g", "gadgets", true, "directory for gadgets, defaults to ./gadgets");
//        options.addOption("c", "configs", true, "directory for configs");
//        options.addOption("d", true, "the default directory");
//        options.addOption("w", "wires", true, "directory for wires, defaults to ./wires");
//        options.addOption("o", "out", true, "output file location, defaults to ./out.txt");
//
//        CommandLineParser parser = new GnuParser();
//        CommandLine cmd = parser.parse(options, args);
//
//        File curDir = new File(System.getProperty("user.dir"));
//        File gadgetsDir = cmd.hasOption("g") ? new File(cmd.getOptionValue("g")) : new File(curDir, "gadgets");
//        File configsDir = cmd.hasOption("c") ? new File(cmd.getOptionValue("c")): null;
//        File wiresDir = cmd.hasOption("w") ? new File(cmd.getOptionValue("w")) : new File(curDir, "wires");
//        File outDir = cmd.hasOption("o") ? new File(cmd.getOptionValue("o")) : new File(curDir, "out.txt");
//        File defaultsDir = cmd.hasOption("d") ? new File(cmd.getOptionValue("d")) : null;
////        InputStream defaultConfigDir = Main.class.getResourceAsStream("default/configs");
////        Main.class.getResource("default/configs").getPath();
//        String bool = cmd.getArgs()[0];
//
//        Main main = new Main();
//        Iterable<File> gadgets = Arrays.asList(gadgetsDir.listFiles());
//        Iterable<Reader> configs = ImmutableList.of();
//        if (configsDir != null) {
//            configs = Iterables.concat(configs, toReader(Arrays.asList(configsDir.listFiles())));
//        }
//
//        if (defaultsDir != null) {
//            configs = Iterables.concat(configs, toReader(Arrays.asList(defaultsDir.listFiles())));
//        }
//
//        Iterable<File> wires = Arrays.asList(wiresDir.listFiles());
//
////        if (!cmd.hasOption("d")) {
////            configs = Iterables.concat(configs, Arrays.asList(defaultConfigDir));
////        }
//        main.run(bool, gadgets, configs, wires, outDir);
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

    public String[][] crop(String[][] grid, int x, int y, int toX, int toY) {
        toX = Math.min(toX, grid.length);
        toY = Math.min(toY, grid[0].length);
        String[][] cropped = new String[toX - x][toY - y];
        for (int i = x; i < toX; i++) {
            for (int j = y; j < toY; j++) {
                cropped[i - x][j - y] = grid[i][j];
            }
        }
        return cropped;
    }

    public void visualizeAkari(String[][] grid) throws IOException {
        BufferedImage zero = ImageIO.read(ResourceUtils.getAbsoluteFile(getClass(), "Akari/images/zero10.png"));
        BufferedImage one = ImageIO.read(ResourceUtils.getAbsoluteFile(getClass(), "Akari/images/one10.png"));
        BufferedImage two = ImageIO.read(ResourceUtils.getAbsoluteFile(getClass(), "Akari/images/two10.png"));
        BufferedImage blank = ImageIO.read(ResourceUtils.getAbsoluteFile(getClass(), "Akari/images/blank10.png"));
        BufferedImage black = ImageIO.read(ResourceUtils.getAbsoluteFile(getClass(), "Akari/images/black10.png"));
        Map<String, BufferedImage> l = ImmutableMap.of("0", zero, "1", one, "2", two, "x", black, ".", blank);

        int w = 0;
        int h = 0;
        for(BufferedImage b : l.values()){
            w = Math.max(w, b.getWidth());
            h = Math.max(h, b.getHeight());
        }
        int xmax = grid.length;
        int ymax = grid[0].length;

        int splitsize = 1600;

        for(int xsplit = 0; xsplit < xmax/splitsize +1; xsplit ++){
            for(int ysplit = 0; ysplit<ymax/splitsize +1; ysplit ++){
                //split into multiple chunks
                int xwidth = Math.min(splitsize, xmax-xsplit*splitsize);
                int ywidth = Math.min(splitsize, ymax-ysplit*splitsize);

                BufferedImage stitched = new BufferedImage(w*xwidth, h*ywidth, BufferedImage.TYPE_INT_ARGB);

                Graphics g = stitched.getGraphics();
                for(int i = xsplit*splitsize; i < Math.min((xsplit+1)*splitsize, xmax); i++){
                    for(int j = ysplit*splitsize; j < Math.min((ysplit+1)*splitsize, ymax); j++){
                        if(l.containsKey(grid[i][j])) {
                            g.drawImage(l.get(grid[i][j]), w * (i % splitsize), h * (j%splitsize), null);
                        }
                        else{
                            System.err.println("Visualization error: unknown string in output grid.");
                        }
                    }
                }
                // Save as new image
                String name = "stitched2-"+xsplit+"-"+ysplit+".png";
                System.out.println(name);
                File file = new File(name);
                ImageIO.write(stitched, "PNG", file);
            }
        }


    }


}
