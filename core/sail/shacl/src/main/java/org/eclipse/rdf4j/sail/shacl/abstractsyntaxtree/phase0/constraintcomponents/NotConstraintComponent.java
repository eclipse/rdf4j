package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.constraintcomponents;

import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sail.shacl.AST.ShaclProperties;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.Cache;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.NodeShape;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.PropertyShape;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.Shape;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.phase0.targets.TargetChain;

public class NotConstraintComponent extends AbstractConstraintComponent {
	final Shape not;

	public NotConstraintComponent(Resource id, RepositoryConnection connection,
			Cache cache) {
		super(id);

		ShaclProperties p = new ShaclProperties(id, connection);

		if (p.getType() == SHACL.NODE_SHAPE) {
			not = NodeShape.getInstance(p, connection, cache);
		} else if (p.getType() == SHACL.PROPERTY_SHAPE) {
			not = PropertyShape.getInstance(p, connection, cache);
		} else {
			throw new IllegalStateException("Unknown shape type for " + p.getId());
		}

	}

	@Override
	public void toModel(Resource subject, IRI predicate, Model model, Set<Resource> exported) {
		model.add(subject, SHACL.NOT, getId());

		not.toModel(null, null, model, exported);

	}

	@Override
	public void setTargetChain(TargetChain targetChain) {
		super.setTargetChain(targetChain);
		not.setTargetChain(targetChain.setOptimizable(false));
	}

}