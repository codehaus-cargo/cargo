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
package org.codehaus.cargo.module;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * Helper {@link TestCase} that provides a {@link TestCase#setUp} method that creates a 
 * {@link javax.xml.parsers.DocumentBuilder}.
 * 
 * @version $Id: IbmWebBndXmiTest.java 314 2005-04-08 08:34:02Z vmassol $
 */
public abstract class AbstractDocumentBuilderTest extends TestCase
{
    /**
     * The document builder factory.
     */
    protected DocumentBuilderFactory factory;

    /**
     * The JAXP document builder.
     */
    protected SAXBuilder builder;

    protected EntityResolver getEntityResolver()
    {
      return new EntityResolver()
      {
          public InputSource resolveEntity(String thePublicId, 
              String theSystemId) throws SAXException
          {
              return new InputSource(new StringReader(""));
          }
      };
    }
    /**
     * @see TestCase#setUp
     */
    protected void setUp() throws ParserConfigurationException
    {
        this.factory = DocumentBuilderFactory.newInstance();
        this.factory.setValidating(false);
        this.factory.setNamespaceAware(false);

       // this.builder = this.factory.newDocumentBuilder();
        
        this.builder = new SAXBuilder();
        
        this.builder.setEntityResolver(new EntityResolver()
        {
            public InputSource resolveEntity(String thePublicId, 
                String theSystemId) throws SAXException
            {
                return new InputSource(new StringReader(""));
            }
        });
    }
}
