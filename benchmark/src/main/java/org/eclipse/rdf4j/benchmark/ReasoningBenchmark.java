/*******************************************************************************
 * Copyright (c) 2016 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.benchmark;

import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingSchemaCachingRDFSInferencer;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author Håvard Mikkelsen Ottestad
 */

@State(Scope.Thread)
public class ReasoningBenchmark {

    @Param({"moreRdfs", "longChain", "medium", "simple"})
    public String directory;

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void noReasoning() throws IOException {
        SailRepository sail = new SailRepository(new MemoryStore());
        sail.initialize();

        try (SailRepositoryConnection connection = sail.getConnection()) {
            connection.begin();

            connection.add(resourceAsStream("schema.ttl"), "", RDFFormat.TURTLE);
            addAllDataSingleTransaction(connection);

            connection.commit();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void noReasoningMultipleTransactions() throws IOException {
        SailRepository sail = new SailRepository(new MemoryStore());
        sail.initialize();

        try (SailRepositoryConnection connection = sail.getConnection()) {

            connection.begin();
            connection.add(resourceAsStream("schema.ttl"), "", RDFFormat.TURTLE);
            connection.commit();

            addAllDataMultipleTransactions(connection);

        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void forwardChainingRDFSInferencer() throws IOException {
        SailRepository sail = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
        sail.initialize();

        try (SailRepositoryConnection connection = sail.getConnection()) {
            connection.begin();

            connection.add(resourceAsStream("schema.ttl"), "", RDFFormat.TURTLE);
            addAllDataSingleTransaction(connection);

            connection.commit();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void forwardChainingRDFSInferencerMultipleTransactions() throws IOException {
        SailRepository sail = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
        sail.initialize();

        try (SailRepositoryConnection connection = sail.getConnection()) {

            connection.begin();
            connection.add(resourceAsStream("schema.ttl"), "", RDFFormat.TURTLE);
            connection.commit();

            addAllDataMultipleTransactions(connection);

        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void forwardChainingSchemaCachingRDFSInferencer() throws IOException {
        SailRepository sail = new SailRepository(new ForwardChainingSchemaCachingRDFSInferencer(new MemoryStore()));
        sail.initialize();

        try (SailRepositoryConnection connection = sail.getConnection()) {
            connection.begin();

            connection.add(resourceAsStream("schema.ttl"), "", RDFFormat.TURTLE);
            addAllDataSingleTransaction(connection);

            connection.commit();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void forwardChainingSchemaCachingRDFSInferencerMultipleTransactions() throws IOException {
        SailRepository sail = new SailRepository(new ForwardChainingSchemaCachingRDFSInferencer(new MemoryStore()));
        sail.initialize();

        try (SailRepositoryConnection connection = sail.getConnection()) {

            connection.begin();
            connection.add(resourceAsStream("schema.ttl"), "", RDFFormat.TURTLE);
            connection.commit();

            addAllDataMultipleTransactions(connection);

        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void forwardChainingSchemaCachingRDFSInferencerSchema() throws IOException {
        SailRepository sail = new SailRepository(new ForwardChainingSchemaCachingRDFSInferencer(new MemoryStore(), createSchema()));
        sail.initialize();

        try (SailRepositoryConnection connection = sail.getConnection()) {
            connection.begin();
            addAllDataSingleTransaction(connection);
            connection.commit();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void forwardChainingSchemaCachingRDFSInferencerMultipleTransactionsSchema() throws IOException {
        SailRepository sail = new SailRepository(new ForwardChainingSchemaCachingRDFSInferencer(new MemoryStore(), createSchema()));
        sail.initialize();

        try (SailRepositoryConnection connection = sail.getConnection()) {
            addAllDataMultipleTransactions(connection);
        }
    }

    private SailRepository createSchema() throws IOException {
        SailRepository schema = new SailRepository(new MemoryStore());
        schema.initialize();

        try (SailRepositoryConnection schemaConnection = schema.getConnection()) {
            schemaConnection.begin();
            schemaConnection.add(resourceAsStream("schema.ttl"), "", RDFFormat.TURTLE);
            schemaConnection.commit();
        }
        return schema;
    }

    private void addAllDataSingleTransaction(SailRepositoryConnection connection) throws IOException {
        InputStream data = resourceAsStream("data.ttl");

        if (data != null) {
            connection.add(data, "", RDFFormat.TURTLE);
        }

        int counter = 0;
        while (true) {
            data = resourceAsStream("data" + counter++ + ".ttl");
            if (data == null) {
                break;
            }
            connection.add(data, "", RDFFormat.TURTLE);
        }
    }

    private void addAllDataMultipleTransactions(SailRepositoryConnection connection) throws IOException {
        InputStream data = resourceAsStream("data.ttl");

        if (data != null) {
            connection.begin();
            connection.add(data, "", RDFFormat.TURTLE);
            connection.commit();
        }

        int counter = 0;
        while (true) {
            data = resourceAsStream("data" + counter++ + ".ttl");
            if (data == null) {
                break;
            }
            connection.begin();
            connection.add(data, "", RDFFormat.TURTLE);
            connection.commit();
        }
    }

    private InputStream resourceAsStream(String resourceName) {
        return ReasoningBenchmark.class.getClassLoader().getResourceAsStream(directory + "/" + resourceName);

    }

}
