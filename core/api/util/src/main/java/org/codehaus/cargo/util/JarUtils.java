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

package org.codehaus.cargo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Some utility classes for manipulating JAR files.
 * 
 * @version $Id $
 */
public final class JarUtils
{   
    /**
     * Create a jar file from a particular directory.
     * 
     * @param root in the root directory
     * @param directory in the directory we are adding
     * @param jarStream the jar stream to be added to
     * @throws IOException on IOException
     */
    protected void createJarFromDirectory(File root,
                                          File directory,
                                          JarOutputStream jarStream)
        throws IOException
    {
        byte[] buffer = new byte[40960];
        int bytesRead;

        File[] filesToAdd = directory.listFiles();

        for (int i = 0; i < filesToAdd.length; i++)
        {
            File fileToAdd = filesToAdd[i];

            if (fileToAdd.isDirectory())
            {
                createJarFromDirectory(root, fileToAdd, jarStream);
            }
            else
            {
                FileInputStream addFile = new FileInputStream(fileToAdd);
                try
                {
                    // Create a jar entry and add it to the temp jar.
                    String entryName = fileToAdd.getPath().substring(root.getPath().length() + 1);
                    
                    // If we leave these entries as '\'s, then the resulting zip file won't be expandable
                    // on Unix operating systems like OSX, becuase it is possible to have filenames with \s
                    // in them - so it's impossible to determine that this is actually a directory.                    
                    entryName = entryName.replace('\\','/');
                    JarEntry entry = new JarEntry(entryName);
                    jarStream.putNextEntry(entry);

                    // Read the file and write it to the jar.
                    while ((bytesRead = addFile.read(buffer)) != -1)
                    {
                        jarStream.write(buffer, 0, bytesRead);
                    }
                    jarStream.closeEntry();
                }
                finally
                {
                    addFile.close();
                }
            }
        }
    }

    /**
     * Create a JAR file from a directory, recursing through children.
     * 
     * @param directory in directory source
     * @param outputJar in file to output the jar data to
     * @return out File that was generated
     * @throws IOException when there is an I/O exception
     */
    public File createJarFromDirectory(String directory, File outputJar)
        throws IOException
    {
        JarOutputStream jarStream = null;
        try
        {
            if (!outputJar.getParentFile().exists())
            {
                outputJar.getParentFile().mkdirs();
            }

            jarStream = new JarOutputStream(new FileOutputStream(outputJar));
            File dir = new File(directory);

            createJarFromDirectory(dir, dir, jarStream);
        }
        finally
        {
            if (jarStream != null)
            {
                jarStream.close();
            }
        }
        return outputJar;
    }

}
