# springboot-security-oauth2-keycloak #

This repository contains a Spring Boot application that demonstrates how to secure a REST API using Keycloak for authentication and authorization.

## Application Overview ##

This application has three end-points implemented in DucksController. Each end-point shall return a string.

```http request
GET /api/duey
GET /api/huey
GET /api/luey
```

This application concentrates on Spring Security and therefore no complex business logic is implemented behind the REST APIs.

## Keycloak Configuration ##

Pre-condition here is that you have a running Keycloak instance. See for example: https://www.keycloak.org/server/containers.

Keycloak configuration requires following four objects:
- **Realm**: A realm in Keycloak is a space where you manage objects such as users, applications, and roles. It is a logical grouping of these objects.
- **Client**: A client in Keycloak represents an application that can authenticate users and access resources. It is a representation of the application that will be using Keycloak for authentication and authorization.
- **User**: A user in Keycloak is an entity that can authenticate and access resources. Users can be assigned roles and permissions to control their access to resources.
- **Role**: A role in Keycloak is a set of permissions that can be assigned to users or groups. Roles are used to control access to resources and define what actions users can perform.

### Realm ###

1. Login to Keycloak admin console.
2. Open the realm selector and press **Create realm** button, which opens a create realm view.
3. Enter value `springboot-keycloak` to **Name** field.
4. Set **Enabled** switch to **ON**.
5. Press **Create** button.

### Client ###

1. Select `springboot-keycloak` realm.
2. Open the **Clients** tab.
3. Press **Create client** button, which opens a create client view.
4. Select value `OpenID Connect` from the **Client Protocol** dropdown.
5. Enter value `springboot-client` to **Client ID** field.
5. Press **Next** button.
6. Set **Client Authentication** switch to **ON**.
7. Select `Standard flow`, `Direct access grants` and `Service accounts roles`from the **Authentication flow**; unselect the others.
8. Press **Next** button.
9. Set value `http://localhost:8080/*` to **Valid redirect URIs** and **Web origins** fields.
10. Press **Save** button.

`Standard flow` is needed for FE application authentication, which is not implemented in this repository.

`Direct access grants` is needed for password credentials grant type.

`Service accounts roles` is needed for client credentials grant type.

### User ###

1. Select `springboot-keycloak` realm.
2. Open the **Users** tab.
3. Press **Create new user** button, which opens a create user view.
4. Set **Email verified** switch to **ON**.
5. Enter value `duey` to **Username** field.
6. Press **Create** button.
7. Select **Credentials** tab.
8. Press **Set Password** button, which opens a set password view.
9. Enter value `duck` to **Password** and **Passowrd confirmation** fields.
10. Set **Temporary** switch to **OFF**.
11. Press **Save** and then **Save password** buttons.

### Role ###

1. Select `springboot-keycloak` realm.
2. Open the **Realm roles** tab.
3. Press **Create role** button, which opens a create role view.
4. Enter value `USER` to **Role name** field.
5. Press **Save** button.
6. Select **Clients** tab.
7. Select `springboot-client` client.
8. Open the **Service accounts roles** tab.
9. Press **Assign role** button, which opens a assign role view.
10. Switch `Filter by client` to `Filter by realm rolels`.
11. Select `USER` role.
12. Press **Assign** button.


## Security Configuration ##

The end-points are secured as following:
- `/api/duey` is accessible only for users with `USER` role.
- `/api/huey` is accessible for authenticated users.
- `/api/luey` is accessible for all users.

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
    
    httpSecurity
        .sessionManagement(smc -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(requests -> requests
        .requestMatchers("/api/duey").hasRole("USER")
        .requestMatchers("/api/huey").authenticated()
        .requestMatchers("/api/luey").permitAll());
    
    httpSecurity.formLogin(Customizer.withDefaults());
    httpSecurity.oauth2ResourceServer(rsc -> rsc.jwt(jwtc -> jwtc.jwtAuthenticationConverter(jwtAuthenticationConverter)));
    
    return httpSecurity.build();
}
```

The security filter chain enables OAUth2 resource server, which get the token URL from `application.properties`.

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/springboot-keycloak
```

Since roles is SpringBoot Security have `ROLE_` prefix, the `KeycloakRoleConverter` is used to convert the roles from Keycloak to Spring Security roles.

```java
@Override
public Collection<GrantedAuthority> convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    List<String> roles = (List<String>) jwt.getClaim("realm_access").get("roles");
    for (String role : roles) {
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
    }
    return authorities;
}
```

## Access with Client Credentials ##

Access with client credentials is primarily for machine-to-machine communication.

The application client has predefined client ID and client secret to obtain an access token from Keycloak.

The access token is assigned to client application.

Following settings are needed for Postman to obtain an access token:
- **Use Token Type**: `Access token`
- **Header Prefix**: `Bearer`
- **Grant Type**: `Client credentials`
- **Access Token URL**: `http://<host>:<port>/realms/springboot-keycloak/protocol/openid-connect/token`
- **Client ID**: `springboot-client`
- **Client Secret**: client secret from Keycloak; **Clients** → `springboot-client` → **Credentials** → **Client secret**. 
- **Scope**: `openid`
- **Client Authentication**: `Send as Basic Auth header`

## Access with Password Credentials ##

Access with password credentials is primarily for end-user authentication.

The end-user provides username and password to obtain an access token from Keycloak.

The application client has predefined client ID and client secret to obtain an access token from Keycloak; using also the end-user credentials.

The access token is assigned to end-user.

Following settings are needed for Postman to obtain an access token:
- **Use Token Type**: `Access token`
- **Header Prefix**: `Bearer`
- **Grant Type**: `Password credentials`
- **Access Token URL**: `http://<host>:<port>/realms/springboot-keycloak/protocol/openid-connect/token`
- **Client ID**: `springboot-client`
- **Client Secret**: client secret from Keycloak; **Clients** → `springboot-client` → **Credentials** → **Client secret**.
- **Scope**: `openid`
- **Username**: `duey`
- **Password**: `duck`
- **Client Authentication**: `Send as Basic Auth header`
