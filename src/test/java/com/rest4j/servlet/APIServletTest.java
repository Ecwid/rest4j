package com.rest4j.servlet;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class APIServletTest {

	private static int jettyPort;
	private static Server server;

	@BeforeClass public static void initServer() throws Exception {
		server = new Server(jettyPort = Integer.parseInt(System.getProperty("jetty.port", "8087")));
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");

		String warDir = ".";
		webapp.setDescriptor(warDir + "/src/test/resources/com/rest4j/servlet/web.xml");
		webapp.setWar(warDir);
		server.setHandler(webapp);

		server.start();
	}

	@AfterClass	public static void dispose() throws Exception {
		server.stop();
	}

	@Test public void testServe_success() throws Exception {
		URLConnection cnn = new URL("http://localhost:" + jettyPort + "/api/v2/pets?type=cat").openConnection();
		JSONArray array = new JSONArray(IOUtils.toString(cnn.getInputStream()));
		assertEquals(1, array.length());
		assertEquals("cat", array.getJSONObject(0).getString("type"));
	}

	@Test public void testServe_error() throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		// no type parameter
		HttpGet get = new HttpGet("http://localhost:" + jettyPort + "/api/v2/pets");
		HttpResponse response = httpclient.execute(get);
		HttpEntity entity = response.getEntity();
		InputStream instream = entity.getContent();
		String responseString = IOUtils.toString(instream);
		System.out.println(responseString);
		try {
			JSONObject err = new JSONObject(responseString);
			assertEquals("type", err.getString("field"));
		} finally {
			instream.close();
		}
	}


}
