package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.NewspaperUI;

/**
 * The mainpanel is just the a defaultPanel
 */
public class MainView extends VerticalLayout implements View {
    public MainView() {
        setSizeFull();

        Button button = new Button("Jump to deliverypanel",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        getUI().getNavigator().navigateTo(NewspaperUI.DELIVERYPANEL);
                    }
                });
        addComponent(button);
        setComponentAlignment(button, Alignment.MIDDLE_CENTER);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Notification.show("Welcome to Main");
    }
}