package org.codehaus.cargo.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import junit.framework.TestCase;

public class Dom4JUtilTest extends TestCase
{
    private Dom4JUtil util;

    private Element testElement;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        util = new Dom4JUtil();
        Document document = DocumentHelper.createDocument();
        this.testElement = document.addElement("test");
        document.setRootElement(testElement);
    }

    public void testUnparsableElementThrowsException()
    {
        String string = "asdasd";
        try
        {
            util.parseIntoElement(string);
        }
        catch (CargoException e)
        {
            assertEquals("Could not parse element: " + string, e.getMessage());
        }
    }

    public void testParsableElement() throws DocumentException
    {
        String string = "<element>dog</element>";
        assertEquals(string, util.parseIntoElement(string).asXML());
    }

    public void testNoElementThrowsException()
    {
        try
        {
            util.selectElementMatchingXPath("weblogic:app-deployment", testElement);

            fail("should have returned an exception");
        }
        catch (ElementNotFoundException e)
        {
            // this exception is good!
            assertEquals(testElement, e.getSearched());
            return;
        }
    }

}
