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
package org.codehaus.cargo.module.application;

import org.codehaus.cargo.module.DescriptorTag;

/**
 * Represents the various top-level tags in a enterprise application deployment descriptor as a 
 * typesafe enumeration.
 * 
 * @version $Id$
 */
public final class ApplicationXmlTag extends DescriptorTag
{
    /**
     * Element name 'icon'.
     */
    public static final ApplicationXmlTag ICON = new ApplicationXmlTag("icon");
    
    /**
     * Element name 'display-name'.
     */
    public static final ApplicationXmlTag DISPLAY_NAME = new ApplicationXmlTag("display-name");
    
    /**
     * Element name 'description'.
     */
    public static final ApplicationXmlTag DESCRIPTION = new ApplicationXmlTag("description");
    
    /**
     * Element name 'module'.
     */
    public static final ApplicationXmlTag MODULE = new ApplicationXmlTag("module");
    
    /**
     * Element name 'ejb'.
     */
    public static final ApplicationXmlTag EJB = new ApplicationXmlTag("ejb");
    
    /**
     * Element name 'web'.
     */
    public static final ApplicationXmlTag WEB = new ApplicationXmlTag("web");
    
    /**
     * Element name 'web-uri'.
     */
    public static final ApplicationXmlTag WEB_URI = new ApplicationXmlTag("web-uri");
    
    /**
     * Element name 'context-root'.
     */
    public static final ApplicationXmlTag CONTEXT_ROOT = new ApplicationXmlTag("context-root");
   
    /**
     * Element name 'security-role'.
     */
    public static final ApplicationXmlTag SECURITY_ROLE = new ApplicationXmlTag("security-role");
    
    /**
     * Constructor.
     * 
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    protected ApplicationXmlTag(String tagName, boolean isMultipleAllowed)
    {
        super(tagName, isMultipleAllowed);
    }
    
    /**
     * Constructor.
     * 
     * @param tagName The tag name of the element
     */
    protected ApplicationXmlTag(String tagName)
    {
        this(tagName, true);
    }    
}
