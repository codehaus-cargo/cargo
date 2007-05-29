/*
 * ========================================================================
 *
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.resin;

import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.DescriptorType;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.codehaus.cargo.module.webapp.VendorWebAppDescriptor;
import org.jdom.Element;

/**
 * Encapsulates the DOM representation of a web deployment descriptor <code>resin-web.xml</code>
 * to provide convenience methods for easy access and manipulation.
 *
 * @version $Id $
 */
public class ResinWebXml extends AbstractDescriptor implements VendorWebAppDescriptor
{
    /**
     * File name of this descriptor.
     */
    private static final String FILE_NAME = "resin-web.xml";
   
    /**
     * Constructor.
     *
     * @param rootElement The descriptor root element
     * @param type The descriptor type
     */
    public ResinWebXml(Element rootElement, DescriptorType type)
    {
        super(rootElement, type);
    }

    /**
     * Adds a ejb reference.
     *
     * @param ref reference to add
     */
    public void addEjbReference(EjbRef ref)
    {
        // This file doesn't have EJB references in it
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.Descriptor#getFileName()
     */
    public String getFileName()
    {
        return FILE_NAME;
    }

    /**
     * Get the system properties.
     *
     * @return the system properties
     */
    public List getSystemProperties()
    {
        return getTags(ResinWebXmlTag.SYSTEM_PROPERTY);
    }

    /**
     * Get the resource references.
     *
     * @return the resource refs
     */
    public List getResourceRefs()
    {
        return getTags(ResinWebXmlTag.RESOURCE_REFERENCE);
    }

    /**
     * Get the jndi links.
     *
     * @return the jndi links
     */
    public List getJndiLinks()
    {
        return getTags(ResinWebXmlTag.JNDI_LINK);
    }

    /**
     * Get the session config element.
     *
     * @return session config element
     */
    public Element getSessionConfig()
    {
        Iterator i = getElements(ResinWebXmlTag.SESSION_CONFIG);
        if (!i.hasNext())
        {
            return null;
        }

        return (Element) i.next();
    }

    /**
     * Get the directory servlet.
     *
     * @return directory servlet element
     */
    public Element getDirectoryServlet()
    {      
        Iterator i = getElements(ResinWebXmlTag.DIRECTORY_SERVLET);
        if (!i.hasNext())
        {
            return null;
        }
        return (Element) i.next();
    }
}
