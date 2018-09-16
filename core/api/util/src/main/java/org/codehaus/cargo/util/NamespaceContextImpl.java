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
