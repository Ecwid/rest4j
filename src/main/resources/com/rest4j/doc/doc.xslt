<?xml version="1.0"?>
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				version="2.0"
				xmlns:api="http://rest4j.com/api-description"
				xmlns:fn="http://www.w3.org/2005/xpath-functions"
				xmlns:exslt="http://exslt.org/common"
				xmlns="http://www.w3.org/1999/xhtml"
				xmlns:html="http://www.w3.org/1999/xhtml"
				exclude-result-prefixes="#all"
				xpath-default-namespace="http://rest4j.com/api-description"
				>

	<xsl:param name="url"/>
	<xsl:param name="https-url"/>
	<xsl:param name="style"/>
	<xsl:param name="css"/>

	<xsl:template match="api">
		<api:files>
			<xsl:for-each select="endpoint">
				<api:file>
					<xsl:attribute name="name">
						<xsl:apply-templates select="." mode="filename"/>
					</xsl:attribute>
					<xsl:attribute name="service-name" select="service/@name"/>
					<xsl:attribute name="service-method" select="service/@method"/>
					<xsl:apply-templates select="."/>
				</api:file>
			</xsl:for-each>
			<xsl:if test="$style"><api:file name="style.css" copy-from="{$style}"/></xsl:if>
		</api:files>
	</xsl:template>

	<xsl:template match="endpoint" xml:space="preserve">
		<xsl:call-template name="page">
			<xsl:with-param name="title" select="description/html:title"/>
			<xsl:with-param name="navigation">
				<xsl:call-template name="navigation">
					<xsl:with-param name="current-file"><xsl:apply-templates select="." mode="filename"/></xsl:with-param>
				</xsl:call-template>
			</xsl:with-param>
			<xsl:with-param name="content">
				<div class="endpoint-description">
				<xsl:copy-of select="description/(*|text())[fn:name()!='html:title']"/>
				</div>
				<xsl:apply-templates select="." mode="request"/>

				<xsl:if test="parameters/parameter">
					<xsl:apply-templates select="parameters"/>
				</xsl:if>
				<xsl:if test="body">
					<section class="body"><h2>Request body</h2>
					<xsl:apply-templates select="body"/>
					</section>
				</xsl:if>
				<xsl:if test="response">
					<section class="response"><h2>Response</h2>
					<xsl:apply-templates select="response"/>
					</section>
				</xsl:if>
				<xsl:if test="errors/error">
					<xsl:apply-templates select="errors"/>
				</xsl:if>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="page">
		<xsl:param name="title"/>
		<xsl:param name="navigation"/>
		<xsl:param name="content"/>
		<html>
			<head>
				<meta http-equiv="content-type" content="text/html; charset=utf-8" />
				<title><xsl:value-of select="$title"/></title>
				<xsl:if test="$style"><link rel="stylesheet" type="text/css" href="style.css"/></xsl:if>
				<style>
					<xsl:value-of select="$css"/>
				</style>
				<xsl:comment>[if IE]>
				&lt;script src="http://html5shiv.googlecode.com/svn/trunk/html5.js">&lt;/script>
				&lt;![endif]</xsl:comment>
			</head>
			<body>
				<table class="page"><tr>
					<td class="nav-col"><xsl:copy-of select="$navigation"/></td>
					<td class="center-col"><article>
						<h1><xsl:value-of select="$title"/></h1>
						<xsl:copy-of select="$content"/>
						</article></td>
				</tr></table>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="endpoint" mode="request">
		<section class="request"><h2>URL</h2>
			<div class="url">
				<span class="method"><xsl:value-of select="@http"/></span><span class="path">
				<xsl:if test="@httpsonly='true'"><xsl:value-of select="$https-url"/></xsl:if>
				<xsl:if test="not(@httpsonly='true')"><xsl:value-of select="$url"/></xsl:if>
				<xsl:apply-templates select="route" mode="route"/></span>
				<xsl:variable name="queryParams" select="parameters/parameter[not(fn:index-of(../../route/param, @name))]"/>
				<xsl:if test="$queryParams">?<xsl:for-each select="$queryParams"><xsl:if test="fn:position() > 1">&#8203;&amp;</xsl:if><span class="query-param-name"><xsl:value-of select="@name"/></span>=<span class="query-param-value"><xsl:value-of select="@type"/></span></xsl:for-each>
				</xsl:if>
			</div>
		</section>
	</xsl:template>
	<xsl:template match="text()" mode="route">
		<xsl:copy-of select="."/>
	</xsl:template>

	<xsl:template match="param" mode="route"><span class="path-param"><xsl:value-of select="."/></span></xsl:template>

	<xsl:template match="parameters">
		<section class="params">
		<h2>URL Parameters</h2>
		<table>
			<tr><th class="param-name">Name</th><th class="param-type">Type</th><th class="param-description">Description</th></tr>
			<xsl:for-each select="parameter">
				<tr>
					<td class="param-name"><xsl:apply-templates select="." mode="param-name"/></td>
					<td class="param-type"><xsl:apply-templates select="." mode="param-type"/></td>
					<td class="param-description"><xsl:apply-templates select="." mode="param-description"/></td>
				</tr>
			</xsl:for-each>
		</table>
		<xsl:if test="parameter[@optional='false']">
			<ol class="notes">
			<li>Parameters marked with <span class="mandatory"/> are mandatory.</li>
			</ol>
		</xsl:if>
		</section>
	</xsl:template>

	<xsl:template match="parameter" mode="param-name">
		<span class="param-name"><xsl:value-of select="@name"/></span><xsl:if test="@optional='false'"><span class="mandatory"/></xsl:if>
	</xsl:template>

	<xsl:template match="parameter" mode="param-type">
		<span class="param-type"><xsl:value-of select="@type"/></span>
	</xsl:template>

	<xsl:template match="simple|complex" mode="param-name">
		<span class="field-name"><xsl:value-of select="@name"/></span><xsl:if test="@nullable='false' and not(@default) and not (../../@patch) and ../../../name()='body'"><span class="mandatory"/></xsl:if>
		<xsl:if test="@access='readonly'"><span class="readonly"/></xsl:if>
		<xsl:if test="@access='writeonly'"><span class="writeonly"/></xsl:if>
	</xsl:template>

	<xsl:template match="simple|complex" mode="param-type">
		<xsl:if test="@collection='array'">
			Array&lt;<xsl:call-template name="type"><xsl:with-param name="node" select="."/></xsl:call-template>&gt;
		</xsl:if>
		<xsl:if test="@collection='map'">
			Map&lt;string,<xsl:call-template name="type"><xsl:with-param name="node" select="."/></xsl:call-template>&gt;
		</xsl:if>
		<xsl:if test="@collection='singleton' or not(@collection)">
			<xsl:call-template name="type"><xsl:with-param name="node" select="."/></xsl:call-template>
		</xsl:if>
		<xsl:if test="@nullable='true'"><span class="nullable"/></xsl:if>
	</xsl:template>

	<xsl:template name="type">
		<xsl:param name="node"/>
		<xsl:if test="fn:name($node)='complex'">
			<a>
				<xsl:attribute name="href">#<xsl:call-template name="model-link">
					<xsl:with-param name="name" select="$node/@type"/>
					<xsl:with-param name="patch" select="$node/../../@patch and ($node/@collection != 'array' or not($node/@collection))"/>
				</xsl:call-template></xsl:attribute>
				<xsl:attribute name="complex-type" select="$node/@type"/>
				<xsl:value-of select="$node/@type"/>
			</a>
		</xsl:if>
		<xsl:if test="fn:name($node) != 'complex'">
			<span class="simple-type"><xsl:value-of select="$node/@type"/></span>
		</xsl:if>
	</xsl:template>

	<xsl:template match="parameter|simple|complex" mode="param-description">
		<xsl:copy-of select="description/(*|text())"/>
		<xsl:if test="values and not(values/value/@description)">
			<div class="one-of">One of <xsl:for-each select="values/value"><xsl:if test="position()!=1">, </xsl:if>'<xsl:value-of select="."/>'</xsl:for-each></div>
		</xsl:if>
		<xsl:if test="values/value/@description">
			<div class="one-of-following">One of the following:</div>
			<table class="values">
				<xsl:for-each select="values/value">
					<tr><td class="value-value">'<xsl:value-of select="."/>'</td><td class="value-description"><xsl:value-of select="@description"/></td></tr>
				</xsl:for-each>
			</table>
		</xsl:if>
		<xsl:if test="@default!='' and (not(ancestor::model[@patch]) and ancestor::body or name()='parameter')">
			<!-- Defaults are only required for input objects and not patches, or parameters -->
			<div class="default">Default is <xsl:value-of select="@default"/></div>
		</xsl:if>
		<xsl:if test="@httpsonly='true'">
			<div class="https-warning">You should use HTTPS to pass this parameter.</div>
		</xsl:if>
	</xsl:template>

	<xsl:template match="body|response">
		<xsl:variable name="a">
			A<xsl:if test="(json|patch)/@optional = 'true'">n optional</xsl:if>
		</xsl:variable>
		<xsl:if test="(json|patch)[@collection='singleton' or not(@collection)]"><xsl:value-of select="$a"/> JSON object of type '<xsl:value-of select="(json|patch)/@type"/>' with the following fields:</xsl:if>
		<xsl:if test="(json|patch)[@collection='array']"><xsl:value-of select="$a"/> JSON array of objects of type '<xsl:value-of select="(json|patch)/@type"/>' with the following fields:</xsl:if>
		<xsl:if test="(json|patch)[@collection='map']"><xsl:value-of select="$a"/> JSON object whose property values are JSON objects of type '<xsl:value-of select="(json|patch)/@type"/>' with the following fields:</xsl:if>
		<xsl:if test="binary">Binary data.</xsl:if>
		<xsl:if test="text">Plain text in UTF-8.</xsl:if>
		<xsl:apply-templates select="model"/>
	</xsl:template>

	<xsl:template match="model">
		<section class="model">
			<xsl:attribute name="model-name" select="@name"/>
			<xsl:if test="@patch"><xsl:attribute name="patch" select="@patch"/></xsl:if>
			<a>
				<xsl:attribute name="name"><xsl:call-template name="model-link">
					<xsl:with-param name="name" select="@name"/>
					<xsl:with-param name="patch" select="@patch"/>
				</xsl:call-template></xsl:attribute>
			</a>
			<h3>
				<xsl:value-of select="@name"/>
				<!-- if there is an ambiguity between Type and Type_patch-->
				<xsl:variable name="name" select="@name"/>
				<xsl:if test="@patch and fn:count(../model[@name=$name]) > 1"> (patch)</xsl:if>
			</h3>
			<table>
				<tr><th class="param-name">Field</th><th class="param-type">Type</th><th class="param-description">Description</th></tr>
				<xsl:for-each select="fields/(simple|complex)[ancestor::body and not(@access='readonly') or ancestor::response and not(@access='writeonly') or not(ancestor::body) and not(ancestor::response)]">
					<tr>
						<xsl:attribute name="field-name" select="@name"/>
						<td class="param-name"><xsl:apply-templates select="." mode="param-name"/></td>
						<td class="param-type"><xsl:apply-templates select="." mode="param-type"/></td>
						<td class="param-description"><xsl:apply-templates select="." mode="param-description"/></td>
					</tr>
				</xsl:for-each>
			</table>
			<ol class="notes">
			<xsl:if test="not(@patch) and fields/*[@nullable='false' and not(@default)] and ../name()='body'">
				<li>Fields marked with <span class="mandatory"/> are mandatory.</li>
			</xsl:if>
			<xsl:if test="@patch">
				<li>All fields are optional. Missing fields do not affect existing field values</li>
				<xsl:if test="fields/*[@nullable='true']">
				<li>Some fields can be explicitly set to null using the {"field":null} syntax, in which case they are marked as <span class="nullable"/>.</li>
				</xsl:if>
			</xsl:if>
			<xsl:if test="not(@patch)">
				<xsl:if test="fields/*[@nullable='true']">
					<li>Some fields can have null value, in which case they are marked as <span class="nullable"/>.</li>
				</xsl:if>
			</xsl:if>
			</ol>
		</section>
	</xsl:template>

	<xsl:template name="model-link">
		<xsl:param name="name"/>
		<xsl:param name="patch"/>
		<xsl:value-of select="$name"/><xsl:if test="$patch">_patch</xsl:if>
	</xsl:template>

	<xsl:key name="models" match="/api/model" use="@name"/>

	<xsl:template match="errors">
		<section class="errors"><h2>Errors</h2>
		<table class="errors">
			<tr><th>HTTP Status</th><th>Response JSON</th><th>Description</th></tr>
			<xsl:apply-templates select="error"/>
		</table>
		<xsl:apply-templates select="fn:key('models',error/@type)"/>
		</section>
	</xsl:template>
	
	<xsl:template match="error">
		<tr>
			<td class="error-status"><xsl:value-of select="@status"/></td>
			<td class="error-type">
				<xsl:if test="@type">
					<a>
						<xsl:attribute name="href">#<xsl:call-template name="model-link">
							<xsl:with-param name="name" select="@type"/>
							<xsl:with-param name="patch" select="false()"/>
						</xsl:call-template></xsl:attribute>
						<xsl:attribute name="complex-type" select="@type"/>
						<xsl:value-of select="@type"/>
					</a>
				</xsl:if>
			</td>
			<td class="error-description"><xsl:copy-of select="description/(*|text())"/></td>
		</tr>	
	</xsl:template>

	<xsl:template name="navigation">
		<xsl:param name="current-file"/>
		<nav class="navigation">
			<xsl:choose>
			<xsl:when test="//endpoint/extra/group">
				<!-- use groupped list -->
				<xsl:for-each-group select="//endpoint" group-by="extra/group">
					<xsl:call-template name="nav-group">
						<xsl:with-param name="group-name" select="extra/group"/>
					</xsl:call-template>
					<xsl:for-each select="current-group()">
						<xsl:call-template name="endpoint-link">
							<xsl:with-param name="current-file" select="$current-file"/>
							<xsl:with-param name="endpoint" select="."/>
						</xsl:call-template>
					</xsl:for-each>
				</xsl:for-each-group>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="//endpoint">
					<xsl:call-template name="endpoint-link">
						<xsl:with-param name="current-file" select="$current-file"/>
						<xsl:with-param name="endpoint" select="."/>
					</xsl:call-template>
				</xsl:for-each>
			</xsl:otherwise>
			</xsl:choose>
		</nav>
	</xsl:template>

	<xsl:template name="nav-group">
		<xsl:param name="group-name"/>
		<div class="nav-group" group-name="{$group-name}"><span><span><xsl:value-of select="$group-name"/></span></span></div>
	</xsl:template>
	
	<xsl:template name="endpoint-link">
		<xsl:param name="current-file"/>
		<xsl:param name="endpoint"/>
		<xsl:call-template name="nav-link">
			<xsl:with-param name="current-file" select="$current-file"/>
			<xsl:with-param name="title" select="$endpoint/description/html:title"/>
			<xsl:with-param name="filename"><xsl:apply-templates select="$endpoint" mode="filename"/></xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="nav-link">
		<xsl:param name="current-file"/>
		<xsl:param name="title"/>
		<xsl:param name="filename"/>
		<li>
			<xsl:attribute name="file-name" select="$filename"/>
			<xsl:choose>
				<xsl:when test="$filename=$current-file">
					<xsl:value-of select="$title"/>
				</xsl:when>
				<xsl:otherwise>
					<a>
						<xsl:attribute name="href" select="$filename"/>
						<xsl:value-of select="$title"/>
					</a>
				</xsl:otherwise>
			</xsl:choose>
		</li>
	</xsl:template>

	<xsl:template match="endpoint" mode="filename"><xsl:value-of select="service/@name"/>.<xsl:value-of select="service/@method"/>.html</xsl:template>

	<xsl:template match="*"/>

</xsl:stylesheet>
