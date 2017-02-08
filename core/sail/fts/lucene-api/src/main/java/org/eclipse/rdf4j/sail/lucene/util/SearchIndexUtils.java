/*******************************************************************************
 * Copyright (c) 2017 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.lucene.util;

import java.util.Properties;
import static org.eclipse.rdf4j.sail.lucene.LuceneSail.DEFAULT_INDEX_CLASS;
import static org.eclipse.rdf4j.sail.lucene.LuceneSail.INDEX_CLASS_KEY;
import org.eclipse.rdf4j.sail.lucene.SearchIndex;

/**
 *
 * @author githib.com/jgrzebyta
 */
public class SearchIndexUtils {

    /**
     * The method is relocated to
     *
     * @param parameters
     * @return
     * @throws Exception
     */
    public static SearchIndex createSearchIndex(Properties parameters)
            throws Exception {
        String indexClassName = parameters.getProperty(INDEX_CLASS_KEY, DEFAULT_INDEX_CLASS);
        SearchIndex index = (SearchIndex) Class.forName(indexClassName).newInstance();
        index.initialize(parameters);
        return index;
    }
}
