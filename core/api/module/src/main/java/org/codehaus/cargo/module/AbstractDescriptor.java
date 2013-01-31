/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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

import java.util.ArrayList;
import java.util.List;

import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;

/**
 * Encapsulates the DOM representation of a deployment descriptor to provide convenience methods for
 * easy access and manipulation.
 * 
 * @version $Id$
 */
public abstract class AbstractDescriptor extends Document implements Descriptor
{
    /**
     * Grammar of the descriptor.
     */
    private DescriptorType descriptorType;

    /**
     * Constructor.
     * 
     * @param rootElement The root element of the document
     * @param descriptorType The type of the descriptor
     */
    public AbstractDescriptor(Element rootElement, DescriptorType descriptorType)
    {
        super(rootElement);
        if (descriptorType == null)
        {
            throw new NullPointerException();
        }
        this.descriptorType = descriptorType;
    }

    /**
     * Return the representation as a document.
     * 
     * @return JDOM Document
     */
    @Override
    public Document getDocument()
    {
        return this;
    }

    /**
     * Get tags of a particular type.
     * 
     * @param tag type of elements to find
     * @return list of tags
     */
    public List<Element> getTags(DescriptorTag tag)
    {
        return getRootElement().getChildren(tag.getTagName(), tag.getTagNamespace());
    }

    /**
     * Get tags of a particular type.
     * 
     * @param tagName type of elements to find
     * @return list of tags
     */
    public List<Element> getTags(String tagName)
    {
        List<Element> elements = new ArrayList<Element>();
        for (Element child : (List<Element>) getRootElement().getChildren())
        {
            if (child.getName().equals(tagName))
            {
                elements.add(child);
            }
        }
        return elements;
    }

    /**
     * Returns a list of the elements that match the specified tag.
     * 
     * @param tag The descriptor tag of which the elements should be returned
     * @return A list of the elements matching the tag, in the order they occur in the descriptor
     */
    public List<Element> getElements(DescriptorTag tag)
    {
        if (tag == null)
        {
            throw new IllegalArgumentException("tag must not be null");
        }

        return getChildElements(getRootElement(), tag, new ArrayList<Element>());
    }

    /**
     * Returns a list of the elements that match the specified tag.
     * 
     * @param tagName The name of a descriptor tag of which the elements should be returned
     * @return A list of the elements matching the tag, in the order they occur in the descriptor
     */
    public List<Element> getElements(String tagName)
    {
        if (tagName == null)
        {
            throw new IllegalArgumentException("tagName must not be null");
        }

        return getElements(getDescriptorType().getTagByName(tagName));
    }

    /**
     * Recursively get elements matching a particular tag.
     * 
     * @param element the parent element
     * @param tag the tag required
     * @param items collection of items found so far
     * @return List of elements
     */
    private List<Element> getChildElements(Element element, DescriptorTag tag, List<Element> items)
    {
        if (element == null || tag == null || items == null)
        {
            throw new IllegalArgumentException("Cannot pass null values to getChildElements");
        }

        items.addAll(element.getChildren(tag.getTagName(), tag.getTagNamespace()));
        List<Element> children = element.getChildren();
        for (Element child : children)
        {
            getChildElements(child, tag, items);
        }
        return items;
    }

    /**
     * Checks an element whether its name matches the specified name.
     * 
     * @param element The element to check
     * @param expectedTag The expected tag name
     * @throws IllegalArgumentException If the element name doesn't match
     */
    protected void checkElement(Element element, DescriptorTag expectedTag)
        throws IllegalArgumentException
    {
        if (!expectedTag.getTagName().equals(element.getName()))
        {
            throw new IllegalArgumentException("Not a [" + expectedTag + "] element");
        }
    }

    /**
     * Returns a list of the child elements of the specified element that match the specified tag.
     * 
     * @param parent The element of which the nested elements should be retrieved
     * @param tag The descriptor tag of which the elements should be returned
     * @return A copy of the list of the elements matching the tag, in the order they occur in the
     * descriptor
     */
    protected List<Element> getNestedElements(Element parent, DescriptorTag tag)
    {
        List<Element> nodeList = parent.getChildren(tag.getTagName(), tag.getTagNamespace());
        return new ArrayList<Element>(nodeList);
    }

