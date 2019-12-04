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

package demo.jaxrs.client;

import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

import org.apache.cxf.common.util.Base64Utility;

public final class jax_rs_spring_securityClient {

    private jax_rs_spring_securityClient() {
    }

    public static void main(String args[]) throws Exception {
        System.out.println("\n");
        jax_rs_spring_securityClient c = new jax_rs_spring_securityClient();
      
        // 1. get customer info  
        // 
        c.getCustomerInfo("fred", "fred", 123);             //fallisce per pwd sbagliata 401
        c.getCustomerInfoSecured("fred", "fred", 123);
        
        c.getCustomerInfo("fred", "fredspassword", 123);    //successo perche pwd giusta e fred ha role ROLE_CUSTOMER
        c.getCustomerInfoSecured("fred", "fredspassword", 123); 
        c.getCustomerInfo("bob", "bobspassword", 123);      //successo perche pwd giusta e bob ha role ROLE_CUSTOMER
        c.getCustomerInfoSecured("bob", "bobspassword", 123); 
        // 2. add customer info  
        // 
        c.addCustomerInfo("fred", "fredspassword");         //successo perche pwd giusta e fred ha role ROLE_ADMIN
        c.addCustomerInfoSecured("fred", "fredspassword");  
        c.addCustomerInfo("bob", "bobspassword");           //fallisce per bob NON ha role ROLE_ADMIN 403
        c.addCustomerInfoSecured("bob", "bobspassword");
        // 3. update customer info  
        // 
        c.updateCustomerInfo("fred", "fredspassword");      //successo perche pwd giusta e fred ha role ROLE_ADMIN
        c.updateCustomerInfoSecured("fred", "fredspassword");  
        c.updateCustomerInfo("bob", "bobspassword");        //fallisce per bob NON ha role ROLE_ADMIN 403
        c.updateCustomerInfoSecured("bob", "bobspassword");
        // 4. delete customer info  
        // 
        c.deleteCustomerInfo("bob", "bobspassword", 123);     //fallisce per bob NON ha role ROLE_ADMIN 403
        c.deleteCustomerInfoSecured("bob", "bobspassword", 123); 
        c.deleteCustomerInfo("fred", "fredspassword", 123);   //successo perche pwd giusta e fred ha role ROLE_ADMIN
        c.deleteCustomerInfoSecured("fred", "fredspassword", 123); 

    }

    public void getCustomerInfo(String name, String password, int id) throws Exception {
               
        System.out.println("HTTP GET to query customer info, user : " 
            + name + ", password : " + password);
        GetMethod get = 
            new GetMethod("http://127.0.0.1:9002/jax_rs_spring_security/cxf-servlet/jaxrs/customerservice/customers/" + id);
        setMethodHeaders(get, name, password);
        handleHttpMethod(get);
    } 
    
    public void getCustomerInfoSecured(String name, String password, int id) throws Exception {
        
        System.out.println("HTTP GET to query customer info, user : " 
            + name + ", password : " + password);
        GetMethod get = 
            new GetMethod("http://127.0.0.1:9002/jax_rs_spring_security/cxf-servlet/jaxrs/customerservice-secured/customers/" + id);
        setMethodHeaders(get, name, password);
        handleHttpMethod(get);
    } 
    

    public void addCustomerInfo(String name, String password) throws Exception {
               
        System.out.println("HTTP POST to add customer info, user : " 
            + name + ", password : " + password);
        PostMethod post = new PostMethod("http://127.0.0.1:9002/jax_rs_spring_security/cxf-servlet/jaxrs/customerservice/customers");
        setMethodHeaders(post, name, password);
        RequestEntity entity = new InputStreamRequestEntity(
            this.getClass().getClassLoader().getResourceAsStream("add_customer.xml"));
        post.setRequestEntity(entity);
        
        handleHttpMethod(post);
    } 

    public void addCustomerInfoSecured(String name, String password) throws Exception {
        
        System.out.println("HTTP POST to add customer info, user : " 
            + name + ", password : " + password);
        PostMethod post = new PostMethod("http://127.0.0.1:9002/jax_rs_spring_security/cxf-servlet/jaxrs/customerservice-secured/customers");
        setMethodHeaders(post, name, password);
        RequestEntity entity = new InputStreamRequestEntity(
            this.getClass().getClassLoader().getResourceAsStream("add_customer.xml"));
        post.setRequestEntity(entity);
        
        handleHttpMethod(post);
    } 
    
