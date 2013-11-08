package com.rest4j.xmlschemadocgen;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.*;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.net.URL;

/**
 * Generates api.xsd documentation in Google Code wiki format. The output is output/apixmlschema.txt.
 *
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Main {
	public static void main(String[] args) throws Exception {
		Configuration config = new Configuration();
		net.sf.saxon.s9api.Processor processor = new net.sf.saxon.s9api.Processor(config);
		XsltCompiler xsltCompiler = processor.newXsltCompiler();
		URL xsdUrl = Main.class.getClassLoader().getResource("com/rest4j/api.xsd");
		URL xsltUrl = Main.class.getClassLoader().getResource("com/rest4j/xmlschemadocgen/xmlschemawiki.xslt");
		XsltExecutable exec = xsltCompiler.compile(new StreamSource(xsltUrl.openStream(), xsltUrl.toString()));
		XsltTransformer transformer = exec.load();
		// transformer.setParameter(new net.sf.saxon.s9api.QName(param.getName()), new XdmAtomicValue(param.getValue()));
		transformer.setSource(new StreamSource(xsdUrl.openStream(), xsdUrl.toString()));
		Serializer out = new Serializer();
		out.setOutputProperty(Serializer.Property.METHOD, "text");
		File file = new File("output/apixmlschema.txt");
		out.setOutputFile(file);
		transformer.setDestination(out);
		transformer.transform();

		System.out.println("Output written to "+file.getAbsolutePath());
	}
}
