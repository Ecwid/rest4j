<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE stylesheet [
		<!ENTITY spc "<xsl:text xml:space='preserve'> </xsl:text>">
		<!ENTITY apache-copyright SYSTEM "apache-copyright.inc">
]>
<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:api="http://rest4j.com/api-description"
				xmlns:fn="http://www.w3.org/2005/xpath-functions"
				xmlns:rest4j="http://rest4j.com/func"
				xmlns="http://www.w3.org/1999/xhtml"
				xmlns:html="http://www.w3.org/1999/xhtml"
				exclude-result-prefixes="#all"
				xpath-default-namespace="http://rest4j.com/api-description"
		>

	<xsl:param name="url" select="'use the url parameter to set default endpoint url, and an optional https-url to set the https url.'"/>
	<xsl:param name="https-url"/>
	<xsl:param name="module-name" select="'apiclient'"/>
	<xsl:param name="version" select="'1.0-SNAPSHOT'"/>
	<xsl:param name="project-url" select="'use the project-url parameter to set the project URL.'"/>
	<xsl:param name="description" select="'use the deacirption parameter to set the project description.'"/>
	<xsl:param name="name" select="'use the name parameter to set the project name.'"/>
	<xsl:param name="developer-name"/>
	<xsl:param name="developer-email"/>
	<xsl:param name="license-name" select="'License :: OSI Approved :: Apache Software License'"/> <!-- should match one from https://pypi.python.org/pypi?%3Aaction=list_classifiers -->
	<xsl:param name="license-url" select="'http://www.apache.org/licenses/LICENSE-2.0.txt'"/>
	<xsl:param name="common-params" select="''"/>
	<xsl:param name="copyright">&apache-copyright;</xsl:param>
	<xsl:param name="additional-client-code" select="'# use the additional-client-code xslt parameter to insert custom code here'"/>
	<xsl:param name="api-name" select="'API'"/>
	<xsl:param name="python-classifiers"/> <!-- should be python strings with commas, from https://pypi.python.org/pypi?%3Aaction=list_classifiers -->

	<xsl:variable name="common-param-set" select="fn:tokenize($common-params,' ')"/>

	<xsl:template match="api">
		<api:files>
			<api:file>
				<xsl:attribute name="name"><xsl:value-of select="$module-name"/>.py</xsl:attribute>
				<xsl:attribute name="text">on</xsl:attribute>#!/usr/bin/python
# -*- coding: utf-8 -*-
<xsl:value-of select="rest4j:hashComment($copyright)"/>
import urllib
import dateutil.parser
from datetime import datetime
import json
import httplib
from urlparse import urlparse
import types
import socket

APPLICATION_JSON = "application/json; utf-8"
TEXT_PLAIN = "text/plain; utf-8"
<!--
		********************************************************
		********************* Models (DTOs) ********************
		********************************************************
-->
<xsl:for-each select="model">class <xsl:value-of select="@name"/>(object):
	def __init__(self, json = None, **dict):
		if json == None:
			json = {}
		self._json = json
		for key, val in dict.iteritems():
			setattr(self, key, val)
<xsl:for-each select="fields/(complex|simple)" xml:space="preserve">
	<xsl:variable name="name"><xsl:apply-templates select="." mode="field-name"/></xsl:variable>
	def <xsl:value-of select="rest4j:camelCase('Get',$name)"/>(self):
		if <xsl:value-of select="rest4j:quote(@name)"/> in self._json:
			val = self._json[<xsl:value-of select="rest4j:quote(@name)"/>]
			return <xsl:apply-templates select="." mode="prop-cast"/>
		return None

	def <xsl:value-of select="rest4j:camelCase('Set',$name)"/>(self, val):
		self._json[<xsl:value-of select="rest4j:quote(@name)"/>] = <xsl:apply-templates select="." mode="prop-json"/>

	<xsl:value-of select="$name"/> = property(<xsl:value-of select="rest4j:camelCase('Get',$name)"/>, <xsl:value-of select="rest4j:camelCase('Set',$name)"/>, doc = """
		<xsl:value-of select="rest4j:htmlToPlain2(description/(*|text()))"/>""")

	def asJSON(self):
		return self._json

	def __str__(self):
		return json.dumps(self._json)
