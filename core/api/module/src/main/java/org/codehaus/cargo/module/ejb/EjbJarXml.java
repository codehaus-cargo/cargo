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
package org.codehaus.cargo.module.ejb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.Dtd;
import org.codehaus.cargo.module.J2eeDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates the DOM representation of a ejb deployment descriptor
 * <code>ejb-jar.xml</code> to provide convenience methods for easy access and manipulation.
 *
 * @version $Id$
 */
public class EjbJarXml extends AbstractDescriptor implements J2eeDescriptor
{
    /**
     * List of vendor descriptors associated with this ejb-jar.xml.
     */
    private List vendorDescriptors = new ArrayList();

    /**
     * Constructor.
     *
     * @param theDocument The DOM document representing the parsed deployment
     *         descriptor
     */
    public EjbJarXml(Document theDocument)
    {
        super(theDocument, new Dtd("http://java.sun.com/dtd/ejb-jar_2_0.dtd"));
    }

    /**
     * {@inheritDoc}
     * @see J2eeDescriptor#getFileName()
     */
    public String getFileName()
    {
        return "ejb-jar.xml";
    }

    /**
     * Associates a vendor specific descriptor with this web.xml.
     *
     * @param descr the vendor specific dexcriptor to associate
     */
    public void addVendorDescriptor(VendorEjbDescriptor descr)
    {
        this.vendorDescriptors.add(descr);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.J2eeDescriptor#getVendorDescriptors()
     */
    public Iterator getVendorDescriptors()
    {
        return this.vendorDescriptors.iterator();
    }

    /**
     * Returns all session ejbs in this descriptor.
     *
     * @return Iterator of Ssession objects representing all session ejbs
     */
    public final Iterator getSessionEjbs()
    {
        List ejbs = new ArrayList();
        Iterator sessionElements = getElements(EjbJarXmlTag.SESSION);
        while (sessionElements.hasNext())
        {
            Element sessionElement = (Element) sessionElements.next();
            Session session = new Session();
            session.setId(sessionElement.getAttribute("id"));
            session.setName(getChildText(sessionElement, EjbJarXmlTag.EJB_NAME));
            session.setLocal(getChildText(sessionElement, EjbJarXmlTag.LOCAL));
            session.setLocalHome(getChildText(sessionElement, EjbJarXmlTag.LOCAL_HOME));
            ejbs.add(session);
        }

        return ejbs.iterator();
    }

    /**
     * Returns all entity ejbs in this descriptor.
     *
     * @return Iterator of Entity objects representing all entity ejbs
     */
    public final Iterator getEntityEjbs()
    {
        List ejbs = new ArrayList();
        Iterator sessionElements = getElements(EjbJarXmlTag.ENTITY);
        while (sessionElements.hasNext())
        {
            Element sessionElement = (Element) sessionElements.next();
            Entity entity = new Entity();
            entity.setId(sessionElement.getAttribute("id"));
            entity.setName(getChildText(sessionElement, EjbJarXmlTag.EJB_NAME));
            entity.setLocal(getChildText(sessionElement, EjbJarXmlTag.LOCAL));
            entity.setLocalHome(getChildText(sessionElement, EjbJarXmlTag.LOCAL_HOME));
            ejbs.add(entity);
        }

        return ejbs.iterator();
    }

    /**
     * Return a specific session definition.
     *
     * @param name the name of the ejb.
     * @return the Session
     */
    public final Session getSessionEjb(String name)
    {
        Session result = null;
        Iterator sessions = getSessionEjbs();
        while (sessions.hasNext())
        {
            Session ejb = (Session) sessions.next();
            if (ejb.getName().equals(name))
            {
                result = ejb;
                break;
            }
        }

        return result;
    }

    /**
     * Return a specific entity definition.
     *
     * @param name the name of the ejb.
     * @return the Entity
     */
    public final Entity getEntityEjb(String name)
    {
        Entity result = null;
        Iterator entities = getEntityEjbs();
        while (entities.hasNext())
        {
            Entity ejb = (Entity) entities.next();
            if (ejb.getName().equals(name))
            {
                result = ejb;
                break;
            }
        }

        return result;
    }
}
