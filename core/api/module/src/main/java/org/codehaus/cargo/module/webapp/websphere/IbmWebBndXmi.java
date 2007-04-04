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
package org.codehaus.cargo.module.webapp.websphere;

import java.util.Random;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.codehaus.cargo.module.webapp.VendorWebAppDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates the DOM representation of a websphere web deployment descriptor
 * <code>ibm-web-bnd.xmi</code> to provide convenience methods for easy access and manipulation.
 *
 * @version $Id$
 */
public class IbmWebBndXmi extends AbstractDescriptor implements VendorWebAppDescriptor
{
    /**
     * File name of this descriptor.
     */
    private static final String FILE_NAME = "ibm-web-bnd.xmi";

    /**
     * Random instance for generating xml ids.
     */
    private Random random = new Random();

    /**
     * Constructor.
     *
     * @param document The DOM document representing the parsed deployment descriptor
     */
    public IbmWebBndXmi(Document document)
    {
        super(document, new IbmWebBndXmiGrammar());
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.webapp.VendorWebAppDescriptor#getFileName()
     */
    public final String getFileName()
    {
        return FILE_NAME;
    }

    /**
     * Adds a ejb reference description to the websphere web deployment descriptor.
     * @param ref the reference to add
     */
    public final void addEjbReference(EjbRef ref)
    {
        Element ejbRefBindingsElement = getDocument().createElement("ejbRefBindings");
        String id = "EjbRefBinding_" + this.random.nextLong();
        ejbRefBindingsElement.setAttribute("xmi:id", id);
        ejbRefBindingsElement.setAttribute("jndiName", ref.getJndiName());
        Element bindingEjbRef = getDocument().createElement("bindingEjbRef");
        bindingEjbRef.setAttribute("href", "WEB-INF/web.xml#" + ref.getName().replace('/', '_'));
        ejbRefBindingsElement.appendChild(bindingEjbRef);

        addElement(new DescriptorTag("ejbRefBindings", true), ejbRefBindingsElement,
            getRootElement());
    }
}