</xsl:for-each>
</xsl:for-each>
<!--
		*********************************************************
		***************  Parameter Object classes  **************
		*********************************************************
-->
<xsl:for-each select="endpoint[@client-param-object]">
<xsl:variable name="params" select="rest4j:param-variables(.)"/>
class <xsl:value-of select="@client-param-object"/>(object):
	def __init__(self<xsl:for-each select="$params">, <xsl:value-of select="*:name"/>=None</xsl:for-each>):
		<xsl:for-each select="$params">
		self._<xsl:value-of select="*:name"/> = <xsl:value-of select="*:name"/>
		</xsl:for-each>

	<xsl:for-each select="$params" xml:space="preserve">
	def <xsl:value-of select="rest4j:camelCase('Get',*:name)"/> (self):
		return self._<xsl:value-of select="*:name"/>

	def <xsl:value-of select="rest4j:camelCase('Set',*:name)"/> (self, value):
		self._<xsl:value-of select="*:name"/> = value

	<xsl:value-of select="*:name"/> = property(<xsl:value-of select="rest4j:camelCase('Get',*:name)"/>, <xsl:value-of select="rest4j:camelCase('Set',*:name)"/>, doc="""<xsl:value-of select="*:doc"/>
		Of type <xsl:value-of select="*:type"/>""")
	</xsl:for-each>
</xsl:for-each>
<!--
		*********************************************************
		*******************  Utility classes  ******************
		*********************************************************
-->
class UrlBuilder(object):

	def __init__(self, url):
		self._url = url
		self._params = None

	def setParameter(self, name, value):
		if self._params == None:
			self._params = {name: value}
		else:
			self._params[name] = value

	def build(self):
		if self._params != None and len(self._params)>0:
			return self._url+"?"+urllib.urlencode(self._params)
		else:
			return self._url

def boolean_param(b):
	if b: return 'true'
	else: return 'false'

def date_from_json(str):
	if str == None:
		return None
	return dateutil.parser.parse(str)

def singleton_to_json(val):
	if val == None:
		return None
	if isinstance(val, datetime):
		return val.isoformat();
	if hasattr(val, 'asJSON'):
		return val.asJSON()
	return val

def param_str(val):
	if val == None: return ''
	if isinstance(val, datetime): return val.isoformat()
	if isinstance(val, bool): return boolean_param(val)
	return str(val)

class Request(object):
    def __init__(self, executor, uri, body = None):
        self._executor = executor
        self._uri = uri
        self._body = body

    @property
    def uri(self):
        return self._uri

    @property
    def body(self):
        return self._body

    def execute(self):
        return self._execute(self._executor)

class StatusException(Exception):
    def __init__(self, status, message):
        self.status = status
        self.message = message
        super(StatusException, self).__init__("Unexpected status: "+str(status)+" "+message)

class Entity(object):
	def __init__(self, data, content_type):
		self.data = data
		self.content_type = content_type
<!--
		***************************************************
		**************** The main API Class ***************
		***************************************************
