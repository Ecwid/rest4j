//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.10.15 at 09:57:00 AM MSK 
//


package com.rest4j.impl.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CollectionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CollectionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="singleton"/>
 *     &lt;enumeration value="array"/>
 *     &lt;enumeration value="map"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CollectionType")
@XmlEnum
public enum CollectionType {

    @XmlEnumValue("singleton")
    SINGLETON("singleton"),
    @XmlEnumValue("array")
    ARRAY("array"),
    @XmlEnumValue("map")
    MAP("map");
    private final String value;

    CollectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CollectionType fromValue(String v) {
        for (CollectionType c: CollectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
