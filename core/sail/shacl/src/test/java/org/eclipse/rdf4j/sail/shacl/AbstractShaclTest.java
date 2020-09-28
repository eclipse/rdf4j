/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.DynamicModel;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail.TransactionSettings.ValidationApproach;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.Shape;
import org.eclipse.rdf4j.sail.shacl.results.ValidationReport;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Håvard Ottestad
 */
@RunWith(Parameterized.class)
abstract public class AbstractShaclTest {

	private static final Logger logger = LoggerFactory.getLogger(AbstractShaclTest.class);

	private static final String[] FILENAME_EXTENSION = { "rq" };

	// @formatter:off
	// formatter doesn't understand that the trailing ) needs to be on a new line.
	private static final List<String> testCasePaths = Stream.of(

		"test-cases/and-or/datatypeNodeShape",
		"test-cases/class/allObjects",
		"test-cases/class/allSubjects",
		"test-cases/class/and",
		"test-cases/class/and2",
//		"test-cases/class/complexTargetShape",
//		"test-cases/class/complexTargetShape2",
		"test-cases/class/multipleClass",
		"test-cases/class/not",
		"test-cases/class/not2",
		"test-cases/class/notAnd",
		"test-cases/class/notNotSimple",
		"test-cases/class/simple",
//		"test-cases/class/simpleTargetShape",
//		"test-cases/class/sparqlTarget",
//		"test-cases/class/sparqlTargetNot",
		"test-cases/class/subclass",
		"test-cases/class/targetNode",
		"test-cases/class/validateTarget",
		"test-cases/class/validateTargetNot",
//		"test-cases/complex/dcat",
//		"test-cases/complex/foaf",
//		"test-cases/complex/sparqlTarget",
		"test-cases/datatype/allObjects",
		"test-cases/datatype/not",
		"test-cases/datatype/notNodeShape",
		"test-cases/datatype/notNodeShapeAnd",
//		"test-cases/datatype/notNodeShapeTargetShape",
		"test-cases/datatype/notNot",
		"test-cases/datatype/notSimpleNodeShape",
		"test-cases/datatype/notTargetNode",
//		"test-cases/datatype/notTargetShape",
		"test-cases/datatype/simple",
		"test-cases/datatype/simpleNested",
		"test-cases/datatype/simpleNode",
		"test-cases/datatype/simpleNodeNested",
//		"test-cases/datatype/sparqlTarget",
		"test-cases/datatype/targetNode",
		"test-cases/datatype/targetNode2",
		"test-cases/datatype/targetNodeLang",
		"test-cases/datatype/targetObjectsOf",
		"test-cases/datatype/targetSubjectsOf",
		"test-cases/datatype/targetSubjectsOfSingle",
//		"test-cases/deactivated/nodeshape",
//		"test-cases/deactivated/or",
//		"test-cases/deactivated/propertyshape",
		"test-cases/functionalProperty/multipleFunctional",
		"test-cases/functionalProperty/multipleFunctionalOr",
		"test-cases/functionalProperty/singleFunctional",
		"test-cases/hasValue/and",
		"test-cases/hasValue/not",
		"test-cases/hasValue/not2",
		"test-cases/hasValueIn/and",
		"test-cases/hasValueIn/not",
		"test-cases/hasValueIn/not2",
		"test-cases/hasValueIn/simple",
		"test-cases/hasValueIn/targetNode",
		"test-cases/hasValueIn/targetNode2",
		"test-cases/implicitTargetClass/simple",
		"test-cases/in/notAnd",
		"test-cases/in/notOr",
		"test-cases/in/simple",
		"test-cases/languageIn/simple",
		"test-cases/maxCount/not",
		"test-cases/maxCount/nested",
		"test-cases/maxCount/not2",
		"test-cases/maxCount/notNot",
		"test-cases/maxCount/simple",
		"test-cases/maxCount/simpleInversePath",
//		"test-cases/maxCount/sparqlTarget",
		"test-cases/maxCount/targetNode",
		"test-cases/maxExclusive/simple",
		"test-cases/maxExclusiveMinLength/not",
		"test-cases/maxExclusiveMinLength/simple",
		"test-cases/maxInclusive/simple",
		"test-cases/maxLength/simple",
		"test-cases/minCount/not",
		"test-cases/minCount/simple",
		"test-cases/minCount/targetNode",
		"test-cases/minExclusive/dateVsTime",
		"test-cases/minExclusive/simple",
		"test-cases/minInclusive/simple",
		"test-cases/minLength/simple",
		"test-cases/nodeKind/not",
		"test-cases/nodeKind/simple",
		"test-cases/nodeKind/simpleInversePath",
		"test-cases/nodeKind/validateTarget",
		"test-cases/or/class",
		"test-cases/or/class2",
		"test-cases/or/class2InversePath",
		"test-cases/or/classValidateTarget",
		"test-cases/or/datatype",
		"test-cases/or/datatype2",
		"test-cases/or/datatypeDifferentPaths",
		"test-cases/or/datatypeNodeShape",
		"test-cases/or/datatypeTargetNode",
		"test-cases/or/implicitAnd",
//		"test-cases/or/implicitAndSparqlTarget",
		"test-cases/or/inheritance",
		"test-cases/or/inheritance-deep",
		"test-cases/or/inheritanceNodeShape",
		"test-cases/or/maxCount",
		"test-cases/or/minCount",
		"test-cases/or/minCountDifferentPath",
		"test-cases/or/minCountMaxCount",
		"test-cases/or/multiple",
		"test-cases/or/nodeKindMinLength",
		"test-cases/or/nodeKindValidateTarget",
		"test-cases/pattern/multiple",
		"test-cases/pattern/simple",
		"test-cases/propertyShapeWithTarget/simple",
		"test-cases/uniqueLang/not",
		"test-cases/uniqueLang/simple" ,
		"test-cases/datatype/notNestedPropertyShape",
		"test-cases/datatype/notNestedPropertyShape2",
		"test-cases/hasValue/simple",
		"test-cases/hasValue/and2",
		"test-cases/hasValue/targetNode",
		"test-cases/hasValue/targetNode2",
		"test-cases/hasValueIn/simple",
		"test-cases/hasValueIn/and",
		"test-cases/hasValueIn/not",
		"test-cases/hasValueIn/not2",
		"test-cases/hasValueIn/targetNode",
		"test-cases/hasValueIn/targetNode2",
		"test-cases/languageIn/subtags",
		"test-cases/languageIn/subtags2",
		"test-cases/hasValueIn/targetNode2",
		"test-cases/hasValue/or",
//		"test-cases/hasValue/targetShapeOr",
//		"test-cases/hasValue/targetShapeAnd",
//		"test-cases/hasValue/targetShapeAnd2",
//		"test-cases/hasValue/targetShapeAndOr",
//		"test-cases/hasValue/targetShapeAndOr2",
//		"test-cases/hasValueIn/targetShapeOr",
		"test-cases/hasValueIn/or",
				"test-cases/class/simpleNested"

	)
		.distinct()
		.sorted()
		.collect(Collectors.toList());

