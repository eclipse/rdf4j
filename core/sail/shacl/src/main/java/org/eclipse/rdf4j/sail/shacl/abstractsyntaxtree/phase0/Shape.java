package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sail.shacl.AST.ShaclProperties;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.AndConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.ClassConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.ConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.DatatypeConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.InConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.LanguageInConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.MaxCountConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.MaxExclusiveConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.MaxInclusiveConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.MaxLengthConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.MinCountConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.MinExclusiveConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.MinInclusiveConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.MinLengthConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.NodeKindConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.NotConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.OrConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.PatternConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents.UniqueLangConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.targets.Target;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.targets.TargetClass;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.targets.TargetNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.targets.TargetObjectsOf;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.targets.TargetSubjectsOf;

abstract public class Shape implements ConstraintComponent, Identifiable, Exportable {
	Resource id;

	List<Target> target = new ArrayList<>();

	boolean deactivated;
	List<Literal> message;
	Severity severity;

	List<ConstraintComponent> constraintComponent = new ArrayList<>();

	public Shape() {
	}

	public Shape(Shape shape) {
		this.deactivated = shape.deactivated;
		this.message = shape.message;
		this.severity = shape.severity;
		this.id = shape.id;
	}

	public void populate(ShaclProperties properties, RepositoryConnection connection,
			Cache cache) {
		this.deactivated = properties.isDeactivated();
		this.message = properties.getMessage();
		this.id = properties.getId();

		if (!properties.getTargetClass().isEmpty()) {
			target.add(new TargetClass(properties.getTargetClass()));
		}
		if (!properties.getTargetNode().isEmpty()) {
			target.add(new TargetNode(properties.getTargetNode()));
		}
		if (!properties.getTargetObjectsOf().isEmpty()) {
			target.add(new TargetObjectsOf(properties.getTargetObjectsOf()));
		}
		if (!properties.getTargetSubjectsOf().isEmpty()) {
			target.add(new TargetSubjectsOf(properties.getTargetSubjectsOf()));
		}

	}

	@Override
	public Resource getId() {
		return id;
	}

	public static class Factory {

		public static List<Shape> getShapes(RepositoryConnection connection, ShaclSail sail) {

			Cache cache = new Cache();

			Set<Resource> resources = getTargetableShapes(connection);

			List<Shape> collect = resources.stream()
					.map(r -> new ShaclProperties(r, connection))
					.map(p -> {
						if (p.getType() == SHACL.NODE_SHAPE) {
							return NodeShape.getInstance(p, connection, cache);
						} else if (p.getType() == SHACL.PROPERTY_SHAPE) {
							return PropertyShape.getInstance(p, connection, cache);
						}
						throw new IllegalStateException("Unknown shape type for " + p.getId());
					})
					.collect(Collectors.toList());

			// split into shapes by target and constraint component to be able to run them in parallel
			return collect.stream().flatMap(s -> {
				List<Shape> temp = new ArrayList<>();
				s.target.forEach(target -> {
					s.constraintComponent.forEach(constraintComponent -> {

						if (constraintComponent instanceof PropertyShape) {
							((PropertyShape) constraintComponent).constraintComponent
									.forEach(propertyConstraintComponent -> {
										PropertyShape clonedConstraintComponent = (PropertyShape) ((PropertyShape) constraintComponent)
												.shallowClone();
										clonedConstraintComponent.constraintComponent.add(propertyConstraintComponent);

										Shape shape = s.shallowClone();
										shape.target.add(target);
										shape.constraintComponent.add(clonedConstraintComponent);
										temp.add(shape);
									});

						} else {
							Shape shape = s.shallowClone();
							shape.target.add(target);
							shape.constraintComponent.add(constraintComponent);
							temp.add(shape);

						}

					});
				});
				return temp.stream();
			}).collect(Collectors.toList());

		}

