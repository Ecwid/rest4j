//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.05 at 12:16:28 PM MSK 
//


package com.rest4j.impl.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ContentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ContentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="json" type="{http://rest4j.com/api-description}JsonType"/>
 *         &lt;element name="patch" type="{http://rest4j.com/api-description}PatchType"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="binary" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContentType", propOrder = {
    "json",
    "patch",
    "text",
    "binary"
})
public class ContentType {

    protected JsonType json;
    protected PatchType patch;
    protected Object text;
    protected Object binary;

    /**
     * Gets the value of the json property.
     * 
     * @return
     *     possible object is
     *     {@link JsonType }
     *     
     */
    public JsonType getJson() {
        return json;
    }

    /**
     * Sets the value of the json property.
     * 
     * @param value
     *     allowed object is
     *     {@link JsonType }
     *     
     */
    public void setJson(JsonType value) {
        this.json = value;
    }

    /**
     * Gets the value of the patch property.
     * 
     * @return
     *     possible object is
     *     {@link PatchType }
     *     
     */
    public PatchType getPatch() {
        return patch;
    }

    /**
     * Sets the value of the patch property.
     * 
     * @param value
     *     allowed object is
     *     {@link PatchType }
     *     
     */
    public void setPatch(PatchType value) {
        this.patch = value;
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setText(Object value) {
        this.text = value;
    }

    /**
     * Gets the value of the binary property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getBinary() {
        return binary;
    }

    /**
     * Sets the value of the binary property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setBinary(Object value) {
        this.binary = value;
    }

}
