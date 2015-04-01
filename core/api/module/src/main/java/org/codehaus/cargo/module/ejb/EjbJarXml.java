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
package org.codehaus.cargo.module.ejb;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.Descriptor;
import org.codehaus.cargo.module.DescriptorType;
import org.codehaus.cargo.module.J2eeDescriptor;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Encapsulates the DOM representation of a ejb deployment descriptor <code>ejb-jar.xml</code> to
 * provide convenience methods for easy access and manipulation.
 * 
 */
public class EjbJarXml extends AbstractDescriptor implements J2eeDescriptor
{
    /**
     * List of vendor descriptors associated with this ejb-jar.xml.
     */
    private List<Descriptor> vendorDescriptors = new ArrayList<Descriptor>();

    /**
     * Constructor.
     * 
     * @param rootElement the root element of the document
     * @param type the document descriptor type
     */
    public EjbJarXml(Element rootElement, DescriptorType type)
    {
        super(rootElement, type);
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
    public List<Descriptor> getVendorDescriptors()
    {
        return this.vendorDescriptors;
    }

    /**
     * Returns all session ejbs in this descriptor.
     * 
     * @return List of Session objects representing all session ejbs
     */
    public List<Session> getSessionEjbs()
    {
        List<Session> ejbs = new ArrayList<Session>();
        for (Element sessionElement : getElements(EjbJarXmlTag.SESSION))
        {
            Session session = new Session();
            Attribute id = sessionElement.getAttribute("id");
            if (id != null)
            {
                session.setId(id.getValue());
            }
            session.setName(getChildText(sessionElement, EjbJarXmlTag.EJB_NAME));
            session.setLocal(getChildText(sessionElement, EjbJarXmlTag.LOCAL));
            session.setLocalHome(getChildText(sessionElement, EjbJarXmlTag.LOCAL_HOME));
            ejbs.add(session);
        }

        return ejbs;
    }

    /**
     * Returns all entity ejbs in this descriptor.
     * 
     * @return List of Entity objects representing all entity ejbs
     */
    public List<Entity> getEntityEjbs()
    {
        List<Entity> ejbs = new ArrayList<Entity>();
        for (Element entityElement : getElements(EjbJarXmlTag.ENTITY))
        {
            Entity entity = new Entity();
            Attribute id = entityElement.getAttribute("id");
            if (id != null)
            {
                entity.setId(id.getValue());
            }
            entity.setName(getChildText(entityElement, EjbJarXmlTag.EJB_NAME));
            entity.setLocal(getChildText(entityElement, EjbJarXmlTag.LOCAL));
            entity.setLocalHome(getChildText(entityElement, EjbJarXmlTag.LOCAL_HOME));
            ejbs.add(entity);
        }

        return ejbs;
    }

    /**
     * Return a specific session definition.
     * 
     * @param name the name of the ejb.
     * @return the Session
     */
    public Session getSessionEjb(String name)
    {
        Session result = null;
        for (Session ejb : getSessionEjbs())
        {
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
    public Entity getEntityEjb(String name)
    {
        Entity result = null;
        for (Entity ejb : getEntityEjbs())
        {
            if (ejb.getName().equals(name))
            {
                result = ejb;
                break;
            }
        }

        return result;
    }
}
