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
package org.codehaus.cargo.module.ejb.websphere;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.cargo.module.AbstractDescriptorIo;
import org.jdom.JDOMException;

/**
 * Provides convenience methods for reading Webspeher ejb jar deployment descriptor.
 * 
 * @version $Id$
 */
public class IbmEjbJarBndXmiIo extends AbstractDescriptorIo
{
    /**
     * Constructor.
     */
    protected IbmEjbJarBndXmiIo()
    {
        super(IbmEjbJarBndXmiType.getInstance());    
    }
    /**
     * Parses a deployment descriptor provided as input stream.
     * 
     * @param input The input stream
     * @return The parsed descriptor
     * @throws JDOMException If the input could not be parsed     
     * @throws IOException If an I/O error occurs
     */
    public static IbmEjbJarBndXmi parseIbmEjbJarXmi(InputStream input)
        throws IOException, JDOMException
    {
        IbmEjbJarBndXmiIo xio = new IbmEjbJarBndXmiIo();
        return (IbmEjbJarBndXmi) xio.parseXml(input);
    }

}
