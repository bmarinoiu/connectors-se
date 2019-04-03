package org.talend.components.magentocms.helpers.authhandlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.talend.components.magentocms.common.AuthenticationLoginPasswordConfiguration;
import org.talend.components.magentocms.common.MagentoDataStore;
import org.talend.components.magentocms.common.UnknownAuthenticationTypeException;
import org.talend.components.magentocms.service.http.BadCredentialsException;
import org.talend.components.magentocms.service.http.MagentoHttpClientService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class AuthorizationHandlerLoginPassword implements AuthorizationHandler {

    private static Map<AuthenticationLoginPasswordConfiguration, String> cachedTokens = new ConcurrentHashMap<>();

    private final MagentoHttpClientService magentoHttpClientService;

    public static void clearTokenCache(AuthenticationLoginPasswordConfiguration authenticationLoginPasswordSettings) {
        cachedTokens.remove(authenticationLoginPasswordSettings);
    }

    @Override
    public String getAuthorization(MagentoDataStore magentoDataStore)
            throws UnknownAuthenticationTypeException, BadCredentialsException {
        AuthenticationLoginPasswordConfiguration authSettings = (AuthenticationLoginPasswordConfiguration) magentoDataStore
                .getAuthSettings();

        String accessToken = cachedTokens.get(authSettings);
        if (accessToken == null) {
            synchronized (cachedTokens) {
                accessToken = cachedTokens.get(authSettings);
                if (accessToken == null) {
                    accessToken = getToken(magentoDataStore);
                    if (accessToken != null) {
                        cachedTokens.put(authSettings, accessToken);
                    }
                }
            }
        }

        if (accessToken == null) {
            throw new BadCredentialsException("Get user's token exception (token is not set)");
        }

        return "Bearer " + accessToken;
    }

    private String getToken(MagentoDataStore magentoDataStore) throws UnknownAuthenticationTypeException {
        String accessToken;
        accessToken = getTokenForUser(magentoDataStore, UserType.USER_TYPE_CUSTOMER);
        if (accessToken == null) {
            accessToken = getTokenForUser(magentoDataStore, UserType.USER_TYPE_ADMIN);
        }
        return accessToken;
    }

    private String getTokenForUser(MagentoDataStore magentoDataStore, UserType userType)
            throws UnknownAuthenticationTypeException {
        AuthenticationLoginPasswordConfiguration authSettings = (AuthenticationLoginPasswordConfiguration) magentoDataStore
                .getAuthSettings();
        String login = authSettings.getAuthenticationLogin();
        String password = authSettings.getAuthenticationPassword();

        String magentoUrl = "index.php/rest/" + magentoDataStore.getMagentoRestVersion() + "/integration/" + userType.getName()
                + "/token";
        String accessToken = magentoHttpClientService.getToken(magentoUrl, login, password);
        if (accessToken != null && accessToken.isEmpty()) {
            accessToken = null;
        }
        return accessToken;
    }

    @Getter
    @AllArgsConstructor
    enum UserType {
        USER_TYPE_ADMIN("admin"),
        USER_TYPE_CUSTOMER("customer");

        private String name;
    }

}
