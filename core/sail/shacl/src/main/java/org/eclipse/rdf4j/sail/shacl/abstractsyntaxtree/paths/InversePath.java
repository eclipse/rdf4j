package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.paths;

import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sail.shacl.ConnectionsGroup;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.ShaclUnsupportedException;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.constraintcomponents.ConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNodeWrapper;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.Sort;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.UnorderedSelect;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.ValidationTuple;

public class InversePath extends Path {

	private final Path inversePath;

	public InversePath(Resource id, Resource inversePath, RepositoryConnection connection) {
		super(id);
		this.inversePath = Path.buildPath(connection, inversePath);

	}

	@Override
	public String toString() {
		return "InversePath{ " + inversePath + " }";
	}

	@Override
	public void toModel(Resource subject, IRI predicate, Model model, Set<Resource> exported) {
		model.add(subject, SHACL.INVERSE_PATH, inversePath.getId());
		inversePath.toModel(inversePath.getId(), null, model, exported);
	}

	@Override
	public PlanNode getAdded(ConnectionsGroup connectionsGroup, PlanNodeWrapper planNodeWrapper) {
		PlanNode unorderedSelect = new UnorderedSelect(connectionsGroup.getAddedStatements(), null,
				(IRI) inversePath.getId(), null,
				s -> new ValidationTuple(s.getObject(), s.getSubject(), ConstraintComponent.Scope.propertyShape, true));
		if (planNodeWrapper != null) {
			unorderedSelect = planNodeWrapper.apply(unorderedSelect);
		}

		return connectionsGroup.getCachedNodeFor(new Sort(unorderedSelect));
	}

	@Override
	public Stream<StatementPattern> getStatementPatterns(Var subject, Var object) {
		return Stream.of(new StatementPattern(object, new Var(inversePath.getId()), subject));
	}

	@Override
	public String getTargetQueryFragment(Var subject, Var object) {

		return inversePath.getTargetQueryFragment(object, subject);

	}
}