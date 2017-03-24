package org.kb.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.HorizontalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.datamodel.DataModel;
import org.kb.ui.datamodel.TitleComponent;
import org.kb.ui.datamodel.Wrapper;

/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryMainPanel extends HorizontalLayout {

    protected DataModel model;//
    protected GenericListTable sectionSectionTable = new GenericListTable(TitleComponent.class);//
    protected GenericListTable fileSelectionPanel = new GenericListTable(Page.class);//


    public DeliveryMainPanel() {
        this.setWidth("100%");
        this.setHeight("100%");
    }


    public DeliveryMainPanel(DataModel model) {
        this();
        this.model = model;

    }


    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        fileSelectionPanel.addItemClickListener(listener);
    }

    public void performIt() {

    }



    public Wrapper getTitles() {

        return null;
    }
}
