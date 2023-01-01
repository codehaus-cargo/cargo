/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.module.webapp.resin.elements;

import org.codehaus.cargo.module.DescriptorElement;
import org.codehaus.cargo.module.webapp.resin.ResinWebXmlTag;
import org.codehaus.cargo.module.webapp.resin.ResinWebXmlType;
import org.jdom2.Element;

/**
 * Wrapper class representing a resin-web resource-ref entry.
 */
public class ResourceRef extends DescriptorElement
{

    /**
     * Constructor.
     * 
     * @param tag Resin Web Xml Tag
     */
    public ResourceRef(ResinWebXmlTag tag)
    {
        super(tag);
    }

    /**
     * Constructor.
     */
    public ResourceRef()
    {
        this((ResinWebXmlTag) ResinWebXmlType.getInstance().getTagByName(
            ResinWebXmlTag.RESOURCE_REFERENCE));
    }

    /**
     * @return Resource reference name.
     */
    public String getElementId()
    {
        Element child = getChild("res-ref-name");

        // Check there was one
        if (child == null)
        {
            return null;
        }

        return child.getText();
    }

    /**
     * Get the resource type that this is (eg JNDI).
     * 
     * @return the res-type value
     */
    public String getType()
    {
        Element child = getChild("res-type");

        // Check there was one
        if (child == null)
        {
            return null;
        }

        return child.getText();
    }

}
