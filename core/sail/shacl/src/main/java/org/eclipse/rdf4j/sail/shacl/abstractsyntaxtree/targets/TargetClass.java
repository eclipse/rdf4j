package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.targets;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.shacl.ConnectionsGroup;
import org.eclipse.rdf4j.sail.shacl.RdfsSubClassOfReasoner;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.StatementMatcher;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.constraintcomponents.ConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.ExternalPredicateObjectFilter;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.Select;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.Unique;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.UnorderedSelect;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.ValidationTuple;

public class TargetClass extends Target {
	private final Set<Resource> targetClass;

	public TargetClass(Set<Resource> targetClass) {
		this.targetClass = targetClass;
		assert !this.targetClass.isEmpty();

	}

	@Override
	public IRI getPredicate() {
		return SHACL.TARGET_CLASS;
	}

	@Override
	public PlanNode getAdded(ConnectionsGroup connectionsGroup, ConstraintComponent.Scope scope) {
		return getAddedRemovedInner(connectionsGroup, scope, connectionsGroup.getAddedStatements());
	}

	private PlanNode getAddedRemovedInner(ConnectionsGroup connectionsGroup, ConstraintComponent.Scope scope,
			SailConnection connection) {
		PlanNode planNode;
		if (targetClass.size() == 1) {
			Resource clazz = targetClass.stream().findAny().get();
			planNode = connectionsGroup
					.getCachedNodeFor(new UnorderedSelect(connection, null,
							RDF.TYPE, clazz, s -> new ValidationTuple(s.getSubject(), scope, false)));
		} else {
			planNode = connectionsGroup.getCachedNodeFor(
					new Select(connection, getQueryFragment("?a", "?c", null),
							"?a", b -> new ValidationTuple(b.getValue("a"), scope, false)));
		}

		return new Unique(planNode);
	}

	@Override
	public String getQueryFragment(String subjectVariable, String objectVariable,
			RdfsSubClassOfReasoner rdfsSubClassOfReasoner) {
		Set<Resource> targets = targetClass;

		if (rdfsSubClassOfReasoner != null) {
			targets = new HashSet<>(targets);

			targets = targets.stream()
					.flatMap(target -> rdfsSubClassOfReasoner.backwardsChain(target).stream())
					.collect(Collectors.toSet());
		}

		assert targets.size() >= 1;

		return targets.stream()
				.map(r -> "{ BIND(rdf:type as ?b1) \n BIND(<" + r + "> as " + objectVariable + ") \n " + subjectVariable
						+ " ?b1 " + objectVariable + ". } \n")
				.reduce((l, r) -> l + " UNION " + r)
				.get();

	}

	@Override
	public PlanNode getTargetFilter(ConnectionsGroup connectionsGroup, PlanNode parent) {
		return new ExternalPredicateObjectFilter(connectionsGroup.getBaseConnection(), RDF.TYPE, targetClass, parent,
				true, ExternalPredicateObjectFilter.FilterOn.activeTarget);

	}

	@Override
	public void toModel(Resource subject, IRI predicate, Model model, Set<Resource> exported) {
		targetClass.forEach(t -> {
			model.add(subject, getPredicate(), t);
		});
	}

	@Override
	public Stream<StatementMatcher> getStatementMatcher(StatementMatcher.Variable subject,
			StatementMatcher.Variable object,
			RdfsSubClassOfReasoner rdfsSubClassOfReasoner) {
		assert (subject == null);

		return targetClass
				.stream()
				.map(rdfsSubClassOfReasoner::backwardsChain)
				.flatMap(Collection::stream)
				.distinct()
				.map(t -> new StatementMatcher(object, new StatementMatcher.Variable(RDF.TYPE),
						new StatementMatcher.Variable(t)));
	}

	@Override
	public String getTargetQueryFragment(StatementMatcher.Variable subject, StatementMatcher.Variable object,
			RdfsSubClassOfReasoner rdfsSubClassOfReasoner) {
		assert (subject == null);

		List<Resource> targetClass = this.targetClass
				.stream()
				.map(rdfsSubClassOfReasoner::backwardsChain)
				.flatMap(Collection::stream)

				.distinct()
				.collect(Collectors.toList());

		if (targetClass.size() == 1) {

			return targetClass.stream()
					.map(t -> "?" + object.getName() + " a <" + t + "> .")
					.reduce((a, b) -> a + "\n" + b)
					.orElse("");

		} else {

			String in = targetClass.stream()
					.map(t -> "<" + t + ">")
					.reduce((a, b) -> a + " , " + b)
					.orElse("");

			return "?" + object.getName() + " a ?typekokokopko.\n" +
					"FILTER(?typekokokopko in (" + in + ")) \n";
		}

	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TargetClass that = (TargetClass) o;
		return targetClass.equals(that.targetClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(targetClass);
	}
}