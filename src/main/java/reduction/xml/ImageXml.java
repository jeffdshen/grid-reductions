package reduction.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ImageXml {
    private ImageKeyXml key;
    private InputXml input;

    public ImageKeyXml getKey() {
        return key;
    }

    public InputXml getInput() {
        return input;
    }

    @Override
    public String toString() {
        return "ImageXml{" +
            "key=" + key +
            ", input=" + input +
            '}';
    }
}
