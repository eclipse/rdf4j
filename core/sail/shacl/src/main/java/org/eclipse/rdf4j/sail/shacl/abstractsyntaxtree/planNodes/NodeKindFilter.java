/*******************************************************************************
 * Copyright (c) 2020 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.constraintcomponents.NodeKindConstraintComponent;

/**
 * @author Håvard Ottestad
 */
public class NodeKindFilter extends FilterPlanNode {

	private final NodeKindConstraintComponent.NodeKind nodeKind;

	public NodeKindFilter(PlanNode parent, NodeKindConstraintComponent.NodeKind nodeKind) {
		super(parent);
		this.nodeKind = nodeKind;
	}

	@Override
	boolean checkTuple(ValidationTuple t) {

		Value value = t.getValue();
		/*
		 * BlankNode(SHACL.BLANK_NODE), IRI(SHACL.IRI), Literal(SHACL.LITERAL), BlankNodeOrIRI(SHACL.BLANK_NODE_OR_IRI),
		 * BlankNodeOrLiteral(SHACL.BLANK_NODE_OR_LITERAL), IRIOrLiteral(SHACL.IRI_OR_LITERAL),
		 */

		switch (nodeKind) {
		case IRI:
			return value instanceof IRI;
		case Literal:
			return value instanceof Literal;
		case BlankNode:
			return value instanceof BNode;
		case IRIOrLiteral:
			return value instanceof IRI || value instanceof Literal;
		case BlankNodeOrIRI:
			return value instanceof BNode || value instanceof IRI;
		case BlankNodeOrLiteral:
			return value instanceof BNode || value instanceof Literal;
		}

		throw new IllegalStateException("Unknown nodeKind");

	}

	@Override
	public String toString() {
		return "NodeKindFilter{" + "nodeKind=" + nodeKind + '}';
	}
}
