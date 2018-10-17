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

import com.codahale.metrics.MetricRegistry;

import org.apache.cxf.metrics.MetricsContext;
import org.apache.cxf.metrics.codahale.CodahaleMetricsContext;
import org.apache.cxf.throttling.ThrottleResponse;

/**
 *
 */
public abstract class Customer {
    protected final String name;
    protected volatile CodahaleMetricsContext metrics;

    public Customer(String n) {
        name = n;
    }

    MetricsContext getMetricsContext(MetricRegistry registry) {
        if (metrics == null) {
            metrics = new CodahaleMetricsContext("demo.server:customer=" + name + ",type=Customer,", registry);
        }
        return metrics;
    }

    public abstract void throttle(ThrottleResponse r);
<<<<<<< HEAD
    
    //Accesso illimitato!
=======


>>>>>>> 3bacad35e53d71c904838e9b825096010e927c37
    public static class PremiumCustomer extends Customer {
        public PremiumCustomer(String n) {
            super(n);
        }
        public void throttle(ThrottleResponse m) {
            System.out.println("");
            //Premium customers are unthrottled
        }
    }
    
    
    //Max  50 req/sec
    public static class PreferredCustomer extends Customer { 
        public PreferredCustomer(String n) {
            super(n);
        }
        public void throttle(ThrottleResponse m) {
            //System.out.println("p  " + metrics.getTotals().getOneMinuteRate()
            // + "  " + metrics.getTotals().getCount());
            //Preferred customers are unthrottled until they hit 100req/sec, then start delaying by 20 msecs [throughput = 50 req/sec]
            //(drops to max of 50req/sec until below the 100req/sec rate)
            if (metrics.getTotals().getOneMinuteRate() > 100) {
                m.setDelay(20);
            }
        }
    }
    
    //Max  4 req/sec
    //Se 5minutes-rate>10 req/sec abbassa rate a 2 req/sec
    public static class RegularCustomer extends Customer {
        public RegularCustomer(String n) {
            super(n);
        }
        public void throttle(ThrottleResponse m) {
<<<<<<< HEAD
            //Regular customers are unthrottled until they hit 25req/sec, then start delaying by 250 ms [throughput = 4 req/sec]
=======
            //Regular customers are unthrottled until they hit 25req/sec, then start delaying by 0.25 seconds
>>>>>>> 3bacad35e53d71c904838e9b825096010e927c37
            //(drops to max of 4req/sec until below the 25req/sec rate)
            if (metrics.getTotals().getOneMinuteRate() > 25) {
                m.setDelay(250);
            }
            //They also get throttled more if they are over 10req/sec over a 5 minute period
            //(drops to max of 2req/sec until below the 10req/sec rate)
            if (metrics.getTotals().getFiveMinuteRate() > 10) {
                m.setDelay(500);
            }
        }
    }
    
    
    //Max 10 req/sec
    //Se 1minutes-rate>5 req/sec abbassa rate a 1 req/sec
    //Se 5minutes-rate>1 req/sec abbassa rate a 0,5 req/sec
    public static class CheapCustomer extends Customer {
        public CheapCustomer(String n) {
            super(n);
        }
        public void throttle(ThrottleResponse m) {
            //System.out.println("ch  " + metrics.getTotals().getOneMinuteRate()
            // + "  " + metrics.getTotals().getCount());
            //Cheap customers are always get a .1 sec delay
            long delay = 100;
            //Then they get futher throttled dependending on rates
            if (metrics.getTotals().getOneMinuteRate() > 5) {
                delay += 1000;
            }
            //They also get throttled after 5 minutes of more than
            if (metrics.getTotals().getFiveMinuteRate() > 1) {
                delay += 1000;
            }
            m.setDelay(delay);
        }
    }

    
    //Max 10 req/sec
    //Dopo 10 req ritorna error 429.
    public static class TrialCustomer extends Customer {
        long lastTime = System.currentTimeMillis();
        public TrialCustomer(String n) {
            super(n);
        }
        public void throttle(ThrottleResponse m) {
            //Trial customers only get 10 requests, then rejected
            if (metrics.getTotals().getCount() >= 10) {
                m.setResponseCode(429, "Exceeded");
            }
            m.addResponseHeader("X-RateLimit-Limit", "10");
            m.addResponseHeader("X-RateLimit-Remaining", Long.toString(10 - metrics.getTotals().getCount()));
            m.setDelay(100);
        }
    }

}
