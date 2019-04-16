/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.security.session;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationUserDetailsAdapter;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.handlers.LoginHandler;
import io.micronaut.security.filters.SecurityFilter;
import io.micronaut.security.token.config.TokenConfiguration;
import io.micronaut.session.Session;
import io.micronaut.session.SessionStore;
import io.micronaut.session.http.SessionForRequest;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * {@link LoginHandler} implementation for Session-based Authentication.
 * @author Sergio del Amo
 * @since 1.0
 */
@Singleton
public class SessionLoginHandler implements LoginHandler {

    protected final SessionStore<Session> sessionStore;
    protected final SecuritySessionConfiguration securitySessionConfiguration;

    @Nullable
    private final String rolesKeyName;

    /**
     * Constructor.
     * @deprecated use {@link #SessionLoginHandler(SecuritySessionConfiguration, SessionStore, TokenConfiguration)}
     * @param securitySessionConfiguration Security Session Configuration
     * @param sessionStore The session store
     */
    @Deprecated
    public SessionLoginHandler(SecuritySessionConfiguration securitySessionConfiguration,
                               SessionStore<Session> sessionStore) {
        this.securitySessionConfiguration = securitySessionConfiguration;
        this.sessionStore = sessionStore;
        this.rolesKeyName = null;
    }

    /**
     * Constructor.
     * @param securitySessionConfiguration Security Session Configuration
     * @param sessionStore The session store
     * @param tokenConfiguration Token Configuration
     */
    @Inject
    public SessionLoginHandler(SecuritySessionConfiguration securitySessionConfiguration,
                               SessionStore<Session> sessionStore,
                               TokenConfiguration tokenConfiguration) {
        this.securitySessionConfiguration = securitySessionConfiguration;
        this.sessionStore = sessionStore;
        this.rolesKeyName = tokenConfiguration.getRolesName();
    }

    @Override
    public HttpResponse loginSuccess(UserDetails userDetails, HttpRequest<?> request) {
        Session session = SessionForRequest.findOrCreate(request, sessionStore);
        session.put(SecurityFilter.AUTHENTICATION, new AuthenticationUserDetailsAdapter(userDetails, rolesKeyName));
        try {
            URI location = new URI(securitySessionConfiguration.getLoginSuccessTargetUrl());
            return HttpResponse.seeOther(location);
        } catch (URISyntaxException e) {
            return HttpResponse.serverError();
        }
    }

    @Override
    public HttpResponse loginFailed(AuthenticationFailed authenticationFailed) {
        try {
            URI location = new URI(securitySessionConfiguration.getLoginFailureTargetUrl());
            return HttpResponse.seeOther(location);
        } catch (URISyntaxException e) {
            return HttpResponse.serverError();
        }
    }
}
