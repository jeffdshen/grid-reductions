package reduction.module;

import org.apache.log4j.Logger;
import parser.StringGridParser;
import postprocessor.ImagePostProcessor;
import reduction.ReductionData;
import types.Grid;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.log4j.Level.ERROR;

public class ImagePostProcessing implements Module<Grid<String>, BufferedImage> {
    private static final Logger logger = Logger.getLogger(ImagePostProcessing.class.getName());

    private ImagePostProcessor postprocessor;
    private StringGridParser parser = new StringGridParser();

    @Override
    public String name() {
        return "ImagePostProcessing";
    }

    @Override
    public void init(ReductionData data) {
        this.postprocessor = new ImagePostProcessor(data.getImages(), data.getImageSizeX(), data.getImageSizeY());
    }

    @Override
    public void write(BufferedImage image, OutputStream stream) {
        postprocessor.write(image, stream);
    }

    @Override
    public Grid<String> parse(File file) {
        return parser.parse(file);
    }

    @Override
    public Grid<String> parse(InputStream stream, String id) {
        return parser.parse(stream, id);
    }

    @Override
    public BufferedImage process(Grid<String> stringGrid) {
        return postprocessor.process(stringGrid);
    }
}
