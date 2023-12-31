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
package org.codehaus.cargo.container.internal.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the content type for an url encoded form.
 */
public class UrlEncodedFormContentType implements FormContentType
{
    /**
     * Form contents part of this multipart form.
     */
    private final Map<String, String> contents = new HashMap<String, String>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType()
    {
        return "application/x-www-form-urlencoded";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFormContent(String key, String value)
    {
        contents.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getFormContents()
    {
        return contents;
    }
}
