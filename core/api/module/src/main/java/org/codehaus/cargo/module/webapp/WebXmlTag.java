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

import org.codehaus.cargo.module.DescriptorTag;

/**
 * Represents the various top-level tags in a web deployment descriptor as a typesafe enumeration.
 *
 * @version $Id$
 */
public final class WebXmlTag extends DescriptorTag
{
    /**
     * Element name 'icon'.
     */
    public static final WebXmlTag ICON = new WebXmlTag("icon", false);

    /**
     * Element name 'display-name'.
     */
    public static final WebXmlTag DISPLAY_NAME =
        new WebXmlTag("display-name", false);

    /**
     * Element name 'description'.
     */
    public static final WebXmlTag DESCRIPTION =
        new WebXmlTag("description", false);

    /**
     * Element name 'distributable'.
     */
    public static final WebXmlTag DISTRIBUTABLE =
        new WebXmlTag("distributable", false);

    /**
     * Element name 'context-param'.
     */
    public static final WebXmlTag CONTEXT_PARAM =
        new WebXmlTag("context-param");

    /**
     * Element name 'param-name'.
     */
    public static final WebXmlTag PARAM_NAME =
        new WebXmlTag("param-name");

    /**
     * Element name 'param-value'.
     */
    public static final WebXmlTag PARAM_VALUE =
        new WebXmlTag("param-value");

    /**
     * Element name 'filter'.
     */
    public static final WebXmlTag FILTER =
        new WebXmlTag("filter");

    /**
     * Element name 'filter-name'.
     */
    public static final WebXmlTag FILTER_NAME =
        new WebXmlTag("filter-name");

    /**
     * Element name 'filter-class'.
     */
    public static final WebXmlTag FILTER_CLASS =
        new WebXmlTag("filter-class");

    /**
     * Element name 'filter-mapping'.
     */
    public static final WebXmlTag FILTER_MAPPING =
        new WebXmlTag("filter-mapping");

    /**
     * Element name 'init-param'.
     */
    public static final WebXmlTag INIT_PARAM =
        new WebXmlTag("init-param");

    /**
     * Element name 'listener'.
     */
    public static final WebXmlTag LISTENER =
        new WebXmlTag("listener");

    /**
     * Element name 'servlet'.
     */
    public static final WebXmlTag SERVLET =
        new WebXmlTag("servlet");

    /**
     * Element name 'servlet-name'.
     */
    public static final WebXmlTag SERVLET_NAME =
        new WebXmlTag("servlet-name");

    /**
     * Element name 'jsp-file'.
     */
    public static final WebXmlTag JSP_FILE =
        new WebXmlTag("jsp-file");

    /**
     * Element name 'servlet-class'.
     */
    public static final WebXmlTag SERVLET_CLASS =
        new WebXmlTag("servlet-class");

    /**
     * Element name 'load-on-startup'.
     */
    public static final WebXmlTag LOAD_ON_STARTUP =
        new WebXmlTag("load-on-startup");

    /**
     * Element name 'run-as'.
     */
    public static final WebXmlTag RUN_AS =
        new WebXmlTag("run-as");

    /**
     * Element name 'servlet-mapping'.
     */
    public static final WebXmlTag SERVLET_MAPPING =
        new WebXmlTag("servlet-mapping");

    /**
     * Element name 'url-pattern'.
     */
    public static final WebXmlTag URL_PATTERN =
        new WebXmlTag("url-pattern");

    /**
     * Element name 'session-config'.
     */
    public static final WebXmlTag SESSION_CONFIG =
        new WebXmlTag("session-config", false);

    /**
     * Element name 'mime-mapping'.
     */
    public static final WebXmlTag MIME_MAPPING =
        new WebXmlTag("mime-mapping");

    /**
     * Element name 'welcome-file-list'.
     */
    public static final WebXmlTag WELCOME_FILE_LIST =
        new WebXmlTag("welcome-file-list", false);

