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
package org.codehaus.cargo.container.internal.http;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the content type for a multipart form.
 */
public class MultipartFormContentType extends UrlEncodedFormContentType
{
    /**
     * Files part of this multipart form.
     */
    private final Map<String, File> files = new HashMap<String, File>();

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
        return "----CargoFormBoundary" + Long.toString(System.currentTimeMillis(), 16);
    }

    /**
     * Gets the content type for this writer.
     * 
     * @return the content type string
     */
    @Override
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

    /**
     * Sets a form content file, repeating names (keys) will be overriden.
     * 
     * @param key The key name, used as file name
     * @param file The file
     */
    public void setFormFile(String key, File file)
    {
        files.put(key, file);
    }

    /**
     * @return the form file contents map
     */
    public Map<String, File> getFormFiles()
    {
        return files;
    }
}
