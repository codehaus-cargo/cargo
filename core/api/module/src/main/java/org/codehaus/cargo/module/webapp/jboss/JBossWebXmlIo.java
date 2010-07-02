/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.cargo.module.AbstractDescriptorIo;
import org.codehaus.cargo.module.DescriptorType;
import org.jdom.JDOMException;

/**
 * Provides convenience methods for reading JBoss web context descriptor.
 * 
 * @version $Id$
 */
public class JBossWebXmlIo extends AbstractDescriptorIo
{
    /**
     * Constructor.
     * 
     * @param factory Descriptor Type
     */
    protected JBossWebXmlIo(DescriptorType factory)
    {
        super(factory);    
    }

    /**
     * Parses a deployment descriptor provided as input stream.
     * 
     * @param theInput The input stream
     * @return The parsed descriptor
     * @throws IOException If an I/O error occurs
     * @throws JDOMException If the XML parser was not correctly
     *          configured
     */
    public static JBossWebXml parseJBossWebXml(InputStream theInput)
        throws IOException, JDOMException
    {      
        return (JBossWebXml) JBossWebXmlType.getInstance().
              getDescriptorIo().parseXml(theInput);              
    }

}
