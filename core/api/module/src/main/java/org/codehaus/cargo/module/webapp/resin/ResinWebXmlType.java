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
package org.codehaus.cargo.module.webapp.resin;

import org.codehaus.cargo.module.AbstractDescriptorType;

/**
 * @version $Id $
 *
 */
public class ResinWebXmlType extends AbstractDescriptorType
{
    /**
     * Static instance.
     */
    private static ResinWebXmlType instance = new ResinWebXmlType();
      
    /**
     * List of tags in this descriptor.
     */
    private ResinWebXmlTag[] tags = 
        new ResinWebXmlTag[] {  
            new ResinWebXmlTag(this, "system-property", true),
            new ResinWebXmlTag(this, "resource-ref", true),
            new ResinWebXmlTag(this, "session-config", true),
            new ResinWebXmlTag(this, "directory-servlet", true),
            new ResinWebXmlTag(this, "jndi-link", true)        
        };
    
  /**
   * Protected Constructor.
   */
    protected ResinWebXmlType()
    {
        super(null, ResinWebXml.class, null);   
    }
  
  /**
   * Get the static instance.
   * @return The instance
   */
    public static ResinWebXmlType getInstance()
    {
        return instance;
    }
}