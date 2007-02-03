/* 
 * ========================================================================
 * 
 * Copyright 2005 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.weblogic;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.Dtd;
import org.codehaus.cargo.module.webapp.VendorWebAppDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * Encapsulates the DOM representation of a weblogic web deployment descriptor 
 * <code>weblogic.xml</code> to provide convenience methods for easy access and manipulation.
 *
 * @version $Id$
 */
public class WeblogicXml extends AbstractDescriptor implements VendorWebAppDescriptor
{
    /**
     * File name of this descriptor.
     */
    private static final String FILE_NAME = "weblogic.xml";
    
    /**
     * Constructor.
     * 
     * @param document The DOM document representing the parsed deployment descriptor
     */
    public WeblogicXml(Document document)
    {
        super(document, new Dtd("http://www.bea.com/servers/wls810/dtd/weblogic810-web-jar.dtd"));
    }
    
    /**
     * @return weblogic.xml
     */
    public final String getFileName()
    {
        return FILE_NAME;
    }
    
    /**
     * Adds a ejb reference description to the weblogic.xml.
     * @param name name of the reference
     * @param jndiName jndi name to map
     */
    public final void addEjbReference(String name, String jndiName)
    {
        Element refDescr;
        Iterator i = getElements(WeblogicXmlTag.REFERENCE_DESCRIPTOR);
        if (i.hasNext())
        {
            refDescr = (Element) i.next();
        }
        else
        {
            refDescr = 
                getDocument().createElement(WeblogicXmlTag.REFERENCE_DESCRIPTOR.getTagName());
            refDescr = addElement(WeblogicXmlTag.REFERENCE_DESCRIPTOR, refDescr, getRootElement());
        }
        Element ejbRefElement =
            getDocument().createElement(WeblogicXmlTag.EJB_REFERENCE_DESCRIPTION.getTagName());
        ejbRefElement.appendChild(createNestedText(WeblogicXmlTag.EJB_REF_NAME, name));
        ejbRefElement.appendChild(createNestedText(WeblogicXmlTag.JNDI_NAME, jndiName));
        addElement(WeblogicXmlTag.EJB_REFERENCE_DESCRIPTION, ejbRefElement, refDescr);
    }
}
