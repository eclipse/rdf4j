/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.model;

import java.io.Serializable;

/**
 * The supertype of all RDF model objects (URIs, blank nodes and literals).
 */
public interface Value extends Serializable {

	/**
	 * Returns the String-value of a <tt>Value</tt> object. This returns either a {@link Literal}'s label, a
	 * {@link IRI}'s URI or a {@link BNode}'s ID.
	 */
	public String stringValue();

	default boolean isBnode() {
		return this instanceof BNode;
	}

	default boolean isIRI() {
		return this instanceof IRI;
	}

	default boolean isLiteral() {
		return this instanceof Literal;
	}

	default boolean isTriple() {
		return this instanceof Triple;
	}
}
