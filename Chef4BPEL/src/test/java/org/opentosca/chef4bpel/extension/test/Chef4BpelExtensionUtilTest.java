package org.opentosca.chef4bpel.extension.test;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.opentosca.bpel4restlight.rest.HighLevelRestApi;
import org.opentosca.bpel4restlight.rest.HttpResponseMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Chef4BpelExtensionUtilTest {
	
	@Test
	public void testTransformToJson() {
		
		Document testDoc = null;
		try {
			testDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader("<chef4bpel:Chef4BPELScript address=\"localhost\" dependencies=\"{ 'mysql': '>= 0.0.0' }\" SSHPrivateKey=\"someKey\" SSHUser=\"ubuntu\" xmlns:chef4bpel=\"http://www.opentosca.org/bpel/chef4bpel\" access=\"ssh\" SSHPort=\"22\"><![CDATA[include_recipe \"mysql::server\"\n]]></chef4bpel:Chef4BPELScript>"));
			testDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			
		} catch (ParserConfigurationException e) {
			fail();
			e.printStackTrace();
		} catch (SAXException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		assertNotNull(testDoc);
		
		// Element chef4bpelScriptTestElement =
		// testDoc.createElementNS("http://www.opentosca.org/bpel/chef4bpel",
		// "Chef4BPELScript");
		// assertNotNull(chef4bpelScriptTestElement);
		//
		// chef4bpelScriptTestElement.setAttribute("address", "localhost");
		// chef4bpelScriptTestElement.setAttribute("SSHUser", "ubuntu");
		
		JSONObject jsonObj = org.opentosca.chef4bpel.extension.Chef4BpelExtensionUtil.transformToJson(testDoc.getDocumentElement());
		assertNotNull(testDoc);
		
		assertTrue(jsonObj.has("parameters"));
		assertTrue(jsonObj.has("executable"));
		
		Thread testThread = new Thread(new TestServer());
		testThread.start();
		
		// api/v1/invokers/chef-cookbooks/runs
		HttpResponseMessage responseMessage = HighLevelRestApi.Post("http://localhost:8666/", jsonObj.toString(4), "application/json");
		
		assertNotNull(responseMessage);
	}
	
	
	public class TestServer implements Runnable {
		
		private Server server = new Server(8666);
		
		
		public TestServer() {
			server.setHandler(new TestHandler());
		}
		
		public void run() {
			try {
				server.start();
				server.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public class TestHandler extends AbstractHandler {
		
		public void handle(String arg0, Request baseRequest, HttpServletRequest arg2, HttpServletResponse response) throws IOException, ServletException {
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			
			BufferedReader reader = baseRequest.getReader();
			int read = 0;
			String message = "";
			while ((read = reader.read()) != -1) {
				message += (char) read;
			}
			
			JSONObject jsonObj = null;
			try {
				jsonObj = new JSONObject(message);
			} catch (JSONException e) {
				fail("Sent json isn't correct");
			}
			
			assertNotNull(jsonObj);
			response.getWriter().println("<h1>Hello World</h1>");
		}
		
	}
}
