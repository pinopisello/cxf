package demo.client.pullpoint;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;

public class InvokePullPoint {
   private static String TARGET_PULL_POINT_ID ="ID1270011530a1d096210";
    
    
    public static void main(String[] args) throws Exception {
        //Creazione PP
        org.apache.cxf.wsn.client.PullPoint pullpoint = new org.apache.cxf.wsn.client.PullPoint("http://127.0.0.1:9000/wsn/pullpoints/"+TARGET_PULL_POINT_ID);
        
        
        //Invio notifica
        Notify notifica = new Notify();
        List<NotificationMessageHolderType> messaggi_notifica =notifica.getNotificationMessage();
        NotificationMessageHolderType messaggio_holder = new NotificationMessageHolderType();
        Message messaggio_contenuto = new Message();
        messaggio_contenuto.setAny(new JAXBElement<String>(new QName("urn:test:org", "foo"), String.class, "Notifica"));
        messaggio_holder.setMessage(messaggio_contenuto);
        messaggi_notifica.add(messaggio_holder);
        pullpoint.getPullPoint().notify(notifica);
        
        
        //Ricezione notifica
        List<NotificationMessageHolderType> messagess = pullpoint.getMessages(10);
        for(NotificationMessageHolderType currMess: messagess){
          System.out.println(currMess.getMessage().getAny());  
       }
        
        //Distruzione pull point
        pullpoint.destroy();
       
    }

}
