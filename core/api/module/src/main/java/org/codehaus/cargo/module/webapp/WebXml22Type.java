/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
import org.codehaus.cargo.module.webapp.elements.Listener;
import org.codehaus.cargo.module.webapp.elements.MimeMapping;
import org.codehaus.cargo.module.webapp.elements.SecurityConstraint;
import org.codehaus.cargo.module.webapp.elements.Servlet;

/**
 * Web 2.2 Descriptor.
 */
public class WebXml22Type extends WebXmlType
{

    /**
     * Single instance.
     */
    private static WebXml22Type instance = new WebXml22Type();

    /**
     * Ensure all tags constructed.<br>
     * The warning <i>value of the field is not used</i> is irrelevant: the
     * <code>DescriptorTag</code> constructor performs the registrations.
     */
    private WebXmlTag[] tags =
        new WebXmlTag[] {
            new WebXmlTag(this, "icon", false),
            new WebXmlTag(this, "display-name", false),
            new WebXmlTag(this, "description", false),
            new WebXmlTag(this, "distributable", false),
            new WebXmlTag(this, "context-param", true, new Identifier("param-name"),
                ContextParam.class),
            new WebXmlTag(this, "param-name"),
            new WebXmlTag(this, "param-value"),
            new WebXmlTag(this, "filter", true, new Identifier("filter-name"), Filter.class),
            new WebXmlTag(this, "filter-name"),
            new WebXmlTag(this, "filter-class"),
            new WebXmlTag(this, "filter-mapping", true, new Identifier("filter-name"),
                FilterMapping.class),
            new WebXmlTag(this, "init-param", true, new Identifier("param-name"), InitParam.class),
            new WebXmlTag(this, "listener", true, new Identifier("listener-class"), Listener.class),
            new WebXmlTag(this, "listener-class"),
            new WebXmlTag(this, "servlet", true, new Identifier("servlet-name"), Servlet.class),
            new WebXmlTag(this, "servlet-name"),
            new WebXmlTag(this, "jsp-file"),
            new WebXmlTag(this, "servlet-class"),
            new WebXmlTag(this, "load-on-startup"),
            new WebXmlTag(this, "run-as"),
            new WebXmlTag(this, "servlet-mapping"),
            new WebXmlTag(this, "url-pattern"),
            new WebXmlTag(this, "session-config", false),
            new WebXmlTag(this, "mime-mapping", false, null, MimeMapping.class),
            new WebXmlTag(this, "welcome-file-list", false),
            new WebXmlTag(this, "error-page", true, new Identifier(
                "concat(error-code,'>',exception-type)"), null), new WebXmlTag(this, "taglib"),
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
     * Protected Constructor.
     */
    protected WebXml22Type()
    {
        super(null, new Dtd("http://java.sun.com/j2ee/dtds/web-app_2_2.dtd"));
        setDescriptorIo(new WebXmlIo(this));
    }

    /**
     * Protected constructor.
     * 
     * @param parent Parent type if any
     * @param grammar grammar to use
     */
    protected WebXml22Type(AbstractDescriptorType parent, Dtd grammar)
    {
        super(parent, grammar);
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

    /**
     * Get the web xml version for his type.
     * @return the version for this type
     */
    @Override
    public WebXmlVersion getVersion()
    {
        return WebXmlVersion.V2_2;
    }
}
