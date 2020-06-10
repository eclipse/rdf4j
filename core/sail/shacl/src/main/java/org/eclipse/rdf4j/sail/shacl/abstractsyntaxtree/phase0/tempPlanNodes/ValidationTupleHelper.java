/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.tempPlanNodes;

import java.util.ArrayList;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.shacl.planNodes.Tuple;

/**
 * @author Håvard Ottestad
 */
public class ValidationTupleHelper {
	public static ValidationTuple join(ValidationTuple left, ValidationTuple right) {

		ValidationTuple tuple = new ValidationTuple(left.getTargetChain(), right.getPath(), right.getValue());

		return tuple;

	}
}
