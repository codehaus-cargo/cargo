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
package org.codehaus.cargo.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility for compressing ZIP files.
 */
public class ZipCompressor
{
    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Saves the file handler.
     * @param fileHandler File handler.
     */
    public ZipCompressor(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }

    /**
     * Compress a directory into a ZIP file.
     * @param sourceDirectory Source directory.
     * @param targetFile Target ZIP file.
     * @throws IOException If any I/O exception occurs.
     */
    public void compress(String sourceDirectory, String targetFile) throws IOException
    {
        try (ZipOutputStream zipFile =
            new ZipOutputStream(this.fileHandler.getOutputStream(targetFile)))
        {
            this.compressDirectory(sourceDirectory, sourceDirectory, zipFile);
        }
    }

    /**
     * Compress a (sub)directory into a ZIP file.
     * @param currentDirectory Current directory being compressed.
     * @param startDirectory Source directory where compression started from (for the recursion).
     * @param zipFile ZIP file stream to write into.
     * @throws IOException If any I/O exception occurs.
     */
    private void compressDirectory(String currentDirectory, String startDirectory,
        ZipOutputStream zipFile) throws IOException
    {
        for (String child : this.fileHandler.getChildren(currentDirectory))
        {
            String childPath = child.replace(startDirectory, "").replace('\\', '/');
            if (childPath.startsWith("/"))
            {
                childPath = childPath.substring(1);
            }

            if (this.fileHandler.isDirectory(child))
            {
                // In ZIP terminology, en entry is a directory when its name ends with a /
                childPath = childPath + '/';
                ZipEntry entry = new ZipEntry(childPath);
                zipFile.putNextEntry(entry);

                this.compressDirectory(child, startDirectory, zipFile);
            }
            else
            {
                ZipEntry entry = new ZipEntry(childPath);
                zipFile.putNextEntry(entry);
                try (InputStream content = this.fileHandler.getInputStream(child))
                {
                    this.fileHandler.copy(content, zipFile);
                }
            }
        }
    }
}
