package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.planNodes;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.algebra.evaluation.util.ValueComparator;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.paths.Path;
import org.eclipse.rdf4j.sail.shacl.results.ValidationResult;

public class ValidationTuple {

	static ValueComparator valueComparator = new ValueComparator();
	private final Deque<Value> targetChain;
	private final Path path;
	private Value value;

	Deque<ValidationResult> validationResults;

	public ValidationTuple(Deque<Value> targetChain, Path path, Value value) {
		this.targetChain = targetChain;
		this.path = path;
		this.value = value;
	}

	public ValidationTuple(ValidationTuple validationTuple) {
		this.targetChain = new ArrayDeque<>(validationTuple.targetChain);
		this.path = validationTuple.path;
		this.value = validationTuple.value;
		if (validationTuple.validationResults != null) {
			this.validationResults = new ArrayDeque<>(validationTuple.validationResults);
		}
	}

	public ValidationTuple(BindingSet next, String[] variables) {
		targetChain = new ArrayDeque<>();
		for (String variable : variables) {
			targetChain.addLast(next.getValue(variable));
		}
		path = null;
		value = null;
	}

	public ValidationTuple(Value target, Path path, Value value) {
		targetChain = new ArrayDeque<>();
		targetChain.addLast(target);
		this.path = path;
		this.value = value;
	}

	public boolean sameTargetAs(ValidationTuple nextRight) {
		Value current = targetChain.getLast();
		Value currentRight = nextRight.targetChain.getLast();

		return current.equals(currentRight);
	}

	public Deque<Value> getTargetChain() {
		return targetChain;
	}

	public Path getPath() {
		return path;
	}

	public Value getValue() {
		return value;
	}

	public int compareTarget(ValidationTuple nextRight) {

		Value left = targetChain.getLast();
		Value right = nextRight.targetChain.getLast();

		return valueComparator.compare(left, right);
	}

	public Deque<ValidationResult> toValidationResult() {
		return validationResults;
	}

	public void addValidationResult(ValidationResult validationResult) {
		if (validationResults == null) {
			validationResults = new ArrayDeque<>();
		}
		this.validationResults.addFirst(validationResult);
	}

	public Value getAnyValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public Value getActiveTarget() {
		return targetChain.getLast();
	}

	public boolean hasValue() {
		return value != null;
	}
}