    /**
     * Creates an element that contains nested text.
     * 
     * @param tag The tag to create an instance of
     * @param text The text that should be nested in the element
     * @return The created DOM element
     */
    protected Element createNestedText(DescriptorTag tag, String text)
    {
        Element element = new Element(tag.getTagName(), tag.getTagNamespace());
        element.setText(text);

        return element;
    }

    /**
     * Returns the text nested inside a child element of the specified element.
     * 
     * @param parent The element of which the nested text should be returned
     * @param tag The descriptor tag in which the text is nested
     * @return The text nested in the element
     */
    protected String getNestedText(Element parent, DescriptorTag tag)
    {
        String text = null;
        List<Element> nestedElements = parent.getChildren(tag.getTagName(), tag.getTagNamespace());
        if (nestedElements.size() > 0)
        {
            text = getText(nestedElements.get(0));
        }
        return text;
    }

    /**
     * Returns the text value of an element.
     * 
     * @param element the element of wich the text value should be returned
     * @return the text value of an element
     */
    protected String getText(Element element)
    {
        return element.getText();
    }

    /**
     * Gets a certain tag directly under the parent tag.
     * 
     * @param parent the tag to get the cild from
     * @param tag name of the child tag
     * @return tag directly under the parent tag.
     */
    protected Element getImmediateChild(Element parent, DescriptorTag tag)
    {
        Element e = null;
        List<Element> nl = parent.getChildren();
        for (Element n : nl)
        {
            if (n.getName().equals(tag.getTagName()))
            {
                e = n;
            }
        }

        return e;
    }

    /**
     * Returns the text value from a child directly under the parent tag.
     * 
     * @param parent the parent tag to get the child text from
     * @param tag the name of the child tag
     * @return the text value of a child tag directly under the parent tag
     */
    protected String getChildText(Element parent, DescriptorTag tag)
    {
        String text = null;
        Element e = getImmediateChild(parent, tag);
        if (e != null)
        {
            text = getText(e);
        }
        return text;
    }

    /**
     * Returns the text value from a child directly under the parent tag.
     * 
     * @param parent the parent tag to get the child text from
     * @param tagName the name of the child tag
     * @return the text value of a child tag directly under the parent tag
     */
    protected String getChildText(Element parent, String tagName)
    {
        return getChildText(parent, getDescriptorType().getTagByName(tagName));
    }

    /**
     * Adds an element of the specified tag to the descriptor.
     * 
     * @param tag The descriptor tag
     * @param child The child element to add
     * @param parent The parent element to add the child to
     * @return the inserted element
     */
    public Element addElement(DescriptorTag tag, Element child, Element parent)
    {
        Element importedNode = (Element) child.detach();

        Element refNode = getInsertionPointFor(tag, parent.getName());

        int idx = parent.getContent().indexOf(refNode);
        if (idx == -1)
        {
            if (!containsElement(parent.getChildren(), importedNode))
            {
                parent.addContent(importedNode);
            }
        }
        else
        {
            // Navigate backwards if the previous item is a comment
            while (idx > 0 && parent.getContent(idx - 1) instanceof Comment)
            {
                idx--;
            }

            if (!containsElement(parent.getChildren(), importedNode))
            {
                parent.addContent(idx, importedNode);
            }
        }

        return importedNode;
    }

