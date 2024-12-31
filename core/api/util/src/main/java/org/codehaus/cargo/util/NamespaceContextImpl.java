/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Context map for XPath. Used for mapping between prefixes and XML namespaces.
 */
public class NamespaceContextImpl implements NamespaceContext
{

    /**
     * Map of prefixes with XML namespaces.
     */
    private Map<String, String> namespaces;

    /**
     * Constructor.
     */
    public NamespaceContextImpl()
    {
        namespaces = new HashMap<String, String>();
    }

    /**
     * @return Map of prefixes with XML namespaces.
     */
    public Map<String, String> getNamespaces()
    {
        return namespaces;
    }

    /**
     * @param namespaces Map of prefixes with XML namespaces.
     */
    public void setNamespaces(Map<String, String> namespaces)
    {
        this.namespaces = namespaces;
    }

    @Override
    public String getNamespaceURI(String prefix)
    {
        if (namespaces.containsKey(prefix))
        {
            return namespaces.get(prefix);
        }
        else
        {
            return XMLConstants.NULL_NS_URI;
        }
    }

    @Override
    public String getPrefix(String namespaceURI)
    {
        for (Map.Entry<String, String> entry : namespaces.entrySet())
        {
            if (namespaceURI.equals(entry.getValue()))
            {
                return entry.getKey();
            }
        }
        return XMLConstants.DEFAULT_NS_PREFIX;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI)
    {
        throw new UnsupportedOperationException();
    }
}
