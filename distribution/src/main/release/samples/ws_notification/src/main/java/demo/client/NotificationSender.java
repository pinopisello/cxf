package demo.client;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.cxf.wsn.client.NotificationBroker;

public class NotificationSender {

    public static void main(String[] args) {
        NotificationBroker notificationBroker  = new NotificationBroker("http://localhost:9000/wsn/NotificationBroker");
        // Send a notification on the Topic
        notificationBroker.notify("MyTopic", 
                                  new JAXBElement<String>(new QName("urn:test:org", "foo"),
                                          String.class, "Hello World!"));
       
        
    }

}
