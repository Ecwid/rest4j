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
  ~	  http://www.apache.org/licenses/LICENSE-2.0
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
	<xsl:param name="prefix" select="'Api'"/>
	<xsl:param name="common-params" select="''"/>
	<xsl:param name="copyright">&apache-copyright;</xsl:param>
	<xsl:param name="additional-client-code" select="'# use the additional-client-code xslt parameter to insert custom code here'"/>
	<xsl:param name="api-name" select="'API'"/>

	<xsl:variable name="common-param-set" select="fn:tokenize($common-params,' ')"/>

	<xsl:template match="api">
		<api:files>
			<api:file>
				<xsl:attribute name="name"><xsl:value-of select="$module-name"/>.php</xsl:attribute>
				<xsl:attribute name="text">on</xsl:attribute>&lt;?php
/*
<xsl:value-of select="$copyright"/>*/

/**
 * Instances of this object are returned from <xsl:value-of select="$prefix"/>Client's method calls. A caller then should call execute()
 * on this object to make the request and return a result.
 */
abstract class <xsl:value-of select="$prefix"/>Request {
	/**
	 * A function with signature ($url, $method, $body, $headers)
	 * that executes the HTTP request and returns an <xsl:value-of select="$prefix"/>Response object.
	 */
	public $executor;

	public $url;
	public $body;

	public function __construct($executor, $url, HttpEntity $body = null) {
		$this->executor = $executor;
		$this->url = $url;
		$this->body =$body;
	}

	public abstract function execute();

}

class <xsl:value-of select="$prefix"/>Response {
	/**
	 * Response data.
	 */
	public $data;

	/**
	 * Response Content-Type, including charset.
	 */
	public $contentType;

	/**
	 * Response headers, assoc array.
	 */
	public $headers;

	public function __construct($data, $contentType, $headers) {
		$this->data = $data;
		$this->contentType = $contentType;
		$this->headers = $headers;
	}
}

class HttpEntity {
	const JSON = "application/json; charset=utf-8";
	const TEXT = "text/plain; charset=utf-8";
	const BINARY = "application/octet-stream";

	public $body;
	public $contentType;

	public function __construct($body, $contentType) {
		$this->body = $body;
		$this->contentType = $contentType;
	}
}

class StatusException extends Exception {
	public function __construct($message, $code) {
		parent::__construct($message, $code);
	}
}

class IllegalArgumentException extends Exception {
	public function __construct($message, $code = 0) {
		parent::__construct($message, $code);
	}
}

class EmptyBodyException extends Exception {
	public function __construct($message) {
		parent::__construct($message);
	}
}

class <xsl:value-of select="$prefix"/>DTO {
	public function __construct($json = null) {
		foreach ($this as $key => $val) {
			unset($this->{$key});
		}
		if ($json != null) {
			foreach ($json as $key => $value) {
				$this->{$key} = $value;
			}
		}
	}

	/**
	 * Return an object that can be passed to json_encode()
	 */
	public function asJson() {
		return $this;
	}
}
<!--
		**********************************************
		**************** Models (DTOs) ***************
		**********************************************
-->
<xsl:for-each select="model">
class <xsl:value-of select="$prefix"/><xsl:value-of select="@name"/> extends <xsl:value-of select="$prefix"/>DTO {
	<xsl:for-each select="fields/(complex|simple)">
	/**
	 * <xsl:value-of select="rest4j:description(description)"/>
<xsl:apply-templates select="." mode="phpdoc-var-type"/>
	 */
	public $<xsl:apply-templates select="." mode="field-name"/>;
	</xsl:for-each>

	public function __construct($json = null) {
		parent::__construct($json);
	<xsl:for-each select="fields/complex">
		<xsl:variable name="field">$this-><xsl:apply-templates select="." mode="field-name"/></xsl:variable>
		if (isset(<xsl:value-of select="$field"/>))
			<xsl:choose>
			<xsl:when test="@collection='singleton'">
				<xsl:value-of select="$field"/> = new <xsl:value-of select="concat($prefix, @type)"/>(<xsl:value-of select="$field"/>);
			</xsl:when>
			<xsl:when test="@collection='array'">
			for ($i=0; $i &lt; count(<xsl:value-of select="$field"/>); $i++)
				if (isset(<xsl:value-of select="$field"/>[$i]))
					<xsl:value-of select="$field"/>[$i] = new <xsl:value-of select="concat($prefix, @type)"/>(<xsl:value-of select="$field"/>[$i]);
			</xsl:when>
			<xsl:when test="@collection='map'">
			foreach (<xsl:value-of select="$field"/> as $key => $value)
				if ($value != null)
					<xsl:value-of select="$field"/>[$key] = new <xsl:value-of select="concat($prefix, @type)"/>($value);
			</xsl:when>
		</xsl:choose>
	</xsl:for-each>
	}
}
</xsl:for-each>
<!--
		*********************************************************
		***************  Parameter Object classes  **************
		*********************************************************
