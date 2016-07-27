package reduction.xml;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class OutputXml {
    @XmlAttribute
    private InputOutputXmlType type;
    @XmlValue
    private String output;

    public String getOutput() {
        return output;
    }

    public InputOutputXmlType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "OutputXml{" +
            "type='" + type + '\'' +
            ", output='" + output + '\'' +
            '}';
    }
}
