package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.constraintcomponents;

import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.SHACL;

public class MaxInclusiveConstraintComponent extends AbstractConstraintComponent {

	Literal maxInclusive;

	public MaxInclusiveConstraintComponent(Literal maxInclusive) {
		this.maxInclusive = maxInclusive;
	}

	@Override
	public void toModel(Resource subject, IRI predicate, Model model, Set<Resource> exported) {
		model.add(subject, SHACL.MAX_INCLUSIVE, maxInclusive);
	}
}
