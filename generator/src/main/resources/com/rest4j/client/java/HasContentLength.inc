/*
 * <xsl:value-of select="rest4j:javadocEscape0($copyright)"/>
 */
package <xsl:value-of select="$package"/>;
<![CDATA[
/**
 * Sets content-length of a stream.
 * This interface can be implemented by a custom InputStream class passed as a 'body' parameter to the API.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface HasContentLength {
    /**
     * @return The value of the content-length header.
     */
    long getContentLength();
}
]]>