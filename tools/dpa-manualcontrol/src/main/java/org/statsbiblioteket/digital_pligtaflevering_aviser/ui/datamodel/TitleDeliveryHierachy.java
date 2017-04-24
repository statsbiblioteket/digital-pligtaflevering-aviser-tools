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
    private ArrayList<DeliveryTitleInfo> otherStructure = new ArrayList<DeliveryTitleInfo>();


    public void addDeliveryToTitle(DeliveryTitleInfo ds) {
        otherStructure.add(ds);
    }


    public ArrayList<DeliveryTitleInfo> getTheFullStruct() {
        return otherStructure;
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
     * Get the initiated DeliveryTitleInfo from title and delivery
     * @param title
     * @param delivery
     * @return
     */
    public DeliveryTitleInfo getDeliveryTitleCheckStatus(String title, String delivery) {
        return otherStructure.stream().filter(bob -> (bob.getDeliveryName().equals(delivery) && bob.getNewspaperTitle().equals(title))).findFirst().get();
    }


    public List<String> getAllTitles() {
        List<DeliveryTitleInfo> f = otherStructure.stream().filter(distinctByKey(p -> p.getNewspaperTitle())).collect(Collectors.toList());
        List<String> titleList = new ArrayList<String>();

        for(DeliveryTitleInfo a : f) {
            titleList.add(a.getNewspaperTitle());
        }
        return titleList;
    }

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
        return otherStructure.stream()
                .filter(delivery -> ( delivery.getNewspaperTitle().equals(title)))
                .collect(Collectors.toList());
    }

    /**
     * Get a list of all DeliveryTitleInfo which contains the specified deliveryname
     * @param delivery
     * @return
     */
    public List<DeliveryTitleInfo> getDeliveryTitleObjects(String delivery) {
        return otherStructure.stream()
                .filter(deliveryObject -> deliveryObject.getDeliveryName().equals(delivery))
                .collect(Collectors.toList());
    }
}
