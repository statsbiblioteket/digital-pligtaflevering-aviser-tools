package org.kb.ui.panels;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsParser;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.tableBeans.FileComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryInformationPanel2 extends DeliveryMainPanel {


    private DomsParser parser;
    private FileListTable table = new FileListTable(FileComponent.class);

    ArrayList<FileComponent> alr = new ArrayList<FileComponent>();


    public DeliveryInformationPanel2(DomsParser parser) {
        this.setWidth("100%");


        this.parser = parser;
        this.addComponent(table);
    }

    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {

    }


    public void setBatch(FetchEventStructure eventStructureCommunication, String info) {

        table.setEnabled(true);






        Stream<DomsItem> items = eventStructureCommunication.getState(info);

        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                /*Object newItemId = table.addItem();
                com.vaadin.data.Item row1 = table.getItem(newItemId);
                row1.getItemProperty("Batch").setValue(o.getPath());
                itemList.put(row1, o);*/

                DeliveryStatistics delStat =parser.processDomsIdToStream().apply(o);

                table.setCaption(delStat.getDeliveryName());

                for(Title title : delStat.getTitles().getTitles()) {

                    List<Article> articles = title.getArticle();
                    for(Article art : articles) {
                        alr.add(new FileComponent(o.getPath(),title.getTitle(),art.getArticleName()));
                    }

                    List<Page> pages = title.getPage();
                    for(Page art : pages) {
                        alr.add(new FileComponent(o.getPath(),title.getTitle(),art.getPageName()));
                    }

                }







            }

        });

        table.setInfo(alr);




        /*for(Object o : delStat) {
            beans.addBean(o);
        }*/

    }
}
