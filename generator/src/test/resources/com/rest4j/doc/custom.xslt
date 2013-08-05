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
				exclude-result-prefixes="#all"
				xpath-default-namespace="http://www.w3.org/1999/xhtml"
		>
	<xsl:import href="doc.xslt"/>

	<xsl:template match="api:top">
		<xsl:apply-templates select="api:files"/>
	</xsl:template>

	<xsl:template match="nav">
		<xsl:copy>
			<xsl:apply-templates select="*|@*|text()"/>
			<xsl:call-template name="nav-group">
				<xsl:with-param name="group-name">Other</xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="nav-link">
				<xsl:with-param name="current-file" select="ancestor::api:file/@name"/>
				<xsl:with-param name="title">XXX</xsl:with-param>
				<xsl:with-param name="filename">xxx.html</xsl:with-param>
			</xsl:call-template>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="*|@*">
		<xsl:copy>
			<xsl:apply-templates select="*|@*|text()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>