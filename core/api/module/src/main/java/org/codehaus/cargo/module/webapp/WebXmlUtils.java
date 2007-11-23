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
package org.codehaus.cargo.module.webapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.module.DescriptorElement;
import org.codehaus.cargo.module.webapp.elements.Filter;
import org.codehaus.cargo.module.webapp.elements.FilterMapping;
import org.codehaus.cargo.module.webapp.elements.InitParam;
import org.codehaus.cargo.module.webapp.elements.SecurityConstraint;
import org.codehaus.cargo.module.webapp.elements.Servlet;
import org.codehaus.cargo.module.webapp.elements.WebXmlElement;
import org.jdom.Element;

/**
 * @version $Id: $
 */
public final class WebXmlUtils
{
    /**
     * Private no-args constructor.
     */
    private WebXmlUtils()
    {
        // No constructor
    }
    /**
     * Returns a list of names of filters that are mapped to the specified class.
     *
     * @param webXml The webXml file to use
     * @param className The fully qualified name of the filter class
     * @return An iterator over the names of the filters mapped to the class
     */
    public static Iterator getFilterNamesForClass(WebXml webXml, String className)
    {
        if (className == null)
        {
            throw new NullPointerException();
        }
        Iterator filterElements = webXml.getTags(WebXmlType.FILTER).iterator();
        List filterNames = new ArrayList();
        while (filterElements.hasNext())
        {
            Filter filterElement = (Filter) filterElements.next();
            if (className.equals(filterElement.getFilterClass()))
            {
                filterNames.add(filterElement.getFilterName());
            }
        }
        return filterNames.iterator();
    }

    /**
     * Returns the URL-patterns that the specified filter is mapped to in an ordered list. If there
     * are no mappings for the specified filter, an iterator over an empty list is returned.
     *
     * @param webXml The webXml file to use
     * @param theFilterName The name of the servlet filter of which the mappings should be retrieved
     * @return An iterator over the ordered list of URL-patterns
     */
    public static Iterator getFilterMappings(WebXml webXml, String theFilterName)
    {

        if (theFilterName == null)
        {
            throw new NullPointerException();
        }
        List filterMappings = new ArrayList();
        Iterator filterMappingElements =
            webXml.getTags(WebXmlType.FILTER_MAPPING).iterator();
        while (filterMappingElements.hasNext())
        {
            FilterMapping filterMappingElement = (FilterMapping) filterMappingElements.next();
            if (theFilterName.equals(filterMappingElement.getFilterName()))
            {
                String urlPattern = filterMappingElement.getUrlPattern();
                if (urlPattern != null)
                {
                    filterMappings.add(urlPattern);
                }
            }
        }
        return filterMappings.iterator();
    }

    /**
     * Returns the filter mappings that the specified filter is mapped to in an ordered list. If there
     * are no mappings for the specified filter, an iterator over an empty list is returned.
     *
     * @param webXml The webXml file to use
     * @param theFilterName The name of the servlet filter of which the mappings should be retrieved
     * @return An iterator over the ordered list of filter elements
     */
    public static Iterator getFilterMappingElements(WebXml webXml, String theFilterName)
    {

        if (theFilterName == null)
        {
            throw new NullPointerException();
        }
        List filterMappings = new ArrayList();
        Iterator filterMappingElements =
            webXml.getTags(WebXmlType.FILTER_MAPPING).iterator();
        while (filterMappingElements.hasNext())
        {
            FilterMapping filterMappingElement = (FilterMapping) filterMappingElements.next();
            if (theFilterName.equals(filterMappingElement.getFilterName()))
            {
                filterMappings.add(filterMappingElement);
            }
        }
        return filterMappings.iterator();
    }

    /**
     * Returns whether a context param by the specified name is defined in the deployment
     * descriptor.
     *
     * @param webXml The webXml file to use
     * @param theParamName The name of the context param
     * @return <code>true</code> if the context param is defined, <code>false</code> otherwise
     */
    public static boolean hasContextParam(WebXml webXml, String theParamName)
    {
        return (getContextParam(webXml, theParamName) != null);
    }

