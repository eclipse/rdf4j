package org.eclipse.rdf4j.sail.shacl.abstractsyntaxtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class HelperTool {

	public static void listToRdf(Collection<? extends Value> values, Resource current, Model model) {
		// The Turtle parser does not add "a rdf:List" statements when parsing the shorthand list format,
		// so we don't add rdf:List when writing it out either.

		ValueFactory vf = SimpleValueFactory.getInstance();
		Iterator<? extends Value> iter = values.iterator();
		while (iter.hasNext()) {
			Value value = iter.next();
			model.add(current, RDF.FIRST, value);

			if (iter.hasNext()) {
				Resource next = vf.createBNode();
				model.add(current, RDF.REST, next);
				current = next;
			} else {
				model.add(current, RDF.REST, RDF.NIL);
			}
		}
	}

	private static List<Value> toList(RepositoryConnection connection, Resource orList) {
		List<Value> ret = new ArrayList<>();
		while (!orList.equals(RDF.NIL)) {
			try (Stream<Statement> stream = connection.getStatements(orList, RDF.FIRST, null).stream()) {
				Value value = stream.map(Statement::getObject).findAny().get();
				ret.add(value);
			}

			try (Stream<Statement> stream = connection.getStatements(orList, RDF.REST, null).stream()) {
				orList = stream.map(Statement::getObject).map(v -> (Resource) v).findAny().get();
			}

		}

		return ret;

	}

	public static <T extends Value> List<T> toList(RepositoryConnection connection, Resource orList, Class<T> type) {
		if (type == Value.class) {
			return (List<T>) toList(connection, orList);
		}

		List<T> ret = new ArrayList<>();
		while (!orList.equals(RDF.NIL)) {
			try (Stream<Statement> stream = connection.getStatements(orList, RDF.FIRST, null).stream()) {
				Value value = stream.map(Statement::getObject).findAny().get();
				if (type.isInstance(value)) {
					ret.add(((T) value));
				} else {
					throw new IllegalStateException("RDF list should contain only type " + type.getSimpleName()
							+ ", but found " + value.getClass().getSimpleName());
				}
			}

			try (Stream<Statement> stream = connection.getStatements(orList, RDF.REST, null).stream()) {
				orList = stream.map(Statement::getObject).map(v -> (Resource) v).findAny().get();
			}

		}

		return ret;

	}

}