package reduction.xml;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class ImagesXml {
    @XmlAttribute
    private int sizeX;

    @XmlAttribute
    private int sizeY;

    @XmlElement(name="image")
    private List<ImageXml> images;

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public List<ImageXml> getImages() {
        return images;
    }

    @Override
    public String toString() {
        return "ImagesXml{" +
            "sizeX=" + sizeX +
            ", sizeY=" + sizeY +
            ", images=" + images +
            '}';
    }
}
