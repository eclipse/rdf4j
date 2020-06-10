package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.paths;

import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sail.shacl.AST.PlaneNodeWrapper;
import org.eclipse.rdf4j.sail.shacl.ConnectionsGroup;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.tempPlanNodes.TupleValidationPlanNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNode;

public class ZeroOrOnePath extends Path {

	private final Path zeroOrOnePath;

	public ZeroOrOnePath(Resource id, Resource zeroOrOnePath, RepositoryConnection connection) {
		super(id);
		this.zeroOrOnePath = Path.buildPath(connection, zeroOrOnePath);

	}

	@Override
	public String toString() {
		return "ZeroOrOnePath{ " + zeroOrOnePath + " }";
	}

	@Override
	public void toModel(Resource subject, Model model, Set<Resource> exported) {
		model.add(subject, SHACL.ZERO_OR_ONE_PATH, zeroOrOnePath.getId());
		zeroOrOnePath.toModel(zeroOrOnePath.getId(), model, exported);
	}

	@Override
	public TupleValidationPlanNode getAdded(ConnectionsGroup connectionsGroup, PlaneNodeWrapper planeNodeWrapper) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<StatementPattern> getStatementPatterns(Var subject, Var object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getQueryFragment(Var subject, Var object) {
		throw new UnsupportedOperationException();
	}
}