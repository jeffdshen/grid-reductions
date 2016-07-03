package postprocessor;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class PostProcessorUtils {
    /**
     * Pops up the image in a JFrame
     * @param image The image to show
     */
    public static void showImage(BufferedImage image) {
        // TODO: scale image
        JFrame frame = new JFrame();
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
