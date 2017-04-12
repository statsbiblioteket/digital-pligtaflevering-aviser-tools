package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by mmj on 4/12/17.
 */
@XmlRootElement
public class MissingItem implements java.io.Serializable {

    private String type;
    private String value;

    public MissingItem() {
    }

    public MissingItem(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    @XmlAttribute
    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    @XmlAttribute
    public void setValue(String value) {
        this.value = value;
    }
}
