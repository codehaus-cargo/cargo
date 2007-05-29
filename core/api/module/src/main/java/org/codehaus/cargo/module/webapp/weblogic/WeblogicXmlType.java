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

import org.codehaus.cargo.module.AbstractDescriptorType;
import org.codehaus.cargo.module.Dtd;

/**
 * @version $Id: $ 
 */
public class WeblogicXmlType extends AbstractDescriptorType
{
    /**
     * Static instance.
     */
    private static WeblogicXmlType instance = new WeblogicXmlType();
      
    /**
     * Tags for this document type.
     */
    private WeblogicXmlTag[] tags = new WeblogicXmlTag[] {  
        new WeblogicXmlTag(this, "description"),
        new WeblogicXmlTag(this, "weblogic-version"),
        new WeblogicXmlTag(this, "security-role-assignment"),
        new WeblogicXmlTag(this, "run-as-role-assignment"),
        new WeblogicXmlTag(this, "reference-descriptor"),
        new WeblogicXmlTag(this, "session-descriptor"),
        new WeblogicXmlTag(this, "jsp-descriptor"),
        new WeblogicXmlTag(this, "auth-filter"),
        new WeblogicXmlTag(this, "container-descriptor"),
        new WeblogicXmlTag(this, "charset-params"),
        new WeblogicXmlTag(this, "virtual-directory-mapping"),
        new WeblogicXmlTag(this, "url-match-map"),
        new WeblogicXmlTag(this, "preprocessor"),
        new WeblogicXmlTag(this, "preprocessor-mapping"),
        new WeblogicXmlTag(this, "security-permission"),
        new WeblogicXmlTag(this, "context-root"),
        new WeblogicXmlTag(this, "wl-dispatch-policy"),
        new WeblogicXmlTag(this, "servlet-descriptor"),
        new WeblogicXmlTag(this, "init-as"),
        new WeblogicXmlTag(this, "destroy-as"),
        new WeblogicXmlTag(this, "resource-description"),
        new WeblogicXmlTag(this, "resource-env-description"),
        new WeblogicXmlTag(this, "ejb-reference-description"),
        new WeblogicXmlTag(this, "ejb-ref-name"),
        new WeblogicXmlTag(this, "jndi-name")            
    };
    
    /**
     * Constructor.
     */
    protected WeblogicXmlType()
    {
      super(null, WeblogicXml.class, new Dtd(
          "http://www.bea.com/servers/wls810/dtd/weblogic810-web-jar.dtd"));   
    }
    
    /**
     * Get the static instance.
     * @return The instance
     */
    public static WeblogicXmlType getInstance()
    {
        return instance;
    }
}