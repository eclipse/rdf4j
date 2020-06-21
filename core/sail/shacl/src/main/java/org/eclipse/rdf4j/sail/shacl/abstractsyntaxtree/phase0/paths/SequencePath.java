package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.paths;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sail.shacl.ConnectionsGroup;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.HelperTool;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.planNodes.PlaneNodeWrapper;

public class SequencePath extends Path {

	private final List<Path> sequence;

	public SequencePath(Resource id, RepositoryConnection connection) {
		super(id);
		sequence = HelperTool.toList(connection, id, Resource.class)
				.stream()
				.map(p -> Path.buildPath(connection, p))
				.collect(Collectors.toList());

	}

	@Override
	public String toString() {
		return "SequencePath{ " + Arrays.toString(sequence.toArray()) + " }";
	}

	@Override
	public void toModel(Resource subject, IRI predicate, Model model, Set<Resource> exported) {
		sequence.forEach(p -> p.toModel(p.getId(), null, model, exported));

		List<Resource> values = sequence.stream().map(Path::getId).collect(Collectors.toList());

		HelperTool.listToRdf(values, id, model);
	}

	@Override
	public PlanNode getAdded(ConnectionsGroup connectionsGroup, PlaneNodeWrapper planeNodeWrapper) {
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
