package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.constraintcomponents;

import java.util.Collections;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sail.shacl.ConnectionsGroup;
import org.eclipse.rdf4j.sail.shacl.RdfsSubClassOfReasoner;
import org.eclipse.rdf4j.sail.shacl.SourceConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.StatementMatcher;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.paths.Path;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.BulkedExternalInnerJoin;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.DebugPlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.EmptyNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.ExternalPredicateObjectFilter;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNodeProvider;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.ShiftToPropertyShape;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.TrimToTarget;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.UnionNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.Unique;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.UnorderedSelect;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.ValidationTuple;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.targets.EffectiveTarget;

public class ClassConstraintComponent extends AbstractConstraintComponent {

	Resource clazz;

	public ClassConstraintComponent(Resource clazz) {
		this.clazz = clazz;
	}

	@Override
	public void toModel(Resource subject, IRI predicate, Model model, Set<Resource> exported) {
		model.add(subject, SHACL.CLASS, clazz);
	}

	@Override
	public SourceConstraintComponent getConstraintComponent() {
		return SourceConstraintComponent.ClassConstraintComponent;
	}

	@Override
	public ConstraintComponent deepClone() {
		return new ClassConstraintComponent(clazz);
	}

	@Override
	public PlanNode generateTransactionalValidationPlan(ConnectionsGroup connectionsGroup, boolean logValidationPlans,
			PlanNodeProvider overrideTargetNode, Scope scope) {

		EffectiveTarget target = getTargetChain().getEffectiveTarget("_target", scope,
				connectionsGroup.getRdfsSubClassOfReasoner());

		if (scope == Scope.propertyShape) {
			Path path = getTargetChain().getPath().get();

			PlanNode addedTargets;

			if (overrideTargetNode != null) {
				addedTargets = overrideTargetNode.getPlanNode();
				addedTargets = target.extend(addedTargets, connectionsGroup, scope, EffectiveTarget.Extend.right,
						false);

			} else {
				addedTargets = target.getPlanNode(connectionsGroup, scope, false);
				PlanNode addedByPath = path.getAdded(connectionsGroup, null);

				addedByPath = target.getTargetFilter(connectionsGroup, new Unique(new TrimToTarget(addedByPath)));

				addedByPath = target.extend(addedByPath, connectionsGroup, scope, EffectiveTarget.Extend.left, false);

				if (connectionsGroup.getStats().hasRemoved()) {
					PlanNode deletedTypes = new UnorderedSelect(connectionsGroup.getRemovedStatements(), null, RDF.TYPE,
							clazz, s -> new ValidationTuple(s.getSubject(), Scope.nodeShape, false));

					deletedTypes = new DebugPlanNode(deletedTypes, p -> {
						assert p != null;
					});

					deletedTypes = getTargetChain()
							.getEffectiveTarget("target_", Scope.nodeShape,
									connectionsGroup.getRdfsSubClassOfReasoner())
							.extend(deletedTypes, connectionsGroup, Scope.nodeShape, EffectiveTarget.Extend.left,
									false);

					deletedTypes = new DebugPlanNode(deletedTypes, p -> {
						assert p != null;
					});

					deletedTypes = getTargetChain()
							.getEffectiveTarget("target_", Scope.nodeShape,
									connectionsGroup.getRdfsSubClassOfReasoner())
							.getTargetFilter(connectionsGroup, deletedTypes);

					deletedTypes = new DebugPlanNode(deletedTypes, p -> {
						assert p != null;
					});

					addedTargets = new UnionNode(addedTargets,
							new TrimToTarget(new ShiftToPropertyShape(deletedTypes)));
				}

				addedTargets = new UnionNode(addedByPath, addedTargets);
				addedTargets = new Unique(addedTargets);
			}

			PlanNode joined = new BulkedExternalInnerJoin(
					addedTargets,
					connectionsGroup.getBaseConnection(),
					path.getTargetQueryFragment(new StatementMatcher.Variable("a"), new StatementMatcher.Variable("c"),
							connectionsGroup.getRdfsSubClassOfReasoner()),
					false,
					null,
					(b) -> new ValidationTuple(b.getValue("a"), b.getValue("c"), scope, true)
			);

			RdfsSubClassOfReasoner rdfsSubClassOfReasoner = connectionsGroup.getRdfsSubClassOfReasoner();
			Set<Resource> clazzForwardChained = rdfsSubClassOfReasoner.backwardsChain(clazz);

			// filter by type against the base sail
			PlanNode falseNode = new ExternalPredicateObjectFilter(
					connectionsGroup.getBaseConnection(),
					RDF.TYPE, clazzForwardChained,
					joined, false, ExternalPredicateObjectFilter.FilterOn.value);

			return falseNode;

		} else if (scope == Scope.nodeShape) {

			PlanNode addedTargets;

			if (overrideTargetNode != null) {
				addedTargets = overrideTargetNode.getPlanNode();
				addedTargets = target.extend(addedTargets, connectionsGroup, scope, EffectiveTarget.Extend.right,
						false);
			} else {
				addedTargets = target.getPlanNode(connectionsGroup, scope, false);

				if (connectionsGroup.getStats().hasRemoved()) {
					PlanNode deletedTypes = new UnorderedSelect(connectionsGroup.getRemovedStatements(), null, RDF.TYPE,
							clazz, s -> new ValidationTuple(s.getSubject(), scope, false));
					deletedTypes = getTargetChain()
							.getEffectiveTarget("target_", scope, connectionsGroup.getRdfsSubClassOfReasoner())
							.getTargetFilter(connectionsGroup, deletedTypes);
					deletedTypes = getTargetChain()
							.getEffectiveTarget("target_", scope, connectionsGroup.getRdfsSubClassOfReasoner())
							.extend(deletedTypes, connectionsGroup, scope, EffectiveTarget.Extend.left, false);
					addedTargets = new UnionNode(addedTargets, new TrimToTarget(deletedTypes));
				}
			}

			// filter by type against the base sail
			PlanNode falseNode = new ExternalPredicateObjectFilter(
					connectionsGroup.getBaseConnection(),
					RDF.TYPE, Collections.singleton(clazz),
					addedTargets, false, ExternalPredicateObjectFilter.FilterOn.value);

			falseNode = new DebugPlanNode(falseNode, p -> {
				assert p != null;
			});

			return falseNode;

		} else {
			throw new UnsupportedOperationException("Unknown scope: " + scope);
		}

	}

