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
package org.codehaus.cargo.module.ejb.weblogic;

import org.codehaus.cargo.module.DescriptorTag;

/**
 * Represents the various tags in a weblogic ejb jar deployment descriptor.
 * 
 * @version $Id$
 */
public class WeblogicEjbJarXmlTag extends DescriptorTag
{
    /**
     * Element name 'ejb-name'.
     */
    public static final WeblogicEjbJarXmlTag EJB_NAME = new WeblogicEjbJarXmlTag("ejb-name");

    /**
     * Element name 'local-jndi-name'.
     */
    public static final WeblogicEjbJarXmlTag LOCAL_JNDI_NAME =
        new WeblogicEjbJarXmlTag("local-jndi-name");
    
    /**
     * Element name 'jndi-name'.
     */
    public static final WeblogicEjbJarXmlTag JNDI_NAME = new WeblogicEjbJarXmlTag("jndi-name");
    
    /**
     * Element name 'dispatch-policy'.
     */
    public static final WeblogicEjbJarXmlTag DISPATCH_POLICY = 
        new WeblogicEjbJarXmlTag("dispatch-policy");
    
    /**
     * Constructor.
     * 
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    protected WeblogicEjbJarXmlTag(String tagName, boolean isMultipleAllowed)
    {
        super(tagName, isMultipleAllowed);
    }
    
    /**
     * Constructor.
     * 
     * @param tagName The tag name of the element
     */
    protected WeblogicEjbJarXmlTag(String tagName)
    {
        this(tagName, true);
    }
}
