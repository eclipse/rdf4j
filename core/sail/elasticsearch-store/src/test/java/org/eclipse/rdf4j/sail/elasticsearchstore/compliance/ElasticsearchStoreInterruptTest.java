/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.elasticsearchstore.compliance;

import org.assertj.core.util.Files;
import org.eclipse.rdf4j.sail.NotifyingSail;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailConcurrencyTest;
import org.eclipse.rdf4j.sail.SailInterruptTest;
import org.eclipse.rdf4j.sail.elasticsearchstore.ElasticsearchStore;
import org.eclipse.rdf4j.sail.elasticsearchstore.TestHelpers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;

import java.io.File;
import java.io.IOException;

/**
 * An extension of {@link SailConcurrencyTest} for testing the class {@link NativeStore}.
 */
public class ElasticsearchStoreInterruptTest extends SailInterruptTest {

	private static EmbeddedElastic embeddedElastic;

	private static File installLocation = Files.newTemporaryFolder();

	@BeforeClass
	public static void beforeClass() throws IOException, InterruptedException {

		embeddedElastic = TestHelpers.startElasticsearch(installLocation);

	}

	@AfterClass
	public static void afterClass() throws IOException {

		TestHelpers.stopElasticsearch(embeddedElastic, installLocation);
	}

	@Override
	protected NotifyingSail createSail() {
		ElasticsearchStore elasticsearchStore = new ElasticsearchStore("localhost", 9350, "cluster1", "index1");
		try (NotifyingSailConnection connection = elasticsearchStore.getConnection()) {
			connection.begin();
			connection.clear();
			connection.commit();
		}
		return elasticsearchStore;
	}
}
