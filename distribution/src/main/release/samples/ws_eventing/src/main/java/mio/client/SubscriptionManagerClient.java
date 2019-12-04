package mio.client;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.GetStatus;
import org.apache.cxf.ws.eventing.GetStatusResponse;
import org.apache.cxf.ws.eventing.Renew;
import org.apache.cxf.ws.eventing.RenewResponse;
import org.apache.cxf.ws.eventing.Unsubscribe;
import org.apache.cxf.ws.eventing.UnsubscribeResponse;
import org.apache.cxf.ws.eventing.manager.SubscriptionManagerEndpoint;

public class SubscriptionManagerClient {
    private static String TARGET_ENDPOINT_URL="http://127.0.0.1:9000/ws_eventing/services/SubscriptionManager";
    private static SubscriptionManagerEndpoint requestorClient;
    private static String TARGET_SUBSCRIPTION_UUID="c2a114ec-84b7-413d-aa6e-252ac3ffca88";
    
    
    
    public static void main(String[] args) throws Exception{
        ClientProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(org.apache.cxf.ws.eventing.manager.SubscriptionManagerEndpoint.class);
        factory.setAddress(TARGET_ENDPOINT_URL);
        requestorClient = (SubscriptionManagerEndpoint)factory.create();
      
        //getStatus();
        
        //renewOp();
 
         unsubscribeOp();
        
        
    
    }
    
    
    
    
    private static void getStatus() throws Exception{
        Client proxy = ClientProxy.getClient(requestorClient);
        proxy.getRequestContext().put(Header.HEADER_LIST, getUUIDSoapHeader());
        GetStatus body = new GetStatus();
        GetStatusResponse response = requestorClient.getStatusOp(body);
        System.out.println(response);
    }
    
    
    private static void renewOp() throws Exception{
        Client proxy = ClientProxy.getClient(requestorClient);
        proxy.getRequestContext().put(Header.HEADER_LIST, getUUIDSoapHeader());
        Renew renew=new Renew();
        ExpirationType newExpire = new ExpirationType();
        newExpire.setValue("2017-06-26T12:23:12.000-01:00");
        
        renew.setExpires(newExpire);
        RenewResponse response = requestorClient.renewOp(renew);
        System.out.println(response);
    }
    
    private static void unsubscribeOp() throws Exception{
        Client proxy = ClientProxy.getClient(requestorClient);
        proxy.getRequestContext().put(Header.HEADER_LIST, getUUIDSoapHeader());
        Unsubscribe unsub =new Unsubscribe();
        UnsubscribeResponse response = requestorClient.unsubscribeOp(unsub);
        System.out.println(response);
    }
    
    private static List<Header> getUUIDSoapHeader() throws Exception{
        List<Header> headersList = new ArrayList<Header>();
        //Setta quale subscription si vuole verificare lo status.
        Header UUIDSoapHeader = new Header(new QName("http://cxf.apache.org/ws-eventing", "SubscriptionID"), TARGET_SUBSCRIPTION_UUID, new JAXBDataBinding(String.class));
        headersList.add(UUIDSoapHeader);
        return headersList;
    }
}
