package controller;

import dal.DBContext;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestDB extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        DBContext dbContext = new DBContext();
        
        try (PrintWriter out = response.getWriter())  {
            if (dbContext.getConnection() != null) {
                out.println("<h1> Connect database successfully </h1> ");
            }
            else {
                out.println("<h1> Connect database fail </h1> ");
            }
        } catch (SQLException ex) {
            Logger.getLogger(TestDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
