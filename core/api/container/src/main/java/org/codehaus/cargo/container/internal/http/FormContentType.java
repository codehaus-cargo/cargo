/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2021 Ali Tokmen.
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

import java.util.Map;

/**
 * Represents the content type and basic content of a form.
 */
public interface FormContentType
{
    /**
     * @return the content type of a form
     */
    String getContentType();

    /**
     * Sets a form content, repeating names (keys) will be overriden.
     * 
     * @param key The key name
     * @param value The value string for the specified key
     */
    void setFormContent(String key, String value);

    /**
     * @return the form contents map
     */
    Map<String, String> getFormContents();
}
