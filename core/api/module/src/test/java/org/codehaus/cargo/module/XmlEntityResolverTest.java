package org.codehaus.cargo.module;

import junit.framework.TestCase;

public class XmlEntityResolverTest extends TestCase
{
    public void testGetKnownFileName() throws Exception
    {
        String pId = "-//ORACLE//DTD OC4J Web Application 9.04//EN";
        String sId = "http://xmlns.oracle.com/ias/dtds/orion-web-9_04.dtd";
        XmlEntityResolver resolver = new XmlEntityResolver();
        String file = resolver.getDtdFileName(pId, sId);
        assertEquals(file, "orion-web-9_04.dtd");
    }
    
    public void testGetUnknownFileName() throws Exception
    {
        String pId = "-//BEA Systems, Inc.//DTD Web Application 6.1//EN";
        String sId = "http://www.bea.com/servers/wls610/dtd/weblogic610-web-jar.dtd";
        XmlEntityResolver resolver = new XmlEntityResolver();
        String file = resolver.getDtdFileName(pId, sId);
        assertEquals(file, "weblogic610-web-jar.dtd");
    }
}
