package com.example.customerservice;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 3.1.0-SNAPSHOT-c04c27200226b564108b4fe58b8ac70ca5ec7638
 * 2015-04-14T16:30:14.336-07:00
 * Generated source version: 3.1.0-SNAPSHOT
 * 
 */
@WebService(targetNamespace = "http://customerservice.example.com/", name = "CustomerService")
@XmlSeeAlso({ObjectFactory.class})
public interface CustomerService {

    @WebMethod
    @Oneway
    @RequestWrapper(localName = "updateCustomer", targetNamespace = "http://customerservice.example.com/", className = "com.example.customerservice.UpdateCustomer")
    public void updateCustomer(
        @WebParam(name = "customer", targetNamespace = "")
        com.example.customerservice.Customer customer
    );

    @WebMethod
    @RequestWrapper(localName = "getCustomersByName", targetNamespace = "http://customerservice.example.com/", className = "com.example.customerservice.GetCustomersByName")
    @ResponseWrapper(localName = "getCustomersByNameResponse", targetNamespace = "http://customerservice.example.com/", className = "com.example.customerservice.GetCustomersByNameResponse")
    @WebResult(name = "return", targetNamespace = "")
    public java.util.List<com.example.customerservice.Customer> getCustomersByName(
        @WebParam(name = "name", targetNamespace = "")
        java.lang.String name
    ) throws NoSuchCustomerException;
}
