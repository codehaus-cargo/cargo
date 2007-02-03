/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.module;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of the SAX EntityResolver interface that looks up the web-app
 * DTDs from the JAR.
 * 
 * @version $Id: Dtd.java 359 2005-05-10 07:39:02Z vmassol $
 */
public class XmlEntityResolver implements EntityResolver
{
    /**
     * Properties containing mappings of public identifiers to system identifiers.
     */
    private static Map publicIdentifiers;

    static
    {
        publicIdentifiers = new HashMap();
        // application.xml
        publicIdentifiers.put("-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN",
                              "application_1_2.dtd");
        publicIdentifiers.put("-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN",
                              "application_1_3.dtd");
        // ejb-jar.xml
        publicIdentifiers.put("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN",
                              "ejb-jar_2_0.dtd");
        // web.xml
        publicIdentifiers.put("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
                              "web-app_2_2.dtd");
        publicIdentifiers.put("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN",
                              "web-app_2_3.dtd");
        
        // orion
        publicIdentifiers.put("-//ORACLE//DTD OC4J Enterprise JavaBeans runtime 9.04//EN", 
                              "orion-ejb-jar-9_04.dtd");
        publicIdentifiers.put("-//ORACLE//DTD OC4J Web Application 9.04//EN", 
                              "orion-web-9_04.dtd");
        
        // weblogic
        publicIdentifiers.put("-//BEA Systems, Inc.//DTD WebLogic 8.1.0 EJB//EN",
                              "weblogic-ejb-jar.dtd");
        publicIdentifiers.put("-//BEA Systems, Inc.//DTD Web Application 8.1//EN", 
                              "weblogic810-web-jar.dtd");
    }

    /**
     * {@inheritDoc}
     * @see org.xml.sax.EntityResolver#resolveEntity
     */
    public InputSource resolveEntity(String thePublicId, String theSystemId)
        throws SAXException, IOException
    {
        InputSource inSource = null;
        String fileName = getDtdFileName(thePublicId, theSystemId);
        
        InputStream in = this.getClass().getResourceAsStream(
            "/org/codehaus/cargo/module/internal/resource/" + fileName);

        if (in == null)
        {
            URL url = new URL(theSystemId);
            in = url.openStream();
        }
        
        if (in != null)
        {
            inSource = new InputSource(in);
            inSource.setPublicId(thePublicId);
            inSource.setSystemId(theSystemId);
        }
        return inSource;
    }
    
    /**
     * Tries to decide the file name of a DTD from the public and system id.
     * 
     * @param thePublicId the publid id
     * @param theSystemId the system id
     * @return the file name
     */
    public String getDtdFileName(String thePublicId, String theSystemId)
    {
        String fileName = null;
        if (thePublicId != null)
        {
            String mappedValue = (String) publicIdentifiers.get(thePublicId);
            if (mappedValue != null)
            {
                fileName = mappedValue;
            }
        }

        if (fileName == null)
        {
            fileName = theSystemId.substring(theSystemId.lastIndexOf('/') + 1);
        }
        
        return fileName;
    }
}