-->
class Api(object):

	def _default_executor(self, url, method, uri, body, headers):
		connection = self._connect(url)
		connection.request(method, uri, body, headers)
		return connection.getresponse()

	def _make_request(self, executor, method, url, body = None, headers = {}):
		if isinstance(body, Entity):
			newheaders = {}
			newheaders.update(headers)
			newheaders['Content-Type'] = body.content_type
			body = body.data
		else:
			newheaders = headers

		parsed = urlparse(url)
		query = parsed.path
		if len(parsed.query) > 0:
			query = query + "?" + parsed.query

		return executor(url, method, query, body, newheaders)

	def __init__(self,
	<xsl:variable name="doc" select="/"/>
	<xsl:for-each select="$common-param-set">
		<xsl:variable name='param-name' select='.'/>
		<xsl:if test="not($doc/api/endpoint/parameters/parameter[@name=$param-name and @optional='true'])">
			<xsl:text xml:space="preserve">	</xsl:text>
			<xsl:value-of select="rest4j:pythonIdentifier($param-name)"/>,
		</xsl:if>
	</xsl:for-each><xsl:for-each select="$common-param-set">
		<xsl:variable name='param-name' select='.'/>
		<xsl:if test="$doc/api/endpoint/parameters/parameter[@name=$param-name and @optional='true']">
			<xsl:text xml:space="preserve">	</xsl:text>
			<xsl:value-of select="rest4j:pythonIdentifier($param-name)"/> = None,
		</xsl:if>
	</xsl:for-each>
			executor = None,
			url = "<xsl:value-of select='$url'/>",
	<xsl:if test="$https-url">
			secure_url = "<xsl:value-of select='$https-url'/>",
	</xsl:if>
			debug_level = 0):
		"""
		Create an object used to make <xsl:value-of select="$api-name"/> requests.

		Args:<xsl:for-each select="$common-param-set">
		<xsl:variable name='param-name' select='.'/>
		<xsl:if test="not($doc/api/endpoint/parameters/parameter[@name=$param-name])"><xsl:value-of select="error(fn:QName('http://rest4j.com/','PARAM-NOT-FOUND'),concat('Parameter not found: ', $param-name))"/></xsl:if>
		<xsl:text xml:space="preserve">
		</xsl:text>
		<xsl:value-of select="rest4j:pythonIdentifier($param-name)"/> - <xsl:value-of select="rest4j:description(($doc/api/endpoint/parameters/parameter[@name=$param-name]/description)[1])"/>
	</xsl:for-each>
		executor - an optional function that replaces HTTPConnection.request.
			The function signature is: self, url, method, uri, body, headers
			The default executor is:
				connection = self._connect(url)
				connection.request(method, uri, body, headers)
				return connection.getresponse()
		url - an optional alternative HTTP URL of the REST API endpoint
	<xsl:if test="$https-url">
		secure_url - an optional alternative HTTPS URL of the REST API endpoint
	</xsl:if>
		debug_level - the debug level passed through to httplib; default is zero
		"""
		<xsl:for-each select="$common-param-set">
		<xsl:variable name='param-name' select='.'/>
		self._<xsl:value-of select="rest4j:pythonIdentifier($param-name)"/> = <xsl:value-of select="rest4j:pythonIdentifier($param-name)"/>
	</xsl:for-each>
		self._url = url
	<xsl:if test="$https-url">
		self._secure_url = secure_url
	</xsl:if>
		if executor == None:
			self._executor = self._default_executor
		else:
			self._executor = executor

		self._debug_level = debug_level
		self._conn = None

	def GetUrl(self):
		return self._url

	def SetUrl(self, url):
		self._url = url
		self.close()
		return self

	url = property(GetUrl, SetUrl, doc=<xsl:value-of select="rest4j:quote($api-name)"/>+" endpoint URL")
	<xsl:if test="$https-url">
	def GetSecureUrl(self):
		return self._secure_url

	def SetSecureUrl(self, url):
		self._secure_url = url
		self.close()
		return self

	secure_url = property(GetSecureUrl, SetSecureUrl, doc=<xsl:value-of select="rest4j:quote($api-name)"/>+" endpoint URL for HTTPS")
	</xsl:if>
<!-- setters for common params -->
<xsl:for-each select="$common-param-set" xml:space="preserve">
	<xsl:variable name='param-name' select='.'/>
	<xsl:if test="not($doc/api/endpoint/parameters/parameter[@name=$param-name])"><xsl:value-of select="error(fn:QName('http://rest4j.com/','PARAM-NOT-FOUND'),concat('Parameter not found: ', $param-name))"/></xsl:if>
	def <xsl:value-of select="rest4j:camelCase('Set',rest4j:pythonIdentifier($param-name))"/>(self, <xsl:value-of select="rest4j:pythonIdentifier($param-name)"/>):
		self._<xsl:value-of select="rest4j:pythonIdentifier($param-name)"/> = <xsl:value-of select="rest4j:pythonIdentifier($param-name)"/>
		return self

	def <xsl:value-of select="rest4j:camelCase('Get',rest4j:pythonIdentifier($param-name))"/>(self):
		return self._<xsl:value-of select="rest4j:pythonIdentifier($param-name)"/>

	<xsl:value-of select="rest4j:pythonIdentifier($param-name)"/> = property(<xsl:value-of select="rest4j:camelCase('Get',rest4j:pythonIdentifier($param-name))"/>, <xsl:value-of select="rest4j:camelCase('Set',rest4j:pythonIdentifier($param-name))"/>, doc="""
		<xsl:value-of select="rest4j:description(($doc/api/endpoint/parameters/parameter[@name=$param-name]/description)[1])"/>""")
