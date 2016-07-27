package reduction.xml;

import javax.xml.bind.annotation.XmlEnumValue;

public enum InputOutputXmlType {
    @XmlEnumValue(value = "file")
    FILE,
    @XmlEnumValue(value = "string")
    STRING,
    @XmlEnumValue(value = "default_resource")
    DEFAULT_RESOURCE,
}
