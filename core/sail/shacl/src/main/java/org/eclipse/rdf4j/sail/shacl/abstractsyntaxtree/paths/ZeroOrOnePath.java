package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.paths;

import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sail.shacl.ConnectionsGroup;
import org.eclipse.rdf4j.sail.shacl.RdfsSubClassOfReasoner;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.ShaclUnsupportedException;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.StatementMatcher;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNodeWrapper;

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
	public void toModel(Resource subject, IRI predicate, Model model, Set<Resource> exported) {
		model.add(subject, SHACL.ZERO_OR_ONE_PATH, zeroOrOnePath.getId());
		zeroOrOnePath.toModel(zeroOrOnePath.getId(), null, model, exported);
	}

	@Override
	public PlanNode getAdded(ConnectionsGroup connectionsGroup, PlanNodeWrapper planNodeWrapper) {
		throw new ShaclUnsupportedException();
	}

	@Override
	public Stream<StatementMatcher> getStatementMatcher(StatementMatcher.Variable subject,
			StatementMatcher.Variable object,
			RdfsSubClassOfReasoner rdfsSubClassOfReasoner) {
		throw new ShaclUnsupportedException();
	}

	@Override
	public String getTargetQueryFragment(StatementMatcher.Variable subject, StatementMatcher.Variable object,
			RdfsSubClassOfReasoner rdfsSubClassOfReasoner) {
		throw new ShaclUnsupportedException();
	}
}