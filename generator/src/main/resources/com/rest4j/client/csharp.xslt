<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE stylesheet [
		<!ENTITY spc "<xsl:text xml:space='preserve'> </xsl:text>">
		<!ENTITY apache-copyright SYSTEM "apache-copyright.inc">
		<!ENTITY Utils SYSTEM "csharp/Utils.cs.inc">
        <!ENTITY Exceptions SYSTEM "csharp/Exceptions.cs.inc">
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
	<xsl:param name="package" select="'api'"/>
	<xsl:param name="common-params" select="''"/>
	<xsl:param name="copyright">&apache-copyright;</xsl:param>
	<xsl:param name="group-id" select="$package"/>
	<xsl:param name="artifact-id" select="'cs-client'"/>
	<xsl:param name="api-name" select="'API'"/>
	<xsl:param name="version" select="'1.0.0.0'"/>
	<xsl:param name="project-url" select="'use the project-url parameter to set the project URL.'"/>
	<xsl:param name="description" select="'use the deacirption parameter to set the project description.'"/>
	<xsl:param name="name" select="'use the name parameter to set the project name.'"/>
	<xsl:param name="developer-name"/>
	<xsl:param name="developer-email"/>
	<xsl:param name="license-name" select="'The Apache Software License, Version 2.0'"/>
	<xsl:param name="license-url" select="'http://www.apache.org/licenses/LICENSE-2.0.txt'"/>
	<xsl:param name="additional-client-code" select="'// use the additional-client-code xslt parameter to insert custom code here'"/>
    <xsl:param name="namespace" select="rest4j:packageCamelCase($package)"/>

	<xsl:variable name="common-param-set" select="fn:tokenize($common-params,' ')"/>

	<xsl:template match="api">
		<api:files>

			<!--
					**********************************************
					**************** Models (DTOs) ***************
					**********************************************
			-->

			<xsl:for-each select="model">
				<api:file>
					<xsl:attribute name="name"><xsl:value-of select="$namespace"/>/Src/Model/<xsl:value-of select="@name"/>.cs</xsl:attribute>
					<xsl:attribute name="text">on</xsl:attribute>using System;
using System.Collections.Generic;

#region Copyright
/*
 * <xsl:value-of select="rest4j:javadocEscape0($copyright)"/>
 */
#endregion

namespace <xsl:value-of select="$namespace"/>.Model
{

	public class <xsl:value-of select="@name"/> {

		private Newtonsoft.Json.Linq.JObject dict;

        public <xsl:value-of select="@name"/>()
        {
            dict = new Newtonsoft.Json.Linq.JObject();
        }
		public <xsl:value-of select="@name"/>(Newtonsoft.Json.Linq.JObject json)
		{
			dict = json;
		}

<xsl:for-each select="fields/(complex|simple)" xml:space="preserve">
		/// &lt;summary&gt;
		/// <xsl:value-of select="rest4j:description(description)"/>
		/// &lt;/summary&gt;
		public <xsl:apply-templates select="." mode="prop-type"/> <xsl:apply-templates select="." mode="prop-name"/> {
			get {
                Newtonsoft.Json.Linq.JToken val = dict.GetValue(<xsl:value-of select="rest4j:quote(@name)"/>);
                if (val == null) return null;
                return <xsl:apply-templates select="." mode="prop-cast"/>;
			}
			set {
				dict[<xsl:value-of select="rest4j:quote(@name)"/>] = <xsl:apply-templates select="." mode="prop-json"/>;
			}
		}
</xsl:for-each>
		public Newtonsoft.Json.Linq.JObject AsJSON()
		{
			return dict;
		}

        public Boolean HasKey(string key)
        {
            Newtonsoft.Json.Linq.JToken val;
            return dict.TryGetValue(key, out val);
        }

		public override int GetHashCode()
		{
			return dict.GetHashCode();
		}

		public override bool Equals(Object obj)
		{
			if (obj is <xsl:value-of select="@name"/>) return ((<xsl:value-of select="@name"/>)obj).dict.Equals(dict);
			return false;
		}
		public override string ToString()
		{
			return dict.ToString();
		}
	}
}</api:file>
			</xsl:for-each>

			<!--
					*********************************************************
					***************  Parameter Object classes  **************
					*********************************************************
			-->

			<xsl:for-each select="endpoint[@client-param-object]">
				<api:file>
					<xsl:attribute name="name"><xsl:value-of select="$namespace"/>/Src/Model/<xsl:value-of select="@client-param-object"/>.cs</xsl:attribute>
					<xsl:attribute name="text">on</xsl:attribute>using System;
using System.Collections.Generic;

#region Copyright
/*
 * <xsl:value-of select="rest4j:javadocEscape0($copyright)"/>
 */
#endregion

namespace <xsl:value-of select="$namespace"/>.Model
{

	public class <xsl:value-of select="@client-param-object"/> {

		<xsl:variable name="params" select="rest4j:param-variables(.)"/>
		<xsl:for-each select="$params" xml:space="preserve">
		/// &lt;summary&gt;
		/// <xsl:value-of select="*:doc"/>
		/// &lt;/summary&gt;
		public <xsl:value-of select="*:type"/>&spc;<xsl:value-of select="rest4j:csCapIdentifier(*:name)"/> {
			get; set;
		}
		</xsl:for-each>
		public <xsl:value-of select="@client-param-object"/>() { }

		/// &lt;summary>
		/// Copy constructor
		/// &lt;/summary>
		public <xsl:value-of select="@client-param-object"/>(<xsl:value-of select="@client-param-object"/> another) {
			<xsl:for-each select="$params" xml:space="preserve">
			this.<xsl:value-of select="rest4j:csCapIdentifier(*:name)"/> = another.<xsl:value-of select="rest4j:csCapIdentifier(*:name)"/>;
			</xsl:for-each>
		}

	}
}</api:file>
			</xsl:for-each>

			<api:file>
				<xsl:attribute name="name"><xsl:value-of select="$namespace"/>/Src/Client.cs</xsl:attribute>
				<xsl:attribute name="text">on</xsl:attribute>using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using System.Net;
using System.IO;
using System.Web;
using <xsl:value-of select="$namespace"/>.Model;

#region Copyright
/*
 * <xsl:value-of select="rest4j:javadocEscape0($copyright)"/>
 */
#endregion

namespace <xsl:value-of select="$namespace"/>
{
	<!--
			***************************************************
			*************** Some Utility Classes **************
			***************************************************
	-->
    /// &lt;summary>
    /// Executes HTTP Requests. Can be intercepted by changing Request.RequestExecutor or Client(RequestExecutor executor).
    /// The default implementation is:
    /// &lt;code>
    ///   if (body != null)
    ///   {
    ///      body.Write(request.GetRequestStream());
    ///   }
    ///   return request.GetResponse() as HttpWebResponse;
    /// &lt;/code>
    /// &lt;/summary>
    /// &lt;param name="request">HttpWebRequest to be executed&lt;/param>
    /// &lt;param name="body">RequestData to be passed to the the request. If null, no data is passed.&lt;/param>
    /// &lt;returns>The result of request.GetResponse() or null if there is no result.&lt;/returns>
    public delegate Response RequestExecutor(HttpWebRequest request, RequestData body);

    /// &lt;summary>
    /// Implement this interface in your custom TestReader implementation to allow 
    /// the Content-Length header.
    /// &lt;/summary>
    public interface HasLength
    {
        long Length{get;}
    }

    /// &lt;summary>
    /// Implement this interface in your body implementation class (String, Stream, TextReader)
    /// to overwrite the Content-Type header. The default is text/plain for text data and
    /// application/octet-stream for binary data. Example:
    /// &lt;code>
    ///   class MyString : String, HasContentType
    ///   {
    ///     ....
    ///     string HasContentType.ContentType
    ///     {
    ///        get { return "application/x-www-form-urlencoded"; }
    ///     }
    ///   }
    /// &lt;/code>
    /// &lt;/summary>
    public interface HasContentType
    {
        String ContentType{get;}
    }

    /// &lt;summary>
    /// This object is passed to the RequestExecutor and contains the HTTP request entity.
    /// &lt;/summary>
    public class RequestData
    {
        private readonly Object body;
        private readonly string contentType;

        public RequestData(Object body, String contentType = null)
        {
            this.body = body;
            this.contentType = contentType;
        }

        public virtual long Length
        {
            get
            {
                if (body == null) return 0;
                if (body is Stream) return ((Stream)body).Length;
                if (body is HasLength) return ((HasLength)body).Length;
                if (body is String)
                {
                    MemoryStream ms = new MemoryStream();
                    StreamWriter sw = new StreamWriter(ms, Encoding.UTF8);
                    sw.Write(((String)body));
                    sw.Flush();
                    long len = ms.Position;
                    sw.Close();
                    ms.Close();
                    return len;
                }
                if (body is byte[])
                {
                    return ((byte[])body).Length;
                }
                throw new ArgumentException("Unknown body type: " + body.GetType());
            }
        }

        public virtual String ContentType
        {
            get
            {
                if (body is HasContentType) return ((HasContentType)body).ContentType;
                if (contentType == null)
                {
                    if (body is string || body is TextReader) return "plain/text";
                    return "application/octet-stream";
                }
                return contentType;
            }
        }

        public virtual object Body
        {
            get { return body;  }
        }

        public virtual void Write(Stream to) {
            if (body == null) return;
            if (body is byte[])
            {
                to.Write((byte[])body, 0, ((byte[])body).Length);
            }
            else if (body is Stream)
            {
                Stream os = (Stream)body;
                try {
                    os.CopyTo(to);
                } finally {
                    os.Close();
                }
            }
            else if (body is String)
            {
                StreamWriter sw = new StreamWriter(to, Encoding.UTF8);
                sw.Write((String)body);
                sw.Close();
            }
            else if (body is TextReader)
            {
                StreamWriter sw = new StreamWriter(to, Encoding.UTF8);
                TextReader tr = (TextReader)body;
                try
                {
                    char[] buf = new char[4096];
                    int read;
                    while ((read = tr.Read(buf, 0, buf.Length)) > 0) {
                        sw.Write(buf, 0, read);
                    }
                }
                finally
                {
                    sw.Close();
                    tr.Close();
                }
            }
            else
            {
                throw new ArgumentException("Unknown body type: " + body.GetType());
            }
        }
    }

    /// &lt;summary>
    /// This object is returned from the Client methods. To execute a request, call the Execute() method
    /// on the returned object. You can replace the RequestExecutor of this object to intercept further calls.
    /// &lt;/summary>
    public abstract class Request
    {
        private readonly HttpWebRequest request;
        private readonly RequestData body;

        protected Request(RequestExecutor requestExecutor, HttpWebRequest request, RequestData body = null)
        {
            this.request = request;
            this.body = body;
            this.RequestExecutor = requestExecutor;
        }

        public HttpWebRequest WebRequest
        {
            get
            {
                return request;
            }
        }
        public RequestData Body
        {
            get
            {
                return body;
            }
        }
        public RequestExecutor RequestExecutor
        {
            get;
            set;
        }

        /// &lt;summary>
        /// Run this method to actually execute the request.
        /// &lt;/summary>
        /// &lt;returns>The deserialized response.&lt;/returns>
        public object ExecuteUntyped()
        {
            // configure request
            request.Method = Method;
            if (body != null)
            {
                long length = body.Length;
                if (length >= 0) request.ContentLength = length;
                request.ContentType = body.ContentType;
            }
            try
            {
                var response = RequestExecutor(request, body);
                if (request.HaveResponse &amp;&amp; response != null)
                {
                    if ((int)response.StatusCode >= 400)
                    {
                       throw new StatusException((int)response.StatusCode, response.StatusDescription);
                    }
                }
                try
                {
                    return ConvertResponse(response);
                }
                finally
                {
                    if (response != null) response.Close();
                }
            }
            catch (WebException we)
            {
                var response = (HttpWebResponse)we.Response;
                if (response == null) throw we;
                StatusException se = new StatusException((int)response.StatusCode, response.StatusDescription);
                response.Close();
                throw se;
            }
        }
        public abstract String Method
        {
            get;
        }
        public abstract object ConvertResponse(Response response);

    }
    /// &lt;summary>
    /// This object is returned from the Client methods. To execute a request, call the Execute() method
    /// on the returned object. You can replace the RequestExecutor of this object to intercept further calls.
    /// &lt;/summary>
    /// &lt;typeparam name="T">The response type. Can be None, if there is no response.&lt;/typeparam>
    public abstract class Request&lt;T> : Request
    {
        protected Request(RequestExecutor requestExecutor, HttpWebRequest request, RequestData body = null)
                : base (requestExecutor, request, body)
        { }

        public T Execute()
        {
            return (T)base.ExecuteUntyped();
        }
    }

#region Response wrapper

    public class Response
    {
        private readonly HttpWebResponse response;

        public Response(HttpWebResponse response)
        {
            this.response = response;
        }

        public virtual string CharacterSet
        {
            get { return response.CharacterSet; }
        }

        public virtual string ContentEncoding
        {
            get { return response.ContentEncoding; }
        }

        public virtual long ContentLength
        {
            get { return response.ContentLength; }
        }

        public virtual string ContentType
        {
            get { return response.ContentType; }
        }

        public virtual CookieCollection Cookies
        {
            get
            {
                return response.Cookies;
            }
            set
            {
                response.Cookies = value;
            }
        }

        public virtual int StatusCode
        {
            get { return (int)response.StatusCode; }
        }

        public virtual string StatusDescription
        {
            get { return response.StatusDescription; }
        }

        public virtual void Close()
        {
            response.Close();
        }

        public virtual string GetResponseHeader(string headerName)
        {
            return response.GetResponseHeader(headerName);
        }

        public virtual Stream GetResponseStream()
        {
            return response.GetResponseStream();
        }
    }
#endregion

    /// &lt;summary>
    /// The response data type which specifies that there is no response. The result of
    /// the &lt;code>Request&amp;lt;None>.Execute()&lt;/code> is always null.
    /// &lt;/summary>
    public enum None { }
	<!--
			***************************************************
			**************** The main API Class ***************
			***************************************************
	-->
    /// &lt;summary>
    /// The main class that actually makes <xsl:value-of select="$api-name"/> calls.
    /// &lt;/summary>
    public class Client
    {
        string url = "<xsl:value-of select='$url'/>";
<xsl:if test="$https-url">        string secureUrl = "<xsl:value-of select='$https-url'/>";
</xsl:if>
<xsl:variable name="doc" select="/"/>

        public Client()
        {
            RequestExecutor = (request, body) => {
                if (body != null)
                {
                    body.Write(request.GetRequestStream());
                }
                return new Response(request.GetResponse() as HttpWebResponse);
            };
        }

        /// &lt;summary>
        /// Use this constructor to intercept HTTP calls, e.g. add headers to the request.
        /// &lt;/summary>
        /// &lt;param name="executor">A custom RequestExecutor.
        /// The default request executor does exactly the following:
        /// &lt;code>
        ///     if (body != null)
        ///     {
        ///         body.Write(request.GetRequestStream());
        ///     }
        ///     return request.GetResponse() as HttpWebResponse;
        /// &lt;/code>
        /// With this constructor, you can replace this functionality.
        /// &lt;/param>
        public Client(RequestExecutor executor)
        {
            this.RequestExecutor = executor;
        }

        /// &lt;summary>
        /// Sets the REST API endpoint URL. The default is "<xsl:value-of select='$url'/>".
        /// &lt;/summary>
        public string Url
        {
            get
            {
                return url;
            }
            set
            {
                url = value;
            }
        }
<xsl:if test="$https-url">
        /// &lt;summary>
        /// Sets the HTTPS REST API endpoint URL. The default is "<xsl:value-of select='$https-url'/>".
        /// &lt;/summary>
        public string SecureUrl
        {
            get
            {
                return secureUrl;
            }
            set
            {
                secureUrl = value;
            }
        }
</xsl:if>
        public RequestExecutor RequestExecutor { get; set; }
<!-- common params -->
<xsl:for-each select="$common-param-set" xml:space="preserve">
	<xsl:variable name='param-name' select='.'/>
	<xsl:if test="not($doc/api/endpoint/parameters/parameter[@name=$param-name])"><xsl:value-of select="error(fn:QName('http://rest4j.com/','PARAM-NOT-FOUND'),concat('Parameter not found: ', $param-name))"/></xsl:if>
        /// &lt;summary>
        /// The value of the "<xsl:value-of select="$param-name"/>" request parameter for subsequent requests.&lt;para/>
        /// <xsl:value-of select="rest4j:description(($doc/api/endpoint/parameters/parameter[@name=$param-name]/description)[1])"/>
        /// &lt;/summary>
        public <xsl:value-of select="rest4j:param-type($doc/api/endpoint/parameters/parameter[@name=$param-name]/@type[1])"/>&spc;<xsl:value-of select="rest4j:csCapIdentifier($param-name)"/>
        {
           get; set;
        }
</xsl:for-each>
<xsl:for-each select="endpoint">
<xsl:apply-templates select='.' mode='endpoint-method'/>
</xsl:for-each>

        <xsl:value-of select='$additional-client-code'/>

        private static string asString(Response response)
        {
            var ms = new MemoryStream();
            response.GetResponseStream().CopyTo(ms);
            ms.Seek(0, SeekOrigin.Begin);
            String charset = response.CharacterSet;
            if (charset == null) charset = "UTF-8";
            var reader = new StreamReader(ms, System.Text.Encoding.GetEncoding(charset));
            return reader.ReadToEnd();
        }
    }

    class UriBuilder
    {
        private readonly string uri;
        private readonly Dictionary&lt;string, string> parameters = new Dictionary&lt;string, string>();

        public UriBuilder(string uri)
        {
            this.uri = uri;
        }

        public void SetParameter(string name, string value)
        {
            parameters[name] = value;
        }

        public string build()
        {
            String uri = this.uri;
            Boolean first = true;
            foreach (KeyValuePair&lt;String, String> kvp in parameters)
            {
                if (first)
                {
                    uri += '?';
                    first = false;
                }
                else
                {
                    uri += '&amp;';
                }
                uri += String.Format("{0}={1}", kvp.Key, HttpUtility.UrlEncode(kvp.Value, Encoding.UTF8));
            }
            return uri;

        }
    }
}</api:file>

			<!--
					***************************************************
					******************  .sln file  ********************
					***************************************************
			-->

            <xsl:variable name="GUID" select="rest4j:randomUUID()"/>
			<api:file name="{$namespace}/{$namespace}.sln" xml:space='preserve' text="on">
Microsoft Visual Studio Solution File, Format Version 12.00
# Visual Studio Express 2013 for Windows Desktop
VisualStudioVersion = 12.0.21005.1
MinimumVisualStudioVersion = 10.0.40219.1
Project("{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}") = "<xsl:value-of select="$namespace"/>", "Src\<xsl:value-of select="$namespace"/>.csproj", "{<xsl:value-of select="$GUID"/>}"
EndProject
Global
	GlobalSection(SolutionConfigurationPlatforms) = preSolution
		Debug|Any CPU = Debug|Any CPU
		Release|Any CPU = Release|Any CPU
	EndGlobalSection
	GlobalSection(ProjectConfigurationPlatforms) = postSolution
		{<xsl:value-of select="$GUID"/>}.Debug|Any CPU.ActiveCfg = Debug|Any CPU
		{<xsl:value-of select="$GUID"/>}.Debug|Any CPU.Build.0 = Debug|Any CPU
		{<xsl:value-of select="$GUID"/>}.Release|Any CPU.ActiveCfg = Release|Any CPU
		{<xsl:value-of select="$GUID"/>}.Release|Any CPU.Build.0 = Release|Any CPU
	EndGlobalSection
	GlobalSection(SolutionProperties) = preSolution
		HideSolutionNode = FALSE
	EndGlobalSection
EndGlobal
</api:file>

            <!--
                    ***************************************************
                    ******************  .csproj file  ********************
                    ***************************************************
            -->

            <api:file name="{$namespace}/Src/{$namespace}.csproj" xml:space='preserve'>
<Project ToolsVersion="12.0" DefaultTargets="Build" xmlns="http://schemas.microsoft.com/developer/msbuild/2003">
  <PropertyGroup>
    <Configuration Condition=" '$(Configuration)' == '' ">Debug</Configuration>
    <Platform Condition=" '$(Platform)' == '' ">AnyCPU</Platform>
    <ProjectGuid>{<xsl:value-of select="$GUID"/>}</ProjectGuid>
    <OutputType>Library</OutputType>
    <AppDesignerFolder>Properties</AppDesignerFolder>
    <RootNamespace><xsl:value-of select="$namespace"/></RootNamespace>
    <AssemblyName><xsl:value-of select="$namespace"/></AssemblyName>
    <TargetFrameworkVersion>v4.5</TargetFrameworkVersion>
    <FileAlignment>512</FileAlignment>
    <ProjectTypeGuids>{3AC096D0-A1C2-E12C-1390-A8335801FDAB};{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}</ProjectTypeGuids>
    <VisualStudioVersion Condition="'$(VisualStudioVersion)' == ''">10.0</VisualStudioVersion>
    <VSToolsPath Condition="'$(VSToolsPath)' == ''">$(MSBuildExtensionsPath32)\Microsoft\VisualStudio\v$(VisualStudioVersion)</VSToolsPath>
    <ReferencePath>$(ProgramFiles)\Common Files\microsoft shared\VSTT\$(VisualStudioVersion)\UITestExtensionPackages</ReferencePath>
    <IsCodedUITest>False</IsCodedUITest>
    <TestProjectType>UnitTest</TestProjectType>
  </PropertyGroup>
  <PropertyGroup Condition=" '$(Configuration)|$(Platform)' == 'Debug|AnyCPU' ">
    <DebugSymbols>true</DebugSymbols>
    <DebugType>full</DebugType>
    <Optimize>false</Optimize>
    <OutputPath>bin\Debug\</OutputPath>
    <DefineConstants>DEBUG;TRACE</DefineConstants>
    <ErrorReport>prompt</ErrorReport>
    <WarningLevel>4</WarningLevel>
  </PropertyGroup>
  <PropertyGroup Condition=" '$(Configuration)|$(Platform)' == 'Release|AnyCPU' ">
    <DebugType>pdbonly</DebugType>
    <Optimize>true</Optimize>
    <OutputPath>bin\Release\</OutputPath>
    <DefineConstants>TRACE</DefineConstants>
    <ErrorReport>prompt</ErrorReport>
    <WarningLevel>4</WarningLevel>
  </PropertyGroup>
  <ItemGroup>
    <Reference Include="Newtonsoft.Json">
      <HintPath>..\packages\Newtonsoft.Json.5.0.8\lib\net45\Newtonsoft.Json.dll</HintPath>
    </Reference>
    <Reference Include="System" />
    <Reference Include="System.Web" />
  </ItemGroup>
  <Choose>
    <When Condition="('$(VisualStudioVersion)' == '10.0' or '$(VisualStudioVersion)' == '') and '$(TargetFrameworkVersion)' == 'v3.5'">
      <ItemGroup>
        <Reference Include="Microsoft.VisualStudio.QualityTools.UnitTestFramework, Version=10.1.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a, processorArchitecture=MSIL" />
      </ItemGroup>
    </When>
    <Otherwise>
      <ItemGroup>
        <Reference Include="Microsoft.VisualStudio.QualityTools.UnitTestFramework" />
      </ItemGroup>
    </Otherwise>
  </Choose>
  <ItemGroup>
    <Compile Include="Client.cs" />
    <Compile Include="Exceptions.cs" />
<xsl:for-each select="model">
    <Compile Include="Model\{@name}.cs" />
</xsl:for-each>
<xsl:for-each select="endpoint[@client-param-object]">
    <Compile Include="Model\{@client-param-object}.cs" />
</xsl:for-each>
    <Compile Include="Utils.cs" />
    <Compile Include="Properties\AssemblyInfo.cs" />
  </ItemGroup>
  <ItemGroup>
    <None Include="packages.config" />
  </ItemGroup>
  <Choose>
    <When Condition="'$(VisualStudioVersion)' == '10.0' And '$(IsCodedUITest)' == 'True'">
      <ItemGroup>
        <Reference Include="Microsoft.VisualStudio.QualityTools.CodedUITestFramework, Version=10.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a, processorArchitecture=MSIL">
          <Private>False</Private>
        </Reference>
        <Reference Include="Microsoft.VisualStudio.TestTools.UITest.Common, Version=10.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a, processorArchitecture=MSIL">
          <Private>False</Private>
        </Reference>
        <Reference Include="Microsoft.VisualStudio.TestTools.UITest.Extension, Version=10.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a, processorArchitecture=MSIL">
          <Private>False</Private>
        </Reference>
        <Reference Include="Microsoft.VisualStudio.TestTools.UITesting, Version=10.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a, processorArchitecture=MSIL">
          <Private>False</Private>
        </Reference>
      </ItemGroup>
    </When>
  </Choose>
  <Import Project="$(VSToolsPath)\TeamTest\Microsoft.TestTools.targets" Condition="Exists('$(VSToolsPath)\TeamTest\Microsoft.TestTools.targets')" />
  <Import Project="$(MSBuildToolsPath)\Microsoft.CSharp.targets" />
  <!-- To modify your build process, add your task inside one of the targets below and uncomment it.
       Other similar extension points exist, see Microsoft.Common.targets.
  <Target Name="BeforeBuild">
  </Target>
  <Target Name="AfterBuild">
  </Target>
  -->
</Project>
            </api:file>

            <!-- AssemblyInfo.cs -->
            <api:file name="{$namespace}/Src/Properties/AssemblyInfo.cs" text="on">
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;

// General Information about an assembly is controlled through the following
// set of attributes. Change these attribute values to modify the information
// associated with an assembly.
[assembly: AssemblyTitle(<xsl:value-of select="rest4j:quote($name)"/>)]
[assembly: AssemblyDescription(<xsl:value-of select="rest4j:quote($description)"/>)]
[assembly: AssemblyConfiguration("")]
[assembly: AssemblyCompany("")]
[assembly: AssemblyProduct(<xsl:value-of select="rest4j:quote($namespace)"/>)]
[assembly: AssemblyCopyright(<xsl:value-of select="rest4j:quote($license-name)"/>)]
[assembly: AssemblyTrademark("")]
[assembly: AssemblyCulture("")]

// Setting ComVisible to false makes the types in this assembly not visible
// to COM components.  If you need to access a type in this assembly from
// COM, set the ComVisible attribute to true on that type.
[assembly: ComVisible(true)]

// The following GUID is for the ID of the typelib if this project is exposed to COM
[assembly: Guid(<xsl:value-of select="rest4j:quote(rest4j:assemblyUUID($namespace))"/>)]

// Version information for an assembly consists of the following four values:
//
//      Major Version
//      Minor Version
//      Build Number
//      Revision
//
// You can specify all the values or you can default the Build and Revision Numbers
// by using the '*' as shown below:
// [assembly: AssemblyVersion("1.0.*")]
[assembly: AssemblyVersion(<xsl:value-of select="rest4j:quote(replace($version, '(([0-9]+\.)*[0-9]+).*', '$1'))"/>)] <!-- digits only -->
[assembly: AssemblyFileVersion("1.0.0.0")]
</api:file>

            <!-- Newtonsoft.Json library -->
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/net20/Newtonsoft.Json.dll" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/net20/Newtonsoft.Json.dll"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/net20/Newtonsoft.Json.xml" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/net20/Newtonsoft.Json.xml"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/net35/Newtonsoft.Json.dll" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/net35/Newtonsoft.Json.dll"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/net35/Newtonsoft.Json.xml" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/net35/Newtonsoft.Json.xml"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/net40/Newtonsoft.Json.dll" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/net40/Newtonsoft.Json.dll"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/net40/Newtonsoft.Json.xml" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/net40/Newtonsoft.Json.xml"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/net45/Newtonsoft.Json.dll" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/net45/Newtonsoft.Json.dll"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/net45/Newtonsoft.Json.xml" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/net45/Newtonsoft.Json.xml"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/netcore45/Newtonsoft.Json.dll" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/netcore45/Newtonsoft.Json.dll"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/netcore45/Newtonsoft.Json.xml" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/netcore45/Newtonsoft.Json.xml"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/portable-net40+sl4+wp7+win8/Newtonsoft.Json.dll" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/portable-net40+sl4+wp7+win8/Newtonsoft.Json.dll"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/portable-net40+sl4+wp7+win8/Newtonsoft.Json.xml" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/portable-net40+sl4+wp7+win8/Newtonsoft.Json.xml"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/portable-net45+wp80+win8/Newtonsoft.Json.dll" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/portable-net45+wp80+win8/Newtonsoft.Json.dll"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/lib/portable-net45+wp80+win8/Newtonsoft.Json.xml" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/lib/portable-net45+wp80+win8/Newtonsoft.Json.xml"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/Newtonsoft.Json.5.0.8.nupkg" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/Newtonsoft.Json.5.0.8.nupkg"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/Newtonsoft.Json.5.0.8.nuspec" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/Newtonsoft.Json.5.0.8.nuspec"/>
<api:file name="{$namespace}/packages/Newtonsoft.Json.5.0.8/tools/install.ps1" copy-from="csharp/packages/Newtonsoft.Json.5.0.8/tools/install.ps1"/>
<api:file name="{$namespace}/packages/repositories.config" copy-from="csharp/packages/repositories.config"/>

			<!-- Utility files -->
			<api:file name="{$namespace}/Src/packages.config" copy-from="csharp/packages.config"/>
			<api:file name="{$namespace}/Src/Utils.cs" text="on">&Utils;</api:file>
			<api:file name="{$namespace}/Src/Exceptions.cs" text="on">&Exceptions;</api:file>
		</api:files>
	</xsl:template>

	<xsl:template match="*[@collection='singleton']" mode="prop-type"><xsl:apply-templates select='.' mode='prop-singleton-type'/></xsl:template>
	<xsl:template match="*[@collection='array']" mode="prop-type">IList&lt;<xsl:apply-templates select='.' mode='prop-singleton-type'/>&gt;</xsl:template>
	<xsl:template match="*[@collection='map']" mode="prop-type">IDictionary&lt;string,<xsl:apply-templates select='.' mode='prop-singleton-type'/>&gt;</xsl:template>

	<xsl:template match="complex" mode="prop-singleton-type">
		<xsl:value-of select="@type"/>
	</xsl:template>
	<xsl:template match="simple[@type='number']" mode="prop-singleton-type">decimal?</xsl:template>
	<xsl:template match="simple[@type='string']" mode="prop-singleton-type">string</xsl:template>
	<xsl:template match="simple[@type='boolean']" mode="prop-singleton-type">bool?</xsl:template>
	<xsl:template match="simple[@type='date']" mode="prop-singleton-type">DateTime?</xsl:template>
	<xsl:template match="simple" mode="prop-singleton-type">Object</xsl:template>

	<xsl:template match="*[@collection='singleton']" mode="prop-cast">
        <xsl:param name="name">val</xsl:param>
        <xsl:apply-templates select='.' mode='prop-singleton-cast'>
           <xsl:with-param name="name" select="$name"/>
        </xsl:apply-templates>
    </xsl:template>
	<xsl:template match="*[@collection='array']" mode="prop-cast">
        <xsl:param name="name">val</xsl:param>new ConvertingList&lt;<xsl:apply-templates select='.' mode='prop-singleton-type'/>>(
                    (Newtonsoft.Json.Linq.JArray)<xsl:value-of select="$name"/>,
                    json =>
                    {
                        if (json == null) return null;
                        return <xsl:apply-templates select='.' mode='prop-singleton-cast'><xsl:with-param name="name">json</xsl:with-param></xsl:apply-templates>;
                    },
                    value =>
                    {
                        if (value == null) return null;
                        return <xsl:apply-templates select='.' mode='prop-singleton-json'/>;
                    }
                )</xsl:template>
	<xsl:template match="*[@collection='map']" mode="prop-cast">
        <xsl:param name="name">val</xsl:param>new ConvertingDictionary&lt;<xsl:apply-templates select='.' mode='prop-singleton-type'/>>(
                    (Newtonsoft.Json.Linq.JObject)<xsl:value-of select="$name"/>,
                    json =>
                    {
                        if (json == null) return null;
                        return <xsl:apply-templates select='.' mode='prop-singleton-cast'><xsl:with-param name="name">json</xsl:with-param></xsl:apply-templates>;
                    },
                    value =>
                    {
                        if (value == null) return null;
                        return <xsl:apply-templates select='.' mode='prop-singleton-json'/>;
                    }
                )</xsl:template>

	<xsl:template match="complex" mode="prop-singleton-cast">
        <xsl:param name="name">val</xsl:param>new <xsl:value-of select="@type"/>((Newtonsoft.Json.Linq.JObject)<xsl:value-of select="$name"/>)</xsl:template>
	<xsl:template match="simple[@type='number']" mode="prop-singleton-cast">
        <xsl:param name="name">val</xsl:param>JsonUtil.toDecimal(((Newtonsoft.Json.Linq.JValue)<xsl:value-of select="$name"/>).Value)</xsl:template>
	<xsl:template match="simple[@type='string']" mode="prop-singleton-cast">
        <xsl:param name="name">val</xsl:param>(string)((Newtonsoft.Json.Linq.JValue)<xsl:value-of select="$name"/>).Value</xsl:template>
	<xsl:template match="simple[@type='boolean']" mode="prop-singleton-cast">
        <xsl:param name="name">val</xsl:param>JsonUtil.toBoolean(((Newtonsoft.Json.Linq.JValue)<xsl:value-of select="$name"/>).Value)</xsl:template>
	<xsl:template match="simple[@type='date']" mode="prop-singleton-cast">
        <xsl:param name="name">val</xsl:param>JsonUtil.toDate(((Newtonsoft.Json.Linq.JValue)<xsl:value-of select="$name"/>).Value)</xsl:template>
	<xsl:template match="simple" mode="prop-singleton-cast">
        <xsl:param name="name">val</xsl:param>((Newtonsoft.Json.Linq.JValue)<xsl:value-of select="$name"/>).Value</xsl:template>

	<xsl:template match="*[@collection='singleton']" mode="prop-json">
        <xsl:param name="name">value</xsl:param>
        <xsl:apply-templates select='.' mode='prop-singleton-json'>
           <xsl:with-param name="name" select="$name"/>
        </xsl:apply-templates>
    </xsl:template>
	<xsl:template match="*[@collection='array']" mode="prop-json"><xsl:param name="name">value</xsl:param>JsonUtil.asJsonArray(<xsl:value-of select="$name"/>, element =>
                    {
                        if (element == null) return null;
                        return <xsl:apply-templates select='.' mode='prop-singleton-json'><xsl:with-param name="name">element</xsl:with-param></xsl:apply-templates>;
                    })</xsl:template>
	<xsl:template match="*[@collection='map']" mode="prop-json"><xsl:param name="name">value</xsl:param>JsonUtil.asJsonMap(<xsl:value-of select="$name"/>, element =>
                    {
                        if (element == null) return null;
                        return <xsl:apply-templates select='.' mode='prop-singleton-json'><xsl:with-param name="name">element</xsl:with-param></xsl:apply-templates>;
                    })</xsl:template>

	<xsl:template match="complex" mode="prop-singleton-json"><xsl:param name="name">value</xsl:param><xsl:value-of select="$name"/>.AsJSON()</xsl:template>
	<xsl:template match="simple" mode="prop-singleton-json"><xsl:param name="name">value</xsl:param>new Newtonsoft.Json.Linq.JValue(<xsl:value-of select="$name"/>)</xsl:template>

	<!--
			***************************************************
			**************  The endpoint method  **************
			***************************************************
	-->
	<xsl:template match="endpoint" mode="endpoint-method">
		<xsl:variable name="params" select="rest4j:param-variables(.)"/>
        private class <xsl:apply-templates select='.' mode="endpoint-method-name"/>Request : Request&lt;<xsl:apply-templates select='.' mode="endpoint-result-type"/>>
        {
            public <xsl:apply-templates select='.' mode="endpoint-method-name"/>Request(RequestExecutor executor, HttpWebRequest request, RequestData body = null)
                : base(executor, request, body)
            { }

            public override string Method
            {
                get { return <xsl:value-of select="rest4j:quote(@http)"/>; }
            }

            public override object ConvertResponse(Response response)
            {
                <xsl:choose>
                    <xsl:when test="response/json/@collection='array'">if (response == null)<xsl:if test="response/json/@optional='false'"> throw new AbsentResponseException();</xsl:if><xsl:if test="response/json/@optional='true'"> return null;</xsl:if>
                Newtonsoft.Json.Linq.JArray list = Newtonsoft.Json.Linq.JArray.Parse(asString(response));
                List&lt;<xsl:value-of select='response/json/@type'/>&gt; result = new List&lt;<xsl:value-of select='response/json/@type'/>&gt;(list.Count);
                foreach (Newtonsoft.Json.Linq.JToken dict in list) {
                    result.Add(new <xsl:value-of select='response/json/@type'/>((Newtonsoft.Json.Linq.JObject)dict));
                }
                return result;</xsl:when>
                    <xsl:when test="response/json">if (response == null)<xsl:if test="response/json/@optional='false'"> throw new AbsentResponseException();</xsl:if><xsl:if test="response/json/@optional='true'"> return null;</xsl:if>
                return new <xsl:value-of select='response/json/@type'/>(Newtonsoft.Json.Linq.JObject.Parse(asString(response)));
                    </xsl:when>
                    <xsl:when test="response/text">if (response == null) throw new AbsentResponseException();
                return asString(response);
                    </xsl:when>
                    <xsl:when test="response/binary">if (response == null) throw new AbsentResponseException();
                return response.GetResponseStream();
                    </xsl:when>
                    <xsl:otherwise>return null;</xsl:otherwise>
                </xsl:choose>
            }
        };
        /// &lt;summary><xsl:choose><xsl:when test="description/html:title">
        /// Builds Request object for the "<xsl:value-of select="rest4j:xmlComments(description/html:title/(*|text()))"/>" request.</xsl:when>
        <xsl:otherwise>
        /// Builds Request object for the "<code><xsl:value-of select="@http"/><xsl:text xml:space="preserve"> </xsl:text><xsl:apply-templates select="route" mode="route"/></code>" request.</xsl:otherwise>
	    </xsl:choose>
        /// To actually execute the request, call the execute() method on the returned object.&lt;para/&gt;
        /// <xsl:value-of select="rest4j:description(description)"/>&lt;/summary><xsl:choose><xsl:when test="@client-param-object" xml:space="preserve">
        /// &lt;param name=<xsl:value-of select="rest4j:quote(@client-param-object)"/>>The request parameters object.&lt;/param></xsl:when>
        <xsl:otherwise>
        <xsl:for-each select="$params" xml:space="preserve">
        /// &lt;param name=<xsl:value-of select="rest4j:quote(*:name)"/>><xsl:value-of select="*:doc"/>&lt;/param></xsl:for-each>
        </xsl:otherwise>
        </xsl:choose>
        /// &lt;returns>A Request&amp;lt;<xsl:apply-templates select='.' mode="endpoint-result-type"/>&amp;gt; object that can be executed later.&lt;/returns>
        public Request&lt;<xsl:apply-templates select='.' mode="endpoint-result-type"/>> <xsl:apply-templates select='.' mode="endpoint-method-name"/>(<xsl:apply-templates select='.' mode="endpoint-method-params"/>)
        {
            var builder = new UriBuilder(<xsl:if test="rest4j:secure(.)='true'">secureUrl</xsl:if><xsl:if test="rest4j:secure(.)='false'">url</xsl:if><xsl:apply-templates select="route/(param|text())"/>);
            <xsl:for-each select="parameters/parameter[@optional='true' and rest4j:path-param(ancestor::endpoint,@name)='false']">if (<xsl:value-of select="rest4j:param-value(ancestor::endpoint, @name)"/> != null) builder.SetParameter(<xsl:value-of select="rest4j:quote(@name)"/>, <xsl:value-of select="rest4j:param-value(ancestor::endpoint, @name)"/>.ToString());
            </xsl:for-each>
            <xsl:for-each select="parameters/parameter[@optional='false']">
            if (<xsl:value-of select="rest4j:param-value(ancestor::endpoint, @name)"/> == null) throw new ArgumentException("No parameter <xsl:value-of select="@name"/> is set");
            <xsl:if test="rest4j:path-param(ancestor::endpoint,@name)='false'">builder.SetParameter(<xsl:value-of select="rest4j:quote(@name)"/>, <xsl:value-of select="rest4j:param-value(ancestor::endpoint, @name)"/>.ToString());</xsl:if>
            </xsl:for-each>
            <xsl:variable name="body" select="rest4j:param-value(., 'body')"/>
            <xsl:if test="body and not(body/json/@optional='true')">
            if (<xsl:value-of select="$body"/> == null) throw new ArgumentException("No request body");</xsl:if>
            var request = WebRequest.CreateHttp(builder.build());
            return new <xsl:apply-templates select='.' mode="endpoint-method-name"/>Request(RequestExecutor, request<xsl:choose>
                <xsl:when test="body/json/@optional='true'">, <xsl:value-of select="$body"/> == null ? null : new RequestData(<xsl:apply-templates select="body/json" mode="body-as-json"/>, "application/json; charset=utf-8")</xsl:when>
                <xsl:when test="body/json">, new RequestData(<xsl:apply-templates select="body/json" mode="body-as-json"/>, "application/json; charset=utf-8")</xsl:when>
                <xsl:when test="body/patch">, new RequestData(JsonConvert.SerializeObject(<xsl:value-of select="$body"/>.AsJSON()), "application/json; charset=utf-8")</xsl:when>
                <xsl:when test="body/text">, new RequestData(<xsl:value-of select="$body"/>, "text/plain; charset=utf-8")</xsl:when>
                <xsl:when test="body/binary">, new RequestData(<xsl:value-of select="$body"/>)</xsl:when>
            </xsl:choose>);
        }
	</xsl:template>

	<xsl:template match="route/text()">+"<xsl:value-of select="."/>"</xsl:template>
	<xsl:template match="route/param">+<xsl:value-of select="rest4j:param-value(ancestor::endpoint, text())"/></xsl:template>

	<xsl:function name="rest4j:param-value">
		<xsl:param name="endpoint"/>
		<xsl:param name="name"/>
		<xsl:choose>
			<xsl:when test="$endpoint/@client-param-object and not(fn:index-of($common-param-set,$name))">paramObject.<xsl:value-of select="rest4j:csCapIdentifier($name)"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="rest4j:csCapIdentifier($name)"/></xsl:otherwise>
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

	<xsl:template match="json[@collection='singleton']" mode="body-as-json"><xsl:value-of select="rest4j:param-value(ancestor::endpoint, 'body')"/>.AsJSON().ToString()</xsl:template>

	<xsl:template match="text()" mode="route"><xsl:copy-of select="."/></xsl:template>
	<xsl:template match="param" mode="route">[<xsl:value-of select="."/>]</xsl:template>

	<xsl:template match="json[@collection='array']" mode="body-as-json">JsonUtil.asJsonArray&lt;<xsl:value-of select="@type"/>&gt;(<xsl:value-of select="rest4j:param-value(ancestor::endpoint, 'body')"/>,
            element => {
                return element == null ? null : element.AsJSON();
            }).ToString()</xsl:template>
	<xsl:template match="json[@collection='map']" mode="body-as-json">JsonUtil.asJsonMap&lt;<xsl:value-of select="@type"/>&gt;(<xsl:value-of select="rest4j:param-value(ancestor::endpoint, 'body')"/>,
            element => {
                return element == null ? null : element.AsJSON();
            }).ToString()</xsl:template>

	<xsl:template match="endpoint[response/json/@collection='singleton']" mode="endpoint-result-type"><xsl:value-of select="response/json/@type"/></xsl:template>
	<xsl:template match="endpoint[response/json/@collection='array']" mode="endpoint-result-type">IList&lt;<xsl:value-of select="response/json/@type"/>&gt;</xsl:template>
	<xsl:template match="endpoint[response/json/@collection='map']" mode="endpoint-result-type">IDictionary&lt;String,<xsl:value-of select="response/json/@type"/>&gt;</xsl:template>
	<xsl:template match="endpoint[response/binary]" mode="endpoint-result-type">Stream</xsl:template>
	<xsl:template match="endpoint[response/text]" mode="endpoint-result-type">String</xsl:template>
	<xsl:template match="endpoint" mode="endpoint-result-type">None?</xsl:template>

	<xsl:template match="endpoint[@client-method-name]" mode="endpoint-method-name"><xsl:value-of select="rest4j:camelCase('',@client-method-name)"/></xsl:template>
	<xsl:template match="endpoint" mode="endpoint-method-name"><xsl:value-of select="rest4j:camelCase('',rest4j:camelCase(service/@method,rest4j:singular(service/@name)))"/></xsl:template>
	<xsl:template match="endpoint" mode="endpoint-method-params">
		<xsl:choose>
			<xsl:when test="@client-param-object"><xsl:value-of select="@client-param-object"/>&spc;paramObject</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="rest4j:param-variables(.)">
					<xsl:if test="position()>1">,</xsl:if>
					<xsl:value-of select="*:type"/>&spc;<xsl:value-of select="*:name"/>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
	   Returns params/param/(type|name|doc) for all non-common API method parameters, including the body.
	  -->
	<xsl:function name="rest4j:param-variables">
		<xsl:param name="endpoint"></xsl:param>
		<xsl:variable name="body" select="$endpoint/body"/>
		<xsl:for-each select="$endpoint/parameters/parameter[not(fn:index-of($common-param-set,@name))]">
			<param>
				<type><xsl:value-of select="rest4j:param-type(@type)"/></type>
				<name><xsl:value-of select="rest4j:csCapIdentifier(@name)"/></name>
				<doc><xsl:value-of select="rest4j:description(description)"/></doc>
			</param>
		</xsl:for-each>
		<xsl:variable name="body-name-doc">
			<name>
				<xsl:choose>
					<xsl:when test="$body/@client-name"><xsl:value-of select="rest4j:csCapIdentifier($body/@client-name)"/></xsl:when>
					<xsl:otherwise>Body</xsl:otherwise>
				</xsl:choose>
			</name>
			<doc><xsl:value-of select="rest4j:description($body/description)"/></doc>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$body/json/@collection='singleton' or $body/patch"><param><type><xsl:value-of select="$body/(json|patch)/@type"/></type><xsl:copy-of select="$body-name-doc"/></param></xsl:when>
			<xsl:when test="$body/json/@collection='array'"><param><type>IList&lt;<xsl:value-of select="$body/json/@type"/>&gt;</type><xsl:copy-of select="$body-name-doc"/></param></xsl:when>
			<xsl:when test="$body/json/@collection='map'"><param><type>IDictionary&lt;String,<xsl:value-of select="$body/json/@type"/>&gt;</type><xsl:copy-of select="$body-name-doc"/></param></xsl:when>
			<xsl:when test="$body/binary"><param><type>Stream</type><xsl:copy-of select="$body-name-doc"/></param></xsl:when>
			<xsl:when test="$body/text"><param><type>String</type><xsl:copy-of select="$body-name-doc"/></param></xsl:when>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="rest4j:description">
		<xsl:param name="description"/>
		<xsl:value-of select="rest4j:xmlComments($description/(*[not(@client-lang) or @client-lang='*' or contains('c#,', concat(@client-lang,','))]|text())[name()!='html:title'])"/>
	</xsl:function>

	<xsl:function name="rest4j:param-type">
		<xsl:param name="type"/>
		<xsl:choose>
			<xsl:when test="$type='number'">decimal?</xsl:when>
			<xsl:when test="$type='string'">string</xsl:when>
			<xsl:when test="$type='boolean'">bool?</xsl:when>
			<xsl:when test="$type='date'">DateTime?</xsl:when>
			<xsl:otherwise><xsl:value-of select="error(fn:QName('http://rest4j.com/','TYPE'),'unexpected &lt;parameter&gt; type')"/></xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:template match="simple|complex" mode="prop-name">
		<xsl:if test="@client-name"><xsl:value-of select="rest4j:csCapIdentifier(@client-name)"/></xsl:if>
		<xsl:if test="not(@client-name)"><xsl:value-of select="rest4j:csCapIdentifier(@name)"/></xsl:if>
	</xsl:template>

	<xsl:function name="rest4j:csIdentifier">
		<xsl:param name="str"/>
		<xsl:value-of select="rest4j:identifier($str,'abstract as base bool break byte case catch char checked class const continue decimal default delegate do double else enum event explicit extern false finally fixed float for foreach goto if implicit in int interface internal is lock long namespace new null object operator out override params private protected public readonly ref return sbyte sealed short sizeof stackalloc static string struct switch this throw true try typeof uint ulong unchecked unsafe ushort using virtual void volatile while')"/>
	</xsl:function>

    <xsl:function name="rest4j:csCapIdentifier">
        <xsl:param name="str"/>
        <xsl:value-of select="rest4j:camelCase('', rest4j:identifier($str, ''))"/>
    </xsl:function>

</xsl:stylesheet>