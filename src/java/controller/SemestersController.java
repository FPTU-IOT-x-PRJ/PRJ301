/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.SemesterDAO;
import entity.Semester;
import entity.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dung Ann
 */
public class SemestersController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SemestersController.class.getName());
    SemesterDAO semesterDao = new SemesterDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            throw new ServletException("Không có quyền truy cập");
        }
        
        User user = (User) session.getAttribute("loggedInUser");
        
        switch (action == null ? "" : action) {
            case "/add":
                request.getRequestDispatcher("/components/semester/semester-add.jsp").forward(request, response);
                break;
            case "/edit":
                int editId = Integer.parseInt(request.getParameter("id"));
                Semester s = semesterDao.getSemesterById(editId, user.getId());
                request.setAttribute("semester", s);
                request.getRequestDispatcher("/components/semester/semester-edit.jsp").forward(request, response);
                break;
            case "/delete":
                int deleteId = Integer.parseInt(request.getParameter("id"));
                semesterDao.deleteSemester(deleteId, user.getId());
                response.sendRedirect("semesters");
                break;
            default:
                displayDashboard(request, response, user);
                break;
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

    private void displayDashboard(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        // 1. Lấy các tham số từ request
        String search = request.getParameter("search");
        String statusFilter = request.getParameter("status"); // "Active", "Inactive", "Completed"
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String pageStr = request.getParameter("page");

        // Xử lý ngày tháng
        Date startDate = null;
        Date endDate = null;

        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = Date.valueOf(startDateStr);
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = Date.valueOf(endDateStr);
            }
        } catch (IllegalArgumentException e) {
            // Handle invalid date format
            LOGGER.log(Level.INFO, "Lỗi khi convert startDate và endDate");
        }

        // Thiết lập giá trị mặc định cho phân trang
        int currentPage = 1;
        int recordsPerPage = 10; // Số kỳ học mỗi trang

        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                currentPage = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                currentPage = 1; // Mặc định về trang 1 nếu page không hợp lệ
            }
        }

        int offset = (currentPage - 1) * recordsPerPage;

        // 2. Lấy tổng số kỳ học (cho phân trang)
        int totalSemesters = semesterDao.getTotalSemesterCount(search, statusFilter, startDate, endDate, user.getId());
        int totalPages = (int) Math.ceil((double) totalSemesters / recordsPerPage);

        // 3. Lấy danh sách kỳ học đã lọc và phân trang
        List<Semester> semesterList = semesterDao.getFilteredAndPaginatedSemesters(search, statusFilter, startDate, endDate, offset, recordsPerPage, user.getId());

        // 5. Đặt các thuộc tính vào request để JSP hiển thị
        request.setAttribute("semesterList", semesterList);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalSemestersCount", totalSemesters); // Tổng số kỳ học sau khi lọc

        // Đặt lại các tham số lọc vào request để giữ trạng thái trên form
        Map<String, String> paginationParams = new HashMap<>();
        paginationParams.put("search", request.getParameter("search"));
        paginationParams.put("status", request.getParameter("status"));
        paginationParams.put("startDate", request.getParameter("startDate"));
        paginationParams.put("endDate", request.getParameter("endDate"));
        request.setAttribute("paginationParams", paginationParams);
        request.setAttribute("baseUrl", request.getContextPath() + "/semesters/dashboard");

        // 6. Chuyển tiếp đến JSP
        request.getRequestDispatcher("/components/semester/semester-dashboard.jsp").forward(request, response);
    }

    
    private void addSemester(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String status = request.getParameter("status");
        String description = request.getParameter("description");
        Date startDate = Date.valueOf(startDateStr); // java.sql.Date
        Date endDate = Date.valueOf(endDateStr);
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            throw new ServletException("Không có quyền truy cập");
        }
        
        User user = (User) session.getAttribute("loggedInUser");
        
        if (semesterDao.getSemester(name, user.getId()) != null) {
            LOGGER.log(Level.WARNING, "Lỗi trùng name: {0}", "Tên của kỳ học đã tồn tại. Vui lòng chọn giá trị khác.");
            request.setAttribute("errorMessage", "Tên của kỳ học đã tồn tại. Vui lòng chọn giá trị khác.");   
            
            // Giữ lại dữ liệu đã nhập (nếu muốn)
            request.setAttribute("formName", name);
            request.setAttribute("formStartDate", startDate);
            request.setAttribute("formEndDate", endDate);
            request.setAttribute("formStatus", status);
            request.setAttribute("formDescription", description);

            request.getRequestDispatcher("/components/semester/semester-add.jsp").forward(request, response);
            return;
        }
        
        semesterDao.insertSemester(new Semester(name, startDate, endDate, status, LocalDateTime.now(),LocalDateTime.now(), description, user.getId()));
        response.sendRedirect(request.getContextPath() + "/semesters");   
    }
}
