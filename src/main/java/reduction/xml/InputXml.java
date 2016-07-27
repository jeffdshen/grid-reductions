package reduction.xml;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class InputXml {
    @XmlAttribute
    private InputOutputXmlType type;
    @XmlValue
    private String input;

    public String getInput() {
        return input;
    }

    public InputOutputXmlType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Input{" +
            "type='" + type + '\'' +
            ", input='" + input + '\'' +
            '}';
    }
}
