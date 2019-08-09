/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platforminsightswebhook.events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.cognizant.devops.platforminsightswebhook.config.WebHookConstants;
import com.cognizant.devops.platforminsightswebhook.config.WebHookMessagePublisher;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servlet implementation class to receive webhook data from tool
 */

@WebServlet(urlPatterns = "/insightsDevOpsWebHook/*", loadOnStartup = 1)
public class WebHookHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = LogManager.getLogger(WebHookHandlerServlet.class);
	

	//@Autowired
	WebHookMessagePublisher webhookmessagepublisher = new WebHookMessagePublisher();

	public WebHookHandlerServlet(WebHookMessagePublisher webhookmessagepublisher) {
		this.webhookmessagepublisher = webhookmessagepublisher;
	}
	
	/**
	 * Used to initilize Rabbitq Connection
	 */
	@Override
	public void init() throws ServletException {
		LOG.debug(" In server init .... initilizeMq ");
		webhookmessagepublisher.initilizeMq();
	}

	public WebHookHandlerServlet() {
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOG.debug(" In only doGet not in post  ");
		try {
			processRequest(request);
		} catch (Exception e) {
			LOG.error("Error while adding data in Mq in doget method " + e.getMessage());
		}
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			LOG.debug("In do post ");
			processRequest(request);
		} catch (TimeoutException e) {
			LOG.error(e.getMessage());
		} catch (Exception e) {
			LOG.error("Error while adding data in Mq in doPost method " + e.getMessage());
		}
	}

	/**
	 * used to process request and add data in RabbitMq
	 * 
	 * @param request
	 * @throws IOException
	 * @throws TimeoutException
	 */
	private void processRequest(HttpServletRequest request) throws IOException, TimeoutException {
		JsonObject dataWithReqParam = getBody(request);
		//LOG.debug(" request body in post " + dataWithReqParam);
		if (dataWithReqParam != null) {
			String webHookMqChannelName = WebHookConstants.MQ_CHANNEL_PREFIX
					.concat(dataWithReqParam.get(WebHookConstants.REQUEST_PARAM_KEY_WEBHOOKNAME).getAsString());
			String res = dataWithReqParam.toString();
			LOG.debug(" Final Json after adding parameter " + res);
			webhookmessagepublisher.publishEventAction(res.getBytes(), webHookMqChannelName);
			LOG.debug(" Data successfully published in webhook name as " + webHookMqChannelName);
		} else {
			LOG.debug(" Request body is null ");
		}
	}

	/**
	 * This method used to create json sstring from request body
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public JsonObject getBody(HttpServletRequest request) throws IOException {

		String bodymessage = null;
		StringBuilder stringBuilder = new StringBuilder();
		JsonObject responceJson = null;
		try (BufferedReader reader = request.getReader()) {
			if (reader == null) {
				return null;
			}
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}
		} catch (final Exception e) {
			LOG.error("unable to read data from request body from the http request", e);
			return null;
		}
		if (stringBuilder.length() > 1) {
			bodymessage = stringBuilder.toString();
			JsonElement element = new JsonParser().parse(bodymessage);
			responceJson = element.getAsJsonObject();
			StringBuilder paramDetail = new StringBuilder();
			Enumeration<String> parameterNames = request.getParameterNames();
			while (parameterNames.hasMoreElements()) {
				String paramName = parameterNames.nextElement();
				String[] paramValues = request.getParameterValues(paramName);
				for (int i = 0; i < paramValues.length; i++) {
					String paramValue = paramValues[i];
					paramDetail.append(paramName + ":" + paramValue + ",\n");
					responceJson.addProperty(paramName, paramValue);
				}
			}
		}
		return responceJson;
	}

	/**
	 * Used to release rabbitMq connection
	 */
	@Override
	public void destroy() {
		webhookmessagepublisher.releaseMqConnetion();
	}

}
