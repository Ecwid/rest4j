package com.rest4j.servlet;

import com.rest4j.*;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An utility servlet that performs REST requests. Possible init-params are:
 *
 * <ul>
 *     <li>pathPrefix - a prefix that is added to the endpoint/route, optional.</li>
 *     <li>apiDescriptionXml - either classpath or context path to the API description XML.</li>
 *     <li>serviceProviderClass - class name of ServiceProvider. Should have default constructor.</li>
 *     <li>objectFactories - comma-separated class names of ObjectFactories. Should have default constructors.</li>
 *     <li>fieldFilters - comma-separated class names of FieldFilter. Should have default constructors.</li>
 * </ul>
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class APIServlet extends HttpServlet {

	private API api;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String pathPrefix = config.getInitParameter("pathPrefix");
		if (pathPrefix == null) pathPrefix = "";
		String xml = getMandatoryParam(config, "apiDescriptionXml").trim();
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
				serviceProviderClass = Class.forName(serviceProviderClassName.trim());
			} catch (ClassNotFoundException e) {
				throw new ServletException("Cannot find service provider class "+serviceProviderClassName);
			}
			if (!ServiceProvider.class.isAssignableFrom(serviceProviderClass)) {
				throw new ServletException("Service provider class "+serviceProviderClassName+" does not inherit "+ServiceProvider.class.getName());
			}
			ServiceProvider serviceProvider = (ServiceProvider) serviceProviderClass.newInstance();

			ApiFactory apiFactory = new ApiFactory(xmlResource, pathPrefix, serviceProvider, null);
			String facs = config.getInitParameter("objectFactories");
			if (facs != null) {
				for (String className: facs.split(",")) {
					try {
						Class facClass = Class.forName(className.trim());
						if (!ObjectFactory.class.isAssignableFrom(facClass)) {
							throw new ServletException("Object factory class "+className+" does not inherit "+ObjectFactory.class.getName());
						}
						apiFactory.addObjectFactory((ObjectFactory) facClass.newInstance());
					} catch (ClassNotFoundException e) {
						throw new ServletException("Cannot find factory class "+className);
					}
				}
			}
			String filters = config.getInitParameter("fieldFilters");
			if (filters != null) {
				for (String className: filters.split(",")) {
					try {
						Class ffClass = Class.forName(className.trim());
						if (!FieldFilter.class.isAssignableFrom(ffClass)) {
							throw new ServletException("Field filter class "+className+" does not inherit "+FieldFilter.class.getName());
						}
						apiFactory.addFieldFilter((FieldFilter) ffClass.newInstance());
					} catch (ClassNotFoundException e) {
						throw new ServletException("Cannot find filter class "+className);
					}
				}
			}
			api = apiFactory.createAPI();

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
		ApiResponse response;
		try {
			response = api.serve(ApiRequest.from(httpServletRequest));
		} catch (ApiException e) {
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
