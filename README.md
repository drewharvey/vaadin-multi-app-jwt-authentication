# Vaadin Multi-App JWT SSO Example
A demo showing a custom JWT-based SSO approach for Vaadin apps, of different versions, running on the same domain. This demo includes a Vaadin 24 and Vaadin 8 app, both using the same JWT token for SSO. If your apps are all newer versions of Vaadin, there are much easier solutions (see below).

## Disclaimer
**This is not a production-ready solution.**  This is a simple example to demonstrate how to implement a custom JWT-based SSO for Vaadin apps. It is recommended to use a commercial SSO solution like [Vaadin SSO Kit](https://vaadin.com/docs/latest/tools/sso) for production.  This implementation has not been tested to the same degree as the official Vaadin SSO Kit, or other commercial SSO solutions.

## Why use this approach?
This demo has two primary goals:
- Provide a way to have a single sign-on experience between multiple Vaadin apps, of differing versions, without adding another node to the ecosystem (such as an identity provider)
- Provide seamless navigation between two different apps - this can be very useful if you have a legacy app that you are migrating to a newer version of Vaadin, and you want to provide a seamless experience for your users.

## Running the Demo
To run the demo, you will need to run both the Vaadin 24 and Vaadin 8 apps.  You can run them in separate terminals, or in separate IDEs.  The apps will run on ports 8080 and 8081, respectively.  You can change the ports in the `application.properties` files if you wish. See the readme files in each project for more information.

## How it Works
At a high level, we are just using a typical JWT authentication technique, which includes an auth token and a refresh token. Each of the apps share the same secret keys for signing and verifying the tokens.

### JWT generation via authentication success handler
We add a authentication success handler which is in charge of generating the JWT tokens and setting them as secure cookies after a user has successfully logged in.

### JWT verification via request filter
We add a request filter which is in charge of verifying the JWT tokens on each request. If the tokens are valid, the user is allowed to proceed. If the tokens are invalid, the user is redirected to the login page. If the tokens are valid, but there is no authenticated user, the user is automatically logged in.

### JWT removal via logout handler
We add a logout handler which is in charge of removing the JWT tokens, from browser cookies, when a user logs out.

## Recommended SSO Alternatives
As mentioned above, there are better solutions for SSO under most circumstances.

### Vaadin 24+ Apps
If all your apps are Vaadin 24 or newer, you can use the [Vaadin SSO Kit](https://vaadin.com/docs/latest/tools/sso) to easily implement SSO.  The Vaadin SSO Kit is a commercial product, but it is a much more robust and secure solution than this example.

You can also use Spring's `setStatelessAuthentication` API, having all apps share the same secret key.  This is a more secure solution than this example. See [Stateless Authentication with Spring Security](https://vaadin.com/docs/latest/hilla/guides/security/spring-stateless).

## FAQ
### Why not use a third-party identity provider?
If you have a third-party identity provider, such as Okta, Auth0, or Keycloak, you should use that.  This example is for cases where you do not have a third-party identity provider, or you want to avoid adding another node to your ecosystem.

### Does this only work with Spring?
No, you can use this approach with any backend technology.  The key is being able to generate/verify the JWT tokens, which are stored as secure cookies.

### Can I use this with Vaadin versions 7 or earlier?
Yes, you can use this approach with Vaadin 7 or earlier.  You will need to adjust the code to work with whatever security backend you are using.

### Can I use this with applications on different domains?
No, this approach only works with applications on the same domain.  Cookies can only be shared between applications on the same domain.  If you need to have applications on different domains, you should use a third-party identity provider.

### Can I use this with applications on different ports?
Yes, you can use this approach with applications on different ports.  Cookies can be shared between applications on different ports, as long as they are on the same domain. This demo uses ports 8080 and 8081.

