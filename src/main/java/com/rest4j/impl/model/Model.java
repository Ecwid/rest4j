//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.29 at 12:22:07 PM MSK 
//


package com.rest4j.impl.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Model complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Model">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://rest4j.com/api-description}Description" minOccurs="0"/>
 *         &lt;element name="fields" type="{http://rest4j.com/api-description}Fields"/>
 *         &lt;element name="extra" type="{http://rest4j.com/api-description}ExtraInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://rest4j.com/api-description}identifier" />
 *       &lt;attribute name="class" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="instantiate" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="field-mapper" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Model", propOrder = {
    "description",
    "fields",
    "extra"
})
public class Model {

    protected Description description;
    @XmlElement(required = true)
    protected Fields fields;
    protected ExtraInfo extra;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "class", required = true)
    protected String clazz;
    @XmlAttribute(name = "instantiate")
    protected String instantiate;
    @XmlAttribute(name = "field-mapper")
    protected String fieldMapper;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link Description }
     *     
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link Description }
     *     
     */
    public void setDescription(Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the fields property.
     * 
     * @return
     *     possible object is
     *     {@link Fields }
     *     
     */
    public Fields getFields() {
        return fields;
    }

    /**
     * Sets the value of the fields property.
     * 
     * @param value
     *     allowed object is
     *     {@link Fields }
     *     
     */
    public void setFields(Fields value) {
        this.fields = value;
    }

    /**
     * Gets the value of the extra property.
     * 
     * @return
     *     possible object is
     *     {@link ExtraInfo }
     *     
     */
    public ExtraInfo getExtra() {
        return extra;
    }

    /**
     * Sets the value of the extra property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtraInfo }
     *     
     */
    public void setExtra(ExtraInfo value) {
        this.extra = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the instantiate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstantiate() {
        return instantiate;
    }

    /**
     * Sets the value of the instantiate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstantiate(String value) {
        this.instantiate = value;
    }

    /**
     * Gets the value of the fieldMapper property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFieldMapper() {
        return fieldMapper;
    }

    /**
     * Sets the value of the fieldMapper property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFieldMapper(String value) {
        this.fieldMapper = value;
    }

}