</xsl:for-each>
	def _connect(self, url):
		if self._conn != None:
			self._conn.close()
		parsed = urlparse(url)
		if parsed.scheme == 'http':
			self._conn = httplib.HTTPConnection(parsed.netloc,strict=True)
		elif parsed.scheme == 'https':
			self._conn = httplib.HTTPSConnection(parsed.netloc,strict=True)
		else:
			raise Exception("Unrecognized scheme: "+parsed.scheme)
		self._conn.set_debuglevel(self._debug_level)
		return self._conn

	def close(self):
		if self._conn != None:
			self._conn.close()

<xsl:for-each select="endpoint">
	<xsl:apply-templates select='.' mode='endpoint-method'/>
</xsl:for-each>
<xsl:text>
</xsl:text>
<xsl:value-of select='$additional-client-code'/>

			</api:file>

<!--
		***************************************************
		*******************  setup.py  ********************
		***************************************************
-->
			<api:file name="setup.py" text="on">#!/usr/bin/python
# -*- coding: utf-8 -*-
from distutils.core import setup

setup(
	name=<xsl:value-of select="rest4j:quote($module-name)"/>,
	version='<xsl:value-of select="$version"/>',
	author=<xsl:value-of select="rest4j:quote($developer-name)"/>,
	author_email='<xsl:value-of select="$developer-email"/>',
	url='<xsl:value-of select="$project-url"/>',
	download_url='<xsl:value-of select="$project-url"/>',
	description=<xsl:value-of select="rest4j:quote($description)"/>,
	license='<xsl:value-of select="$license-url"/>',
	packages=[],
	py_modules = [<xsl:value-of select="rest4j:quote($module-name)"/>],
	platforms=['Any'],
	<!-- long_description=desc, TODO: add a generated sample here -->
	classifiers=['Development Status :: 5 - Production/Stable',
		'Intended Audience :: Developers',
		'<xsl:value-of select="$license-name"/>',
		'Operating System :: OS Independent',
		'Topic :: Software Development :: Libraries :: Python Modules',
		'Programming Language :: Python',
		<xsl:value-of select="$python-classifiers"/>
	]
)
			</api:file>
		</api:files>
	</xsl:template>

	<!--
			***************************************************
			**************  The endpoint method  **************
			***************************************************
	-->
	<xsl:template match="endpoint" mode="endpoint-method">
		<xsl:variable name="params" select="rest4j:param-variables(.)"/>
	def <xsl:apply-templates select='.' mode="endpoint-method-name"/>(self, <xsl:apply-templates select='.' mode="endpoint-method-params"/>):
		"""
		<xsl:choose>
		<xsl:when test="description/html:title">
		Builds Request object for the "<xsl:value-of select="rest4j:htmlToPlain2(description/html:title/(*|text()))"/>" request.</xsl:when>
		<xsl:otherwise>
		Builds Request object for the "<code><xsl:value-of select="@http"/><xsl:text xml:space="preserve"> </xsl:text><xsl:apply-templates select="route" mode="route"/></code>" request.</xsl:otherwise>
		</xsl:choose>
		To actually execute the request, call the execute() method on the returned object. <xsl:choose>
			<xsl:when test="response/json/@collection='singleton'">execute() returns an object of type <xsl:value-of select="response/json/@type"/>.</xsl:when>
			<xsl:when test="response/json/@collection='list'">execute() returns a list <xsl:value-of select="response/json/@type"/> objects.</xsl:when>
			<xsl:when test="response/json/@collection='map'">execute() returns a dictionary containing <xsl:value-of select="response/json/@type"/> objects.</xsl:when>
			<xsl:when test="response/binary">execute() returns binary data.</xsl:when>
			<xsl:when test="response/text">execute() returns text.</xsl:when>
		</xsl:choose>
		<xsl:text>
		</xsl:text>
		<xsl:value-of select="rest4j:description(description)"/>
		<xsl:text>
		</xsl:text>
		Args:<xsl:choose>
		<xsl:when test="@client-param-object" xml:space="preserve">
		<xsl:value-of select="@client-param-object"/> - The request parameters object.</xsl:when>
		<xsl:otherwise>
		<xsl:for-each select="$params" xml:space="preserve">
		<xsl:value-of select="*:name"/> - <xsl:value-of select="*:doc"/> Of type <xsl:value-of select="*:type"/></xsl:for-each>
		</xsl:otherwise>
		</xsl:choose>
		"""
		builder = UrlBuilder(<xsl:if test="rest4j:secure(.)='true'">self._secure_url</xsl:if><xsl:if test="rest4j:secure(.)='false'">self._url</xsl:if><xsl:apply-templates select="route/(param|text())"/>);
		<xsl:for-each select="parameters/parameter[@optional='true' and rest4j:path-param(ancestor::endpoint,@name)='false']">if <xsl:value-of select="rest4j:param-value(ancestor::endpoint, @name)"/> != None: builder.setParameter("<xsl:value-of select="@name"/>", <xsl:value-of select="rest4j:param-str(.)"/>)
		</xsl:for-each>
		<xsl:for-each select="parameters/parameter[@optional='false']">
		if <xsl:value-of select="rest4j:param-value(ancestor::endpoint, @name)"/> == None: raise Exception("No parameter <xsl:value-of select="@name"/> is set")
		<xsl:if test="rest4j:path-param(ancestor::endpoint,@name)='false'">builder.setParameter("<xsl:value-of select="@name"/>", <xsl:value-of select="rest4j:param-str(.)"/>)</xsl:if>
		</xsl:for-each>
		<xsl:variable name="body" select="rest4j:param-value(., 'body')"/>
		<xsl:if test="body and not(body/json/@optional='true')">
		if <xsl:value-of select="$body"/> == None: raise Exception("No request body")
		</xsl:if>
		<xsl:choose>
			<xsl:when test="body/json/@optional='true'">
		if <xsl:value-of select="$body"/> == None:
			body = None
		else:
			body = Entity(<xsl:apply-templates select="body/json" mode="body-as-json"/>, APPLICATION_JSON)
			</xsl:when>
			<xsl:when test="body/json">
		body = Entity(<xsl:apply-templates select="body/json" mode="body-as-json"/>, APPLICATION_JSON)
			</xsl:when>
			<xsl:when test="body/patch">
		body = Entity(json.dumps(<xsl:value-of select="$body"/>.asJSON()), APPLICATION_JSON)
			</xsl:when>
			<xsl:when test="body/text">
		body = Entity(<xsl:value-of select="$body"/>, TEXT_PLAIN)
			</xsl:when>
			<xsl:when test="body/binary">
		if isinstance(<xsl:value-of select="$body"/>, Entity):
			body = <xsl:value-of select="$body"/>
		else:
			body = Entity(<xsl:value-of select="$body"/>, "application/octet-stream")
			</xsl:when>
			<xsl:otherwise>
		body = None
			</xsl:otherwise>
		</xsl:choose>
		request = Request(self._executor, builder.build(), body)
		api = self

		def execute(self, executor): # will be part of the Request class
			response = api._make_request(executor, '<xsl:value-of select="@http"/>', self.uri, self.body)
			if response.status >= 400: raise StatusException(response.status, response.reason)
			<xsl:choose>
			<xsl:when test="response/json/@collection='array'">data = response.read()
			if data == '':<xsl:if test="response/json/@optional='false'"> raise Exception("No response. Expected JSON array.")</xsl:if><xsl:if test="response/json/@optional='true'"> return None</xsl:if>
			data = json.loads(data)
			if not isinstance(data, list): raise Exception("Expected JSON array, "+str(type(data))+" is returned")
			data = [<xsl:value-of select='response/json/@type'/>(x) for x in data]
			return data
			</xsl:when>
			<xsl:when test="response/json">data = response.read()
			if data == '':<xsl:if test="response/json/@optional='false'"> raise Exception("No response. Expected JSON object.")</xsl:if><xsl:if test="response/json/@optional='true'"> return None</xsl:if>
			data = json.loads(data)
			if not isinstance(data, dict): raise Exception("Expected JSON object, "+str(type(data))+" is returned")
			return <xsl:value-of select='response/json/@type'/>(data)
			</xsl:when>
			<xsl:when test="response/text">return data
			</xsl:when>
			<xsl:when test="response/binary">return data
			</xsl:when>
			</xsl:choose>

		request._execute = types.MethodType(execute, request)
		return request

	</xsl:template>

	<xsl:template match="route/text()">+"<xsl:value-of select="."/>"</xsl:template>
	<xsl:template match="route/param">+param_str(<xsl:value-of select="rest4j:param-value(ancestor::endpoint, text())"/>)</xsl:template>

	<xsl:template match="text()" mode="route"><xsl:copy-of select="."/></xsl:template>
	<xsl:template match="param" mode="route">[<xsl:value-of select="."/>]</xsl:template>

	<xsl:function name="rest4j:param-value">
		<xsl:param name="endpoint"/>
		<xsl:param name="name"/>
		<xsl:choose>
			<xsl:when test="$endpoint/@client-param-object and not(fn:index-of($common-param-set,$name))">params.<xsl:value-of select="rest4j:pythonIdentifier($name)"/></xsl:when>
			<xsl:when test="fn:index-of($common-param-set,$name)">self._<xsl:value-of select="rest4j:pythonIdentifier($name)"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="rest4j:pythonIdentifier($name)"/></xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="rest4j:path-param">
		<xsl:param name="endpoint"/>
		<xsl:param name="name"/>
		<xsl:value-of select="count($endpoint/route/param[text()=$name])>0"/>
	</xsl:function>

	<xsl:function name="rest4j:secure">
		<xsl:param name="endpoint"/>
		<xsl:value-of select="$https-url and ($endpoint/@httpsonly='true' or $endpoint/parameters/parameter[@httpsonly='true'])"/>
	</xsl:function>

	<xsl:template match="endpoint[@client-method-name]" mode="endpoint-method-name"><xsl:value-of select="@client-method-name"/></xsl:template>
	<xsl:template match="endpoint" mode="endpoint-method-name"><xsl:value-of select="rest4j:camelCase(service/@method,rest4j:singular(service/@name))"/></xsl:template>
	<xsl:template match="endpoint" mode="endpoint-method-params">
		<xsl:choose>
			<xsl:when test="@client-param-object" xml:space="preserve">params # of type <xsl:value-of select="@client-param-object"/>
