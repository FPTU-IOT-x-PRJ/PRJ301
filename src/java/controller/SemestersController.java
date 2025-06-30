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
 * Controller xử lý các thao tác liên quan đến kỳ học (Semester).
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
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        User user = (User) session.getAttribute("loggedInUser");
        
        try {
            switch (action == null ? "" : action) {
                case "/add":
                    displayAddForm(request, response, user.getId());
                    break;
                case "/edit":
                    displayEditForm(request, response, user.getId());
                    break;
                case "/delete-confirm":
                    displayDeleteConfirm(request, response, user.getId());
                    break;
                case "/detail": // Giả định bạn có thể muốn xem chi tiết một kỳ học
                    displaySemesterDetail(request, response, user.getId());
                    break;
                default: // Mặc định là hiển thị danh sách/dashboard
                    displaySemesters(request, response, user.getId());
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong SemestersController doGet: " + action, e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "Action received in SemestersController (POST): {0}", action);

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        User user = (User) session.getAttribute("loggedInUser"); 
        request.setCharacterEncoding("UTF-8"); // Đảm bảo nhận tiếng Việt
        response.setCharacterEncoding("UTF-8"); // Đảm bảo gửi tiếng Việt

        try {
            switch (action == null ? "" : action) {
                case "/add":
                    addSemester(request, response, user.getId());
                    break;
                case "/edit":
                    editSemester(request, response, user.getId());
                    break;
                case "/delete":
                    deleteSemester(request, response, user.getId()); 
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action không hợp lệ");
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong SemestersController doPost: " + action, e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Hiển thị form xác nhận xóa kỳ học.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void displayDeleteConfirm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Semester semester = semesterDao.getSemesterById(id, userId); 
            if (semester != null) {
                request.setAttribute("semesterToDelete", semester);
                request.getRequestDispatcher("/components/semester/semester-delete-confirm.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Không tìm thấy kỳ học bạn muốn xóa.");
                response.sendRedirect(request.getContextPath() + "/semesters/display"); 
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID kỳ học không hợp lệ để xác nhận xóa", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID kỳ học không hợp lệ.");
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy kỳ học để xác nhận xóa", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Có lỗi xảy ra khi lấy thông tin kỳ học.");
        }
    }

    /**
     * Hiển thị danh sách các kỳ học cho người dùng hiện tại, có hỗ trợ tìm kiếm và phân trang.
     * Tên hàm cũ: `displayDashboard`
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void displaySemesters(HttpServletRequest request, HttpServletResponse response, int userId) throws ServletException, IOException {
        String search = request.getParameter("search");
        String statusFilter = request.getParameter("status"); 
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String pageStr = request.getParameter("page");

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
            LOGGER.log(Level.WARNING, "Lỗi khi chuyển đổi startDate hoặc endDate: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Định dạng ngày không hợp lệ.");
        }

        int currentPage = 1;
        int recordsPerPage = 10; 

        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                currentPage = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                currentPage = 1; 
                LOGGER.log(Level.WARNING, "Định dạng số trang không hợp lệ: {0}", pageStr);
            }
        }

        int offset = (currentPage - 1) * recordsPerPage;

        // Sử dụng các phương thức mới trong DAO
        int totalSemesters = semesterDao.countSemesters(search, statusFilter, startDate, endDate, userId);
        List<Semester> semesterList = semesterDao.getAllSemesters(search, statusFilter, startDate, endDate, offset, recordsPerPage, userId);

        int totalPages = (int) Math.ceil((double) totalSemesters / recordsPerPage);

        request.setAttribute("semesterList", semesterList);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalSemestersCount", totalSemesters); 

        Map<String, String> paginationParams = new HashMap<>();
        paginationParams.put("search", search != null ? search : "");
        paginationParams.put("status", statusFilter != null ? statusFilter : "");
        paginationParams.put("startDate", startDateStr != null ? startDateStr : "");
        paginationParams.put("endDate", endDateStr != null ? endDateStr : "");
        request.setAttribute("paginationParams", paginationParams);
        request.setAttribute("baseUrl", request.getContextPath() + "/semesters"); // Điều chỉnh baseUrl

        request.getRequestDispatcher("/components/semester/semester-dashboard.jsp").forward(request, response);
    }
    
    /**
     * Hiển thị chi tiết của một kỳ học.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void displaySemesterDetail(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Semester semester = semesterDao.getSemesterById(id, userId);
            if (semester != null) {
                request.setAttribute("semester", semester);
                request.getRequestDispatcher("/components/semester/semester-detail.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Không tìm thấy kỳ học bạn muốn xem chi tiết hoặc bạn không có quyền truy cập.");
                displaySemesters(request, response, userId); // Quay lại trang danh sách
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID kỳ học không hợp lệ để xem chi tiết: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID kỳ học không hợp lệ.");
            displaySemesters(request, response, userId);
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
             LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi hiển thị chi tiết kỳ học: {0}", e.getMessage());
             request.setAttribute("errorMessage", "Lỗi cơ sở dữ liệu.");
             displaySemesters(request, response, userId);
        }
    }

    /**
     * Hiển thị form chỉnh sửa kỳ học.
     * Tên hàm cũ: `displayEditSemester`
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void displayEditForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        try {
            int editId = Integer.parseInt(request.getParameter("id"));
            Semester s = semesterDao.getSemesterById(editId, userId);
            if (s != null) {
                request.setAttribute("semester", s);
                request.getRequestDispatcher("/components/semester/semester-edit.jsp").forward(request, response);
            } else {
                LOGGER.log(Level.WARNING, "Kỳ học với ID {0} không tìm thấy cho người dùng {1}.", new Object[]{editId, userId});
                request.setAttribute("errorMessage", "Không tìm thấy kỳ học để chỉnh sửa.");
                displaySemesters(request, response, userId); // Quay lại dashboard
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID kỳ học không hợp lệ: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID kỳ học không hợp lệ.");
            displaySemesters(request, response, userId);
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi hiển thị form chỉnh sửa kỳ học: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Lỗi cơ sở dữ liệu.");
            displaySemesters(request, response, userId);
        }
    }
    
    /**
     * Hiển thị form để thêm kỳ học mới.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void displayAddForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        // Không cần dữ liệu đặc biệt nào để hiển thị form thêm mới, chỉ cần chuyển hướng
        request.getRequestDispatcher("/components/semester/semester-add.jsp").forward(request, response);
    }

    /**
     * Xử lý logic thêm kỳ học mới.
     * Tên hàm cũ: `addSemester`
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void addSemester(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String status = request.getParameter("status");
        String description = request.getParameter("description");
        
        Map<String, String> errors = new HashMap<>();

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

        // Kiểm tra tên trùng lặp cho người dùng hiện tại
        // semesterDao.getSemesterByName đã tự xử lý SQLException
        if (name != null && !name.trim().isEmpty() && semesterDao.getSemesterByName(name, userId) != null) {
            errors.put("name", "Tên của kỳ học đã tồn tại. Vui lòng chọn giá trị khác.");
        }
        
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors); 
            request.setAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập.");
            
            request.setAttribute("formName", name);
            request.setAttribute("formStartDate", startDateStr); 
            request.setAttribute("formEndDate", endDateStr);     
            request.setAttribute("formStatus", status);
            request.setAttribute("formDescription", description);

            request.getRequestDispatcher("/components/semester/semester-add.jsp").forward(request, response);
            return;
        }
        
        try {
            semesterDao.addSemester(new Semester(name, startDate, endDate, status, LocalDateTime.now(),LocalDateTime.now(), description, userId));
            response.sendRedirect(request.getContextPath() + "/semesters/display");   
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi thêm kỳ học: {0}", e.getMessage());
            errors.put("general", "Lỗi cơ sở dữ liệu khi thêm kỳ học.");
            request.setAttribute("errors", errors);
            request.setAttribute("formName", name);
            request.setAttribute("formStartDate", startDateStr); 
            request.setAttribute("formEndDate", endDateStr);     
            request.setAttribute("formStatus", status);
            request.setAttribute("formDescription", description);
            request.getRequestDispatcher("/components/semester/semester-add.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý logic cập nhật kỳ học.
     * Tên hàm cũ: `editSemester`
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void editSemester(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String status = request.getParameter("status");
        String description = request.getParameter("description");
        
        int semesterId = -1;
        Map<String, String> errors = new HashMap<>();

        try {
            semesterId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            errors.put("id", "ID kỳ học không hợp lệ.");
            LOGGER.log(Level.WARNING, "Định dạng ID kỳ học không hợp lệ để chỉnh sửa: {0}", idStr);
        }

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

        // Kiểm tra tên trùng lặp, loại trừ ID của kỳ học hiện tại
        // semesterDao.getSemesterByNameExceptId đã tự xử lý SQLException
        if (name != null && !name.trim().isEmpty() && semesterId != -1) {
            if (semesterDao.getSemesterByNameExceptId(name, semesterId, userId) != null) {
                errors.put("name", "Tên của kỳ học đã tồn tại. Vui lòng chọn giá trị khác.");
            }
        }
        

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập.");
            
            Semester currentSemester = new Semester(semesterId, name, startDate, endDate, status, null, null, description, userId);
            request.setAttribute("semester", currentSemester); 
            
            request.getRequestDispatcher("/components/semester/semester-edit.jsp").forward(request, response);
            return;
        }

        try {
            Semester updatedSemester = new Semester(semesterId, name, startDate, endDate, status, null, LocalDateTime.now(), description, userId);
            boolean success = semesterDao.editSemester(updatedSemester); // Sử dụng editSemester

            if (success) {
                response.sendRedirect(request.getContextPath() + "/semesters/display");
            } else {
                LOGGER.log(Level.SEVERE, "Không thể cập nhật kỳ học với ID: {0}", semesterId);
                request.setAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật kỳ học. Vui lòng thử lại.");
                Semester currentSemester = new Semester(semesterId, name, startDate, endDate, status, null, null, description, userId);
                request.setAttribute("semester", currentSemester);
                request.getRequestDispatcher("/components/semester/semester-edit.jsp").forward(request, response);
            }
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi cập nhật kỳ học: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Lỗi cơ sở dữ liệu khi cập nhật kỳ học.");
            Semester currentSemester = new Semester(semesterId, name, startDate, endDate, status, null, null, description, userId);
            request.setAttribute("semester", currentSemester);
            request.getRequestDispatcher("/components/semester/semester-edit.jsp").forward(request, response);
        }
    }
    
    /**
     * Xử lý logic xóa kỳ học.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws IOException
     * @throws ServletException
     */
    private void deleteSemester(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, ServletException {
        try {
            int deleteId = Integer.parseInt(request.getParameter("id"));
            boolean success = semesterDao.deleteSemester(deleteId, userId); // Sử dụng deleteSemester
            if (success) {
                LOGGER.log(Level.INFO, "Kỳ học với ID {0} đã xóa thành công bởi người dùng {1}.", new Object[]{deleteId, userId});
                response.sendRedirect(request.getContextPath() + "/semesters/display?message=deleteSuccess");
            } else {
                LOGGER.log(Level.WARNING, "Không thể xóa kỳ học với ID {0} cho người dùng {1}.", new Object[]{deleteId, userId});
                request.setAttribute("errorMessage", "Không thể xóa kỳ học. Có thể kỳ học không tồn tại hoặc bạn không có quyền.");
                displaySemesters(request, response, userId); // Quay lại dashboard với thông báo lỗi
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID kỳ học không hợp lệ để xóa: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID kỳ học không hợp lệ.");
            displaySemesters(request, response, userId);
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa kỳ học.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi xóa kỳ học.");
            displaySemesters(request, response, userId);
        }
    }
}
