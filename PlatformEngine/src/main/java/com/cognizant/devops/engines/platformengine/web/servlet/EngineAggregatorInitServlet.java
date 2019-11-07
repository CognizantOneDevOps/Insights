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
/*package com.cognizant.devops.platformengine.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformengine.message.factory.MessagePublisherFactory;
import com.cognizant.devops.platformengine.modules.aggregator.EngineAggregatorModule;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EngineAggregatorInitServlet extends HttpServlet{
	private static Logger log = LogManager.getLogger(EngineAggregatorInitServlet.class.getName());
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String routingKey = null;
		JsonObject configJson = null;
		Scanner s;
		try {
			s = new Scanner(req.getInputStream(), "UTF-8").useDelimiter("\\A");
			String jsonStr =  s.hasNext() ? s.next() : "";
			JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
			configJson = json.get("data").getAsJsonObject();
			routingKey = configJson.get("subscribe").getAsJsonObject().get("config").getAsString();
		} catch (Exception e1) {
			log.error(e1);
		}
		try {
			MessagePublisherFactory.publish(routingKey, configJson);
		} catch (Exception e) {
			log.error(e);
		}
		new EngineAggregatorModule().registerAggregators();
		String msg = "{ \"status\" : \"success\"}";
		PrintWriter writer = resp.getWriter();
		writer.write(msg);
		writer.flush();
		writer.close();
	}
	
}
*/
