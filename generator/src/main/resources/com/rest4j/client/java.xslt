<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE stylesheet [
		<!ENTITY apache-copyright SYSTEM "apache-copyright.inc">
		<!ENTITY JsonUtil SYSTEM "java/JsonUtil.inc">
		<!ENTITY JsonArrayList SYSTEM "java/JsonArrayList.inc">
		<!ENTITY JsonElementFactory SYSTEM "java/JsonElementFactory.inc">
		<!ENTITY JsonObjectMap SYSTEM "java/JsonObjectMap.inc">
		<!ENTITY Request SYSTEM "java/Request.inc">
		<!ENTITY HasContentLength SYSTEM "java/HasContentLength.inc">
		<!ENTITY HasContentType SYSTEM "java/HasContentType.inc">
]>
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
	<xsl:param name="package" select="'api'"/>
	<xsl:param name="common-params" select="''"/>
	<xsl:param name="copyright">&apache-copyright;</xsl:param>
	<xsl:param name="group-id" select="$package"/>
	<xsl:param name="artifact-id" select="'java-client'"/>
	<xsl:param name="version" select="'1.0-SNAPSHOT'"/>
	<xsl:param name="project-url" select="'use the project-url parameter to set the project URL.'"/>
	<xsl:param name="description" select="'use the deacirption parameter to set the project description.'"/>
	<xsl:param name="name" select="'use the name parameter to set the project name.'"/>
	<xsl:param name="developer-name"/>
	<xsl:param name="developer-email"/>
	<xsl:param name="license-name" select="'The Apache Software License, Version 2.0'"/>
	<xsl:param name="license-url" select="'http://www.apache.org/licenses/LICENSE-2.0.txt'"/>

	<xsl:variable name="common-param-set" select="fn:tokenize($common-params,' ')"/>

	<xsl:template match="api">
		<api:files>
			<xsl:for-each select="model">
				<api:file>
					<xsl:attribute name="name">src/main/java/<xsl:value-of select="fn:replace($package,'\.','/')"/>/model/<xsl:value-of select="@name"/>.java</xsl:attribute>
					<xsl:attribute name="text">on</xsl:attribute>/*
<xsl:value-of select="$copyright"/>*/
package <xsl:value-of select="$package"/>.model;
import java.util.*;
import org.json.*;
import <xsl:value-of select="$package"/>.util.*;

public class <xsl:value-of select="@name"/> {
    private JSONObject object;
    public <xsl:value-of select="@name"/>() { object = new JSONObject(); }
    public <xsl:value-of select="@name"/>(JSONObject json) { object = json; }
<xsl:for-each select="fields/(complex|simple)" xml:space="preserve">
    /**
     * @return <xsl:value-of select="rest4j:javadocEscape(description/(*|text()))"/>
     */
    public <xsl:apply-templates select="." mode="prop-type"/> <xsl:value-of select="rest4j:camelCase('get',@name)"/>() {
        Object val = object.opt(<xsl:value-of select="rest4j:quote(@name)"/>);
        return val == null ? null : <xsl:apply-templates select="." mode="prop-cast"/>;
    }

