package reduction.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class GadgetXml {
    private InputXml input;
    private String type;
    private Symmetries symmetries;

    public String getType() {
        return type;
    }

    public Symmetries getSymmetries() {
        return symmetries;
    }

    public InputXml getInput() {
        return input;
    }

    @Override
    public String toString() {
        return "GadgetXml{" +
            "input=" + input +
            ", type='" + type + '\'' +
            ", symmetries=" + symmetries +
            '}';
    }
}
