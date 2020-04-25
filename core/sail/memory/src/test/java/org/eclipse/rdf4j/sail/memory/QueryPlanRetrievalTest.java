/*******************************************************************************
 * Copyright (c) 2020 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.memory;

import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.explanation.Explanation;
import org.eclipse.rdf4j.query.explanation.GenericPlanNode;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.junit.Assert;
import org.junit.Test;

public class QueryPlanRetrievalTest {

	public static final String TUPLE_QUERY = "select * where {?a a ?c, ?d. filter(?c != ?d) optional {?d ?e ?f}}";

	@Test
	public void testTupleQuery() {
		SailRepository sailRepository = new SailRepository(new MemoryStore());
		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			TupleQuery tupleQuery = connection.prepareTupleQuery(TUPLE_QUERY);

			String actual = tupleQuery.explain(Explanation.Level.Unoptimized).toString();

			String expected = "Projection\n" +
					"   ProjectionElemList\n" +
					"      ProjectionElem \"a\"\n" +
					"      ProjectionElem \"c\"\n" +
					"      ProjectionElem \"d\"\n" +
					"      ProjectionElem \"e\"\n" +
					"      ProjectionElem \"f\"\n" +
					"   Filter\n" +
					"      Compare (!=)\n" +
					"         Var (name=c)\n" +
					"         Var (name=d)\n" +
					"      LeftJoin\n" +
					"         Join\n" +
					"            StatementPattern\n" +
					"               Var (name=a)\n" +
					"               Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)\n"
					+
					"               Var (name=c)\n" +
					"            StatementPattern\n" +
					"               Var (name=a)\n" +
					"               Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)\n"
					+
					"               Var (name=d)\n" +
					"         StatementPattern\n" +
					"            Var (name=d)\n" +
					"            Var (name=e)\n" +
					"            Var (name=f)\n";

			Assert.assertEquals(expected, actual);

		}
	}

	@Test
	public void testTupleQueryOptimized() {
		SailRepository sailRepository = new SailRepository(new MemoryStore());
		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			connection.add(RDFS.RESOURCE, RDF.TYPE, RDFS.RESOURCE);
			connection.add(RDF.TYPE, RDF.TYPE, RDF.PROPERTY);
			connection.add(RDF.TYPE, RDF.TYPE, RDFS.RESOURCE);
		}

		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			TupleQuery tupleQuery = connection.prepareTupleQuery(TUPLE_QUERY);

			String actual = tupleQuery.explain(Explanation.Level.Optimized).toString();
			String expected = "Projection\n" +
					"   ProjectionElemList\n" +
					"      ProjectionElem \"a\"\n" +
					"      ProjectionElem \"c\"\n" +
					"      ProjectionElem \"d\"\n" +
					"      ProjectionElem \"e\"\n" +
					"      ProjectionElem \"f\"\n" +
					"   LeftJoin\n" +
					"      Filter\n" +
					"         Compare (!=)\n" +
					"            Var (name=c)\n" +
					"            Var (name=d)\n" +
					"         Join\n" +
					"            StatementPattern (costEstimate=2, resultSizeEstimate=3)\n" +
					"               Var (name=a)\n" +
					"               Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)\n"
					+
					"               Var (name=c)\n" +
					"            StatementPattern (costEstimate=2, resultSizeEstimate=3)\n" +
					"               Var (name=a)\n" +
					"               Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)\n"
					+
					"               Var (name=d)\n" +
					"      StatementPattern\n" +
					"         Var (name=d)\n" +
					"         Var (name=e)\n" +
					"         Var (name=f)\n";
			Assert.assertEquals(expected, actual);

		}
	}

	@Test
	public void testTupleQueryExecuted() {
		SailRepository sailRepository = new SailRepository(new MemoryStore());
		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			connection.add(RDFS.RESOURCE, RDF.TYPE, RDFS.RESOURCE);
			connection.add(RDF.TYPE, RDF.TYPE, RDF.PROPERTY);
			connection.add(RDF.TYPE, RDF.TYPE, RDFS.RESOURCE);
		}
		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			TupleQuery tupleQuery = connection.prepareTupleQuery(TUPLE_QUERY);

			String actual = tupleQuery.explain(Explanation.Level.Executed).toString();
			String expected = "Projection (resultSizeActual=2)\n" +
					"   ProjectionElemList\n" +
					"      ProjectionElem \"a\"\n" +
					"      ProjectionElem \"c\"\n" +
					"      ProjectionElem \"d\"\n" +
					"      ProjectionElem \"e\"\n" +
					"      ProjectionElem \"f\"\n" +
					"   LeftJoin (resultSizeActual=2)\n" +
					"      Filter (resultSizeActual=2)\n" +
					"         Compare (!=)\n" +
					"            Var (name=c)\n" +
					"            Var (name=d)\n" +
					"         Join (resultSizeActual=5)\n" +
					"            StatementPattern (costEstimate=2, resultSizeEstimate=3, resultSizeActual=3)\n" +
					"               Var (name=a)\n" +
					"               Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)\n"
					+
					"               Var (name=c)\n" +
					"            StatementPattern (costEstimate=2, resultSizeEstimate=3, resultSizeActual=5)\n" +
					"               Var (name=a)\n" +
					"               Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)\n"
					+
					"               Var (name=d)\n" +
					"      StatementPattern (resultSizeActual=1)\n" +
					"         Var (name=d)\n" +
					"         Var (name=e)\n" +
					"         Var (name=f)\n";
			Assert.assertEquals(expected, actual);

		}
	}

	@Test
	public void testGenericPlanNode() {
		SailRepository sailRepository = new SailRepository(new MemoryStore());
		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			connection.add(RDFS.RESOURCE, RDF.TYPE, RDFS.RESOURCE);
			connection.add(RDF.TYPE, RDF.TYPE, RDF.PROPERTY);
			connection.add(RDF.TYPE, RDF.TYPE, RDFS.RESOURCE);
		}
		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			TupleQuery tupleQuery = connection.prepareTupleQuery(TUPLE_QUERY);

			String actual = tupleQuery.explain(Explanation.Level.Executed).asGenericPlanNode().toString();
			String expected = "Projection (resultSizeActual=2)\n" +
					"   ProjectionElemList\n" +
					"      ProjectionElem \"a\"\n" +
					"      ProjectionElem \"c\"\n" +
					"      ProjectionElem \"d\"\n" +
					"      ProjectionElem \"e\"\n" +
					"      ProjectionElem \"f\"\n" +
					"   LeftJoin (resultSizeActual=2)\n" +
					"      Filter (resultSizeActual=2)\n" +
					"         Compare (!=)\n" +
					"            Var (name=c)\n" +
					"            Var (name=d)\n" +
					"         Join (resultSizeActual=5)\n" +
					"            StatementPattern (costEstimate=2, resultSizeEstimate=3, resultSizeActual=3)\n" +
					"               Var (name=a)\n" +
					"               Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)\n"
					+
					"               Var (name=c)\n" +
					"            StatementPattern (costEstimate=2, resultSizeEstimate=3, resultSizeActual=5)\n" +
					"               Var (name=a)\n" +
					"               Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)\n"
					+
					"               Var (name=d)\n" +
					"      StatementPattern (resultSizeActual=1)\n" +
					"         Var (name=d)\n" +
					"         Var (name=e)\n" +
					"         Var (name=f)\n";
			Assert.assertEquals(expected, actual);
		}
	}

	@Test
	public void testJsonPlanNode() {
		SailRepository sailRepository = new SailRepository(new MemoryStore());
		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			connection.add(RDFS.RESOURCE, RDF.TYPE, RDFS.RESOURCE);
			connection.add(RDF.TYPE, RDF.TYPE, RDF.PROPERTY);
			connection.add(RDF.TYPE, RDF.TYPE, RDFS.RESOURCE);
		}
		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			TupleQuery tupleQuery = connection.prepareTupleQuery(TUPLE_QUERY);

			String actual = tupleQuery.explain(Explanation.Level.Executed).asJson();
			String expected = "{\n" +
					"  \"type\" : \"Projection\",\n" +
					"  \"resultSizeActual\" : 2,\n" +
					"  \"plans\" : [ {\n" +
					"    \"type\" : \"ProjectionElemList\",\n" +
					"    \"plans\" : [ {\n" +
					"      \"type\" : \"ProjectionElem \\\"a\\\"\"\n" +
					"    }, {\n" +
					"      \"type\" : \"ProjectionElem \\\"c\\\"\"\n" +
					"    }, {\n" +
					"      \"type\" : \"ProjectionElem \\\"d\\\"\"\n" +
					"    }, {\n" +
					"      \"type\" : \"ProjectionElem \\\"e\\\"\"\n" +
					"    }, {\n" +
					"      \"type\" : \"ProjectionElem \\\"f\\\"\"\n" +
					"    } ]\n" +
					"  }, {\n" +
					"    \"type\" : \"LeftJoin\",\n" +
					"    \"resultSizeActual\" : 2,\n" +
					"    \"plans\" : [ {\n" +
					"      \"type\" : \"Filter\",\n" +
					"      \"resultSizeActual\" : 2,\n" +
					"      \"plans\" : [ {\n" +
					"        \"type\" : \"Compare (!=)\",\n" +
					"        \"plans\" : [ {\n" +
					"          \"type\" : \"Var (name=c)\"\n" +
					"        }, {\n" +
					"          \"type\" : \"Var (name=d)\"\n" +
					"        } ]\n" +
					"      }, {\n" +
					"        \"type\" : \"Join\",\n" +
					"        \"resultSizeActual\" : 5,\n" +
					"        \"plans\" : [ {\n" +
					"          \"type\" : \"StatementPattern\",\n" +
					"          \"costEstimate\" : 1.5,\n" +
					"          \"resultSizeEstimate\" : 3.0,\n" +
					"          \"resultSizeActual\" : 3,\n" +
					"          \"plans\" : [ {\n" +
					"            \"type\" : \"Var (name=a)\"\n" +
					"          }, {\n" +
					"            \"type\" : \"Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)\"\n"
					+
					"          }, {\n" +
					"            \"type\" : \"Var (name=c)\"\n" +
					"          } ]\n" +
					"        }, {\n" +
					"          \"type\" : \"StatementPattern\",\n" +
					"          \"costEstimate\" : 1.7320508075688772,\n" +
					"          \"resultSizeEstimate\" : 3.0,\n" +
					"          \"resultSizeActual\" : 5,\n" +
					"          \"plans\" : [ {\n" +
					"            \"type\" : \"Var (name=a)\"\n" +
					"          }, {\n" +
					"            \"type\" : \"Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)\"\n"
					+
					"          }, {\n" +
					"            \"type\" : \"Var (name=d)\"\n" +
					"          } ]\n" +
					"        } ]\n" +
					"      } ]\n" +
					"    }, {\n" +
					"      \"type\" : \"StatementPattern\",\n" +
					"      \"resultSizeActual\" : 1,\n" +
					"      \"plans\" : [ {\n" +
					"        \"type\" : \"Var (name=d)\"\n" +
					"      }, {\n" +
					"        \"type\" : \"Var (name=e)\"\n" +
					"      }, {\n" +
					"        \"type\" : \"Var (name=f)\"\n" +
					"      } ]\n" +
					"    } ]\n" +
					"  } ]\n" +
					"}";
			Assert.assertEquals(expected, actual);

		}
	}

}
