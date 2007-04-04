/*
 * ========================================================================
 *
 * Copyright 2005-2007 Vincent Massol.
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
package org.codehaus.cargo.module.ejb.jboss;

import java.util.Iterator;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.Dtd;
import org.codehaus.cargo.module.ejb.EjbDef;
import org.codehaus.cargo.module.ejb.VendorEjbDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates the DOM representation of a jboss ejb deployment descriptor
 * <code>jboss.xml</code> to provide convenience methods for easy access and
 * manipulation.
 *
 * @version $Id: $
 */
public class JBossXml extends AbstractDescriptor implements VendorEjbDescriptor
{
    public JBossXml(Document document)
    {
        super(document, new Dtd("http://www.jboss.org/j2ee/dtd/jboss_4_0.dtd"));
    }

    public String getJndiName(EjbDef ejb)
    {
        String jndiName = null;

        Element ejbElement = getEjb(ejb.getName());
        if (ejbElement != null)
        {
            jndiName = getNestedText(ejbElement, JBossXmlTag.LOCAL_JNDI_NAME);
            if(jndiName == null)
            {
                jndiName = getNestedText(ejbElement, JBossXmlTag.JNDI_NAME);
            }
        }

        return jndiName;
    }

    public String getFileName()
    {
        return "jboss.xml";
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

        Iterator names = getElements(JBossXmlTag.EJB_NAME);
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
