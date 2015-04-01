/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.module.application;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.Descriptor;
import org.codehaus.cargo.module.DescriptorType;
import org.codehaus.cargo.module.J2eeDescriptor;
import org.jdom.DocType;
import org.jdom.Element;

/**
 * Encapsulates the DOM representation of an EAR descriptor (<code>application.xml</code>) to
 * provide convenience methods for easy access and manipulation.
 * 
 */
public class ApplicationXml extends AbstractDescriptor implements J2eeDescriptor
{
    /**
     * List of vendor descriptors associated with this application.xml.
     */
    private List<Descriptor> vendorDescriptors = new ArrayList<Descriptor>();

    /**
     * Constructor.
     * 
     * @param rootElement the root element for this descriptor
     * @param type the type of this descriptor
     */
    public ApplicationXml(Element rootElement, DescriptorType type)
    {
        super(rootElement, type);
    }

    /**
     * @return The J2EE API version.
     */
    public ApplicationXmlVersion getVersion()
    {
        ApplicationXmlVersion version = null;
        DocType docType = getDocument().getDocType();
        if (docType != null)
        {
            version = ApplicationXmlVersion.valueOf(docType);
        }
        return version;
    }

    /**
     * Returns the element that contains the definition of a specific web module, or
     * <code>null</code> if a web module with the specified web-uri is not defined.
     * 
     * @param webUri The uri of the web module
     * @return The DOM element representing the web module definition
     */
    public Element getWebModule(String webUri)
    {
        if (webUri == null)
        {
            throw new NullPointerException();
        }
        List<Element> moduleElements = getElements(ApplicationXmlTag.MODULE);
        for (Element moduleElement : moduleElements)
        {
            List<Element> webElements = getNestedElements(moduleElement,
                getDescriptorType().getTagByName(ApplicationXmlTag.WEB));
            if (!webElements.isEmpty())
            {
                Element webElement = webElements.get(0);
                if (webUri.equals(getNestedText(webElement,
                    getDescriptorType().getTagByName(ApplicationXmlTag.WEB_URI))))
                {
                    return webElement;
                }
            }
        }
        return null;
    }

    /**
     * Returns the context root of the the specified web module.
     * 
     * @param webUri The uri of the web module
     * @return The context root of the web module
     */
    public String getWebModuleContextRoot(String webUri)
    {
        Element webModuleElement = getWebModule(webUri);
        if (webModuleElement == null)
        {
            throw new IllegalArgumentException("Web module [" + webUri + "] is not defined");
        }
        return getNestedText(webModuleElement, getDescriptorType().getTagByName(
            ApplicationXmlTag.CONTEXT_ROOT));
    }

    /**
     * Returns a list of the URIs of the web modules defined in the descriptor.
     * 
     * @return A list of the URIs of the web modules
     */
    public List<String> getWebModuleUris()
    {
        List<String> webUris = new ArrayList<String>();
        List<Element> moduleElements = getElements(ApplicationXmlTag.MODULE);
        for (Element moduleElement : moduleElements)
        {
            List<Element> webElements = getNestedElements(moduleElement,
                getDescriptorType().getTagByName(ApplicationXmlTag.WEB));
            if (!webElements.isEmpty())
            {
                Element webElement = webElements.get(0);
                String webUri = getNestedText(
                    webElement, getDescriptorType().getTagByName(ApplicationXmlTag.WEB_URI));
                if (webUri != null)
                {
                    webUris.add(webUri);
                }
            }
        }
        return webUris;
    }

    /**
     * Returns a list of the ejb modules defined in the descriptor.
     * 
     * @return A list of Strings over the ejb modules
     */
    public List<String> getEjbModules()
    {
        List<String> modules = new ArrayList<String>();
        List<Element> moduleElements = getElements(ApplicationXmlTag.MODULE);
        for (Element moduleElement : moduleElements)
        {
            String ejb = getNestedText(
                moduleElement, getDescriptorType().getTagByName(ApplicationXmlTag.EJB));
            if (ejb != null)
            {
                modules.add(ejb);
            }
        }
        return modules;
    }

    /**
     * Returns a list of the elements that match the specified tag.
     * 
     * @param tag The descriptor tag of which the elements should be returned
     * @return A list of the elements matching the tag, in the order they occur in the descriptor
     */
    public List<Element> getElements(ApplicationXmlTag tag)
    {
        return super.getElements(tag);
    }

    /**
     * Adds a web module to the deployment descriptor.
     * 
     * @param uri the uri of the new module
     * @param context the context of the new module
     */
    public void addWebModule(String uri, String context)
    {
        Element moduleElement = new Element(ApplicationXmlTag.MODULE);
        Element webElement = new Element(ApplicationXmlTag.WEB);
        webElement.addContent(createNestedText(
            getDescriptorType().getTagByName(ApplicationXmlTag.WEB_URI), uri));
        webElement.addContent(createNestedText(
            getDescriptorType().getTagByName(ApplicationXmlTag.CONTEXT_ROOT), context));
        moduleElement.addContent(webElement);
        addElement(getDescriptorType().getTagByName(
            ApplicationXmlTag.MODULE), moduleElement, getRootElement());
    }

    /**
     * Adds a ejb module to the deployment descriptor.
     * 
     * @param name the name of the new module
     */
    public void addEjbModule(String name)
    {
        Element moduleElement = new Element(ApplicationXmlTag.MODULE);
        moduleElement.addContent(createNestedText(
            getDescriptorType().getTagByName(ApplicationXmlTag.EJB), name));
        addElement(getDescriptorType().getTagByName(
            ApplicationXmlTag.MODULE), moduleElement, getRootElement());
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
     * {@inheritDoc}
     * @see J2eeDescriptor#getFileName()
     */
    public String getFileName()
    {
        return "application.xml";
    }
}
