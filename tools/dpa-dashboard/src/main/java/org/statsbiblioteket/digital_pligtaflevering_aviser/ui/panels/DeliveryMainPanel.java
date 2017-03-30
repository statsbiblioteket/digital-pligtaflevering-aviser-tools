package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.HorizontalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleComponent;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.Wrapper;

import java.util.ArrayList;
import java.util.List;

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

    public void performIt() throws Exception {

    }

    public List runThrough() {
        return new ArrayList();
    }


    public Wrapper getTitles() {

        return null;
    }
}
