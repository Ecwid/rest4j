/*
 * <xsl:value-of select="rest4j:javadocEscape0($copyright)"/>
 */
package <xsl:value-of select="$package"/>;
<![CDATA[
/**
 * Changes the content-type of a stream.
 * This interface can be implemented by a custom InputStream class passed as a 'body' parameter to the API.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface HasContentType {
    /**
     * @return What content-type should be used instead the default 'application/octet-stream'.
     */
    String getContentType();
}
]]>