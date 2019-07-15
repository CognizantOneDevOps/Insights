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

import com.cognizant.devops.platforminsightswebhook.config.WebHookMessagePublisher;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servlet implementation class GitEvent
 */

@WebServlet(urlPatterns = "/webhookEvent/*", loadOnStartup = 1)
public class WebHookHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Long allrequestTime = 0L;
	private static Logger LOG = LogManager.getLogger(WebHookHandlerServlet.class);

	//@Autowired
	WebHookMessagePublisher webhookmessagepublisher = new WebHookMessagePublisher();// 

	@Override
	public void init() throws ServletException {
		try {
			LOG.debug(" In server init .... initilizeMq ");
			webhookmessagepublisher.initilizeMq();
		} catch (Exception e) {
			LOG.error("Error while initilize mq " + e.getMessage());
			LOG.error(e.getMessage());
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOG.debug(" In only doGet not post Git ");
		try {

			long millis = System.currentTimeMillis();
			String res = getBody(request).toString();
			LOG.debug("Current time in millis: after getBody  " + (System.currentTimeMillis() - millis));
			millis = System.currentTimeMillis();
			LOG.debug(" before publish " + (System.currentTimeMillis() - millis));

			webhookmessagepublisher.publishEventAction(res.getBytes());

			long requestTime = (System.currentTimeMillis() - millis);
			allrequestTime = allrequestTime + requestTime;
			LOG.debug("Current time in millis: " + requestTime + "  allrequestTime  " + allrequestTime);
		} catch (Exception e) {
			LOG.error("Error while adding data in Mq in doget method ");
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

			long millis = System.currentTimeMillis();
			String res = getBody(request).toString();
			LOG.debug("Current time in millis: after getBody  " + (System.currentTimeMillis() - millis));
			millis = System.currentTimeMillis();
			//LOG.debug(res);
			//LOG.debug(request.getContentType());
			LOG.debug(" before publish " + (System.currentTimeMillis() - millis));
			webhookmessagepublisher.publishEventAction(res.getBytes());//webHookMessagePublisher
			long requestTime = (System.currentTimeMillis() - millis);
			allrequestTime = allrequestTime + requestTime;
			LOG.debug(" Current time in millis: " + requestTime + "  allrequestTime  " + allrequestTime);
		} catch (TimeoutException e) {
			LOG.error(e.getMessage());
		}
	}

	public JsonObject getBody(HttpServletRequest request) throws IOException {

		String bodymessage = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
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
			LOG.error("Could not obtain the saml request body from the http request", e);
			return null;
		}

		bodymessage = stringBuilder.toString();
		LOG.debug("bodymessage" + bodymessage);
		JsonElement element = new JsonParser().parse(bodymessage);
		LOG.debug("  element  " + element);
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
		LOG.debug("Request Parameter  " + paramDetail.toString());

		return responceJson;
	}

	@Override
	public void destroy() {
		webhookmessagepublisher.releaseMqConnetion();
	}

}
