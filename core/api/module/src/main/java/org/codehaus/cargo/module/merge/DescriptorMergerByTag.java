/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.module.merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.module.Descriptor;
import org.codehaus.cargo.module.DescriptorElement;
import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.DescriptorType;
import org.codehaus.cargo.module.Identifier;
import org.codehaus.cargo.module.merge.tagstrategy.MergeStrategy;
import org.codehaus.cargo.util.CargoException;
import org.jdom.Element;

/**
 * Merge a descriptor by tag.
 * 
 * @version $Id$
 */
public class DescriptorMergerByTag implements DescriptorMerger
{
    /**
     * A default merge strategy for ignoring items.
     */
    public static final MergeStrategy IGNORE = new MergeStrategy()
    {        
        public int inBoth(Descriptor target, DescriptorElement left, DescriptorElement right)
        {         
            return 0;
        }
       
        public int inLeft(Descriptor target, DescriptorElement left)
        {
            return 0;
        }

        public int inRight(Descriptor target, DescriptorElement right)
        {         
            return 0;
        }
    };
    
    /**
     * A default merge strategy for overwriting items.
     */
    public static final MergeStrategy OVERWRITE = new MergeStrategy()
    {        
        public int inBoth(Descriptor target, DescriptorElement left, DescriptorElement right)
        {
            Element parent = left.getParentElement();
            parent.removeContent(left);
            target.addElement(right.getTag(), right, parent);
            return 1;
        }
       
        public int inLeft(Descriptor target, DescriptorElement left)
        {
            return 0;
        }
       
        public int inRight(Descriptor target, DescriptorElement right)
        {
            target.addElement(right.getTag(), right, target.getRootElement());
            return 1;
        }
    };

    /**
     * A default merge strategy for preserving items.
     */
    public static final MergeStrategy PRESERVE = new MergeStrategy()
    {       
        public int inBoth(Descriptor target, DescriptorElement left, DescriptorElement right)
        {            
            return 0;
        }
        
        public int inLeft(Descriptor target, DescriptorElement left)
        {         
            return 0;
        }
        
        public int inRight(Descriptor target, DescriptorElement right)
        {
            target.addElement(right.getTag(), right, target.getRootElement());
            return 1;
        }

    };

    /**
     * The descriptor being merged onto.
     */
    private Descriptor baseDescriptor;

    /**
     * The descriptor tag factory.
     */
    private DescriptorType descriptorTagFactory;

    /**
     * Various Strategies for merging the individual descriptor tags.
     */
    private Map<String, MergeStrategy> mapDescriptorTagToStrategy;

    /**
     * the default merging strategy.
     */
    private MergeStrategy defaultStrategyIfNoneSpecified = OVERWRITE;

    /**
     * Constructor.
     */
    public DescriptorMergerByTag()
    {
        this.mapDescriptorTagToStrategy = new HashMap<String, MergeStrategy>();
    }

    /**
     * Set the merging strategy for a particular tag.
     * @param tag Tag to set
     * @param strategy Strategy to use
     */
    public void setStrategy(String tag, MergeStrategy strategy)
    {
        this.mapDescriptorTagToStrategy.put(tag, strategy);
    }

    /**
     * Initialize.
     * 
     * @param base the base for the merge.
     */
    public void init(Descriptor base)
    {
        this.baseDescriptor = base;
    }

