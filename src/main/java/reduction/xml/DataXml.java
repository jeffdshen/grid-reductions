package reduction.xml;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataXml {
    @XmlElementWrapper
    @XmlElement(name="gadget")
    private List<GadgetXml> gadgets;

    @XmlElementWrapper
    @XmlElement(name="config")
    private List<ConfigXml> configs;

    @XmlElement(name="images")
    private ImagesXml images;

    public List<GadgetXml> getGadgets() {
        return gadgets;
    }

    public List<ConfigXml> getConfigs() {
        return configs;
    }

    public ImagesXml getImages() {
        return images;
    }

    @Override
    public String toString() {
        return "DataXml{" +
            "gadgets=" + gadgets +
            ", configs=" + configs +
            ", images=" + images +
            '}';
    }
}
