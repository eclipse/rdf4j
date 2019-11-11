/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.elasticsearchstore.config;

import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.config.SailConfigException;
import org.eclipse.rdf4j.sail.config.SailFactory;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import org.eclipse.rdf4j.sail.elasticsearchstore.ElasticsearchStore;

/**
 * A {@link SailFactory} that creates {@link ElasticsearchStore}s based on RDF configuration data.
 *
 * @author Arjohn Kampman
 */
public class ElasticsearchStoreFactory implements SailFactory {

	/**
	 * The type of repositories that are created by this factory.
	 *
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "rdf4j:ElasticsearchStore";

	/**
	 * Returns the Sail's type: <tt>openrdf:ElasticsearchStore</tt>.
	 */
	@Override
	public String getSailType() {
		return SAIL_TYPE;
	}

	@Override
	public SailImplConfig getConfig() {
		return new ElasticsearchStoreConfig();
	}

	@Override
	public Sail getSail(SailImplConfig sailImplConfig) throws SailConfigException {
		if (!SAIL_TYPE.equals(sailImplConfig.getType())) {
			throw new SailConfigException("Invalid Sail type: " + sailImplConfig.getType());
		}

//		ElasticsearchStore elasticsearchStore = new ElasticsearchStore();
//
//		if (sailImplConfig instanceof ElasticsearchStoreConfig) {
//			ElasticsearchStoreConfig config = (ElasticsearchStoreConfig) sailImplConfig;
//
//			EvaluationStrategyFactory evalStratFactory = config.getEvaluationStrategyFactory();
//			if (evalStratFactory != null) {
//				elasticsearchStore.setEvaluationStrategyFactory(evalStratFactory);
//			}
//		}
//
//		return elasticsearchStore;

		return null;
	}
}
