//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.05 at 12:16:28 PM MSK 
//


package com.rest4j.impl.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Field complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Field">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://rest4j.com/api-description}Description" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="optional" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="collection" type="{http://rest4j.com/api-description}CollectionType" default="singleton" />
 *       &lt;attribute name="prop" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="converter" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="mapping-method" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="access" type="{http://rest4j.com/api-description}FieldAccessType" default="readwrite" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Field", propOrder = {
    "description"
})
@XmlSeeAlso({
    SimpleField.class,
    ComplexField.class
})
public class Field {

    protected Description description;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "optional")
    protected Boolean optional;
    @XmlAttribute(name = "collection")
    protected CollectionType collection;
    @XmlAttribute(name = "prop")
    protected String prop;
    @XmlAttribute(name = "converter")
    protected String converter;
    @XmlAttribute(name = "mapping-method")
    protected String mappingMethod;
    @XmlAttribute(name = "access")
    protected FieldAccessType access;

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
     * Gets the value of the optional property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOptional() {
        if (optional == null) {
            return true;
        } else {
            return optional;
        }
    }

    /**
     * Sets the value of the optional property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOptional(Boolean value) {
        this.optional = value;
    }

    /**
     * Gets the value of the collection property.
     * 
     * @return
     *     possible object is
     *     {@link CollectionType }
     *     
     */
    public CollectionType getCollection() {
        if (collection == null) {
            return CollectionType.SINGLETON;
        } else {
            return collection;
        }
    }

    /**
     * Sets the value of the collection property.
     * 
     * @param value
     *     allowed object is
     *     {@link CollectionType }
     *     
     */
    public void setCollection(CollectionType value) {
        this.collection = value;
    }

    /**
     * Gets the value of the prop property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProp() {
        return prop;
    }

    /**
     * Sets the value of the prop property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProp(String value) {
        this.prop = value;
    }

    /**
     * Gets the value of the converter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConverter() {
        return converter;
    }

    /**
     * Sets the value of the converter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConverter(String value) {
        this.converter = value;
    }

    /**
     * Gets the value of the mappingMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMappingMethod() {
        return mappingMethod;
    }

    /**
     * Sets the value of the mappingMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMappingMethod(String value) {
        this.mappingMethod = value;
    }

    /**
     * Gets the value of the access property.
     * 
     * @return
     *     possible object is
     *     {@link FieldAccessType }
     *     
     */
    public FieldAccessType getAccess() {
        if (access == null) {
            return FieldAccessType.READWRITE;
        } else {
            return access;
        }
    }

    /**
     * Sets the value of the access property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldAccessType }
     *     
     */
    public void setAccess(FieldAccessType value) {
        this.access = value;
    }

}
