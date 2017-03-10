package org.kb.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.HorizontalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsParser;
import org.kb.ui.FetchEventStructure;


/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryInformationPanel extends HorizontalLayout {

    DomsParser parser;

    DeliveryListPanel infoPanel = new DeliveryListPanel();
    TitleListPanel titPanel = new TitleListPanel();

    FileListTable table1 = new FileListTable(Article.class);
    FileListTable table2 = new FileListTable(Page.class);

    public DeliveryInformationPanel(DomsParser parser) {
        this.setWidth("100%");

        this.parser = parser;
        table1.setEnabled(false);
        table2.setEnabled(false);

        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                com.vaadin.data.Item selectedItem = itemClickEvent.getItem();
                DomsItem dItem = infoPanel.getDomsItem(selectedItem);

                DeliveryStatistics delStat =parser.processDomsIdToStream().apply(dItem);

                titPanel.setInfo(delStat);
                table1.setEnabled(false);
                table2.setEnabled(false);
            }
        });



        titPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                table1.setCaption(((Title)itemClickEvent.getItemId()).getTitle());
                table1.setEnabled(true);
                table1.setInfo(((Title)itemClickEvent.getItemId()).getArticle());
                table1.setCaption(((Title)itemClickEvent.getItemId()).getTitle());
                table2.setEnabled(true);
                table2.setInfo(((Title)itemClickEvent.getItemId()).getPage());
            }
        });

        this.addComponent(infoPanel);
        this.addComponent(titPanel);
        this.addComponent(table1);
        this.addComponent(table2);
    }

    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        table2.addItemClickListener(listener);
    }


    public void setBatch(FetchEventStructure eventStructureCommunication, String info) {
        infoPanel.setInfo(eventStructureCommunication, "Data_Archived");
    }
}
