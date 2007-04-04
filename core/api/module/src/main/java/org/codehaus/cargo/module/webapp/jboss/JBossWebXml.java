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
package org.codehaus.cargo.module.webapp.jboss;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.Dtd;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.codehaus.cargo.module.webapp.VendorWebAppDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates the DOM representation of a web deployment descriptor
 * <code>jboss-web.xml</code> to provide convenience methods for easy access and
 * manipulation.
 *
 * @version $Id$
 */
public class JBossWebXml extends AbstractDescriptor implements VendorWebAppDescriptor
{
    /**
     * File name of this descriptor.
     */
    private static final String FILE_NAME = "jboss-web.xml";

    /**
     * Constructor.
     *
     * @param document The DOM document representing the parsed deployment
     *         descriptor
     */
    public JBossWebXml(Document document)
    {
        super(document, new Dtd("http://www.jboss.org/j2ee/dtd/jboss-web.dtd"));
    }

    /**
     * @return the context root element found in the <code>jboss-web.xml</code>
     *         file or null if not defined
     */
    public String getContextRoot()
    {
        String context = getNestedText(getRootElement(), JBossWebXmlTag.CONTEXT_ROOT);

        // Remove leading slash if there is one.
        if ((context != null) && context.startsWith("/"))
        {
            context = context.substring(1);
        }

        return context;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.Descriptor#getFileName()
     */
    public final String getFileName()
    {
        return FILE_NAME;
    }

    public void addEjbReference(EjbRef ref)
    {
        JBossWebXmlTag ejbRefTag = JBossWebXmlTag.EJB_REF;
        JBossWebXmlTag jndiTag = JBossWebXmlTag.JNDI_NAME;
        if(ref.isLocal())
        {
            ejbRefTag = JBossWebXmlTag.EJB_LOCAL_REF;
            jndiTag = JBossWebXmlTag.LOCAL_JNDI_NAME;
        }
        Element ejbRefElement =
            getDocument().createElement(ejbRefTag.getTagName());
        ejbRefElement.appendChild(createNestedText(JBossWebXmlTag.EJB_REF_NAME, ref.getName()));
        ejbRefElement.appendChild(createNestedText(jndiTag, ref.getJndiName()));
        addElement(JBossWebXmlTag.EJB_REF, ejbRefElement, getRootElement());
    }
}
