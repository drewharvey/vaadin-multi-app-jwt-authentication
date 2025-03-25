package com.example.application.views;

import com.example.application.MainUI;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SpringView(name="profile", ui = MainUI.class)
public class ProfileView extends VerticalLayout implements View {

    public ProfileView() {
        TextField fullName = new TextField("Full Name", "John Doe");
        TextField nickName = new TextField("Nickname", "Johnny");
        CheckBox isExtUser = new CheckBox("Is Ext. User", true);
        ComboBox<String> appTheme = new ComboBox<>("Theme");
        appTheme.setValue("Demo Theme");
        addComponents(fullName, nickName, isExtUser, appTheme);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // Handle any view entry logic here
    }
}