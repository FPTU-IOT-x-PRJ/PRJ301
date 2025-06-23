package controller;

import dao.LessonDAO;
import dao.SubjectDAO;
import entity.Lesson;
import entity.Subject;
import entity.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet; // Để dùng @WebServlet
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Date; // Sử dụng cho lessonDate
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet Controller để quản lý các buổi học (Lessons).
 *
 * @author Dung Ann
 */
@WebServlet(name = "LessonsController", urlPatterns = {"/lessons", "/lessons/*"})
public class LessonsController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LessonsController.class.getName());
    private LessonDAO lessonDao = new LessonDAO();
    private SubjectDAO subjectDao = new SubjectDAO(); // Cần để lấy thông tin Subject

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo(); // Lấy phần sau /lessons
        HttpSession session = request.getSession(false);

        // Kiểm tra đăng nhập
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        User user = (User) session.getAttribute("loggedInUser");

        try {
            if (action == null || action.equals("/")) {
                displayLessons(request, response, user);
            } else if (action.equals("/add")) {
                showAddForm(request, response, user);
            } else if (action.equals("/edit")) {
                showEditForm(request, response, user);
            } else if (action.equals("/delete")) {
                deleteLesson(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action Not Found");
            }
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.SEVERE, "Error in LessonsController doGet: " + action, e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        HttpSession session = request.getSession(false);

        // Kiểm tra đăng nhập
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        User user = (User) session.getAttribute("loggedInUser");

        try {
            if (action == null || action.equals("/") || action.equals("/add")) { // Gửi form add
                addLesson(request, response, user);
            } else if (action.equals("/edit")) {
                updateLesson(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action Not Found");
            }
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.SEVERE, "Error in LessonsController doPost: " + action, e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Hiển thị danh sách các buổi học cho một môn học cụ thể.
     * Yêu cầu tham số `subjectId`.
     */
    private void displayLessons(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String subjectIdStr = request.getParameter("subjectId");
        if (subjectIdStr == null || subjectIdStr.isEmpty()) {
            LOGGER.log(Level.WARNING, "Missing subjectId parameter for displaying lessons.");
            request.setAttribute("errorMessage", "ID môn học không được cung cấp.");
            request.getRequestDispatcher("/error.jsp").forward(request, response); // Chuyển hướng đến trang lỗi hoặc trang môn học
            return;
        }

        try {
            int subjectId = Integer.parseInt(subjectIdStr);
            Subject subject = subjectDao.getSubjectById(subjectId);

            if (subject == null) {
                LOGGER.log(Level.WARNING, "Subject with ID {0} not found for user {1}.", new Object[]{subjectId, user.getId()});
                request.setAttribute("errorMessage", "Môn học không tồn tại.");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }

            // Phân trang, tìm kiếm, lọc
            String search = request.getParameter("search");
            String statusFilter = request.getParameter("status");
            int page = 1;
            int pageSize = 10; // Có thể cấu hình

            try {
                if (request.getParameter("page") != null) {
                    page = Integer.parseInt(request.getParameter("page"));
                }
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid page number format: {0}", request.getParameter("page"));
            }

            List<Lesson> lessons = lessonDao.getFilteredLessonsBySubjectId(subjectId, search, statusFilter, page, pageSize);
            int totalLessons = lessonDao.countLessonsBySubjectId(subjectId, search, statusFilter);
            int totalPages = (int) Math.ceil((double) totalLessons / pageSize);

            request.setAttribute("subject", subject); // Để hiển thị tên môn học
            request.setAttribute("lessons", lessons);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("search", search);
            request.setAttribute("statusFilter", statusFilter); // Giữ lại giá trị filter

            request.getRequestDispatcher("/components/lesson/lesson-list.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid subject ID format: {0}", subjectIdStr);
            request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Hiển thị form thêm buổi học mới.
     * Yêu cầu tham số `subjectId`.
     */
    private void showAddForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String subjectIdStr = request.getParameter("subjectId");
        if (subjectIdStr == null || subjectIdStr.isEmpty()) {
            LOGGER.log(Level.WARNING, "Missing subjectId parameter for add lesson form.");
            request.setAttribute("errorMessage", "ID môn học không được cung cấp để thêm buổi học.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }
        try {
            int subjectId = Integer.parseInt(subjectIdStr);
            Subject subject = subjectDao.getSubjectById(subjectId);
            if (subject == null) {
                LOGGER.log(Level.WARNING, "Subject with ID {0} not found for adding lesson.", subjectId);
                request.setAttribute("errorMessage", "Môn học không tồn tại để thêm buổi học.");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }
            request.setAttribute("subject", subject);
            request.getRequestDispatcher("/components/lesson/lesson-add.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid subject ID format for add lesson form: {0}", subjectIdStr);
            request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý yêu cầu thêm buổi học mới.
     */
    private void addLesson(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();
        int subjectId = 0; // Khởi tạo để dùng trong phần catch và redirect
        try {
            String subjectIdStr = request.getParameter("subjectId");
            if (subjectIdStr == null || subjectIdStr.isEmpty()) {
                 errors.put("general", "ID môn học không được cung cấp.");
            } else {
                subjectId = Integer.parseInt(subjectIdStr);
                Subject subject = subjectDao.getSubjectById(subjectId);
                if (subject == null) {
                    errors.put("general", "Môn học không tồn tại.");
                } else {
                    request.setAttribute("subject", subject); // Để hiển thị lại trên form
                }
            }
            
            String name = request.getParameter("name");
            String lessonDateStr = request.getParameter("lessonDate");
            String description = request.getParameter("description");
            String status = request.getParameter("status");

            // Validate inputs
            if (name == null || name.trim().isEmpty()) {
                errors.put("name", "Tên buổi học không được để trống.");
            }
            Date lessonDate = null;
            if (lessonDateStr == null || lessonDateStr.trim().isEmpty()) {
                errors.put("lessonDate", "Ngày buổi học không được để trống.");
            } else {
                try {
                    lessonDate = Date.valueOf(lessonDateStr);
                } catch (IllegalArgumentException e) {
                    errors.put("lessonDate", "Định dạng ngày không hợp lệ. Vui lòng sử dụng YYYY-MM-DD.");
                }
            }
            if (description == null || description.trim().isEmpty()) {
                errors.put("description", "Mô tả không được để trống.");
            }
            if (status == null || status.trim().isEmpty()) {
                errors.put("status", "Trạng thái không được để trống.");
            }


            if (!errors.isEmpty()) {
                // Giữ lại các giá trị form đã nhập để hiển thị lại
                request.setAttribute("errors", errors);
                request.setAttribute("formName", name);
                request.setAttribute("formLessonDate", lessonDateStr);
                request.setAttribute("formDescription", description);
                request.setAttribute("formStatus", status);
                request.getRequestDispatcher("/components/lesson/lesson-add.jsp").forward(request, response);
                return;
            }

            Lesson newLesson = new Lesson(subjectId, name, lessonDate, description, status, LocalDateTime.now(), LocalDateTime.now());
            boolean success = lessonDao.addLesson(newLesson);

            if (success) {
                LOGGER.log(Level.INFO, "Lesson '{0}' added successfully for subject ID {1} by user {2}.", new Object[]{name, subjectId, user.getId()});
                response.sendRedirect(request.getContextPath() + "/lessons?subjectId=" + subjectId + "&message=addSuccess");
            } else {
                errors.put("general", "Có lỗi xảy ra khi thêm buổi học.");
                request.setAttribute("errors", errors);
                // Giữ lại các giá trị form đã nhập nếu có lỗi
                request.setAttribute("formName", name);
                request.setAttribute("formLessonDate", lessonDateStr);
                request.setAttribute("formDescription", description);
                request.setAttribute("formStatus", status);
                request.getRequestDispatcher("/components/lesson/lesson-add.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid subjectId format when adding lesson: {0}", request.getParameter("subjectId"));
            errors.put("general", "ID môn học không hợp lệ.");
            request.setAttribute("errors", errors);
            // Vẫn cần subject để load lại form
            try {
                if (subjectId > 0) {
                    request.setAttribute("subject", subjectDao.getSubjectById(subjectId));
                }
            } catch (Exception ex) {
                 LOGGER.log(Level.SEVERE, "Error retrieving subject for error page.", ex);
            }
            request.getRequestDispatcher("/components/lesson/lesson-add.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding lesson.", e);
            errors.put("general", "Đã xảy ra lỗi không mong muốn khi thêm buổi học.");
            request.setAttribute("errors", errors);
            // Giữ lại các giá trị form đã nhập
            request.setAttribute("formName", request.getParameter("name"));
            request.setAttribute("formLessonDate", request.getParameter("lessonDate"));
            request.setAttribute("formDescription", request.getParameter("description"));
            request.setAttribute("formStatus", request.getParameter("status"));
            try {
                if (subjectId > 0) {
                    request.setAttribute("subject", subjectDao.getSubjectById(subjectId));
                }
            } catch (Exception ex) {
                 LOGGER.log(Level.SEVERE, "Error retrieving subject for error page.", ex);
            }
            request.getRequestDispatcher("/components/lesson/lesson-add.jsp").forward(request, response);
        }
    }

    /**
     * Hiển thị form chỉnh sửa buổi học.
     * Yêu cầu tham số `id` (lessonId) và `subjectId`.
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String lessonIdStr = request.getParameter("id");
        String subjectIdStr = request.getParameter("subjectId");

        if (lessonIdStr == null || lessonIdStr.isEmpty() || subjectIdStr == null || subjectIdStr.isEmpty()) {
            LOGGER.log(Level.WARNING, "Missing lessonId or subjectId parameter for edit lesson form.");
            request.setAttribute("errorMessage", "Thông tin buổi học hoặc môn học không đầy đủ để chỉnh sửa.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }

        try {
            int lessonId = Integer.parseInt(lessonIdStr);
            int subjectId = Integer.parseInt(subjectIdStr);

            Lesson lesson = lessonDao.getLessonById(lessonId);
            Subject subject = subjectDao.getSubjectById(subjectId);

            if (lesson == null || subject == null) {
                LOGGER.log(Level.WARNING, "Lesson ID {0} or Subject ID {1} not found for editing.", new Object[]{lessonId, subjectId});
                request.setAttribute("errorMessage", "Buổi học hoặc môn học không tồn tại.");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }
            
            // Đảm bảo buổi học thuộc môn học đã chọn (tùy chọn, nhưng nên kiểm tra)
            if (lesson.getSubjectId() != subjectId) {
                 LOGGER.log(Level.WARNING, "Lesson ID {0} does not belong to Subject ID {1}.", new Object[]{lessonId, subjectId});
                 request.setAttribute("errorMessage", "Buổi học không thuộc môn học này.");
                 request.getRequestDispatcher("/error.jsp").forward(request, response);
                 return;
            }

            request.setAttribute("lesson", lesson);
            request.setAttribute("subject", subject);
            request.getRequestDispatcher("/components/lesson/lesson-edit.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid lesson ID ({0}) or subject ID ({1}) format for edit lesson form.", new Object[]{lessonIdStr, subjectIdStr});
            request.setAttribute("errorMessage", "ID buổi học hoặc ID môn học không hợp lệ.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý yêu cầu cập nhật buổi học.
     */
    private void updateLesson(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();
        int lessonId = 0;
        int subjectId = 0;
        try {
            String lessonIdStr = request.getParameter("id");
            String subjectIdStr = request.getParameter("subjectId");

            if (lessonIdStr == null || lessonIdStr.isEmpty() || subjectIdStr == null || subjectIdStr.isEmpty()) {
                errors.put("general", "Thông tin buổi học hoặc môn học không đầy đủ để cập nhật.");
            } else {
                lessonId = Integer.parseInt(lessonIdStr);
                subjectId = Integer.parseInt(subjectIdStr);

                // Lấy lesson cũ để so sánh hoặc giữ lại nếu có lỗi
                Lesson existingLesson = lessonDao.getLessonById(lessonId);
                Subject subject = subjectDao.getSubjectById(subjectId);

                if (existingLesson == null || subject == null) {
                    errors.put("general", "Buổi học hoặc môn học không tồn tại.");
                } else {
                    request.setAttribute("lesson", existingLesson); // Để hiển thị lại nếu có lỗi
                    request.setAttribute("subject", subject);
                    // Đảm bảo buổi học thuộc môn học đã chọn
                    if (existingLesson.getSubjectId() != subjectId) {
                         errors.put("general", "Buổi học không thuộc môn học này.");
                    }
                }
            }

            String name = request.getParameter("name");
            String lessonDateStr = request.getParameter("lessonDate");
            String description = request.getParameter("description");
            String status = request.getParameter("status");

            // Validate inputs
            if (name == null || name.trim().isEmpty()) {
                errors.put("name", "Tên buổi học không được để trống.");
            }
            Date lessonDate = null;
            if (lessonDateStr == null || lessonDateStr.trim().isEmpty()) {
                errors.put("lessonDate", "Ngày buổi học không được để trống.");
            } else {
                try {
                    lessonDate = Date.valueOf(lessonDateStr);
                } catch (IllegalArgumentException e) {
                    errors.put("lessonDate", "Định dạng ngày không hợp lệ. Vui lòng sử dụng YYYY-MM-DD.");
                }
            }
            if (description == null || description.trim().isEmpty()) {
                errors.put("description", "Mô tả không được để trống.");
            }
            if (status == null || status.trim().isEmpty()) {
                errors.put("status", "Trạng thái không được để trống.");
            }


            if (!errors.isEmpty()) {
                request.setAttribute("errors", errors);
                // Giữ lại các giá trị form đã nhập để hiển thị lại
                request.setAttribute("formName", name);
                request.setAttribute("formLessonDate", lessonDateStr);
                request.setAttribute("formDescription", description);
                request.setAttribute("formStatus", status);
                request.getRequestDispatcher("/components/lesson/lesson-edit.jsp").forward(request, response);
                return;
            }

            // Tạo đối tượng Lesson đã cập nhật
            // Cần lấy createdAt từ lesson cũ nếu không muốn thay đổi nó
            Lesson currentLesson = lessonDao.getLessonById(lessonId);
            LocalDateTime createdAt = (currentLesson != null) ? currentLesson.getCreatedAt() : LocalDateTime.now(); // Fallback if currentLesson is null
            
            Lesson updatedLesson = new Lesson(lessonId, subjectId, name, lessonDate, description, status, createdAt, LocalDateTime.now());

            boolean success = lessonDao.updateLesson(updatedLesson);

            if (success) {
                LOGGER.log(Level.INFO, "Lesson with ID {0} updated successfully by user {1}.", new Object[]{lessonId, user.getId()});
                response.sendRedirect(request.getContextPath() + "/lessons?subjectId=" + subjectId + "&message=editSuccess");
            } else {
                errors.put("general", "Có lỗi xảy ra khi cập nhật buổi học.");
                request.setAttribute("errors", errors);
                // Giữ lại các giá trị form đã nhập
                request.setAttribute("formName", name);
                request.setAttribute("formLessonDate", lessonDateStr);
                request.setAttribute("formDescription", description);
                request.setAttribute("formStatus", status);
                request.getRequestDispatcher("/components/lesson/lesson-edit.jsp").forward(request, response);
            }

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid lesson ID ({0}) or subject ID ({1}) format when updating lesson.", new Object[]{request.getParameter("id"), request.getParameter("subjectId")});
            errors.put("general", "ID buổi học hoặc ID môn học không hợp lệ.");
            request.setAttribute("errors", errors);
            // Cố gắng giữ lại subject và lesson (nếu có) để form hiển thị đúng
            try {
                if (lessonId > 0) request.setAttribute("lesson", lessonDao.getLessonById(lessonId));
                if (subjectId > 0) request.setAttribute("subject", subjectDao.getSubjectById(subjectId));
            } catch (Exception ex) {
                 LOGGER.log(Level.SEVERE, "Error retrieving lesson/subject for error page during update.", ex);
            }
            request.getRequestDispatcher("/components/lesson/lesson-edit.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating lesson.", e);
            errors.put("general", "Đã xảy ra lỗi không mong muốn khi cập nhật buổi học.");
            request.setAttribute("errors", errors);
            // Giữ lại các giá trị form đã nhập
            request.setAttribute("formName", request.getParameter("name"));
            request.setAttribute("formLessonDate", request.getParameter("lessonDate"));
            request.setAttribute("formDescription", request.getParameter("description"));
            request.setAttribute("formStatus", request.getParameter("status"));
             try {
                if (lessonId > 0) request.setAttribute("lesson", lessonDao.getLessonById(lessonId));
                if (subjectId > 0) request.setAttribute("subject", subjectDao.getSubjectById(subjectId));
            } catch (Exception ex) {
                 LOGGER.log(Level.SEVERE, "Error retrieving lesson/subject for error page during update.", ex);
            }
            request.getRequestDispatcher("/components/lesson/lesson-edit.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý yêu cầu xóa buổi học.
     */
    private void deleteLesson(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String lessonIdStr = request.getParameter("id");
        String subjectIdStr = request.getParameter("subjectId");

        if (lessonIdStr == null || lessonIdStr.isEmpty() || subjectIdStr == null || subjectIdStr.isEmpty()) {
            LOGGER.log(Level.WARNING, "Missing lessonId or subjectId parameter for deleting lesson.");
            request.setAttribute("errorMessage", "Thông tin buổi học hoặc môn học không đầy đủ để xóa.");
            displayLessons(request, response, user); // Quay lại trang danh sách với lỗi
            return;
        }

        try {
            int deleteId = Integer.parseInt(lessonIdStr);
            int subjectId = Integer.parseInt(subjectIdStr);

            boolean success = lessonDao.deleteLesson(deleteId);

            if (success) {
                LOGGER.log(Level.INFO, "Lesson with ID {0} deleted successfully by user {1}.", new Object[]{deleteId, user.getId()});
                response.sendRedirect(request.getContextPath() + "/lessons?subjectId=" + subjectId + "&message=deleteSuccess");
            } else {
                LOGGER.log(Level.WARNING, "Failed to delete lesson with ID {0} for user {1}. It might not exist or be linked to other data.", new Object[]{deleteId, user.getId()});
                request.setAttribute("errorMessage", "Không thể xóa buổi học. Có thể buổi học không tồn tại hoặc có dữ liệu liên quan.");
                displayLessons(request, response, user); // Quay lại trang danh sách với lỗi
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid lesson ID ({0}) or subject ID ({1}) format for delete.", new Object[]{lessonIdStr, subjectIdStr});
            request.setAttribute("errorMessage", "ID buổi học hoặc ID môn học không hợp lệ.");
            displayLessons(request, response, user); // Quay lại trang danh sách với lỗi
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting lesson.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi xóa buổi học.");
            displayLessons(request, response, user); // Quay lại trang danh sách với lỗi
        }
    }
}