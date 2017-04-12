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
 * Created by mmj on 3/23/17.
 */
@XmlRootElement(name = "hierachy")
@XmlAccessorType(XmlAccessType.FIELD)
public class TitleDeliveryHierachy {

    @XmlElement(name = "title")
    private ArrayList<DeliveryIdentifier> otherStructure = new ArrayList<DeliveryIdentifier>();


    public void addDeliveryToTitle(DeliveryIdentifier ds) {
        otherStructure.add(ds);
    }


    public ArrayList<DeliveryIdentifier> getTheFullStruct() {
        return otherStructure;
    }


    /**
     * Write information to the defined DeliveryIdentifier
     * @param title
     * @param delivery
     * @param checked
     * @param initials
     * @param comment
     * @param missingItems
     * @return
     */
    public DeliveryIdentifier setDeliveryTitleCheckStatus(String title, String delivery, boolean checked, String initials, String comment, List<MissingItem> missingItems) {
        DeliveryIdentifier delId = getDeliveryTitleCheckStatus(title, delivery);
        delId.setChecked(checked);
        delId.setInitials(initials);
        delId.setComment(comment);
        delId.setMissingItems(missingItems);
        return delId;
    }

    /**
     * Get the initiated DeliveryIdentifier from title and delivery
     * @param title
     * @param delivery
     * @return
     */
    public DeliveryIdentifier getDeliveryTitleCheckStatus(String title, String delivery) {
        return otherStructure.stream().filter(bob -> (bob.getDeliveryName().equals(delivery) && bob.getNewspaperTitle().equals(title))).findFirst().get();
    }


    public List<String> getAllTitles() {
        List<DeliveryIdentifier> f = otherStructure.stream().filter(distinctByKey(p -> p.getNewspaperTitle())).collect(Collectors.toList());
        List<String> titleList = new ArrayList<String>();

        for(DeliveryIdentifier a : f) {
            titleList.add(a.getNewspaperTitle());
        }
        return titleList;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    public List<DeliveryIdentifier> getDeliverysFromTitle(String title) {
        return otherStructure.stream().filter(bob -> ( bob.getNewspaperTitle().equals(title))).collect(Collectors.toList());
    }



    public List<DeliveryIdentifier> getOtherStructure(String delivery) {
        return otherStructure.stream().filter(bob -> bob.getDeliveryName().equals(delivery)).collect(Collectors.toList());
    }
}
