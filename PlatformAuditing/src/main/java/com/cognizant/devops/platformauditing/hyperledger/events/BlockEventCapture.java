/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformauditing.hyperledger.events;

import org.apache.logging.log4j.LogManager;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockListener;

import java.sql.Timestamp;

public class BlockEventCapture implements BlockListener {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(BlockEventCapture.class);

    @Override
    public void received(BlockEvent blockEvent) {

        try {
            long blockNum = blockEvent.getBlockNumber();

            LOG.info("RECEIVED BLOCK : " + blockNum);
            Iterable<BlockEvent.TransactionEvent> transactionEvents = blockEvent.getTransactionEvents();
            // Iterate over the many transactions in the block
            for (BlockEvent.TransactionEvent transactionEvent : transactionEvents) {
                LOG.info("Tx ID: " + transactionEvent.getTransactionID());
                // Validation Code is 0 for valid transactions
                LOG.info("Tx Validation Code: " + transactionEvent.getValidationCode());
                LOG.info("Tx isValid: " + transactionEvent.isValid());
                LOG.info("Peer: " + transactionEvent.getPeer());
                Timestamp txTimestamp = new Timestamp(transactionEvent.getTimestamp().getTime());
                LOG.info("Tx Timestamp: " + txTimestamp);
                //for future scope if required for retrying invalid transactions
				/*BCNetworkClient bcNetworkClient = BCNetworkClient.getInstance();
				TransactionInfo info = bcNetworkClient.queryByTransactionId(transactionEvent.getTransactionID());
				Logger.getLogger(BlockEventCapture.class.getName()).log(Level.INFO, "Transaction details: " + info);*/

                if (!transactionEvent.isValid())
                    throw new Exception("Invalid transaction event captured:: Tranasaction ID: " + transactionEvent.getTransactionID() + "\n Block data: " + blockEvent.getBlock().getData());

            }

            LOG.info("END of BLOCK" + blockNum);
        } catch (Exception e) {
            LOG.warn("ERROR:" + e);

        }
    }
}
