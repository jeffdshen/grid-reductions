package postprocessor;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import parser.GadgetParser;
import types.Grid;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ImagePostProcessor implements PostProcessor<Grid<String>, BufferedImage>, PostWriter<BufferedImage> {
    private static final Logger logger = Logger.getLogger(GadgetParser.class.getName());

    private final Map<String, BufferedImage> map;
    private final int sizeX;
    private final int sizeY;
    public ImagePostProcessor(Map<String, BufferedImage> map, int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.map = ImmutableMap.copyOf(map);
    }

    @Override
    public BufferedImage process(Grid<String> grid) {
        int lengthX = grid.getSizeX();
        int lengthY = grid.getSizeY();
        BufferedImage image = new BufferedImage(sizeX * lengthX, sizeY * lengthY, BufferedImage.TYPE_INT_ARGB);

        Graphics g = image.getGraphics();

        for (int x = 0; x < lengthX; x++) {
            for (int y = 0; y < lengthY; y++) {
                String s = grid.getCell(x, y);
                if (map.containsKey(s)) {
                    g.drawImage(map.get(s), x * sizeX, y * sizeY, sizeX, sizeY, null);
                } else {
                    throw new IllegalArgumentException(String.format("Unknown string in cell (%s, %s): %s", x, y, s));
                }
            }
        }

        return image;
    }

    @Override
    public void write(BufferedImage image, File file) {
        try {
            ImageIO.write(image, "PNG", file);
        } catch (IOException e) {
            logger.error("Could not write image to file : " + file.getName(), e);
        }
    }
}
