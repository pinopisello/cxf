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



package demo.hw.server;

import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.apache.cxf.annotations.UseAsyncMethod;
import org.apache.cxf.jaxws.ServerAsyncResponse;
import org.apache.hello_world_async_soap_http.GreeterAsync;
import org.apache.hello_world_async_soap_http.types.GreetMeSometimeResponse;

@WebService(serviceName = "SOAPService", 
            portName = "SoapPort", 
            endpointInterface = "org.apache.hello_world_async_soap_http.GreeterAsync",
            targetNamespace = "http://apache.org/hello_world_async_soap_http")
                  
public class GreeterImpl implements GreeterAsync {
    private static final Logger LOG =
        Logger.getLogger(GreeterImpl.class.getPackage().getName());
 
    /* (non-Javadoc)
     * @see org.apache.hello_world_soap_http.Greeter#greetMeSometime(java.lang.String)
     */
    //@UseAsyncMethod
    public String greetMeSometime(String me) {
        LOG.info("Executing operation greetMeSometime synchronously");
        System.out.println("Executing operation greetMeSometime synchronously\n");
        try{
            Thread.sleep(10000);
        }catch(Exception e){}
        return "How are you " + me;
    }
    
    /**
     *   Invocazione asincrona tipo "Callback" .
     *   Server invoca AsyncHandler quando ha fatto e il client a quel punto riceve risposta.
     */
    public Future<?> greetMeSometimeAsync(final String me,   final AsyncHandler<GreetMeSometimeResponse> asyncHandler) {
        LOG.info("Executing operation greetMeSometimeAsync asynchronously");
        System.out.println("Executing operation greetMeSometimeAsync asynchronously\n");
        final ServerAsyncResponse<GreetMeSometimeResponse> r   = new ServerAsyncResponse<GreetMeSometimeResponse>();

        new Thread() {
            public void run() {
                GreetMeSometimeResponse resp = new GreetMeSometimeResponse();
                resp.setResponseType("How are you " + me);
                r.set(resp);
                System.out.println("Responding on background thread\n");
                try{
                    Thread.sleep(15000);
                }catch(Exception e){}
                asyncHandler.handleResponse(r);
            }
        } .start();
        
        return r; 
    }
    
    
    /**
     *   Invocazione asincrona tipo "Polling" Response.isDone() ...
     */
    

    public Response<GreetMeSometimeResponse> greetMeSometimeAsync(String requestType) {
        try{
            Thread.sleep(15000);
        }catch(Exception e){}
        return null; 
        /*not called */
    }

    
}
