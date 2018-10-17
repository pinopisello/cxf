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

package demo.ws_rm.common;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.io.AbstractWrappedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.rm.RMContextUtils;
import org.apache.cxf.ws.rm.RMProperties;
import org.apache.cxf.ws.rm.v200702.SequenceType;

/**
<<<<<<< HEAD:distribution/src/main/release/samples/ws_rm/src/main/java/demo/ws_rm/common/MessageLossInterceptor.java
 * Rimuove, previene l'invio messaggi con sequence pari lato client.
 * Ergo il client li inviera'2 volte non ricevendo ack per il primo.
=======
 *
>>>>>>> 3bacad35e53d71c904838e9b825096010e927c37:distribution/src/main/release/samples/ws_rm/src/main/java/demo/ws_rm/common/MessageLossSimulator.java
 */
public class MessageLossInterceptor extends AbstractPhaseInterceptor<Message> {

<<<<<<< HEAD:distribution/src/main/release/samples/ws_rm/src/main/java/demo/ws_rm/common/MessageLossInterceptor.java
    private static final Logger LOG = Logger.getLogger(MessageLossInterceptor.class.getName());
    private int appMessageCount; 
    private long lastrun=System.currentTimeMillis();
    
    
    public MessageLossInterceptor() {
=======
    private static final Logger LOG = Logger.getLogger(MessageLossSimulator.class.getName());
    private int appMessageCount;

    public MessageLossSimulator() {
>>>>>>> 3bacad35e53d71c904838e9b825096010e927c37:distribution/src/main/release/samples/ws_rm/src/main/java/demo/ws_rm/common/MessageLossSimulator.java
        super(Phase.PREPARE_SEND);
        addBefore(MessageSenderInterceptor.class.getName());
    }

    // CHECKSTYLE:OFF: ReturnCount 
    public void handleMessage(Message message) throws Fault {
        System.out.println("Last run:  "+(System.currentTimeMillis() - lastrun) );
        AddressingProperties maps =    RMContextUtils.retrieveMAPs(message, false, true);
        
        RMProperties rmps = RMContextUtils.retrieveRMProperties(message, true);
        if(null !=rmps){
            SequenceType st = rmps.getSequence();
            Long messgaeNumber = st.getMessageNumber();
        }
        //Fixed the build error of ws_rm, now there is no ensureExposedVersion anymore
        //RMContextUtils.ensureExposedVersion(maps);
        String action = null;
        if (maps != null && null != maps.getAction()) {
            action = maps.getAction().getValue();
        }
        if (RMContextUtils.isRMProtocolMessage(action)) {
            return;
        }
        appMessageCount++;
        // do not discard odd-numbered messages
        if (0 != (appMessageCount % 2)) {
            return;
        }


        // discard even-numbered message
        InterceptorChain chain = message.getInterceptorChain();
        ListIterator<Interceptor<? extends Message>> it = chain.getIterator();
        while (it.hasNext()) {
            PhaseInterceptor<? extends Message> pi = (PhaseInterceptor<? extends Message>)it.next();
            if (MessageSenderInterceptor.class.getName().equals(pi.getId())) {
                chain.remove(pi);
                LOG.info("Removed MessageSenderInterceptor from interceptor chain.");
                break;
            }
        }

        message.setContent(OutputStream.class, new WrappedOutputStream(message));

        message.getInterceptorChain().add(new AbstractPhaseInterceptor<Message>(Phase.PREPARE_SEND_ENDING) {

            public void handleMessage(Message message) throws Fault {
                try {
                    message.getContent(OutputStream.class).close();
                } catch (IOException e) {
                    throw new Fault(e);
                }
            }

        });
    }
    // CHECKSTYLE:ON: ReturnCount 

    private class WrappedOutputStream extends AbstractWrappedOutputStream {

        private Message outMessage;

        WrappedOutputStream(Message m) {
            this.outMessage = m;
        }

        @Override
        protected void onFirstWrite() throws IOException {
            if (LOG.isLoggable(Level.FINE)) {
                Long nr = RMContextUtils.retrieveRMProperties(outMessage, true)
                    .getSequence().getMessageNumber();
                LOG.fine("Losing message " + nr);
            }
            wrappedStream = new DummyOutputStream();
        }
    }



    private class DummyOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            // TODO Auto-generated method stub

        }

    }



}