-->
<xsl:for-each select="endpoint[@client-param-object]">
class <xsl:value-of select="$prefix"/><xsl:value-of select="@client-param-object"/> {
	<xsl:for-each select="rest4j:param-variables(.)" xml:space="preserve">
	/**
	 * <xsl:value-of select="*:doc"/>
	 * @var <xsl:value-of select="*:phpdoc-type"/>
	 */
	public $<xsl:value-of select="*:name"/>;
	</xsl:for-each>
}
</xsl:for-each>
<!--
		***************************************************
		***************** Request Classes *****************
		***************************************************
-->
<xsl:for-each select="endpoint">
class <xsl:apply-templates select='.' mode="endpoint-request-class"/> extends <xsl:value-of select="$prefix"/>Request {
	/**
	 * Make the HTTP request and return the result.<xsl:if test="response[json]">
	 * @return <xsl:value-of select="concat($prefix, response/json/@type)"/> <xsl:if test="response/json/@collection!='singleton'">[]</xsl:if>
	</xsl:if>
	 */
	public function execute()
	{
		$executor = $this->executor;
		$response = $executor($this->url, <xsl:value-of select="rest4j:quote(@http)"/>, $this->body);
		<xsl:choose>
			<xsl:when test="response/json/@collection='array'">if (!$response->data)<xsl:if test="response/json/@optional='false'"> throw new EmptyBodyException("No response. Expected JSON array.");</xsl:if><xsl:if test="response/json/@optional='true'"> return null;</xsl:if>
		$result = array();
		foreach (json_decode($response->data) as $json) {
			$result[] = new <xsl:value-of select='concat($prefix,response/json/@type)'/>($json);
		}
		return $result;</xsl:when>
			<xsl:when test="response/json">if (!$response->data)<xsl:if test="response/json/@optional='false'"> throw new EmptyBodyException("No response. Expected JSON object.");</xsl:if><xsl:if test="response/json/@optional='true'"> return null;</xsl:if>
				return new <xsl:value-of select='concat($prefix,response/json/@type)'/>(json_decode($response->data));
			</xsl:when>
			<xsl:when test="response/text">if (!$response->data) throw new EmptyBodyException("No response. Expected text output.");
				return $response->data;
			</xsl:when>
			<xsl:when test="response/binary">if (!$response->data) throw new EmptyBodyException("No response. Expected binary output.");
				return $response->data;
			</xsl:when>
			<xsl:otherwise>return null;</xsl:otherwise>
		</xsl:choose>
	}
}
</xsl:for-each>
class _UriBuilder {
	private $_url;
	private $_params;

	public function __construct($url) {
		$this->_url = $url;
	}

	public function setParameter($name, $value) {
		if (!isset($this->_params)) $this->_params = array();
		$this->_params[$name] = $value;
	}

	public function build() {
		if (isset($this->_params)) {
			$url = $this->_url . "?";
			$first = TRUE;
			foreach ($this->_params as $name => $value) {
				if ($first) $first = FALSE;
				else $url .= "&amp;";
				$url .= "$name=".rawurlencode($value);
			}
			return $url;
		}
		return $this->_url;
	}
}

class <xsl:value-of select="$prefix"/>Client {
	
	/**
	 * A function with signature ($url, $method, $body, $headers) that executes an HTTP
	 * request.
	 */
	public $executor;

