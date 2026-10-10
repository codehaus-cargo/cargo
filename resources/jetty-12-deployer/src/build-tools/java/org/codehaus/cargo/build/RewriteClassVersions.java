/*
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.cargo.build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Rewrites Jetty class files compiled for Java 17 to appear as Java 11 class
 * files.<br>
 * <br>
 * Jetty 12 is compiled for Java 17, while Cargo is compiled for Java 11. The
 * rewritten JARs are only used during compilation. The original Jetty JARs
 * are still used at runtime.
 */
public class RewriteClassVersions
{
    /**
     * Java 11 class file major version.
     */
    private static final int JAVA_11_CLASS_FILE_VERSION = 55;

    /**
     * Java 17 class file major version.
     */
    private static final int JAVA_17_CLASS_FILE_VERSION = 61;

    /**
     * Java 22 class file major version.
     */
    private static final int JAVA_22_CLASS_FILE_VERSION = 66;

    /**
     * Main entry point for rewriting class file versions.
     *
     * @param args command line arguments containing the directory to process
     * @throws Exception if a JAR cannot be processed
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            throw new IllegalArgumentException(
                "Usage: RewriteClassVersions <directory>");
        }

        processDirectory(new File(args[0]));
    }

    /**
     * Processes all JAR files in the specified directory and its subdirectories.
     *
     * @param directory directory containing the JAR files to process
     * @throws IOException if a JAR cannot be processed
     */
    private static void processDirectory(File directory) throws IOException
    {
        File[] files = directory.listFiles();

        if (files == null)
        {
            return;
        }

        for (File file : files)
        {
            if (file.isDirectory())
            {
                processDirectory(file);
            }
            else if (file.getName().endsWith(".jar"))
            {
                rewriteJar(file);
            }
        }
    }

    /**
     * Rewrites the class file versions contained in the specified JAR.
     *
     * @param file JAR file to rewrite
     * @throws IOException if the JAR cannot be read or written
     */
    private static void rewriteJar(File file) throws IOException
    {
        System.out.println("Rewriting " + file);

        File temporaryFile = new File(
            file.getParentFile(), file.getName() + ".tmp");

        try (
            ZipInputStream input = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(file)));
            ZipOutputStream output = new ZipOutputStream(
                new BufferedOutputStream(new FileOutputStream(temporaryFile))))
        {
            ZipEntry entry;

            while ((entry = input.getNextEntry()) != null)
            {
                ZipEntry outputEntry = new ZipEntry(entry.getName());
                outputEntry.setTime(entry.getTime());

                output.putNextEntry(outputEntry);

                if (!entry.isDirectory())
                {
                    byte[] bytes = readEntry(input);

                    rewriteClassFileVersion(bytes, file, entry);

                    output.write(bytes);
                }

                output.closeEntry();
            }
        }

        if (!file.delete())
        {
            throw new IOException("Cannot delete " + file);
        }

        if (!temporaryFile.renameTo(file))
        {
            throw new IOException(
                "Cannot rename " + temporaryFile + " to " + file);
        }
    }

    /**
     * Reads all bytes from the current ZIP entry.
     *
     * @param input input stream containing the ZIP entry
     * @return the bytes read from the ZIP entry
     * @throws IOException if the entry cannot be read
     */
    private static byte[] readEntry(ZipInputStream input) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buffer = new byte[8192];

        int count;

        while ((count = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, count);
        }

        return output.toByteArray();
    }

    /**
     * Rewrites a Java 17 class file to the Java 11 class file version.<br>
     * <br>
     * Only the class file major version is changed. All other class file
     * contents remain untouched.
     *
     * @param bytes class file or other ZIP entry contents
     * @param jar JAR containing the entry
     * @param entry ZIP entry being processed
     */
    private static void rewriteClassFileVersion(
        byte[] bytes, File jar, ZipEntry entry)
    {
        if (bytes.length < 8)
        {
            return;
        }

        /*
         * Check for the class file magic number CAFEBABE.
         */
        if ((bytes[0] & 0xff) != 0xca
            || (bytes[1] & 0xff) != 0xfe
            || (bytes[2] & 0xff) != 0xba
            || (bytes[3] & 0xff) != 0xbe)
        {
            return;
        }

        int majorVersion =
            ((bytes[6] & 0xff) << 8) | (bytes[7] & 0xff);

        if (majorVersion == JAVA_17_CLASS_FILE_VERSION
            || majorVersion == JAVA_22_CLASS_FILE_VERSION)
        {
            bytes[6] = (byte) (JAVA_11_CLASS_FILE_VERSION >>> 8);
            bytes[7] = (byte) JAVA_11_CLASS_FILE_VERSION;
        }
        else if (majorVersion > JAVA_22_CLASS_FILE_VERSION)
        {
            throw new IllegalStateException(
                "Unexpected class file version " + majorVersion
                    + " in " + jar + "!" + entry.getName());
        }
    }
}
