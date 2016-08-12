package org.verapdf.pdfa.results;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * The purpose of the class is to convert the result of a prf-validation into xml
 */
public class ParseResult2Xml {

    /**
     * Deliver the validationresult to the static method and return the result as xml
     * @param objectModel
     * @return
     * @throws Exception
     */
    public static synchronized String convert2Xml(ValidationResult objectModel) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ValidationResultImpl.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(objectModel, sw);
        return sw.toString();
    }
}
