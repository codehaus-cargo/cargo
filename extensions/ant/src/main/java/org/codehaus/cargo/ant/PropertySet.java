/* 
 * ========================================================================
 * 
 * Copyright 2004 Vincent Massol.
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
package org.codehaus.cargo.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.tools.ant.BuildException;

/**
 * Ant element used to tell the Cargo task to load a properties file containing system properties.
 * 
 * Note: When we switch to Ant 1.6 we will be able to replace this by Ant 1.6 PropertySet
 * 
 * @version $Id$
 */
public class PropertySet
{
    /**
     * Properties file to load.
     */
    private File propertiesFile;

    /**
     * @param propertiesFile the properties file to load
     */
    public void setFile(File propertiesFile)
    {
        this.propertiesFile = propertiesFile;
    }

    /**
     * @return the properties loaded from the properties file
     */
    public ResourceBundle readProperties()
    {
        if (this.propertiesFile == null)
        {
            throw new BuildException("Missing [propertiesFiles] attribute");
        }
        
        ResourceBundle bundle;
        try
        {
            bundle = new PropertyResourceBundle(new FileInputStream(this.propertiesFile));
        } 
        catch (IOException e)
        {
            throw new BuildException("Failed to load properties file [" + this.propertiesFile 
                + "]");
        }
        return bundle;
    }
}
