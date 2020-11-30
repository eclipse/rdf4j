/*******************************************************************************
 * Copyright (c) 2020 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes;

import org.eclipse.rdf4j.model.Value;

/**
 * @author Håvard Ottestad
 */
public class MinLengthFilter extends FilterPlanNode {

	private final long minLength;

	public MinLengthFilter(PlanNode parent, long minLength) {
		super(parent);
		this.minLength = minLength;
	}

	@Override
	boolean checkTuple(ValidationTuple t) {
		Value literal = t.getValue();

		return literal.stringValue().length() >= minLength;
	}

	@Override
	public String toString() {
		return "MinLengthFilter{" + "minLength=" + minLength + '}';
	}
}