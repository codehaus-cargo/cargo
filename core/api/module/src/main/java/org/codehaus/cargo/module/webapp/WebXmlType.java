/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
 * 
 * Copyright 2004-2005 Vincent Massol.
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

import org.codehaus.cargo.module.AbstractDescriptorType;
import org.codehaus.cargo.module.Dtd;
import org.codehaus.cargo.module.Identifier;
import org.codehaus.cargo.module.webapp.elements.AuthConstraint;
import org.codehaus.cargo.module.webapp.elements.ContextParam;
import org.codehaus.cargo.module.webapp.elements.Filter;
import org.codehaus.cargo.module.webapp.elements.FilterMapping;
import org.codehaus.cargo.module.webapp.elements.InitParam;
import org.codehaus.cargo.module.webapp.elements.SecurityConstraint;
import org.codehaus.cargo.module.webapp.elements.Servlet;

/**
 * A document type for web.xml web deployment descriptors.
 * 
 * @version $Id: $
 */
public class WebXmlType extends AbstractDescriptorType
{        
    /**
     * Element name 'icon'.
     */
    public static final String ICON = "icon";

    /**
     * Element name 'display-name'.
     */
    public static final String DISPLAY_NAME = "display-name";

    /**
     * Element name 'description'.
     */
    public static final String DESCRIPTION = "description";

    /**
     * Element name 'distributable'.
     */
    public static final String DISTRIBUTABLE = "distributable";

    /**
     * Element name 'context-param'.
     */
    public static final String CONTEXT_PARAM = "context-param";        

    /**
     * Element name 'param-name'.
     */
    public static final String PARAM_NAME = "param-name";

    /**
     * Element name 'param-value'.
     */
    public static final String PARAM_VALUE = "param-value";

    /**
     * Element name 'filter'.
     */
    public static final String FILTER = "filter";        

    /**
     * Element name 'filter-name'.
     */
    public static final String FILTER_NAME = "filter-name";

    /**
     * Element name 'filter-class'.
     */
    public static final String FILTER_CLASS = "filter-class";

    /**
     * Element name 'filter-mapping'.
     */
    public static final String FILTER_MAPPING = "filter-mapping";

    /**
     * Element name 'init-param'.
     */
    public static final String INIT_PARAM = "init-param";
    
    /**
     * Element name 'listener'.
     */
    public static final String LISTENER = "listener";

    /**
     * Element name 'servlet'.
     */
    public static final String SERVLET = "servlet";

    /**
     * Element name 'servlet-name'.
     */
    public static final String SERVLET_NAME = "servlet-name";

    /**
     * Element name 'jsp-file'.
     */
    public static final String JSP_FILE = "jsp-file";

    /**
     * Element name 'servlet-class'.
     */
    public static final String SERVLET_CLASS = "servlet-class";

    /**
     * Element name 'load-on-startup'.
     */
    public static final String LOAD_ON_STARTUP = "load-on-startup";

    /**
     * Element name 'run-as'.
     */
    public static final String RUN_AS = "run-as";

    /**
     * Element name 'servlet-mapping'.
     */
    public static final String SERVLET_MAPPING = "servlet-mapping";

    /**
     * Element name 'url-pattern'.
     */
    public static final String URL_PATTERN = "url-pattern";

    /**
     * Element name 'session-config'.
     */
    public static final String SESSION_CONFIG = "session-config";

    /**
     * Element name 'mime-mapping'.
     */
    public static final String MIME_MAPPING = "mime-mapping";

    /**
     * Element name 'welcome-file-list'.
     */
    public static final String WELCOME_FILE_LIST = "welcome-file-list";

    /**
     * Element name 'error-page'.
     */
    public static final String ERROR_PAGE = "error-page";

    /**
     * Element name 'taglib'.
     */
    public static final String TAGLIB = "taglib";

    /**
     * Element name 'resource-env-ref'.
     */
    public static final String RESOURCE_ENV_REF = "resource-env-ref";

    /**
     * Element name 'resource-ref'.
     */
    public static final String RESOURCE_REF = "resource-ref";

    /**
     * Element name 'security-constraint'.
     */
    public static final String SECURITY_CONSTRAINT = "security-constraint";

    /**
     * Element name 'web-resource-collection'.
     */
    public static final String WEB_RESOURCE_COLLECTION = "web-resource-collection";

    /**
     * Element name 'web-resource-name'.
     */
    public static final String WEB_RESOURCE_NAME = "web-resource-name";

    /**
     * Element name 'auth-constraint'.
     */
    public static final String AUTH_CONSTRAINT = "auth-constraint";

    /**
     * Element name 'login-config'.
     */
    public static final String LOGIN_CONFIG = "login-config";

    /**
     * Element name 'auth-method'.
     */
    public static final String AUTH_METHOD = "auth-method";

