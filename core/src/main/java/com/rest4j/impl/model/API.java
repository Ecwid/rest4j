//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.10.15 at 09:57:00 AM MSK 
//


package com.rest4j.impl.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for API complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="API">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="params" type="{http://rest4j.com/api-description}APIParams" minOccurs="0"/>
 *         &lt;sequence maxOccurs="unbounded">
 *           &lt;element name="endpoint" type="{http://rest4j.com/api-description}Endpoint" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="model" type="{http://rest4j.com/api-description}Model" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "API", propOrder = {
    "params",
    "endpointAndModel"
})
public class API {

    protected APIParams params;
    @XmlElements({
        @XmlElement(name = "model", type = Model.class),
        @XmlElement(name = "endpoint", type = Endpoint.class)
    })
    protected List<Object> endpointAndModel;

    /**
     * Gets the value of the params property.
     * 
     * @return
     *     possible object is
     *     {@link APIParams }
     *     
     */
    public APIParams getParams() {
        return params;
    }

    /**
     * Sets the value of the params property.
     * 
     * @param value
     *     allowed object is
     *     {@link APIParams }
     *     
     */
    public void setParams(APIParams value) {
        this.params = value;
    }

    /**
     * Gets the value of the endpointAndModel property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the endpointAndModel property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEndpointAndModel().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Model }
     * {@link Endpoint }
     * 
     * 
     */
    public List<Object> getEndpointAndModel() {
        if (endpointAndModel == null) {
            endpointAndModel = new ArrayList<Object>();
        }
        return this.endpointAndModel;
    }

}