		private static Set<Resource> getTargetableShapes(RepositoryConnection connection) {
			Set<Resource> collect;
			try (Stream<Statement> TARGET_NODE = connection.getStatements(null, SHACL.TARGET_NODE, null, true)
					.stream()) {
				try (Stream<Statement> TARGET_CLASS = connection.getStatements(null, SHACL.TARGET_CLASS, null, true)
						.stream()) {
					try (Stream<Statement> TARGET_SUBJECTS_OF = connection
							.getStatements(null, SHACL.TARGET_SUBJECTS_OF, null, true)
							.stream()) {
						try (Stream<Statement> TARGET_OBJECTS_OF = connection
								.getStatements(null, SHACL.TARGET_OBJECTS_OF, null, true)
								.stream()) {

							collect = Stream
									.of(TARGET_CLASS, TARGET_NODE, TARGET_OBJECTS_OF, TARGET_SUBJECTS_OF)
									.reduce(Stream::concat)
									.get()
									.map(Statement::getSubject)
									.collect(Collectors.toSet());
						}

					}
				}
			}
			return collect;
		}
	}

	protected abstract Shape shallowClone();

	public void toModel(Model model) {
		toModel(null, model, new HashSet<>());
	}

	public void toModel(Resource subject, Model model, Set<Resource> exported) {
		ModelBuilder modelBuilder = new ModelBuilder();

		modelBuilder.subject(getId());

		if (deactivated) {
			modelBuilder.add(SHACL.DEACTIVATED, deactivated);
		}

		target.forEach(t -> {
			t.toModel(getId(), model, exported);
		});

		model.addAll(modelBuilder.build());
	}

	List<ConstraintComponent> getConstraintComponents(ShaclProperties properties, RepositoryConnection connection,
			Cache cache) {

		List<ConstraintComponent> constraintComponent = new ArrayList<>();

		properties.getProperty()
				.stream()
				.map(r -> new ShaclProperties(r, connection))
				.map(p -> PropertyShape.getInstance(p, connection, cache))
				.forEach(constraintComponent::add);

		properties.getNode()
				.stream()
				.map(r -> new ShaclProperties(r, connection))
				.map(p -> NodeShape.getInstance(p, connection, cache))
				.forEach(constraintComponent::add);

		if (properties.getMinCount() != null) {
			constraintComponent.add(new MinCountConstraintComponent(properties.getMinCount()));
		}

		if (properties.getMaxCount() != null) {
			constraintComponent.add(new MaxCountConstraintComponent(properties.getMaxCount()));
		}

		if (properties.getDatatype() != null) {
			constraintComponent.add(new DatatypeConstraintComponent(properties.getDatatype()));
		}

		if (properties.getMinLength() != null) {
			constraintComponent.add(new MinLengthConstraintComponent(properties.getMinLength()));
		}

		if (properties.getMaxLength() != null) {
			constraintComponent.add(new MaxLengthConstraintComponent(properties.getMaxLength()));
		}

		if (properties.getMinInclusive() != null) {
			constraintComponent.add(new MinInclusiveConstraintComponent(properties.getMinInclusive()));
		}

		if (properties.getMaxInclusive() != null) {
			constraintComponent.add(new MaxInclusiveConstraintComponent(properties.getMaxInclusive()));
		}

		if (properties.getMinExclusive() != null) {
			constraintComponent.add(new MinExclusiveConstraintComponent(properties.getMinExclusive()));
		}

		if (properties.getMaxExclusive() != null) {
			constraintComponent.add(new MaxExclusiveConstraintComponent(properties.getMaxExclusive()));
		}

		if (properties.isUniqueLang()) {
			constraintComponent.add(new UniqueLangConstraintComponent());
		}

		if (properties.getPattern() != null) {
			constraintComponent
					.add(new PatternConstraintComponent(properties.getPattern(), properties.getFlags()));
		}

		if (properties.getLanguageIn() != null) {
			constraintComponent.add(new LanguageInConstraintComponent(connection, properties.getLanguageIn()));
		}

		if (properties.getIn() != null) {
			constraintComponent.add(new InConstraintComponent(connection, properties.getIn()));
		}

		if (properties.getNodeKind() != null) {
			constraintComponent.add(new NodeKindConstraintComponent(properties.getNodeKind()));
		}

		properties.getOr()
				.stream()
				.map(or -> new OrConstraintComponent(or, connection, cache))
				.forEach(constraintComponent::add);

		properties.getAnd()
				.stream()
				.map(and -> new AndConstraintComponent(and, connection, cache))
				.forEach(constraintComponent::add);

		properties.getNot()
				.stream()
				.map(or -> new NotConstraintComponent(or, connection, cache))
				.forEach(constraintComponent::add);

		properties.getClazz()
				.stream()
				.map(clazz -> new ClassConstraintComponent(clazz))
				.forEach(constraintComponent::add);

		return constraintComponent;
	}
}