    /**
     * Element name 'realm-name'.
     */
    public static final String REALM_NAME = "realm-name";

    /**
     * Element name 'security-role'.
     */
    public static final String SECURITY_ROLE = "security-role";

    /**
     * Element name 'role-name'.
     */
    public static final String ROLE_NAME = "role-name";

    /**
     * Element name 'env-entry'.
     */
    public static final String ENV_ENTRY = "env-entry";

    /**
     * Element name 'ejb-ref'.
     */
    public static final String EJB_REF = "ejb-ref";

    /**
     * Element name 'ejb-local-ref'.
     */
    public static final String EJB_LOCAL_REF = "ejb-local-ref";

    /**
     * Element name 'ejb-ref-name'.
     */
    public static final String EJB_REF_NAME = "ejb-ref-name";

    /**
     * Element name 'ejb-ref-type'.
     */
    public static final String EJB_REF_TYPE = "ejb-ref-type";

    /**
     * Element name 'local'.
     */
    public static final String LOCAL = "local";

    /**
     * Element name 'local-home'.
     */
    public static final String LOCAL_HOME = "local-home";

    /**
     * Element name 'remote'.
     */
    public static final String REMOTE = "remote";

    /**
     * Element name 'home'.
     */
    public static final String HOME = "home";

    /**
     * Element name 'local-home'.
     */
    public static final String EJB_LINK = "ejb-link";

    /**
     * Single instance.
     */
    private static WebXmlType instance = new WebXmlType(null, null);

    /**
     * Ensure all tags constructed.
     */
    private WebXmlTag[] tags =
        new WebXmlTag[] {
            new WebXmlTag(this, "icon", false),
            new WebXmlTag(this, "display-name", false),
            new WebXmlTag(this, "description", false),
            new WebXmlTag(this, "distributable", false),
            new WebXmlTag(this,
                "context-param",
                true,
                new Identifier("param-name"),
                ContextParam.class),
            new WebXmlTag(this, "param-name"),
            new WebXmlTag(this, "param-value"),
            new WebXmlTag(this, "filter", true, new Identifier("filter-name"), Filter.class),
            new WebXmlTag(this, "filter-name"),
            new WebXmlTag(this, "filter-class"),
            new WebXmlTag(this,
                "filter-mapping",
                true,
                new Identifier("filter-name"),
                FilterMapping.class),
            new WebXmlTag(this, "init-param", true, new Identifier("param-name"), InitParam.class),
            new WebXmlTag(this, "listener"),
            new WebXmlTag(this, "servlet", true, new Identifier("servlet-name"), Servlet.class),
            new WebXmlTag(this, "servlet-name"), new WebXmlTag(this, "jsp-file"),
            new WebXmlTag(this, "servlet-class"), new WebXmlTag(this, "load-on-startup"),
            new WebXmlTag(this, "run-as"), new WebXmlTag(this, "servlet-mapping"),
            new WebXmlTag(this, "url-pattern"), new WebXmlTag(this, "session-config", false),
            new WebXmlTag(this, "mime-mapping"), new WebXmlTag(this, "welcome-file-list", false),
            new WebXmlTag(this, "error-page"), new WebXmlTag(this, "taglib"),
            new WebXmlTag(this, "resource-env-ref"), new WebXmlTag(this, "resource-ref"),
            new WebXmlTag(this, "security-constraint", true, null, SecurityConstraint.class),
            new WebXmlTag(this, "web-resource-collection"), 
            new WebXmlTag(this, "web-resource-name"),
            new WebXmlTag(this, "auth-constraint", false, null, AuthConstraint.class),
            new WebXmlTag(this, "login-config", false), new WebXmlTag(this, "auth-method"),
            new WebXmlTag(this, "realm-name"),
            new WebXmlTag(this, "security-role", true, new Identifier("role-name"), null),
            new WebXmlTag(this, "role-name"), new WebXmlTag(this, "env-entry"),
            new WebXmlTag(this, "ejb-ref", true, new Identifier("ejb-ref-name"), null),
            new WebXmlTag(this, "ejb-local-ref"), new WebXmlTag(this, "ejb-ref-name"),
            new WebXmlTag(this, "ejb-ref-type"), new WebXmlTag(this, "local"),
            new WebXmlTag(this, "local-home"), new WebXmlTag(this, "remote"),
            new WebXmlTag(this, "home"), new WebXmlTag(this, "ejb-link")
        };
    /**
     * Protected constructor.
     * 
     * @param parent Parent type if any
     * @param grammar grammar to use
     */
    protected WebXmlType(AbstractDescriptorType parent, Dtd grammar)
    {
        super(parent, WebXml.class, grammar);
        setDescriptorIo(new WebXmlIo(this));
    }
    
    /**
     * Get the instance of the WEB XML Type.
     * @return WebXmlType
     */
    public static WebXmlType getInstance()
    {
        return instance;
    }

    
    

}
