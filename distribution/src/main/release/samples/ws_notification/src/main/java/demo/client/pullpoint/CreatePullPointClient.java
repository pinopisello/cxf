package demo.client.pullpoint;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.apache.cxf.wsn.client.CreatePullPoint;
import org.apache.cxf.wsn.client.PullPoint;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.oasis_open.docs.wsn.b_2.Notify;

public class CreatePullPointClient {
    private static String CREATE_PULL_POINT_URL="http://127.0.0.1:9000/wsn/CreatePullPoint";


    public static void main(String[] args) throws Exception{ 
        //Creazione pull-point lato server
        CreatePullPoint cpp = new CreatePullPoint(CREATE_PULL_POINT_URL);
        PullPoint createdpp = cpp.create();
        W3CEndpointReference  pper =  createdpp.getEpr(); //Contiene il soap della response incluso l address del nuovo pull point
        System.out.println("Pull point creato:  "+ pper );
  
    }

}
