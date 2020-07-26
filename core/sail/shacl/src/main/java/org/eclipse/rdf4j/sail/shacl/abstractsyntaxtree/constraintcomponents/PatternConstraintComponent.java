package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.constraintcomponents;

import java.util.Set;
import java.util.function.Function;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sail.shacl.SourceConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.FilterPlanNode;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PatternFilter;
import org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree.planNodes.PlanNode;

public class PatternConstraintComponent extends SimpleAbstractConstraintComponent {

	String pattern;
	String flags;

	public PatternConstraintComponent(String pattern, String flags) {
		this.pattern = pattern;
		this.flags = flags;

		if (flags == null)
			flags = "";
	}

	@Override
	public void toModel(Resource subject, IRI predicate, Model model, Set<Resource> exported) {
		model.add(subject, SHACL.PATTERN, SimpleValueFactory.getInstance().createLiteral(pattern));
		if (flags != null) {
			model.add(subject, SHACL.FLAGS, SimpleValueFactory.getInstance().createLiteral(flags));
		}

	}

	@Override
	String getFilter(String varName, boolean negated) {
		if (negated) {
			return "!isBlank(?" + varName + ") && REGEX(STR(?" + varName + "), \"" + pattern + "\", \"" + flags
					+ "\") ";
		} else {
			return " isBlank(?" + varName + ") || !REGEX(STR(?" + varName + "), \"" + pattern + "\", \"" + flags
					+ "\") ";
		}
	}

	@Override
	public SourceConstraintComponent getConstraintComponent() {
		return SourceConstraintComponent.PatternConstraintComponent;
	}

	@Override
	Function<PlanNode, FilterPlanNode> getFilterAttacher() {
		return (parent) -> new PatternFilter(parent, pattern, flags);
	}
}
