/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This interface intends to remove hard-bindings to a specific xml api.<br>
 * <br>
 * Implementations of this interface will: 1. optionally load a file 2. insert some elements into
 * the current document 3. write the file to disk
 */
public interface XmlFileBuilder
{

    /**
     * sets the name of the file we will read and write.
     * 
     * @param path - where the xml file will be read from or written to.
     */
    void setFile(String path);

    /**
     * assign prefix to namespace mappings used for xpath and other xml operations. Leave alone, or
     * set to null, if you have no namespaces.
     * 
     * @param namespaces - key is prefix value is url
     */
    void setNamespaces(Map<String, String> namespaces);

    /**
     * load the current xml file into a Document.
     * 
     * @return loaded file, represented as a Document.
     */
    Document loadFile();

    /**
     * this will parse one or more elements from elementToParse and insert them under the xpath.
     * note: elementsToParse may not be well formed, but only in one way. The elements may be
     * missing a parent. Example: the following is acceptable by this method elementsToParse =
     * &lt;child1 /&gt; &lt;child2 /&gt; xpath = //parent In this case, both elements child1 and
     * child2 would be placed under the first match for parent. s
     * 
     * @param elementsToParse String containing one or more elements in textual format
     * @param xpath where to place the above elements.
     */
    void insertElementsUnderXPath(String elementsToParse, String xpath);

    /**
     * This will take element from elementToInsert and insert it under the xpath.
     * 
     * @param elementToInsert Element to insert
     * @param xpath where to place the above element.
     */
    void insertElementUnderXPath(Element elementToInsert, String xpath);

    /**
     * write the current xml to disk.
     */
    void writeFile();

}
