/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.query.algebra.evaluation.util.ValueComparator;
import org.eclipse.rdf4j.sail.SailException;

public class Sort implements PlanNode {

	private final PlanNode parent;
	private boolean printed = false;
	private ValidationExecutionLogger validationExecutionLogger;
	final static ValueComparator valueComparator = new ValueComparator();

	public Sort(PlanNode parent) {
		this.parent = parent;
	}

	@Override
	public CloseableIteration<? extends ValidationTuple, SailException> iterator() {
		return new LoggingCloseableIteration(this, validationExecutionLogger) {

			final CloseableIteration<? extends ValidationTuple, SailException> iterator = parent.iterator();

			List<ValidationTuple> sortedTuples;

			Iterator<ValidationTuple> sortedTuplesIterator;

			@Override
			public void close() throws SailException {
				iterator.close();
			}

			@Override
			boolean localHasNext() throws SailException {
				sortTuples();
				return sortedTuplesIterator.hasNext();
			}

			private void sortTuples() {
				if (sortedTuples == null) {
					sortedTuples = new ArrayList<>();
					boolean alreadySorted = true;
					ValidationTuple prev = null;
					while (iterator.hasNext()) {
						ValidationTuple next = iterator.next();
						sortedTuples.add(next);
						if (prev != null
								&& valueComparator.compare(prev.getActiveTarget(), next.getActiveTarget()) > 0) {
							alreadySorted = false;
						}
						prev = next;
					}

					if (!alreadySorted && sortedTuples.size() > 1) {
						if (sortedTuples.size() > 8192) { // MIN_ARRAY_SORT_GRAN in Arrays.parallelSort(...)
							ValidationTuple[] objects = sortedTuples.toArray(new ValidationTuple[0]);
							Arrays.parallelSort(objects,
									(a, b) -> valueComparator.compare(a.getActiveTarget(), b.getActiveTarget()));
							sortedTuples = Arrays.asList(objects);
						} else {
							sortedTuples
									.sort((a, b) -> valueComparator.compare(a.getActiveTarget(), b.getActiveTarget()));
						}
					}
					sortedTuplesIterator = sortedTuples.iterator();

				}
			}

			@Override
			ValidationTuple loggingNext() throws SailException {
				sortTuples();

				return sortedTuplesIterator.next();
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
		return "Sort";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Sort)) {
			return false;
		}
		Sort sort = (Sort) o;
		return parent.equals(sort.parent);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parent);
	}

	@Override
	public void receiveLogger(ValidationExecutionLogger validationExecutionLogger) {
		this.validationExecutionLogger = validationExecutionLogger;
		parent.receiveLogger(validationExecutionLogger);
	}
}
