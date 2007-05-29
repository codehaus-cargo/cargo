/* 
 * ========================================================================
 * 
 * Copyright 2005-2007 Vincent Massol.
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
package org.codehaus.cargo.module.ejb.jboss;

import org.codehaus.cargo.module.AbstractDescriptorType;
import org.codehaus.cargo.module.Dtd;

/**
 * @version $Id: $
 */
public class JBossXmlType extends AbstractDescriptorType
{
	/**
     * Static instance.
     */
    private static JBossXmlType instance = new JBossXmlType();
    
    /**
     * All the tags in this type.
     */
    private JBossXmlTag[] tags = new JBossXmlTag[] {
        new JBossXmlTag(this, JBossXmlTag.EJB_NAME),
        new JBossXmlTag(this, JBossXmlTag.JNDI_NAME),
        new JBossXmlTag(this, JBossXmlTag.LOCAL_JNDI_NAME)                     
    };
    
    /**
     * Protected constructor.
     * 
     */
    protected JBossXmlType()
    {
        super(null, JBossXml.class, 
            new Dtd("http://www.jboss.org/j2ee/dtd/jboss_4_0.dtd"));   
        setDescriptorIo(new JBossXmlIo());
    }
    
    /**
     * Get the static instance.
     * @return The instance
     */
    public static JBossXmlType getInstance()
    {
        return instance;
    }
}
