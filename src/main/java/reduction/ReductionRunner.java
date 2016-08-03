package reduction;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import parser.ConfigurationParser;
import parser.GadgetParser;
import parser.ImageParser;
import parser.Parser;
import reduction.module.Module;
import reduction.xml.*;
import transform.GadgetUtils;
import types.Gadget;
import types.configuration.CellConfiguration;
import types.configuration.Configuration;
import utils.ResourceUtils;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.log4j.Level.ERROR;
import static org.apache.log4j.Level.WARN;

public class ReductionRunner {
    private Map<String, Module<?, ?>> modules;
    private Parser<Gadget> gadgetParser;
    private Parser<Configuration> configParser;
    private Parser<Image> imageParser;
    private static final Logger logger = Logger.getLogger(ReductionRunner.class.getName());

    public ReductionRunner(Iterable<Module<?, ?>> modules) {
        this.modules = Maps.uniqueIndex(modules, new Function<Module, String>() {
            @Override
            public String apply(Module input) {
                return input.name();
            }
        });
        gadgetParser = new GadgetParser();
        configParser = new ConfigurationParser();
        imageParser = new ImageParser();
    }

    private <E> E parse(Parser<E> parser, InputXml input, File cwd) {
        if (input == null) {
            return null;
        }

        InputOutputXmlType type = input.getType();
        String s = input.getInput();

        switch (type) {
            case FILE:
                return parser.parse(new File(cwd, s));
            case STRING:
                try (InputStream stream = new ByteArrayInputStream(s.getBytes(Charset.forName("UTF-8")))) {
                    return parser.parse(stream, input.getType().toString());
                } catch (IOException e) {
                    logger.log(WARN, e.getMessage(), e);
                    return null;
                }
            case DEFAULT_RESOURCE:
                try {
                    String resourceName = "/default/" + s;
                    return parser.parse(ResourceUtils.getInputStream(resourceName), resourceName);
                } catch (ResourceUtils.ResourceNotFoundException e) {
                    logger.log(WARN, e.getMessage(), e);
                    return null;
                }
        }

        return null;
    }

    private ReductionData parseGadgetXmls(List<GadgetXml> xmls, File cwd) {
        if (xmls == null) {
            return new ReductionData(
                ImmutableList.<Gadget>of(), ImmutableMap.<String, Iterable<Gadget>>of(), null, null, 0, 0
            );
        }

        ImmutableList.Builder<Gadget> gadgetBuilder = ImmutableList.builder();
        Map<String, ImmutableList.Builder<Gadget>> typedBuilderMap = new HashMap<>();

        for (GadgetXml xml : xmls) {
            String type = xml.getType();
            Gadget gadget = parse(gadgetParser, xml.getInput(), cwd);
            if (gadget == null) {
                continue;
            }

            List<Gadget> gadgets;
            if (xml.getSymmetries() == null) {
                gadgets = ImmutableList.of(gadget);
            } else {
                switch (xml.getSymmetries()) {
                    case ALL:
                        gadgets = ImmutableList.copyOf(GadgetUtils.getSymmetries(gadget));
                        break;
                    case ROTATION:
                        gadgets = ImmutableList.copyOf(GadgetUtils.getRotations(gadget));
                        break;
                    default:
                        gadgets = ImmutableList.of(gadget);
                        break;
                }
            }

            if (type != null) {
                if (!typedBuilderMap.containsKey(type)) {
                    typedBuilderMap.put(type, ImmutableList.<Gadget>builder());
                }
                typedBuilderMap.get(type).addAll(gadgets);
            } else {
                gadgetBuilder.addAll(gadgets);
            }
        }

        ImmutableMap.Builder<String, Iterable<Gadget>> typedBuilder = ImmutableMap.builder();
        for (Map.Entry<String, ImmutableList.Builder<Gadget>> entry : typedBuilderMap.entrySet()) {
            typedBuilder.put(entry.getKey(), entry.getValue().build());
        }

        return new ReductionData(gadgetBuilder.build(), typedBuilder.build(), null, null, 0, 0);
    }

    private Iterable<Configuration> parseConfigXmls(List<ConfigXml> xmls, File cwd) {
        if (xmls == null) {
            return ImmutableList.of();
        }

        ImmutableList.Builder<Configuration> builder = ImmutableList.builder();

        for (ConfigXml xml : xmls) {
            builder.add(parse(configParser, xml.getInput(), cwd));
        }

        return builder.build();
    }

    private Map<String, Image> parseImageXmls(List<ImageXml> xmls, File cwd) {
        if (xmls == null) {
            return ImmutableMap.of();
        }

        ImmutableMap.Builder<String, Image> builder = ImmutableMap.builder();

        for (ImageXml xml : xmls) {
            builder.put(xml.getKey().getKey(), parse(imageParser, xml.getInput(), cwd));
        }

        return builder.build();
    }

    private ReductionData parseDataXml(DataXml dataXml, File cwd) {
        ReductionData gadgetData = parseGadgetXmls(dataXml.getGadgets(), cwd);
        Iterable<Gadget> gadgets = gadgetData.getGadgets();
        Map<String, Iterable<Gadget>> typedGadgets = gadgetData.getTypedGadgets();
        Iterable<Configuration> configs = parseConfigXmls(dataXml.getConfigs(), cwd);
        ImagesXml imagesXml = dataXml.getImages();
        if (imagesXml == null) {
            return new ReductionData(gadgets, typedGadgets, configs, ImmutableMap.<String, Image>of(), 0, 0);
        }

        Map<String, Image> images = parseImageXmls(imagesXml.getImages(), cwd);
        return new ReductionData(gadgets, typedGadgets, configs, images, imagesXml.getSizeX(), imagesXml.getSizeY());
    }

    @SuppressWarnings("unchecked")
    public void run(ReductionXml xml, File cwd) {
        ReductionData data = parseDataXml(xml.getData(), cwd);

        for (Module m : modules.values()) {
            m.init(data);
        }

        Object input = null;
        for (ModuleXml mx : xml.getModules()) {
            Module m = modules.get(mx.getName());
            if (m == null) {
                logger.log(ERROR, "No module named: " + mx.getName());
                return;
            }

            if (mx.getInput() != null) {
                input = parse(m, mx.getInput(), cwd);
            }

            Object output = m.process(input);
            input = output;

            if (mx.getOutput() != null) {
                OutputXml outputXml = mx.getOutput();

                InputOutputXmlType type = outputXml.getType();
                String s = outputXml.getOutput();
                switch (type) {
                    case FILE:
                        try (FileOutputStream stream = new FileOutputStream(new File(cwd, s))) {
                            m.write(output, stream);
                        } catch (IOException e) {
                            logger.log(WARN, e.getMessage(), e);
                        }
                        break;
                    case STRING:
                        logger.log(WARN, "Can't output to a string");
                        break;
                    case DEFAULT_RESOURCE:
                        logger.log(WARN, "Can't output to default resource");
                        break;
                }
            }
        }
    }
}
