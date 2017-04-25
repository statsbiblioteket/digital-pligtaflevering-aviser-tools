package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.ResultStorePanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.StoreResultWindow;

import java.util.List;

/**
 * The full panel for showing all selection details of deliveries
 */
public class DeliveryOverviewPanel extends VerticalLayout implements StatisticsPanels {

    protected DataModel model;//

    protected HorizontalLayout tablesLayout = new HorizontalLayout();
    protected HorizontalLayout buttonLayout = new HorizontalLayout();

    protected SingleStringListPanel infoPanel = new SingleStringListPanel();
    protected DatePanel datePanel = new DatePanel();

    /**
     * Construct the panel with a reference to the datamodel
     * @param model
     */
    public DeliveryOverviewPanel(DataModel model) {
        this.model = model;

        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("Item").getValue();
                String selectedTitle = page.toString();

                model.setSelectedTitle(selectedTitle);
                model.setSelectedSection(null);
                showTheSelectedTitle();
                List<DeliveryTitleInfo> list = model.getDeliverysFromTitle(selectedTitle);

                datePanel.setInfo(list);
            }
        });


        tablesLayout.addComponent(infoPanel);
        tablesLayout.setExpandRatio(infoPanel, 0.1f);
        tablesLayout.addComponent(datePanel);

        this.addComponent(buttonLayout);
        this.addComponent(tablesLayout);

    }

    /**
     * Initiate columnwith of the graphical components
     */
    public void initialLayout() {
    }

    @Override
    public void checkThePage(Object itemId, ConfirmationState checkedState) {

    }

    @Override
    public void checkTheArticle(Object itemId, ConfirmationState checkedState) {

    }

    @Override
    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {

    }


    /**
     * Show the content of the selection defined by the delivery and title.
     * The information is fetched from fedora as the statistics stream and is shown in tables with section, article and page
     */
    protected void showTheSelectedTitle() {


        String selectedDelivery = model.getSelectedDelivery();
        String selectedTitle = model.getSelectedTitle();
        if(selectedDelivery==null || selectedTitle==null) {
            return;
        }

        Title title = model.getTitleObj(selectedDelivery, selectedTitle);
        if(title==null) {
            return;
        }
        model.selectTitleDelivery();
    }

    public void insertInitialTableValues() throws Exception {
        infoPanel.setTableContent(model.getTitlesFromFileSystem());
    }

    /**
     *
     */
    public void viewDialogForSettingDeliveryToChecked() {
        String selectedDelivery = model.getSelectedDelivery();
        String selectedTitle = model.getSelectedTitle();
        DeliveryTitleInfo item = model.getCurrentDelItem();

        final StoreResultWindow dialog = new StoreResultWindow(selectedTitle + " - " + selectedDelivery);
        ResultStorePanel storePanel = new ResultStorePanel();

        dialog.setDialogContent(storePanel);
        storePanel.setValues(item);
        dialog.setReady(!item.isChecked());
        dialog.setModal(true);

        UI.getCurrent().addWindow(dialog);
        dialog.setListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().removeWindow(dialog);
                if("OKBUTTON".equals(event.getButton().getId())) {

                    //selectedDelivery.
                    boolean writeResult = model.writeToCurrentItemCashed(selectedDelivery, selectedTitle, true,
                            storePanel.getInitials(), storePanel.getComment(), storePanel.getMissingItems());

                    if(!writeResult) {
                        Notification.show("The result can not get stored", Notification.Type.ERROR_MESSAGE);
                    }

                }
            }});

        dialog.addCloseListener(new Window.CloseListener() {
            // inline close-listener
            public void windowClose(Window.CloseEvent e) {

                UI.getCurrent().removeWindow(dialog);
            }
        });
    }
}