    /**
     * Sets the <xsl:value-of select="rest4j:quote(@name)"/> property value. Properties that are not set will not be
     * present in the JSON. Properties that are set to null will be passed as null values in JSON.
     */
    public void <xsl:value-of select="rest4j:camelCase('set',@name)"/>(<xsl:apply-templates select="." mode="prop-type"/> val) {
        try {
            object.put(<xsl:value-of select="rest4j:quote(@name)"/>, <xsl:apply-templates select="." mode="prop-json"/>);
        } catch (JSONException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
</xsl:for-each>
    /**
     * @return A JSON representation of the object. Changes to the returned object affect this object.
     */
    public JSONObject asJson() { return object; }

    /**
     * @return True if the property named propName was set using an appropriate setter.
     */
    public boolean hasProp(String key) { return object.has(key); }

    public String toString() { return object.toString(); }

    public int hashCode() { return object.hashCode(); }

    public boolean equals(Object o) {
        if (!(o instanceof <xsl:value-of select="@name"/>)) return false;
        return object.equals(((<xsl:value-of select="@name"/>)o).object);
    }
}</api:file>
			</xsl:for-each>
			<api:file>
				<xsl:attribute name="name">src/main/java/<xsl:value-of select="fn:replace($package,'\.','/')"/>/Client.java</xsl:attribute>
				<xsl:attribute name="text">on</xsl:attribute>/*
<xsl:value-of select="$copyright"/>*/
package <xsl:value-of select="$package"/>;

import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.HttpEntity;
import org.apache.http.entity.*;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpResponse;
import java.net.URISyntaxException;
import <xsl:value-of select="$package"/>.model.*;
import <xsl:value-of select="$package"/>.util.*;
import java.util.*;
import java.io.IOException;
import org.json.*;

public class Client {
    final HttpClient client;
    String url = "<xsl:value-of select='$url'/>";
<xsl:if test="$https-url">    String secureUrl = "<xsl:value-of select='$https-url'/>";
</xsl:if>
<xsl:variable name="doc" select="/"/>
<xsl:for-each select="$common-param-set">
	<xsl:variable name='param-name' select='.'/>
	<xsl:text xml:space="preserve">    </xsl:text> <!-- indent -->
	<xsl:call-template name='param-type'>
		<xsl:with-param name="type" select="$doc/api/endpoint/parameters/parameter[@name=$param-name]/@type[1]"/>
	</xsl:call-template>
	<xsl:value-of select="concat(' ', rest4j:paramNameAsIdentifier($param-name))"/>;
</xsl:for-each>
    public Client() {
        client = new DefaultHttpClient();
    }

    public Client(HttpClient client) {
        this.client = client;
    }

    /**
     * Sets the REST API endpoint URL. The default is "<xsl:value-of select='$url'/>".
     */
    public Client setUrl(String url) throws URISyntaxException {
        new URIBuilder(url);
        this.url = url;
        return this;
    }
<xsl:if test="$https-url">
    /**
     * Sets the HTTPS REST API endpoint URL. The default is "<xsl:value-of select='$https-url'/>".
     */
    public Client setSecureUrl(String url) throws URISyntaxException {
        new URIBuilder(url);
        this.secureUrl = url;
        if (this.url == null) this.url = url;
        return this;
    }
</xsl:if>
<!-- setters for common params -->
<xsl:for-each select="$common-param-set" xml:space="preserve">
	<xsl:variable name='param-name' select='.'/>
	<xsl:if test="not($doc/api/endpoint/parameters/parameter[@name=$param-name])"><xsl:value-of select="error(fn:QName('http://rest4j.com/','PARAM-NOT-FOUND'),concat('Parameter not found: ', $param-name))"/></xsl:if>
    /**
     * Sets the value of the "<xsl:value-of select="$param-name"/>" request parameter for subsequent requests.
     * <xsl:value-of select="rest4j:javadocEscape(($doc/api/endpoint/parameters/parameter[@name=$param-name]/description)[1]/(*[not(@client-lang) or @client-lang='*' or contains('java,', concat(@client-lang,','))]|text()))"/>
     */
    public Client <xsl:value-of select="rest4j:camelCase('set',$param-name)"/>(
	<xsl:call-template name='param-type'>
		<xsl:with-param name="type" select="$doc/api/endpoint/parameters/parameter[@name=$param-name]/@type[1]"/>
	</xsl:call-template> <xsl:value-of select="rest4j:paramNameAsIdentifier($param-name)"/>) {
        this.<xsl:value-of select="rest4j:paramNameAsIdentifier($param-name)"/> = <xsl:value-of select="rest4j:paramNameAsIdentifier($param-name)"/>;
        return this;
    }
</xsl:for-each>
<xsl:for-each select="endpoint">
	<xsl:apply-templates select='.' mode='endpoint-method'/>
</xsl:for-each>
<xsl:if test="endpoint/body/binary">
    private InputStreamEntity createInputStreamEntity(java.io.InputStream body) {
        String contentType = body instanceof HasContentType ? ((HasContentType)body).getContentType() : "application/octet-stream";
        long contentLength = body instanceof HasContentLength ? ((HasContentLength)body).getContentLength() : -1;
        InputStreamEntity ise = new InputStreamEntity(body, contentLength);
        if (contentType != null) ise.setContentType(contentType);
        return ise;
	}
</xsl:if>
}
			</api:file>
			<api:file name="pom.xml" xml:space='preserve'>
<project xmlns="http://maven.apache.org/POM/4.0.0"
		 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId><xsl:value-of select="$group-id"/></groupId>
	<artifactId><xsl:value-of select="$artifact-id"/></artifactId>
	<version><xsl:value-of select="$version"/></version>

	<packaging>jar</packaging>

	<parent>
		<groupId>org.sonatype.oss</groupId>
		<artifactId>oss-parent</artifactId>
		<version>7</version>
	</parent>
	<url><xsl:value-of select="$project-url"/></url>
	<description><xsl:value-of select="$description"/></description>
	<name><xsl:value-of select="$name"/></name>
	<licenses>
		<license>
			<name><xsl:value-of select="$license-name"/></name>
			<url><xsl:value-of select="$license-url"/></url>
			<distribution>repo</distribution>
		</license>
	</licenses>
	<xsl:if test="$developer-name and $developer-email">
	<developers>
		<developer>
			<name><xsl:value-of select="$developer-name"/></name>
			<email><xsl:value-of select="$developer-email"/></email>
			<roles>
				<role>architect</role>
				<role>developer</role>
			</roles>
		</developer>
	</developers>
	</xsl:if>
	<dependencies>
		<dependency>
			<groupId>org.json</groupId>
			<artifactId>json</artifactId>
			<version>20090211</version>
		</dependency>
		<dependency>
			<groupId>org.apache.httpcomponents</groupId>
			<artifactId>httpclient</artifactId>
			<version>4.2.2</version>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-gpg-plugin</artifactId>
				<executions>
					<execution>
						<id>sign-artifacts</id>
						<phase>verify</phase>
						<goals>
							<goal>sign</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-source-plugin</artifactId>
				<executions>
					<execution>
						<id>attach-sources</id>
						<goals>
							<goal>jar</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-javadoc-plugin</artifactId>
				<executions>
					<execution>
						<id>attach-javadocs</id>
						<goals>
							<goal>jar</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
</project>
			</api:file>

			<!-- Utility files -->
			<api:file name="src/main/java/{replace($package,'\.','/')}/util/JsonArrayList.java" text="on">&JsonArrayList;</api:file>
			<api:file name="src/main/java/{replace($package,'\.','/')}/util/JsonElementFactory.java" text="on">&JsonElementFactory;</api:file>
			<api:file name="src/main/java/{replace($package,'\.','/')}/util/JsonObjectMap.java" text="on">&JsonObjectMap;</api:file>
			<api:file name="src/main/java/{replace($package,'\.','/')}/util/JsonUtil.java" text="on">&JsonUtil;</api:file>
			<api:file name="src/main/java/{replace($package,'\.','/')}/Request.java" text="on">&Request;</api:file>
			<api:file name="src/main/java/{replace($package,'\.','/')}/HasContentLength.java" text="on">&HasContentLength;</api:file>
			<api:file name="src/main/java/{replace($package,'\.','/')}/HasContentType.java" text="on">&HasContentType;</api:file>
		</api:files>
	</xsl:template>

	<xsl:template match="*[@collection='singleton']" mode="prop-type"><xsl:apply-templates select='.' mode='prop-singleton-type'/></xsl:template>
	<xsl:template match="*[@collection='array']" mode="prop-type">List&lt;<xsl:apply-templates select='.' mode='prop-singleton-type'/>&gt;</xsl:template>
	<xsl:template match="*[@collection='map']" mode="prop-type">Map&lt;String,<xsl:apply-templates select='.' mode='prop-singleton-type'/>&gt;</xsl:template>

	<xsl:template match="complex" mode="prop-singleton-type">
		<xsl:value-of select="@type"/>
	</xsl:template>
	<xsl:template match="simple[@type='number']" mode="prop-singleton-type">Number</xsl:template>
	<xsl:template match="simple[@type='string']" mode="prop-singleton-type">String</xsl:template>
	<xsl:template match="simple[@type='boolean']" mode="prop-singleton-type">Boolean</xsl:template>
	<xsl:template match="simple[@type='date']" mode="prop-singleton-type">Date</xsl:template>
	<xsl:template match="simple" mode="prop-singleton-type">Object</xsl:template>

	<xsl:template match="*[@collection='singleton']" mode="prop-cast"><xsl:apply-templates select='.' mode='prop-singleton-cast'/></xsl:template>
	<xsl:template match="*[@collection='array']" mode="prop-cast">val == JSONObject.NULL ? null : new JsonArrayList&lt;<xsl:apply-templates select='.' mode='prop-singleton-type'/>&gt;((JSONArray)val) {
            protected <xsl:apply-templates select='.' mode='prop-singleton-type'/> read(Object val) { return <xsl:apply-templates select='.' mode='prop-singleton-cast'/>; }
            protected Object write(<xsl:apply-templates select='.' mode='prop-singleton-type'/> val) { return <xsl:apply-templates select='.' mode='prop-singleton-json'/>; }
        }</xsl:template>
	<xsl:template match="*[@collection='map']" mode="prop-cast">new JsonObjectMap&lt;<xsl:apply-templates select='.' mode='prop-singleton-type'/>&gt;((JSONObject)val) {
            protected <xsl:apply-templates select='.' mode='prop-singleton-type'/> read(Object val) { return <xsl:apply-templates select='.' mode='prop-singleton-cast'/>; }
            protected Object write(<xsl:apply-templates select='.' mode='prop-singleton-type'/> val) { return <xsl:apply-templates select='.' mode='prop-singleton-json'/>; }
        }</xsl:template>

	<xsl:template match="complex" mode="prop-singleton-cast">val == JSONObject.NULL ? null : new <xsl:value-of select="@type"/>((JSONObject)val)</xsl:template>
	<xsl:template match="simple[@type='number']" mode="prop-singleton-cast">JsonUtil.asNumber(val)</xsl:template>
	<xsl:template match="simple[@type='string']" mode="prop-singleton-cast">JsonUtil.asString(val)</xsl:template>
	<xsl:template match="simple[@type='boolean']" mode="prop-singleton-cast">JsonUtil.asBoolean(val)</xsl:template>
	<xsl:template match="simple[@type='date']" mode="prop-singleton-cast">JsonUtil.asDate(val)</xsl:template>
	<xsl:template match="simple" mode="prop-singleton-cast">val == JSONObject.NULL? null : val</xsl:template>

	<xsl:template match="*[@collection='singleton']" mode="prop-json"><xsl:apply-templates select='.' mode='prop-singleton-json'/></xsl:template>
	<xsl:template match="*[@collection='array']" mode="prop-json">JsonUtil.asJsonArray(val, new JsonElementFactory&lt;<xsl:apply-templates select='.' mode='prop-singleton-type'/>&gt;() {
            public Object json(<xsl:apply-templates select='.' mode='prop-singleton-type'/> val) { return <xsl:apply-templates select='.' mode='prop-singleton-json'/>; }
        })</xsl:template>
	<xsl:template match="*[@collection='map']" mode="prop-json">JsonUtil.asJsonMap(val, new JsonElementFactory&lt;<xsl:apply-templates select='.' mode='prop-singleton-type'/>&gt;() {
            public Object json(<xsl:apply-templates select='.' mode='prop-singleton-type'/> val) { return <xsl:apply-templates select='.' mode='prop-singleton-json'/>; }
        })</xsl:template>

	<xsl:template match="complex" mode="prop-singleton-json">val == null? JSONObject.NULL : val.asJson()</xsl:template>
	<xsl:template match="simple" mode="prop-singleton-json">JsonUtil.asJsonSingleton(val)</xsl:template>

	<xsl:template match="endpoint" mode="endpoint-method">
    /**<xsl:choose>
		<xsl:when test="description/html:title">
     * Builds Request object for the "<xsl:value-of select="rest4j:javadocEscape(description/html:title/(*|text()))"/>" request.
		</xsl:when>
		<xsl:otherwise>
     * Builds Request object for the "<code><xsl:value-of select="@http"/><xsl:text> </xsl:text><xsl:apply-templates select="route" mode="route"/></code>" request.
		</xsl:otherwise>
	</xsl:choose>
     * To actually execute the request, call the execute() method on the returned object.&lt;p/&gt;
     * <xsl:value-of select="rest4j:javadocEscape(description/(*[not(@client-lang) or @client-lang='*' or contains('java,', concat(@client-lang,','))]|text())[name()!='html:title'])"/>
<xsl:for-each select="parameters/parameter[not(index-of($common-param-set,@name))]" xml:space="preserve">
     * @param <xsl:value-of select="@name"/> <xsl:value-of select="rest4j:javadocEscape(description/(*[not(@client-lang) or @client-lang='*' or contains('java,', concat(@client-lang,','))]|text()))"/>
</xsl:for-each>
     * @return A Request&amp;lt;<xsl:apply-templates select='.' mode="endpoint-result-type"/>&amp;gt; object that can be executed later.
     */
    public Request&lt;<xsl:apply-templates select='.' mode="endpoint-result-type"/>&gt; <xsl:apply-templates select='.' mode="endpoint-method-name"/>(<xsl:apply-templates select='.' mode="endpoint-method-params"/>) {
        try {
            URIBuilder builder = new URIBuilder(<xsl:if test="rest4j:secure(.)='true'">secureUrl</xsl:if><xsl:if test="rest4j:secure(.)='false'">url</xsl:if><xsl:apply-templates select="route/(param|text())"/>);
            <xsl:for-each select="parameters/parameter[@optional='true' and rest4j:path-param(ancestor::endpoint,@name)='false']">if (<xsl:value-of select="rest4j:paramNameAsIdentifier(@name)"/> != null) builder.setParameter("<xsl:value-of select="@name"/>", <xsl:value-of select="rest4j:paramNameAsIdentifier(@name)"/>.toString());
            </xsl:for-each>
            <xsl:for-each select="parameters/parameter[@optional='false']">if (<xsl:value-of select="rest4j:paramNameAsIdentifier(@name)"/> == null) throw new IllegalArgumentException("No parameter <xsl:value-of select="@name"/> is set");
            <xsl:if test="rest4j:path-param(ancestor::endpoint,@name)='false'">builder.setParameter("<xsl:value-of select="@name"/>", <xsl:value-of select="rest4j:paramNameAsIdentifier(@name)"/>.toString());</xsl:if>
            </xsl:for-each>

            <xsl:if test="body and not(body/json/@optional='true')">if (body == null) throw new IllegalArgumentException("No request body");</xsl:if>
            return new Request&lt;<xsl:apply-templates select='.' mode="endpoint-result-type"/>&gt;(builder.build()<xsl:choose>
		<xsl:when test="body/json/@optional='true'">, body == null ? null : new StringEntity(<xsl:apply-templates select="body/json" mode="body-as-json"/>, ContentType.APPLICATION_JSON)</xsl:when>
		<xsl:when test="body/json">, new StringEntity(<xsl:apply-templates select="body/json" mode="body-as-json"/>, ContentType.APPLICATION_JSON)</xsl:when>
		<xsl:when test="body/patch">, new StringEntity(body.asJson().toString(), ContentType.APPLICATION_JSON)</xsl:when>
		<xsl:when test="body/text">, new StringEntity(body, ContentType.TEXT_PLAIN)</xsl:when>
		<xsl:when test="body/binary">, createInputStreamEntity(body)</xsl:when>
	</xsl:choose>) {
                @Override
                public <xsl:apply-templates select='.' mode="endpoint-result-type"/> execute() throws IOException, JSONException {
                    <xsl:choose>
						<xsl:when test="@http='GET'">HttpGet method = new HttpGet(uri);</xsl:when>
						<xsl:when test="@http='PUT'">HttpPut method = new HttpPut(uri); method.setEntity(body);</xsl:when>
						<xsl:when test="@http='POST'">HttpPost method = new HttpPost(uri); method.setEntity(body);</xsl:when>
						<xsl:when test="@http='DELETE'">HttpDelete method = new HttpDelete(uri);</xsl:when>
					</xsl:choose>
                    try {
                        HttpResponse response = client.execute(method);
                        if (response.getStatusLine().getStatusCode() >= 400) {
                            throw new IOException("Unexpected HTTP status: "+response.getStatusLine());
                        }
                        HttpEntity entity = response.getEntity();
                        <xsl:choose>
							<xsl:when test="response/json/@collection='array'">if (entity == null)<xsl:if test="response/json/@optional='false'"> throw new JSONException("No response. Expected JSON array.");</xsl:if><xsl:if test="response/json/@optional='true'"> return null;</xsl:if>
                        JSONArray list = new JSONArray(EntityUtils.toString(entity));
                        List&lt;<xsl:value-of select='response/json/@type'/>&gt; result = new ArrayList&lt;<xsl:value-of select='response/json/@type'/>&gt;(list.length());
                        for (int i=0; i &lt; list.length(); i++) {
                            result.add(new <xsl:value-of select='response/json/@type'/>(list.getJSONObject(i)));
                        }
                        return result;</xsl:when>
							<xsl:when test="response/json">if (entity == null)<xsl:if test="response/json/@optional='false'"> throw new JSONException("No response. Expected JSON object.");</xsl:if><xsl:if test="response/json/@optional='true'"> return null;</xsl:if>
                        return new <xsl:value-of select='response/json/@type'/>(new JSONObject(EntityUtils.toString(entity)));
							</xsl:when>
							<xsl:when test="response/text">if (entity == null) throw new JSONException("No response. Expected text output.");
                        return EntityUtils.toString(entity);
							</xsl:when>
							<xsl:when test="response/binary">if (entity == null) throw new JSONException("No response. Expected binary output.");
                        return entity.getContent();
							</xsl:when>
							<xsl:otherwise>return null;</xsl:otherwise>
						</xsl:choose>
                    } finally {
                        method.releaseConnection();
                    }
                }
            };
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
	</xsl:template>

	<xsl:template match="route/text()">+"<xsl:value-of select="."/>"</xsl:template>
	<xsl:template match="route/param">+<xsl:value-of select="rest4j:paramNameAsIdentifier(text())"/></xsl:template>

	<xsl:function name="rest4j:path-param">
		<xsl:param name="endpoint"/>
		<xsl:param name="name"/>
		<xsl:value-of select="count($endpoint/route/param[text()=$name])>0"/>
	</xsl:function>

	<xsl:function name="rest4j:secure">
		<xsl:param name="endpoint"/>
		<xsl:value-of select="$https-url and ($endpoint/@httpsonly='true' or $endpoint/parameters/parameter[@httpsonly='true'])"/>
	</xsl:function>

	<xsl:template match="json[@collection='singleton']" mode="body-as-json">body.asJson().toString()</xsl:template>

	<xsl:template match="text()" mode="route"><xsl:copy-of select="."/></xsl:template>
	<xsl:template match="param" mode="route">[<xsl:value-of select="."/>]</xsl:template>

	<xsl:template match="json[@collection='array']" mode="body-as-json">JsonUtil.asJsonArray(body, new JsonElementFactory&lt;<xsl:value-of select="@type"/>&gt;() {
               public Object json(<xsl:value-of select="@type"/> element) { return element == null ? JSONObject.NULL : element.asJson(); }
           }).toString()</xsl:template>
	<xsl:template match="json[@collection='map']" mode="body-as-json">JsonUtil.asJsonMap(body, new JsonElementFactory&lt;<xsl:value-of select="@type"/>&gt;() {
               public Object json(<xsl:value-of select="@type"/> element) { return element == null ? JSONObject.NULL : element.asJson(); }
           }).toString()</xsl:template>

	<xsl:template match="endpoint[response/json/@collection='singleton']" mode="endpoint-result-type"><xsl:value-of select="response/json/@type"/></xsl:template>
	<xsl:template match="endpoint[response/json/@collection='array']" mode="endpoint-result-type">List&lt;<xsl:value-of select="response/json/@type"/>&gt;</xsl:template>
	<xsl:template match="endpoint[response/json/@collection='map']" mode="endpoint-result-type">Map&lt;String,<xsl:value-of select="response/json/@type"/>&gt;</xsl:template>
	<xsl:template match="endpoint[response/binary]" mode="endpoint-result-type">java.io.InputStream</xsl:template>
	<xsl:template match="endpoint[response/text]" mode="endpoint-result-type">String</xsl:template>
	<xsl:template match="endpoint" mode="endpoint-result-type">Void</xsl:template>

	<xsl:template match="endpoint[@client-method-name]" mode="endpoint-method-name"><xsl:value-of select="@client-method-name"/></xsl:template>
	<xsl:template match="endpoint" mode="endpoint-method-name"><xsl:value-of select="rest4j:camelCase(service/@method,rest4j:singular(service/@name))"/></xsl:template>
	<xsl:template match="endpoint" mode="endpoint-method-params">
		<xsl:variable name="params">
			<xsl:for-each select="parameters/parameter[not(fn:index-of($common-param-set,@name))]" xml:space='preserve'><xxx><xsl:call-template name='param-type'><xsl:with-param name="type" select="@type"/></xsl:call-template> <xsl:value-of select="rest4j:paramNameAsIdentifier(@name)"/></xxx>
			</xsl:for-each>
			<xsl:choose>
				<xsl:when test="body/json/@collection='singleton' or body/patch"><xxx><xsl:value-of select="body/(json|patch)/@type"/> body</xxx></xsl:when>
				<xsl:when test="body/json/@collection='array'"><xxx>List&lt;<xsl:value-of select="body/json/@type"/>&gt; body</xxx></xsl:when>
				<xsl:when test="body/json/@collection='map'"><xxx>Map&lt;String,<xsl:value-of select="body/json/@type"/>&gt; body</xxx></xsl:when>
				<xsl:when test="body/binary"><xxx>java.io.InputStream body</xxx></xsl:when>
				<xsl:when test="body/text"><xxx>String body</xxx></xsl:when>
			</xsl:choose>
		</xsl:variable>
		<xsl:value-of select="string-join($params/*:xxx,', ')"/>
	</xsl:template>

	<xsl:template name="param-type">
		<xsl:param name="type"/>
		<xsl:choose>
			<xsl:when test="$type='number'">Number</xsl:when>
			<xsl:when test="$type='string'">String</xsl:when>
			<xsl:when test="$type='boolean'">Boolean</xsl:when>
			<xsl:when test="$type='date'">java.util.Date</xsl:when>
			<xsl:otherwise><xsl:value-of select="error(fn:QName('http://rest4j.com/','TYPE'),'unexpected &lt;parameter&gt; type')"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>