package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name="fileList", namespace = "kb.dk/dpa/embeddedfiles")
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