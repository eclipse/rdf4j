package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.constraintcomponents;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.sail.shacl.ConnectionsGroup;
import org.eclipse.rdf4j.sail.shacl.SourceConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.paths.Path;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.BulkedExternalInnerJoin;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.InnerJoin;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.NonUniqueTargetLang;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNodeProvider;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.Select;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.TrimToTarget;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.UnBufferedPlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.UnionNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.Unique;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.ValidationTuple;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.targets.EffectiveTarget;

public class UniqueLangConstraintComponent extends AbstractConstraintComponent {

	public UniqueLangConstraintComponent() {
	}

	@Override
	public void toModel(Resource subject, IRI predicate, Model model, Set<Resource> exported) {
		model.add(subject, SHACL.UNIQUE_LANG, SimpleValueFactory.getInstance().createLiteral(true));
	}

	@Override
	public SourceConstraintComponent getConstraintComponent() {
		return SourceConstraintComponent.UniqueLangConstraintComponent;
	}

	@Override
	public PlanNode generateSparqlValidationPlan(ConnectionsGroup connectionsGroup, boolean logValidationPlans,
			boolean negatePlan, boolean negateChildren) {
		assert !negateChildren : "There are no subplans!";
		assert !negatePlan;

		if (!targetChain.getPath().isPresent()) {
			throw new IllegalStateException("UniqueLang only operates on paths");
		}

		String targetVarPrefix = "target_";

		ComplexQueryFragment complexQueryFragment = getComplexQueryFragment(targetVarPrefix);

		String query = complexQueryFragment.getQuery();

		return new Select(connectionsGroup.getBaseConnection(), query, b -> {

			List<String> targetVars = b.getBindingNames()
					.stream()
					.filter(s -> s.startsWith(targetVarPrefix))
					.sorted()
					.collect(Collectors.toList());

			ValidationTuple validationTuple = new ValidationTuple(b, targetVars);
			validationTuple.setPath(targetChain.getPath().get());

			return validationTuple;

		}, null);

	}

	private ComplexQueryFragment getComplexQueryFragment(String targetVarPrefix) {

		EffectiveTarget effectiveTarget = targetChain.getEffectiveTarget(targetVarPrefix);
		String query = effectiveTarget.getQuery();

		Var targetVar = effectiveTarget.getTargetVar();

		String pathQuery1 = targetChain.getPath()
				.map(p -> p.getQueryFragment(effectiveTarget.getTargetVar(), new Var("value1")))
				.get();

		String pathQuery2 = targetChain.getPath()
				.map(p -> p.getQueryFragment(effectiveTarget.getTargetVar(), new Var("value2")))
				.get();

		query += "\n FILTER(EXISTS{" +
				"\n" + query +
				"\n" + pathQuery1 +
				"\n" + pathQuery2 +
				"FILTER(?value1 != ?value2 && lang(?value1) = lang(?value2) && lang(?value1) != \"\")" +
				"} )";

		return new ComplexQueryFragment(query, targetVarPrefix, targetVar, null);

	}

	@Override
	public PlanNode generateTransactionalValidationPlan(ConnectionsGroup connectionsGroup, boolean logValidationPlans,
			PlanNodeProvider overrideTargetNode, boolean negatePlan, boolean negateChildren) {
		assert !negateChildren : "There are no subplans!";
		assert !negatePlan;

		EffectiveTarget effectiveTarget = targetChain.getEffectiveTarget("target_");
		Optional<Path> path = targetChain.getPath();

		if (!path.isPresent()) {
			throw new IllegalStateException("UniqueLang only operates on paths");
		}

		if (overrideTargetNode != null) {
			PlanNode relevantTargetsWithPath = new BulkedExternalInnerJoin(
					overrideTargetNode.getPlanNode(),
					connectionsGroup.getBaseConnection(),
					path.get().getQueryFragment(new Var("a"), new Var("c")),
					false,
					null,
					(b) -> new ValidationTuple(b.getValue("a"), path.get(), b.getValue("c"))
			);

			return new TrimToTarget(new NonUniqueTargetLang(relevantTargetsWithPath), true);
		}

		if (connectionsGroup.getStats().isBaseSailEmpty()) {
			PlanNode addedTargets = effectiveTarget.getAdded(connectionsGroup);

			PlanNode addedByPath = path.get().getAdded(connectionsGroup, null);

			PlanNode innerJoin = new InnerJoin(addedTargets, addedByPath).getJoined(UnBufferedPlanNode.class);

			return new TrimToTarget(new NonUniqueTargetLang(innerJoin), true);

		}

		PlanNode addedTargets = effectiveTarget.getAdded(connectionsGroup);

		PlanNode addedByPath = path.get().getAdded(connectionsGroup, null);

		addedByPath = effectiveTarget.getTargetFilter(connectionsGroup, addedByPath);

		PlanNode mergeNode = new UnionNode(addedTargets, addedByPath);

		PlanNode trimmed = new TrimToTarget(mergeNode);

		PlanNode allRelevantTargets = new Unique(trimmed);

		PlanNode relevantTargetsWithPath = new BulkedExternalInnerJoin(
				allRelevantTargets,
				connectionsGroup.getBaseConnection(),
				path.get().getQueryFragment(new Var("a"), new Var("c")),
				false,
				null,
				(b) -> new ValidationTuple(b.getValue("a"), path.get(), b.getValue("c"))
		);

		return new TrimToTarget(new NonUniqueTargetLang(relevantTargetsWithPath), true);

	}
}