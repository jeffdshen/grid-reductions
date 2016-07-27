package reduction.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class ImageKeyXml {
    @XmlValue
    private String key;

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "ImageKeyXml{" +
            "key='" + key + '\'' +
            '}';
    }
}
