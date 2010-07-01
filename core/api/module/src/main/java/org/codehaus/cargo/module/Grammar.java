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

/**
 * Contains methods for retrieving grammar information for an XML file.
 * 
 * @version $Id$
 */
public interface Grammar
{
    /**
     * Returns a List of {@link DescriptorTag} that describes the order that
     * elements can appear in a certain element accordingly to the grammar.
     * @param tagName the tag name to get the element order of.
     * @return a List of {@link DescriptorTag} or null if tagName doesn't exist
     */
    List getElementOrder(String tagName);
}
