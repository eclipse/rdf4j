package org.eclipse.rdf4j.sail.extensiblestore.evaluationstatistics;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.druid.hll.HyperLogLogCollector;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.sail.extensiblestore.ExtensibleSailStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class ExtensibleDynamicEvaluationStatistics extends ExtensibleEvaluationStatistics implements DynamicStatistics {
	private static final Logger logger = LoggerFactory.getLogger(ExtensibleDynamicEvaluationStatistics.class);

	private final HashFunction hashFunction = Hashing.murmur3_128();

	private final HyperLogLogCollector size = HyperLogLogCollector.makeLatestCollector();

	private final HyperLogLogCollector[] subjectIndex = new HyperLogLogCollector[1024];
	private final HyperLogLogCollector[] predicateIndex = new HyperLogLogCollector[1024];
	private final HyperLogLogCollector[] objectIndex = new HyperLogLogCollector[1024];
	private final HyperLogLogCollector[] contextIndex = new HyperLogLogCollector[1024];
	private final HyperLogLogCollector defaultContext = HyperLogLogCollector.makeLatestCollector();

	private final HyperLogLogCollector[][] subjectPredicateIndex = new HyperLogLogCollector[64][64];
	private final HyperLogLogCollector[][] predicateObjectIndex = new HyperLogLogCollector[64][64];

	public ExtensibleDynamicEvaluationStatistics(ExtensibleSailStore extensibleSailStore) {
		super(extensibleSailStore);

		Stream.of(subjectIndex, predicateIndex, objectIndex, contextIndex).forEach(index -> {
			for (int i = 0; i < index.length; i++) {
				index[i] = HyperLogLogCollector.makeLatestCollector();
			}
		});

		Stream.of(subjectPredicateIndex, predicateObjectIndex).forEach(index -> {
			for (int i = 0; i < index.length; i++) {
				for (int j = 0; j < index[i].length; j++) {
					index[i][j] = HyperLogLogCollector.makeLatestCollector();
				}
			}
		});

	}

	@Override
	protected CardinalityCalculator createCardinalityCalculator() {
		return cardinalityCalculator;
	}

	CardinalityCalculator cardinalityCalculator = new CardinalityCalculator() {

		@Override
		protected double getCardinality(StatementPattern sp) {
			double min = size.estimateCardinality();

			min = Math.min(min, getSubjectCardinality(sp.getSubjectVar()));
			min = Math.min(min, getPredicateCardinality(sp.getPredicateVar()));
			min = Math.min(min, getObjectCardinality(sp.getObjectVar()));

			if (sp.getSubjectVar().getValue() != null && sp.getPredicateVar().getValue() != null) {
				min = Math.min(min, getHllCardinality(subjectPredicateIndex, sp.getSubjectVar().getValue(),
						sp.getPredicateVar().getValue()));
			}

			if (sp.getPredicateVar().getValue() != null && sp.getObjectVar().getValue() != null) {
				min = Math.min(min, getHllCardinality(predicateObjectIndex, sp.getPredicateVar().getValue(),
						sp.getObjectVar().getValue()));
			}

			return min;

		}

		@Override
		protected double getSubjectCardinality(Var var) {
			if (var.getValue() == null) {
				return size.estimateCardinality();
			} else {
				return getHllCardinality(subjectIndex, var.getValue());
			}

		}

		@Override
		protected double getPredicateCardinality(Var var) {
			if (var.getValue() == null) {
				return size.estimateCardinality();
			} else {
				return getHllCardinality(predicateIndex, var.getValue());
			}
		}

		@Override
		protected double getObjectCardinality(Var var) {
			if (var.getValue() == null) {
				return size.estimateCardinality();
			} else {
				return getHllCardinality(objectIndex, var.getValue());
			}
		}

		@Override
		protected double getContextCardinality(Var var) {
			if (var.getValue() == null) {
				return defaultContext.estimateCardinality();
			} else {
				return getHllCardinality(contextIndex, var.getValue());
			}
		}
	};

	private double getHllCardinality(HyperLogLogCollector[][] index, Value value1, Value value2) {
		return index[Math.abs(value1.hashCode() % index.length)][Math.abs(value2.hashCode() % index.length)]
				.estimateCardinality();
	}

	private double getHllCardinality(HyperLogLogCollector[] index, Value value) {
		return index[Math.abs(value.hashCode() % index.length)].estimateCardinality();
	}

	@Override
	public void add(Statement statement, boolean inferred) {

		byte[] statementHash = hashFunction.hashString(statement.toString(), StandardCharsets.UTF_8).asBytes();

		size.add(statementHash);
		int subjectHash = statement.getSubject().hashCode();
		int predicateHash = statement.getPredicate().hashCode();
		int objectHash = statement.getObject().hashCode();

		indexSingleValue(statementHash, subjectIndex, subjectHash);
		indexSingleValue(statementHash, predicateIndex, predicateHash);
		indexSingleValue(statementHash, objectIndex, objectHash);

		indexDoubleValue(statementHash, subjectPredicateIndex, subjectHash, predicateHash);
		indexDoubleValue(statementHash, predicateObjectIndex, predicateHash, objectHash);

		if (statement.getContext() == null) {
			defaultContext.add(statementHash);
		} else {
			indexSingleValue(statementHash, contextIndex, statement.getContext().hashCode());
		}

		// logger.info("added: {} : {} ", statement, inferred ? "INFERRED" : "REAL");
	}

	private void indexDoubleValue(byte[] statementHash, HyperLogLogCollector[][] index, int indexHash, int indexHash2) {
		index[Math.abs(indexHash % index.length)][Math.abs(indexHash2 % index.length)].add(statementHash);

	}

	private void indexSingleValue(byte[] statementHash, HyperLogLogCollector[] index, int indexHash) {
		index[Math.abs(indexHash % index.length)].add(statementHash);
	}

	@Override
	public void remove(Statement statement, boolean inferred) {
		// logger.info("removed: {} : {} ", statement, inferred ? "INFERRED" : "REAL");

	}

	@Override
	public void removeByQuery(Resource subj, IRI pred, Value obj, boolean inferred, Resource... contexts) {
		// logger.info("removed by query: ?????");

	}
}
