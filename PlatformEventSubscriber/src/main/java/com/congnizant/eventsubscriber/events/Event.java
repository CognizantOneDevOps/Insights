package com.congnizant.eventsubscriber.events;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


/**
 * Servlet implementation class GitEvent
 */
//@WebServlet("/webhookEvent")
@WebServlet(urlPatterns = "/webhookEvent/*", loadOnStartup = 1)
public class Event extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Long allrequestTime = 0L;
	private ConnectionFactory factory;
	private Channel channel;
	private Connection connection;
	private String routingKey = "WEBHOOK_EVENTDATA";
	private String exchangeName = "iSight";
	@Autowired
	private Environment env;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Event() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @Override
	public void init() throws ServletException {
		try {
			initilizeMq();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println(" In only doGet not post  Git Git Git ");
		
		try {
			
			long millis = System.currentTimeMillis();
			String res = getBody(request);
			System.out.println("Current time in millis: after getBody  "+(System.currentTimeMillis()-millis));
			millis = System.currentTimeMillis();
			//System.out.println(res);
			//System.out.println(request.getContentType());
			
			System.out.print(" before publish " + (System.currentTimeMillis() - millis));

			publishEventAction(res.getBytes());
			
			long requestTime = (System.currentTimeMillis() - millis);
			allrequestTime = allrequestTime + requestTime;
			System.out.println("Current time in millis: " + requestTime + "  allrequestTime  " + allrequestTime);

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
			//System.out.println("In do post ");
			Enumeration<String> parameterNames = request.getParameterNames();

			while (parameterNames.hasMoreElements()) {

				String paramName = parameterNames.nextElement();
				//System.out.println(" paramName " + paramName);

				String[] paramValues = request.getParameterValues(paramName);
				for (int i = 0; i < paramValues.length; i++) {
					String paramValue = paramValues[i];
					//System.out.println(" paramValues " + paramValue);
				}

			}

		long millis = System.currentTimeMillis();
		String res = getBody(request);
			//System.out.print("Current time in millis: after getBody  "+(System.currentTimeMillis()-millis));
		millis = System.currentTimeMillis();
		//System.out.println(res);
		//System.out.println(request.getContentType());
		
			System.out.print(" before publish " + (System.currentTimeMillis() - millis));

		publishEventAction(res.getBytes());
		
			long requestTime = (System.currentTimeMillis() - millis);
			allrequestTime = allrequestTime + requestTime;
			System.out.println(" Current time in millis: " + requestTime + "  allrequestTime  " + allrequestTime);
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
	
	/*
	 * public void pushDataToRabbitMq(String res) { ConnectionFactory factory = new
	 * ConnectionFactory(); factory.setHost("localhost"); try (Connection connection
	 * = factory.newConnection(); Channel channel = connection.createChannel()) {
	 * channel.queueDeclare(QUEUE_NAME, false, false, false, null); String message =
	 * "Hello World!"; channel.basicPublish("", QUEUE_NAME, null,
	 * message.getBytes("UTF-8")); System.out.println(" [x] Sent '" + message +
	 * "'"); } }
	 */
	
	
	private void publishEventAction(byte[] data)
			throws TimeoutException, IOException {
		
		if (channel != null) {
			channel.basicPublish(exchangeName, routingKey, null, data);
		}


	}

	private void initilizeMq() throws TimeoutException {


		try {
			//String hostPath = env.getProperty("mq.host");
			//System.out.println("  hostPath  " + hostPath);
			factory = new ConnectionFactory();
			factory.setHost("localhost");
			factory.setUsername("iSight");
			factory.setPassword("iSight");
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(exchangeName, "topic", true);
			channel.queueDeclare(routingKey, true, false, false, null);
			channel.queueBind(routingKey, exchangeName, routingKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void destroy() {

		try {
			if (channel != null && connection != null) {
				channel.close();
				connection.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
