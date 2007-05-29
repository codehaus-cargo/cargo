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

import java.util.Collection;

import org.jdom.JDOMFactory;

/**
 * Represents a "type" of descriptor - e.g. web.xml
 * @version $Id: $
 */
public interface DescriptorType
{
    /**
     * @return XML Grammar for this descriptor.
     */
    Grammar getGrammar();
    
    /** 
     * @return JDOM Factory that can create typed descriptor elements.
     */
    JDOMFactory getJDOMFactory();

    /**
     * @return class for serializing descriptors of this type.
     */
    DescriptorIo getDescriptorIo();

    /**
     * Get a descriptor tag by name.
     * 
     * @param name the name of the tag
     * @return the descriptor tag, or null if not found
     */
    DescriptorTag getTagByName(String name);

    /**
     * Get all defined tags for this descriptor.
     * 
     * @return a collection of all the tags
     */
    Collection getAllTags();

    /**
     * Add a descriptor tag into this descriptor.
     * 
     * @param tag the tag to add
     */
    void addTag(DescriptorTag tag);
}
