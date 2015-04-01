/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
 * Contains methods for getting information from a XSD.
 * 
 */
public class Xsd implements Grammar
{

    /**
     * Contructor.
     * 
     * @param xsdPath path (URL) of the XSD to parse
     */
    public Xsd(String xsdPath)
    {
        // Empty for now, maybe one day we'll do something with the XSD path
    }

    /**
     * {@inheritDoc}
     * @see Grammar#getElementOrder(String)
     */
    public List<DescriptorTag> getElementOrder(String tagName)
    {
        // The XSDs that we handle (webapp 2.5, webapp 3.0, etc.) do not have any order
        return null;
    }
}
