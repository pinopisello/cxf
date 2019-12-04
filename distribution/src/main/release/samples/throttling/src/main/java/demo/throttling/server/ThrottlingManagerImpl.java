package demo.throttling.server;

import java.util.Collections;
import java.util.List;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.throttling.ThrottleResponse;
import org.apache.cxf.throttling.ThrottlingManager;

public class ThrottlingManagerImpl implements ThrottlingManager {
    @Override
    public ThrottleResponse getThrottleResponse(String phase, Message m) {
        ThrottleResponse r = new ThrottleResponse();
        if (m.get("THROTTLED") != null) {
            return null;
        }
        m.put("THROTTLED", true);
        Customer c = m.getExchange().get(Customer.class);
        c.throttle(r);
        return r;
    }

    @Override
    public List<String> getDecisionPhases() {
        return Collections.singletonList(Phase.PRE_STREAM);
    }
}
