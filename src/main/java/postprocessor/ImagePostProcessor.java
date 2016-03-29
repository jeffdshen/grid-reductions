package postprocessor;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import parser.GadgetParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ImagePostProcessor {
    private static final Logger logger = Logger.getLogger(GadgetParser.class.getName());

    private final Map<String, BufferedImage> map;
    private final int sizeX;
    private final int sizeY;
    private final File output;

    public ImagePostProcessor(Map<String, BufferedImage> map, int sizeX, int sizeY, File output) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.output = output;
        this.map = ImmutableMap.copyOf(map);
    }

    // TODO modularize and generalize
    public void process(String[][] grid) {
        int lengthX = grid.length;
        int lengthY = lengthX == 0 ? 0 : grid[0].length;
        BufferedImage image = new BufferedImage(sizeX * lengthX, sizeY * lengthY, BufferedImage.TYPE_INT_ARGB);

        Graphics g = image.getGraphics();

        for (int x = 0; x < lengthX; x++) {
            for (int y = 0; y < lengthY; y++) {
                String s = grid[x][y];
                if(map.containsKey(s)) {
                    g.drawImage(map.get(s), x * sizeX, y * sizeY, sizeX, sizeY, null);
                }
                else{
                    throw new IllegalArgumentException(String.format("Unknown string in cell (%s, %s): %s", x, y, s));
                }
            }
        }

        // TODO move this to a utils class
        JFrame frame = new JFrame();
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        try {
            ImageIO.write(image, "PNG", output);
        } catch (IOException e) {
            logger.error("Could not write image to file : " + output.getName(), e);
        }
    }
}
