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
package org.codehaus.cargo.module.ejb.websphere;

import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.DescriptorType;
import org.codehaus.cargo.module.ejb.EjbDef;
import org.codehaus.cargo.module.ejb.VendorEjbDescriptor;
import org.jdom.Element;


/**
 * Encapsulates the DOM representation of a websphere ejb deployment descriptor 
 * <code>ibm-ejb-jar-bnd.xmi</code> to provide convenience methods for easy access and manipulation.
 *
 * @version $Id$
 */
public class IbmEjbJarBndXmi extends AbstractDescriptor implements VendorEjbDescriptor
{
    /**
     * Constructor.
     * 
     * @param rootElement The root document element
     * @param type The descriptor type
     */
    public IbmEjbJarBndXmi(Element rootElement, DescriptorType type)
    {
        super(rootElement, type);
    }
    
    /**
     * {@inheritDoc}
     * @see VendorEjbDescriptor#getFileName()
     */
    public String getFileName()
    {
        return "ibm-ejb-jar-bnd.xmi";
    }
    
    /**
     * {@inheritDoc}
     * @see VendorEjbDescriptor#getJndiName(EjbDef)
     */
    public String getJndiName(EjbDef ejb)
    {
        String jndiName = null;
        Element bindings = getEjbBindings(ejb.getId());
        if (bindings != null)
        {
            jndiName = bindings.getAttribute("jndiName").getValue();
        }
        return jndiName;
    }
    
    /**
     * Returns a specific ejb binding.
     * 
     * @param id the name of the ejb to get
     * @return the ejb or null if no ejb with that name exists
     */
    private Element getEjbBindings(String id)
    {
        Element ejbElement = null;
        String wantedHref = "META-INF/ejb-jar.xml#" + id;
        Iterator names = getElements(
            new DescriptorTag(IbmEjbJarBndXmiType.getInstance(), "ejbBindings", true));
        while (names.hasNext())
        {
            Element bindingsElement = (Element) names.next();
            List nl = bindingsElement.getChildren("enterpriseBean");
            Element beanElement = (Element) nl.get(0);
            String href = beanElement.getAttribute("href").getValue();
            if (wantedHref.equals(href))
            {
                ejbElement = bindingsElement;
                break;
            }
        }
        
        return ejbElement;
    }
}
