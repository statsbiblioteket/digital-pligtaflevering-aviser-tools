package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.MissingItem;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.MissingItemTable;
import java.util.Date;
import java.util.List;

/**
 * This panel contains results of validations in a titleDelivery, it gives the possiblity of viewing what has been validated
 * and store it if it is approved.
 */
public class ResultStorePanel extends VerticalLayout {

    private TextField initials = new TextField("Initials");
    private DateField date = new DateField();
    private TextArea area = new TextArea("Check description");
    private BeanItemContainer pageBeans;
    private BeanItemContainer articleBeans;
    private Table pageTable;
    private Table articleTable;
    private MissingItemTable missingItemTable = new MissingItemTable();

    public ResultStorePanel() {
        super();
        this.setSpacing(true);
        pageBeans = new BeanItemContainer(Page.class);
        // Bind a table to it
        pageTable = new Table("checked pages", pageBeans);
        pageTable.setWidth("100%");
        pageTable.setHeight("150px");
        pageTable.setSelectable(true);
        pageTable.setImmediate(true);
        this.addComponent(pageTable);

        articleBeans = new BeanItemContainer(Article.class);
        // Bind a table to it
        articleTable = new Table("checked articles", articleBeans);
        articleTable.setWidth("100%");
        articleTable.setHeight("150px");
        articleTable.setSelectable(true);
        articleTable.setImmediate(true);
        this.addComponent(articleTable);


        missingItemTable.setWidth("500px");
        missingItemTable.setHeight("150px");
        this.addComponent(missingItemTable);

        // Set the date to present
        date.setValue(new Date());
        area.setRows(10);
        area.setWidth("500px");

        this.addComponent(initials);
        this.addComponent(date);
        this.addComponent(area);

    }

    /**
     * Set the initials of the person currently performing the approvement
     * @param defaultInitials
     */
    public void setInitials(String defaultInitials) {
        initials.setValue(defaultInitials);
    }

    /**
     * Set values to be viewed in the panel
     * @param item
     */
    public void setValues(DeliveryTitleInfo item) {

        if (item.getComment() != null) {
            area.setValue(item.getComment());
        }
        if (item.getInitials() != null) {
            initials.setValue(item.getInitials());
        }
        for (Page o : item.getPageList()) {
            pageBeans.addBean(o);
        }
        for (Article o : item.getArticleList()) {
            articleBeans.addBean(o);
        }
        missingItemTable.setInfo(item.getMissingItems());
    }

    /**
     * Get a list of items, that has been marekd as missin items
     * @return
     */
    public List<MissingItem> getMissingItems() {
        return missingItemTable.getInfo();
    }


    @Override
    public void setEnabled(boolean enabled) {
        pageBeans.removeAllItems();
        articleBeans.removeAllItems();
        super.setEnabled(enabled);
    }

    /**
     * Set a caption to the panel
     * @param caption
     */
    @Override
    public void setCaption(String caption) {
        pageTable.setCaption(caption);
    }

    /**
     * Get the initials from the edit-field, the initial value can have been replaced by the user
     * @return
     */
    public String getInitials() {
        return initials.getValue();
    }

    /**
     * Get the text from the commentField
     * @return
     */
    public String getComment() {
        return area.getValue();
    }

}
