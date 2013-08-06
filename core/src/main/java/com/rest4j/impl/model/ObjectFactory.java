//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.07.13 at 11:56:40 PM MSK 
//


package com.rest4j.impl.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.rest4j.impl.model package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _P_QNAME = new QName("http://www.w3.org/1999/xhtml", "p");
    private final static QName _Tbody_QNAME = new QName("http://www.w3.org/1999/xhtml", "tbody");
    private final static QName _Title_QNAME = new QName("http://www.w3.org/1999/xhtml", "title");
    private final static QName _Li_QNAME = new QName("http://www.w3.org/1999/xhtml", "li");
    private final static QName _Dd_QNAME = new QName("http://www.w3.org/1999/xhtml", "dd");
    private final static QName _Span_QNAME = new QName("http://www.w3.org/1999/xhtml", "span");
    private final static QName _Ol_QNAME = new QName("http://www.w3.org/1999/xhtml", "ol");
    private final static QName _A_QNAME = new QName("http://www.w3.org/1999/xhtml", "a");
    private final static QName _Group_QNAME = new QName("http://rest4j.com/api-description", "group");
    private final static QName _B_QNAME = new QName("http://www.w3.org/1999/xhtml", "b");
    private final static QName _Tr_QNAME = new QName("http://www.w3.org/1999/xhtml", "tr");
    private final static QName _Ul_QNAME = new QName("http://www.w3.org/1999/xhtml", "ul");
    private final static QName _Dt_QNAME = new QName("http://www.w3.org/1999/xhtml", "dt");
    private final static QName _Api_QNAME = new QName("http://rest4j.com/api-description", "api");
    private final static QName _I_QNAME = new QName("http://www.w3.org/1999/xhtml", "i");
    private final static QName _Th_QNAME = new QName("http://www.w3.org/1999/xhtml", "th");
    private final static QName _Code_QNAME = new QName("http://www.w3.org/1999/xhtml", "code");
    private final static QName _Table_QNAME = new QName("http://www.w3.org/1999/xhtml", "table");
    private final static QName _Td_QNAME = new QName("http://www.w3.org/1999/xhtml", "td");
    private final static QName _Div_QNAME = new QName("http://www.w3.org/1999/xhtml", "div");
    private final static QName _StringWithParamsParam_QNAME = new QName("http://rest4j.com/api-description", "param");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.rest4j.impl.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExtraInfo }
     * 
     */
    public ExtraInfo createExtraInfo() {
        return new ExtraInfo();
    }

    /**
     * Create an instance of {@link Field }
     * 
     */
    public Field createField() {
        return new Field();
    }

    /**
     * Create an instance of {@link SimpleField }
     * 
     */
    public SimpleField createSimpleField() {
        return new SimpleField();
    }

    /**
     * Create an instance of {@link API }
     * 
     */
    public API createAPI() {
        return new API();
    }

    /**
     * Create an instance of {@link PatchType }
     * 
     */
    public PatchType createPatchType() {
        return new PatchType();
    }

    /**
     * Create an instance of {@link Files.File }
     * 
     */
    public Files.File createFilesFile() {
        return new Files.File();
    }

    /**
     * Create an instance of {@link Error }
     * 
     */
    public Error createError() {
        return new Error();
    }

    /**
     * Create an instance of {@link Values }
     * 
     */
    public Values createValues() {
        return new Values();
    }

    /**
     * Create an instance of {@link ContentType }
     * 
     */
    public ContentType createContentType() {
        return new ContentType();
    }

    /**
     * Create an instance of {@link Description }
     * 
     */
    public Description createDescription() {
        return new Description();
    }

    /**
     * Create an instance of {@link Response }
     * 
     */
    public Response createResponse() {
        return new Response();
    }

    /**
     * Create an instance of {@link Parameter }
     * 
     */
    public Parameter createParameter() {
        return new Parameter();
    }

    /**
     * Create an instance of {@link StringWithParams }
     * 
     */
    public StringWithParams createStringWithParams() {
        return new StringWithParams();
    }

    /**
     * Create an instance of {@link Errors }
     * 
     */
    public Errors createErrors() {
        return new Errors();
    }

    /**
     * Create an instance of {@link Files }
     * 
     */
    public Files createFiles() {
        return new Files();
    }

    /**
     * Create an instance of {@link Endpoint }
     * 
     */
    public Endpoint createEndpoint() {
        return new Endpoint();
    }

    /**
     * Create an instance of {@link Value }
     * 
     */
    public Value createValue() {
        return new Value();
    }

    /**
     * Create an instance of {@link Parameters }
     * 
     */
    public Parameters createParameters() {
        return new Parameters();
    }

    /**
     * Create an instance of {@link Complex }
     * 
     */
    public Complex createComplex() {
        return new Complex();
    }

    /**
     * Create an instance of {@link JsonType }
     * 
     */
    public JsonType createJsonType() {
        return new JsonType();
    }

    /**
     * Create an instance of {@link Model }
     * 
     */
    public Model createModel() {
        return new Model();
    }

    /**
     * Create an instance of {@link APIParams }
     * 
     */
    public APIParams createAPIParams() {
        return new APIParams();
    }

    /**
     * Create an instance of {@link Fields }
     * 
     */
    public Fields createFields() {
        return new Fields();
    }

    /**
     * Create an instance of {@link ComplexField }
     * 
     */
    public ComplexField createComplexField() {
        return new ComplexField();
    }

    /**
     * Create an instance of {@link ServiceEntry }
     * 
     */
    public ServiceEntry createServiceEntry() {
        return new ServiceEntry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "p")
    public JAXBElement<Complex> createP(Complex value) {
        return new JAXBElement<Complex>(_P_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "tbody")
    public JAXBElement<Complex> createTbody(Complex value) {
        return new JAXBElement<Complex>(_Tbody_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "title")
    public JAXBElement<Complex> createTitle(Complex value) {
        return new JAXBElement<Complex>(_Title_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "li")
    public JAXBElement<Complex> createLi(Complex value) {
        return new JAXBElement<Complex>(_Li_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "dd")
    public JAXBElement<Complex> createDd(Complex value) {
        return new JAXBElement<Complex>(_Dd_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "span")
    public JAXBElement<Complex> createSpan(Complex value) {
        return new JAXBElement<Complex>(_Span_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "ol")
    public JAXBElement<Complex> createOl(Complex value) {
        return new JAXBElement<Complex>(_Ol_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "a")
    public JAXBElement<Complex> createA(Complex value) {
        return new JAXBElement<Complex>(_A_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rest4j.com/api-description", name = "group")
    public JAXBElement<String> createGroup(String value) {
        return new JAXBElement<String>(_Group_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "b")
    public JAXBElement<Complex> createB(Complex value) {
        return new JAXBElement<Complex>(_B_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "tr")
    public JAXBElement<Complex> createTr(Complex value) {
        return new JAXBElement<Complex>(_Tr_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "ul")
    public JAXBElement<Complex> createUl(Complex value) {
        return new JAXBElement<Complex>(_Ul_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "dt")
    public JAXBElement<Complex> createDt(Complex value) {
        return new JAXBElement<Complex>(_Dt_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link API }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rest4j.com/api-description", name = "api")
    public JAXBElement<API> createApi(API value) {
        return new JAXBElement<API>(_Api_QNAME, API.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "i")
    public JAXBElement<Complex> createI(Complex value) {
        return new JAXBElement<Complex>(_I_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "th")
    public JAXBElement<Complex> createTh(Complex value) {
        return new JAXBElement<Complex>(_Th_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "code")
    public JAXBElement<Complex> createCode(Complex value) {
        return new JAXBElement<Complex>(_Code_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "table")
    public JAXBElement<Complex> createTable(Complex value) {
        return new JAXBElement<Complex>(_Table_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "td")
    public JAXBElement<Complex> createTd(Complex value) {
        return new JAXBElement<Complex>(_Td_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Complex }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xhtml", name = "div")
    public JAXBElement<Complex> createDiv(Complex value) {
        return new JAXBElement<Complex>(_Div_QNAME, Complex.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rest4j.com/api-description", name = "param", scope = StringWithParams.class)
    public JAXBElement<String> createStringWithParamsParam(String value) {
        return new JAXBElement<String>(_StringWithParamsParam_QNAME, String.class, StringWithParams.class, value);
    }

}