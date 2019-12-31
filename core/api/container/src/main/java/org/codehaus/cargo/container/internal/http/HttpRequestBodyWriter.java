/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2020 Ali Tokmen.
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Class which want to create own custom HTTP request body has to implement this interface.
 */
public interface HttpRequestBodyWriter
{
    /**
     * Write custom request body to output stream.
     *
     * @param outputStream Output stream.
     * @throws IOException If anything goes wrong.
     */
    void writeToOutputStream(OutputStream outputStream) throws IOException;
}
