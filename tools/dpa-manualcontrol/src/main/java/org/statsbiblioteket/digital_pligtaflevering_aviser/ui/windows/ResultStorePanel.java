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
 * Created by mmj on 3/9/17.
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
        pageBeans=new BeanItemContainer(Page.class);
        // Bind a table to it
        pageTable = new Table("Approved", pageBeans);
        pageTable.setWidth("100%");
        pageTable.setHeight("150px");
        pageTable.setSelectable(true);
        pageTable.setImmediate(true);
        this.addComponent(pageTable);

        articleBeans=new BeanItemContainer(Article.class);
        // Bind a table to it
        articleTable = new Table("Approved", articleBeans);
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

    public void setValues(DeliveryTitleInfo item) {

        if(item.getComment()!=null) {
            area.setValue(item.getComment());
        }
        if(item.getInitials()!=null) {
            initials.setValue(item.getInitials());
        }
        for(Page o : item.getPages()) {
            pageBeans.addBean(o);
        }
        for(Article o : item.getArticles()) {
            articleBeans.addBean(o);
        }
        missingItemTable.setInfo(item.getMissingItems());
    }

    public List<MissingItem> getMissingItems() {
        return missingItemTable.getInfo();
    }


    public void setEnabled(boolean enabled) {
        pageBeans.removeAllItems();
        articleBeans.removeAllItems();
        super.setEnabled(enabled);
    }

    public void setCaption(String caption) {
        pageTable.setCaption(caption);
    }


    public String getInitials() {
        return initials.getValue();
    }

    public String getComment() {
        return area.getValue();
    }

}