</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="rest4j:param-variables(.)">
					<xsl:if test="position()>1">,</xsl:if>
					<xsl:value-of select="*:name"/>
					<xsl:if test="*:optional" xml:space="preserve"> = None</xsl:if>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
	   Returns params/param/(name|doc) for all non-common API method parameters, including the body.
	  -->
	<xsl:function name="rest4j:param-variables">
		<xsl:param name="endpoint"></xsl:param>
		<xsl:variable name="body" select="$endpoint/body"/>
		<xsl:for-each select="$endpoint/parameters/parameter[not(fn:index-of($common-param-set,@name))]">
			<param>
				<type><xsl:value-of select="@type"/></type>
				<name><xsl:value-of select="rest4j:pythonIdentifier(@name)"/></name>
				<doc><xsl:value-of select="rest4j:description(description)"/></doc>
				<xsl:if test="@optional='true'"><optional/></xsl:if>
			</param>
		</xsl:for-each>
		<xsl:if test="$body"> 
			<param>
				<type>
					<xsl:choose>
						<xsl:when test="$body/json/@collection='singleton' or $body/patch"><xsl:value-of select="$body/(json|patch)/@type"/></xsl:when>
						<xsl:when test="$body/json/@collection='array'">Array&lt;<xsl:value-of select="$body/json/@type"/>&gt;</xsl:when>
						<xsl:when test="$body/json/@collection='map'">Map&lt;string,<xsl:value-of select="$body/json/@type"/>&gt;</xsl:when>
						<xsl:when test="$body/binary">binary</xsl:when>
						<xsl:when test="$body/text">text</xsl:when>
					</xsl:choose>
				</type>
				<name>
					<xsl:choose>
						<xsl:when test="$body/@client-name"><xsl:value-of select="$body/@client-name"/></xsl:when>
						<xsl:otherwise>body</xsl:otherwise>
					</xsl:choose>
				</name>
				<doc><xsl:value-of select="rest4j:description($body/description)"/></doc>
				<xsl:if test="not($body/json/@optional='false')"><optional/></xsl:if>
			</param>
		</xsl:if>
	</xsl:function>

	<!--
		Casting values from JSON to Python
	-->
	<xsl:template match="*[@collection='singleton']" mode="prop-cast"><xsl:apply-templates select='.' mode='prop-singleton-cast'/></xsl:template>
	<xsl:template match="*[@collection='array']" mode="prop-cast">[<xsl:apply-templates select='.' mode='prop-singleton-cast'/> for val in val]</xsl:template>
	<xsl:template match="*[@collection='map']" mode="prop-cast">dict([(key, <xsl:apply-templates select='.' mode='prop-singleton-cast'/>) for key, val in val.iteritems()])</xsl:template>
	<xsl:template match="complex" mode="prop-singleton-cast"><xsl:value-of select="@type"/>(val)</xsl:template>
	<xsl:template match="simple[@type='number']" mode="prop-singleton-cast">val</xsl:template>
	<xsl:template match="simple[@type='string']" mode="prop-singleton-cast">val</xsl:template>
	<xsl:template match="simple[@type='boolean']" mode="prop-singleton-cast">val</xsl:template>
	<xsl:template match="simple[@type='date']" mode="prop-singleton-cast">date_from_json(val)</xsl:template>
	<xsl:template match="simple" mode="prop-singleton-cast">val</xsl:template>

	<!--
		Casting values from Python to JSON
	-->
	<xsl:template match="*[@collection='singleton']" mode="prop-json"><xsl:apply-templates select='.' mode='prop-singleton-json'/></xsl:template>
	<xsl:template match="*[@collection='array']" mode="prop-json">[<xsl:apply-templates select='.' mode='prop-singleton-json'/> for val in val]</xsl:template>
	<xsl:template match="*[@collection='map']" mode="prop-json">dict([(key, <xsl:apply-templates select='.' mode='prop-singleton-json'/>) for key, val in val.iteritems()])</xsl:template>
	<xsl:template match="complex|simple" mode="prop-singleton-json">singleton_to_json(val)</xsl:template>

	<!--
		Transforming a request body into a JSON
	-->
	<xsl:template match="json[@collection='singleton']" mode="body-as-json">json.dumps(body.asJSON())</xsl:template>

	<xsl:template match="json[@collection='array']" mode="body-as-json">json.dumps([x.asJSON() for x in body])</xsl:template>
	<xsl:template match="json[@collection='map']" mode="body-as-json">json.dumps(dict([(key, x.asJSON()) for key, x in body.iteritems()]))</xsl:template>

	<!--
		Auxiliary functions
	-->
	<xsl:function name="rest4j:description">
		<xsl:param name="description"/>
		<xsl:value-of select="rest4j:htmlToPlain2($description/(*[not(@client-lang) or @client-lang='*' or contains('python,', concat(@client-lang,','))]|text())[name()!='html:title'])"/>
	</xsl:function>

	<xsl:function name="rest4j:param-str">
		<xsl:param name="param"/>
		<xsl:variable name="val" select="rest4j:param-value($param/ancestor::endpoint, $param/@name)"/>
		<xsl:choose>
			<xsl:when test="$param/@type='date'"><xsl:value-of select="$val"/>.isoformat()</xsl:when>
			<xsl:when test="$param/@type='boolean'">boolean_param(<xsl:value-of select="$val"/>)</xsl:when>
			<xsl:otherwise>str(<xsl:value-of select="$val"/>)</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:template match="simple|complex" mode="field-name">
		<xsl:if test="@client-name"><xsl:value-of select="@client-name"/></xsl:if>
		<xsl:if test="not(@client-name)"><xsl:value-of select="rest4j:pythonIdentifier(@name)"/></xsl:if>
	</xsl:template>

	<xsl:function name="rest4j:pythonIdentifier">
		<xsl:param name="str"/>
		<xsl:value-of select="rest4j:identifier($str, 'and del from not while as elif global or with assert else if pass yield break except import print class exec in raise continue finally is return def for lambda try')"/>
	</xsl:function>
</xsl:stylesheet>