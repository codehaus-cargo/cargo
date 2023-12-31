/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.cargo.util.CargoException;
import org.jdom2.DefaultJDOMFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMFactory;
import org.jdom2.Namespace;

/**
 */
public class AbstractDescriptorType extends DefaultJDOMFactory implements DescriptorType
{

    /**
     * Parent descriptor.
     */
    private DescriptorType parent;

    /**
     * Grammar of the descriptor.
     */
    private Grammar grammar;

    /**
     * List of descriptor tags.
     */
    private List<DescriptorTag> tags;

    /**
     * Descriptor class.
     */
    private Class descriptorClass;

    /**
     * Class used for I/O.
     */
    private DescriptorIo descriptorIo;

    /**
     * Constructor.
     * 
     * @param parent the parent of this type
     * @param descriptorClass the class that implements this descriptor
     * @param grammar grammar for this type (or null if none).
     */
    protected AbstractDescriptorType(DescriptorType parent, Class descriptorClass,
        Grammar grammar)
    {
        this.parent = parent;
        this.grammar = grammar;
        this.descriptorClass = descriptorClass;

        tags = new ArrayList<DescriptorTag>();
    }

    /**
     * Get the IO class for this descriptor type.
     * 
     * @return the IO class
     */
    @Override
    public DescriptorIo getDescriptorIo()
    {
        return this.descriptorIo;
    }

    /**
     * Set the IO class for this descriptor type.
     * 
     * @param descriptorIo the IO class
     */
    protected void setDescriptorIo(DescriptorIo descriptorIo)
    {
        this.descriptorIo = descriptorIo;
    }

    /**
     * Get the grammar for this descriptor type.
     * 
     * @return grammar
     */
    @Override
    public Grammar getGrammar()
    {
        return this.grammar;
    }

    /**
     * Add a descriptor tag to this descriptor type.
     * 
     * @param tag the tag to add.
     */
    @Override
    public void addTag(DescriptorTag tag)
    {
        this.tags.add(tag);
    }

    /**
     * @param name name of the tag
     * @return the matching descriptor tag
     */
    @Override
    public DescriptorTag getTagByName(String name)
    {
        for (DescriptorTag tag : tags)
        {
            if (tag.getTagName().equals(name))
            {
                return tag;
            }
        }

        return parent != null ? parent.getTagByName(name) : null;
    }

    /**
     * @return a collection of all tags
     */
    @Override
    public Collection<DescriptorTag> getAllTags()
    {
        List<DescriptorTag> items = new ArrayList<DescriptorTag>(tags);
        if (parent != null)
        {
            items.addAll(parent.getAllTags());
        }
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element element(int line, int col, String name)
    {
        DescriptorTag tag = getTagByName(name);
        if (tag != null)
        {
            try
            {
                return tag.create();
            }
            catch (Exception ignored)
            {
                // Ignored
            }
        }

        return super.element(line, col, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element element(int line, int col, String name, Namespace namespace)
    {
        DescriptorTag tag = getTagByName(name);
        if (tag != null)
        {
            try
            {
                DescriptorElement element = tag.create();
                if (element.getNamespace() == null && namespace != null)
                {
                    element.setNamespace(namespace);
                }
                return element;
            }
            catch (Exception ignored)
            {
                // Ignored
            }
        }
        return super.element(line, col, name, namespace);
    }

    /**
     * Create a JDOM Document.
     * @param rootElement the root element for the document
     * @return the document created
     */
    @Override
    public Document document(Element rootElement)
    {
        if (this.descriptorClass != null)
        {
            try
            {
                Constructor constructor =
                    this.descriptorClass.getConstructor(Element.class, DescriptorType.class);
                Document doc = (Document) constructor.newInstance(rootElement, this);
                return doc;
            }
            catch (Exception ex)
            {
                throw new CargoException("Error constructing document type", ex);
            }
        }
        return new Document(rootElement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JDOMFactory getJDOMFactory()
    {
        // This class is itself the JDOMFactory
        return this;
    }
}
