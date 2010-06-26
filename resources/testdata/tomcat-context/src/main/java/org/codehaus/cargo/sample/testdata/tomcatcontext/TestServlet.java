/*
 * ========================================================================
 * 
 * Copyright 2004-2005 Vincent Massol.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * ========================================================================
 */
package org.codehaus.cargo.sample.testdata.tomcatcontext;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sample test Servlet used to verify that parameters passed in Tomcat's <code>context.xml</code>
 * file are correctly passed to Tomcat when the WAR module is deployed. 
 *
 * @version $Id$
 */
public class TestServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException
    {
        String value = getServletContext().getInitParameter("testcontextxml");
        PrintWriter out = response.getWriter();
        out.print("Test value is [" + value + "]");
        out.close();
    }
}