    /**
     * Checks if <code>haystack</code> contains <code>needle</code>.
     * @param haystack List of element to look into.
     * @param needle Element to look for.
     * @return Whether <code>haystack</code> contains <code>needle</code>.
     */
    protected boolean containsElement(List<Element> haystack, Element needle)
    {
        for (Element element : haystack)
        {
            if (sameElement(element, needle))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if two elements are the same.
     * @param element1 First element.
     * @param element2 Second element.
     * @return Whether <code>element1</code> and <code>element2</code> are the same.
     */
    protected boolean sameElement(Element element1, Element element2)
    {
        if (!element1.getName().equals(element2.getName()))
        {
            return false;
        }

        List<Element> children1 = element1.getChildren();
        List<Element> children2 = element2.getChildren();
        if (children1.size() != children2.size())
        {
            return false;
        }
        if (children1.isEmpty())
        {
            return sameContent(element1, element2);
        }

        for (int i = 0; i < children1.size(); i++)
        {
            if (!sameElement(children1.get(i), children2.get(i)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if two elements have the same content (excluding comments).
     * @param element1 First element.
     * @param element2 Second element.
     * @return Whether <code>element1</code> and <code>element2</code> have the same content
     * (excluding comments).
     */
    protected boolean sameContent(Element element1, Element element2)
    {
        List<Content> content1 = getContentExceptComments(element1);
        List<Content> content2 = getContentExceptComments(element2);
        if (content1.size() != content2.size())
        {
            return false;
        }
        for (int i = 0; i < content1.size(); i++)
        {
            String content1Value = content1.get(i).getValue();
            String content2Value = content2.get(i).getValue();
            if (content1Value == null || content2Value == null)
            {
                return content1Value == content2Value;
            }
            if (!content1Value.equals(content2Value))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the content of an element (excluding comments).
     * @param element Element to get the contents for.
     * @return All content for <code>element</code> (excluding comments).
     */
    protected List<Content> getContentExceptComments(Element element)
    {
        List<Content> content = element.getContent();
        List<Content> filteredContent = new ArrayList<Content>();
        for (Content contentToCheck : content)
        {
            if (!(contentToCheck instanceof Comment))
            {
                filteredContent.add(contentToCheck);
            }
        }
        return filteredContent;
    }

    /**
     * Returns the node before which the specified tag should be inserted, or <code>null</code> if
     * the node should be inserted at the end of the descriptor.
     * 
     * @param tag The tag that should be inserted
     * @param parent name of the parent tag
     * @return The node before which the tag can be inserted
     */
    protected Element getInsertionPointFor(DescriptorTag tag, String parent)
    {
        List<DescriptorTag> elementOrder =
            this.getDescriptorType().getGrammar().getElementOrder(parent);
        if (elementOrder == null)
        {
            // No order required, just insert anywhere
            return null;
        }
        for (int i = 0; i < elementOrder.size(); i++)
        {
            DescriptorTag orderTag = elementOrder.get(i);
            if (orderTag.equals(tag))
            {
                for (int j = i + 1; j < elementOrder.size(); j++)
                {
                    DescriptorTag theTag = elementOrder.get(j);
                    List<Element> elements =
                        getRootElement().getChildren(
                            theTag.getTagName(), theTag.getTagNamespace());
                    if (elements.size() > 0)
                    {
                        Element result = elements.get(0);
                        return result;
                    }
                }
                break;
            }
        }
        return null;
    }

    /**
     * @return the descriptorType
     */
    public DescriptorType getDescriptorType()
    {
        return this.descriptorType;
    }

    /**
     * Get elements of a particular descriptor tag whose identifier matches the passed parameter.
     * 
     * @param tag tag to search for
     * @param value value for the identifier to match
     * @return the element that matches
     */
    public Element getTagByIdentifier(DescriptorTag tag, String value)
    {
        if (value == null || tag == null)
        {
            throw new NullPointerException();
        }
        Identifier id = tag.getIdentifier();
        if (id != null)
        {
            List<Element> tags = getTags(tag);

            for (Element e : tags)
            {
                if (value.equals(id.getIdentifier(e)))
                {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * Get elements of a particular descriptor tag whose identifier matches the passed parameter.
     * 
     * @param tagName Name of the tag to search for
     * @param value value for the identifier to match
     * @return the element that matches
     */
    public Element getTagByIdentifier(String tagName, String value)
    {
        if (value == null || tagName == null)
        {
            throw new NullPointerException();
        }
        DescriptorTag tag = getDescriptorType().getTagByName(tagName);
        Identifier id = tag.getIdentifier();
        if (id != null)
        {
            List<Element> tags = getTags(tagName);

            for (Element e : tags)
            {
                if (value.equals(id.getIdentifier(e)))
                {
                    return e;
                }
            }
        }
        return null;
    }
}
