/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.sample.testdata.jdbc;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

/**
 * Sample test Servlet used to verify that resource XADataSource is deployed.
 */
public class TestServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        XAConnection c = null;
        Object o = null;
        try
        {
            o = new InitialContext().lookup("java:comp/env/resource/XADataSource");
            XADataSource ds = (XADataSource) o;
            c = ds.getXAConnection();
            PrintWriter out = response.getWriter();
            out.print("Got connection!");
            out.close();
        }
        catch (NamingException e)
        {
            throw new ServletException(e);
        }
        catch (SQLException e)
        {
            throw new ServletException(e);
        }
        catch (ClassCastException e)
        {
            throw new ServletException("Got " + o.getClass() + " instead of XADataSource", e);
        }
        finally
        {
            try
            {
                if (c != null)
                {
                    c.close();
                }
            }
            catch (SQLException e)
            {
                throw new ServletException(e);
            }
        }
    }
}
