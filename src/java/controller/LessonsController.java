package controller;

import dao.DocumentDAO;
import dao.LessonDAO;
import dao.NoteDAO;
import dao.SubjectDAO;
import dao.SemesterDAO; // Import SemesterDAO
import entity.Lesson;
import entity.Subject;
import entity.User;
import entity.Document; // Import Document
import entity.Note;
import entity.Semester; // Import Semester
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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

@WebServlet(name = "LessonsController", urlPatterns = {"/lessons", "/lessons/*"})
public class LessonsController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LessonsController.class.getName());
    private LessonDAO lessonDao = new LessonDAO();
    private SubjectDAO subjectDao = new SubjectDAO();
    private DocumentDAO documentDao = new DocumentDAO(); // Thêm DocumentDAO
    private SemesterDAO semesterDao = new SemesterDAO(); // Thêm SemesterDAO
    private NoteDAO noteDao = new NoteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("loggedInUser");

        if (action == null) {
            action = "/";
        }

        switch (action) {
            case "/":
                displayLessons(request, response, user);
                break;
            case "/add":
                showAddLessonForm(request, response, user);
                break;
            case "/edit":
                showEditLessonForm(request, response, user);
                break;
            case "/delete-confirm":
                showDeleteConfirm(request, response, user);
                break;
            case "/detail":
                displayLessonDetail(request, response, user); // Thêm action /detail
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("loggedInUser");

        if (action == null) {
            action = "/";
        }

        switch (action) {
            case "/add":
                addLesson(request, response, user);
                break;
            case "/edit":
                editLesson(request, response, user);
                break;
            case "/delete":
                deleteLesson(request, response, user);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    // Phương thức displayLessons đã có sẵn (hoặc tương tự)
    private void displayLessons(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        // Triển khai logic hiển thị danh sách lessons (ví dụ cho một subjectId nào đó)
        // Hiện tại chỉ là placeholder, cần logic để xác định subjectId nếu chưa có
        String subjectIdStr = request.getParameter("subjectId");
        if (subjectIdStr != null && !subjectIdStr.isEmpty()) {
            try {
                int subjectId = Integer.parseInt(subjectIdStr);
                Subject subject = subjectDao.getSubjectById(subjectId);
                if (subject != null) {
                    Semester semester = semesterDao.getSemesterById(subject.getSemesterId(), user.getId());
                    if (semester != null && semester.getUserId() == user.getId()) {
                        request.setAttribute("subject", subject);
                        List<Lesson> lessons = lessonDao.getAllLessonsBySubjectId(subjectId, null, null, 1, Integer.MAX_VALUE);
                        request.setAttribute("lessons", lessons);
                        request.getRequestDispatcher("/components/lesson/lesson-list.jsp").forward(request, response);
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid subject ID format for lessons list: {0}", subjectIdStr);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error fetching lessons list for subject.", e);
            }
        }
        // Fallback nếu không có subjectId hợp lệ hoặc không có quyền
        response.sendRedirect(request.getContextPath() + "/subjects"); // Chuyển hướng về danh sách môn học
    }

    // Phương thức showAddLessonForm đã có sẵn (hoặc tương tự)
    private void showAddLessonForm(HttpServletRequest request, HttpServletResponse response, User user)
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

    // Phương thức addLesson đã có sẵn (hoặc tương tự)
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
                LOGGER.log(Level.INFO, "Lesson '{0}' added successfully for subject ID {1}.", new Object[]{name, subjectId});
                response.sendRedirect(request.getContextPath() + "/subjects/detail?id=" + subjectId);
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

    // Phương thức showEditLessonForm đã có sẵn (hoặc tương tự)
    private void showEditLessonForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String lessonIdStr = request.getParameter("id");

        if (lessonIdStr == null || lessonIdStr.isEmpty()) {
            LOGGER.log(Level.WARNING, "Missing lessonId parameter for edit lesson form.");
            request.setAttribute("errorMessage", "Thông tin buổi học không đầy đủ để chỉnh sửa.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }

        try {
            int lessonId = Integer.parseInt(lessonIdStr);

            Lesson lesson = lessonDao.getLessonById(lessonId);
            if (lesson == null) {
                LOGGER.log(Level.WARNING, "Lesson ID {0} not found for editing.", new Object[]{lessonId});
                request.setAttribute("errorMessage", "Buổi học không tồn tại.");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }

            Subject subject = subjectDao.getSubjectById(lesson.getSubjectId());

            request.setAttribute("lesson", lesson);
            request.setAttribute("subject", subject);
            request.getRequestDispatcher("/components/lesson/lesson-edit.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid lesson ID ({0})) format for edit lesson form.", new Object[]{lessonIdStr});
            request.setAttribute("errorMessage", "ID buổi học không hợp lệ.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    // Phương thức updateLesson đã có sẵn (hoặc tương tự)
    private void editLesson(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String lessonIdStr = request.getParameter("id");
        String subjectIdStr = request.getParameter("subjectId"); // Cần lấy subjectId từ form để chuyển hướng
        String name = request.getParameter("name");
        String lessonDateStr = request.getParameter("lessonDate");
        String description = request.getParameter("description");
        String status = request.getParameter("status");

        Map<String, String> errors = new HashMap<>();

        int lessonId = 0;
        int subjectId = 0;

        try {
            lessonId = Integer.parseInt(lessonIdStr);
            Lesson existingLesson = lessonDao.getLessonById(lessonId);
            if (existingLesson == null) {
                errors.put("lesson", "Buổi học không tồn tại.");
            } else {
                subjectId = existingLesson.getSubjectId(); // Lấy subjectId từ lesson hiện có
                Subject subject = subjectDao.getSubjectById(subjectId);
                if (subject == null) {
                    errors.put("subject", "Môn học liên quan không tồn tại.");
                } else {
                    Semester semester = semesterDao.getSemesterById(subject.getSemesterId(), user.getId());
                    if (semester == null || semester.getUserId() != user.getId()) {
                        errors.put("permission", "Bạn không có quyền chỉnh sửa buổi học này.");
                    }
                }
            }
        } catch (NumberFormatException e) {
            errors.put("id", "ID buổi học không hợp lệ.");
        }

        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Tên buổi học không được để trống.");
        }
        Date lessonDate = null;
        if (lessonDateStr == null || lessonDateStr.trim().isEmpty()) {
            errors.put("lessonDate", "Ngày học không được để trống.");
        } else {
            try {
                lessonDate = Date.valueOf(lessonDateStr);
            } catch (IllegalArgumentException e) {
                errors.put("lessonDate", "Định dạng ngày học không hợp lệ.");
            }
        }
        if (status == null || status.trim().isEmpty()) {
            errors.put("status", "Trạng thái không được để trống.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errorMessage", errors);
            // Giữ lại dữ liệu đã nhập để hiển thị lại trên form
            request.setAttribute("lesson", new Lesson(lessonId, subjectId, name, lessonDate, description, status, null, null));
            request.setAttribute("subject", subjectDao.getSubjectById(subjectId)); // Lấy lại subject để hiển thị
            request.getRequestDispatcher("/components/lesson/edit-lesson.jsp").forward(request, response);
            return;
        }

        Lesson updatedLesson = new Lesson(lessonId, subjectId, name, lessonDate, description, status, null, LocalDateTime.now());
        try {
            boolean success = lessonDao.editLesson(updatedLesson);
            if (success) {
                response.sendRedirect(request.getContextPath() + "/lessons/detail?id=" + lessonId);
            } else {
                errors.put("general", "Không thể cập nhật buổi học. Vui lòng thử lại.");
                request.setAttribute("errorMessage", errors);
                request.setAttribute("lesson", updatedLesson);
                request.setAttribute("subject", subjectDao.getSubjectById(subjectId));
                request.getRequestDispatcher("/components/lesson/edit-lesson.jsp").forward(request, response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating lesson.", e);
            errors.put("general", "Đã xảy ra lỗi hệ thống khi cập nhật buổi học.");
            request.setAttribute("errorMessage", errors);
            request.setAttribute("lesson", updatedLesson);
            request.setAttribute("subject", subjectDao.getSubjectById(subjectId));
            request.getRequestDispatcher("/components/lesson/edit-lesson.jsp").forward(request, response);
        }
    }

    // Phương thức showDeleteConfirm đã có sẵn (hoặc tương tự)
    private void showDeleteConfirm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Lesson lesson = lessonDao.getLessonById(id);
            if (lesson != null) {
                request.setAttribute("lessonToDelete", lesson);
                request.getRequestDispatcher("/components/lesson/lesson-delete-confirm.jsp").forward(request, response);
            } else {
                int subjectId = -1;
                try {
                    subjectId = Integer.parseInt(request.getParameter("subjectId"));
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Subject ID not found or invalid in delete-confirm request", e);
                }

                request.setAttribute("errorMessage", "Không tìm thấy buổi học bạn muốn xóa.");
                response.sendRedirect(request.getContextPath() + "/lessons?subjectId=" + subjectId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid lesson ID for delete confirmation", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID buổi học không hợp lệ.");
        } catch (Exception e) { // Bắt các lỗi khác có thể xảy ra trong DAO
            LOGGER.log(Level.SEVERE, "Error fetching lesson for delete confirmation", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Có lỗi xảy ra khi lấy thông tin buổi học.");
        }
    }

    // Phương thức deleteLesson đã có sẵn (hoặc tương tự)
    private void deleteLesson(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String lessonIdStr = request.getParameter("id");
        String subjectIdStr = request.getParameter("subjectId"); // Lấy subjectId để chuyển hướng sau khi xóa

        if (lessonIdStr == null || lessonIdStr.isEmpty() || subjectIdStr == null || subjectIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/subjects");
            return;
        }

        try {
            int deleteId = Integer.parseInt(lessonIdStr);
            int subjectId = Integer.parseInt(subjectIdStr);

            Lesson lessonToDelete = lessonDao.getLessonById(deleteId);
            if (lessonToDelete == null) {
                response.sendRedirect(request.getContextPath() + "/subjects/detail?id=" + subjectId);
                return;
            }

            Subject subjectOfLesson = subjectDao.getSubjectById(lessonToDelete.getSubjectId());
            if (subjectOfLesson == null) {
                response.sendRedirect(request.getContextPath() + "/subjects/detail?id=" + subjectId);
                return;
            }

            Semester semesterOfSubject = semesterDao.getSemesterById(subjectOfLesson.getSemesterId(), user.getId());
            if (semesterOfSubject == null || semesterOfSubject.getUserId() != user.getId()) {
                response.sendRedirect(request.getContextPath() + "/subjects/detail?id=" + subjectId);
                return;
            }

            boolean success = lessonDao.deleteLesson(deleteId);

            if (success) {
                LOGGER.log(Level.INFO, "Lesson with ID {0} deleted successfully.", new Object[]{deleteId});
                response.sendRedirect(request.getContextPath() + "/subjects/detail?id=" + subjectId);
            } else {
                LOGGER.log(Level.WARNING, "Failed to delete lesson with ID {0}. It might not exist or be linked to other data.", new Object[]{deleteId});
                response.sendRedirect(request.getContextPath() + "/subjects/detail?id=" + subjectId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid lesson ID ({0}) or subject ID ({1}) format for delete.", new Object[]{lessonIdStr, subjectIdStr});
            response.sendRedirect(request.getContextPath() + "/subjects");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting lesson.", e);
            response.sendRedirect(request.getContextPath() + "/subjects");
        }
    }

    /**
     * Phương thức mới để hiển thị chi tiết buổi học.
     *
     * @param request
     * @param response
     * @param user
     * @throws ServletException
     * @throws IOException
     */
    private void displayLessonDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String lessonIdStr = request.getParameter("id");
        if (lessonIdStr == null || lessonIdStr.isEmpty()) {
            request.setAttribute("errorMessage", "ID buổi học không được cung cấp.");
            response.sendRedirect(request.getContextPath() + "/subjects"); // Chuyển hướng về trang môn học
            return;
        }

        try {
            int lessonId = Integer.parseInt(lessonIdStr);
            Lesson lesson = lessonDao.getLessonById(lessonId); // Lấy buổi học theo ID

            if (lesson == null) {
                request.setAttribute("errorMessage", "Không tìm thấy buổi học với ID: " + lessonId);
                response.sendRedirect(request.getContextPath() + "/subjects");
                return;
            }

            // Kiểm tra quyền sở hữu của người dùng thông qua Subject và Semester
            Subject subject = subjectDao.getSubjectById(lesson.getSubjectId());
            if (subject == null) {
                request.setAttribute("errorMessage", "Không tìm thấy môn học liên quan đến buổi học này.");
                response.sendRedirect(request.getContextPath() + "/subjects");
                return;
            }

            Semester semester = semesterDao.getSemesterById(subject.getSemesterId(), user.getId());
            if (semester == null || semester.getUserId() != user.getId()) {
                request.setAttribute("errorMessage", "Bạn không có quyền truy cập buổi học này.");
                response.sendRedirect(request.getContextPath() + "/subjects");
                return;
            }

            // Nếu mọi thứ hợp lệ, đặt các thuộc tính vào request
            request.setAttribute("lesson", lesson);
            request.setAttribute("subject", subject); // Để hiển thị tên môn học và link quay lại
            List<Note> notes = noteDao.getNotesByLessonOrSubjectId(lesson.getId(), null);
            request.setAttribute("notes", notes);

            // Lấy danh sách tài liệu cho buổi học này (sử dụng getFilteredDocuments)
            List<Document> documents = documentDao.getFilteredDocuments(user.getId(), lesson.getSubjectId(), lesson.getId());
            request.setAttribute("documents", documents);

            request.getRequestDispatcher("/components/lesson/lesson-detail.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid lesson ID format: {0}", lessonIdStr);
            request.setAttribute("errorMessage", "Định dạng ID buổi học không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/subjects");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error displaying lesson detail.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi tải chi tiết buổi học.");
            response.sendRedirect(request.getContextPath() + "/subjects");
        }
    }
}
