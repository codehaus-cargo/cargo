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

import org.codehaus.cargo.module.DescriptorTag;

/**
 * Represents the various top-level tags in a web deployment descriptor as a typesafe enumeration.
 * 
 * @version $Id$
 */
public class WeblogicXmlTag extends DescriptorTag
{
    /**
     * Element name 'description'.
     */
    public static final WeblogicXmlTag DESCRIPTION = new WeblogicXmlTag("description");

    /**
     * Element name 'weblogic-version'.
     */
    public static final WeblogicXmlTag WEBLOGIC_VERSION = new WeblogicXmlTag("weblogic-version");

    /**
     * Element name 'security-role-assignment'.
     */
    public static final WeblogicXmlTag SECURITY_ROLE_ASSIGNMENT =
        new WeblogicXmlTag("security-role-assignment");

    /**
     * Element name 'run-as-role-assignment'.
     */
    public static final WeblogicXmlTag RUN_AS_ROLE_ASSIGNMENT =
        new WeblogicXmlTag("run-as-role-assignment");

    /**
     * Element name 'reference-descriptor'.
     */
    public static final WeblogicXmlTag REFERENCE_DESCRIPTOR =
        new WeblogicXmlTag("reference-descriptor");
    
    /**
     * Element name 'session-descriptor'.
     */
    public static final WeblogicXmlTag SESSION_DESCRIPTOR = 
        new WeblogicXmlTag("session-descriptor");
    
    /**
     * Element name 'jsp-descriptor'.
     */
    public static final WeblogicXmlTag JSP_DESCRIPTOR = new WeblogicXmlTag("jsp-descriptor");
    
    /**
     * Element name 'auth-filter'.
     */
    public static final WeblogicXmlTag AUTH_FILTER = new WeblogicXmlTag("auth-filter");
    
    /**
     * Element name 'container-descriptor'.
     */
    public static final WeblogicXmlTag CONTAINER_DESCRIPTOR =
        new WeblogicXmlTag("container-descriptor");

    /**
     * Element name 'charset-params'.
     */
    public static final WeblogicXmlTag CHARSET_PARAMS = new WeblogicXmlTag("charset-params");
    
    /**
     * Element name 'virtual-directory-mapping'.
     */
    public static final WeblogicXmlTag VIRTUAL_DIRECTORY_MAPPING =
        new WeblogicXmlTag("virtual-directory-mapping");
    
    /**
     * Element name 'url-match-map'.
     */
    public static final WeblogicXmlTag URL_MATCH_MAP = new WeblogicXmlTag("url-match-map");
    
    /**
     * Element name 'preprocessor'.
     */
    public static final WeblogicXmlTag PREPROCESSOR = new WeblogicXmlTag("preprocessor");
    
    /**
     * Element name 'preprocessor-mapping'.
     */
    public static final WeblogicXmlTag PREPROCESSOR_MAPPING =
        new WeblogicXmlTag("preprocessor-mapping");

    /**
     * Element name 'security-permission'.
     */
    public static final WeblogicXmlTag SECURITY_PERMISSION =
        new WeblogicXmlTag("security-permission");

    /**
     * Element name 'context-root'.
     */
    public static final WeblogicXmlTag CONTEXT_ROOT = new WeblogicXmlTag("context-root");

    /**
     * Element name 'wl-dispatch-policy'.
     */
    public static final WeblogicXmlTag WL_DISPATCH_POLICY =
        new WeblogicXmlTag("wl-dispatch-policy");

    /**
     * Element name 'servlet-descriptor'.
     */
    public static final WeblogicXmlTag SERVLET_DESCRIPTOR =
        new WeblogicXmlTag("servlet-descriptor");

    /**
     * Element name 'init-as'.
     */
    public static final WeblogicXmlTag INIT_AS = new WeblogicXmlTag("init-as");

    /**
     * Element name 'destroy-as'.
     */
    public static final WeblogicXmlTag DESTROY_AS = new WeblogicXmlTag("destroy-as");

    /**
     * Element name 'resource-description'.
     */
    public static final WeblogicXmlTag RESOURCE_DESCRIPTION =
        new WeblogicXmlTag("resource-description");
    
    /**
     * Element name 'resource-env-description'.
     */
    public static final WeblogicXmlTag RESOURCE_ENV_DESCRIPTION =
        new WeblogicXmlTag("resource-env-description");
    
    /**
     * Element name 'ejb-reference-description'.
     */
    public static final WeblogicXmlTag EJB_REFERENCE_DESCRIPTION =
        new WeblogicXmlTag("ejb-reference-description");
    
    /**
     * Element name 'ejb-ref-name'.
     */
    public static final WeblogicXmlTag EJB_REF_NAME = new WeblogicXmlTag("ejb-ref-name");
    
    /**
     * Element name 'jndi-name'.
     */
    public static final WeblogicXmlTag JNDI_NAME = new WeblogicXmlTag("jndi-name");
    
    /**
     * Constructor.
     * 
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    protected WeblogicXmlTag(String tagName, boolean isMultipleAllowed)
    {
        super(tagName, isMultipleAllowed);
    }

    /**
     * Constructor.
     * 
     * @param tagName The tag name of the element
     */
    protected WeblogicXmlTag(String tagName)
    {
        this(tagName, true);
    }
}
