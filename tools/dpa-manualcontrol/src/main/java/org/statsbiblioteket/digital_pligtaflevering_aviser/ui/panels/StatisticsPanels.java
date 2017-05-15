package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.ResultStorePanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.StoreResultWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * This interface is used to specify the interface that all the different statisticsPanels must comply to
 */
public interface StatisticsPanels extends Component {

    /**
     * Initially perform layout of components inb the panel
     */
    public void initialLayout();

    /**
     * Set the checkmark in the pageTable
     * @param itemId
     * @param checkedState
     */
    public void checkThePage(Object itemId, ConfirmationState checkedState);

    /**
     * Set the checkmark in the articleTable
     * @param itemId
     * @param checkedState
     */
    public void checkTheArticle(Object itemId, ConfirmationState checkedState);



    /**
     * Add selectionListener to fileSelectionTable
     * @param listener
     */
    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener);

    /**
     * Insert values which is initiated into the model to the UI-component in the respective panel
     * @throws Exception
     */
    public void insertInitialTableValues() throws Exception;

    /**
     * Show the Dialog for committing currently  validation status
     */
    public void viewDialogForSettingDeliveryToChecked();

    /**
     * Called when the view is entered to inform the panel that initiation can start
     */
    public void viewIsEntered();

}
