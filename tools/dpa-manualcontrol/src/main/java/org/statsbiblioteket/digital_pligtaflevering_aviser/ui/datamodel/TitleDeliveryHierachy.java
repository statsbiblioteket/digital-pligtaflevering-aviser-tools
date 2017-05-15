package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The full hierachy of metadata from a list of titles and deliveries
 */
@XmlRootElement(name = "hierachy")
@XmlAccessorType(XmlAccessType.FIELD)
public class TitleDeliveryHierachy {

    @XmlElement(name = "title")
    private ArrayList<DeliveryTitleInfo> deliveryStructure = new ArrayList<DeliveryTitleInfo>();

    /**
     * Add DeliveryTitleInfo to list, if it allready exists return without adding
     * @param ds
     */
    public void addDeliveryToTitle(DeliveryTitleInfo ds) {
        if(!hasDeliveryTitleCheckStatus(ds.getNewspaperTitle(), ds.getDeliveryName())) {
            deliveryStructure.add(ds);
        }
    }

    /**
     * Get the full DeliveryStructure
     * @return
     */
    public ArrayList<DeliveryTitleInfo> getTheFullStruct() {
        return deliveryStructure;
    }


    /**
     * Write information to the defined DeliveryTitleInfo
     * @param title
     * @param delivery
     * @param checked
     * @param initials
     * @param comment
     * @param missingItems
     * @return
     */
    public DeliveryTitleInfo setDeliveryTitleCheckStatus(String title, String delivery, boolean checked, String initials, String comment, List<MissingItem> missingItems) {
        DeliveryTitleInfo delId = getDeliveryTitleCheckStatus(title, delivery);
        delId.setChecked(checked);
        delId.setInitials(initials);
        delId.setComment(comment);
        delId.setMissingItems(missingItems);
        return delId;
    }

    /**
     * Does the dataset contain this DeliveryTitleInfo allready
     * @param title
     * @param delivery
     * @return
     */
    public boolean hasDeliveryTitleCheckStatus(String title, String delivery) {
        return deliveryStructure.stream()
                .filter(bob -> (bob.getDeliveryName().equals(delivery) && bob.getNewspaperTitle().equals(title))).count() > 0;
    }

    /**
     * Get the initiated DeliveryTitleInfo from title and delivery
     * @param title
     * @param delivery
     * @return
     */
    public DeliveryTitleInfo getDeliveryTitleCheckStatus(String title, String delivery) {
        return deliveryStructure.stream()
                .filter(bob -> (bob.getDeliveryName().equals(delivery) && bob.getNewspaperTitle().equals(title))).findFirst().get();
    }

    /**
     * Get all titles available in the deliveries
     * @return
     */
    public List<String> getAllTitles() {
        List<DeliveryTitleInfo> deliveryTitleList = deliveryStructure.stream()
                .filter(distinctByKey(p -> p.getNewspaperTitle())).collect(Collectors.toList());

        List<String> titleList = new ArrayList<String>();

        for(DeliveryTitleInfo deliveryTitle : deliveryTitleList) {
            titleList.add(deliveryTitle.getNewspaperTitle());
        }
        return titleList;
    }

    /**
     * Function for filtering on specific equal parameters
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Get a list of all DeliveryTitleInfo which contains the specified title
     * @param title
     * @return
     */
    public List<DeliveryTitleInfo> getDeliverysFromTitle(String title) {
        return deliveryStructure.stream()
                .filter(delivery -> ( delivery.getNewspaperTitle().equals(title)))
                .collect(Collectors.toList());
    }

    /**
     * Get a list of all DeliveryTitleInfo which contains the specified deliveryname
     * @param delivery
     * @return
     */
    public List<DeliveryTitleInfo> getDeliveryTitleObjects(String delivery) {
        return deliveryStructure.stream()
                .filter(deliveryObject -> deliveryObject.getDeliveryName().equals(delivery))
                .collect(Collectors.toList());
    }
}
