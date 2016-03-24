package postprocessor;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;
import utils.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

public class ImagePostProcessorTest {

    // TODO make unit test, make process return image?
    @Test
    public void testParseGadget() throws Exception {
        BufferedImage zero = ImageIO.read(getClass().getResourceAsStream("/Akari/images/zero10.png"));
        BufferedImage one = ImageIO.read(getClass().getResourceAsStream("/Akari/images/one10.png"));
        BufferedImage two = ImageIO.read(getClass().getResourceAsStream("/Akari/images/two10.png"));
        BufferedImage blank = ImageIO.read(getClass().getResourceAsStream("/Akari/images/blank10.png"));
        BufferedImage black = ImageIO.read(getClass().getResourceAsStream("/Akari/images/black10.png"));
        Map<String, BufferedImage> map = ImmutableMap.of("0", zero, "1", one, "2", two, "x", black, ".", blank);

        File output = ResourceUtils.getRelativeFile(getClass(), "akari.png");
        ImagePostProcessor postProcessor = new ImagePostProcessor(map, 20, 20,output);
        postProcessor.process(new String[][] {
            new String[] {"x","x",".","x","x",},
            new String[] {"x","x",".","x","x",},
            new String[] {"x",".","1",".","x",},
            new String[] {"x",".","x",".","x",},
            new String[] {".","2",".","2",".",},
            new String[] {"x","0",".","0","x",},
        });
    }
}
