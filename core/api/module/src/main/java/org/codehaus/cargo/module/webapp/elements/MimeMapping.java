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
package org.codehaus.cargo.module.webapp.elements;

import org.codehaus.cargo.module.webapp.WebXmlTag;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.jdom.Element;

/**
 */
public class MimeMapping extends WebXmlElement
{
    /**
     * Constructor.
     * @param tag Web Xml Tag definition
     */
    public MimeMapping(WebXmlTag tag)
    {
        super(tag);
    }

    /**
     * Get the Extension.
     * @return Extension
     */
    public String getExtension()
    {
        Element e = child(WebXmlType.EXTENSION);
        return e.getText();
    }

    /**
     * Set the Extension.
     * @param extension The Extension
     */
    public void setExtension(String extension)
    {
        Element e = child(WebXmlType.EXTENSION);
        e.setText(extension);
    }

    /**
     * Get the mime type.
     * @return The mime type
     */
    public String getMimeType()
    {
        Element e = child(WebXmlType.MIME_TYPE);
        return e.getText();
    }

    /**
     * Set the mime type.
     * @param mimeType The mime type
     */
    public void setMimeType(String mimeType)
    {
        Element e = child(WebXmlType.MIME_TYPE);
        e.setText(mimeType);
    }
}
