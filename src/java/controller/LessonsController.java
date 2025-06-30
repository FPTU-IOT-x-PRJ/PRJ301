package controller;

import dao.LessonDAO;
import dao.SubjectDAO;
import dao.SemesterDAO; // Import SemesterDAO để kiểm tra quyền truy cập qua semester
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
 * Xử lý các yêu cầu thêm, hiển thị, chỉnh sửa và xóa buổi học.
 */
@WebServlet(name = "LessonsController", urlPatterns = {"/lessons", "/lessons/*"})
public class LessonsController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LessonsController.class.getName());
    private LessonDAO lessonDao;
    private SubjectDAO subjectDao;
    private SemesterDAO semesterDao; // Thêm SemesterDAO

    @Override
    public void init() throws ServletException {
        super.init();
        lessonDao = new LessonDAO();
        subjectDao = new SubjectDAO();
        semesterDao = new SemesterDAO(); // Khởi tạo SemesterDAO
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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
            switch (action == null ? "/" : action) {
                case "/": // Mặc định là hiển thị danh sách buổi học
                    displayLessons(request, response, user.getId());
                    break;
                case "/add": // Hiển thị form thêm buổi học
                    displayAddForm(request, response, user.getId());
                    break;
                case "/edit": // Hiển thị form chỉnh sửa buổi học
                    displayEditForm(request, response, user.getId());
                    break;
                case "/detail": // Hiển thị chi tiết buổi học
                    displayLessonDetail(request, response, user.getId());
                    break;
                case "/delete-confirm": // Hiển thị form xác nhận xóa
                    displayDeleteConfirm(request, response, user.getId());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action không tìm thấy");
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong LessonsController doGet cho action: " + action, e);
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
        request.setCharacterEncoding("UTF-8"); // Đảm bảo nhận tiếng Việt
        response.setCharacterEncoding("UTF-8"); // Đảm bảo gửi tiếng Việt

        try {
            switch (action == null ? "" : action) {
                case "/add": // Xử lý thêm buổi học
                    addLesson(request, response, user.getId());
                    break;
                case "/edit": // Xử lý cập nhật buổi học
                    editLesson(request, response, user.getId());
                    break;
                case "/delete": // Xử lý xóa buổi học chính thức
                    deleteLesson(request, response, user.getId());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action không tìm thấy");
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong LessonsController doPost cho action: " + action, e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Hiển thị chi tiết của một buổi học.
     * Kiểm tra quyền truy cập của người dùng.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng hiện tại
     * @throws ServletException
     * @throws IOException
     */
    private void displayLessonDetail(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        try {
            int lessonId = Integer.parseInt(request.getParameter("id"));
            Lesson lesson = lessonDao.getLessonById(lessonId);

            if (lesson != null) {
                // Kiểm tra xem buổi học có thuộc về môn học và kỳ học của người dùng không
                Subject subject = subjectDao.getSubjectById(lesson.getSubjectId());
                if (subject == null || semesterDao.getSemesterById(subject.getSemesterId(), userId) == null) {
                    request.setAttribute("errorMessage", "Buổi học không tồn tại hoặc bạn không có quyền truy cập.");
                    response.sendRedirect(request.getContextPath() + "/subjects"); // Chuyển về trang subjects
                    return;
                }
                
                request.setAttribute("lesson", lesson);
                request.setAttribute("subject", subject); // Để có thông tin môn học liên quan
                request.getRequestDispatcher("/components/lesson/lesson-detail.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Không tìm thấy buổi học bạn muốn xem chi tiết.");
                String subjectIdParam = request.getParameter("subjectId"); // Cố gắng lấy subjectId để quay về đúng trang
                int subjectId = -1;
                try {
                    if (subjectIdParam != null && !subjectIdParam.isEmpty()) {
                        subjectId = Integer.parseInt(subjectIdParam);
                    }
                } catch (NumberFormatException e) { /* bỏ qua */ }
                
                if (subjectId != -1) {
                    response.sendRedirect(request.getContextPath() + "/lessons?subjectId=" + subjectId + "&errorMessage=" + java.net.URLEncoder.encode("Không tìm thấy buổi học bạn muốn xem chi tiết.", "UTF-8"));
                } else {
                    response.sendRedirect(request.getContextPath() + "/subjects"); // Chuyển về trang Subjects nếu không có subjectId
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID buổi học không hợp lệ để xem chi tiết: {0}", request.getParameter("id"));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID buổi học không hợp lệ.");
        } catch (Exception e) { 
            LOGGER.log(Level.SEVERE, "Đã xảy ra lỗi không mong muốn khi hiển thị chi tiết buổi học", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi không mong muốn.");
        }
    }
    
    /**
     * Hiển thị form xác nhận xóa buổi học.
     * Kiểm tra quyền truy cập của người dùng.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng hiện tại
     * @throws ServletException
     * @throws IOException
     */
    private void displayDeleteConfirm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Lesson lesson = lessonDao.getLessonById(id);
            if (lesson != null) {
                // Kiểm tra xem môn học của buổi học này có thuộc về user hay không
                Subject subjectOfLesson = subjectDao.getSubjectById(lesson.getSubjectId());
                if (subjectOfLesson == null || semesterDao.getSemesterById(subjectOfLesson.getSemesterId(), userId) == null) {
                     request.setAttribute("errorMessage", "Bạn không có quyền xóa buổi học này.");
                     String subjectIdParam = request.getParameter("subjectId");
                     if (subjectIdParam != null && !subjectIdParam.isEmpty()) {
                        response.sendRedirect(request.getContextPath() + "/lessons?subjectId=" + subjectIdParam);
                     } else {
                        response.sendRedirect(request.getContextPath() + "/subjects");
                     }
                     return;
                }
                
                request.setAttribute("lessonToDelete", lesson);
                request.setAttribute("subjectId", lesson.getSubjectId()); // Truyền subjectId để quay về đúng trang
                request.getRequestDispatcher("/components/lesson/lesson-delete-confirm.jsp").forward(request, response);
            } else {
                int subjectId = -1;
                try {
                    subjectId = Integer.parseInt(request.getParameter("subjectId"));
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Subject ID không tìm thấy hoặc không hợp lệ trong yêu cầu xác nhận xóa", e);
                }
                
                request.setAttribute("errorMessage", "Không tìm thấy buổi học bạn muốn xóa.");
                response.sendRedirect(request.getContextPath() + "/lessons?subjectId=" + subjectId); 
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID buổi học không hợp lệ để xác nhận xóa", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID buổi học không hợp lệ.");
        } catch (Exception e) { 
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy buổi học để xác nhận xóa", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Có lỗi xảy ra khi lấy thông tin buổi học.");
        }
    }
    
    /**
     * Hiển thị danh sách các buổi học cho một môn học cụ thể, có hỗ trợ tìm kiếm và phân trang.
     * Kiểm tra quyền truy cập của người dùng.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng hiện tại
     * @throws ServletException
     * @throws IOException
     */
    private void displayLessons(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        String subjectIdStr = request.getParameter("subjectId");
        if (subjectIdStr == null || subjectIdStr.isEmpty()) {
            LOGGER.log(Level.WARNING, "Thiếu tham số subjectId để hiển thị buổi học.");
            request.setAttribute("errorMessage", "ID môn học không được cung cấp.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }

        try {
            int subjectId = Integer.parseInt(subjectIdStr);
            Subject subject = subjectDao.getSubjectById(subjectId);

            // Kiểm tra xem môn học có thuộc về user không (qua semester)
            if (subject == null || semesterDao.getSemesterById(subject.getSemesterId(), userId) == null) {
                LOGGER.log(Level.WARNING, "Môn học với ID {0} không tìm thấy hoặc không thuộc về người dùng {1}.", new Object[]{subjectId, userId});
                request.setAttribute("errorMessage", "Môn học không tồn tại hoặc bạn không có quyền truy cập.");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }

            String search = request.getParameter("search");
            String statusFilter = request.getParameter("status");
            int page = 1;
            int pageSize = 10; 

            try {
                if (request.getParameter("page") != null) {
                    page = Integer.parseInt(request.getParameter("page"));
                }
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Định dạng số trang không hợp lệ: {0}", request.getParameter("page"));
            }

            List<Lesson> lessons = lessonDao.getAllLessonsBySubjectId(subjectId, search, statusFilter, page, pageSize);
            int totalLessons = lessonDao.countLessons(subjectId, search, statusFilter);
            int totalPages = (int) Math.ceil((double) totalLessons / pageSize);

            request.setAttribute("subject", subject); // Đảm bảo có thông tin môn học
            request.setAttribute("lessons", lessons);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("search", search);
            request.setAttribute("statusFilter", statusFilter); 

            request.getRequestDispatcher("/components/lesson/lesson-dashboard.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID môn học không hợp lệ: {0}", subjectIdStr);
            request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        } catch (Exception e) { // Bắt lỗi Exception chung
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi hiển thị buổi học: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Lỗi hệ thống khi tải buổi học.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Hiển thị form thêm buổi học mới.
     * Yêu cầu tham số `subjectId`. Kiểm tra quyền truy cập của người dùng.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng hiện tại
     * @throws ServletException
     * @throws IOException
     */
    private void displayAddForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        String subjectIdStr = request.getParameter("subjectId");
        if (subjectIdStr == null || subjectIdStr.isEmpty()) {
            LOGGER.log(Level.WARNING, "Thiếu tham số subjectId để hiển thị form thêm buổi học.");
            request.setAttribute("errorMessage", "ID môn học không được cung cấp để thêm buổi học.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }
        try {
            int subjectId = Integer.parseInt(subjectIdStr);
            Subject subject = subjectDao.getSubjectById(subjectId);
            
            // Kiểm tra xem môn học có thuộc về user không (qua semester)
            if (subject == null || semesterDao.getSemesterById(subject.getSemesterId(), userId) == null) {
                LOGGER.log(Level.WARNING, "Môn học với ID {0} không tìm thấy hoặc không thuộc về người dùng {1} để thêm buổi học.", new Object[]{subjectId, userId});
                request.setAttribute("errorMessage", "Môn học không tồn tại hoặc bạn không có quyền truy cập.");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }
            request.setAttribute("subject", subject);
            request.getRequestDispatcher("/components/lesson/lesson-add.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID môn học không hợp lệ cho form thêm buổi học: {0}", subjectIdStr);
            request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        } catch (Exception e) { // Bắt lỗi Exception chung
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi hiển thị form thêm buổi học: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Lỗi hệ thống.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý yêu cầu thêm buổi học mới.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng hiện tại
     * @throws ServletException
     * @throws IOException
     */
    private void addLesson(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();
        int subjectId = 0; 
        try {
            String subjectIdStr = request.getParameter("subjectId");
            if (subjectIdStr == null || subjectIdStr.isEmpty()) {
                 errors.put("general", "ID môn học không được cung cấp.");
            } else {
                subjectId = Integer.parseInt(subjectIdStr);
                Subject subject = subjectDao.getSubjectById(subjectId);
                // Kiểm tra quyền truy cập của người dùng đối với môn học này
                if (subject == null || semesterDao.getSemesterById(subject.getSemesterId(), userId) == null) {
                    errors.put("general", "Môn học không tồn tại hoặc bạn không có quyền truy cập.");
                } else {
                    request.setAttribute("subject", subject); 
                }
            }
            
            String name = request.getParameter("name");
            String lessonDateStr = request.getParameter("lessonDate");
            String description = request.getParameter("description");
            String status = request.getParameter("status");

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
            if (status == null || status.trim().isEmpty()) {
                errors.put("status", "Trạng thái không được để trống.");
            }

            if (!errors.isEmpty()) {
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
                LOGGER.log(Level.INFO, "Buổi học '{0}' đã được thêm thành công cho môn học ID {1}.", new Object[]{name, subjectId});
                response.sendRedirect(request.getContextPath() + "/lessons?subjectId=" + subjectId + "&message=addSuccess");
            } else {
                errors.put("general", "Có lỗi xảy ra khi thêm buổi học.");
                request.setAttribute("errors", errors);
                request.setAttribute("formName", name);
                request.setAttribute("formLessonDate", lessonDateStr);
                request.setAttribute("formDescription", description);
                request.setAttribute("formStatus", status);
                request.getRequestDispatcher("/components/lesson/lesson-add.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng subjectId không hợp lệ khi thêm buổi học: {0}", request.getParameter("subjectId"));
            errors.put("general", "ID môn học không hợp lệ.");
            request.setAttribute("errors", errors);
            try {
                if (subjectId > 0) { // Cố gắng lấy lại subject nếu ID có thể parse được
                    request.setAttribute("subject", subjectDao.getSubjectById(subjectId));
                }
            } catch (Exception ex) { // Catch Exception chung
                 LOGGER.log(Level.SEVERE, "Lỗi khi lấy môn học cho trang lỗi.", ex);
            }
            request.getRequestDispatcher("/components/lesson/lesson-add.jsp").forward(request, response);
        } catch (Exception e) { // Bắt lỗi Exception chung
            LOGGER.log(Level.SEVERE, "Lỗi khi thêm buổi học.", e);
            errors.put("general", "Đã xảy ra lỗi không mong muốn khi thêm buổi học.");
            request.setAttribute("errors", errors);
            request.setAttribute("formName", request.getParameter("name"));
            request.setAttribute("formLessonDate", request.getParameter("lessonDate"));
            request.setAttribute("formDescription", request.getParameter("description"));
            request.setAttribute("formStatus", request.getParameter("status"));
            try {
                if (subjectId > 0) { // Cố gắng lấy lại subject nếu ID có thể parse được
                    request.setAttribute("subject", subjectDao.getSubjectById(subjectId));
                }
            } catch (Exception ex) { // Catch Exception chung
                 LOGGER.log(Level.SEVERE, "Lỗi khi lấy môn học cho trang lỗi.", ex);
            }
            request.getRequestDispatcher("/components/lesson/lesson-add.jsp").forward(request, response);
        }
    }

    /**
     * Hiển thị form chỉnh sửa buổi học.
     * Yêu cầu tham số `id` (lessonId) và `subjectId`. Kiểm tra quyền truy cập của người dùng.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng hiện tại
     * @throws ServletException
     * @throws IOException
     */
    private void displayEditForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        String lessonIdStr = request.getParameter("id");
        String subjectIdStr = request.getParameter("subjectId");

        if (lessonIdStr == null || lessonIdStr.isEmpty() || subjectIdStr == null || subjectIdStr.isEmpty()) {
            LOGGER.log(Level.WARNING, "Thiếu tham số lessonId hoặc subjectId cho form chỉnh sửa buổi học.");
            request.setAttribute("errorMessage", "Thông tin buổi học hoặc môn học không đầy đủ để chỉnh sửa.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }

        try {
            int lessonId = Integer.parseInt(lessonIdStr);
            int subjectId = Integer.parseInt(subjectIdStr);

            Lesson lesson = lessonDao.getLessonById(lessonId);
            Subject subject = subjectDao.getSubjectById(subjectId);

            // Kiểm tra quyền truy cập và tính hợp lệ của dữ liệu
            if (lesson == null || subject == null || lesson.getSubjectId() != subjectId || semesterDao.getSemesterById(subject.getSemesterId(), userId) == null) {
                LOGGER.log(Level.WARNING, "Buổi học ID {0} hoặc Môn học ID {1} không tìm thấy, hoặc không hợp lệ, hoặc không thuộc về người dùng {2}.", new Object[]{lessonId, subjectId, userId});
                request.setAttribute("errorMessage", "Buổi học hoặc môn học không tồn tại hoặc bạn không có quyền truy cập.");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }
            
            request.setAttribute("lesson", lesson);
            request.setAttribute("subject", subject);
            request.getRequestDispatcher("/components/lesson/lesson-edit.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID buổi học ({0}) hoặc ID môn học ({1}) không hợp lệ cho form chỉnh sửa buổi học.", new Object[]{lessonIdStr, subjectIdStr});
            request.setAttribute("errorMessage", "ID buổi học hoặc ID môn học không hợp lệ.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        } catch (Exception e) { // Bắt lỗi Exception chung
             LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi hiển thị form chỉnh sửa buổi học: {0}", e.getMessage());
             request.setAttribute("errorMessage", "Lỗi hệ thống.");
             request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý yêu cầu cập nhật buổi học.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng hiện tại
     * @throws ServletException
     * @throws IOException
     */
    private void editLesson(HttpServletRequest request, HttpServletResponse response, int userId)
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

                Lesson existingLesson = lessonDao.getLessonById(lessonId);
                Subject subject = subjectDao.getSubjectById(subjectId);

                // Kiểm tra quyền truy cập và tính hợp lệ của dữ liệu
                if (existingLesson == null || subject == null || existingLesson.getSubjectId() != subjectId || semesterDao.getSemesterById(subject.getSemesterId(), userId) == null) {
                    errors.put("general", "Buổi học hoặc môn học không tồn tại hoặc bạn không có quyền cập nhật.");
                } else {
                    request.setAttribute("lesson", existingLesson); 
                    request.setAttribute("subject", subject);
                }
            }

            String name = request.getParameter("name");
            String lessonDateStr = request.getParameter("lessonDate");
            String description = request.getParameter("description");
            String status = request.getParameter("status");

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

            if (status == null || status.trim().isEmpty()) {
                errors.put("status", "Trạng thái không được để trống.");
            }

            if (!errors.isEmpty()) {
                request.setAttribute("errors", errors);
                request.setAttribute("formName", name);
                request.setAttribute("formLessonDate", lessonDateStr);
                request.setAttribute("formDescription", description);
                request.setAttribute("formStatus", status);
                request.getRequestDispatcher("/components/lesson/lesson-edit.jsp").forward(request, response);
                return;
            }

            Lesson currentLesson = lessonDao.getLessonById(lessonId);
            LocalDateTime createdAt = (currentLesson != null) ? currentLesson.getCreatedAt() : LocalDateTime.now(); 
            
            Lesson updatedLesson = new Lesson(lessonId, subjectId, name, lessonDate, description, status, createdAt, LocalDateTime.now());

            boolean success = lessonDao.editLesson(updatedLesson);

            if (success) {
                LOGGER.log(Level.INFO, "Buổi học với ID {0} đã được cập nhật thành công.", new Object[]{lessonId});
                response.sendRedirect(request.getContextPath() + "/lessons?subjectId=" + subjectId + "&message=editSuccess");
            } else {
                errors.put("general", "Có lỗi xảy ra khi cập nhật buổi học.");
                request.setAttribute("errors", errors);
                request.setAttribute("formName", name);
                request.setAttribute("formLessonDate", lessonDateStr);
                request.setAttribute("formDescription", description);
                request.setAttribute("formStatus", status);
                request.getRequestDispatcher("/components/lesson/lesson-edit.jsp").forward(request, response);
            }

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID buổi học ({0}) hoặc ID môn học ({1}) không hợp lệ khi cập nhật buổi học.", new Object[]{request.getParameter("id"), request.getParameter("subjectId")});
            errors.put("general", "ID buổi học hoặc ID môn học không hợp lệ.");
            request.setAttribute("errors", errors);
            try {
                if (lessonId > 0) request.setAttribute("lesson", lessonDao.getLessonById(lessonId));
                if (subjectId > 0) request.setAttribute("subject", subjectDao.getSubjectById(subjectId));
            } catch (Exception ex) { // Catch Exception chung
                 LOGGER.log(Level.SEVERE, "Lỗi khi lấy buổi học/môn học cho trang lỗi trong quá trình cập nhật.", ex);
            }
            request.getRequestDispatcher("/components/lesson/lesson-edit.jsp").forward(request, response);
        } catch (Exception e) { // Bắt lỗi Exception chung
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật buổi học.", e);
            errors.put("general", "Đã xảy ra lỗi không mong muốn khi cập nhật buổi học.");
            request.setAttribute("errors", errors);
            request.setAttribute("formName", request.getParameter("name"));
            request.setAttribute("formLessonDate", request.getParameter("lessonDate"));
            request.setAttribute("formDescription", request.getParameter("description"));
            request.setAttribute("formStatus", request.getParameter("status"));
             try {
                if (lessonId > 0) request.setAttribute("lesson", lessonDao.getLessonById(lessonId));
                if (subjectId > 0) request.setAttribute("subject", subjectDao.getSubjectById(subjectId));
            } catch (Exception ex) { // Catch Exception chung
                 LOGGER.log(Level.SEVERE, "Lỗi khi lấy buổi học/môn học cho trang lỗi trong quá trình cập nhật.", ex);
            }
            request.getRequestDispatcher("/components/lesson/lesson-edit.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý yêu cầu xóa buổi học.
     * Kiểm tra quyền truy cập của người dùng.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng hiện tại
     * @throws ServletException
     * @throws IOException
     */
    private void deleteLesson(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        String lessonIdStr = request.getParameter("id");
        String subjectIdStr = request.getParameter("subjectId");

        if (lessonIdStr == null || lessonIdStr.isEmpty() || subjectIdStr == null || subjectIdStr.isEmpty()) {
            LOGGER.log(Level.WARNING, "Thiếu tham số lessonId hoặc subjectId để xóa buổi học.");
            request.setAttribute("errorMessage", "Thông tin buổi học hoặc môn học không đầy đủ để xóa.");
            displayLessons(request, response, userId);
            return;
        }

        try {
            int deleteId = Integer.parseInt(lessonIdStr);
            int subjectId = Integer.parseInt(subjectIdStr);

            // Kiểm tra quyền của người dùng đối với buổi học này (qua môn học và kỳ học)
            Lesson lessonToDelete = lessonDao.getLessonById(deleteId);
            if (lessonToDelete == null || lessonToDelete.getSubjectId() != subjectId) {
                request.setAttribute("errorMessage", "Buổi học không tồn tại hoặc không thuộc môn học này.");
                displayLessons(request, response, userId);
                return;
            }
            Subject subjectOfLesson = subjectDao.getSubjectById(subjectId);
            if (subjectOfLesson == null || semesterDao.getSemesterById(subjectOfLesson.getSemesterId(), userId) == null) {
                request.setAttribute("errorMessage", "Bạn không có quyền xóa buổi học này.");
                displayLessons(request, response, userId);
                return;
            }

            boolean success = lessonDao.deleteLesson(deleteId); // Sử dụng deleteLesson

            if (success) {
                LOGGER.log(Level.INFO, "Buổi học với ID {0} đã xóa thành công.", new Object[]{deleteId});
                response.sendRedirect(request.getContextPath() + "/lessons?subjectId=" + subjectId + "&message=deleteSuccess");
            } else {
                LOGGER.log(Level.WARNING, "Không thể xóa buổi học với ID {0}. Có thể không tồn tại hoặc có dữ liệu liên quan.", new Object[]{deleteId});
                request.setAttribute("errorMessage", "Không thể xóa buổi học. Có thể buổi học không tồn tại hoặc có dữ liệu liên quan.");
                displayLessons(request, response, userId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID buổi học ({0}) hoặc ID môn học ({1}) không hợp lệ để xóa.", new Object[]{lessonIdStr, subjectIdStr});
            request.setAttribute("errorMessage", "ID buổi học hoặc ID môn học không hợp lệ.");
            displayLessons(request, response, userId);
        } catch (Exception e) { // Bắt lỗi Exception chung
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa buổi học.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi xóa buổi học.");
            displayLessons(request, response, userId);
        }
    }
}
