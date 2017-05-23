package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * MissingItem contains the description of something in a delivery, which was expected to be there, but can not be found
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

    /**
     * Get the type of the missing item, this can be any undefined text.
     * It could be page, article, setcion or maybe something else
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type of the missing item, , this can be any undefined text.
     * It could be page, article, setcion or maybe something else
     * @param type
     */
    @XmlAttribute
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get a description about the missing item, like "The page exists in the physical delivery, but ist can not be found in the digital delivery"
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * Set a description about the missing item, like "The page exists in the physical delivery, but ist can not be found in the digital delivery"
     * @param value
     */
    @XmlAttribute
    public void setValue(String value) {
        this.value = value;
    }
}
