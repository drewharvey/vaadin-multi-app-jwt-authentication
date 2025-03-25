package com.example.application;

import com.example.application.data.User;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.AdminView;
import com.example.application.views.CookiesView;
import com.example.application.views.ProfileView;
import com.vaadin.annotations.Theme;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;

/**
 * Contains the app layout which is designed to resemble the basic Vaadin 24 app layout:
 *
 * <div class="main-layout">
 *   <div class="sidebar">
 *     <div class="app-heading">
 *       V8 App
 *     </div>
 *     <div class="nav">
 *       <a href="#">Nav Item 1</a>
 *       <a href="#">Nav Item 2</a>
 *     </div>
 *     <div class="user">
 *       User
 *     </div>
 *   </div>
 *   <div class="body">
 *     <div class="view-heading">
 *       Home View
 *     </div>
 *     <div class="content">
 *       Content
 *     </div>
 *   </div>
 * </div>
 */
@Theme("demo")
@SpringUI
public class MainUI extends UI {

    private AuthenticatedUser authenticatedUser;

    @Value("${v24.app.url}")
    private String v24AppUrl;

    @Autowired
    public MainUI(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        CssLayout mainLayout = new CssLayout();
        mainLayout.addStyleName("main-layout");
        setContent(mainLayout);

        // Sidebar
        CssLayout sidebar = new CssLayout();
        sidebar.addStyleName("sidebar");

        CssLayout appHeading = new CssLayout();
        appHeading.addStyleName("app-heading");
        appHeading.addComponents(new Label("V8 App"));

        CssLayout nav = new CssLayout();
        nav.addStyleName("nav");

        CssLayout user = new CssLayout();
        user.addStyleName("user");

        sidebar.addComponents(appHeading, nav, user);

        // Body
        CssLayout body = new CssLayout();
        body.addStyleName("body");

        CssLayout viewHeading = new CssLayout();
        viewHeading.addStyleName("view-heading");

        Label menuIcon = new Label(VaadinIcons.MENU.getHtml(), ContentMode.HTML);

        Label viewTitle = new Label("");
        viewTitle.addStyleName("view-title");
        viewHeading.addComponents(menuIcon, viewTitle);

        CssLayout content = new CssLayout();
        content.addStyleName("content");
        content.addComponent(new Label("Content"));

        body.addComponents(viewHeading, content);

        mainLayout.addComponents(sidebar, body);

        // navigation should match the v24 app

        // v24 views
        nav.addComponent(createNavButton("Home (V24)", "", VaadinIcons.HOME, true));
        nav.addComponent(createNavButton("Cookies (V24)", "cookies", VaadinIcons.CIRCLE_THIN, true));
        // v8 views
        nav.addComponent(createNavButton("Admin (V8)", "admin", VaadinIcons.USER, false));
        nav.addComponent(createNavButton("Profile (V8)", "profile", VaadinIcons.COG, false));
//        nav.addComponent(createNavButton("Legacy Cookies (V8)", "cookies", VaadinIcons.CIRCLE, false));

        // setup navigator
        Navigator navigator = new Navigator(this, content);
        navigator.addView("", AdminView.class);
        navigator.addView("admin", AdminView.class);
        navigator.addView("profile", ProfileView.class);
        navigator.addView("cookies", CookiesView.class); // not visible but available for testing

        // update title and active nave item when view changes
        navigator.addViewChangeListener((ViewChangeListener) e -> {
            // update heading
            viewTitle.setValue(e.getNewView().getClass().getSimpleName());
            // update nav active item
            int childrenCount = nav.getComponentCount();
            for (int i = 0; i < childrenCount; i++) {
                Component child = nav.getComponent(i);
                if (child instanceof Button) {
                    Button btn = (Button) child;
                    String viewName = (String) btn.getData();
                    if (StringUtils.equalsIgnoreCase(e.getViewName(), viewName)) {
                        btn.addStyleName("active");
                    } else {
                        btn.removeStyleName("active");
                    }
                }
            }
            return true;
        });
        setNavigator(navigator);

        // setup user displayed info
        if (authenticatedUser.getUser().isPresent()) {
            User userObj = authenticatedUser.getUser().get();

            Image avatar = new Image("", new StreamResource(
                    (StreamResource.StreamSource) () -> new ByteArrayInputStream(userObj.getProfilePicture()),
                    "profile-pic.jpg"));

            MenuBar userMenu = new MenuBar();

            MenuBar.MenuItem userName = userMenu.addItem(userObj.getName());
            userName.addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            user.addComponents(avatar, userMenu);
        } else {
            Button loginButton = new Button("Sign In", event -> getUI().getNavigator().navigateTo("login"));
            loginButton.addStyleName("login-button");
            user.addComponent(loginButton);
        }
    }

    private Button createNavButton(String text, String route, VaadinIcons icon, boolean isExternal) {
        Button btn = new Button(text);
        btn.addStyleName("nav-button");
        btn.setIcon(icon);
        btn.setData(route); // for tracking active nav item

        if (isExternal) {
            btn.addClickListener(e -> UI.getCurrent().getPage().setLocation(v24AppUrl + "/" + route));
        } else {
            btn.addClickListener(e -> getNavigator().navigateTo(route));
        }
        return btn;
    }

}