    /**
     * Element name 'error-page'.
     */
    public static final WebXmlTag ERROR_PAGE =
        new WebXmlTag("error-page");

    /**
     * Element name 'taglib'.
     */
    public static final WebXmlTag TAGLIB =
        new WebXmlTag("taglib");

    /**
     * Element name 'resource-env-ref'.
     */
    public static final WebXmlTag RESOURCE_ENV_REF =
        new WebXmlTag("resource-env-ref");

    /**
     * Element name 'resource-ref'.
     */
    public static final WebXmlTag RESOURCE_REF =
        new WebXmlTag("resource-ref");

    /**
     * Element name 'security-constraint'.
     */
    public static final WebXmlTag SECURITY_CONSTRAINT =
        new WebXmlTag("security-constraint");

    /**
     * Element name 'web-resource-collection'.
     */
    public static final WebXmlTag WEB_RESOURCE_COLLECTION =
        new WebXmlTag("web-resource-collection");

    /**
     * Element name 'web-resource-name'.
     */
    public static final WebXmlTag WEB_RESOURCE_NAME =
        new WebXmlTag("web-resource-name");

    /**
     * Element name 'auth-constraint'.
     */
    public static final WebXmlTag AUTH_CONSTRAINT =
        new WebXmlTag("auth-constraint");

    /**
     * Element name 'login-config'.
     */
    public static final WebXmlTag LOGIN_CONFIG =
        new WebXmlTag("login-config", false);

    /**
     * Element name 'auth-method'.
     */
    public static final WebXmlTag AUTH_METHOD =
        new WebXmlTag("auth-method");

    /**
     * Element name 'realm-name'.
     */
    public static final WebXmlTag REALM_NAME =
        new WebXmlTag("realm-name");

    /**
     * Element name 'security-role'.
     */
    public static final WebXmlTag SECURITY_ROLE =
        new WebXmlTag("security-role");

    /**
     * Element name 'role-name'.
     */
    public static final WebXmlTag ROLE_NAME =
        new WebXmlTag("role-name");

    /**
     * Element name 'env-entry'.
     */
    public static final WebXmlTag ENV_ENTRY =
        new WebXmlTag("env-entry");

    /**
     * Element name 'ejb-ref'.
     */
    public static final WebXmlTag EJB_REF =
        new WebXmlTag("ejb-ref");

    /**
     * Element name 'ejb-local-ref'.
     */
    public static final WebXmlTag EJB_LOCAL_REF =
        new WebXmlTag("ejb-local-ref");

    /**
     * Element name 'ejb-ref-name'.
     */
    public static final WebXmlTag EJB_REF_NAME =
        new WebXmlTag("ejb-ref-name");

    /**
     * Element name 'ejb-ref-type'.
     */
    public static final WebXmlTag EJB_REF_TYPE =
        new WebXmlTag("ejb-ref-type");

    /**
     * Element name 'local'.
     */
    public static final WebXmlTag LOCAL =
        new WebXmlTag("local");

    /**
     * Element name 'local-home'.
     */
    public static final WebXmlTag LOCAL_HOME =
        new WebXmlTag("local-home");

    /**
     * Element name 'remote'.
     */
    public static final WebXmlTag REMOTE =
        new WebXmlTag("remote");

    /**
     * Element name 'home'.
     */
    public static final WebXmlTag HOME =
        new WebXmlTag("home");

    /**
     * Element name 'local-home'.
     */
    public static final WebXmlTag EJB_LINK =
        new WebXmlTag("ejb-link");

    /**
     * Constructor.
     *
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    protected WebXmlTag(String tagName, boolean isMultipleAllowed)
    {
        super(tagName, isMultipleAllowed);
    }

    /**
     * Constructor.
     *
     * @param tagName The tag name of the element
     */
    protected WebXmlTag(String tagName)
    {
        this(tagName, true);
    }
}
