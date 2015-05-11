/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.rs.security.oauth2.client;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.utils.ExceptionUtils;
import org.apache.cxf.jaxrs.utils.FormUtils;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.rs.security.oauth2.common.AccessTokenGrant;
import org.apache.cxf.rs.security.oauth2.common.ClientAccessToken;
import org.apache.cxf.rs.security.oauth2.grants.code.AuthorizationCodeGrant;
import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;

@PreMatching
@Priority(Priorities.AUTHENTICATION + 1)
public class ClientCodeRequestFilter implements ContainerRequestFilter {
    @Context
    private MessageContext mc;
    
    private String scopes;
    private String relRedirectUri;
    private String startUri;
    private String authorizationServiceUri;
    private Consumer consumer;
    private ClientCodeStateManager clientStateManager;
    private ClientTokenContextManager clientTokenContextManager;
    private WebClient accessTokenService;
    
    @Override
    public void filter(ContainerRequestContext rc) throws IOException {
        SecurityContext sc = rc.getSecurityContext();
        if (sc == null || sc.getUserPrincipal() == null) {
            throw ExceptionUtils.toNotAuthorizedException(null, null);
        }
        UriInfo ui = rc.getUriInfo();
        if (ui.getPath().endsWith(startUri)) {
            if (clientTokenContextManager != null) {
                ClientTokenContext request = clientTokenContextManager.getClientTokenContext(mc);
                if (request != null) {
                    setClientCodeRequest(request);
                    rc.setRequestUri(URI.create(relRedirectUri));
                    return;
                }
            }
            Response codeResponse = createCodeResponse(rc, sc, ui);
            rc.abortWith(codeResponse);
        } else if (ui.getPath().endsWith(relRedirectUri)) {
            processCodeResponse(rc, sc, ui);
        }
    }

    private Response createCodeResponse(ContainerRequestContext rc, SecurityContext sc, UriInfo ui) {
        MultivaluedMap<String, String> redirectState = createRedirectState(rc, sc, ui);
        String redirectScope = redirectState.getFirst(OAuthConstants.SCOPE);
        String theScope = redirectScope != null ? redirectScope : scopes;
        URI uri = OAuthClientUtils.getAuthorizationURI(authorizationServiceUri, 
                                             consumer.getKey(), 
                                             getAbsoluteRedirectUri(ui).toString(), 
                                             redirectState.getFirst(OAuthConstants.STATE), 
                                             theScope);
        return Response.seeOther(uri).build();
    }

    private URI getAbsoluteRedirectUri(UriInfo ui) {
        return ui.getBaseUriBuilder().path(relRedirectUri).build();
    }
    protected void processCodeResponse(ContainerRequestContext rc, SecurityContext sc, UriInfo ui) {
        MultivaluedMap<String, String> params = toRequestState(rc, ui);
        String codeParam = params.getFirst(OAuthConstants.AUTHORIZATION_CODE_VALUE);
        if (codeParam != null) {
            AccessTokenGrant grant = new AuthorizationCodeGrant(codeParam, getAbsoluteRedirectUri(ui));
            ClientAccessToken at = OAuthClientUtils.getAccessToken(accessTokenService, 
                                                                   consumer, 
                                                                   grant);
            ClientTokenContext request = createTokenContext(at);
            MultivaluedMap<String, String> state = null;
            if (clientStateManager != null) {
                state = clientStateManager.fromRedirectState(mc, params);
            }
            ((ClientTokenContextImpl)request).setToken(at);
            ((ClientTokenContextImpl)request).setState(state);
            if (clientTokenContextManager != null) {
                clientTokenContextManager.setClientTokenContext(mc, request);
            }
            setClientCodeRequest(request);
        }
    }
    
    protected ClientTokenContext createTokenContext(ClientAccessToken at) {
        return new ClientTokenContextImpl();
    }
    
    private void setClientCodeRequest(ClientTokenContext request) {
        JAXRSUtils.getCurrentMessage().setContent(ClientTokenContext.class, request);
    }

    private MultivaluedMap<String, String> createRedirectState(ContainerRequestContext rc, SecurityContext sc, 
                                                               UriInfo ui) {
        if (clientStateManager == null) {
            return null;
        }
        return clientStateManager.toRedirectState(mc, toRequestState(rc, ui));
    }

    private MultivaluedMap<String, String> toRequestState(ContainerRequestContext rc, UriInfo ui) {
        MultivaluedMap<String, String> requestState = new MetadataMap<String, String>();
        requestState.putAll(ui.getQueryParameters(false));
        if (MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(rc.getMediaType())) {
            String body = FormUtils.readBody(rc.getEntityStream(), "UTF-8");
            FormUtils.populateMapFromString(requestState, JAXRSUtils.getCurrentMessage(), body, "UTF-8", false);
        }
        return requestState;
    }

    public void setScopeList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(s);
        }
        setScopeString(sb.toString());
    }
    public void setScopeString(String scopesString) {
        this.scopes = scopesString;
    }

    public void setRelativeStartUri(String relStartUri) {
        this.startUri = relStartUri;
    }

    public void setAuthorizationServiceUri(String authorizationServiceUri) {
        this.authorizationServiceUri = authorizationServiceUri;
    }

    public void setRelativeCompleteUri(String completeUri) {
        this.relRedirectUri = completeUri;
    }

    public void setAccessTokenService(WebClient accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    public void setClientCodeStateManager(ClientCodeStateManager manager) {
        this.clientStateManager = manager;
    }
    public void setClientTokenContextManager(ClientTokenContextManager clientTokenContextManager) {
        this.clientTokenContextManager = clientTokenContextManager;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }
    public Consumer getConsumer() {
        return consumer;
    }

}
