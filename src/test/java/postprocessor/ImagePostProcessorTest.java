package postprocessor;

import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;
import parser.GadgetParser;
import types.Gadget;
import utils.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class ImagePostProcessorTest {
    public boolean equals(BufferedImage a, BufferedImage b) {
        // The images must be the same size.
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return false;
        }

        int width = a.getWidth();
        int height = a.getHeight();

        // data in order
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Test
    public void testParseGadget() throws Exception {
        BufferedImage zero = ImageIO.read(ResourceUtils.getRelativeFile(getClass(), "akari/images/zero10.png"));
        BufferedImage one = ImageIO.read(ResourceUtils.getRelativeFile(getClass(), "akari/images/one10.png"));
        BufferedImage two = ImageIO.read(ResourceUtils.getRelativeFile(getClass(), "akari/images/two10.png"));
        BufferedImage blank = ImageIO.read(ResourceUtils.getRelativeFile(getClass(), "akari/images/blank10.png"));
        BufferedImage black = ImageIO.read(ResourceUtils.getRelativeFile(getClass(), "akari/images/black10.png"));

        Map<String, Image> map = ImmutableMap.<String, Image>of("0", zero, "1", one, "2", two, "x", black, ".", blank);

        ImagePostProcessor ipp = new ImagePostProcessor(map, 10, 10);
        Gadget g = new GadgetParser().parse(ResourceUtils.getRelativeFile(getClass(), "akari/gadgets/or.txt"));

        BufferedImage or10 = ImageIO.read(ResourceUtils.getRelativeFile(getClass(), "akari/images/or10.png"));
        Assert.assertTrue(equals(or10, ipp.process(g)));
    }
}
