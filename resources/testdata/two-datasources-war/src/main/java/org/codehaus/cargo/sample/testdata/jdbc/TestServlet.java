/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Sample test Servlet used to verify that support of multiple datasources.
 */
public class TestServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        Connection c = null;
        Connection c2 = null;
        try
        {
            DataSource ds =
                (DataSource) new InitialContext().lookup("java:comp/env/jdbc/CargoDS");
            DataSource ds2 =
                (DataSource) new InitialContext().lookup("java:comp/env/jdbc/CargoDS2");
            c = ds.getConnection();
            c2 = ds2.getConnection();
            PrintWriter out = response.getWriter();
            out.print("Got connections!");
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
        finally
        {
            try
            {
                if (c != null)
                {
                    c.close();
                }
                if (c2 != null)
                {
                    c2.close();
                }
            }
            catch (SQLException e)
            {
                throw new ServletException(e);
            }
        }
    }
}