	/**
	 * The <xsl:value-of select="$api-name"/> endpoint URL. The default is "<xsl:value-of select='$url'/>".
	 */
	public $url = <xsl:value-of select='rest4j:quote($url)'/>;

<xsl:if test="$https-url">
	/**
	 * The HTTPS <xsl:value-of select="$api-name"/> endpoint URL. The default is "<xsl:value-of select='$url'/>".
	 */
	public $secure_url = <xsl:value-of select='rest4j:quote($https-url)'/>;
</xsl:if>
<xsl:variable name="doc" select="/"/>
<xsl:for-each select="$common-param-set">
	<xsl:variable name='param-name' select='.'/>
	/**
	 * The value of the "<xsl:value-of select="$param-name"/>" request parameter for subsequent requests.
	 * <xsl:value-of select="rest4j:description(($doc/api/endpoint/parameters/parameter[@name=$param-name]/description)[1])"/>
	 */
	public $<xsl:value-of select="rest4j:phpIdentifier($param-name)"/>;
</xsl:for-each>

	public function __construct($ch = null) {
		if ($ch == null) {
			$ch = curl_init();
			#curl_setopt($this->ch, CURLOPT_FORBID_REUSE, 1); 
			#curl_setopt($this->ch, CURLOPT_FRESH_CONNECT, 1);
		}
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($ch, CURLOPT_HEADER, 1);
		$this->ch = $ch;
		$client = $this;
		$this->executor = function($url, $method, HttpEntity $body = null, array $headers = null) use($client) {
			$ch = $client->ch;
			curl_setopt($ch, CURLOPT_URL, $url);
			curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
			if ($body != null) {
				curl_setopt($ch, CURLOPT_POSTFIELDS, $body->body);
			} else {
				curl_setopt($ch, CURLOPT_POSTFIELDS, "");
			}
			if ($headers == null) $headers = array();
			if ($body != null &amp;&amp; $body->contentType) $headers[] = "Content-Type: ".$body->contentType;
			$headers[] = "Expect:";
			curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
			if( ! $response = curl_exec($ch))
			{ 
				trigger_error(curl_error($ch)); 
			}
			$info = curl_getinfo($ch);
			$header_size = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
			$headersString = substr($response, 0, $header_size);
			$data = substr($response, $header_size);
			if ($info['http_code'] >= 400) {
				$matches = array();
				preg_match('/HTTP[^ ]+ +[0-9]+ +(.*)/', $headersString, $matches);
				throw new StatusException($matches[1], $info['http_code']);
			}
			$contentType = curl_getinfo($ch, CURLINFO_CONTENT_TYPE);
			$headers = array();
			foreach (preg_split("/[\\n\\r]/", $headersString) as $headerLine) {
				$pos = strpos($headerLine, ':');
				if ($pos !== false) {
					$headers[trim(substr($headerLine, 0, $pos))] = trim(substr($headerLine, $pos+1));
				}
			}
			return new <xsl:value-of select="$prefix"/>Response($data, $contentType, $headers);
		};
	}
<!--
		***************************************************
		**************  The endpoint method  **************
		***************************************************
-->
<xsl:for-each select="endpoint">
	<xsl:variable name="params" select="rest4j:param-variables(.)"/>
	/**<xsl:choose>
		<xsl:when test="description/html:title">
	 * Builds Request object for the "<xsl:value-of select="rest4j:javadocEscape(description/html:title/(*|text()))"/>" request.</xsl:when>
		<xsl:otherwise>
	 * Builds Request object for the "<code><xsl:value-of select="@http"/><xsl:text xml:space="preserve"> </xsl:text><xsl:apply-templates select="route" mode="route"/></code>" request.</xsl:otherwise>
	</xsl:choose>
	 * To actually execute the request, call the execute() method on the returned object.&lt;p/&gt;
	 * <xsl:value-of select="rest4j:description(description)"/>
		<xsl:choose>
			<xsl:when test="@client-param-object" xml:space="preserve">
	 * @param params <xsl:value-of select="concat($prefix,@client-param-object)"/></xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="$params" xml:space="preserve">
	 * @param <xsl:value-of select="*:name"/>&spc;<xsl:value-of select="*:phpdoc-type"/>&spc;<xsl:value-of select="*:doc"/></xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	 * @return <xsl:apply-templates select='.' mode="endpoint-request-class"/>
	 */
	public function <xsl:apply-templates select='.' mode="endpoint-method-name"/>(<xsl:apply-templates select='.' mode="endpoint-method-params"/>) {
		$builder = new _UriBuilder(<xsl:if test="rest4j:secure(.)='true'">$this->secure_url</xsl:if><xsl:if test="rest4j:secure(.)='false'">$this->url</xsl:if><xsl:apply-templates select="route/(param|text())"/>);
		<xsl:for-each select="parameters/parameter[@optional='true' and rest4j:path-param(ancestor::endpoint,@name)='false']">if (<xsl:value-of select="rest4j:param-value(ancestor::endpoint, @name)"/> != null) $builder->setParameter("<xsl:value-of select="@name"/>",
			<xsl:choose>
				<xsl:when test="@type='boolean'">self::boolean_param(<xsl:value-of select="rest4j:param-value(ancestor::endpoint, @name)"/>)</xsl:when>
				<xsl:otherwise><xsl:value-of select="rest4j:param-value(ancestor::endpoint, @name)"/></xsl:otherwise>
			</xsl:choose>);
		</xsl:for-each>
		<xsl:for-each select="parameters/parameter[@optional='false']">
		if (<xsl:value-of select="rest4j:param-value(ancestor::endpoint, @name)"/> == null) throw new IllegalArgumentException("No parameter <xsl:value-of select="@name"/> is set");
		<xsl:if test="rest4j:path-param(ancestor::endpoint,@name)='false'">$builder->setParameter("<xsl:value-of select="@name"/>", <xsl:value-of select="rest4j:param-value(ancestor::endpoint, @name)"/>);</xsl:if>
		</xsl:for-each>
		<xsl:variable name="body" select="rest4j:param-value(., 'body')"/>
		<xsl:if test="body and not(body/json/@optional='true')">
		if (<xsl:value-of select="$body"/> == null) throw new IllegalArgumentException("No request body");</xsl:if>
		return new <xsl:apply-templates select='.' mode="endpoint-request-class"/>($this->executor, $builder->build()
		<xsl:choose>
			<xsl:when test="body/json/@optional='true'">, <xsl:value-of select="$body"/> == null ? null : new HttpEntity(<xsl:apply-templates select="body/json" mode="body-as-json"/>, HttpEntity::JSON)</xsl:when>
			<xsl:when test="body/json">, new HttpEntity(<xsl:apply-templates select="body/json" mode="body-as-json"/>, HttpEntity::JSON)</xsl:when>
			<xsl:when test="body/patch">, new HttpEntity(json_encode(<xsl:value-of select="$body"/>->asJson()), HttpEntity::JSON)</xsl:when>
			<xsl:when test="body/(text|binary)">, new HttpEntity(<xsl:value-of select="$body"/>, HttpEntity::TEXT)</xsl:when>
		</xsl:choose>);
	}
</xsl:for-each>
	<xsl:value-of select='$additional-client-code'/>
	private static function boolean_param($val) {
		if ($val) return "true";
		else return "false";
	}