	@Override
	public PlanNode getAllTargetsPlan(ConnectionsGroup connectionsGroup, Scope scope) {
		if (scope == Scope.propertyShape) {
			PlanNode allTargetsPlan = getTargetChain()
					.getEffectiveTarget("target_", Scope.nodeShape, connectionsGroup.getRdfsSubClassOfReasoner())
					.getPlanNode(connectionsGroup, Scope.nodeShape, true);

			// removed type statements that match clazz could affect sh:or
			if (connectionsGroup.getStats().hasRemoved()) {
				PlanNode deletedTypes = new UnorderedSelect(connectionsGroup.getRemovedStatements(), null, RDF.TYPE,
						clazz, s -> new ValidationTuple(s.getSubject(), Scope.nodeShape, false));
				deletedTypes = getTargetChain()
						.getEffectiveTarget("target_", Scope.nodeShape, connectionsGroup.getRdfsSubClassOfReasoner())
						.getTargetFilter(connectionsGroup, deletedTypes);
				deletedTypes = getTargetChain()
						.getEffectiveTarget("target_", Scope.nodeShape, connectionsGroup.getRdfsSubClassOfReasoner())
						.extend(deletedTypes, connectionsGroup, Scope.nodeShape, EffectiveTarget.Extend.left, false);
				allTargetsPlan = new UnionNode(allTargetsPlan, deletedTypes);
			}

			// added type statements that match clazz could affect sh:not
			if (connectionsGroup.getStats().hasAdded()) {
				PlanNode addedTypes = new UnorderedSelect(connectionsGroup.getAddedStatements(), null, RDF.TYPE,
						clazz, s -> new ValidationTuple(s.getSubject(), Scope.nodeShape, false));
				addedTypes = getTargetChain()
						.getEffectiveTarget("target_", Scope.nodeShape, connectionsGroup.getRdfsSubClassOfReasoner())
						.getTargetFilter(connectionsGroup, addedTypes);
				addedTypes = getTargetChain()
						.getEffectiveTarget("target_", Scope.nodeShape, connectionsGroup.getRdfsSubClassOfReasoner())
						.extend(addedTypes, connectionsGroup, Scope.nodeShape, EffectiveTarget.Extend.left, false);
				allTargetsPlan = new UnionNode(allTargetsPlan, addedTypes);
			}

			return new Unique(new TrimToTarget(new ShiftToPropertyShape(allTargetsPlan)));
		}
		PlanNode allTargetsPlan = new EmptyNode();

		// removed type statements that match clazz could affect sh:or
		if (connectionsGroup.getStats().hasRemoved()) {
			PlanNode deletedTypes = new UnorderedSelect(connectionsGroup.getRemovedStatements(), null, RDF.TYPE, clazz,
					s -> new ValidationTuple(s.getSubject(), Scope.nodeShape, false));
			deletedTypes = getTargetChain()
					.getEffectiveTarget("target_", Scope.nodeShape, connectionsGroup.getRdfsSubClassOfReasoner())
					.getTargetFilter(connectionsGroup, deletedTypes);
			deletedTypes = getTargetChain()
					.getEffectiveTarget("target_", Scope.nodeShape, connectionsGroup.getRdfsSubClassOfReasoner())
					.extend(deletedTypes, connectionsGroup, Scope.nodeShape, EffectiveTarget.Extend.left, false);
			allTargetsPlan = new UnionNode(allTargetsPlan, deletedTypes);

		}

		// added type statements that match clazz could affect sh:not
		if (connectionsGroup.getStats().hasAdded()) {
			PlanNode addedTypes = new UnorderedSelect(connectionsGroup.getAddedStatements(), null, RDF.TYPE, clazz,
					s -> new ValidationTuple(s.getSubject(), Scope.nodeShape, false));
			addedTypes = getTargetChain()
					.getEffectiveTarget("target_", Scope.nodeShape, connectionsGroup.getRdfsSubClassOfReasoner())
					.getTargetFilter(connectionsGroup, addedTypes);
			addedTypes = getTargetChain()
					.getEffectiveTarget("target_", Scope.nodeShape, connectionsGroup.getRdfsSubClassOfReasoner())
					.extend(addedTypes, connectionsGroup, Scope.nodeShape, EffectiveTarget.Extend.left, false);
			allTargetsPlan = new UnionNode(allTargetsPlan, addedTypes);

		}

		return new Unique(allTargetsPlan);
	}

	@Override
	public boolean requiresEvaluation(ConnectionsGroup connectionsGroup, Scope scope) {
		return super.requiresEvaluation(connectionsGroup, scope)
				|| connectionsGroup.getRemovedStatements().hasStatement(null, RDF.TYPE, clazz, true)
				|| connectionsGroup.getAddedStatements().hasStatement(null, RDF.TYPE, clazz, true);
	}
}