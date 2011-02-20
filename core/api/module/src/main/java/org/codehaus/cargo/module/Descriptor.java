/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Common interface for Deployment Descriptors.
 * 
 * @version $Id$
 */
public interface Descriptor extends org.jdom.Parent
{
    /**
     * Returns the file name of this descriptor. For example "web.xml", "weblogic.xml", etc.
     * 
     * @return the file name
     */
    String getFileName();

    /**
     * 
     * @return this descriptor as a document
     */
    Document getDocument();

    /**
     * {@inheritDoc}
     * @see org.jdom.Parent#getRootElement
     */
    Element getRootElement();

    /**
     * Add an element into the descriptor.
     * 
     * @param tag Descriptor Tag
     * @param right Element insert before
     * @param rootElement Parent element
     * @return The added element
     */
    Element addElement(DescriptorTag tag, Element right, Element rootElement);

    /**
     * Get elements of a particular descriptor tag whose identifier matches the passed parameter.
     * 
     * @param tag tag to search for
     * @param value value for the identifier to match
     * @return the element that matches
     */
    Element getTagByIdentifier(DescriptorTag tag, String value);

    /**
     * Get tags of a particular type.
     * 
     * @param tag type of elements to find
     * @return list of tags
     */
    List<Element> getTags(DescriptorTag tag);

    /**
     * Get the descriptor type for this descriptor.
     * @return descriptor type.
     */
    DescriptorType getDescriptorType();

}
