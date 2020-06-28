/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.sail.SailException;

/**
 * @author Håvard Ottestad
 */
public class ValidationSingleCloseablePlanNode implements PlanNode {

	PlanNode parent;

	private ValidationExecutionLogger validationExecutionLogger;

	public ValidationSingleCloseablePlanNode(PlanNode parent) {
		this.parent = parent;

	}

	@Override
	public CloseableIteration<? extends ValidationTuple, SailException> iterator() {
		return new CloseableIteration<ValidationTuple, SailException>() {

			final CloseableIteration<? extends ValidationTuple, SailException> parentIterator = parent.iterator();
			final AtomicBoolean closed = new AtomicBoolean(false);

			@Override
			public void close() throws SailException {
				if (closed.compareAndSet(false, true)) {
					parentIterator.close();
				}
			}

			@Override
			public boolean hasNext() throws SailException {
				return parentIterator.hasNext();
			}

			@Override
			public ValidationTuple next() throws SailException {
				return parentIterator.next();
			}

			@Override
			public void remove() throws SailException {
				parentIterator.remove();
			}
		};

	}

	@Override
	public int depth() {
		throw new IllegalStateException();
	}

	@Override
	public void getPlanAsGraphvizDot(StringBuilder stringBuilder) {
		parent.getPlanAsGraphvizDot(stringBuilder);
	}

	@Override
	public String toString() {
		return "SingleCloseablePlanNode{" +
				"parent=" + parent +
				'}';
	}

	@Override
	public String getId() {
		return System.identityHashCode(this) + "";
	}

	@Override
	public void receiveLogger(ValidationExecutionLogger validationExecutionLogger) {
		parent.receiveLogger(validationExecutionLogger);
	}
}
