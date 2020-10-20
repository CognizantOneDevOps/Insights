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
package com.cognizant.devops.platformauditing.hyperledger.accesslayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.hyperledger.fabric.gateway.Contract;

import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

import org.hyperledger.fabric.gateway.impl.GatewayImpl;

import java.security.InvalidKeyException;
import java.security.PrivateKey;

import java.security.cert.CertificateException;

import com.cognizant.devops.platformauditing.commons.ChainCodeMethods;
import com.cognizant.devops.platformauditing.util.LoadFile;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BCNetworkGatewayClient {

	public Contract gatewayContract() throws IOException, CertificateException, InvalidKeyException {

		JsonObject Config = LoadFile.getConfig();
		// peer0.org1 user, signcert and keystore
		final String certUser = Config.get("peer0_org1_user").getAsString();
		// fetch peer0.org1 org
		final JsonElement orgElement = Config.get("organizations");
		final JsonElement org = orgElement.getAsJsonObject().get("Org1");
		// fetch peer0.org1 signedCertPEM
		final JsonElement signedCertPEM = org.getAsJsonObject().get("signedCertPEM");
		final String signedCertPEMPath = signedCertPEM.getAsJsonObject().get("path").getAsString();
		// fetch peer0.org1 adminPrivateKeyPEM
		final JsonElement adminPrivateKeyPEM = org.getAsJsonObject().get("adminPrivateKeyPEM");
		final String adminPrivateKeyPEMPath = adminPrivateKeyPEM.getAsJsonObject().get("path").getAsString();
		// fetch peer0.org1 org msp
		final String orgmsp = org.getAsJsonObject().get("mspid").getAsString();
		// fetch peer0.org1 org signed cert and keystore
		final String x509CertificatePem = readLineByLine(signedCertPEMPath);
		final String pkcs8PrivateKeyPem = readLineByLine(adminPrivateKeyPEMPath);
		// fetch channel name
		final JsonElement channelElement = Config.get("channels");
		final JsonElement channelname = channelElement.getAsJsonObject().get("channelname");
		final String channel = channelname.getAsJsonObject().get("channel").getAsString();
		// fetch chaincode
		final String contractName = Config.get("contractname").getAsString();

		GatewayImpl.Builder builder = (GatewayImpl.Builder) Gateway.createBuilder();
		Wallet wallet = Wallets.newInMemoryWallet();
		java.security.cert.X509Certificate certificate = Identities.readX509Certificate(x509CertificatePem);
		PrivateKey privateKey = Identities.readPrivateKey(pkcs8PrivateKeyPem);
		wallet.put(certUser, Identities.newX509Identity(orgmsp, certificate, privateKey));

		// Load an existing wallet holding identities used to access the network.
		// Path walletDirectory = Paths.get("wallet");
		// wallet = Wallets.newFileSystemWallet(walletDirectory);
		// Path to a common connection profile describing the network.
		Path networkConfigFile = Paths.get(ConfigOptions.BLOCKCHAIN_CONFIG_FILE_RESOLVED_PATH);

		// Configure the gateway connection used to access the network.
		builder.identity(wallet, certUser).networkConfig(networkConfigFile);
		// Create a gateway connection
		Gateway gateway = builder.connect();

		// Obtain a smart contract deployed on the network.
		Network network = gateway.getNetwork(channel);
		Contract contract = network.getContract(contractName);

		return contract;
	}

	// Read signcert and keystore file content. Read file content into the string
	// with - Files.lines(Path path, Charset cs)
	private static String readLineByLine(String filePath) {
		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}

	/**
	 * Invoke blockchain query
	 * 
	 * @throws Exception
	 */
	private String queryBlockChain(String functionName, String[] queryArgs) throws Exception {
		BCNetworkGatewayClient gatewayObject = new BCNetworkGatewayClient();
		byte[] queryChainCodeResult = gatewayObject.gatewayContract().evaluateTransaction(functionName, queryArgs);
		String stringResponse = new String(queryChainCodeResult, StandardCharsets.UTF_8);
		return stringResponse;

	}

	/**
	 * Query Blockchain for a date range
	 * 
	 * @throws Exception
	 */
	public String getAllAssetsByDates(String[] queryArgs) throws Exception {
		return queryBlockChain(ChainCodeMethods.GETASSETSBYDATE, queryArgs);
	}

	/**
	 * Query Blockchain for an asset Id
	 * 
	 * @throws Exception
	 */
	public String getAssetDetails(String[] queryArgs) throws Exception {
		return queryBlockChain(ChainCodeMethods.GETASSETDETAILS, queryArgs);
	}

	/**
	 * Query Blockchain for an asset history
	 * 
	 * @throws Exception
	 */
	public String getAssetHistory(String[] queryArgs) throws Exception {
		return queryBlockChain(ChainCodeMethods.GETASSETHISTORY, queryArgs);
	}

	/**
	 * Invoke Blockchain for creating a record
	 */
	public JsonObject createBCNode(String[] functionArgs) throws Exception {
		BCNetworkGatewayClient gatewayObject = new BCNetworkGatewayClient();
		byte[] createChainCodeResult = gatewayObject.gatewayContract().createTransaction(ChainCodeMethods.INSTANTIATE)
				.submit(functionArgs);
		JsonObject response = new JsonParser().parse(new String(createChainCodeResult, StandardCharsets.UTF_8))
				.getAsJsonObject();
		return response;
	}
}