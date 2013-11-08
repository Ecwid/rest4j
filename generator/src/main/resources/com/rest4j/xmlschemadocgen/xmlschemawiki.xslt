<stylesheet
                version="2.0"
                xmlns="http://www.w3.org/1999/XSL/Transform"
                xmlns:rest4j="http://rest4j.com/func"
                xpath-default-namespace="http://www.w3.org/2001/XMLSchema">
    <output method="text"/>

    <variable name="xsd" select="/schema"/>

    <template match="/">#summary API Description XML Schema

= Root tags =
An API description should contain the following top-level XML tags:

{{{
&lt;api xmlns="http://rest4j.com/api-description" xmlns:html="http://www.w3.org/1999/xhtml">

   &lt;!-- optional params section -->
   &lt;params>...&lt;/params>

   &lt;!-- mixture of models and endpoints, in an arbitrary order -->
   &lt;model>...&lt;/model>
   &lt;endpoint>...&lt;/endpoint>
&lt;/api>
}}}

= Tags =
        <apply-templates/>
    </template>

    <template match="element[annotation/documentation]">
== <value-of select="@name"/> tag<value-of select="rest4j:num(.)"/> ==
        <if test="rest4j:child-elements(.)">
_Children tags:_ <value-of select="rest4j:tag-list(rest4j:child-elements(.))"/>
        </if>

_Parent tags:_ <value-of select="rest4j:tag-list(rest4j:parent-elements(.))"/>
<text>
</text>
        <value-of select="annotation/documentation"/>
        <if test="rest4j:attributes(.)">

_Attributes:_

            <for-each select="rest4j:attributes(.)">
*<value-of select="@name"/>*<if test="@use='optional'">(optional)</if>  -  <value-of select="replace(annotation/documentation, '^[ \r\n\t]*(.+)', '$1')"/>
<if test="@default">
Default is '<value-of select="@default"/>'.</if>
<text>
</text>
            </for-each>
        </if>
    </template>

    <template match="text()">
    </template>

    <function name="rest4j:child-elements">
        <param name="element"/>
        <variable name="type" select="$element/@type"/>
        <copy-of select="$element//sequence/element"/>
        <copy-of select="$xsd//complexType[@name=$type]//sequence/element"/>
        <copy-of select="$xsd//complexType[@name=$xsd//complexType[@name=$type]/complexContent/extension/@base]//sequence/element"/>
    </function>

    <function name="rest4j:attributes">
        <param name="element"/>
        <variable name="type" select="$element/@type"/>
        <copy-of select="$element/attribute"/>
        <copy-of select="$xsd//complexType[@name=$type]/attribute"/>
        <copy-of select="$xsd//complexType[@name=$type]/complexContent/extension/attribute"/>
        <copy-of select="$xsd//complexType[@name=$xsd//complexType[@name=$type]/complexContent/extension/@base]/attribute"/>
    </function>

    <function name="rest4j:parent-elements">
        <param name="element"/>
        <variable name="type" select="$element/parent::sequence/ancestor::complexType[@name]/@name | $element/parent::sequence/ancestor::extension/parent::complexContent/parent::complexType[@name]/@name"/>
        <if test="$type">
            <copy-of select="$xsd//element[@type=$type]"/>
            <for-each select="$xsd//complexType[complexContent/extension[@base=$type] and @name]/@name">
                <variable name="extended-type" select="."/>
                <copy-of select="$xsd//element[@type=$extended-type]"/>
            </for-each>
        </if>
        <copy-of select="$element/parent::sequence/ancestor::complexType/parent::element"/>
    </function>

    <function name="rest4j:num">
        <param name="element"/>
        <if test="count($xsd//element[@name=$element/@name])>1">(<for-each select="$xsd//element[@name=$element/@name]"><if test="$element=."><value-of select="position()"/></if></for-each>)</if>
    </function>

    <function name="rest4j:tag-list">
        <param name="elements"/>
        <for-each select="$elements">
            <if test="position()>1"><text>, </text></if>
            <if test="annotation/documentation">[#<value-of select="@name"/>_tag<value-of select="rest4j:num(.)"/><text> </text><value-of select="@name"/><value-of select="rest4j:num(.)"/>]</if>
            <if test="not(annotation/documentation)"><value-of select="@name"/></if>
        </for-each>
    </function>
</stylesheet>