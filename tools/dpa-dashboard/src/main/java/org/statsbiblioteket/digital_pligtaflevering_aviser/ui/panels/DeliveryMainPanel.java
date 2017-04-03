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
    protected GenericListTable sectionSectionTable = new GenericListTable(TitleComponent.class, null, new String[]{"sectionName", "sectionNumber"});//
    protected GenericListTable fileSelectionPanel = new GenericListTable(Page.class, null, new String[]{"pageName", "pageNumber", "sectionName", "sectionNumber"});//
    protected String selectedSection = null;


    public DeliveryMainPanel() {
        this.setWidth("100%");
        this.setHeight("100%");
    }


    public DeliveryMainPanel(DataModel model) {
        this();
        this.model = model;

    }

    public void addPageList(List<Page> pagelist) {
        fileSelectionPanel.setInfo(pagelist);
    }


    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        fileSelectionPanel.addItemClickListener(listener);
    }

    public void performIt() throws Exception {

    }

    public List runThrough() {
        return new ArrayList();
    }
}
