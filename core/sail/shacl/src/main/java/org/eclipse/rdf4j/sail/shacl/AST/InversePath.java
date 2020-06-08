/*******************************************************************************
 * Copyright (c) 2020 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.AST;

import java.util.List;
import java.util.Objects;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.shacl.ConnectionsGroup;
import org.eclipse.rdf4j.sail.shacl.RdfsSubClassOfReasoner;
import org.eclipse.rdf4j.sail.shacl.Stats;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNodeProvider;
import org.eclipse.rdf4j.sail.shacl.planNodes.Sort;
import org.eclipse.rdf4j.sail.shacl.planNodes.UnorderedSelect;

/**
 * The AST (Abstract Syntax Tree) node that represents a simple path for exactly one predicate. Currently there is no
 * support for complex paths.
 *
 * @author Håvard M. Ottestad
 */
public class InversePath extends Path {

	private final IRI path;

	public InversePath(Resource id, IRI path) {
		super(id);
		this.path = path;

	}

	@Override
	public PlanNode getPlan(ConnectionsGroup connectionsGroup, boolean printPlans,
			PlanNodeProvider overrideTargetNode, boolean negateThisPlan, boolean negateSubPlans) {
		return connectionsGroup
				.getCachedNodeFor(new Sort(new UnorderedSelect(connectionsGroup.getBaseConnection(), null,
						path, null, UnorderedSelect.OutputPattern.ObjectSubject)));
	}

	@Override
	public PlanNode getPlanAddedStatements(ConnectionsGroup connectionsGroup,
			PlaneNodeWrapper planeNodeWrapper) {

		PlanNode unorderedSelect = new UnorderedSelect(connectionsGroup.getAddedStatements(), null,
				path, null, UnorderedSelect.OutputPattern.ObjectSubject);
		if (planeNodeWrapper != null) {
			unorderedSelect = planeNodeWrapper.apply(unorderedSelect);
		}
		return connectionsGroup.getCachedNodeFor(new Sort(unorderedSelect));
	}

	@Override
	public PlanNode getPlanRemovedStatements(ConnectionsGroup connectionsGroup,
			PlaneNodeWrapper planeNodeWrapper) {
		PlanNode unorderedSelect = new UnorderedSelect(connectionsGroup.getRemovedStatements(), null,
				path, null, UnorderedSelect.OutputPattern.ObjectSubject);
		if (planeNodeWrapper != null) {
			unorderedSelect = planeNodeWrapper.apply(unorderedSelect);
		}
		return connectionsGroup.getCachedNodeFor(new Sort(unorderedSelect));
	}

	@Override
	public PlanNode getAllTargetsPlan(ConnectionsGroup connectionsGroup, boolean negated) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Path> getPaths() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean requiresEvaluation(SailConnection addedStatements, SailConnection removedStatements, Stats stats) {

		if (stats.isEmpty()) {
			return false;
		}

		return addedStatements.hasStatement(null, path, null, false)
				|| removedStatements.hasStatement(null, path, null, false);
	}

	@Override
	public String getQuery(String subjectVariable, String objectVariable,
			RdfsSubClassOfReasoner rdfsSubClassOfReasoner) {

		return objectVariable + " <" + path + "> " + subjectVariable + " . \n";

	}

	public IRI getPath() {
		return path;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		InversePath that = (InversePath) o;
		return Objects.equals(path, that.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path);
	}

	@Override
	public String toString() {
		return path.toString();
	}
}
