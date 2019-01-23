package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name="fileList", namespace = "kb.dk/dpa/embeddedfiles")
/**
 <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 <ns2:fileList xmlns:ns2="kb.dk/dpa/embeddedfiles">
 <EmbeddedItems xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema" xsi:type="xs:string">NewspaperAds_1v4.joboptions</EmbeddedItems>
 <EmbeddedItems xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema" xsi:type="xs:string">NewspaperAds_1v4.joboptions</EmbeddedItems>
 </ns2:fileList>
 */
public class JaxbList<T> implements java.io.Serializable {

    @XmlElement(name="EmbeddedItems")
    protected List<T> EmbeddedItems;

    public JaxbList(){}

    public JaxbList(List<T> EmbeddedItems){
        this.EmbeddedItems=EmbeddedItems;
    }


    public List<T> getList(){
        return EmbeddedItems;
    }
}