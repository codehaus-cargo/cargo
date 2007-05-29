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
package org.codehaus.cargo.module.webapp.jboss;

import org.codehaus.cargo.module.AbstractDescriptorType;
import org.codehaus.cargo.module.Dtd;

/**
 * @version $Id: $
 *
 */
public class JBossWebXmlType extends AbstractDescriptorType
{
    /**
     * Static instance.
     */
    private static JBossWebXmlType instance = new JBossWebXmlType();
  
    
    /**
     * All the tags in this type.
     */
    private JBossWebXmlTag[] tags = 
        new JBossWebXmlTag[] {  
            new JBossWebXmlTag(this, JBossWebXmlTag.CONTEXT_ROOT, false),
            new JBossWebXmlTag(this, JBossWebXmlTag.EJB_LOCAL_REF),
            new JBossWebXmlTag(this, JBossWebXmlTag.EJB_REF),
            new JBossWebXmlTag(this, JBossWebXmlTag.EJB_REF_NAME),
            new JBossWebXmlTag(this, JBossWebXmlTag.JNDI_NAME),
            new JBossWebXmlTag(this, JBossWebXmlTag.LOCAL_JNDI_NAME)
        };
  
   /**
    * Protected Constructor.
    */
    protected JBossWebXmlType()
    {
        super(null, JBossWebXml.class, new Dtd("http://www.jboss.org/j2ee/dtd/jboss-web.dtd"));  
        setDescriptorIo(new JBossWebXmlIo(this));
    }

  /**
   * Get the static instance.
   * @return The instance
   */
    public static JBossWebXmlType getInstance()
    {
        return instance;
    }
}
