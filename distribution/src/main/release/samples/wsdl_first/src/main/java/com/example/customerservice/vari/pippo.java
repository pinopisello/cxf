package com.example.customerservice.vari;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.WebApplicationContext;


@Component()
@Scope(value = WebApplicationContext.SCOPE_REQUEST,proxyMode= ScopedProxyMode.TARGET_CLASS)
public class pippo {


    public pippo(){
        System.out.println("pippo creato!!!");
    }
    
    
    public void chiamapippo(){
        System.out.println("chiamapippo!!!");
    }
}
