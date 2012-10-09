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
package org.codehaus.cargo.tools.daemon;

/**
 * Represents the content type for a multipart form.
 *
 * @version $Id$
 */
public class MultipartFormContentType implements FormContentType
{
    /**
     * The multipart boundary string.
     */
    private String boundary = null;

    /**
     * Constructs a multipart token
     */
    public MultipartFormContentType()
    {
        this.boundary = createBoundary();
    }

    /**
     * Creates a boundary string that is highly probable not to be found inside the form data.
     *
     * @return a multipart boundary string
     */
    private String createBoundary()
    {
        return "--------------------" + Long.toString(System.currentTimeMillis(), 16);
    }

    /**
     * Gets the content type for this writer.
     *
     * @return the content type string
     */
    public String getContentType()
    {
        return "multipart/form-data; boundary=" + this.boundary;
    }

    /**
     * @return the multipart chunk boundary
     */
    public String getBoundary()
    {
        return boundary;
    }
}
