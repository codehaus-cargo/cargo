/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;

/**
 * Contains methods for getting information from a dtd.
 * 
 * @version $Id$
 */
public class Dtd implements Grammar
{
    /**
     * Map containing all possible tag name as keys and the tags that they can contain as a List of
     * {@link DescriptorTag}s. The list is ordered in the order that the tag can appear accordingly
     * to the DTD.
     */
    private Map<String, List<DescriptorTag>> elementOrders;

    /**
     * Implementation of the SAX DeclHandler interface for parsing the DTD.
     */
    private static class DtdHandler implements DeclHandler
    {
        /**
         * Map containing all possible tag name as keys and the tags that they can contain as a List
         * of {@link DescriptorTag}s. The list is ordered in the order that the tag can appear
         * accordingly to the DTD.
         */
        private Map<String, List<DescriptorTag>> elementOrders =
            new HashMap<String, List<DescriptorTag>>();

        /**
         * {@inheritDoc}
         * @see org.xml.sax.ext.DeclHandler#attributeDecl(java.lang.String, java.lang.String,
         * java.lang.String, java.lang.String, java.lang.String)
         */
        public void attributeDecl(String eName, String aName, String type, String mode,
            String value)
        {
        }

        /**
         * {@inheritDoc}
         * @see org.xml.sax.ext.DeclHandler#elementDecl(java.lang.String, java.lang.String)
         */
        public void elementDecl(String name, String model)
        {
            List<DescriptorTag> elements = new ArrayList<DescriptorTag>();
            if (!model.equals("EMPTY")
                && !model.equals("(#PCDATA)"))
            {
                StringTokenizer st = new StringTokenizer(model, ",()| ");
                while (st.hasMoreTokens())
                {
                    boolean multipleAllowed = false;
                    String element = st.nextToken();
                    if (element.endsWith("*")
                        || element.endsWith("+"))
                    {
                        element = element.substring(0, element.length() - 1);
                        multipleAllowed = true;
                    }
                    if (element.endsWith("?"))
                    {
                        element = element.substring(0, element.length() - 1);
                    }
                    DescriptorTag tag = new DescriptorTag(null, element, multipleAllowed);
                    elements.add(tag);
                }
            }
            this.elementOrders.put(name, elements);
        }

        /**
         * {@inheritDoc}
         * @see org.xml.sax.ext.DeclHandler#externalEntityDecl(java.lang.String, java.lang.String,
         * java.lang.String)
         */
        public void externalEntityDecl(String name, String publicId, String systemId)
        {
        }

        /**
         * {@inheritDoc}
         * @see org.xml.sax.ext.DeclHandler#internalEntityDecl(java.lang.String, java.lang.String)
         */
        public void internalEntityDecl(String name, String value)
        {
        }

        /**
         * @return Returns the elementOrders.
         */
        public Map<String, List<DescriptorTag>> getElementOrders()
        {
            return this.elementOrders;
        }
    }

    /**
     * Contructor.
     * 
     * @param systemId system id of the dtd to parse
     */
    public Dtd(String systemId)
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setEntityResolver(new XmlEntityResolver());
            DtdHandler dtdHandler = new DtdHandler();
            try
            {
                reader.setProperty("http://xml.org/sax/properties/declaration-handler", dtdHandler);
            }
            catch (SAXNotRecognizedException e)
            {
                throw new SAXException(e);
            }

            String xml = "<!DOCTYPE dummy SYSTEM \"" + systemId + "\"><dummy/>";
            reader.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))));
            this.elementOrders = dtdHandler.getElementOrders();
        }
        catch (IOException e)
        {
            throw new DtdParseException("Failed to read dtd", e);
        }
        catch (SAXException e)
        {
            throw new DtdParseException("Failed to parse dtd", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new DtdParseException("Failed to parse dtd", e);
        }
    }

    /**
     * {@inheritDoc}
     * @see Grammar#getElementOrder(String)
     */
    public List<DescriptorTag> getElementOrder(String tagName)
    {
        return this.elementOrders.get(tagName);
    }
}
