/* 
 * ========================================================================
 * 
 * Copyright 2005 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.websphere;

import org.codehaus.cargo.module.AbstractDescriptorType;

/**
 * @version $Id: $
 *
 */
public class IbmWebBndXmiType  extends AbstractDescriptorType
{
    /**
     * Instance variable.
     */
    private static IbmWebBndXmiType instance = new IbmWebBndXmiType();
  
    /**
     * Protected constructor.
     */
    protected IbmWebBndXmiType()
    {
        super(null, IbmWebBndXmi.class, new IbmWebBndXmiGrammar());   
    }
 
    /**
     * @return static singleton instance
     */
    public static IbmWebBndXmiType getInstance()
    {
        return instance;
    }
}