    /**
     * Merge this descriptor onto another.
     * @param other descriptor to merge
     */
    public void merge(Descriptor other)
    {

        for (DescriptorTag tag : descriptorTagFactory.getAllTags())
        {
            Identifier identifier = tag.getIdentifier();
            if (identifier != null)
            {
                MergeStrategy strategy = getMergeStrategy(tag.getTagName());

                Descriptor left = baseDescriptor;
                Descriptor right = other;

                List<Element> itemsL = new ArrayList<Element>(left.getTags(tag));
                List<Element> itemsR = new ArrayList<Element>(right.getTags(tag));

                try
                {
                    for (Element itemL : itemsL)
                    {
                        DescriptorElement lElement = (DescriptorElement) itemL;
                        DescriptorElement rElement =
                            (DescriptorElement) right.getTagByIdentifier(tag, identifier
                                .getIdentifier(lElement));

                        if (rElement != null)
                        {
                            strategy.inBoth(left, lElement, rElement);
                        }
                        else
                        {
                            strategy.inLeft(left, lElement);
                        }
                    }

                    for (Element itemR : itemsR)
                    {
                        DescriptorElement rElement = (DescriptorElement) itemR;
                        DescriptorElement lElement =
                            (DescriptorElement) left.getTagByIdentifier(tag, identifier
                                .getIdentifier(rElement));

                        if (lElement == null)
                        {
                            strategy.inRight(left, rElement);
                        }
                    }
                }
                catch (Exception ex)
                {
                    throw new CargoException("Element Merging Exception", ex);
                }
            }
            else
            {
                Descriptor left = baseDescriptor;
                Descriptor right = other;

                List<Element> itemsL = left.getTags(tag);
                List<Element> itemsR = new ArrayList<Element>(right.getTags(tag));

                if (tag.isMultipleAllowed())
                {
                    // If multiple items are allowed, but there's no way of
                    // identifying tags
                    // From each other, then the best we can do is merge them
                    // together by addition...
                    for (Element itemR : itemsR)
                    {
                        DescriptorElement rightElement = (DescriptorElement) itemR;
                        left.addElement(tag, rightElement, left.getRootElement());
                    }
                } 
                else
                {
                    // It is possible that this tag is a single value item (e.g.
                    // webxml display-name)
                    // so either it can exist singly, or not at all.
                    MergeStrategy strategy = getMergeStrategy(tag.getTagName());

                    DescriptorElement leftElement = (itemsL.size() == 0) ? null
                            : (DescriptorElement) itemsL.get(0);
                    DescriptorElement rightElement = (itemsR.size() == 0) ? null
                            : (DescriptorElement) itemsR.get(0);

                    try
                    {
                        if (leftElement != null && rightElement != null)
                        {
                            strategy.inBoth(left, leftElement, rightElement);
                        }
                        else if (leftElement != null)
                        {
                            strategy.inLeft(left, leftElement);
                        }
                        else if (rightElement != null)
                        {
                            strategy.inRight(left, rightElement);
                        }
                    } 
                    catch (Exception ex)
                    {
                        throw new CargoException("Element Merging Exception", ex);
                    }
                }
              
            }

        }
    }

    /**
     * @param tag tag to get the merge strategy for
     * @return the merge strategy
     */
    protected MergeStrategy getMergeStrategy(String tag)
    {
        MergeStrategy strategy = mapDescriptorTagToStrategy.get(tag);
        if (strategy == null)
        {
            return defaultStrategyIfNoneSpecified;
        }
        return strategy;
    }

    /**
     * @return the defaultStrategyIfNoneSpecified
     */
    public MergeStrategy getDefaultStrategyIfNoneSpecified()
    {
        return this.defaultStrategyIfNoneSpecified;
    }

    /**
     * @param defaultStrategyIfNoneSpecified the defaultStrategyIfNoneSpecified to set
     */
    public void setDefaultStrategyIfNoneSpecified(MergeStrategy defaultStrategyIfNoneSpecified)
    {
        this.defaultStrategyIfNoneSpecified = defaultStrategyIfNoneSpecified;
    }

    /**
     * @return the descriptorTagFactory
     */
    public DescriptorType getDescriptorType()
    {
        return this.descriptorTagFactory;
    }

    /**
     * @param descriptorTagFactory the descriptorTagFactory to set
     */
    public void setDescriptorType(DescriptorType descriptorTagFactory)
    {
        this.descriptorTagFactory = descriptorTagFactory;
    }

}
