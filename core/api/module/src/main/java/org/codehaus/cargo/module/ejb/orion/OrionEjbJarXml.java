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
package org.codehaus.cargo.module.ejb.orion;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.DescriptorType;
import org.codehaus.cargo.module.ejb.EjbDef;
import org.codehaus.cargo.module.ejb.VendorEjbDescriptor;
import org.jdom.Element;

/**
 * Encapsulates the DOM representation of a oracle ejb deployment descriptor
 * <code>orion-ejb-jar.xml</code> to provide convenience methods for easy access and manipulation.
 * 
 */
public class OrionEjbJarXml extends AbstractDescriptor implements VendorEjbDescriptor
{
    /**
     * Constructor.
     * 
     * @param document The DOM document representing the parsed deployment descriptor
     * @param type the type of the descriptor
     */
    public OrionEjbJarXml(Element document, DescriptorType type)
    {
        super(document, type);
    }

    /**
     * {@inheritDoc}
     * @see VendorEjbDescriptor#getFileName()
     */
    public String getFileName()
    {
        return "orion-ejb-jar.xml";
    }

    /**
     * {@inheritDoc}
     * @see VendorEjbDescriptor#getJndiName(EjbDef)
     */
    public String getJndiName(EjbDef ejb)
    {
        String jndiName = null;
        Element ejbElement = getEjb(ejb.getName());
        if (ejbElement != null)
        {
            if (ejb.getLocal() != null)
            {
                jndiName = ejbElement.getAttribute("local-location").getValue();
            }
            if (jndiName == null)
            {
                jndiName = ejbElement.getAttribute("location").getValue();
            }
        }
        return jndiName;
    }

    /**
     * Returns a specific ejb.
     * 
     * @param ejbName the name of the ejb to get
     * @return the ejb or null if no ejb with that name exists
     */
    private Element getEjb(String ejbName)
    {
        Element ejbElement = getSessionEjb(ejbName);
        if (ejbElement == null)
        {
            ejbElement = getEntityEjb(ejbName);
        }

        return ejbElement;
    }

    /**
     * Returns a specific ejb.
     * 
     * @param ejbName the name of the ejb to get
     * @return the ejb or null if no ejb with that name exists
     */
    private Element getSessionEjb(String ejbName)
    {
        Element ejbElement = null;

        for (Element deploymentElement : getElements(new DescriptorTag(
            OrionEjbJarXmlType.getInstance(), "session-deployment", true)))
        {
            String name = deploymentElement.getAttribute("name").getValue();
            if (name.equals(ejbName))
            {
                ejbElement = deploymentElement;
                break;
            }
        }

        return ejbElement;
    }

    /**
     * Returns a specific ejb.
     * 
     * @param ejbName the name of the ejb to get
     * @return the ejb or null if no ejb with that name exists
     */
    private Element getEntityEjb(String ejbName)
    {
        Element ejbElement = null;

        for (Element deploymentElement : getElements(new DescriptorTag(
            OrionEjbJarXmlType.getInstance(), "entity-deployment", true)))
        {
            String name = deploymentElement.getAttribute("name").getValue();
            if (name.equals(ejbName))
            {
                ejbElement = deploymentElement;
                break;
            }
        }

        return ejbElement;
    }
}
