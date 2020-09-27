package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree;

import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.DynamicModel;
import org.eclipse.rdf4j.model.impl.LinkedHashModelFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.shacl.AST.ShaclProperties;
import org.eclipse.rdf4j.sail.shacl.ConnectionsGroup;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.constraintcomponents.ConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.paths.Path;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.DebugPlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.EmptyNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNodeProvider;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.ShiftToNodeShape;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.TargetChainPopper;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.UnionNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.Unique;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.ValidationReportNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.targets.TargetChain;
import org.eclipse.rdf4j.sail.shacl.results.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyShape extends Shape implements ConstraintComponent, Identifiable {
	private static final Logger logger = LoggerFactory.getLogger(PropertyShape.class);

	List<String> name;
	List<String> description;
	Object defaultValue;
	Object group;

	Path path;

	public PropertyShape() {
	}

	public PropertyShape(PropertyShape propertyShape) {
		super(propertyShape);
		this.name = propertyShape.name;
		this.description = propertyShape.description;
		this.defaultValue = propertyShape.defaultValue;
		this.group = propertyShape.group;
		this.path = propertyShape.path;
	}

	public static PropertyShape getInstance(ShaclProperties properties, RepositoryConnection connection, Cache cache,
			ShaclSail shaclSail) {
		Shape shape = cache.get(properties.getId());
		if (shape == null) {
			shape = new PropertyShape();
			cache.put(properties.getId(), shape);
			shape.populate(properties, connection, cache, shaclSail);
		}

		if (shape.constraintComponents.isEmpty()) {
			shape.deactivated = true;
		}

		return (PropertyShape) shape;
	}

	@Override
	public void populate(ShaclProperties properties, RepositoryConnection connection,
			Cache cache, ShaclSail shaclSail) {
		super.populate(properties, connection, cache, shaclSail);

		this.path = Path.buildPath(connection, properties.getPath());

		if (this.path == null) {
			throw new IllegalStateException(properties.getId() + " is a sh:PropertyShape without a sh:path!");
		}

		constraintComponents = getConstraintComponents(properties, connection, cache, shaclSail);
	}

	@Override
	protected Shape shallowClone() {
		return new PropertyShape(this);
	}

	@Override
	public void toModel(Resource subject, IRI predicate, Model model, Set<Resource> exported) {

		super.toModel(subject, predicate, model, exported);
		model.add(getId(), RDF.TYPE, SHACL.PROPERTY_SHAPE);

		if (subject != null) {
			if (predicate == null) {
				model.add(subject, SHACL.PROPERTY, getId());
			} else {
				model.add(subject, predicate, getId());
			}
		}

		model.add(getId(), SHACL.PATH, path.getId());
		path.toModel(path.getId(), null, model, exported);

		if (exported.contains(getId())) {
			return;
		}
		exported.add(getId());

		constraintComponents.forEach(c -> c.toModel(getId(), null, model, exported));

	}

	@Override
	public void setTargetChain(TargetChain targetChain) {
		super.setTargetChain(targetChain.add(path));
	}

	@Override
	public PlanNode generateSparqlValidationPlan(ConnectionsGroup connectionsGroup,
			boolean logValidationPlans, boolean negatePlan, boolean negateChildren, Scope scope) {
		if (isDeactivated()) {
			return new EmptyNode();
		}

		PlanNode union = new EmptyNode();

		for (ConstraintComponent constraintComponent : constraintComponents) {
			PlanNode validationPlanNode = constraintComponent
					.generateSparqlValidationPlan(connectionsGroup, logValidationPlans, negatePlan, false,
							Scope.propertyShape);

			if (!(constraintComponent instanceof PropertyShape)) {
				validationPlanNode = new ValidationReportNode(validationPlanNode, t -> {
					return new ValidationResult(t.getActiveTarget(), t.getValue(), this,
							constraintComponent.getConstraintComponent(), getSeverity(), t.getScope());
				});
			}

			validationPlanNode = new TargetChainPopper(validationPlanNode);

			union = new UnionNode(union, validationPlanNode);
		}

		return union;
	}

	@Override
	public PlanNode generateTransactionalValidationPlan(ConnectionsGroup connectionsGroup,
			boolean logValidationPlans, PlanNodeProvider overrideTargetNode, boolean negatePlan,
			boolean negateChildren, Scope scope) {

		if (isDeactivated()) {
			return new EmptyNode();
		}

		PlanNode union = new EmptyNode();

//		if (negatePlan) {
//			assert overrideTargetNode == null : "Negated property shape with override target is not supported at the moment!";
//
//			PlanNode ret = new EmptyNode();
//
//			for (ConstraintComponent constraintComponent : constraintComponents) {
//				PlanNode planNode = constraintComponent.generateTransactionalValidationPlan(connectionsGroup,
//						logValidationPlans, () -> getAllLocalTargetsPlan(connectionsGroup, negatePlan), negateChildren,
//						false, Scope.propertyShape);
//
//				PlanNode allTargetsPlan = getAllLocalTargetsPlan(connectionsGroup, negatePlan);
//
//				Unique invalid = new Unique(planNode);
//
//				PlanNode discardedLeft = new InnerJoin(allTargetsPlan, invalid)
//						.getDiscardedLeft(BufferedPlanNode.class);
//
//				ret = new UnionNode(ret, discardedLeft);
//
//			}
//
//			return ret;
//
//		}

		for (ConstraintComponent constraintComponent : constraintComponents) {
			PlanNode validationPlanNode = constraintComponent
					.generateTransactionalValidationPlan(connectionsGroup, logValidationPlans, overrideTargetNode,
							negateChildren,
							false, Scope.propertyShape);

			validationPlanNode = new DebugPlanNode(validationPlanNode, "", p -> {
//				System.out.println(constraintComponent);
//				System.out.println(scope);
//				assert p != null;
			});

			if (!(constraintComponent instanceof PropertyShape)) {
				validationPlanNode = new ValidationReportNode(validationPlanNode, t -> {
					return new ValidationResult(t.getActiveTarget(), t.getValue(), this,
							constraintComponent.getConstraintComponent(), getSeverity(), t.getScope());
				});
			}

			if (scope == Scope.propertyShape) {
				validationPlanNode = new TargetChainPopper(validationPlanNode);
			} else {
				validationPlanNode = new ShiftToNodeShape(validationPlanNode);
			}

			validationPlanNode = new DebugPlanNode(validationPlanNode, "", p -> {
//				assert p != null;
			});

			union = new UnionNode(union, validationPlanNode);
		}

		return union;
	}

	@Override
	public PlanNode getAllTargetsPlan(ConnectionsGroup connectionsGroup, boolean negated, Scope scope) {
		PlanNode planNode = constraintComponents.stream()
				.map(c -> c.getAllTargetsPlan(connectionsGroup, negated, Scope.propertyShape))
				.reduce(UnionNode::new)
				.orElse(new EmptyNode());

		planNode = new UnionNode(planNode,
				getTargetChain()
						.getEffectiveTarget("_target", Scope.propertyShape,
								connectionsGroup.getRdfsSubClassOfReasoner())
						.getPlanNode(connectionsGroup, Scope.propertyShape, true));

		if (scope == Scope.propertyShape) {
			planNode = new TargetChainPopper(planNode);
		} else {
			planNode = new ShiftToNodeShape(planNode);
		}

		planNode = new Unique(planNode);

		planNode = new DebugPlanNode(planNode, "PropertyShapeGetAllTargetsPlan::getAllTargetsPlan");

		return planNode;
	}

	@Override
	public ValidationApproach getPreferedValidationApproach() {
		return constraintComponents.stream()
				.map(ConstraintComponent::getPreferedValidationApproach)
				.reduce(ValidationApproach::reduce)
				.orElse(ValidationApproach.Transactional);
	}

	public Path getPath() {
		return path;
	}

	@Override
	public String toString() {
		Model statements = toModel(new DynamicModel(new LinkedHashModelFactory()));
		StringWriter stringWriter = new StringWriter();
		Rio.write(statements, stringWriter, RDFFormat.TURTLE);
		return stringWriter.toString();
	}

	@Override
	public ConstraintComponent deepClone() {
		PropertyShape nodeShape = new PropertyShape(this);

		constraintComponents.stream()
				.map(ConstraintComponent::deepClone)
				.collect(Collectors.toList());

		nodeShape.constraintComponents = constraintComponents;

		return nodeShape;
	}

}
