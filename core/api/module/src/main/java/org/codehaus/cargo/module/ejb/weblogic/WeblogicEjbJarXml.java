/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.module.ejb.weblogic;

import java.util.Iterator;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.Dtd;
import org.codehaus.cargo.module.ejb.EjbDef;
import org.codehaus.cargo.module.ejb.VendorEjbDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates the DOM representation of a weblogic ejb deployment descriptor 
 * <code>weblogic-ejb-jar.xml</code> to provide convenience methods for easy access and 
 * manipulation.
 *
 * @version $Id$
 */
public class WeblogicEjbJarXml extends AbstractDescriptor implements VendorEjbDescriptor
{
    /**
     * Constructor.
     * 
     * @param document The DOM document representing the parsed deployment descriptor
     */
    public WeblogicEjbJarXml(Document document)
    {
        super(document, new Dtd("http://www.bea.com/servers/wls810/dtd/weblogic-ejb-jar.dtd"));
    }
    
    /**
     * {@inheritDoc}
     * @see VendorEjbDescriptor#getFileName()
     */
    public String getFileName()
    {
        return "weblogic-ejb-jar.xml";
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
            jndiName = getNestedText(ejbElement, WeblogicEjbJarXmlTag.LOCAL_JNDI_NAME);
            if (jndiName == null)
            {
                jndiName = getNestedText(ejbElement, WeblogicEjbJarXmlTag.JNDI_NAME);
            }
        }
        
        return jndiName;
    }

    /**
     * Adds a dispatch policy to a ejb definition.
     *
     * @param ejb The ejb to be modified
     * @param policy The policy to add
     * @throws IllegalArgumentException if the given ejb does not exist in the descriptor
     */
    public void addDispatchPolicy(EjbDef ejb, String policy)
    {
        Element ejbElement = getEjb(ejb.getName());
        if (ejbElement == null)
        {
            throw new IllegalArgumentException("No ejb named " + ejb.getName() + " found.");
        }
        else
        {
            ejbElement.appendChild(createNestedText(WeblogicEjbJarXmlTag.DISPATCH_POLICY, policy));
        }
    }

    /**
     * Returns the dispatch policy for a given ejb.
     *
     * @param ejb The ejb to get the dispatch policy for
     * @return the dispatch policy or null if no one is specified
     * @throws IllegalArgumentException if the given ejb does not exist in the descriptor
     */
    public String getDispatchPolicy(EjbDef ejb)
    {
        String policy;

        Element ejbElement = getEjb(ejb.getName());
        if (ejbElement == null)
        {
            throw new IllegalArgumentException("No ejb named " + ejb.getName() + " found.");
        }
        else
        {
            policy = getNestedText(ejbElement, WeblogicEjbJarXmlTag.DISPATCH_POLICY);
        }

        return policy;
    }
    
    /**
     * Returns a specific ejb.
     * 
     * @param ejbName the name of the ejb to get
     * @return the ejb or null if no ejb with that name exists
     */
    private Element getEjb(String ejbName)
    {
        Element ejbElement = null;
        
        Iterator names = getElements(WeblogicEjbJarXmlTag.EJB_NAME);
        while (names.hasNext())
        {
            Element nameElement = (Element) names.next();
            String name = nameElement.getFirstChild().getNodeValue();
            if (ejbName.equals(name))
            {
                ejbElement = (Element) nameElement.getParentNode();
                break;
            }
        }
        
        return ejbElement;
    }
}