    /**
     * Returns the element that contains the definition of a specific context param, or
     * <code>null</code> if a context param of the specified name is not defined in the
     * descriptor.
     *
     * @param webXml The webXml file to use
     * @param paramName The context param name
     * @return The DOM element representing the context param definition
     */
    public static Element getContextParam(WebXml webXml, String paramName)
    {
        if (paramName == null)
        {
            throw new NullPointerException();
        }
        return webXml.getTagByIdentifier(WebXmlType.CONTEXT_PARAM, paramName);
    }

    /**
     * Returns whether a servlet filter by the specified name is defined in the deployment
     * descriptor.
     *
     * @param webXml The webXml file to use
     * @param theFilterName The name of the filter
     * @return <code>true</code> if the filter is defined, <code>false</code> otherwise
     */
    public static boolean hasFilter(WebXml webXml, String theFilterName)
    {
        return webXml.getTagByIdentifier(WebXmlType.FILTER, theFilterName) != null;
    }

    /**
     * For a named servlet, return the run-as role name.
     *
     * @param webXml The webXml file to use
     * @param theServletName the name of the servlet
     * @return the run-as role name
     */
    public static String getServletRunAsRoleName(WebXml webXml, String theServletName)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        String roleName = null;
        Servlet servlet = getServlet(webXml, theServletName);
        List nodeList = servlet.getChildren(WebXmlType.RUN_AS, servlet.getNamespace());
        if (nodeList != null && nodeList.size() > 0)
        {
            Element e = (Element) nodeList.get(0);
            if (e != null)
            {
                roleName = e.getChildText(WebXmlType.ROLE_NAME, e.getNamespace());
            }
        }

