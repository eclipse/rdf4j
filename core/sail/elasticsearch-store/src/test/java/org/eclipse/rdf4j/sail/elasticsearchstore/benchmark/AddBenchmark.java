/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.elasticsearchstore.benchmark;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Files;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.elasticsearchstore.ElasticsearchStore;
import org.eclipse.rdf4j.sail.elasticsearchstore.TestHelpers;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.JavaHomeOption;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Håvard Ottestad
 */
@State(Scope.Benchmark)
@Warmup(iterations = 20)
@BenchmarkMode({ Mode.AverageTime })
@Fork(value = 1, jvmArgs = { "-Xms8G", "-Xmx8G", "-Xmn4G", "-XX:+UseSerialGC" })
//@Fork(value = 1, jvmArgs = {"-Xms8G", "-Xmx8G", "-Xmn4G", "-XX:+UseSerialGC", "-XX:+UnlockCommercialFeatures", "-XX:StartFlightRecording=delay=60s,duration=120s,filename=recording.jfr,settings=profile", "-XX:FlightRecorderOptions=samplethreads=true,stackdepth=1024", "-XX:+UnlockDiagnosticVMOptions", "-XX:+DebugNonSafepoints"})
@Measurement(iterations = 10)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class AddBenchmark {

	private static EmbeddedElastic embeddedElastic;

	private static File installLocation = Files.newTemporaryFolder();

	private SailRepository elasticsearchStore;

	@Setup(Level.Trial)
	public void beforeClass() throws IOException, InterruptedException {
		embeddedElastic = TestHelpers.startElasticsearch(installLocation,
				"/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home");

		elasticsearchStore = new SailRepository(new ElasticsearchStore("localhost", 9350, "testindex"));

		System.gc();

	}

	private static InputStream getResourceAsStream(String name) {
		return AddBenchmark.class.getClassLoader().getResourceAsStream(name);
	}

	@TearDown(Level.Trial)
	public void afterClass() {

		elasticsearchStore.shutDown();
		TestHelpers.stopElasticsearch(embeddedElastic, installLocation);
	}

	@Benchmark
	public void clearAndAddLargeFile() throws IOException {

		try (SailRepositoryConnection connection = elasticsearchStore.getConnection()) {
			connection.begin(IsolationLevels.NONE);
			connection.clear();
			connection.commit();

			connection.begin(IsolationLevels.NONE);
			connection.add(getResourceAsStream("benchmarkFiles/datagovbe-valid.ttl"), "", RDFFormat.TURTLE);
			connection.commit();
		}
	}

}
