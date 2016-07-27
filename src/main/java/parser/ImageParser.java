package parser;

import org.apache.log4j.Logger;
import utils.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import static org.apache.log4j.Level.ERROR;

public class ImageParser implements Parser<Image> {
    private static final Logger logger = Logger.getLogger(ImageParser.class.getName());

    @Override
    public Image parse(File file) {
        try {
            return parse(new FileInputStream(file), file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            logger.log(ERROR, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Image parse(InputStream stream, String id) {
        try {
            return ImageIO.read(stream);
        } catch (IOException e) {
            logger.log(ERROR, "Error while reading " + id + ": " + e.getMessage(), e);
        }

        return null;
    }
}
