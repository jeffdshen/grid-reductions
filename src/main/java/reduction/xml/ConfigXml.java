package reduction.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigXml {
    private InputXml input;

    public InputXml getInput() {
        return input;
    }

    @Override
    public String toString() {
        return "ConfigXml{" +
            "input=" + input +
            '}';
    }
}