	private static function json_array($array, $transform) {
		$result = array();
		foreach ($array as $element) {
			$result[] = $transform($element);
		}
		return $result;
	}

	private static function json_map($map, $transform) {
		$result = array();
		foreach ($map as $key => $element) {
			$result[$key] = $transform($element);
		}
		return $result;
	}
}
			</api:file>
		</api:files>
	</xsl:template>

	<xsl:template match="route/text()">."<xsl:value-of select="."/>"</xsl:template>
	<xsl:template match="route/param">.<xsl:value-of select="rest4j:param-value(ancestor::endpoint, text())"/></xsl:template>

	<xsl:template match="json[@collection='singleton']" mode="body-as-json">json_encode($body->asJson())</xsl:template>
	<xsl:template match="json[@collection='array']" mode="body-as-json">self::json_array($body)</xsl:template>
	<xsl:template match="json[@collection='map']" mode="body-as-json">self::json_map($body)</xsl:template>

	<xsl:function name="rest4j:param-value">
		<xsl:param name="endpoint"/>
		<xsl:param name="name"/>
		<xsl:choose>
			<xsl:when test="index-of($common-param-set,$name)">$this-><xsl:value-of select="rest4j:phpIdentifier($name)"/></xsl:when>
			<xsl:when test="$endpoint/@client-param-object">$params-><xsl:value-of select="rest4j:phpIdentifier($name)"/></xsl:when>
			<xsl:otherwise>$<xsl:value-of select="rest4j:phpIdentifier($name)"/></xsl:otherwise>
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
			<xsl:when test="@client-param-object"><xsl:value-of select="concat($prefix,@client-param-object)"/> $params</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="rest4j:param-variables(.)">
					<xsl:if test="position()>1">,</xsl:if>
					<xsl:value-of select="*:type"/> $<xsl:value-of select="*:name"/>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="endpoint" mode="endpoint-request-class">
		<xsl:variable name="method-name"><xsl:apply-templates select="." mode="endpoint-method-name"/></xsl:variable>
		<xsl:value-of select="rest4j:camelCase(concat('_',$prefix), $method-name)"/>
	</xsl:template>

	<!--
	   Returns params/param/(type|name|doc|phpdoc-type) for all non-common API method parameters, including the body.
	  -->
	<xsl:function name="rest4j:param-variables">
		<xsl:param name="endpoint"></xsl:param>
		<xsl:variable name="body" select="$endpoint/body"/>
		<xsl:for-each select="$endpoint/parameters/parameter[not(fn:index-of($common-param-set,@name))]">
			<param>
				<type></type>
				<name><xsl:value-of select="rest4j:phpIdentifier(@name)"/></name>
				<doc><xsl:value-of select="rest4j:description(description)"/></doc>
				<phpdoc-type><xsl:value-of select="@type"/></phpdoc-type>
			</param>
		</xsl:for-each>
		<xsl:variable name="body-name-doc">
			<name>
				<xsl:choose>
					<xsl:when test="$body/@client-name"><xsl:value-of select="$body/@client-name"/></xsl:when>
					<xsl:otherwise>body</xsl:otherwise>
				</xsl:choose>
			</name>
			<doc><xsl:value-of select="rest4j:description($body/description)"/></doc>
		</xsl:variable>
		<xsl:variable name="prefixed-type" select="concat($prefix,$body/(json|patch)/@type)"/>
		<xsl:choose>
			<xsl:when test="$body/json/@collection='singleton' or $body/patch"><param><type><xsl:value-of select="$prefixed-type"/></type><phpdoc-type><xsl:value-of select="$prefixed-type"/></phpdoc-type><xsl:copy-of select="$body-name-doc"/></param></xsl:when>
			<xsl:when test="$body/json/@collection='array' or $body/json/@collection='map'"><param><type>array</type><phpdoc-type><xsl:value-of select="$prefixed-type"/>[]</phpdoc-type><xsl:copy-of select="$body-name-doc"/></param></xsl:when>
			<xsl:when test="$body/binary"><param><type></type><phpdoc-type>string</phpdoc-type><xsl:copy-of select="$body-name-doc"/></param></xsl:when>
			<xsl:when test="$body/text"><param><type></type><phpdoc-type>string</phpdoc-type><xsl:copy-of select="$body-name-doc"/></param></xsl:when>
		</xsl:choose>
	</xsl:function>

	<xsl:template match="simple|complex" mode="field-name">
		<xsl:if test="@client-name"><xsl:value-of select="@client-name"/></xsl:if>
		<xsl:if test="not(@client-name)"><xsl:value-of select="rest4j:phpIdentifier(@name)"/></xsl:if>
	</xsl:template>

	<xsl:template match="complex" mode="phpdoc-var-type">
	 * @var <xsl:value-of select="concat($prefix, @type)"/> <xsl:if test="@collection!='singleton'">[]</xsl:if>
	</xsl:template>

	<xsl:template match="text()" mode="route"><xsl:copy-of select="."/></xsl:template>
	<xsl:template match="param" mode="route">[<xsl:value-of select="."/>]</xsl:template>

	<xsl:function name="rest4j:phpIdentifier">
		<xsl:param name="str"/>
		<xsl:value-of select="rest4j:identifier($str, '')"/> <!-- keywords can be used as identifiers in PHP because of $ sign -->
	</xsl:function>

	<xsl:function name="rest4j:description">
		<xsl:param name="description"/>
		<xsl:value-of select="rest4j:javadocEscape($description/(*[not(@client-lang) or @client-lang='*' or contains('php,', concat(@client-lang,','))]|text())[name()!='html:title'])"/>
	</xsl:function>

	<xsl:template match="*|text()" mode="#all"></xsl:template>
</xsl:stylesheet>