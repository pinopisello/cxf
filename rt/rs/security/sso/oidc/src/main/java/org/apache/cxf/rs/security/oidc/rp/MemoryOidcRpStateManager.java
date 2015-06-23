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
package org.apache.cxf.rs.security.oidc.rp;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.MultivaluedMap;


public class MemoryOidcRpStateManager implements OidcRpStateManager {
    private ConcurrentHashMap<String, MultivaluedMap<String, String>> map = 
        new ConcurrentHashMap<String, MultivaluedMap<String, String>>();
    private ConcurrentHashMap<String, OidcClientTokenContext> map2 = 
        new ConcurrentHashMap<String, OidcClientTokenContext>();
    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRequestState(String token, MultivaluedMap<String, String> state) {
        map.put(token, state);
    }

    @Override
    public MultivaluedMap<String, String> removeRequestState(String token) {
        return map.remove(token);
    }

    @Override
    public void setTokenContext(String contextKey, OidcClientTokenContext state) {
        map2.put(contextKey, state);
        
    }

    @Override
    public OidcClientTokenContext getTokenContext(String contextKey) {
        return map2.get(contextKey);
    }

    @Override
    public OidcClientTokenContext removeTokenContext(String contextKey) {
        return map2.remove(contextKey);
    }
}
