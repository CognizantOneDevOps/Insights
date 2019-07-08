package com.cognizant.devops.platformauditing.hyperledger.events;

import org.apache.logging.log4j.LogManager;
import org.hyperledger.fabric.sdk.ChaincodeEvent;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Vector;
import java.util.regex.Pattern;

public class ChaincodeEventCapture {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(ChaincodeEventCapture.class);
    private final String handle;
    private final ChaincodeEvent chaincodeEvent;

    public ChaincodeEventCapture(String handle, ChaincodeEvent chaincodeEvent) {
        this.handle = handle;
        this.chaincodeEvent = chaincodeEvent;
    }

    public static String setChaincodeEventListener(Channel channel,
                                                   String expectedEventName, Vector<ChaincodeEventCapture> chaincodeEvents)
            throws InvalidArgumentException {

        ChaincodeEventListener chaincodeEventListener = (handle, blockEvent, chaincodeEvent) -> {
            chaincodeEvents.add(new ChaincodeEventCapture(handle, chaincodeEvent));

            String eventHub = blockEvent.getPeer().getName();
            if (eventHub != null) {
                eventHub = blockEvent.getPeer().getName();
            } else {
                eventHub = channel.getEventHubs().toString();
            }
            // Here put what you want to do when receive chaincode event
            LOG.info("RECEIVED CHAINCODE EVENT with handle: " + handle + ", chaincodeId: " + chaincodeEvent.getChaincodeId() + ", chaincode event name: " + chaincodeEvent.getEventName() + ", transactionId: " + chaincodeEvent.getTxId() + ", event Payload: " + new String(chaincodeEvent.getPayload()) + ", from eventHub: " + eventHub);

        };
        // chaincode events.
        String eventListenerHandle = channel.registerChaincodeEventListener(Pattern.compile(".*"),
                Pattern.compile(Pattern.quote(expectedEventName)), chaincodeEventListener);
        return eventListenerHandle;
    }

    public static boolean waitForChaincodeEvent(Integer timeout, Channel channel,
                                                Vector<ChaincodeEventCapture> chaincodeEvents, String chaincodeEventListenerHandle)
            throws InvalidArgumentException {
        boolean eventDone = false;
        if (chaincodeEventListenerHandle != null) {


            int numberEventsExpected = channel.getEventHubs().size() + channel
                    .getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)).size();
            LOG.info("numberOfEventsExpected: " + numberEventsExpected);
            //just make sure we get the notifications
            if (timeout.equals(0)) {
                // get event without timer
                while (chaincodeEvents.size() != numberEventsExpected) {
                    // do nothing
                }
                eventDone = true;
            } else {
                // get event with timer
                for (int i = 0; i < timeout; i++) {
                    if (chaincodeEvents.size() == numberEventsExpected) {
                        eventDone = true;
                        break;
                    } else {
                        try {
                            double j = i;
                            j = j / 10;
                            //Logger.getLogger(ChaincodeEventCapture.class.getName()).log(Level.INFO, (j + " second"));
                            Thread.sleep(100); // wait for the events for one tenth of second.
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            LOG.info("chaincodeEvents.size(): " + chaincodeEvents.size());

            // unregister event listener
            channel.unregisterChaincodeEventListener(chaincodeEventListenerHandle);
            int i = 1;
            // arrived event handling
            for (ChaincodeEventCapture chaincodeEventCapture : chaincodeEvents) {
                LOG.info("Event number. " + i);
                LOG.info("event capture object: " + chaincodeEventCapture.toString());
                LOG.info("Event Handle: " + chaincodeEventCapture.getHandle());
                LOG.info("Event TxId: " + chaincodeEventCapture.getChaincodeEvent().getTxId());
                LOG.info("Event Name: " + chaincodeEventCapture.getChaincodeEvent().getEventName());
                LOG.info("Event Payload: " + Arrays.toString(chaincodeEventCapture.getChaincodeEvent().getPayload())); // byte
                LOG.info("Event ChaincodeId: " + chaincodeEventCapture.getChaincodeEvent()
                        .getChaincodeId());

                i++;
            }

        } else {
            LOG.info("chaincodeEvents.isEmpty(): " + chaincodeEvents.isEmpty());
        }
        LOG.info("eventDone: " + eventDone);
        return eventDone;
    }

    /**
     * @return the handle
     */
    private String getHandle() {
        return handle;
    }

    /**
     * @return the chaincodeEvent
     */
    private ChaincodeEvent getChaincodeEvent() {
        return chaincodeEvent;
    }


}
