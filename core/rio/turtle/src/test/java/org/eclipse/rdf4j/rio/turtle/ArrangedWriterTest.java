///*******************************************************************************
// * Copyright (c) 2020 Eclipse RDF4J contributors.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Distribution License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/org/documents/edl-v10.php.
// *******************************************************************************/
//package org.eclipse.rdf4j.rio.turtle;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.StringReader;
//import java.io.StringWriter;
//
//import org.eclipse.rdf4j.model.IRI;
//import org.eclipse.rdf4j.model.Model;
//import org.eclipse.rdf4j.model.ValueFactory;
//import org.eclipse.rdf4j.model.impl.LinkedHashModel;
//import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
//import org.eclipse.rdf4j.model.util.Models;
//import org.eclipse.rdf4j.rio.RDFFormat;
//import org.eclipse.rdf4j.rio.RDFWriter;
//import org.eclipse.rdf4j.rio.RDFWriterFactory;
//import org.eclipse.rdf4j.rio.Rio;
//import org.eclipse.rdf4j.rio.WriterConfig;
//import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
//import org.junit.Test;
//
///**
// * @author TriangularIT
// */
//public class ArrangedWriterTest {
//	private ValueFactory vf;
//
//	private IRI uri1;
//
//	private IRI uri2;
//
//	private String exNs;
//
//	private RDFWriterFactory writerFactory;
//
//	public ArrangedWriterTest() {
//		vf = SimpleValueFactory.getInstance();
//
//		exNs = "http://example.org/";
//
//		uri1 = vf.createIRI(exNs, "uri1");
//		uri2 = vf.createIRI(exNs, "uri2");
//
//		writerFactory = new TurtleWriterFactory();
//	}
//
//	@Test
//	public void testWriteSingleStatementWithSubjectNamespace() {
//		String otherNs = "http://example.net/";
//		IRI uri0 = vf.createIRI(otherNs, "uri0");
//
//		Model input = new LinkedHashModel();
//		input.add(vf.createStatement(uri0, uri1, uri2));
//
//		input.setNamespace("org", exNs);
//		input.setNamespace("net", otherNs);
//
//		ByteArrayOutputStream outputWriter = new ByteArrayOutputStream();
//		write(input, outputWriter);
//
//		String sep = System.lineSeparator();
//		String expectedResult = "@prefix net: <http://example.net/> ." + sep +
//				"@prefix org: <http://example.org/> ." + sep + sep +
//				"net:uri0 org:uri1 org:uri2 ." + sep;
//
//		assertEquals(expectedResult, outputWriter.toString());
//	}
//
//	@Test
//	public void testBlankNodeInlining() throws IOException {
//		Model expected = Rio.parse(
//				new StringReader(
//						String.join("\n", "",
//								"_:b1 <http://www.w3.org/ns/shacl#focusNode> <http://example.com/ns#validPerson1>, _:b3;",
//								"		<http://www.w3.org/ns/shacl#value> _:b3;",
//								"  	<http://www.w3.org/ns/shacl#sourceShape> [ a <http://www.w3.org/ns/shacl#PropertyShape>; a [ a [] ] ] .",
//								"[] a [a []]."
//
//						)
//				), "", RDFFormat.TURTLE);
//
//		StringWriter stringWriter = new StringWriter();
//		WriterConfig config = new WriterConfig();
//		config.set(BasicWriterSettings.INLINE_BLANK_NODES, true);
//		Rio.write(expected, stringWriter, RDFFormat.TURTLE, config);
//
//		System.out.println(stringWriter.toString());
//
//		Model actual = Rio.parse(new StringReader(stringWriter.toString()), "", RDFFormat.TURTLE);
//
//		assertTrue(Models.isomorphic(expected, actual));
//
//	}
//
//	private void write(Model model, OutputStream writer) {
//		RDFWriter rdfWriter = writerFactory.getWriter(writer);
//		// "pretty print" forces ArrangedWriter to handle namespaces
//		rdfWriter.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
//		Rio.write(model, rdfWriter);
//	}
//}
