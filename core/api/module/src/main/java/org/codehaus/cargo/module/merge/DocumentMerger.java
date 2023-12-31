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
package org.codehaus.cargo.module.merge;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Class that can merge two DOM Documents, relatively simply. This works by just importing all the
 * child nodes from the right-hand document into the left-hand document.<br>
 * <br>
 * Create the <code>DocumentMerger</code>, then call <code>addMergeItem</code> successive times
 * with <code>Document</code>s. Finally call <code>performMerge()</code>, which will return you the
 * merged DOM Document.<br>
 * <br>
 * This class may be overridden by subclasses that wish to apply strategies to particular types of
 * XML document.
 */
public class DocumentMerger implements MergeProcessor
{
    /**
     * The list of merge documents.
     */
    private List<Document> documents = new ArrayList<Document>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMergeItem(Object mergeItem) throws MergeException
    {
        if (mergeItem instanceof Document)
        {
            this.documents.add((Document) mergeItem);
        }
        else
        {
            throw new MergeException("DocumentMerger can only merge Documents");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object performMerge()
    {
        if (this.documents.isEmpty())
        {
            return null;
        }

        Document doc = this.documents.get(0);

        for (int i = 1; i < this.documents.size(); i++)
        {
            Document temp = merge(doc, this.documents.get(i));
            doc = temp;
        }

        return doc;
    }


    /**
     * Merge the right hand document into the left hand document, currently by just adding all the
     * nodes from right into left.
     * 
     * @param left in the left hand document
     * @param right the right hand document
     * @return merged document
     */
    private Document merge(Document left, Document right)
    {
        List<Content> children = new ArrayList<Content>();
        children.addAll(right.getRootElement().getContent());

        Document tempLeft = (Document) left.clone();

        for (Content node : children)
        {
            if (node instanceof Element)
            {
                Content clone = ((Element) node).detach();
                tempLeft.getRootElement().addContent(clone);
            }
        }

        return tempLeft;
    }
}
