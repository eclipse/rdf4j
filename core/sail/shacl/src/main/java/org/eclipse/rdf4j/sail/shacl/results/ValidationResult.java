/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.results;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.sail.shacl.AST.Path;
import org.eclipse.rdf4j.sail.shacl.AST.PathPropertyShape;
import org.eclipse.rdf4j.sail.shacl.AST.PropertyShape;
import org.eclipse.rdf4j.sail.shacl.AST.SimplePath;
import org.eclipse.rdf4j.sail.shacl.SourceConstraintComponent;

/**
 * The ValidationResult represents the results from a SHACL validation in an easy-to-use Java API.
 *
 * @deprecated The ValidationResult is deprecated because it is planned moved to a new package to allow it to be used
 *             with remote validation results.
 */
@Deprecated
public class ValidationResult {

	private Resource id = SimpleValueFactory.getInstance().createBNode();

	private SourceConstraintComponent sourceConstraintComponent;
	private PropertyShape sourceShape;
	private Path path;
	private ValidationResult detail;
	private Value focusNode;
	private Value Expected;
	private Value Actual = SimpleValueFactory.getInstance().createLiteral("null");
	private Value ActualFormat = SimpleValueFactory.getInstance().createLiteral("null");

	public ValidationResult(PropertyShape sourceShape, Value focusNode) {
		this.sourceShape = sourceShape;
		this.focusNode = focusNode;
		this.sourceConstraintComponent = sourceShape.getSourceConstraintComponent();
		this.Expected = sourceShape.getEXP();
		if (sourceShape instanceof PathPropertyShape) {
			this.path = ((PathPropertyShape) sourceShape).getPath();
		}

	}

	public void setDetail(ValidationResult detail) {
		this.detail = detail;
	}

	/**
	 * @return ValidationResult with more information as to what failed. Usually for nested Shapes in eg. sh:or.
	 */
	public ValidationResult getDetail() {
		return detail;
	}

	/**
	 * @return all ValidationResult(s) with more information as to what failed. Usually for nested Shapes in eg. sh:or.
	 */
	public List<ValidationResult> getDetails() {

		ArrayList<ValidationResult> validationResults = new ArrayList<>();

		ValidationResult temp = detail;
		while (temp != null) {
			validationResults.add(temp);
			temp = temp.detail;
		}

		return validationResults;

	}

	public Model asModel(Model model) {

		model.add(getId(), RDF.TYPE, SHACL.VALIDATION_RESULT);

		model.add(getId(), SHACL.FOCUS_NODE, getFocusNode());
		model.add(getId(), SHACL.SOURCE_CONSTRAINT_COMPONENT, getSourceConstraintComponent().getIri());
		model.add(getId(), SHACL.SOURCE_SHAPE, getSourceShapeResource());
		model.add(getId(), SHACL.ACTUAL, getAct());
		model.add(getId(), SHACL.ACTUALFORMAT, getActFor());
		model.add(getId(), SHACL.EXPECTED, getExp());

		if (getPath() != null) {
			model.add(getId(), SHACL.RESULT_PATH, ((SimplePath) getPath()).getPath());
		}

		if (detail != null) {
			model.add(getId(), SHACL.DETAIL, detail.getId());
			detail.asModel(model);
		}

		return model;
	}

	/**
	 * @return the path, as specified in the Shape, that caused the violation
	 */
	private Path getPath() {
		return path;
	}

	/**
	 * @return the Resource (IRI or BNode) that identifies the source shape
	 */
	public Resource getSourceShapeResource() {
		return sourceShape.getId();
	}

	/**
	 * @return the focus node, aka. the subject, that caused the violation
	 */
	private Value getFocusNode() {
		return focusNode;
	}

	public Resource getId() {
		return id;
	}

	private Value getAct() {
		return Actual;
	}
	
	private Value getActFor() {
		return ActualFormat;
	}
	
	public void SetAct(Value Ac) {
		if(!(Ac instanceof Literal)) {
			return;
		}
		this.Actual = Ac;
		if(this.sourceConstraintComponent.equals(SourceConstraintComponent.MinCountConstraintComponent)) {
			this.ActualFormat = Ac;
		}
		if(this.sourceConstraintComponent.equals(SourceConstraintComponent.DatatypeConstraintComponent)) {
			this.ActualFormat = ((Literal)Ac).getDatatype();
		}
		if(this.sourceConstraintComponent.equals(SourceConstraintComponent.LanguageInConstraintComponent)) {
			Optional<String> x = ((Literal)Ac).getLanguage();
			if(x.isPresent())
			{
				this.ActualFormat = SimpleValueFactory.getInstance().createLiteral(x.get());
			}
		}
		if(this.sourceConstraintComponent.equals(SourceConstraintComponent.MaxCountConstraintComponent)) {
			this.ActualFormat = Ac;
		}
		if(this.sourceConstraintComponent.equals(SourceConstraintComponent.MaxLengthConstraintComponent)) {
			this.ActualFormat=SimpleValueFactory.getInstance().createLiteral((long)((Literal)Ac).stringValue().length());
		}
		if(this.sourceConstraintComponent.equals(SourceConstraintComponent.MinLengthConstraintComponent)) {
			this.ActualFormat=SimpleValueFactory.getInstance().createLiteral((long)((Literal)Ac).stringValue().length());
		}
		if(this.sourceConstraintComponent.equals(SourceConstraintComponent.NodeKindConstraintComponent)) {
			this.ActualFormat = SimpleValueFactory.getInstance().createLiteral(Ac.getClass().getSimpleName());
		}
		if(this.sourceConstraintComponent.equals(SourceConstraintComponent.PatternConstraintComponent)) {
			this.ActualFormat=Ac;
		}
		if(this.sourceConstraintComponent.equals(SourceConstraintComponent.MinExclusiveConstraintComponent)) {
			this.ActualFormat=Ac;
		}
		
	}

	private Value getExp() {
		return Expected;
	}

	/**
	 * @return the type of the source constraint that caused the violation
	 */
	public SourceConstraintComponent getSourceConstraintComponent() {
		return sourceConstraintComponent;
	}

	@Override
	public String toString() {
		return "ValidationResult{" +
				"sourceConstraintComponent=" + sourceConstraintComponent +
				", sourceShape=" + sourceShape +
				", path=" + path +
				", detail=" + detail +
				", focusNode=" + focusNode +
				'}';
	}
}
