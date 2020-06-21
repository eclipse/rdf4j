/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.planNodes;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.sail.SailException;

/**
 * @author Håvard Ottestad
 */
public class TrimToTarget implements PlanNode {

	PlanNode parent;
	private boolean printed = false;
	private ValidationExecutionLogger validationExecutionLogger;

	public TrimToTarget(PlanNode parent) {
		this.parent = parent;
	}

	@Override
	public CloseableIteration<? extends ValidationTuple, SailException> iterator() {
		return new LoggingCloseableIteration(this, validationExecutionLogger) {

			final CloseableIteration<? extends ValidationTuple, SailException> parentIterator = parent.iterator();

			@Override
			public void close() throws SailException {
				parentIterator.close();
			}

			@Override
			boolean localHasNext() throws SailException {
				return parentIterator.hasNext();
			}

			@Override
			ValidationTuple loggingNext() throws SailException {

				ValidationTuple next = parentIterator.next();

				next.setValue(null);
				return next;
			}

			@Override
			public void remove() throws SailException {

			}
		};

	}

	@Override
	public int depth() {
		return parent.depth() + 1;
	}

	@Override
	public void getPlanAsGraphvizDot(StringBuilder stringBuilder) {
		if (printed) {
			return;
		}
		printed = true;
		stringBuilder.append(getId() + " [label=\"" + StringEscapeUtils.escapeJava(this.toString()) + "\"];")
				.append("\n");
		stringBuilder.append(parent.getId() + " -> " + getId()).append("\n");
		parent.getPlanAsGraphvizDot(stringBuilder);
	}

	@Override
	public String toString() {
		return "TrimToTarget";
	}

	@Override
	public String getId() {
		return System.identityHashCode(this) + "";
	}

	@Override
	public void receiveLogger(ValidationExecutionLogger validationExecutionLogger) {
		this.validationExecutionLogger = validationExecutionLogger;
		parent.receiveLogger(validationExecutionLogger);
	}
}
