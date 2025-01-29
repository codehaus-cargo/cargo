/*
* Copyright 2016 IBM Corp.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
 */
package org.codehaus.cargo.container.liberty.internal;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.cargo.container.configuration.entry.DataSource;

/**
 * A utility class holding useful methods for writing WebSphere Liberty server config files.
 */
public final class ServerConfigUtils
{

    /**
     * Private constructor to prevent getting an instance.
     */
    private ServerConfigUtils()
    {
        // Utility classes have no public constructors
    }

    /**
     * Opens a new <code>server.xml</code> for writing and writes the opening server element
     * 
     * @param xmlFile the file to write into
     * @return a print stream for writing into
     * @throws IOException if an exception occurred
     */
    public static PrintStream open(File xmlFile) throws IOException
    {
        PrintStream writer = new PrintStream(xmlFile);
        writer.println("<server>");
        return writer;
    }

    /**
     * Closes a <code>server.xml</code> and writes the closing <code>server</code> element.
     * 
     * @param writer the print stream to close
     */
    public static void close(PrintStream writer)
    {
        writer.println("</server>");
        writer.close();
    }

    /**
     * Write a library.
     * 
     * @param writer the writer to write the library to
     * @param id the id for the library, if null no id is written.
     * @param cp the classpath, must be non null
     */
    public static void writeLibrary(PrintStream writer, String id, String[] cp)
    {
        writer.print("  <library");
        if (id != null)
        {
            writer.print(" id=\"");
            writer.print(id);
            writer.print('\"');
        }
        writer.println('>');
        for (String file : cp)
        {
            File f = new File(file);
            if (f.isDirectory())
            {
                writer.print("        <folder dir=\"");
                writer.print(f.getAbsolutePath());
                writer.println("\"/>");
            }
            else
            {
                writer.print("        <file name=\"");
                writer.print(f.getAbsolutePath());
                writer.println("\"/>");
            }
        }
        writer.println("  </library>");
    }

    /**
     * Writes a datasource to the write.
     * 
     * @param writer the writer.
     * @param ds the datasource.
     */
    public static void writeDataSource(PrintStream writer, DataSource ds)
    {
        writer.print("  <dataSource jndiName=\"");
        writer.print(ds.getJndiLocation());
        writer.print("\" id=\"");
        writer.print(ds.getId());
        writer.println("\">");
        writer.print("    <jdbcDriver libraryRef=\"cargoLib\" ");
        writer.print(ds.getConnectionType());
        writer.print("=\"");
        writer.print(ds.getDriverClass());
        writer.println("\"/>");
        writer.print("    <properties ");
        String url = ds.getUrl();
        if (url != null)
        {
            writer.print("URL=\"");
            writer.print(ds.getUrl());
            writer.print("\" ");
        }
        writeProperties(writer, ds.getConnectionProperties());
        writer.println("/>");

        String user = ds.getUsername();
        String pass = ds.getPassword();
        if (user != null && pass != null)
        {
            writer.print("    <containerAuthData user=\"");
            writer.print(user);
            writer.print("\" password=\"");
            writer.print(pass);
            writer.println("\"/>");
        }
        writer.println("  </dataSource>");
    }

    /**
     * Write a map as a set of xml attributes.
     * 
     * @param writer the writer to write to
     * @param props the properties to write.
     */
    private static void writeProperties(PrintStream writer, Map<Object, Object> props)
    {
        if (props != null)
        {
            for (Entry<Object, Object> entry : props.entrySet())
            {
                writer.print(entry.getKey());
                writer.print("=\"");
                writer.print(entry.getValue());
                writer.print("\" ");
            }
        }
    }
}
