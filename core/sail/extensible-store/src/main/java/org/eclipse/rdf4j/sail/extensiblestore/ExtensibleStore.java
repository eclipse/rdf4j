/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.extensiblestore;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.common.annotation.Experimental;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategyFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.StrictEvaluationStrategyFactory;
import org.eclipse.rdf4j.repository.sparql.federation.SPARQLServiceResolver;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractNotifyingSail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * A store where the backing storage can be implemented by the user. Supports up to ReadCommitted.
 * </p>
 * <p>
 * Extend this class and extend ExtensibleStoreConnection. Implement getConnection().
 * </p>
 * <p>
 * Implement the DataStructureInterface and the NamespaceStoreInterface. In your ExtensibleStore-extending class
 * implement a constructor and set the following variables: namespaceStore, dataStructure, dataStructureInferred.
 * </p>
 * <p>
 * Note that the entire ExtensibleStore and all code in this package is experimental. Method signatures, class names,
 * interfaces and the like are likely to change in future releases.
 * </p>
 *
 *
 * @author Håvard Mikkelsen Ottestad
 */
@Experimental
public abstract class ExtensibleStore<T extends DataStructureInterface, N extends NamespaceStoreInterface>
		extends AbstractNotifyingSail implements FederatedServiceResolverClient {

	private static final Logger logger = LoggerFactory.getLogger(ExtensibleStore.class);

	private ExtensibleSailStore sailStore;

	protected N namespaceStore;

	protected T dataStructure;
	protected T dataStructureInferred;

	public ExtensibleStore() {
	}

	ExtensibleSailStore getSailStore() {
		return sailStore;
	}

	@Override
	synchronized protected void initializeInternal() throws SailException {
		if (sailStore != null) {
			sailStore.close();
		}

		sailStore = new ExtensibleSailStore(Objects.requireNonNull(dataStructure),
				Objects.requireNonNull(dataStructureInferred), Objects.requireNonNull(namespaceStore));

		sailStore.init();
		namespaceStore.init();
	}

	@Override
	public List<IsolationLevel> getSupportedIsolationLevels() {
		return Arrays.asList(IsolationLevels.NONE, IsolationLevels.READ_UNCOMMITTED, IsolationLevels.READ_COMMITTED);
	}

	@Override
	public IsolationLevel getDefaultIsolationLevel() {
		return IsolationLevels.READ_COMMITTED;
	}

	@Override
	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {

	}

	@Override
	public ValueFactory getValueFactory() {
		return SimpleValueFactory.getInstance();
	}

	private EvaluationStrategyFactory evalStratFactory;

	public synchronized EvaluationStrategyFactory getEvaluationStrategyFactory() {
		if (evalStratFactory == null) {
			evalStratFactory = new StrictEvaluationStrategyFactory(getFederatedServiceResolver());
		}
		evalStratFactory.setQuerySolutionCacheThreshold(0);
		return evalStratFactory;
	}

	/**
	 * independent life cycle
	 */
	private FederatedServiceResolver serviceResolver;

	/**
	 * dependent life cycle
	 */
	private SPARQLServiceResolver dependentServiceResolver;

	public synchronized FederatedServiceResolver getFederatedServiceResolver() {
		if (serviceResolver == null) {
			if (dependentServiceResolver == null) {
				dependentServiceResolver = new SPARQLServiceResolver();
			}
			setFederatedServiceResolver(dependentServiceResolver);
		}
		return serviceResolver;
	}

	public void setEvaluationStrategyFactory(EvaluationStrategyFactory evalStratFactory) {
		this.evalStratFactory = evalStratFactory;

	}

	@Override
	synchronized protected void shutDownInternal() throws SailException {
		sailStore.close();
		sailStore = null;
	}

}
