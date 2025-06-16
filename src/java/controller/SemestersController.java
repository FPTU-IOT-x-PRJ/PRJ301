/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.SemesterDAO;
import dao.UserDAO;
import entity.Semester;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Dung Ann
 */
public class SemestersController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());
    SemesterDAO semesterDao = new SemesterDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet SemestersController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet SemestersController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();

        switch (action == null ? "" : action) {
            case "/add":
                request.getRequestDispatcher("/components/semester/semester-add.jsp").forward(request, response);
                break;
            case "/update":
                int editId = Integer.parseInt(request.getParameter("id"));
                Semester s = semesterDao.getSemesterById(editId);
                request.setAttribute("semester", s);
                request.getRequestDispatcher("/components/semester/semester-update.jsp").forward(request, response);
                break;
            case "/delete":
                int deleteId = Integer.parseInt(request.getParameter("id"));
                semesterDao.deleteSemester(deleteId);
                response.sendRedirect("semesters");
                break;
            default: // list
                List<Semester> semesters = semesterDao.selectAllSemesters();
                System.out.println(semesters);
                request.setAttribute("semesterList", semesters);
                request.getRequestDispatcher("/components/semester/semester-dashboard.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "action: {0}", action);
        switch (action != null ? action : "") {
            case "/add":
                addSemester(request, response);
                break;
            case "/edit":
//                editUser(request, response);
                break;
            case "/delete":
//                deleteUser(request, response);
            default:
                break;
        }

    }

    private void addSemester(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String status = request.getParameter("status");
        Date startDate = Date.valueOf(startDateStr); // java.sql.Date
        Date endDate = Date.valueOf(endDateStr);
        

        semesterDao.insertSemester(new Semester(name, startDate, endDate, status, LocalDateTime.now(),LocalDateTime.now()));
            response.sendRedirect(request.getContextPath() + "/semesters");
       

            // Giữ lại dữ liệu đã nhập (nếu muốn)
//            request.setAttribute("formname", name);
//            request.setAttribute("formLastName", lastName);
//            request.setAttribute("formUsername", username);
//            request.setAttribute("formEmail", email);
//            request.setAttribute("formRole", role);

//            request.getRequestDispatcher("/components/user/admin-user-add.jsp").forward(request, response);
        
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
