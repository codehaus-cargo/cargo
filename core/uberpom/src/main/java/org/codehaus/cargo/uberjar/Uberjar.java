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
package org.codehaus.cargo.uberjar;

import java.io.PrintStream;

/**
 * Cargo uberjar's main class. Shows information about Cargo.
 * 
 * @version $Id$
 */
public class Uberjar
{

    /**
     * Utility classes should not have a public or default constructor.
     */
    protected Uberjar()
    {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Print out some basic documentation.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        PrintStream ps = System.out;

        ps.print("Welcome to Codehaus Cargo");
        String version = Uberjar.class.getPackage().getImplementationVersion();
        if (version != null && version.length() > 0)
        {
            ps.print(" version " + version);
        }
        ps.println(".");
        ps.println();

        ps.println(
            "Cargo is a thin wrapper that allows you to manipulate Java EE containers in a");
        ps.println(
            "standard way. Cargo can be used from a \"classic\" Java program, as a set of ANT");
        ps.println("tasks and even Maven2 goals.");
        ps.println();

        ps.println("See our website for the full documentation: http://cargo.codehaus.org/");
    }

}
