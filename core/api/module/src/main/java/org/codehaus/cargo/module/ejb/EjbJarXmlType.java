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
package org.codehaus.cargo.module.ejb;

import org.codehaus.cargo.module.AbstractDescriptorType;
import org.codehaus.cargo.module.Dtd;

/**
 * @version $Id: $
 */
public class EjbJarXmlType extends AbstractDescriptorType
{
    /**
     * Static instance.
     */
    private static EjbJarXmlType instance = new EjbJarXmlType();
  
    /**
     * All the tags in this type.
     */
    private EjbJarXmlTag[] tags = new EjbJarXmlTag[] {
        new EjbJarXmlTag(this, "session"),        
        new EjbJarXmlTag(this, "entity"),
        new EjbJarXmlTag(this, "ejb-name"),
        new EjbJarXmlTag(this, "local"),
        new EjbJarXmlTag(this, "local-home")        
    };
    
    /**
     * Protected constructor.
     */
    protected EjbJarXmlType()
    {
        super(null, EjbJarXml.class, new Dtd("http://java.sun.com/dtd/ejb-jar_2_0.dtd"));   
    }
    /**
     * Get the static instance.
     * @return The instance
     */
    public static EjbJarXmlType getInstance()
    {
        return instance;
    }
  
    
    
    

}
