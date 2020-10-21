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

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platforminsightswebhook.application.AppProperties;
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
	private static Logger log = LogManager.getLogger(WebHookHandlerServlet.class);

	public WebHookHandlerServlet() {
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.debug(" In only doGet not in post  ");
		try {
			processRequest(request);
		} catch (Exception e) {
			log.error("Error while adding data in Mq in doget method " + e.getMessage());
			setResponseMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} finally {
			try {
				response.getWriter().append("Served at: ").append(request.getContextPath())
						.append(" with Instance Name " + AppProperties.instanceName);
			} catch (Exception e) {
				log.error("Error while appending at response in doGet method: ",e);
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			log.debug("In do post ");
			processRequest(request);
		} catch (TimeoutException e) {
			setResponseMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error("Error while adding data in Mq in doPost method " + e);
			setResponseMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * used to process request and add data in RabbitMq
	 * 
	 * @param request
	 * @throws Exception
	 */
	private void processRequest(HttpServletRequest request) throws Exception {
		JsonObject dataWithReqParam = getBody(request);
		if (dataWithReqParam != null) {
			String webHookMqChannelName = WebHookConstants.MQ_CHANNEL_PREFIX
					.concat(dataWithReqParam.get(WebHookConstants.REQUEST_PARAM_KEY_WEBHOOKNAME).getAsString());
			String res = dataWithReqParam.toString();
			WebHookMessagePublisher.getInstance().publishEventAction(res.getBytes(), webHookMqChannelName);
			log.debug(" Data successfully published in webhook name as ",webHookMqChannelName);
		} else {
			log.debug(" Request body is empty ");
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


		JsonObject responceJson = null;
		try {
			String bodymessage = request.getReader().lines().collect(Collectors.joining());
			if (bodymessage.length() > 1) {
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
				responceJson.addProperty("iswebhookdata", Boolean.TRUE);
			}
		} catch (Exception e) {
			log.error("unable to read data from request body from the http request ==== ", e);
			return null;
		}

		return responceJson;
	}

	/**
	 * Used to release rabbitMq connection
	 */
	@Override
	public void destroy() {
		WebHookMessagePublisher.getInstance().releaseMqConnetion();
	}

	/**
	 * This method is used to send response message
	 * 
	 * @param response
	 * @param statusCode
	 * @param message
	 */
	public static void setResponseMessage(HttpServletResponse response, int statusCode, String message) {
		try {
			response.setStatus(statusCode);
		} catch (Exception e) {
			log.error("Error in setUnauthorizedResponse ", e);
		}
	}

}
