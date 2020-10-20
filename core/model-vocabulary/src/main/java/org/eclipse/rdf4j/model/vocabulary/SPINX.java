/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.model.vocabulary;

import static org.eclipse.rdf4j.model.base.AbstractIRI.createIRI;

import org.eclipse.rdf4j.model.IRI;

/**
 * http://spinrdf.org/spinx#.
 */
public final class SPINX {

	private SPINX() {
	}

	/**
	 * http://spinrdf.org/spinx
	 */
	public static final String NAMESPACE = "http://spinrdf.org/spinx#";

	public static final String PREFIX = "spinx";

	public static final IRI JAVA_SCRIPT_CODE_PROPERTY;

	public static final IRI JAVA_SCRIPT_FILE_PROPERTY;

	static {
		JAVA_SCRIPT_CODE_PROPERTY = createIRI(NAMESPACE, "javaScriptCode");
		JAVA_SCRIPT_FILE_PROPERTY = createIRI(NAMESPACE, "javaScriptFile");
	}
}
