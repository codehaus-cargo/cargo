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
package org.codehaus.cargo.sample.java.validator;

import org.codehaus.cargo.generic.packager.DefaultPackagerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Abstract validator, that instanciates the {@link PackagerFactory}.
 * 
 */
public abstract class AbstractPackagerValidator implements Validator
{
    /**
     * Configuration packager factory.
     */
    protected final PackagerFactory factory = new DefaultPackagerFactory();
}
