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

import java.io.ByteArrayInputStream;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.ejb.EjbDef;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link WeblogicEjbJarXml}.
 *
 * @version $Id$
 */
public class WeblogicEjbJarXmlTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests the basic functionality of {@link WeblogicEjbJarXml.getLocalJndiName}.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetLocalJndiName() throws Exception
    {
        String xml = "<weblogic-ejb-jar>"
            + "  <weblogic-enterprise-bean>"
            + "    <ejb-name>MyEjb</ejb-name>"
            + "    <local-jndi-name>mycomp/MyEjb</local-jndi-name>"        
            + "  </weblogic-enterprise-bean>"
            + "</weblogic-ejb-jar>";
            
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WeblogicEjbJarXml descr = new WeblogicEjbJarXml(doc);
        assertEquals("mycomp/MyEjb", descr.getJndiName(new EjbDef("MyEjb")));
    }
    
    /**
     * Tests {@link WeblogicEjbJarXml.getLocalJndiName} when no local jndi name is
     * specified.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetJndiName() throws Exception
    {
        String xml = "<weblogic-ejb-jar>"
            + "  <weblogic-enterprise-bean>"
            + "    <ejb-name>MyEjb</ejb-name>"
            + "    <jndi-name>mycomp/MyEjb</jndi-name>"        
            + "  </weblogic-enterprise-bean>"
            + "</weblogic-ejb-jar>";
            
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WeblogicEjbJarXml descr = new WeblogicEjbJarXml(doc);
        assertEquals("mycomp/MyEjb", descr.getJndiName(new EjbDef("MyEjb")));
    }
    
    /**
     * Tests {@link WeblogicEjbJarXml.getLocalJndiName} with wrong ejb name
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetLocalJndiNameWithWrongEjbName() throws Exception
    {
        String xml = "<weblogic-ejb-jar>"
            + "  <weblogic-enterprise-bean>"
            + "    <ejb-name>MyEjb</ejb-name>"
            + "    <jndi-name>mycomp/MyEjb</jndi-name>"        
            + "  </weblogic-enterprise-bean>"
            + "</weblogic-ejb-jar>";
            
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WeblogicEjbJarXml descr = new WeblogicEjbJarXml(doc);
        assertNull(descr.getJndiName(new EjbDef("MyEjd")));
    }
    
    /**
     * Tests {@link WeblogicEjbJarXml.getDispatchPolicy}
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetDispatchPolicy() throws Exception
    {
        String xml = "<weblogic-ejb-jar>"
            + "  <weblogic-enterprise-bean>"
            + "    <ejb-name>MyEjb</ejb-name>"
            + "    <jndi-name>mycomp/MyEjb</jndi-name>"
            + "    <dispatch-policy>threadQueue</dispatch-policy>"
            + "  </weblogic-enterprise-bean>"
            + "</weblogic-ejb-jar>";
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WeblogicEjbJarXml descr = new WeblogicEjbJarXml(doc);
        assertEquals("threadQueue", descr.getDispatchPolicy(new EjbDef("MyEjb")));
    }
    
    /**
     * Tests {@link WeblogicEjbJarXml.addDispatchPolicy}
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddDispatchPolicy() throws Exception
    {
        String xml = "<weblogic-ejb-jar>"
            + "  <weblogic-enterprise-bean>"
            + "    <ejb-name>MyEjb</ejb-name>"
            + "    <jndi-name>mycomp/MyEjb</jndi-name>"        
            + "  </weblogic-enterprise-bean>"
            + "</weblogic-ejb-jar>";
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WeblogicEjbJarXml descr = new WeblogicEjbJarXml(doc);
        descr.addDispatchPolicy(new EjbDef("MyEjb"), "threadQueue");
        assertEquals("threadQueue", descr.getDispatchPolicy(new EjbDef("MyEjb")));
    }
}
