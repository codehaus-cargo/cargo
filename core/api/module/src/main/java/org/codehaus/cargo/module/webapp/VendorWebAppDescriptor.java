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
package org.codehaus.cargo.module.webapp;

import org.codehaus.cargo.module.VendorDescriptor;

/**
 * Common interface for vendor specific deployment descriptors.
 * 
 */
public interface VendorWebAppDescriptor extends VendorDescriptor
{
    /**
     * Adds an ejb reference to the descriptor.
     * 
     * @param ref ejb reference to add to the descriptor
     */
    void addEjbReference(EjbRef ref);
}
