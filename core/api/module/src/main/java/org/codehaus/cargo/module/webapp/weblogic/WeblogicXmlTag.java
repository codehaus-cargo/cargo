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
import org.codehaus.cargo.module.DescriptorType;

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
    public static final String DESCRIPTION = "description";

    /**
     * Element name 'weblogic-version'.
     */
    public static final String WEBLOGIC_VERSION = "weblogic-version";

    /**
     * Element name 'security-role-assignment'.
     */
    public static final String SECURITY_ROLE_ASSIGNMENT =
        "security-role-assignment";

    /**
     * Element name 'run-as-role-assignment'.
     */
    public static final String RUN_AS_ROLE_ASSIGNMENT =
        "run-as-role-assignment";

    /**
     * Element name 'reference-descriptor'.
     */
    public static final String REFERENCE_DESCRIPTOR =
        "reference-descriptor";
    
    /**
     * Element name 'session-descriptor'.
     */
    public static final String SESSION_DESCRIPTOR = 
        "session-descriptor";
    
    /**
     * Element name 'jsp-descriptor'.
     */
    public static final String JSP_DESCRIPTOR = "jsp-descriptor";
    
    /**
     * Element name 'auth-filter'.
     */
    public static final String AUTH_FILTER = "auth-filter";
    
    /**
     * Element name 'container-descriptor'.
     */
    public static final String CONTAINER_DESCRIPTOR =
        "container-descriptor";

    /**
     * Element name 'charset-params'.
     */
    public static final String CHARSET_PARAMS = "charset-params";
    
    /**
     * Element name 'virtual-directory-mapping'.
     */
    public static final String VIRTUAL_DIRECTORY_MAPPING =
        "virtual-directory-mapping";
    
    /**
     * Element name 'url-match-map'.
     */
    public static final String URL_MATCH_MAP = "url-match-map";
    
    /**
     * Element name 'preprocessor'.
     */
    public static final String PREPROCESSOR = "preprocessor";
    
    /**
     * Element name 'preprocessor-mapping'.
     */
    public static final String PREPROCESSOR_MAPPING =
        "preprocessor-mapping";

    /**
     * Element name 'security-permission'.
     */
    public static final String SECURITY_PERMISSION =
        "security-permission";

    /**
     * Element name 'context-root'.
     */
    public static final String CONTEXT_ROOT = "context-root";

    /**
     * Element name 'wl-dispatch-policy'.
     */
    public static final String WL_DISPATCH_POLICY =
        "wl-dispatch-policy";

    /**
     * Element name 'servlet-descriptor'.
     */
    public static final String SERVLET_DESCRIPTOR =
        "servlet-descriptor";

    /**
     * Element name 'init-as'.
     */
    public static final String INIT_AS = "init-as";

    /**
     * Element name 'destroy-as'.
     */
    public static final String DESTROY_AS = "destroy-as";

    /**
     * Element name 'resource-description'.
     */
    public static final String RESOURCE_DESCRIPTION =
        "resource-description";
    
    /**
     * Element name 'resource-env-description'.
     */
    public static final String RESOURCE_ENV_DESCRIPTION =
        "resource-env-description";
    
    /**
     * Element name 'ejb-reference-description'.
     */
    public static final String EJB_REFERENCE_DESCRIPTION =
        "ejb-reference-description";
    
    /**
     * Element name 'ejb-ref-name'.
     */
    public static final String EJB_REF_NAME = "ejb-ref-name";
    
    /**
     * Element name 'jndi-name'.
     */
    public static final String JNDI_NAME = "jndi-name";
    
    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    protected WeblogicXmlTag(DescriptorType type, String tagName, boolean isMultipleAllowed)
    {
        super(type, tagName, isMultipleAllowed);
    }

    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     */
    protected WeblogicXmlTag(DescriptorType type, String tagName)
    {
        this(type, tagName, true);
    }
}
