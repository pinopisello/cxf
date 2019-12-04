/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package demo.throttling.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codahale.metrics.MetricRegistry;

import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.metrics.ExchangeMetrics;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 *
 */
public class CustomerMetricsInterceptor extends AbstractPhaseInterceptor<Message> {
    MetricRegistry registry;
    static Map<String, Customer> customers = new HashMap<>();
    
    static {
        customers.put("Tom", new Customer.PremiumCustomer("Tom"));
        customers.put("Rob", new Customer.PreferredCustomer("Rob"));
        customers.put("Vince", new Customer.RegularCustomer("Vince"));
        customers.put("Malcolm", new Customer.CheapCustomer("Malcolm"));
        customers.put("Jonas", new Customer.TrialCustomer("Jonas"));
    }
    
    public CustomerMetricsInterceptor(MetricRegistry reg) {
        super(Phase.PRE_STREAM);
        registry = reg;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        ExchangeMetrics exchangeMetrix = message.getExchange().get(ExchangeMetrics.class);
        if (exchangeMetrix != null) {
            Map<String, List<String>> h = CastUtils.cast((Map<?, ?>)message.get(Message.PROTOCOL_HEADERS));
            String auth = h.get("Authorization").toString();  //1)leggo valore "Authorization" header dalla request
            auth = auth.substring(auth.indexOf(' ') + 1);
            try {
                auth = new String(Base64Utility.decode(auth));
            } catch (Base64Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            auth = auth.substring(0, auth.indexOf(':'));
            Customer c = customers.get(auth);                  //2) seleziona l'istanza Customer nella Map customers con nome uguale a 
                                                               //   quello step 1.Se non la trova RuntimeException e stop!
            if (c == null) {
                throw new RuntimeException("Not authorized");
            }
            exchangeMetrix.addContext(c.getMetricsContext(registry));       // 3) crea un CodahaleMetricsContext con 5 timers, un counter e due meters
                                                               //    con nomi del tipo "demo.server:customer=Tom,type=Customer,"
            message.getExchange().put(Customer.class, c);      // 4) metto l'istanza Customer matchata in message.exchange
        }
    }

}
