package ${package};

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

public class DataSourceServlet extends HttpServlet
{
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        Connection c = null;
        try
        {
            DataSource ds =
                (DataSource) new InitialContext().lookup("java:comp/env/jdbc/CargoDS");
            c = ds.getConnection();
            PrintWriter out = response.getWriter();
            out.print("Got connection!");
            out.close();
        }
        catch (SQLException e)
        {
            throw new ServletException(e);
        }
        catch (NamingException e)
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
            }
            catch (SQLException e)
            {
                throw new ServletException(e);
            }
        }
    }
}
