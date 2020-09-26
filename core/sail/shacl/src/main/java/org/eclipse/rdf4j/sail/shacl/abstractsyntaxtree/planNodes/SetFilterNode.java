/*******************************************************************************
 * .Copyright (c) 2020 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.SailException;

public class SetFilterNode implements PlanNode {

	private Set<Value> targetNodeList;
	private PlanNode parent;
	private int index;
	private boolean returnValid;
	private boolean printed;
	private ValidationExecutionLogger validationExecutionLogger;

	public SetFilterNode(Set<Value> targetNodeList, PlanNode parent, int index, boolean returnValid) {
		parent = PlanNodeHelper.handleSorting(this, parent);
		this.targetNodeList = targetNodeList;
		this.parent = parent;
		this.index = index;
		this.returnValid = returnValid;
	}

	@Override
	public CloseableIteration<? extends ValidationTuple, SailException> iterator() {
		return new LoggingCloseableIteration(this, validationExecutionLogger) {

			CloseableIteration<? extends ValidationTuple, SailException> iterator = parent.iterator();

			ValidationTuple next;

			private void calulateNext() {
				while (next == null && iterator.hasNext()) {
					ValidationTuple temp = iterator.next();
					boolean contains = targetNodeList.contains(temp.getActiveTarget());
					if (returnValid && contains) {
						next = temp;
					} else if (!returnValid && !contains) {
						next = temp;
					}
				}
			}

			@Override
			public void close() throws SailException {
				iterator.close();
			}

			@Override
			boolean localHasNext() throws SailException {
				calulateNext();
				return next != null;
			}

			@Override
			ValidationTuple loggingNext() throws SailException {
				calulateNext();

				ValidationTuple temp = next;
				next = null;

				return temp;
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
	public String getId() {
		return System.identityHashCode(this) + "";
	}

	@Override
	public String toString() {
		return "SetFilterNode{" + "targetNodeList="
				+ Arrays.toString(targetNodeList.stream().map(Formatter::prefix).toArray()) + ", index=" + index
				+ ", returnValid=" + returnValid + '}';
	}

	@Override
	public void receiveLogger(ValidationExecutionLogger validationExecutionLogger) {
		this.validationExecutionLogger = validationExecutionLogger;
		parent.receiveLogger(validationExecutionLogger);
	}

	@Override
	public boolean producesSorted() {
		return parent.producesSorted();
	}

	@Override
	public boolean requiresSorted() {
		return false;
	}
}