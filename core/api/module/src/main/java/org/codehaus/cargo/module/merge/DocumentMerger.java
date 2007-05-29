/*
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
import java.util.List;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;


/**
 * Class that can merge two DOM Documents, relatively simply. This works by just
 * importing all the child nodes from the right-hand document into the left-hand
 * document.
 * 
 * Create the DocumentMerger, then call addMergeItem successive times
 * with Documents. Finally call performMerge(), which will return you the merged
 * DOM Document.
 * 
 * This class may be overridden by subclasses that wish to apply strategies
 * to particular types of XML document.
 * 
 * @version $Id: $
 */
public class DocumentMerger implements MergeProcessor
{
    /**
     * The list of merge documents.
     */
    private List documents = new ArrayList();

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.MergeProcessor#addMergeItem(java.lang.Object)
     */
    public void addMergeItem(Object mergeItem) throws MergeException
    {
        if (mergeItem instanceof Document)
        {
            this.documents.add(mergeItem);
        }
        else
        {
            throw new MergeException("DocumentMerger can only merge Documents");
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.MergeProcessor#performMerge()
     */
    public Object performMerge()
    {
        if (this.documents.size() == 0)
        {
            return null;
        }

        Document doc = (Document) this.documents.get(0);

        for (int i = 1; i < this.documents.size(); i++)
        {
            merge(doc, (Document) this.documents.get(i));
        }

        return doc;
    }

    /**
     * Merge the right hand document into the left hand document, currently by just adding all the
     * nodes from right into left.
     * 
     * @param left in the left hand document
     * @param right the right hand document
     */
    private void merge(Document left, Document right)
    {
        List children = right.getRootElement().getContent();
        for (int i = 0; i < children.size(); i++)
        {
            Content node = (Content) children.get(i);
            if (node instanceof Element)
            {
                Content clone = ((Element) node).detach();
                left.getRootElement().addContent(clone);
            }
        }
    }
}
