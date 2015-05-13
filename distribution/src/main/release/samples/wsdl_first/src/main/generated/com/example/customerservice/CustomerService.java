package com.example.customerservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 3.1.1-SNAPSHOT-a3258e1853ebfe564b23a7fc1bf4ce5ca0c02eae
 * 2015-05-12T00:12:02.968-07:00
 * Generated source version: 3.1.1-SNAPSHOT
 * 
 */
@WebService(targetNamespace = "http://customerservice.example.com/", name = "CustomerService")
@XmlSeeAlso({ObjectFactory.class})
public interface CustomerService {

    @WebMethod
    @RequestWrapper(localName = "updateCustomer", targetNamespace = "http://customerservice.example.com/", className = "com.example.customerservice.UpdateCustomer")
    @ResponseWrapper(localName = "updateCustomerResponse", targetNamespace = "http://customerservice.example.com/", className = "com.example.customerservice.UpdateCustomerResponse")
    @WebResult(name = "name", targetNamespace = "")
    public java.lang.String updateCustomer(
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