	// @formatter:on
	static boolean fullLogging = false;
	final String testCasePath;
	final String path;
	final ExpectedResult expectedResult;
	final IsolationLevel isolationLevel;

	public AbstractShaclTest(String testCasePath, String path, ExpectedResult expectedResult,
			IsolationLevel isolationLevel) {
		this.testCasePath = testCasePath;
		this.path = path;
		this.expectedResult = expectedResult;
		this.isolationLevel = isolationLevel;
	}

	@Parameterized.Parameters(name = "{2} - {1} - {3}")
	public static Collection<Object[]> data() {

		return getTestsToRun();
	}

	private static List<String> findTestCases(String testCase, String baseCase) {

		try {
			URL resource = AbstractShaclTest.class.getClassLoader().getResource(testCase + "/shacl.ttl");
			assert Objects.nonNull(resource) : "Could not find: " + testCase + "/shacl.ttl";
			URI uri = resource.toURI();
			assert Files.exists(Paths.get(uri)) : uri;

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		List<String> ret = new ArrayList<>();

		URL resource = AbstractShaclTest.class.getClassLoader().getResource(testCase + "/" + baseCase + "/");
		if (resource == null) {
			return ret;
		}

		String[] list = new File(resource.getFile()).list();
		Arrays.sort(list);

		for (String caseName : list) {
			if (caseName.startsWith(".")) {
				continue;
			}
			String path = testCase + "/" + baseCase + "/" + caseName;
			InputStream resourceAsStream = AbstractShaclTest.class.getClassLoader().getResourceAsStream(path);
			if (resourceAsStream != null) {
				ret.add(path);
				try {
					resourceAsStream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		}

		return ret;

	}

	@AfterClass
	public static void afterClass() {
		GlobalValidationExecutionLogging.loggingEnabled = false;
	}

	private static Collection<Object[]> getTestsToRun() {
		List<Object[]> ret = new ArrayList<>();

		for (String testCasePath : testCasePaths) {
			List<Object[]> temp = new ArrayList<>();

			for (ExpectedResult baseCase : ExpectedResult.values()) {
				List<Object[]> collect = findTestCases(testCasePath, baseCase.name())
						.stream()
						.flatMap(path -> Stream
//								.of(IsolationLevels.NONE, IsolationLevels.SNAPSHOT, IsolationLevels.SERIALIZABLE) TODO: Uncomment this line!!!!!
								.of(IsolationLevels.NONE)
								.map(isolationLevel -> new Object[] { testCasePath, path, baseCase, isolationLevel }))
						.collect(Collectors.toList());

				temp.addAll(collect);
			}
			assert !temp.isEmpty() : "There were no test cases for: " + testCasePath;
			ret.addAll(temp);

		}

		return ret;
	}

	static void runTestCase(String shaclPath, String dataPath, ExpectedResult expectedResult,
			IsolationLevel isolationLevel, boolean preloadWithDummyData) {

		if (!dataPath.endsWith("/")) {
			dataPath = dataPath + "/";
		}

		if (!shaclPath.endsWith("/")) {
			shaclPath = shaclPath + "/";
		}

		String shaclFile = shaclPath + "shacl.ttl";
		logger.debug(shaclFile);

		printFile(shaclFile);

		SailRepository shaclRepository = getShaclSail();
		try {
			try {
				Utils.loadShapeData(shaclRepository, shaclFile);
				Utils.loadInitialData(shaclRepository, dataPath + "initialData.ttl");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			boolean exception = false;
			boolean ran = false;

			if (preloadWithDummyData) {
				try (SailRepositoryConnection connection = shaclRepository.getConnection()) {
					connection.begin(isolationLevel, ValidationApproach.Disabled);
					ValueFactory vf = connection.getValueFactory();
					connection.add(vf.createIRI("http://example.com/fewfkj9832ur8fh8whiu32hu"),
							vf.createIRI("http://example.com/jkhsdfiu3r2y9fjr3u0"),
							vf.createLiteral("123", XSD.INTEGER), vf.createBNode());
					try {
						connection.commit();
					} catch (RepositoryException sailException) {
						if (!(sailException.getCause() instanceof ShaclSailValidationException)) {
							throw sailException;
						}
						exception = true;
						logger.debug(sailException.getMessage());

						printResults(sailException);
					}
				}

			}

			URL resource = AbstractShaclTest.class.getClassLoader().getResource(dataPath);
			List<File> queries = FileUtils.listFiles(new File(resource.getFile()), FILENAME_EXTENSION, false)
					.stream()
					.sorted()
					.collect(Collectors.toList());

			Model validationReportActual = new LinkedHashModel();

			for (File queryFile : queries) {
				try {
					String query = FileUtils.readFileToString(queryFile, StandardCharsets.UTF_8);

					printCurrentState(shaclRepository);

					ran = true;
					printFile(dataPath + queryFile.getName());

					try (SailRepositoryConnection connection = shaclRepository.getConnection()) {
						connection.begin(isolationLevel);
						connection.prepareUpdate(query).execute();
						printCurrentState(shaclRepository);
						connection.commit();
					} catch (RepositoryException sailException) {
						if (!(sailException.getCause() instanceof ShaclSailValidationException)) {
							throw sailException;
						}

						exception = true;
						logger.debug(sailException.getMessage());
						validationReportActual = ((ShaclSailValidationException) sailException.getCause())
								.validationReportAsModel();
						printResults(sailException);
					} catch (Exception e) {
						throw e;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			if (ran) {

				if (expectedResult == ExpectedResult.valid) {
					assertFalse("Expected transaction to succeed", exception);
				} else {
					assertTrue("Expected transaction to fail", exception);
				}
			}
		} finally {
			shaclRepository.shutDown();
		}

	}

	private static void testValidationReport(String dataPath, Model validationReportActual) {
		try {
			InputStream resourceAsStream = AbstractShaclTest.class.getClassLoader()
					.getResourceAsStream(dataPath + "report.ttl");
			if (resourceAsStream == null) {
				logger.error(dataPath + "report.ttl did not exist. Creating an empty file!");

				String file = AbstractShaclTest.class.getClassLoader()
						.getResource(dataPath)
						.getFile()
						.replace("/target/test-classes/", "/src/test/resources/");
				boolean newFile = new File(file + "report.ttl").createNewFile();

			}

			Model validationReportExpected = Rio.parse(resourceAsStream, "", RDFFormat.TURTLE);

			writeActualModelToExpectedModelForDevPurposes(dataPath, validationReportActual);

			if (!isIsomorphic(validationReportActual, validationReportExpected)) {
				String validationReportExpectedString = modelToString(validationReportExpected);
				String validationReportActualString = modelToString(validationReportActual);
				assertEquals(validationReportExpectedString, validationReportActualString);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isIsomorphic(Model validationReportActual, Model validationReportExpected) {
		validationReportActual = normalizeModel(validationReportActual);
		validationReportExpected = normalizeModel(validationReportExpected);

		return Models.isomorphic(validationReportActual, validationReportExpected);
//		return true;
	}

	private static Model normalizeModel(Model model) {
		StringWriter sw = new StringWriter();
		Rio.write(model, sw, RDFFormat.TURTLE);
		try {
			return Rio.parse(new StringReader(sw.toString()), "", RDFFormat.TURTLE);
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	private static void writeActualModelToExpectedModelForDevPurposes(String dataPath, Model report)
			throws IOException {
		String file = AbstractShaclTest.class.getClassLoader()
				.getResource(dataPath)
				.getFile()
				.replace("/target/test-classes/", "/src/test/resources/");
		File file1 = new File(file + "report.ttl");
		try (FileOutputStream fileOutputStream = new FileOutputStream(file1)) {
			IOUtils.write(modelToString(report), fileOutputStream, StandardCharsets.UTF_8);
		}

	}

	private static void printCurrentState(SailRepository shaclRepository) {
		if (!fullLogging) {
			return;
		}

		try (SailRepositoryConnection connection = shaclRepository.getConnection()) {

			if (connection.isEmpty()) {
				System.out.println("########### CURRENT REPOSITORY STATE ###########");
				System.out.println("\nEMPTY!\n");
				System.out.println("################################################\n\n");
			} else {

				try (Stream<Statement> stream = connection.getStatements(null, null, null, false).stream()) {
					LinkedHashModel model = stream.collect(Collectors.toCollection(LinkedHashModel::new));

					String prettyPrintedModel = modelToString(model);

					System.out.println("########### CURRENT REPOSITORY STATE ###########");
					System.out.println(prettyPrintedModel);
					System.out.println("################################################\n\n");

				}
			}

		}
	}

	static String modelToString(Model model) {
		model.setNamespace("ex", "http://example.com/ns#");
		model.setNamespace(FOAF.PREFIX, FOAF.NAMESPACE);
		model.setNamespace(XSD.PREFIX, XSD.NAMESPACE);
		model.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
		model.setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
		model.setNamespace(SHACL.NS);
		model.setNamespace(RDF.NS);
		model.setNamespace(RDFS.NS);

		WriterConfig writerConfig = new WriterConfig();
		writerConfig.set(BasicWriterSettings.PRETTY_PRINT, true);
		writerConfig.set(BasicWriterSettings.INLINE_BLANK_NODES, true);
		writerConfig.set(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true);

		StringWriter stringWriter = new StringWriter();

		Rio.write(model, stringWriter, RDFFormat.TURTLE, writerConfig);

		return stringWriter.toString();
	}

	private static void printFile(String filename) {
		if (!fullLogging) {
			return;
		}

		try {
			System.out.println("### " + filename + " ###");
			String s = IOUtils.toString(AbstractShaclTest.class.getClassLoader().getResourceAsStream(filename),
					StandardCharsets.UTF_8);

			s = removeLeadingPrefixStatements(s);

			System.out.println(s);
			System.out.println("################################################\n\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String removeLeadingPrefixStatements(String s) {
		String[] split = s.split("\n");
		s = "";
		boolean skippingPrefixes = true;

		for (String s1 : split) {
			if (skippingPrefixes) {
				if (!(s1.trim().equals("") ||
						s1.trim().toLowerCase().startsWith("@prefix") ||
						s1.trim().toLowerCase().startsWith("@base") ||
						s1.trim().toLowerCase().startsWith("prefix"))) {
					skippingPrefixes = false;
				}
			}

			if (!skippingPrefixes) {
				s += s1 + "\n";
			}

		}
		return s;
	}

	static void runTestCaseSingleTransaction(String shaclPath, String dataPath, ExpectedResult expectedResult,
			IsolationLevel isolationLevel) {

		if (!dataPath.endsWith("/")) {
			dataPath = dataPath + "/";
		}

		if (!shaclPath.endsWith("/")) {
			shaclPath = shaclPath + "/";
		}

		SailRepository shaclRepository = getShaclSail();

		try {
			try {
				Utils.loadShapeData(shaclRepository, shaclPath + "shacl.ttl");
				Utils.loadInitialData(shaclRepository, dataPath + "initialData.ttl");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			boolean exception = false;
			boolean ran = false;
			Model validationReportActual = new LinkedHashModel();

			try (SailRepositoryConnection shaclSailConnection = shaclRepository.getConnection()) {
				shaclSailConnection.begin(isolationLevel);

				URL resource = AbstractShaclTest.class.getClassLoader().getResource(dataPath);
				List<File> queries = FileUtils.listFiles(new File(resource.getFile()), FILENAME_EXTENSION, false)
						.stream()
						.sorted()
						.collect(Collectors.toList());

				for (File queryFile : queries) {
					try {
						String query = FileUtils.readFileToString(queryFile, StandardCharsets.UTF_8);

						ran = true;
						logger.debug(queryFile.getName());

						shaclSailConnection.prepareUpdate(query).execute();

					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					shaclSailConnection.commit();

				} catch (RepositoryException sailException) {
					if (!(sailException.getCause() instanceof ShaclSailValidationException)) {
						throw sailException;
					}
					exception = true;
					logger.debug(sailException.getMessage());

					validationReportActual = ((ShaclSailValidationException) sailException.getCause())
							.validationReportAsModel();
					printResults(sailException);
				}
			}

			if (ran) {
				testValidationReport(dataPath, validationReportActual);

				if (expectedResult == ExpectedResult.valid) {
					assertFalse(exception);
				} else {
					assertTrue(exception);
				}
			}
		} finally {
			shaclRepository.shutDown();
		}

	}

	static void runTestCaseRevalidate(String shaclPath, String dataPath, ExpectedResult expectedResult,
			IsolationLevel isolationLevel) {

		if (!dataPath.endsWith("/")) {
			dataPath = dataPath + "/";
		}

		if (!shaclPath.endsWith("/")) {
			shaclPath = shaclPath + "/";
		}

		SailRepository shaclRepository = getShaclSail();
		try {
			try {
				Utils.loadShapeData(shaclRepository, shaclPath + "shacl.ttl");
				Utils.loadInitialData(shaclRepository, dataPath + "initialData.ttl");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			ValidationReport report = new ValidationReport(true);

			try (SailRepositoryConnection shaclSailConnection = shaclRepository.getConnection()) {
				shaclSailConnection.begin(isolationLevel, ValidationApproach.Disabled);

				URL resource = AbstractShaclTest.class.getClassLoader().getResource(dataPath);
				List<File> queries = FileUtils.listFiles(new File(resource.getFile()), FILENAME_EXTENSION, false)
						.stream()
						.sorted()
						.collect(Collectors.toList());

				for (File queryFile : queries) {
					try {
						String query = FileUtils.readFileToString(queryFile, StandardCharsets.UTF_8);
						shaclSailConnection.prepareUpdate(query).execute();

					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				shaclSailConnection.commit();

				shaclSailConnection.begin(ValidationApproach.Bulk);

				try {
					shaclSailConnection.commit();
				} catch (RepositoryException e) {
					if (e.getCause() instanceof ShaclSailValidationException) {
						report = ((ShaclSailValidationException) e.getCause()).getValidationReport();
					}
				}
			}

			printResults(report);

			if (!report.conforms()) {
				testValidationReport(dataPath, report.asModel());
			}

			if (expectedResult == ExpectedResult.valid) {
				assertTrue(report.conforms());
			} else {
				assertFalse(report.conforms());
			}
		} finally {
			shaclRepository.shutDown();
		}

	}

	static void runParsingTest(String shaclPath, String dataPath, ExpectedResult expectedResult) {
		if (!dataPath.endsWith("/")) {
			dataPath = dataPath + "/";
		}

		if (!shaclPath.endsWith("/")) {
			shaclPath = shaclPath + "/";
		}

		SailRepository shaclRepository = getShaclSail();
		try {
			try {
				Utils.loadShapeData(shaclRepository, shaclPath + "shacl.ttl");
				Utils.loadInitialData(shaclRepository, dataPath + "initialData.ttl");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			List<Shape> shapes = ((ShaclSail) shaclRepository.getSail()).getCurrentShapes();

			DynamicModel actual = new DynamicModelFactory().createEmptyModel();
			shapes.forEach(shape -> shape.toModel(actual));

			try (InputStream resourceAsStream = AbstractShaclTest.class.getClassLoader()
					.getResourceAsStream(shaclPath + "shacl.ttl")) {
				Model parse = Rio.parse(resourceAsStream, "", RDFFormat.TURTLE);

				// handle implicit targets in SHACL
				parse.filter(null, RDF.TYPE, RDFS.CLASS).subjects().forEach(s -> {
					if (parse.contains(s, RDF.TYPE, SHACL.PROPERTY_SHAPE)
							|| parse.contains(s, RDF.TYPE, SHACL.NODE_SHAPE)) {
						parse.add(s, SHACL.TARGET_CLASS, s);
					}
				});
				parse.remove(null, RDF.TYPE, RDFS.CLASS);

				// we add inferred NodeShape and PropertyShape, easier to remove when comparing
				parse.remove(null, RDF.TYPE, SHACL.NODE_SHAPE);
				parse.remove(null, RDF.TYPE, SHACL.SHAPE);
				parse.remove(null, RDF.TYPE, SHACL.PROPERTY_SHAPE);
				actual.remove(null, RDF.TYPE, SHACL.NODE_SHAPE);
				actual.remove(null, RDF.TYPE, SHACL.SHAPE);
				actual.remove(null, RDF.TYPE, SHACL.PROPERTY_SHAPE);

				if (!isIsomorphic(parse, actual)) {
					assertEquals(modelToString(parse), modelToString(actual));
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} finally {
			shaclRepository.shutDown();
		}

	}

	private static void printResults(ValidationReport report) {
		if (!fullLogging) {
			return;
		}
		System.out.println("\n############################################");
		System.out.println("\tValidation Report\n");
		Model validationReport = report.asModel();

		WriterConfig writerConfig = new WriterConfig();
		writerConfig.set(BasicWriterSettings.PRETTY_PRINT, true);
		writerConfig.set(BasicWriterSettings.INLINE_BLANK_NODES, true);

		Rio.write(validationReport, System.out, RDFFormat.TURTLE, writerConfig);
		System.out.println("\n############################################");
	}

	private static void printResults(RepositoryException sailException) {
		ValidationReport validationReport = ((ShaclSailValidationException) sailException.getCause())
				.getValidationReport();
		printResults(validationReport);
	}

	private static SailRepository getShaclSail() {

		ShaclSail shaclSail = new ShaclSail(new MemoryStore());
		SailRepository repository = new SailRepository(shaclSail);

		shaclSail.setLogValidationPlans(fullLogging);
		shaclSail.setCacheSelectNodes(false);
		shaclSail.setParallelValidation(false);
		shaclSail.setLogValidationViolations(fullLogging);
		shaclSail.setGlobalLogValidationExecution(fullLogging);
		shaclSail.setEclipseRdf4jShaclExtensions(true);
		shaclSail.setDashDataShapes(true);

		repository.init();

		return repository;
	}

	/**
	 * Sort and output testCasePaths
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("\n\tprivate static final List<String> testCasePaths = Stream.of(");
		String testCasesString = testCasePaths
				.stream()
				.map(a -> "\t\t\"" + a + "\"")
				.reduce((a, b) -> a + ",\n" + b)
				.orElse("");

		System.out.println(testCasesString);
		System.out.println("\t)\n" +
				"\t\t.distinct()\n" +
				"\t\t.sorted()\n" +
				"\t\t.collect(Collectors.toList());");
	}

	String getShaclPath() {
		String shaclPath = testCasePath;

		if (!shaclPath.endsWith("/")) {
			shaclPath = shaclPath + "/";
		}

		return shaclPath + "shacl.ttl";
	}

	enum ExpectedResult {
		valid,
		invalid
	}

}
