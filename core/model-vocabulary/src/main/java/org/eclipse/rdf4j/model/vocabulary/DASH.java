/*******************************************************************************
 * Copyright (c) 2020 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.model.vocabulary;

import static org.eclipse.rdf4j.model.base.AbstractNamespace.createNamespace;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.base.AbstractIRI;

public class DASH {

	public static final String NAMESPACE = "http://datashapes.org/dash#";

	public static final String PREFIX = "dash";

	/**
	 * An immutable {@link Namespace} constant that represents the namespace.
	 */
	public static final Namespace NS = createNamespace(PREFIX, NAMESPACE);

	public static final IRI AllObjectsTarget = createIRI("AllObjectsTarget");
	public static final IRI AllSubjectsTarget = createIRI("AllSubjectsTarget");
	public static final IRI hasValueIn = createIRI("hasValueIn");
	public static final IRI HasValueInConstraintComponent = createIRI("HasValueInConstraintComponent");

	private static IRI createIRI(String allObjectsTarget) {
		return AbstractIRI.createIRI(NAMESPACE, allObjectsTarget);
	}

}
