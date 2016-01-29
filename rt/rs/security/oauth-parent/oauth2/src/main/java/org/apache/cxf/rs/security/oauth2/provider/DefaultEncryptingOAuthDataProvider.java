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
package org.apache.cxf.rs.security.oauth2.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import org.apache.cxf.rs.security.oauth2.common.Client;
import org.apache.cxf.rs.security.oauth2.common.ServerAccessToken;
import org.apache.cxf.rs.security.oauth2.common.UserSubject;
import org.apache.cxf.rs.security.oauth2.tokens.refresh.RefreshToken;
import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;
import org.apache.cxf.rs.security.oauth2.utils.crypto.ModelEncryptionSupport;
import org.apache.cxf.rt.security.crypto.CryptoUtils;
import org.apache.cxf.rt.security.crypto.KeyProperties;

public class DefaultEncryptingOAuthDataProvider extends AbstractOAuthDataProvider {
    protected SecretKey key;
    private Set<String> tokens = Collections.synchronizedSet(new HashSet<String>());
    private ConcurrentHashMap<String, String> refreshTokens = new ConcurrentHashMap<String, String>();
    private ConcurrentHashMap<String, String> clientsMap = new ConcurrentHashMap<String, String>();
    public DefaultEncryptingOAuthDataProvider(String algo, int keySize) {
        this(new KeyProperties(algo, keySize));
    }
    public DefaultEncryptingOAuthDataProvider(KeyProperties props) {
        this(CryptoUtils.getSecretKey(props));
    }
    public DefaultEncryptingOAuthDataProvider(SecretKey key) {
        this.key = key;
    }
    
    @Override
    public Client getClient(String clientId) throws OAuthServiceException {
        return ModelEncryptionSupport.decryptClient(clientsMap.get(clientId), key);
    }

    @Override
    public void setClient(Client client) {
        clientsMap.put(client.getClientId(), ModelEncryptionSupport.encryptClient(client, key));
        
    }
    @Override
    public Client removeClient(String clientId) {
        Client client = getClient(clientId);
        clientsMap.remove(clientId);
        removeClientTokens(client);
        return client;
    }
    @Override
    public List<Client> getClients(UserSubject resourceOwner) {
        List<Client> clients = new ArrayList<Client>(clientsMap.size());
        for (String clientKey : clientsMap.keySet()) {
            Client c = getClient(clientKey);
            if (resourceOwner == null 
                || c.getResourceOwnerSubject() != null 
                   && c.getResourceOwnerSubject().getLogin().equals(resourceOwner.getLogin())) {
                clients.add(c);
            }
        }
        return clients;
    }
    @Override
    public List<ServerAccessToken> getAccessTokens(Client c, UserSubject sub) {
        List<ServerAccessToken> list = new ArrayList<ServerAccessToken>(tokens.size());
        for (String tokenKey : tokens) {
            ServerAccessToken token = getAccessToken(tokenKey);
            if (isTokenMatched(token, c, sub)) {
                list.add(token);
            }
        }
        return list;
    }
    @Override
    public List<RefreshToken> getRefreshTokens(Client c, UserSubject sub) {
        List<RefreshToken> list = new ArrayList<RefreshToken>(refreshTokens.size());
        for (String tokenKey : tokens) {
            RefreshToken token = getRefreshToken(tokenKey);
            if (isTokenMatched(token, c, sub)) {
                list.add(token);
            }
        }
        return list;
    }
    
    protected static boolean isTokenMatched(ServerAccessToken token, Client c, UserSubject sub) {
        if (c == null || token.getClient().getClientId().equals(c.getClientId())) {
            UserSubject tokenSub = token.getSubject();
            if (sub == null || tokenSub != null && tokenSub.getLogin().equals(sub.getLogin())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public ServerAccessToken getAccessToken(String accessToken) throws OAuthServiceException {
        try {
            return ModelEncryptionSupport.decryptAccessToken(this, accessToken, key);
        } catch (SecurityException ex) {
            throw new OAuthServiceException(OAuthConstants.ACCESS_DENIED, ex);
        }
    }

    @Override
    protected void saveAccessToken(ServerAccessToken serverToken) {
        encryptAccessToken(serverToken);
    }

    @Override
    protected ServerAccessToken revokeAccessToken(String accessTokenKey) {
        ServerAccessToken at = getAccessToken(accessTokenKey);
        tokens.remove(accessTokenKey);
        return at;
    }
    
    @Override
    protected void saveRefreshToken(ServerAccessToken at, RefreshToken refreshToken) {
        String encryptedRefreshToken = ModelEncryptionSupport.encryptRefreshToken(refreshToken, key);
        at.setRefreshToken(encryptedRefreshToken);
    }

    @Override
    protected RefreshToken revokeRefreshToken(String refreshTokenKey) {
        RefreshToken rt = null;
        if (refreshTokens.containsKey(refreshTokenKey)) {
            rt = getRefreshToken(refreshTokenKey);
            refreshTokens.remove(refreshTokenKey);
        }
        return rt;
        
    }

    private void encryptAccessToken(ServerAccessToken token) {
        String encryptedToken = ModelEncryptionSupport.encryptAccessToken(token, key);
        tokens.add(encryptedToken);
        refreshTokens.put(token.getRefreshToken(), encryptedToken);
        token.setTokenKey(encryptedToken);
    }
    @Override
    protected RefreshToken getRefreshToken(String refreshTokenKey) {
        try {
            return ModelEncryptionSupport.decryptRefreshToken(this, refreshTokenKey, key);
        } catch (SecurityException ex) {
            throw new OAuthServiceException(OAuthConstants.ACCESS_DENIED, ex);
        }
    }
    
}
