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
package org.codehaus.cargo.daemon;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cargo daemon error handler servlet.
 *
 */
public class CargoDaemonErrorServlet extends HttpServlet
{

    /**
     * Serial version UUID.
     */
    private static final long serialVersionUID = -7505058847561492712L;

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String message = (String) request.getAttribute("javax.servlet.error.message");
        if (message == null)
        {
            Throwable exception = (Throwable)
                request.getAttribute("javax.servlet.error.exception");
            if (exception != null)
            {
                message = exception.toString();
            }
        }

        if (message == null)
        {
            message = "An unknown error has occured while handling your request";
        }
        else
        {
            message = "Error handling your request: " + message;
        }

        response.setContentType("text/html");
        response.getWriter().println("<script>");
        response.getWriter().println("window.alert(\"" + message.replace("\\", "\\\\")
            .replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\")");
        response.getWriter().println("</script>");
        response.getWriter().println(message);
    }

}
