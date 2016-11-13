/**
 * Copyright (c) 2016 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.eclipse.rdf4j.repository.sail.memory;

import org.eclipse.rdf4j.repository.EvaluationStrategyTest;
import org.eclipse.rdf4j.sail.base.config.BaseSailConfig;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;


/**
 * @author jeen
 *
 */
public class MemoryEvaluationStrategyTest extends EvaluationStrategyTest {

	@Override
	protected BaseSailConfig getBaseSailConfig() {
		return new MemoryStoreConfig(false);
	}

}
