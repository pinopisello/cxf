
/*
 * 
 */

package demo.order;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.2.2
 * Mon Oct 19 14:25:48 IST 2009
 * Generated source version: 2.2.2
 * 
 */


@WebServiceClient(name = "OrderProcessService", 
                  wsdlLocation = "file:OrderProcess.wsdl",
                  targetNamespace = "http://order.demo/") 
public class OrderProcessService extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://order.demo/", "OrderProcessService");
    public final static QName OrderProcessPort = new QName("http://order.demo/", "OrderProcessPort");
    static {
        URL url = null;
        try {
            url = new URL("file:OrderProcess.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:OrderProcess.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public OrderProcessService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public OrderProcessService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public OrderProcessService() {
        super(WSDL_LOCATION, SERVICE);
    }

    /**
     * 
     * @return
     *     returns OrderProcess
     */
    @WebEndpoint(name = "OrderProcessPort")
    public OrderProcess getOrderProcessPort() {
        return super.getPort(OrderProcessPort, OrderProcess.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OrderProcess
     */
    @WebEndpoint(name = "OrderProcessPort")
    public OrderProcess getOrderProcessPort(WebServiceFeature... features) {
        return super.getPort(OrderProcessPort, OrderProcess.class, features);
    }

}
