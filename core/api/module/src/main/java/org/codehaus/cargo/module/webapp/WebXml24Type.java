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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.module.Dtd;
import org.codehaus.cargo.module.Identifier;
import org.codehaus.cargo.module.webapp.elements.AuthConstraint;
import org.codehaus.cargo.module.webapp.elements.ContextParam;
import org.codehaus.cargo.module.webapp.elements.Filter;
import org.codehaus.cargo.module.webapp.elements.FilterMapping;
import org.codehaus.cargo.module.webapp.elements.InitParam;
import org.codehaus.cargo.module.webapp.elements.Listener;
import org.codehaus.cargo.module.webapp.elements.SecurityConstraint;
import org.codehaus.cargo.module.webapp.elements.Servlet;
import org.jdom.JDOMException;

/**
 * Web 2.4 Descriptor.
 * @version $Id: $
 */
public class WebXml24Type extends WebXmlType
{

   /**
    * Ensure all tags constructed.
    */
   private WebXmlTag[] tags;
   
    /**
     * Single instance.
     */
    private static WebXml24Type instance = new WebXml24Type();
    
    /**
     * Protected constructor.     
     * @throws  
     */
    protected WebXml24Type()
    {
        super(null, new Dtd("http://java.sun.com/dtd/web-app_2_3.dtd"));
        
        Map namespaceMap = new HashMap();
        namespaceMap.put("j2ee", this.getVersion().getNamespace().getURI());
        
        this.tags = new WebXmlTag[] {
      
                new WebXmlTag(this, "icon", false),
                new WebXmlTag(this, "display-name", false),
                new WebXmlTag(this, "description", false),
                new WebXmlTag(this, "distributable", false),
                new WebXmlTag(this,
                    "context-param",
                    true,
                    new Identifier(namespaceMap, "j2ee:param-name"),
                    ContextParam.class),
                new WebXmlTag(this, "param-name"),
                new WebXmlTag(this, "param-value"),
                new WebXmlTag(this, "filter", true, new Identifier(namespaceMap, "j2ee:filter-name"), Filter.class),
                new WebXmlTag(this, "filter-name"),
                new WebXmlTag(this, "filter-class"),
                new WebXmlTag(this,
                    "filter-mapping",
                    true,
                    new Identifier(namespaceMap, "j2ee:filter-name"),
                    FilterMapping.class),
                new WebXmlTag(this, "init-param", true, new Identifier(namespaceMap, "j2ee:param-name"), InitParam.class),
                new WebXmlTag(this, "listener", true, new Identifier(namespaceMap, "j2ee:listener-class"), Listener.class),
                new WebXmlTag(this, "servlet", true, new Identifier(namespaceMap, "j2ee:servlet-name"), Servlet.class),
                new WebXmlTag(this, "servlet-name"), new WebXmlTag(this, "jsp-file"),
                new WebXmlTag(this, "servlet-class"), new WebXmlTag(this, "load-on-startup"),
                new WebXmlTag(this, "run-as"), 
                new WebXmlTag(this, "servlet-mapping"),
                new WebXmlTag(this, "url-pattern"), new WebXmlTag(this, "session-config", false),
                new WebXmlTag(this, "mime-mapping"), new WebXmlTag(this, "welcome-file-list", false),
                new WebXmlTag(this, "error-page", true, new Identifier("concat(error-code,'>',exception-type)"), null), new WebXmlTag(this, "taglib"),
                new WebXmlTag(this, "resource-env-ref"), new WebXmlTag(this, "resource-ref"),
                new WebXmlTag(this, "security-constraint", true, null, SecurityConstraint.class),
                new WebXmlTag(this, "web-resource-collection"), 
                new WebXmlTag(this, "web-resource-name"),
                new WebXmlTag(this, "auth-constraint", false, null, AuthConstraint.class),
                new WebXmlTag(this, "login-config", false), new WebXmlTag(this, "auth-method"),
                new WebXmlTag(this, "realm-name"),
                new WebXmlTag(this, "security-role", true, new Identifier(namespaceMap, "j2ee:role-name"), null),
                new WebXmlTag(this, "role-name"), new WebXmlTag(this, "env-entry"),
                new WebXmlTag(this, "ejb-ref", true, new Identifier(namespaceMap, "j2ee:ejb-ref-name"), null),
                new WebXmlTag(this, "ejb-local-ref"), new WebXmlTag(this, "ejb-ref-name"),
                new WebXmlTag(this, "ejb-ref-type"), new WebXmlTag(this, "local"),
                new WebXmlTag(this, "local-home"), new WebXmlTag(this, "remote"),
                new WebXmlTag(this, "home"), new WebXmlTag(this, "ejb-link") };
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
     * {@inheritDoc}
     */
    public WebXmlVersion getVersion()
    {
        return WebXmlVersion.V2_4;
    }
}
