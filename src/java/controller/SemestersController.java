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
            // Nếu không có session hoặc người dùng chưa đăng nhập, chuyển hướng về trang login
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return; // Dừng xử lý request
        }
        
        User user = (User) session.getAttribute("loggedInUser");
        
        switch (action == null ? "" : action) {
            case "/add":
                request.getRequestDispatcher("/components/semester/semester-add.jsp").forward(request, response);
                break;
            case "/edit":
                displayEditSemester(request, response, user); // Gọi phương thức hiển thị form chỉnh sửa
                break;
            case "/delete":
                deleteSemester(request, response, user); // Gọi phương thức xóa
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
            case "/update": // Sửa từ /edit sang /update theo action của form JSP
                editSemester(request, response);
                break;
            case "/delete":
                // Xử lý delete qua POST (nếu muốn, nhưng GET thường dùng cho delete đơn giản)
                // deleteSemester(request, response, (User) request.getSession().getAttribute("loggedInUser"));
                break;
            default:
                // Xử lý mặc định hoặc thông báo lỗi nếu action không hợp lệ
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
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
            LOGGER.log(Level.WARNING, "Lỗi khi convert startDate hoặc endDate: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Định dạng ngày không hợp lệ.");
        }

        // Thiết lập giá trị mặc định cho phân trang
        int currentPage = 1;
        int recordsPerPage = 10; // Số kỳ học mỗi trang

        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                currentPage = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                currentPage = 1; // Mặc định về trang 1 nếu page không hợp lệ
                LOGGER.log(Level.WARNING, "Invalid page number format: {0}", pageStr);
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
        paginationParams.put("search", search != null ? search : "");
        paginationParams.put("status", statusFilter != null ? statusFilter : "");
        paginationParams.put("startDate", startDateStr != null ? startDateStr : "");
        paginationParams.put("endDate", endDateStr != null ? endDateStr : "");
        request.setAttribute("paginationParams", paginationParams);
        request.setAttribute("baseUrl", request.getContextPath() + "/semesters/dashboard");

        // 6. Chuyển tiếp đến JSP
        request.getRequestDispatcher("/components/semester/semester-dashboard.jsp").forward(request, response);
    }

    private void displayEditSemester(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        try {
            int editId = Integer.parseInt(request.getParameter("id"));
            Semester s = semesterDao.getSemesterById(editId, user.getId());
            if (s != null) {
                request.setAttribute("semester", s);
                request.getRequestDispatcher("/components/semester/semester-edit.jsp").forward(request, response);
            } else {
                // Không tìm thấy kỳ học, có thể chuyển hướng hoặc hiển thị lỗi
                LOGGER.log(Level.WARNING, "Semester with ID {0} not found for user {1}.", new Object[]{editId, user.getId()});
                request.setAttribute("errorMessage", "Không tìm thấy kỳ học để chỉnh sửa.");
                // Chuyển hướng về dashboard hoặc trang lỗi
                request.getRequestDispatcher("/components/semester/semester-dashboard.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid semester ID format: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID kỳ học không hợp lệ.");
            request.getRequestDispatcher("/components/semester/semester-dashboard.jsp").forward(request, response);
        }
    }

    private void addSemester(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String status = request.getParameter("status");
        String description = request.getParameter("description");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        User user = (User) session.getAttribute("loggedInUser");
        
        Map<String, String> errors = new HashMap<>();

        // Server-side validation for Add Semester
        if (name == null || name.trim().isEmpty() || name.length() > 100) {
            errors.put("name", "Tên kỳ học không được để trống và không quá 100 ký tự.");
        }
        if (startDateStr == null || startDateStr.trim().isEmpty()) {
            errors.put("startDate", "Ngày bắt đầu không được để trống.");
        }
        if (endDateStr == null || endDateStr.trim().isEmpty()) {
            errors.put("endDate", "Ngày kết thúc không được để trống.");
        }
        if (status == null || status.trim().isEmpty()) {
            errors.put("status", "Trạng thái không được để trống.");
        }
        if (description != null && description.length() > 300) {
            errors.put("description", "Mô tả không được vượt quá 300 ký tự.");
        }
        
        Date startDate = null;
        Date endDate = null;
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                 startDate = Date.valueOf(startDateStr);
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = Date.valueOf(endDateStr);
            }
           
            if (startDate != null && endDate != null && endDate.before(startDate)) {
                errors.put("endDate", "Ngày kết thúc phải sau ngày bắt đầu.");
            }
        } catch (IllegalArgumentException e) {
            errors.put("date", "Định dạng ngày không hợp lệ.");
            LOGGER.log(Level.WARNING, "Lỗi định dạng ngày khi thêm kỳ học: {0}", e.getMessage());
        }


        // Check for duplicate name for the current user
        if (name != null && !name.trim().isEmpty() && semesterDao.getSemester(name, user.getId()) != null) {
            errors.put("name", "Tên của kỳ học đã tồn tại. Vui lòng chọn giá trị khác.");
        }

        if (!errors.isEmpty()) {
            // Nếu có lỗi, thiết lập các thuộc tính để hiển thị trên JSP
            request.setAttribute("errors", errors); // Dùng Map để lưu nhiều lỗi
            request.setAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập."); // Thông báo lỗi chung
            
            // Giữ lại dữ liệu đã nhập
            request.setAttribute("formName", name);
            request.setAttribute("formStartDate", startDateStr); // Truyền lại dạng String để input type=date hiển thị đúng
            request.setAttribute("formEndDate", endDateStr);     // Truyền lại dạng String
            request.setAttribute("formStatus", status);
            request.setAttribute("formDescription", description);

            request.getRequestDispatcher("/components/semester/semester-add.jsp").forward(request, response);
            return;
        }
        
        // Nếu không có lỗi, tiến hành thêm kỳ học
        semesterDao.insertSemester(new Semester(name, startDate, endDate, status, LocalDateTime.now(),LocalDateTime.now(), description, user.getId()));
        response.sendRedirect(request.getContextPath() + "/semesters");   
    }

    private void editSemester(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String status = request.getParameter("status");
        String description = request.getParameter("description");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        User user = (User) session.getAttribute("loggedInUser");
        
        int semesterId = -1;
        Map<String, String> errors = new HashMap<>();

        try {
            semesterId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            errors.put("id", "ID kỳ học không hợp lệ.");
            LOGGER.log(Level.WARNING, "Invalid semester ID format for edit: {0}", idStr);
        }

        // Server-side validation for Edit Semester
        if (name == null || name.trim().isEmpty() || name.length() > 100) {
            errors.put("name", "Tên kỳ học không được để trống và không quá 100 ký tự.");
        }
        if (startDateStr == null || startDateStr.trim().isEmpty()) {
            errors.put("startDate", "Ngày bắt đầu không được để trống.");
        }
        if (endDateStr == null || endDateStr.trim().isEmpty()) {
            errors.put("endDate", "Ngày kết thúc không được để trống.");
        }
        if (status == null || status.trim().isEmpty()) {
            errors.put("status", "Trạng thái không được để trống.");
        }
        if (description != null && description.length() > 300) {
            errors.put("description", "Mô tả không được vượt quá 300 ký tự.");
        }

        Date startDate = null;
        Date endDate = null;
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = Date.valueOf(startDateStr);
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = Date.valueOf(endDateStr);
            }
            if (startDate != null && endDate != null && endDate.before(startDate)) {
                errors.put("endDate", "Ngày kết thúc phải sau ngày bắt đầu.");
            }
        } catch (IllegalArgumentException e) {
            errors.put("date", "Định dạng ngày không hợp lệ.");
            LOGGER.log(Level.WARNING, "Lỗi định dạng ngày khi sửa kỳ học: {0}", e.getMessage());
        }

        // Check for duplicate name, excluding the current semester's ID
        if (name != null && !name.trim().isEmpty() && semesterId != -1) {
            if (semesterDao.getSemesterByNameExceptId(name, semesterId, user.getId()) != null) {
                errors.put("name", "Tên của kỳ học đã tồn tại. Vui lòng chọn giá trị khác.");
            }
        }

        if (!errors.isEmpty()) {
            // Nếu có lỗi, thiết lập các thuộc tính để hiển thị trên JSP
            request.setAttribute("errors", errors);
            request.setAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập.");
            
            // Giữ lại dữ liệu đã nhập bằng cách tạo lại đối tượng Semester
            Semester currentSemester = new Semester(semesterId, name, startDate, endDate, status, null, null, description, user.getId());
            request.setAttribute("semester", currentSemester); // Truyền đối tượng semester đã sửa đổi để giữ lại giá trị trên form
            
            request.getRequestDispatcher("/components/semester/semester-edit.jsp").forward(request, response);
            return;
        }

        // Nếu không có lỗi, tiến hành cập nhật kỳ học
        Semester updatedSemester = new Semester(semesterId, name, startDate, endDate, status, null, LocalDateTime.now(), description, user.getId());
        boolean success = semesterDao.updateSemester(updatedSemester);

        if (success) {
            response.sendRedirect(request.getContextPath() + "/semesters");
        } else {
            LOGGER.log(Level.SEVERE, "Failed to update semester with ID: {0}", semesterId);
            request.setAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật kỳ học. Vui lòng thử lại.");
            // Giữ lại dữ liệu đã nhập
            Semester currentSemester = new Semester(semesterId, name, startDate, endDate, status, null, null, description, user.getId());
            request.setAttribute("semester", currentSemester);
            request.getRequestDispatcher("/components/semester/semester-edit.jsp").forward(request, response);
        }
    }
    
    private void deleteSemester(HttpServletRequest request, HttpServletResponse response, User user) throws IOException, ServletException {
        try {
            int deleteId = Integer.parseInt(request.getParameter("id"));
            boolean success = semesterDao.deleteSemester(deleteId, user.getId());
            if (success) {
                LOGGER.log(Level.INFO, "Semester with ID {0} deleted successfully by user {1}.", new Object[]{deleteId, user.getId()});
                response.sendRedirect(request.getContextPath() + "/semesters");
            } else {
                LOGGER.log(Level.WARNING, "Failed to delete semester with ID {0} for user {1}.", new Object[]{deleteId, user.getId()});
                request.setAttribute("errorMessage", "Không thể xóa kỳ học. Có thể kỳ học không tồn tại hoặc bạn không có quyền.");
                // Quay lại dashboard với thông báo lỗi
                displayDashboard(request, response, user);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid semester ID format for delete: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID kỳ học không hợp lệ.");
            displayDashboard(request, response, user);
        }
    }
}