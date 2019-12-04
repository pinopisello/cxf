package mio.client;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.ObjectFactory;
import org.apache.cxf.ws.eventing.Subscribe;
import org.apache.cxf.ws.eventing.SubscribeResponse;
import org.apache.cxf.ws.eventing.eventsource.EventSourceEndpoint;

public class CreateSubscriptionClient {
    private static String TARGET_ENDPOINT_URL="http://localhost:9000/ws_eventing/services/EventSource";
    private static String SUBSCRIPTION_TARGET_URL="http://localhost:9000/ws_eventing/services/default";
    private static String SUBSCRIPTION_FILTER="//location[text()='Russia']";
    
    
    public static void main(String[] args) throws Exception{
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(EventSourceEndpoint.class);
        factory.setAddress(TARGET_ENDPOINT_URL);
        EventSourceEndpoint requestorClient = (EventSourceEndpoint)factory.create();
        Subscribe sub = createSubscribeMessage(SUBSCRIPTION_TARGET_URL,SUBSCRIPTION_FILTER ,null);

        
        //Invocazione http://127.0.0.1:9000/ws_eventing/services/EventSource.subscribeOp(Subscribe body)
        SubscribeResponse subscribeResponse = requestorClient.subscribeOp(sub);
        System.out.println(subscribeResponse);
    }
    
    
    public static Subscribe createSubscribeMessage(String targetURL, String filter, String expires)
        throws DatatypeConfigurationException {
        Subscribe sub = new Subscribe();


        // expires
        if (expires != null) {
            sub.setExpires(new ExpirationType());
            sub.getExpires().setValue(expires);
        }

        // delivery
        EndpointReferenceType eventSink = new EndpointReferenceType();
        AttributedURIType eventSinkAddr = new AttributedURIType();
        eventSinkAddr.setValue(targetURL);
        eventSink.setAddress(eventSinkAddr);
        sub.setDelivery(new DeliveryType());
        sub.getDelivery().getContent().add(new ObjectFactory().createNotifyTo(eventSink));

        // filter
        if (filter != null && filter.length() > 0) {
            sub.setFilter(new FilterType());
            sub.getFilter().getContent().add(filter);
        }

        return sub;
    }
}
