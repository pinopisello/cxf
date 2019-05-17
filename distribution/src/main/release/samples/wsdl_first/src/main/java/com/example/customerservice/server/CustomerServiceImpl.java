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
package com.example.customerservice.server;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import com.example.customerservice.Customer;
import com.example.customerservice.CustomerService;
import com.example.customerservice.CustomerType;
import com.example.customerservice.NoSuchCustomer;
import com.example.customerservice.NoSuchCustomerException;
import com.example.customerservice.vari.pippo;

import org.apache.cxf.message.Message;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component("CustomerServiceImplementor")
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode= ScopedProxyMode.TARGET_CLASS)
public class CustomerServiceImpl implements CustomerService {
    /**
     * The WebServiceContext can be used to retrieve special attributes like the
     * user principal. Normally it is not needed
     */
    //@Inject
   // WebServiceContext wsContext;

      
    //@Inject
    pippo pippo;
    

    public CustomerServiceImpl(){
        System.out.println("CustomerServiceImpl creato!!!");
    }

    

    @WebMethod(exclude=false)
    public List<Customer> getCustomersByName(String name) throws NoSuchCustomerException {
        if(pippo!=null)pippo.chiamapippo();
        if ("None".equals(name)) {
            NoSuchCustomer noSuchCustomer = new NoSuchCustomer();
            noSuchCustomer.setCustomerName(name);
            throw new NoSuchCustomerException("Did not find any matching customer for name=" + name,
                                              noSuchCustomer);
        }

        List<Customer> customers = new ArrayList<>();
        for (int c = 0; c < 2; c++) {
            Customer cust = new Customer();
            cust.setName(name);
            cust.getAddress().add("Pine Street 200");
            Date bDate = new GregorianCalendar(2009, 01, 01).getTime();
            cust.setBirthDate(bDate);
            cust.setNumOrders(1);
            cust.setRevenue(10000);
            cust.setTest(new BigDecimal(1.5));
            cust.setType(CustomerType.BUSINESS);
            customers.add(cust);
        }

        return customers;
    }

    
    @WebMethod(exclude=false)
    public void  updateCustomer(Customer customer) {
        
       // MessageContext ctx = wsContext.getMessageContext();
       // QName operation = (QName) ctx.get(Message.WSDL_OPERATION);
        //System.out.println("The operation name is " + operation);
        
        if(pippo!=null)pippo.chiamapippo();
        //System.out.println("update request was received");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Nothing to do here
        }
        System.out.println("Customer was updated");
        
    }

}
