package reduction.xml;

import javax.xml.bind.annotation.XmlEnumValue;

public enum Symmetries {
    @XmlEnumValue(value = "all")
    ALL,

    @XmlEnumValue(value = "rotation")
    ROTATION,
}
