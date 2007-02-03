/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.module.webapp;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.Dtd;
import org.codehaus.cargo.module.J2eeDescriptor;
import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.internal.util.xml.AbstractNodeList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
     * Resource Refs.
     */
    private AbstractNodeList contextParams;

    /**
     * Listeners.
     */
    private AbstractNodeList listeners;

    /**
     * Constructor.
     *
     * @param document The DOM document representing the parsed deployment descriptor
     */
    public WebXml(Document document)
    {
        super(document, new Dtd("http://java.sun.com/dtd/web-app_2_3.dtd"));
        this.contextParams = new TagNodeList(getRootElement(), WebXmlTag.CONTEXT_PARAM,
            ContextParam.class);
        this.listeners = new TagNodeList(getRootElement(), WebXmlTag.LISTENER,
            Listener.class);

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
     * Get the context parameters.
     *
     * @return the context parameters
     */
    public AbstractNodeList getContextParams()
    {
        return this.contextParams;
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
     * Returns the servlet API version.
     *
     * @return The version
     */
    public final WebXmlVersion getVersion()
    {
        DocumentType docType = getDocument().getDoctype();
        if (docType != null)
        {
            return WebXmlVersion.valueOf(docType);
        }
        return null;
    }

    /**
     * Adds a new servlet filter to the descriptor.
     *
     * @param filterName The name of the filter to add
     * @param filterClass The name of the class implementing the filter
     */
    public final void addFilter(String filterName, String filterClass)
    {
        if (filterName == null)
        {
            throw new NullPointerException();
        }
        if (hasFilter(filterName))
        {
            throw new IllegalStateException("Filter '" + filterName + "' already defined");
        }
        Element filterElement = getDocument().createElement(WebXmlTag.FILTER.getTagName());
        filterElement.appendChild(createNestedText(WebXmlTag.FILTER_NAME, filterName));
        filterElement.appendChild(createNestedText(WebXmlTag.FILTER_CLASS, filterClass));
        addElement(WebXmlTag.FILTER, filterElement, getRootElement());
    }

    /**
     * Adds a new context-param element to the descriptor.
     *
     * @param contextParam The element representing the context-param definition
     */
    public final void addContextParam(Element contextParam)
    {
        checkElement(contextParam, WebXmlTag.CONTEXT_PARAM);

        String paramName = getNestedText(contextParam, WebXmlTag.PARAM_NAME);
        if (paramName == null)
        {
            throw new IllegalArgumentException("Not a valid context-param name element");
        }

        String paramValue = getNestedText(contextParam, WebXmlTag.PARAM_VALUE);
        if (paramValue == null)
        {
            throw new IllegalArgumentException("Not a valid context-param value element");
        }

        if (hasContextParam(paramName))
        {
            throw new IllegalStateException("Context param '" + paramName + "' already defined");
        }
        addElement(WebXmlTag.CONTEXT_PARAM, contextParam, getRootElement());
    }

    /**
     * Adds a new servlet filter to the descriptor.
     *
     * @param filter The element representing the filter definition
     */
    public final void addFilter(Element filter)
    {
        checkElement(filter, WebXmlTag.FILTER);
        String filterName = getNestedText(filter, WebXmlTag.FILTER_NAME);
        if (filterName == null)
        {
            throw new IllegalArgumentException("Not a valid filter element");
        }
        if (hasFilter(filterName))
        {
            throw new IllegalStateException("Filter '" + filterName + "' already defined");
        }
        addElement(WebXmlTag.FILTER, filter, getRootElement());
    }

    /**
     * Adds an initialization parameter to the specified filter.
     *
     * @param filterName The name of the filter
     * @param paramName The name of the parameter
     * @param paramValue The parameter value
     */
    public final void addFilterInitParam(String filterName, String paramName, String paramValue)
    {
        Element filterElement = getFilter(filterName);
        if (filterElement == null)
        {
            throw new IllegalStateException("Filter '" + filterName + "' not defined");
        }
        addInitParam(filterElement, paramName, paramValue);
    }

    /**
     * Adds a filter mapping to the descriptor.
     *
     * @param filterName The name of the filter
     * @param urlPattern The URL pattern the filter should be mapped to
     */
    public final void addFilterMapping(String filterName, String urlPattern)
    {
        if (!hasFilter(filterName))
        {
            throw new IllegalStateException("Filter '" + filterName + "' not defined");
        }
        Element filterMappingElement =
            getDocument().createElement(WebXmlTag.FILTER_MAPPING.getTagName());
        filterMappingElement.appendChild(createNestedText(WebXmlTag.FILTER_NAME, filterName));
        filterMappingElement.appendChild(createNestedText(WebXmlTag.URL_PATTERN, urlPattern));
        addElement(WebXmlTag.FILTER_MAPPING, filterMappingElement, getRootElement());
    }

    /**
     * Returns the element that contains the definition of a specific servlet filter, or
     * <code>null</code> if a filter of the specified name is not defined in the descriptor.
     *
     * @param filterName The name of the servlet filter
     *
     * @return The DOM element representing the filter definition
     */
    public final Element getFilter(String filterName)
    {
        if (filterName == null)
        {
            throw new NullPointerException();
        }
        Iterator filterElements = getElements(WebXmlTag.FILTER);
        while (filterElements.hasNext())
        {
            Element filterElement = (Element) filterElements.next();
            if (filterName.equals(getNestedText(filterElement, WebXmlTag.FILTER_NAME)))
            {
                return filterElement;
            }
        }
        return null;
    }

    /**
     * Returns the element that contains the definition of a specific context param, or
     * <code>null</code> if a context param of the specified name is not defined in the descriptor.
     *
     * @param paramName The context param name
     *
     * @return The DOM element representing the context param definition
     */
    public final Element getContextParam(String paramName)
    {
        if (paramName == null)
        {
            throw new NullPointerException();
        }
        return contextParams.getByElementId(paramName);
    }

    /**
     * @param contextParam the context param element from which to extractthe name
     *
     * @return the name of the passed context param element
     */
    public final String getContextParamName(Element contextParam)
    {
        return getNestedText(contextParam, WebXmlTag.PARAM_NAME);
    }

    /**
     * Returns a list of names of filters that are mapped to the specified class.
     *
     * @param className The fully qualified name of the filter class
     *
     * @return An iterator over the names of the filters mapped to the class
     */
    public final Iterator getFilterNamesForClass(String className)
    {
        if (className == null)
        {
            throw new NullPointerException();
        }
        Iterator filterElements = getElements(WebXmlTag.FILTER);
        List filterNames = new ArrayList();
        while (filterElements.hasNext())
        {
            Element filterElement = (Element) filterElements.next();
            if (className.equals(getNestedText(filterElement, WebXmlTag.FILTER_CLASS)))
            {
                filterNames.add(getNestedText(filterElement, WebXmlTag.FILTER_NAME));
            }
        }
        return filterNames.iterator();
    }

    /**
     * Returns the value of an initialization parameter of the specified filter.
     *
     * @param filterName The name of the servlet filter
     * @param paramName The name of the initialization parameter
     *
     * @return The parameter value
     */
    public final String getFilterInitParam(String filterName, String paramName)
    {
        return getInitParam(getFilter(filterName), paramName);
    }

    /**
     * Returns the names of the initialization parameters of the specified servlet filter.
     *
     * @param theFilterName The name of the servlet filter of which the parameter names should be
     * retrieved
     *
     * @return An iterator over the ordered list of parameter names
     */
    public final Iterator getFilterInitParamNames(String theFilterName)
    {
        return getInitParamNames(getFilter(theFilterName));
    }

    /**
     * Returns the URL-patterns that the specified filter is mapped to in an ordered list. If there
     * are no mappings for the specified filter, an iterator over an empty list is returned.
     *
     * @param theFilterName The name of the servlet filter of which the mappings should be
     * retrieved
     *
     * @return An iterator over the ordered list of URL-patterns
     */
    public final Iterator getFilterMappings(String theFilterName)
    {
        if (theFilterName == null)
        {
            throw new NullPointerException();
        }
        List filterMappings = new ArrayList();
        Iterator filterMappingElements = getElements(WebXmlTag.FILTER_MAPPING);
        while (filterMappingElements.hasNext())
        {
            Element filterMappingElement = (Element)
                filterMappingElements.next();
            if (theFilterName.equals(getNestedText(
                filterMappingElement, WebXmlTag.FILTER_NAME)))
            {
                String urlPattern = getNestedText(
                    filterMappingElement, WebXmlTag.URL_PATTERN);
                if (urlPattern != null)
                {
                    filterMappings.add(urlPattern);
                }
            }
        }
        return filterMappings.iterator();
    }

    /**
     * Returns the names of all filters defined in the deployment descriptor. The names are returned
     * as an iterator over an ordered list.
     *
     * @return The filter names
     */
    public final Iterator getFilterNames()
    {
        List filterNames = new ArrayList();
        Iterator filterElements = getElements(WebXmlTag.FILTER);
        while (filterElements.hasNext())
        {
            Element filterElement = (Element) filterElements.next();
            String filterName =
                getNestedText(filterElement, WebXmlTag.FILTER_NAME);
            if (filterName != null)
            {
                filterNames.add(filterName);
            }
        }
        return filterNames.iterator();
    }

    /**
     * Returns whether a context param by the specified name is defined in the deployment
     * descriptor.
     *
     * @param theParamName The name of the context param
     *
     * @return <code>true</code> if the context param is defined, <code>false</code> otherwise
     */
    public final boolean hasContextParam(String theParamName)
    {
        return (getContextParam(theParamName) != null);
    }

    /**
     * Returns whether a servlet filter by the specified name is defined in the deployment
     * descriptor.
     *
     * @param theFilterName The name of the filter
     *
     * @return <code>true</code> if the filter is defined, <code>false</code> otherwise
     */
    public final boolean hasFilter(String theFilterName)
    {
        return (getFilter(theFilterName) != null);
    }

    /**
     * Adds a mapped JSP file to the descriptor.
     *
     * @param theServletName The name of the servlet to add
     * @param theJspFile The path to the JSP file relative to the root of the web application
     */
    public final void addJspFile(String theServletName, String theJspFile)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        if (hasFilter(theServletName))
        {
            throw new IllegalStateException("Servlet '" + theServletName
                + "' already defined");
        }
        Element servletElement =
            getDocument().createElement(WebXmlTag.SERVLET.getTagName());
        servletElement.appendChild(
            createNestedText(WebXmlTag.SERVLET_NAME, theServletName));
        servletElement.appendChild(
            createNestedText(WebXmlTag.JSP_FILE, theJspFile));
        addElement(WebXmlTag.SERVLET, servletElement, getRootElement());
    }

    /**
     * Adds a new servlet to the descriptor.
     *
     * @param theServletName The name of the servlet to add
     * @param theServletClass The name of the class implementing the servlet
     */
    public final void addServlet(String theServletName, String theServletClass)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        if (hasServlet(theServletName))
        {
            throw new IllegalStateException("Servlet '" + theServletName
                + "' already defined");
        }
        Element servletElement =
            getDocument().createElement(WebXmlTag.SERVLET.getTagName());
        servletElement.appendChild(
            createNestedText(WebXmlTag.SERVLET_NAME, theServletName));
        servletElement.appendChild(
            createNestedText(WebXmlTag.SERVLET_CLASS, theServletClass));
        addElement(WebXmlTag.SERVLET, servletElement, getRootElement());
    }

    /**
     * Adds a new servlet to the descriptor.
     *
     * @param theServlet The element representing the servlet definition
     */
    public final void addServlet(Element theServlet)
    {
        checkElement(theServlet, WebXmlTag.SERVLET);
        String servletName = getNestedText(theServlet, WebXmlTag.SERVLET_NAME);
        if (servletName == null)
        {
            throw new IllegalArgumentException("Not a valid servlet element");
        }
        if (hasServlet(servletName))
        {
            throw new IllegalStateException("Servlet '" + servletName
                + "' already defined");
        }
        addElement(WebXmlTag.SERVLET, theServlet, getRootElement());
    }

    /**
     * Adds an initialization parameter to the specified servlet.
     *
     * @param theServletName The name of the filter
     * @param theParamName The name of the parameter
     * @param theParamValue The parameter value
     */
    public final void addServletInitParam(String theServletName,
        String theParamName, String theParamValue)
    {
        Element servletElement = getServlet(theServletName);
        if (servletElement == null)
        {
            throw new IllegalStateException("Servlet '" + theServletName
                + "' not defined");
        }
        addInitParam(servletElement, theParamName, theParamValue);
    }

    /**
     * Adds a run-as declaration to the specified servlet.
     *
     * @param theServletName the name of the servlet to manipulate
     * @param theRoleName the role name that the servlet should be running as
     */
    public final void addServletRunAsRoleName(String theServletName,
        String theRoleName)
    {
        Element servlet = getServlet(theServletName);
        Element runAsElement =
            getDocument().createElement(WebXmlTag.RUN_AS.getTagName());
        runAsElement.appendChild(createNestedText(WebXmlTag.ROLE_NAME,
            theRoleName));
        servlet.appendChild(runAsElement);
    }

    /**
     * Adds a servlet mapping to the descriptor.
     *
     * @param theServletName The name of the servlet
     * @param theUrlPattern The URL pattern the servlet should be mapped to
     */
    public final void addServletMapping(String theServletName,
        String theUrlPattern)
    {
        if (!hasServlet(theServletName))
        {
            throw new IllegalStateException("Servlet '" + theServletName
                + "' not defined");
        }
        Element servletMappingElement =
            getDocument().createElement(WebXmlTag.SERVLET_MAPPING.getTagName());
        servletMappingElement.appendChild(
            createNestedText(WebXmlTag.SERVLET_NAME, theServletName));
        servletMappingElement.appendChild(
            createNestedText(WebXmlTag.URL_PATTERN, theUrlPattern));
        addElement(WebXmlTag.SERVLET_MAPPING, servletMappingElement, getRootElement());
    }

    /**
     * Returns the element that contains the definition of a specific servlet, or <code>null</code>
     * if a servlet of the specified name is not defined in the descriptor.
     *
     * @param theServletName The name of the servlet
     *
     * @return The DOM element representing the servlet definition
     */
    public final Element getServlet(String theServletName)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        Iterator servletElements = getElements(WebXmlTag.SERVLET);
        while (servletElements.hasNext())
        {
            Element servletElement = (Element) servletElements.next();
            if (theServletName.equals(getNestedText(
                servletElement, WebXmlTag.SERVLET_NAME)))
            {
                return servletElement;
            }
        }
        return null;
    }

    /**
     * Returns the value of an initialization parameter of the specified servlet.
     *
     * @param theServletName The name of the servlet
     * @param theParamName The name of the initialization parameter
     *
     * @return The parameter value
     */
    public final String getServletInitParam(String theServletName,
        String theParamName)
    {
        return getInitParam(getServlet(theServletName), theParamName);
    }

    /**
     * Returns the names of the initialization parameters of the specified servlet.
     *
     * @param theServletName The name of the servlet of which the parameter names should be
     * retrieved
     *
     * @return An iterator over the ordered list of parameter names
     */
    public final Iterator getServletInitParamNames(String theServletName)
    {
        return getInitParamNames(getServlet(theServletName));
    }

    /**
     * Returns the role name that the servlet is running as.
     *
     * @param theServletName The name of the servlet of which the role name should be retrieved
     *
     * @return the roleName or null if non is specified
     */
    public final String getServletRunAsRoleName(String theServletName)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        String roleName = null;
        Element servlet = getServlet(theServletName);
        NodeList nodeList =
            servlet.getElementsByTagName(WebXmlTag.RUN_AS.getTagName());
        if (nodeList != null)
        {
            Element e = (Element) nodeList.item(0);
            if (e != null)
            {
                roleName = getNestedText(e, WebXmlTag.ROLE_NAME);
            }
        }

        return roleName;
    }

    /**
     * Returns the URL-patterns that the specified servlet is mapped to in an ordered list. If there
     * are no mappings for the specified servlet, an iterator over an empty list is returned.
     *
     * @param theServletName The name of the servlet of which the mappings should be retrieved
     *
     * @return An iterator over the ordered list of URL-patterns
     */
    public final Iterator getServletMappings(String theServletName)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        List servletMappings = new ArrayList();
        Iterator servletMappingElements =
            getElements(WebXmlTag.SERVLET_MAPPING);
        while (servletMappingElements.hasNext())
        {
            Element servletMappingElement = (Element)
                servletMappingElements.next();
            if (theServletName.equals(getNestedText(
                servletMappingElement, WebXmlTag.SERVLET_NAME)))
            {
                String urlPattern = getNestedText(
                    servletMappingElement, WebXmlTag.URL_PATTERN);
                if (urlPattern != null)
                {
                    servletMappings.add(urlPattern);
                }
            }
        }
        return servletMappings.iterator();
    }

    /**
     * Returns the names of all servlets defined in the deployment descriptor. The names are
     * returned as an iterator over an ordered list.
     *
     * @return The servlet names
     */
    public final Iterator getServletNames()
    {
        List servletNames = new ArrayList();
        Iterator servletElements = getElements(WebXmlTag.SERVLET);
        while (servletElements.hasNext())
        {
            Element servletElement = (Element) servletElements.next();
            String servletName =
                getNestedText(servletElement, WebXmlTag.SERVLET_NAME);
            if (servletName != null)
            {
                servletNames.add(servletName);
            }
        }
        return servletNames.iterator();
    }

    /**
     * Returns a list of names of servlets that are mapped to the specified class.
     *
     * @param theClassName The fully qualified name of the servlet class
     *
     * @return An iterator over the names of the servlets mapped to the class
     */
    public final Iterator getServletNamesForClass(String theClassName)
    {
        if (theClassName == null)
        {
            throw new NullPointerException();
        }
        Iterator servletElements = getElements(WebXmlTag.SERVLET);
        List servletNames = new ArrayList();
        while (servletElements.hasNext())
        {
            Element servletElement = (Element) servletElements.next();
            if (theClassName.equals(getNestedText(
                servletElement, WebXmlTag.SERVLET_CLASS)))
            {
                servletNames.add(getNestedText(
                    servletElement, WebXmlTag.SERVLET_NAME));
            }
        }
        return servletNames.iterator();
    }

    /**
     * Returns a list of names of servlets that are mapped to the specified JSP file.
     *
     * @param theJspFile The path to the JSP file, relative to the root of the web-application
     *
     * @return An iterator over the names of the servlets mapped to the JSP file
     */
    public final Iterator getServletNamesForJspFile(String theJspFile)
    {
        if (theJspFile == null)
        {
            throw new NullPointerException();
        }
        Iterator servletElements = getElements(WebXmlTag.SERVLET);
        List servletNames = new ArrayList();
        while (servletElements.hasNext())
        {
            Element servletElement = (Element) servletElements.next();
            if (theJspFile.equals(getNestedText(
                servletElement, WebXmlTag.JSP_FILE)))
            {
                servletNames.add(getNestedText(
                    servletElement, WebXmlTag.SERVLET_NAME));
            }
        }
        return servletNames.iterator();
    }

    /**
     * Returns whether a servlet by the specified name is defined in the deployment descriptor.
     *
     * @param theServletName The name of the servlet
     *
     * @return <code>true</code> if the servlet is defined, <code>false</code> otherwise
     */
    public final boolean hasServlet(String theServletName)
    {
        return (getServlet(theServletName) != null);
    }

    /**
     * Creates and adds a security-constraint to the descriptor.
     *
     * @param theWebResourceName The name of the web resource collection to protect
     * @param theUrlPattern The URL pattern to apply the constraint to
     * @param theRoles The list of authorized roles
     */
    public final void addSecurityConstraint(String theWebResourceName, String theUrlPattern,
        List theRoles)
    {
        if ((theWebResourceName == null) || (theUrlPattern == null) || (theRoles == null))
        {
            throw new NullPointerException();
        }
        if (hasSecurityConstraint(theUrlPattern))
        {
            throw new IllegalStateException("Security constraint for URL "
                + "pattern " + theUrlPattern + " already defined");
        }
        Element securityConstraintElement = getDocument().createElement(
            WebXmlTag.SECURITY_CONSTRAINT.getTagName());
        Element webResourceCollectionElement = getDocument().createElement(
            WebXmlTag.WEB_RESOURCE_COLLECTION.getTagName());
        webResourceCollectionElement.appendChild(
            createNestedText(WebXmlTag.WEB_RESOURCE_NAME, theWebResourceName));
        webResourceCollectionElement.appendChild(
            createNestedText(WebXmlTag.URL_PATTERN, theUrlPattern));
        securityConstraintElement.appendChild(webResourceCollectionElement);
        Element authConstraintElement =
            getDocument().createElement(WebXmlTag.AUTH_CONSTRAINT.getTagName());
        for (Iterator i = theRoles.iterator(); i.hasNext();)
        {
            authConstraintElement.appendChild(
                createNestedText(WebXmlTag.ROLE_NAME, (String) i.next()));
        }
        securityConstraintElement.appendChild(authConstraintElement);
        addElement(WebXmlTag.SECURITY_CONSTRAINT, securityConstraintElement,
            getRootElement());
    }

    /**
     * Returns the element that contains the security constraint defined for the specified URL
     * pattern.
     *
     * @param theUrlPattern The URL pattern
     *
     * @return The DOM element representing the security constraint
     */
    public final Element getSecurityConstraint(String theUrlPattern)
    {
        if (theUrlPattern == null)
        {
            throw new NullPointerException();
        }
        Iterator securityConstraintElements =
            getElements(WebXmlTag.SECURITY_CONSTRAINT);
        while (securityConstraintElements.hasNext())
        {
            Element securityConstraintElement = (Element)
                securityConstraintElements.next();
            Iterator webResourceCollectionElements =
                getNestedElements(securityConstraintElement,
                    WebXmlTag.WEB_RESOURCE_COLLECTION);
            if (webResourceCollectionElements.hasNext())
            {
                Element webResourceCollectionElement = (Element)
                    webResourceCollectionElements.next();
                if (theUrlPattern.equals(getNestedText(
                    webResourceCollectionElement, WebXmlTag.URL_PATTERN)))
                {
                    return securityConstraintElement;
                }
            }
        }
        return null;
    }

    /**
     * Returns whether a security constraint has been mapped to the specified URL pattern.
     *
     * @param theUrlPattern The URL patterm
     *
     * @return <code>true</code> if a security constraint is defined, <code>false</code> otherwise
     */
    public final boolean hasSecurityConstraint(String theUrlPattern)
    {
        return (getSecurityConstraint(theUrlPattern) != null);
    }

    /**
     * Returns whether the descriptor has a login configuration.
     *
     * @return <code>true</code> if a login config is defined, <code>false</code> otherwise
     */
    public final boolean hasLoginConfig()
    {
        return (getLoginConfig() != null);
    }

    /**
     * Returns whether the descriptor has a login configuration.
     *
     * @return <code>true</code> if a login config is defined, <code>false</code> otherwise
     */
    public final Element getLoginConfig()
    {
        Iterator loginConfigElements = getElements(WebXmlTag.LOGIN_CONFIG);
        if (loginConfigElements.hasNext())
        {
            return (Element) loginConfigElements.next();
        }
        return null;
    }

    /**
     * Returns the authorization method defined by the login configuration.
     *
     * @return The authorization method
     */
    public final String getLoginConfigAuthMethod()
    {
        return getNestedText(getLoginConfig(), WebXmlTag.AUTH_METHOD);
    }

    /**
     * Sets the login configuration.
     *
     * @param theAuthMethod The authentication method (for example, BASIC)
     * @param theRealmName The name of the realm
     */
    public final void setLoginConfig(String theAuthMethod, String theRealmName)
    {
        if ((theRealmName == null) || (theAuthMethod == null))
        {
            throw new NullPointerException();
        }
        Element loginConfigElement =
            getDocument().createElement(WebXmlTag.LOGIN_CONFIG.getTagName());
        loginConfigElement.appendChild(
            createNestedText(WebXmlTag.AUTH_METHOD, theAuthMethod));
        loginConfigElement.appendChild(
            createNestedText(WebXmlTag.REALM_NAME, theRealmName));
        replaceElement(WebXmlTag.LOGIN_CONFIG, loginConfigElement,
            getRootElement());
    }

    /**
     * Adds a new security role to the descriptor.
     *
     * @param theRoleName The name of the role to add
     */
    public final void addSecurityRole(String theRoleName)
    {
        if (theRoleName == null)
        {
            throw new NullPointerException();
        }
        if (hasSecurityRole(theRoleName))
        {
            throw new IllegalStateException("Security role '" + theRoleName
                + "' already defined");
        }
        Element securityRoleElement =
            getDocument().createElement(WebXmlTag.SECURITY_ROLE.getTagName());
        securityRoleElement.appendChild(
            createNestedText(WebXmlTag.ROLE_NAME, theRoleName));
        addElement(WebXmlTag.SECURITY_ROLE, securityRoleElement,
            getRootElement());
    }

    /**
     * Returns the element that contains the specified security role, or <code>null</code> if the
     * role is not defined in the descriptor.
     *
     * @param theRoleName The name of the role
     *
     * @return The DOM element representing the security role
     */
    public final Element getSecurityRole(String theRoleName)
    {
        if (theRoleName == null)
        {
            throw new NullPointerException();
        }
        Iterator securityRoleElements = getElements(WebXmlTag.SECURITY_ROLE);
        while (securityRoleElements.hasNext())
        {
            Element securityRoleElement = (Element) securityRoleElements.next();
            if (theRoleName.equals(getNestedText(
                securityRoleElement, WebXmlTag.ROLE_NAME)))
            {
                return securityRoleElement;
            }
        }
        return null;
    }

    /**
     * Returns a list of the security role names defined in the deployment descriptor.
     *
     * @return An iterator over the list of security role names, or an empty iterator if no security
     *         roles are defined in the descriptor
     */
    public final Iterator getSecurityRoleNames()
    {
        List securityRoleNames = new ArrayList();
        Iterator securityRoleElements = getElements(WebXmlTag.SECURITY_ROLE);
        while (securityRoleElements.hasNext())
        {
            Element securityRoleElement = (Element) securityRoleElements.next();
            String securityRoleName =
                getNestedText(securityRoleElement, WebXmlTag.ROLE_NAME);
            if (securityRoleName != null)
            {
                securityRoleNames.add(securityRoleName);
            }
        }
        return securityRoleNames.iterator();
    }

    /**
     * Returns whether a specific security role has been defined.
     *
     * @param theRoleName The name of the role
     *
     * @return <code>true</code> if the security role is defined, <code>false</code> otherwise
     */
    public final boolean hasSecurityRole(String theRoleName)
    {
        return (getSecurityRole(theRoleName) != null);
    }

    /**
     * Adds an element of the specified tag to root of the descriptor.
     *
     * @param tag The descriptor tag
     * @param element The element to add
     */
    public final void addRootElement(DescriptorTag tag,
        Element element)
    {
        super.addElement(tag, element, getRootElement());
    }

    /**
     * Replaces all elements of the specified tag with the provided element. This method only works
     * on tags directly under the root tag.
     *
     * @param tag The descriptor tag
     * @param element The element to replace the current elements with
     */
    public final void replaceRootElement(DescriptorTag tag,
        Element element)
    {
        super.replaceElement(tag, element, getRootElement());
    }

    /**
     * Adds an ejb-ref.
     *
     * @param ref the ejb-ref
     */
    public void addEjbRef(EjbRef ref)
    {
        Element ejbRefElement =
            getDocument().createElement(WebXmlTag.EJB_LOCAL_REF.getTagName());
        ejbRefElement.setAttribute("id", ref.getName().replace('/', '_'));
        ejbRefElement.appendChild(createNestedText(WebXmlTag.EJB_REF_NAME, ref.getName()));
        ejbRefElement.appendChild(createNestedText(WebXmlTag.EJB_REF_TYPE, ref.getType()));
        if (ref.isLocal())
        {
            ejbRefElement.appendChild(createNestedText(WebXmlTag.LOCAL_HOME,
                                                       ref.getEjbHomeInterface()));
            ejbRefElement.appendChild(createNestedText(WebXmlTag.LOCAL,
                                                       ref.getEjbInterface()));
        }
        else
        {
            ejbRefElement.appendChild(createNestedText(WebXmlTag.HOME, ref.getEjbHomeInterface()));
            ejbRefElement.appendChild(createNestedText(WebXmlTag.REMOTE, ref.getEjbInterface()));

        }
        if (ref.getEjbName() != null)
        {
            ejbRefElement.appendChild(createNestedText(WebXmlTag.EJB_LINK, ref.getEjbName()));
        }
        else if (ref.getJndiName() != null)
        {
            Iterator i = this.vendorDescriptors.iterator();
            while (i.hasNext())
            {
                VendorWebAppDescriptor descr = (VendorWebAppDescriptor) i.next();
                descr.addEjbReference(ref.getName(), ref.getJndiName());
            }
        }
        else
        {
            throw new IllegalStateException("Either ejbName or jndiName must be set for ejb "
                                            + ref.getName() + ".");
        }

        addRootElement(WebXmlTag.EJB_LOCAL_REF, ejbRefElement);
    }

    /**
     * Adds an initialization parameter to the specified filter or servlet.
     *
     * @param theElement The filter or servlet element to which the initialization parameter should
     * be added
     * @param theParamName The name of the parameter
     * @param theParamValue The parameter value
     */
    private void addInitParam(Element theElement, String theParamName, String theParamValue)
    {
        Element initParamElement =
            getDocument().createElement(WebXmlTag.INIT_PARAM.getTagName());
        initParamElement.appendChild(
            createNestedText(WebXmlTag.PARAM_NAME, theParamName));
        initParamElement.appendChild(
            createNestedText(WebXmlTag.PARAM_VALUE, theParamValue));
        Iterator loadOnStartupElements = getNestedElements(theElement,
            WebXmlTag.LOAD_ON_STARTUP);
        if (loadOnStartupElements.hasNext())
        {
            theElement.insertBefore(initParamElement, (Element) loadOnStartupElements.next());
        }
        else
        {
            theElement.appendChild(initParamElement);
        }
    }

    /**
     * Returns the value of an initialization parameter of the specified filter or servlet.
     *
     * @param theElement The filter or servlet element that contains the initialization parameters
     * @param theParamName The name of the initialization parameter
     *
     * @return The parameter value
     */
    private String getInitParam(Element theElement, String theParamName)
    {
        if (theElement != null)
        {
            NodeList initParamElements =
                theElement.getElementsByTagName(WebXmlTag.INIT_PARAM.getTagName());
            for (int i = 0; i < initParamElements.getLength(); i++)
            {
                Element initParamElement = (Element) initParamElements.item(i);
                String paramName = getNestedText(initParamElement, WebXmlTag.PARAM_NAME);
                if (theParamName.equals(paramName))
                {
                    return getNestedText(initParamElement, WebXmlTag.PARAM_VALUE);
                }
            }
        }
        return null;
    }

    /**
     * Returns the names of the initialization parameters of the specified filter or servlet.
     *
     * @param theElement The filter or servlet element that contains the initialization parameters
     *
     * @return An iterator over the ordered list of parameter names
     */
    private Iterator getInitParamNames(Element theElement)
    {
        List initParamNames = new ArrayList();
        if (theElement != null)
        {
            NodeList initParamElements =
                theElement.getElementsByTagName(
                    WebXmlTag.INIT_PARAM.getTagName());
            for (int i = 0; i < initParamElements.getLength(); i++)
            {
                Element initParamElement = (Element) initParamElements.item(i);
                String paramName = getNestedText(
                    initParamElement, WebXmlTag.PARAM_NAME);
                if (paramName != null)
                {
                    initParamNames.add(paramName);
                }
            }
        }
        return initParamNames.iterator();
    }

    /**
     * Returns a list of nodes that are of web listeners.
     * @return a list of listener nodes
     */
    public AbstractNodeList getListeners()
    {
        return this.listeners;
    }
}