        return roleName;
    }

    /**
     * Add an init-param to the web xml.
     * @param itemElement The the parent element to add to
     * @param name The name of the param
     * @param value The value for the param
     */
    public static void addTagInitParam(WebXmlElement itemElement, String name, String value)
    {
        WebXmlTag tag = (WebXmlTag)itemElement.getTag().getDescriptorType().getTagByName("init-param");
        InitParam init = new InitParam(tag, name, value);
        itemElement.getChildren().add(init);
    }

    /**
     * Get the init parameter names for a filter.
     *
     * @param webXml The webXml file to use
     * @param name The name of the filter to use
     * @return an iterator over the param names
     */
    public static Iterator getFilterInitParamNames(WebXml webXml, String name)
    {
        WebXmlElement element =
            (WebXmlElement) webXml.getTagByIdentifier(WebXmlType.FILTER, name);
        List items = element.getChildren("init-param", element.getTag().getTagNamespace());
        List result = new ArrayList(items.size());
        for (Iterator i = items.iterator(); i.hasNext();)
        {
            InitParam ip = (InitParam) i.next();
            result.add(ip.getParamName());
        }
        return result.iterator();
    }

    /**
     * Add an init param to a filter.
     *
     * @param webXml The webXml file to use
     * @param name The name of the filter
     * @param paramName The name of the parameter
     * @param paramValue The value of the parameter
     */
    public static void addFilterInitParam(WebXml webXml, String name, String paramName,
        String paramValue)
    {
        WebXmlElement element =
            (WebXmlElement) webXml.getTagByIdentifier(WebXmlType.FILTER, name);
        addTagInitParam(element, paramName, paramValue);

    }

    /**
     * Does the web xml have a named servlet.
     *
     * @param webXml The webXml file to use
     * @param servletName The name of the servlet
     * @return <code>true</code> if it does, <code>false</code> if not.
     */
    public static boolean hasServlet(WebXml webXml, String servletName)
    {
        return webXml.getTagByIdentifier(WebXmlType.SERVLET, servletName) != null;
    }

    /**
     * Add a servlet to the descriptor.
     *
     * @param webXml The webXml file to use
     * @param servletName The servlet name
     * @param servletClass The servlet class name
     */
    public static void addServlet(WebXml webXml, String servletName, String servletClass)
    {
        WebXmlTag tag = (WebXmlTag)webXml.getDescriptorType().getTagByName("servlet");
        Servlet servlet = new Servlet(tag, servletName, servletClass);
        webXml.addTag(servlet);
    }

    /**
     * Get the names that this servlet uses.
     * @param webXml The webXml file to use
     * @param className the name of the class
     * @return Iterator
     */
    public static Iterator getServletNamesForClass(WebXml webXml, String className)
    {
        List items = webXml.getTags(WebXmlType.SERVLET);

        List result = new ArrayList(items.size());
        for (Iterator i = items.iterator(); i.hasNext();)
        {
            Servlet servlet = (Servlet) i.next();
            if (servlet.getServletClass().equals(className))
            {
                result.add(servlet.getServletName());
            }
        }
        return result.iterator();
    }

    /**
     * Returns a list of names of servlets that are mapped to the specified JSP file.
     *
     * @param webXml The webXml file to use
     * @param theJspFile The path to the JSP file, relative to the root of the web-application
     * @return An iterator over the names of the servlets mapped to the JSP file
     */
    public static Iterator getServletNamesForJspFile(WebXml webXml, String theJspFile)
    {
        if (theJspFile == null)
        {
            throw new NullPointerException();
        }
        Iterator servletElements = webXml.getElements(WebXmlType.SERVLET);
        List servletNames = new ArrayList();
        while (servletElements.hasNext())
        {
            Element servletElement = (Element) servletElements.next();
            Element thisElement =
                servletElement.getChild(WebXmlType.JSP_FILE, servletElement.getNamespace());
            if (thisElement != null && theJspFile.equals(thisElement.getText()))
            {
                servletNames.add(servletElement.getChild(
                    WebXmlType.SERVLET_NAME, servletElement.getNamespace()).getText());

            }
        }
        return servletNames.iterator();
    }

    /**
     * Get an iterator of the servlet names in the web xml.
     *
     * @param webXml The webXml file to use
     * @return Iterator
     */
    public static Iterator getServletNames(WebXml webXml)
    {
        List items = webXml.getTags(WebXmlType.SERVLET);

        List result = new ArrayList(items.size());
        for (Iterator i = items.iterator(); i.hasNext();)
        {
            Servlet servlet = (Servlet) i.next();
            result.add(servlet.getServletName());
        }
        return result.iterator();
    }

    /**
     * Get the servlet mappings to the named servlet.
     *
     * @param webXml The webXml file to use
     * @param theServletName The name of the servlet
     * @return An iterator over the mappings
     */
    public static Iterator getServletMappings(WebXml webXml, String theServletName)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        List servletMappings = new ArrayList();
        Iterator servletMappingElements = webXml.getElements(WebXmlType.SERVLET_MAPPING);
        while (servletMappingElements.hasNext())
        {
            Element servletMappingElement = (Element) servletMappingElements.next();
            if (theServletName.equals(servletMappingElement.getChild(
                WebXmlType.SERVLET_NAME, servletMappingElement.getNamespace()).getText()))
            {
                String urlPattern =
                    servletMappingElement.getChild(WebXmlType.URL_PATTERN, servletMappingElement.getNamespace() )
                        .getText();
                if (urlPattern != null)
                {
                    servletMappings.add(urlPattern);
                }
            }
        }
        return servletMappings.iterator();
    }

    /**
     * Add a servlet to the web xml.
     *
     * @param webXml The webXml file to use
     * @param element The servlet element
     */
    public static void addServlet(WebXml webXml, Servlet element)
    {
        if (element == null)
        {
            throw new NullPointerException();
        }
        webXml.addTag(element);
    }

    /**
     * Add an init param to a servlet.
     *
     * @param webXml The webXml file to use
     * @param theServletName The name of the servlet
     * @param name The name of the init param
     * @param value The value for the init param
     */
    public static void addServletInitParam(WebXml webXml, String theServletName, String name,
        String value)
    {
        WebXmlTag tag = (WebXmlTag)webXml.getDescriptorType().getTagByName("init-param");
        Servlet servletElement = getServlet(webXml, theServletName);
        if (servletElement == null)
        {
            throw new IllegalStateException("Servlet '" + theServletName + "' not defined");
        }
        InitParam ip = new InitParam(tag);
        ip.setParamName(name);
        ip.setParamValue(value);

        Element loadOnStartupElements =
            servletElement.getChild(WebXmlType.LOAD_ON_STARTUP, servletElement.getNamespace());

        if (loadOnStartupElements != null)
        {
            servletElement.addContent(loadOnStartupElements.getParentElement().getChildren()
                .indexOf(loadOnStartupElements), ip);
        }
        else
        {
            servletElement.addContent(ip);
        }

    }

    /**
     * Get the names of all the servlet init parameters.
     *
     * @param webXml The webXml file to use
     * @param theServletName The name of the servlet
     * @return An iterator over the parameter names
     */
    public static Iterator getServletInitParamNames(WebXml webXml, String theServletName)
    {
        return getInitParamNames(webXml, getServlet(webXml, theServletName));
    }

    /**
     * Get the names of all the servlet init parameters.
     *
     * @param webXml The webXml file to use
     * @param theElement The element containing the servlet
     * @return An iterator over the parameter names
     */
    private static Iterator getInitParamNames(WebXml webXml, Element theElement)
    {
        List initParamNames = new ArrayList();
        if (theElement != null)
        {
            List initParamElements =
                theElement.getChildren(WebXmlType.INIT_PARAM, theElement.getNamespace());
            for (int i = 0; i < initParamElements.size(); i++)
            {
                Element initParamElement = (Element) initParamElements.get(i);
                String paramName =
                    initParamElement.getChildText(WebXmlType.PARAM_NAME, theElement.getNamespace());
                if (paramName != null)
                {
                    initParamNames.add(paramName);
                }
            }
        }
        return initParamNames.iterator();
    }

    /**
     * Add a security constraint.
     *
     * @param webXml The webXml file to use
     * @param theWebResourceName The name of the web resource
     * @param theUrlPattern The URL Pattern
     * @param theRoles the Roles to Allow
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws SecurityException
     */
    public static void addSecurityConstraint(WebXml webXml, String theWebResourceName,
        String theUrlPattern, List theRoles)
    {
        if ((theWebResourceName == null) || (theUrlPattern == null) || (theRoles == null))
        {
            throw new NullPointerException();
        }
        if (hasSecurityConstraint(webXml, theUrlPattern))
        {
            throw new IllegalStateException("Security constraint for URL " + "pattern "
                + theUrlPattern + " already defined");
        }

        WebXmlElement securityConstraintElement =
            (WebXmlElement) webXml.getDescriptorType().getTagByName(
                WebXmlType.SECURITY_CONSTRAINT).create();

        Element webResourceCollectionElement =
            webXml.getDescriptorType().getTagByName(WebXmlType.WEB_RESOURCE_COLLECTION).create();

        webResourceCollectionElement.addContent(webXml.getDescriptorType().getTagByName(
            WebXmlType.WEB_RESOURCE_NAME).create().setText(theWebResourceName));

        webResourceCollectionElement.addContent(webXml.getDescriptorType().getTagByName(
            WebXmlType.URL_PATTERN).create().setText(theUrlPattern));

        securityConstraintElement.addContent(webResourceCollectionElement);

        Element authConstraintElement = webXml.getDescriptorType().getTagByName(
            WebXmlType.AUTH_CONSTRAINT).create();

        for (Iterator i = theRoles.iterator(); i.hasNext();)
        {
            authConstraintElement.addContent(webXml.getDescriptorType().getTagByName(
                WebXmlType.ROLE_NAME).create().setText(
                (String) i.next()));
        }

        securityConstraintElement.addContent(authConstraintElement);
        webXml.addTag(securityConstraintElement);

    }

    /**
     * Add a JSP file.
     *
     * @param webXml The webXml file to use
     * @param theServletName The name of the servlet
     * @param theJspFile The name of the JSP file
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws SecurityException
     */
    public static void addJspFile(WebXml webXml, String theServletName, String theJspFile)
    {
        if (theServletName == null)
        {
            throw new NullPointerException();
        }
        if (hasFilter(webXml, theServletName))
        {
            throw new IllegalStateException("Servlet '" + theServletName + "' already defined");
        }
        WebXmlElement servletElement = (WebXmlElement) webXml.getDescriptorType().getTagByName(
            WebXmlType.SERVLET).create();

        servletElement.addContent(webXml.getDescriptorType().getTagByName(
            WebXmlType.SERVLET_NAME).create().setText(theServletName));

        servletElement.addContent(webXml.getDescriptorType().getTagByName(
            WebXmlType.JSP_FILE).create().setText(theJspFile));

        webXml.addTag(servletElement);

    }

    /**
     * Does the descriptor have a security constraint for a URL?
     *
     * @param webXml The webXml file to use
     * @param theUrlPattern The URL pattern to query
     * @return boolean
     */
    public static boolean hasSecurityConstraint(WebXml webXml, String theUrlPattern)
    {
        return (getSecurityConstraint(webXml, theUrlPattern) != null);
    }

    /**
     * Does the descriptor have a login config?
     *
     * @param webXml The webXml file to use
     * @return boolean
     */
    public static boolean hasLoginConfig(WebXml webXml)
    {
        return getLoginConfig(webXml) != null;
    }

    /**
     * Get the login config.
     *
     * @param webXml The webXml file to use
     * @return The element containing the login config
     */
    private static Element getLoginConfig(WebXml webXml)
    {
        return webXml.getTag(WebXmlType.LOGIN_CONFIG);
    }

    /**
     * Set the login config.
     *
     * @param webXml The webXml file to use
     * @param theAuthMethod The authorization method
     * @param theRealmName The realm name
     */
    public static void setLoginConfig(WebXml webXml, String theAuthMethod,
        String theRealmName)
    {
        if ((theRealmName == null) || (theAuthMethod == null))
        {
            throw new NullPointerException();
        }
        DescriptorElement loginConfigElement =
            (DescriptorElement) webXml.getDescriptorType().getTagByName(
                WebXmlType.LOGIN_CONFIG).create();
        loginConfigElement.addContent(webXml.getDescriptorType().getTagByName(
            WebXmlType.AUTH_METHOD).create().setText(theAuthMethod));

        loginConfigElement.addContent(webXml.getDescriptorType().getTagByName(
            WebXmlType.REALM_NAME).create().setText(theRealmName));

        webXml.getRootElement().removeContent(
            new org.jdom.filter.ElementFilter(WebXmlType.LOGIN_CONFIG));
        webXml.addTag(loginConfigElement);

    }

    /**
     * Get the login config authorization method.
     *
     * @param webXml The webXml file to use
     * @return the auth method
     */
    public static String getLoginConfigAuthMethod(WebXml webXml)
    {
        DescriptorElement de =  (DescriptorElement)getLoginConfig(webXml);
        return de.getChildText(WebXmlType.AUTH_METHOD, de.getNamespace() );
    }

    /**
     * Get a security constraint by URL.
     *
     * @param webXml The webXml file to use
     * @param theUrlPattern The URL Pattern
     * @return Security Constraint
     */
    public static SecurityConstraint getSecurityConstraint(WebXml webXml, String theUrlPattern)
    {
        if (theUrlPattern == null)
        {
            throw new NullPointerException();
        }
        Iterator securityConstraintElements =
            webXml.getTags(WebXmlType.SECURITY_CONSTRAINT).iterator();
        while (securityConstraintElements.hasNext())
        {
            SecurityConstraint securityConstraintElement =
                (SecurityConstraint) securityConstraintElements.next();
            Iterator webResourceCollectionElements =
                securityConstraintElement.getChildren(
                    WebXmlType.WEB_RESOURCE_COLLECTION, securityConstraintElement.getNamespace()).iterator();
            if (webResourceCollectionElements.hasNext())
            {
                Element webResourceCollectionElement =
                    (Element) webResourceCollectionElements.next();

                String url =
                    webResourceCollectionElement.getChildText(WebXmlType.URL_PATTERN, securityConstraintElement.getNamespace());

                if (theUrlPattern.equals(url))
                {
                    return securityConstraintElement;
                }
            }
        }
        return null;
    }

    /**
     * Does the role have a security definition?
     *
     * @param webXml The webXml file to use
     * @param theRoleName The name of the role
     * @return boolean
     */
    public static boolean hasSecurityRole(WebXml webXml, String theRoleName)
    {
        return (getSecurityRole(webXml, theRoleName) != null);
    }

    /**
     * Get the security role names.
     *
     * @param webXml The webXml file to use
     * @return an iterator over the role names
     */
    public static Iterator getSecurityRoleNames(WebXml webXml)
    {
        List securityRoleNames = new ArrayList();
        Iterator securityRoleElements = webXml.getElements(WebXmlType.SECURITY_ROLE);
        while (securityRoleElements.hasNext())
        {
            Element securityRoleElement = (Element) securityRoleElements.next();
            Element securityRoleName =
                securityRoleElement.getChild(WebXmlType.ROLE_NAME, securityRoleElement.getNamespace());

            if (securityRoleName != null)
            {
                securityRoleNames.add(securityRoleName.getText());
            }
        }
        return securityRoleNames.iterator();
    }

    /**
     * Get the security role by name.
     *
     * @param webXml The webXml file to use
     * @param theRoleName The name of the role
     * @return Element containing the security role
     */
    public static Element getSecurityRole(WebXml webXml, String theRoleName)
    {
        if (theRoleName == null)
        {
            throw new NullPointerException();
        }
        Iterator securityRoleElements =
            webXml.getTags(WebXmlType.SECURITY_ROLE).iterator();
        while (securityRoleElements.hasNext())
        {
            DescriptorElement securityRoleElement = (DescriptorElement) securityRoleElements.next();
            if (theRoleName.equals(securityRoleElement.getChildText(
                WebXmlType.ROLE_NAME, securityRoleElement.getNamespace() )))
            {
                return securityRoleElement;
            }
        }
        return null;
    }

    /**
     * Add an EJB Reference.
     *
     * @param webXml The webXml file to use
     * @param ref the EJB Reference element to add
     */
    public static void addEjbRef(WebXml webXml, EjbRef ref)
    {
        DescriptorElement ejbRefElement = webXml.getDescriptorType().getTagByName(
            WebXmlType.EJB_LOCAL_REF).create();

        ejbRefElement.setAttribute("id", ref.getName().replace('/', '_'));
        ejbRefElement
            .addContent(webXml.getDescriptorType().getTagByName(
                WebXmlType.EJB_REF_NAME).create().setText(ref.getName()));
        ejbRefElement
            .addContent(webXml.getDescriptorType().getTagByName(
                WebXmlType.EJB_REF_TYPE).create().setText(ref.getType()));
        if (ref.isLocal())
        {
            ejbRefElement.addContent(webXml.getDescriptorType().getTagByName(
                WebXmlType.LOCAL_HOME).create().setText(
                ref.getEjbHomeInterface()));
            ejbRefElement.addContent(webXml.getDescriptorType().getTagByName(
                WebXmlType.LOCAL).create().setText(
                ref.getEjbInterface()));
        }
        else
        {
            ejbRefElement.addContent(webXml.getDescriptorType().getTagByName(WebXmlType.HOME).create().setText(
                ref.getEjbHomeInterface()));
            ejbRefElement.addContent(webXml.getDescriptorType().getTagByName(
                WebXmlType.REMOTE).create().setText(
                ref.getEjbInterface()));

        }
        if (ref.getEjbName() != null)
        {
            ejbRefElement.addContent(webXml.getDescriptorType().getTagByName(
                WebXmlType.EJB_LINK).create().setText(
                ref.getEjbName()));
        }
        else if (ref.getJndiName() != null)
        {
            Iterator i = webXml.getVendorDescriptors();
            while (i.hasNext())
            {
                VendorWebAppDescriptor descr = (VendorWebAppDescriptor) i.next();
                descr.addEjbReference(ref);
            }
        }
        else
        {
            throw new IllegalStateException("Either ejbName or jndiName must be set.");
        }

        webXml.addTag(ejbRefElement);

    }

    /**
     * @param webXml The webXml file to use
     * @param theRoleName The role name to use
     */
    public static void addSecurityRole(WebXml webXml, String theRoleName)
    {
        if (theRoleName == null)
        {
            throw new NullPointerException();
        }
        if (hasSecurityRole(webXml, theRoleName))
        {
            throw new IllegalStateException("Security role '" + theRoleName + "' already defined");
        }
        Element securityRoleElement = webXml.getDescriptorType().getTagByName(
            WebXmlType.SECURITY_ROLE).create();

        securityRoleElement.addContent(webXml.getDescriptorType().getTagByName(WebXmlType.ROLE_NAME).create()
            .setText(theRoleName));

        webXml.getRootElement().addContent(securityRoleElement);

    }

    /**
     * @param theWebXml The webXml file to use
     * @param servletName The name of the servlet to get
     * @return the servlet
     */
    public static Servlet getServlet(WebXml theWebXml, String servletName)
    {
        return (Servlet) theWebXml.getTagByIdentifier(WebXmlType.SERVLET, servletName);
    }

    /**
     * @param webXml The webXml file to use
     * @return Iterator
     */
    public static Iterator getFilterNames(WebXml webXml)
    {
        List items = webXml.getTags(WebXmlType.FILTER);

        List result = new ArrayList(items.size());
        for (Iterator i = items.iterator(); i.hasNext();)
        {
            Filter filter = (Filter) i.next();
            result.add(filter.getFilterName());
        }
        return result.iterator();
    }

    /**
     * @param theWebXml The webXml file to use
     * @param filterName The name of the filter
     * @return Filter
     */
    public static Filter getFilter(WebXml theWebXml, String filterName)
    {
        return (Filter) theWebXml.getTagByIdentifier(WebXmlType.FILTER, filterName);
    }

    /**
     * @param webXml The webXml file to use
     * @param rhs The mapping to add
     */
    public static void addFilterMapping(WebXml webXml, FilterMapping rhs)
    {
        String filterName = rhs.getFilterName();
              
        if (!hasFilter(webXml, filterName))
        {
            throw new IllegalStateException("Filter '" + filterName + "' not defined");
        }
        
        
        
        FilterMapping filterMappingElement = (FilterMapping)webXml.getDescriptorType().getTagByName(
            WebXmlType.FILTER_MAPPING).create();

        filterMappingElement.addContent(webXml.getDescriptorType().getTagByName(
            WebXmlType.FILTER_NAME).create().setText(
            filterName));

        String urlPattern = rhs.getUrlPattern();
        
        if( urlPattern != null )
        {
          filterMappingElement.addContent(webXml.getDescriptorType().getTagByName(
              WebXmlType.URL_PATTERN).create().setText(
              urlPattern));
        }
        else
        {
          // must be servlet name instead
          String servletName = rhs.getServletName();
          if( servletName == null )
          {
            throw new IllegalStateException("Filter '" + filterName + "' has neither a servlet-name nor a url-pattern.");
          }
          filterMappingElement.setServletName(servletName);
        }
        
        String[] dispatchers = rhs.getDispatchers();
        
        if( dispatchers != null )
        {
          for( int i=0; i<dispatchers.length; i++ )
          {
            filterMappingElement.addDispatcher(dispatchers[i]);
          }
        }

        webXml.addElement(filterMappingElement.getTag(), filterMappingElement, webXml.getRootElement() );

    }

    /**
     * @param theWebXml The webXml file to use
     * @param servletName The servlet name
     * @param paramName The parameter Name
     * @return The value of the init param
     */
    public static String getServletInitParam(WebXml theWebXml, String servletName,
        String paramName)
    {
        return getInitParam(theWebXml, getServlet(theWebXml, servletName), paramName);
    }

    /**
     * @param theWebXml The webXml file to use
     * @param theElement The element to get the parameter from
     * @param theParamName The name of the parameter
     * @return The value of the init param
     */
    private static String getInitParam(WebXml theWebXml, Element theElement,
        String theParamName)
    {
        if (theElement != null)
        {
            List initParamElements =
                theElement.getChildren(WebXmlType.INIT_PARAM, theElement.getNamespace());
            for (int i = 0; i < initParamElements.size(); i++)
            {
                Element initParamElement = (Element) initParamElements.get(i);
                String paramName =
                    initParamElement.getChildText(WebXmlType.PARAM_NAME, theElement.getNamespace());
                if (theParamName.equals(paramName))
                {
                    return initParamElement.getChildText(WebXmlType.PARAM_VALUE, theElement.getNamespace());
                }
            }
        }
        return null;
    }

    /**
     * @param webXml The webXml file to use
     * @param theServletName The name of the servlet
     * @param theRoleName The role name to add
     */
    public static void addServletRunAsRoleName(WebXml webXml, String theServletName,
        String theRoleName)
    {
        Element servlet = getServlet(webXml, theServletName);
        Element runAsElement = webXml.getDescriptorType().getTagByName(WebXmlType.RUN_AS).create();

        runAsElement.addContent(webXml.getDescriptorType().getTagByName(
            WebXmlType.ROLE_NAME).create().setText(theRoleName));

        servlet.addContent(runAsElement);

    }

    /**
     * @param webXml The webXml file to use
     * @param theServletName The name of the servlet
     * @param theUrlPattern the URL PAttern to add
     */
    public static void addServletMapping(WebXml webXml, String theServletName,
        String theUrlPattern)
    {
        if (!hasServlet(webXml, theServletName))
        {
            throw new IllegalStateException("Servlet '" + theServletName + "' not defined");
        }
        DescriptorElement servletMappingElement = webXml.getDescriptorType().getTagByName(
            WebXmlType.SERVLET_MAPPING).create();

        servletMappingElement.addContent(webXml.getDescriptorType().getTagByName(
            WebXmlType.SERVLET_NAME).create().setText(
            theServletName));
        servletMappingElement.addContent(webXml.getDescriptorType().getTagByName(
            WebXmlType.URL_PATTERN).create().setText(
            theUrlPattern));

        webXml.addElement(servletMappingElement.getTag(), servletMappingElement, webXml.getRootElement() );
    }

    /**
     * @param theWebXml The webXml file to use
     * @param filterName The name of the filter
     * @param paramName The name of the parameter
     * @return the init parameter value
     */
    public static String getFilterInitParam(WebXml theWebXml,
        String filterName, String paramName)
    {
        Filter filter =
            (Filter) theWebXml.getTagByIdentifier(
                WebXmlType.FILTER, filterName);
        if( filter == null )
        {
        	throw new IllegalStateException("Filter '" + filterName + "' not defined");
        }

        InitParam initParam =  filter.getInitParam(paramName);
        if( initParam == null )
        {
        	throw new IllegalStateException("Filter '" + filterName + "' Initialization parameter '" + paramName + "' not defined");
        }

        return initParam.getParamValue();

    }

    /**
     * @param webXml The webXml file to use
     * @param filter The filter to add
     */
    public static void addFilter(WebXml webXml, Filter filter)
    {
        webXml.addTag(filter);
    }

    /**
     * Add a filter to the descriptor.
     *
     * @param webXml The webXml file to use
     * @param filterName The servlet name
     * @param filterClass The servlet class name
     */
    public static void addFilter(WebXml webXml, String filterName, String filterClass)
    {
        WebXmlTag tag = (WebXmlTag)webXml.getDescriptorType().getTagByName("filter");
        Filter filter = new Filter(tag, filterName, filterClass);
        webXml.addTag(filter);
    }
}
