/*******************************************************************************
 * Copyright (c) 2020 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.federated.evaluation.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import org.eclipse.rdf4j.federated.FedXConfig;

/**
 * A {@link TaskWrapper} is a facility to wrap {@link Runnable} background tasks before they are passed to the
 * {@link Executor}. Use-cases include injection of thread-local context variables, or more fine-granular error
 * handling.
 * 
 * <p>
 * All sub-queries sent by the federation engine that make use of the concurrency infrastructure (e.g.
 * {@link ControlledWorkerScheduler}) are passing this wrapper.
 * </p>
 * 
 * <p>
 * The concrete implementation can be configured using {@link FedXConfig#withTaskWrapper(TaskWrapper)}.
 * </p>
 * 
 * @author Andreas Schwarte
 * @see ControlledWorkerScheduler
 * @see FedXConfig#withTaskWrapper(TaskWrapper)
 */
public interface TaskWrapper {


	/**
	 * Wrap the given {@link Runnable} and add custom logic.
	 * 
	 * <p>
	 * Use cases include injection of state into the thread-local context, or more fine granular error handling.
	 * </p>
	 * 
	 * <p>
	 * Note that when modifying state in {@link ThreadLocal} it must be reset properly in a try/finally block.
	 * </p>
	 * 
	 * @param runnable the task as generated by the FedX engine
	 * @return the wrapped {@link Runnable}
	 */
	Runnable wrap(Runnable runnable);

	/**
	 * Wrap the given {@link Callable} and add custom logic.
	 * 
	 * <p>
	 * Use cases include injection of state into the thread-local context, or more fine granular error handling.
	 * </p>
	 * 
	 * <p>
	 * Note that when modifying state in {@link ThreadLocal} it must be reset properly in a try/finally block.
	 * </p>
	 * 
	 * @param callable the task as generated by the FedX engine
	 * @return the wrapped {@link Callable}
	 */
	<T> Callable<T> wrap(Callable<T> callable);

}
