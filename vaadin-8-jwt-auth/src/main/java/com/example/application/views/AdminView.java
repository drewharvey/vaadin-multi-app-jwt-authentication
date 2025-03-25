package com.example.application.views;

import com.example.application.MainUI;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SpringView(name="admin", ui = MainUI.class)
public class AdminView extends VerticalLayout implements View {

    public AdminView() {
        Label info = new Label("<p>You have been a great admin :)</p>", ContentMode.HTML);
        addComponents(info);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // Handle any view entry logic here
    }
}
