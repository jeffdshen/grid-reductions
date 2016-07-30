package reduction;

import reduction.xml.ReductionXml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class ReductionXmlParser {
    public ReductionXml parse(File file) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ReductionXml.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (ReductionXml) jaxbUnmarshaller.unmarshal(file);
    }
}
