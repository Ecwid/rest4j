/*
 * <xsl:value-of select="rest4j:javadocEscape0($copyright)"/>
 */
package <xsl:value-of select="$package"/>;
<![CDATA[
import org.apache.http.HttpEntity;
import org.json.JSONException;

import java.io.IOException;
import java.net.URI;

/**
 * Instances of this object are returned from Client's method calls. A caller then should call execute()
 * on this object to make the request and return a result.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public abstract class Request<T> {
	final protected URI uri;
	final protected HttpEntity body;

	public Request(URI uri) {
		this.uri = uri;
		this.body = null;
	}

	public Request(URI uri, HttpEntity body) {
		this.uri = uri;
		this.body = body;
	}

	public URI getUri() {
		return uri;
	}

	public HttpEntity getBody() {
		return body;
	}

	public abstract T execute() throws IOException, JSONException;
}
]]>