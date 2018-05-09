package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.NewspaperContextListener;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
//TODO:MMJ CHECK THIS
/**
 * The Calenderview
 * The full panel for showing all selection details of deliveries
 */
public class FrontpageOverviewPanel extends VerticalLayout implements StatisticsPanels {

    private Logger log = LoggerFactory.getLogger(getClass());

    private DataModel model;
    private HorizontalLayout tablesLayout = new HorizontalLayout();
    private HorizontalLayout buttonLayout = new HorizontalLayout();
    private SingleStringListPanel infoPanel = new SingleStringListPanel("Title");
    private FrontpagePanel frontpagePanel = new FrontpagePanel();

    /**
     * Construct the panel with a reference to the datamodel
     * @param model
     */
    public FrontpageOverviewPanel(DataModel model) {
        this.model = model;

        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                try {

                    String cashingFolder = NewspaperContextListener.configurationmap.getRequired("dpa.manualcontrol.cashingfolder");

                    Object page = itemClickEvent.getItem().getItemProperty("Title").getValue();
                    String selectedTitle = page.toString();

                    model.setSelectedTitle(selectedTitle);
                    model.setSelectedSection(null);
                    model.saveThumbnailsToFilesystem();

                    String folder = cashingFolder+"/"+model.getSelectedMonthString()+"/thumbnails/" + selectedTitle;
                    frontpagePanel.setInfo(folder);
                } catch (Exception e) {
                    log.error("Date could not get parsed", e);
                }

            }
        });


        tablesLayout.addComponent(infoPanel);
        infoPanel.setWidth("200px");
        tablesLayout.addComponent(frontpagePanel);
        tablesLayout.setExpandRatio(frontpagePanel, 1f);

        this.addComponent(buttonLayout);
        this.addComponent(tablesLayout);
    }


    /**
     * Initiate columnwith of the graphical components
     */
    @Override
    public void initialLayout() {

    }

    /**
     * Dummy implementation
     * @param itemId
     * @param checkedState
     */
    @Override
    public void checkThePage(Object itemId, ConfirmationState checkedState) {

    }

    /**
     * Dummy implementation
     * @param itemId
     * @param checkedState
     */
    @Override
    public void checkTheArticle(Object itemId, ConfirmationState checkedState) {

    }

    /**
     * Dummy implementation
     * @param listener
     */
    @Override
    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {

    }


    @Override
    public void insertInitialTableValues() throws Exception {
        String cashingFolder = NewspaperContextListener.configurationmap.getRequired("dpa.manualcontrol.cashingfolder");
        String selectedCashing = cashingFolder + model.getSelectedMonthString() + "/thumbnails";
        Collection<String> list = new ArrayList<String>();
        File cashingFiles = new File(selectedCashing);
        List<File> files = Arrays.asList(cashingFiles.listFiles()).stream().filter(File::isDirectory).collect(Collectors.toList());

        for(File f : files) {
            list.add(f.getName());
        }
        infoPanel.setTableContent(list);
    }

    /**
     * Dummy implementation
     */
    @Override
    public void viewDialogForSettingDeliveryToChecked() {
    }

    /**
     * Dummy implementation
     */
    @Override
    public void viewIsEntered() {
        System.out.println("ENTERED");
        try {
            model.initiateThumbnailFolders();
            insertInitialTableValues();
        } catch (Exception e) {
            log.error("Date could not get parsed", e);
        }

    }
}
