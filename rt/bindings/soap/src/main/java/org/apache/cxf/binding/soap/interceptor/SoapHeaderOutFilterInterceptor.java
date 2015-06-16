

package org.apache.cxf.binding.soap.interceptor;

import java.util.Iterator;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

public class SoapHeaderOutFilterInterceptor extends AbstractSoapInterceptor {
    public static final SoapHeaderOutFilterInterceptor INSTANCE = new SoapHeaderOutFilterInterceptor();
    
    public SoapHeaderOutFilterInterceptor()  {
        super(Phase.PRE_LOGICAL);
    }

    public void handleMessage(SoapMessage message) throws Fault {
        Iterator<Header> iter =  message.getHeaders().iterator();
        
        while (iter.hasNext()) {
            Header hdr  = iter.next();
            //Only remove inbound marked headers..
            if (hdr == null || hdr.getDirection() == Header.Direction.DIRECTION_IN) {
                iter.remove(); 
            }
        }
    }

}
