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
import com.cognizant.devops.platforminsightswebhook.config.WebHookMessagePublisher;


/**
 * Servlet implementation class GitEvent
 */

@WebServlet(urlPatterns = "/webhookEvent/*", loadOnStartup = 1)
public class WebHookHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Long allrequestTime = 0L;
	private static Logger LOG = LogManager.getLogger(WebHookHandlerServlet.class);

	//@Autowired
	WebHookMessagePublisher webhookmessagepublisher = new WebHookMessagePublisher();
    
    @Override
	public void init() throws ServletException {
		try {
			LOG.debug(" In server init .... initilizeMq ");
			webhookmessagepublisher.initilizeMq();
		} catch (Exception e) {
			LOG.error("Error while initilize mq " + e.getMessage());
			e.printStackTrace();
		}
    }


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		LOG.debug(" In only doGet not post Git ");

		try {
			
			long millis = System.currentTimeMillis();
			String res = getBody(request);
			LOG.debug("Current time in millis: after getBody  " + (System.currentTimeMillis() - millis));
			millis = System.currentTimeMillis();
			//LOG.debug(res);
			//LOG.debug(request.getContentType());
			
			System.out.print(" before publish " + (System.currentTimeMillis() - millis));

			webhookmessagepublisher.publishEventAction(res.getBytes());
			
			long requestTime = (System.currentTimeMillis() - millis);
			allrequestTime = allrequestTime + requestTime;
			LOG.debug("Current time in millis: " + requestTime + "  allrequestTime  " + allrequestTime);

			//doGet(request, response);
			
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			//LOG.debug("In do post ");
			Enumeration<String> parameterNames = request.getParameterNames();

			while (parameterNames.hasMoreElements()) {

				String paramName = parameterNames.nextElement();
				//LOG.debug(" paramName " + paramName);

				String[] paramValues = request.getParameterValues(paramName);
				for (int i = 0; i < paramValues.length; i++) {
					String paramValue = paramValues[i];
					//LOG.debug(" paramValues " + paramValue);
				}

			}

		long millis = System.currentTimeMillis();
		String res = getBody(request);
			//System.out.print("Current time in millis: after getBody  "+(System.currentTimeMillis()-millis));
		millis = System.currentTimeMillis();
			//LOG.debug(res);
			//LOG.debug(request.getContentType());
		
			System.out.print(" before publish " + (System.currentTimeMillis() - millis));

			webhookmessagepublisher.publishEventAction(res.getBytes());//webHookMessagePublisher
		
			long requestTime = (System.currentTimeMillis() - millis);
			allrequestTime = allrequestTime + requestTime;
			LOG.debug(" Current time in millis: " + requestTime + "  allrequestTime  " + allrequestTime);
		//doGet(request, response);
		
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getBody(HttpServletRequest request) throws IOException {

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;
	    
	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
		} catch (Exception ex) {
			ex.printStackTrace();
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	   return body;
	}
	
	@Override
	public void destroy() {
		webhookmessagepublisher.releaseMqConnetion();
	}

}
