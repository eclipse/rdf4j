/**
 * Copyright (c) 2017 Eclipse RDF4J contributors, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.eclipse.rdf4j.model.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

/**
 * Utility functions for working with vocabularies.
 * 
 * @author Bart Hanssens
 */
public class VocabUtil {
    
    /**
     * Get all the {@link IRI IRIs} of the classes and properties of a vocabulary.
     *  
     * @param vocabulary RDF vocabulary
     * @return set of IRIs
     */
    public static Set<IRI> getVocabularyIRIs(Class vocabulary) {
        Set<IRI> iris = new HashSet<>();
        
        for(Field f : vocabulary.getFields ()) {
            if (f.getType().equals(IRI.class) && Modifier.isPublic(f.getModifiers())) {
                try {
                    iris.add((IRI) f.get(f));
                } catch (IllegalArgumentException|IllegalAccessException ex) {
                   // should not happen
                }
            }
        }
        return iris;
    }
}
