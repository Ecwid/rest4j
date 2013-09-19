/*
 * <xsl:value-of select="rest4j:javadocEscape0($copyright)"/>
 */
package <xsl:value-of select="$package"/>.util;
<![CDATA[
/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public interface JsonElementFactory<T> {
	Object json(T val);
}
]]>