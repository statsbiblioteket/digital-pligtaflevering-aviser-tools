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


    public void addDeliveryToTitle(String title, DeliveryIdentifier ds) {
        otherStructure.add(ds);
    }


    public ArrayList<DeliveryIdentifier> getTheFullStruct() {
        return otherStructure;
    }



    public DeliveryIdentifier setDeliveryTitleCheckStatus(String title, String delivery, boolean checked, String initials, String comment) {
        DeliveryIdentifier delId = getDeliveryTitleCheckStatus(title, delivery);
        delId.setChecked(checked);
        delId.setInitials(initials);
        delId.setComment(comment);
        return delId;
    }


    public DeliveryIdentifier getDeliveryTitleCheckStatus(String title, String delivery) {
        return otherStructure.stream().filter(bob -> (bob.getDeliveryName().equals(delivery) && bob.getNewspaperTitle().equals(title))).collect(Collectors.toList()).get(0);
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
