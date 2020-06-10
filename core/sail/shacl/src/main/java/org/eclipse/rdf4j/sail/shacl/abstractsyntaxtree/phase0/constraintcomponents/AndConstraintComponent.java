package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sail.shacl.AST.ShaclProperties;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.Cache;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.HelperTool;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.NodeShape;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.PropertyShape;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.Shape;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.targets.TargetChain;

public class AndConstraintComponent extends AbstractConstraintComponent {
	List<Shape> and;

	public AndConstraintComponent(Resource id, RepositoryConnection connection,
			Cache cache) {
		super(id);
		and = HelperTool.toList(connection, id, Resource.class)
				.stream()
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

	}

	@Override
	public void toModel(Resource subject, Model model, Set<Resource> exported) {
		model.add(subject, SHACL.AND, getId());
		HelperTool.listToRdf(and.stream().map(Shape::getId).collect(Collectors.toList()), getId(), model);

		if (exported.contains(getId())) {
			return;
		}
		exported.add(getId());
		and.forEach(o -> o.toModel(null, model, exported));

	}

	@Override
	public void setTargetChain(TargetChain targetChain) {
		super.setTargetChain(targetChain);
		for (Shape shape : and) {
			shape.setTargetChain(targetChain.setOptimizable(false));
		}
	}
}