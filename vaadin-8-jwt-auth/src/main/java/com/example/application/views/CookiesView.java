package com.example.application.views;

import com.example.application.MainUI;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import javax.servlet.http.Cookie;

@SpringView(name="cookies", ui = MainUI.class)
public class CookiesView extends VerticalLayout implements View {

    public CookiesView() {
        setSpacing(true);
        Label header = new Label("<h2>Cookies listed below</h2>", ContentMode.HTML);
        addComponent(header);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        printCookie("auth_token");
        printCookie("refresh_token");
    }

    private void printCookie(String name) {
        Cookie targetCookie = null;

        Cookie[] cookies = VaadinRequest.getCurrent().getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                targetCookie = cookie;
            }
        }

        addComponent(new Label("<h3>" + name + "</h3>", ContentMode.HTML));
        addComponent(targetCookie != null ? new Label("<p>" + targetCookie.getValue() + "</p>", ContentMode.HTML) : new Label("Not Found"));
    }
}
