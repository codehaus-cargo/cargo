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
package org.codehaus.cargo.sample.maven2.ping_test;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Ping test servlet.
 * 
 * @version $Id$
 */
public class PingTestServlet extends HttpServlet
{

    /**
     * Logger.
     */
    private static Logger logger = Logger.getLogger(PingTestServlet.class.getName());

    /**
     * Start time of the servlet.
     */
    private static long startTime = 0;

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        final long wait = 25000;

        if (PingTestServlet.startTime == 0)
        {
            PingTestServlet.startTime = System.currentTimeMillis();
        }

        long timeElapsed = System.currentTimeMillis() - PingTestServlet.startTime;
        if (timeElapsed < wait)
        {
            PingTestServlet.logger.info(
                "PingTestServlet - remaining milliseconds before OK: " + (wait - timeElapsed));

            throw new IOException("The servlet is not ready yet");
        }
        else
        {
            response.getWriter().write("Servlet is now ready");
        }
    }

}
