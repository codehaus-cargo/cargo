/* 
 * ========================================================================
 * 
 * Copyright 2004-2005 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.codehaus.cargo.module;

import java.io.IOException;
import java.io.InputStream;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;

/**
 * @version $Id: $
 */
public interface DescriptorIo
{
    /**
     * Parse XML into a document.
     * 
     * @param documentStream stream containing XML 
     * @return the document
     * @throws IOException if error reading
     * @throws JDOMException if error constructing document
     */
    Document parseXml(InputStream documentStream) throws JDOMException, IOException;

    /**
     * @param theInput Input XML stream
     * @param theEntityResolver Entity Resolver
     * @return the document
     * @throws IOException if error reading
     * @throws JDOMException if error constructing document
     */
    Document parseXml(InputStream theInput, EntityResolver theEntityResolver) 
        throws JDOMException, IOException;
    
    /**
     * Create a document builder. 
     * @return new document builder
     */
    SAXBuilder createDocumentBuilder();
   
    /**
     * @param theEntityResolver entity resolver or null
     * @return a new non-validating, non-namespace-aware {@link DocumentBuilder} instance
     */
    SAXBuilder createDocumentBuilder(EntityResolver theEntityResolver);   
}
