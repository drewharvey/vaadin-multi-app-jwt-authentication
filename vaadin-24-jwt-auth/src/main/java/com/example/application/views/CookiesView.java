package com.example.application.views;

import com.example.application.security.JwtProperties;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.Cookie;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("CookiesView")
@Route("cookies")
@Menu(order = 1, icon = LineAwesomeIconUrl.CIRCLE)
@PermitAll
public class CookiesView extends VerticalLayout {

    private final JwtProperties jwtProperties;

    public CookiesView(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        printCookie(jwtProperties.getAuthTokenName());
        printCookie(jwtProperties.getRefreshTokenName());

        add(new H3("JWT Authentication Flow"));
        var img = new Image("images/jwt-with-refresh-flow_drawio.jpg", "JWT Authentication Flow");
        img.setMaxWidth("100%");
        add(img);
    }

    private void printCookie(String name) {
        Cookie targetCookie = null;

        Cookie[] cookies = VaadinRequest.getCurrent().getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                targetCookie = cookie;
            }
        }

        add(new H4(new Text(name), createStatusIcon(targetCookie)));

        var wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.getStyle().set("word-wrap", "break-word").set("font-size", "0.85rem");
        wrapper.add(targetCookie != null ? targetCookie.getValue() : "Not Found");
        add(wrapper);
    }

    private Icon createStatusIcon(Cookie tokenCookie) {
        Icon icon = tokenCookie != null ? VaadinIcon.CHECK.create() : VaadinIcon.CLOSE.create();
        icon.addClassNames(tokenCookie != null ? LumoUtility.TextColor.SUCCESS : LumoUtility.TextColor.ERROR);
        icon.addClassNames(LumoUtility.FontSize.XXSMALL, LumoUtility.Margin.Left.XSMALL);
        return icon;
    }

}
