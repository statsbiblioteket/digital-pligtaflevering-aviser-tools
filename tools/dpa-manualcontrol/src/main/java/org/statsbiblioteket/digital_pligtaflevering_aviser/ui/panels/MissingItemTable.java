package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * List containing single String items
 */
public class MissingItemTable extends VerticalLayout {

    private Table table;

    private HorizontalLayout hl = new HorizontalLayout();

    private Button add = new Button("Add to table");
    private TextField type = new TextField();
    private TextField name = new TextField();

    public MissingItemTable() {

        this.setSpacing(false);

        // Bind a table to it
        table = new Table("List");
        table.addContainerProperty("Type", String.class, null);
        table.addContainerProperty("Name", String.class, null);
        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);

        add.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                Object newItemId = table.addItem();
                Item row1 = table.getItem(newItemId);
                row1.getItemProperty("Type").setValue(type.getValue());
                row1.getItemProperty("Name").setValue(name.getValue());
            }});

        hl.addComponent(add);
        hl.addComponent(type);
        hl.addComponent(name);
        this.addComponent(hl);
        this.addComponent(table);
    }
}
