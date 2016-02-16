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
import java.util.Map;

import javax.xml.ws.Endpoint;

import com.codahale.metrics.MetricRegistry;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.metrics.MetricsFeature;
import org.apache.cxf.metrics.codahale.CodahaleMetricsProvider;
import org.apache.cxf.throttling.ThrottlingFeature;
import org.apache.cxf.throttling.ThrottlingManager;

public class Server {

    
    protected Server() throws Exception {
        System.out.println("Starting Server");


        //==========   JMX   ================
        //abilito jmx [InstrumentationManagerImpl come una bus extension] e pubblia mbean per il bus e uno per l.endpoint SoapPort
        Map<String, Object> properties = new HashMap<>();
        properties.put("bus.jmx.usePlatformMBeanServer", Boolean.TRUE);
        properties.put("bus.jmx.enabled", Boolean.TRUE);
        Bus b = new CXFBusFactory().createBus(null, properties);
        
        
        
        //============== cxf-rt-features-metrics : Endpoint  metrics  =====================
        //MetricRegistry contiene tutti le metriche che misurano l'applicazione [ Meters,Timers,Counters,Gauges,...]
        //Contiene metriche Customer.metrics e sendpoint.metrics
        MetricRegistry registry = new MetricRegistry();          //metrics-core.jar  https://dropwizard.github.io/metrics/3.1.0/
        
        
        //CodahaleMetricsProvider setta un JmxReporter che pubblic le metriche su jmx 
        CodahaleMetricsProvider.setupJMXReporter(b, registry);   //cxf-rt-features-metrics  [dipende da metrics-core.jar]
        b.setExtension(registry, MetricRegistry.class);          //Setta MetricRegistry come bus extension.    
        
        
        //============ cxf-rt-features-throttling  ====================
        //ThrottlingManager e' una feature che si registra nella Phase.PRE_STREAM 
        //Legge il Message m in arrivo ed estrae Customer.class da m.getExchange().get(Customer.class)
        //E' l'istanza Customer che, in base al tipo [RegularCustomer,PreferredCustomer,PremiumCustomer] ritorna 
        //una ThrottleResponse che influenza come/quando la response viene ritornata al client.
        ThrottlingManager manager = new ThrottlingManagerImpl();  //cxf-rt-features-throttling
            
        //Aggiungo CustomerMetricsInterceptor al bus che eegue in PRE_STREAM
        b.getInInterceptors().add(new CustomerMetricsInterceptor(registry));
        
        
        
        
        Object implementor = new GreeterImpl();
        String address = "http://localhost:9002/SoapContext/SoapPort";
        Endpoint.publish(address, implementor, 
                         new MetricsFeature(),
                         new ThrottlingFeature(manager));  //cxf-rt-features-throttling
    }

    public static void main(String args[]) throws Exception {
        new Server();
        System.out.println("Server ready...");

        Thread.sleep(5 * 60 * 100000);
        System.out.println("Server exiting");
        System.exit(0);
    }
}
