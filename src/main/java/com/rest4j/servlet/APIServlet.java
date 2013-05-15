package com.rest4j.servlet;

import com.rest4j.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class APIServlet extends HttpServlet {

	private API api;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String pathPrefix = config.getInitParameter("pathPrefix");
		if (pathPrefix == null) pathPrefix = "";
		String xml = getMandatoryParam(config, "apiDescriptionXml");
		try {
			if (!xml.startsWith("/")) xml = "/"+xml;
			URL xmlResource = config.getServletContext().getResource(xml);
			if (xmlResource == null) {
				xmlResource = getClass().getClassLoader().getResource(xml);
			}
			if (xmlResource == null) {
				throw new ServletException("Cannot find "+xml+" in the servlet context or classpath for servlet "+config.getServletName()+". Put it somewhere in your .war or classpath");
			}
			String serviceProviderClassName = getMandatoryParam(config, "serviceProviderClass");
			Class serviceProviderClass;
			try {
				serviceProviderClass = Class.forName(serviceProviderClassName);
			} catch (ClassNotFoundException e) {
				throw new ServletException("Cannot find class "+serviceProviderClassName);
			}
			if (!ServiceProvider.class.isAssignableFrom(serviceProviderClass)) {
				throw new ServletException("Class "+serviceProviderClassName+" does not inherit "+ServiceProvider.class.getName());
			}
			ServiceProvider serviceProvider = (ServiceProvider) serviceProviderClass.newInstance();
			api = new APIFactory(xmlResource, pathPrefix, serviceProvider).createAPI();
		} catch (MalformedURLException e) {
			throw new ServletException(e);
		} catch (InstantiationException e) {
			throw new ServletException(e);
		} catch (IllegalAccessException e) {
			throw new ServletException(e);
		} catch (ConfigurationException e) {
			throw new ServletException(e);
		}

	}

	@Override
	protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		APIResponse response;
		try {
			response = api.serve(APIRequest.from(httpServletRequest));
		} catch (APIException e) {
			response = e.createResponse();
		}
		response.outputBody(httpServletResponse);
	}

	private String getMandatoryParam(ServletConfig config, String name) throws ServletException {
		String val = config.getInitParameter(name);
		if (val == null) {
			throw new ServletException("Init parameter "+name+" is not specified for servlet "+config.getServletName());
		}
		return val;
	}
}
