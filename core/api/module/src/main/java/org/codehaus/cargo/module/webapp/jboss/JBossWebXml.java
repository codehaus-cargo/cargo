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
package org.codehaus.cargo.module.webapp.jboss;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.DescriptorType;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.codehaus.cargo.module.webapp.VendorWebAppDescriptor;
import org.jdom2.Element;

/**
 * Encapsulates the DOM representation of a web deployment descriptor <code>jboss-web.xml</code> to
 * provide convenience methods for easy access and manipulation.
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
     * @param rootElement The root document element
     * @param type The document type
     */
    public JBossWebXml(Element rootElement, DescriptorType type)
    {
        super(rootElement, type);
    }

    /**
     * @return the context root element found in the <code>jboss-web.xml</code> file or null if not
     * defined
     */
    public String getContextRoot()
    {
        String context = getNestedText(
            getRootElement(), getDescriptorType().getTagByName(JBossWebXmlTag.CONTEXT_ROOT));

        // Remove leading slash if there is one.
        if (context != null && context.startsWith("/"))
        {
            context = context.substring(1);
        }

        return context;
    }

    /**
     * @return the virtual host element found in the <code>jboss-web.xml</code> file or null if not
     * defined
     */
    public String getVirtualHost()
    {
        return getNestedText(
            getRootElement(), getDescriptorType().getTagByName(JBossWebXmlTag.VIRTUAL_HOST));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileName()
    {
        return FILE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEjbReference(EjbRef ref)
    {
        JBossWebXmlTag ejbRefTag = (JBossWebXmlTag) getDescriptorType().getTagByName(
                JBossWebXmlTag.EJB_REF);
        JBossWebXmlTag jndiTag = (JBossWebXmlTag) getDescriptorType().getTagByName(
                JBossWebXmlTag.JNDI_NAME);
        if (ref.isLocal())
        {
            ejbRefTag = (JBossWebXmlTag) getDescriptorType().getTagByName(
                    JBossWebXmlTag.EJB_LOCAL_REF);
            jndiTag = (JBossWebXmlTag) getDescriptorType().getTagByName(
                    JBossWebXmlTag.LOCAL_JNDI_NAME);
        }

        Element ejbRefElement = ejbRefTag.create();

        ejbRefElement.addContent(createNestedText(getDescriptorType().getTagByName(
                JBossWebXmlTag.EJB_REF_NAME), ref.getName()));
        ejbRefElement.addContent(createNestedText(jndiTag, ref.getJndiName()));

        getRootElement().addContent(ejbRefElement);
    }
}
