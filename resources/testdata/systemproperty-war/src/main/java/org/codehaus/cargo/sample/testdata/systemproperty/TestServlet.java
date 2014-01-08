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
package org.codehaus.cargo.sample.testdata.systemproperty;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sample test Servlet used to verify that the system properties are put in place correctly.
 * 
 * @version $Id$
 */
public class TestServlet extends HttpServlet
{
    private static final String PROPERTY_NAME = "systemPropertyName";

    @Override
    public void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String systemPropertyName = request.getParameter(TestServlet.PROPERTY_NAME);

        if (systemPropertyName == null || systemPropertyName.length() == 0)
        {
            out.print("Please pass the property to retrieve via the parameter <b><code>");
            out.print(TestServlet.PROPERTY_NAME);
            out.print("</code></b>.");
        }
        else
        {
            String systemPropertyValue = System.getProperty(systemPropertyName);

            if (systemPropertyValue == null || systemPropertyValue.length() == 0)
            {
                out.print("The system property <b><code>");
                out.print(systemPropertyName);
                out.print("</code></b> is not set.");
            }
            else
            {
                out.print("The value for the system property <b><code>");
                out.print(systemPropertyName);
                out.print("</code></b> is <b><code>");
                out.print(systemPropertyValue);
                out.print("</code></b>.");
            }
        }

        out.close();
    }
}
