/*
 * <xsl:value-of select="rest4j:javadocEscape0($copyright)"/>
 */
package <xsl:value-of select="$package"/>;
<![CDATA[
import org.apache.http.client.methods.*;
import org.apache.http.client.*;
import org.apache.http.*;
import java.io.IOException;

/**
 * This interface is used to execute HTTP requests. You can create wrappers e.g. to add headers.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface RequestExecutor {
	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException;
}
]]>