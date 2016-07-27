package reduction;

import types.Gadget;
import types.configuration.Configuration;

import java.awt.*;
import java.util.Map;

public class ReductionData {
    // TODO make GadgetData, ImageData classes
    private Iterable<Gadget> gadgets;
    private Map<String, Iterable<Gadget>> typedGadgets;
    private Iterable<Configuration> configs;
    private Map<String, Image> images;
    private int imageSizeX;
    private int imageSizeY;

    public ReductionData(
        Iterable<Gadget> gadgets,
        Map<String, Iterable<Gadget>> typedGadgets,
        Iterable<Configuration> configs,
        Map<String, Image> images,
        int imageSizeX,
        int imageSizeY
    ) {
        this.gadgets = gadgets;
        this.typedGadgets = typedGadgets;
        this.configs = configs;
        this.images = images;
        this.imageSizeX = imageSizeX;
        this.imageSizeY = imageSizeY;
    }

    public Iterable<Gadget> getGadgets() {
        return gadgets;
    }

    public Map<String, Iterable<Gadget>> getTypedGadgets() {
        return typedGadgets;
    }

    public Iterable<Configuration> getConfigs() {
        return configs;
    }

    public Map<String, Image> getImages() {
        return images;
    }

    public int getImageSizeX() {
        return imageSizeX;
    }

    public int getImageSizeY() {
        return imageSizeY;
    }
}
