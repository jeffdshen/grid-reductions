package reduction.xml;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ModuleXml {
    private String name;
    private InputXml input;
    private OutputXml output;

    public String getName() {
        return name;
    }

    public InputXml getInput() {
        return input;
    }

    public OutputXml getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return "ModuleXml{" +
            "name='" + name + '\'' +
            ", input=" + input +
            ", output=" + output +
            '}';
    }
}
