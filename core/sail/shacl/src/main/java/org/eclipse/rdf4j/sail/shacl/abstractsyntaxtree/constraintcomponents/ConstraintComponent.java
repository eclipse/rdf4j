package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.constraintcomponents;

import org.eclipse.rdf4j.sail.shacl.ConnectionsGroup;
import org.eclipse.rdf4j.sail.shacl.SourceConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.Exportable;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.TargetChainInterface;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.ValidationApproach;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNodeProvider;

public interface ConstraintComponent extends Exportable, TargetChainInterface {

	PlanNode generateSparqlValidationPlan(ConnectionsGroup connectionsGroup, boolean logValidationPlans,
			boolean negatePlan, boolean negateChildren);

	PlanNode generateTransactionalValidationPlan(ConnectionsGroup connectionsGroup,
			boolean logValidationPlans, PlanNodeProvider overrideTargetNode, boolean negatePlan,
			boolean negateChildren);

	ValidationApproach getPreferedValidationApproach();

	SourceConstraintComponent getConstraintComponent();
}
