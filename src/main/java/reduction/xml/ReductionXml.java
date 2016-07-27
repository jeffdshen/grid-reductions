package reduction.xml;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name="reduction")
@XmlType(propOrder = {"data", "modules"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ReductionXml {
    private DataXml data;

    @XmlElementWrapper
    @XmlElement(name="module")
    private List<ModuleXml> modules;

    public DataXml getData() {
        return data;
    }

    public List<ModuleXml> getModules() {
        return modules;
    }

    @Override
    public String toString() {
        return "ReductionXml{" +
            "data=" + data +
            ", module=" + modules +
            '}';
    }
}
