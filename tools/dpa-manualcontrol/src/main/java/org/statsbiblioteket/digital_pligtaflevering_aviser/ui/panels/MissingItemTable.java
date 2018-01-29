package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.MissingItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Table for viewing items which is missing. This table it used in a dialog that gives the user the possibility of
 * committing the validation-status of a title in a delivery
 */
public class MissingItemTable extends VerticalLayout {

    private BeanItemContainer beans = new BeanItemContainer(MissingItem.class);
    private Table table;
    private List<MissingItem> missingItems = new ArrayList<MissingItem>();

    private HorizontalLayout hl = new HorizontalLayout();

    private Button add = new Button("Add to missing table");
    private TextField type = new TextField();
    private TextField name = new TextField();

    public MissingItemTable() {

        // Bind a table to it
        table = new Table(MissingItem.class.getSimpleName(), beans);

        this.setSpacing(false);

        // Bind a table to it
        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);

        add.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                MissingItem item = new MissingItem(type.getValue(), name.getValue());
                beans.addBean(item);
                missingItems.add(item);

            }
        });

        hl.addComponent(add);
        hl.addComponent(type);
        hl.addComponent(name);
        this.addComponent(table);
        this.addComponent(hl);
    }

    public void setInfo(List<MissingItem> items) {
        beans.removeAllItems();
        missingItems.clear();
        for (MissingItem o : items) {
            missingItems.add(o);
            beans.addBean(o);
        }
    }

    public List<MissingItem> getInfo() {
        return missingItems;
    }
}
