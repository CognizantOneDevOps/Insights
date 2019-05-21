package com.cognizant.devops.platformauditing.hyperledger.accesslayer;

import com.cognizant.devops.platformauditing.commons.ChainCodeMethods;
import com.cognizant.devops.platformauditing.hyperledger.user.UserUtil;
import com.cognizant.devops.platformauditing.util.LoadFile;
import com.cognizant.devops.platformauditing.hyperledger.client.CAClient;
import com.cognizant.devops.platformauditing.hyperledger.events.BlockEventCapture;
import com.cognizant.devops.platformauditing.hyperledger.events.ChaincodeEventCapture;
import com.cognizant.devops.platformauditing.hyperledger.user.UserContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.cognizant.devops.platformauditing.hyperledger.events.ChaincodeEventCapture.setChaincodeEventListener;
import static com.cognizant.devops.platformauditing.hyperledger.events.ChaincodeEventCapture.waitForChaincodeEvent;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BCNetworkClient {

    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "InstantiateSuccess";
    private static final Logger LOG = LogManager.getLogger(BCNetworkClient.class);
    private static HFClient client = null;
    private static Channel channel = null;
    private static UserContext userContext = null;
    private static JsonObject Config;
    private static volatile BCNetworkClient bcNetworkClient;

    static {
        try {
            Config = LoadFile.getConfig();
            bcNetworkClient = new BCNetworkClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BCNetworkClient() throws Exception {
        UserUtil.cleanUp();
        userContext = new CAClient(Config.get("CA_ORG1_URL").getAsString(), null).getUserContext(Config.get("USERCERT_NAME").getAsString());
        LOG.info(Config.get("USERCERT_NAME").getAsString());

        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // setup the client
        client = HFClient.createNewInstance();
        client.setCryptoSuite(cryptoSuite);
        // set user context
        client.setUserContext(userContext);
        // get HFC channel using the client
        channel = createChannel();
        channel.initialize();
        LOG.info("Channel: " + channel.getName());
    }

    public static BCNetworkClient getInstance() {
        return bcNetworkClient;
    }

    /**
     * Query Blockchain for a date range
     * <p>
     * <p>
     * The HF Client
     *
     * @throws ProposalException
     * @throws InvalidArgumentException
     * @throws TransactionException
     */
    public String getAllAssetsByDates(String[] queryArgs)
            throws ProposalException, InvalidArgumentException {
        return queryBlockChain(ChainCodeMethods.GETASSETSBYDATE, queryArgs);
    }

    /**
     * Query Blockchain for an asset Id
     * <p>
     * <p>
     * The HF Client
     *
     * @throws ProposalException
     * @throws InvalidArgumentException
     * @throws TransactionException
     */
    public String getAssetDetails(String[] queryArgs)
            throws ProposalException, InvalidArgumentException {
        return queryBlockChain(ChainCodeMethods.GETASSETDETAILS, queryArgs);
    }

    public String getAssetHistory(String[] queryArgs)
            throws ProposalException, InvalidArgumentException {
        return queryBlockChain(ChainCodeMethods.GETASSETHISTORY, queryArgs);
    }

    public JsonObject createBCNode(String[] functionArgs)
            throws Exception {
        Vector<ChaincodeEventCapture> chaincodeEvents = new Vector<>(); // list to capture chaincode events
        String chaincodeEventListenerHandle = setChaincodeEventListener(channel, EXPECTED_EVENT_NAME, chaincodeEvents);

        JsonObject response = sendTransaction(ChainCodeMethods.INSTANTIATE, functionArgs);

        boolean eventDone = waitForChaincodeEvent(150, channel, chaincodeEvents, chaincodeEventListenerHandle);
        LOG.info("eventDone: " + eventDone);
        return response;
    }

    /**
     * Query a transaction by id.
     *
     * @param txnId
     * @return
     * @throws ProposalException
     * @throws InvalidArgumentException
     */
    //future scope
    public TransactionInfo queryByTransactionId(String txnId) throws ProposalException, InvalidArgumentException {
        LOG.info("Querying by trasaction id " + txnId + " on channel " + channel.getName());
        Collection<Peer> peers = channel.getPeers();
        for (Peer peer : peers) {
            TransactionInfo info = channel.queryTransactionByID(peer, txnId);
            LOG.info("TransactionInfo :***********");
            LOG.info(info);
            LOG.info("TRANSACTION ENVELOPE");
            LOG.info(info.getEnvelope());
            LOG.info("PROCESSED TRANSACTION");
            LOG.info(info.getProcessedTransaction());
            return info;
        }
        return null;
    }


    private JsonObject sendTransaction(String functionName, String[] functionArgs)
            throws InvalidArgumentException, ProposalException {
        TransactionProposalRequest tpr = getTransactionProposalRequest(functionName, functionArgs);
        Collection<ProposalResponse> responses = channel.sendTransactionProposal(tpr);
        Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(responses);
        if (proposalConsistencySets.size() != 1) {
            LOG.error("Expected only one set of consistent proposal responses but got more");
        }

        JsonObject result = new JsonObject();
        for (ProposalResponse pres : responses) {
            String stringResponse = new String(pres.getChaincodeActionResponsePayload());
            JsonParser parser = new JsonParser();
            result = (JsonObject) parser.parse(stringResponse);
            if (!result.getAsJsonPrimitive("statusCode").getAsString().equals("201")) {
                LOG.error(result.getAsJsonPrimitive("msg"));
                //throw new RuntimeException(result.getAsJsonPrimitive("msg").getAsString());
            }
        }
        CompletableFuture<TransactionEvent> transactionResponse = channel.sendTransaction(responses);
        return result;
    }

    /**
     * Invoke blockchain query
     * <p>
     * <p>
     * The HF Client
     *
     * @throws ProposalException
     * @throws InvalidArgumentException
     */
    private String queryBlockChain(String functionName, String[] queryArgs)
            throws ProposalException, InvalidArgumentException {

        QueryByChaincodeRequest request = getQueryByChaincodeRequest(functionName, queryArgs);

        Collection<ProposalResponse> res = channel.queryByChaincode(request);

        // display response
        for (ProposalResponse pres : res) {
            if (pres.getChaincodeActionResponsePayload().length != 2) {
                String stringResponse = new String(pres.getChaincodeActionResponsePayload());
                LOG.debug(stringResponse);
                return stringResponse;
            }
        }

        return null;
    }

    /**
     * Initialize and get HF channel
     * <p>
     * <p>
     * The HFC client
     *
     * @return Initialized channel
     * @throws InvalidArgumentException
     * @throws TransactionException
     */
    private Channel createChannel() throws InvalidArgumentException, TransactionException {
        // initialize channel
        // peer name and endpoint in fabcar network
        Peer peer = client.newPeer(Config.get("ORG1_PEER_0").getAsString(), Config.get("ORG1_PEER_0_URL").getAsString());
        // eventhub name and endpoint in fabcar network
        EventHub eventHub = client.newEventHub(Config.get("EVENTHUB_NAME").getAsString(), Config.get("EVENTHUB_URL").getAsString());
        // orderer name and endpoint in fabcar network
        Orderer orderer = client.newOrderer(Config.get("ORDERER_NAME").getAsString(), Config.get("ORDERER_URL").getAsString());
        // channel name in fabcar network
        channel = client.newChannel(Config.get("CHANNEL_NAME").getAsString());
        channel.addPeer(peer);
        channel.addEventHub(eventHub);
        channel.addOrderer(orderer);
        BlockEventCapture blockEvents = new BlockEventCapture();
        channel.registerBlockListener(blockEvents);
        return channel;
    }

    /**
     * Initialize and get Query Request object
     */

    private QueryByChaincodeRequest getQueryByChaincodeRequest(String functionName, String[] funcArgs) {
        QueryByChaincodeRequest request = client.newQueryProposalRequest();
        ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.get("CHAINCODE_1_NAME").getAsString()).build();
        request.setChaincodeID(ccid);
        request.setChaincodeLanguage(TransactionRequest.Type.NODE);
        request.setChaincodeVersion(Config.get("CHAINCODE_1_VERSION").getAsString());
        request.setChaincodeName(Config.get("CHAINCODE_1_NAME").getAsString());
        request.setChaincodePath(Config.get("CHAINCODE_1_PATH").getAsString());

        request.setFcn(functionName);
        request.setArgs(funcArgs);

        return request;
    }


    /**
     * Initialize and get Query Request object
     *
     * @throws InvalidArgumentException
     */

    private TransactionProposalRequest getTransactionProposalRequest(String functionName, String[] funcArgs) throws InvalidArgumentException {
        TransactionProposalRequest request = TransactionProposalRequest.newInstance(userContext);
        ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.get("CHAINCODE_1_NAME").getAsString()).build();
        request.setChaincodeLanguage(TransactionRequest.Type.NODE);
        request.setChaincodeID(ccid);
        request.setChaincodeVersion(Config.get("CHAINCODE_1_VERSION").getAsString());
        request.setChaincodeName(Config.get("CHAINCODE_1_NAME").getAsString());
        request.setChaincodePath(Config.get("CHAINCODE_1_PATH").getAsString());
        request.setFcn(functionName);
        request.setArgs(funcArgs);
        request.setProposalWaitTime(1000);
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        tm2.put("result", ":)".getBytes(UTF_8));
        tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);
        request.setTransientMap(tm2);

        return request;
    }


}