    public void updateCustomerInfo(String name, String password) throws Exception {
               
        System.out.println("HTTP PUT to update customer info, user : " 
            + name + ", password : " + password);
        PutMethod put = new PutMethod("http://127.0.0.1:9002/jax_rs_spring_security/cxf-servlet/jaxrs/customerservice/customers/123");
        setMethodHeaders(put, name, password);
        RequestEntity entity = new InputStreamRequestEntity(
            this.getClass().getClassLoader().getResourceAsStream("update_customer.xml"));
        put.setRequestEntity(entity);
        
        handleHttpMethod(put);
    } 
    
    public void updateCustomerInfoSecured(String name, String password) throws Exception {
        
        System.out.println("HTTP PUT to update customer info, user : " 
            + name + ", password : " + password);
        PutMethod put = new PutMethod("http://127.0.0.1:9002/jax_rs_spring_security/cxf-servlet/jaxrs/customerservice-secured/customers/123");
        setMethodHeaders(put, name, password);
        RequestEntity entity = new InputStreamRequestEntity(
            this.getClass().getClassLoader().getResourceAsStream("update_customer.xml"));
        put.setRequestEntity(entity);
        
        handleHttpMethod(put);
    } 
    public void deleteCustomerInfo(String name, String password, int id) throws Exception {
               
       // System.out.println("HTTP DELETE to update customer info, user : " 
       //     + name + ", password : " + password);
       // System.out.println("Confirming a customer with id " + id + " exists first");
        ///getCustomerInfo(name, password, id);
        System.out.println("Deleting now...");
        DeleteMethod del = 
            new DeleteMethod("http://127.0.0.1:9002/jax_rs_spring_security/cxf-servlet/jaxrs/customerservice/customers/" + id);
        setMethodHeaders(del, name, password);
        handleHttpMethod(del);
       // System.out.println("Confirming a customer with id " + id + " does not exist anymore");
       // getCustomerInfo(name, password, id);
    }  
    public void deleteCustomerInfoSecured(String name, String password, int id) throws Exception {
        
        //System.out.println("HTTP DELETE to update customer info, user : " 
        //    + name + ", password : " + password);
        //System.out.println("Confirming a customer with id " + id + " exists first");
        //getCustomerInfo(name, password, id);
        //System.out.println("Deleting now...");
        DeleteMethod del = 
            new DeleteMethod("http://127.0.0.1:9002/jax_rs_spring_security/cxf-servlet/jaxrs/customerservice-secured/customers/" + id);
        setMethodHeaders(del, name, password);
        handleHttpMethod(del);
       // System.out.println("Confirming a customer with id " + id + " does not exist anymore");
       // getCustomerInfo(name, password, id);
    }  
    private static void handleHttpMethod(HttpMethod httpMethod) throws Exception {
        HttpClient client = new HttpClient();

        try {
            int statusCode = client.executeMethod(httpMethod);
            System.out.println("Response status : " + statusCode); 

            Response.Status status =  Response.Status.fromStatusCode(statusCode);

            if (status == Response.Status.OK) {
                System.out.println(httpMethod.getResponseBodyAsString());    
            } else if (status == Response.Status.FORBIDDEN) {
                System.out.println("Authorization failure");
            } else if (status == Response.Status.UNAUTHORIZED) {
                System.out.println("Authentication failure");
            }
            System.out.println(); 

        } finally {
            // release any connection resources used by the method
            httpMethod.releaseConnection();
        }  
    }

    private static void setMethodHeaders(HttpMethod httpMethod, String name, String password) {
        if (httpMethod instanceof PostMethod || httpMethod instanceof PutMethod) {
            httpMethod.setRequestHeader("Content-Type", "application/xml");
        }         
        httpMethod.setDoAuthentication(false);
       // httpMethod.setRequestHeader("Accept", "application/xml");
       // if(httpMethod.getPath().contains("-secured"))
        //    httpMethod.setRequestHeader("Accept", "application/json");
       // else
       //     httpMethod.setRequestHeader("Accept", "application/xml");
        httpMethod.setRequestHeader("Authorization", 
                             "Basic " + base64Encode(name + ":" + password));
          
    }

    private static String base64Encode(String value) {
        return Base64Utility.encode(value.getBytes());
    }
}
