/*******************************************************************************
 * Copyright (c) 2020 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.query.explanation;

import org.eclipse.rdf4j.common.annotation.Experimental;

/**
 * This is an experimental feature. The interface may bw changes, moved or potentially removed in a future release.
 *
 * The interface is used to implement query explanations (query plan)
 */
@Experimental
public interface Explanation {

	String toString();

	/**
	 * The different levels that the query explanation can be at.
	 *
	 * @since 3.2.0
	 */
	enum Level {
		Unoptimized, // simple parsed
		Optimized, // parsed and optimized, which includes cost estimated
		Executed // plan as it was executed, which includes resultSizeActual
	}
	// TupleExpr asTupleExpr(); location in maven hierarchy prevents us from using TupleExpr here
}
