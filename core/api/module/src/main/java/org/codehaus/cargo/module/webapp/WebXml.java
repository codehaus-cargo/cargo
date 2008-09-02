/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.module.webapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.DescriptorElement;
import org.codehaus.cargo.module.DescriptorType;
import org.codehaus.cargo.module.Identifier;
import org.codehaus.cargo.module.J2eeDescriptor;
import org.jdom.DocType;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Encapsulates the DOM representation of a web deployment descriptor <code>web.xml</code> to
 * provide convenience methods for easy access and manipulation.
 *
 * @version $Id$
 */
public class WebXml extends AbstractDescriptor implements J2eeDescriptor
{
    /**
     * List of vendor specific descriptors associated with this web.xml.
     */
    private List vendorDescriptors = new ArrayList();

    /**
     * Constructor.
     */
    public WebXml()
    {
        super(new Element("web-app"), WebXml23Type.getInstance());
    }
    
    /**
     * Constructor.
     *
     * @param rootElement The root webxml element
     * @param type The descriptor type to use
     */
    public WebXml(Element rootElement, DescriptorType type)
    {
        super(rootElement, type);                
    }

    /**
     * {@inheritDoc}
     * @see J2eeDescriptor#getFileName()
     */
    public String getFileName()
    {
        return "web.xml";
    }

    /**
     * Associates a vendor specific descriptor with this web.xml.
     *
     * @param descr the vendor specific dexcriptor to associate
     */
    public void addVendorDescriptor(VendorWebAppDescriptor descr)
    {
        this.vendorDescriptors.add(descr);
    }

    /**
     * Returns all vendor descriptors associated with this web.xml.
     *
     * @return Iterator containing VendorDescriptors
     */
    public Iterator getVendorDescriptors()
    {
        return this.vendorDescriptors.iterator();
    }

    /**
     * Get the namespace that tags in this descriptor live in.
     * @return the namespace, or null if none
     */
    protected Namespace getTagNamespace()
    {
        if (this.getVersion() == null)
        {
            return null;
        }
        return this.getVersion().getNamespace();
    }
    
    /**
     * Returns the servlet API version.
     *
     * @return The version
     */
    public final WebXmlVersion getVersion()
    {
        DocType docType = getDocType();
        if (docType != null)
        {
            return WebXmlVersion.valueOf(docType);
        }
        
        return WebXmlVersion.valueOf(this.getRootElement());
    }

    /**
     * Add a tag into the document.
     * @param element The element to add
     */
    public final void addTag(DescriptorElement element)
    {
        Identifier id = element.getTag().getIdentifier();
        if (id != null)
        {
            if (getTagByIdentifier(element.getTag(), id.getIdentifier(element)) != null)
            {
                throw new IllegalStateException();
            }
        }
        
        addElement(element.getTag(), element, getRootElement());
    }
   
    /**
     * Get a tag by name.
     * 
     * @param tag The tag name
     * @return the element for the tag
     */
    public Element getTag(String tag)
    {
        if (tag == null)
        {
            throw new NullPointerException();
        }
            
        List items = getRootElement().getChildren(tag, getTagNamespace());
        if (items.size() == 0)
        {
            return null;
        }
        
        return ((Element) items.get(0));
    }


}

