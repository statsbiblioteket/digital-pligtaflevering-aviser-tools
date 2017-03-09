package org.kb.ui.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

/**
 * Created by mmj on 3/8/17.
 */
public class MainView extends VerticalLayout implements View {
    public MainView() {
        setSizeFull();

        Button button = new Button("Main",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        //navigator.navigateTo(MAINVIEW);
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