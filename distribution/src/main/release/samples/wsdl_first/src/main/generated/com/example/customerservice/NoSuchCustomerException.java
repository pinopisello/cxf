
package com.example.customerservice;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 3.3.0-SNAPSHOT-0ffdc66b52bb4f1eadd9abb060afa36d7ac32907
 * 2018-10-18T19:45:18.126-07:00
 * Generated source version: 3.3.0-SNAPSHOT
 */

@WebFault(name = "NoSuchCustomer", targetNamespace = "http://customerservice.example.com/")
public class NoSuchCustomerException extends Exception {
    public static final long serialVersionUID = 1L;

    private com.example.customerservice.NoSuchCustomer noSuchCustomer;

    public NoSuchCustomerException() {
        super();
    }

    public NoSuchCustomerException(String message) {
        super(message);
    }

    public NoSuchCustomerException(String message, java.lang.Throwable cause) {
        super(message, cause);
    }

    public NoSuchCustomerException(String message, com.example.customerservice.NoSuchCustomer noSuchCustomer) {
        super(message);
        this.noSuchCustomer = noSuchCustomer;
    }

    public NoSuchCustomerException(String message, com.example.customerservice.NoSuchCustomer noSuchCustomer, java.lang.Throwable cause) {
        super(message, cause);
        this.noSuchCustomer = noSuchCustomer;
    }

    public com.example.customerservice.NoSuchCustomer getFaultInfo() {
        return this.noSuchCustomer;
    }
}
