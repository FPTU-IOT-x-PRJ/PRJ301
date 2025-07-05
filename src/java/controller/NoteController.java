package controller;

import dao.NoteDAO;
import dao.SubjectDAO;
import dao.LessonDAO;
import entity.Note;
import entity.Lesson;
import entity.Subject;
import entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson; // Import Gson
import utils.ConfigManager; // Giả định này nếu bạn có cấu hình đặc biệt cho ghi chú

/**
 * Controller xử lý các thao tác liên quan đến ghi chú (Note).
 */
@WebServlet(name = "NoteController", urlPatterns = {"/notes/*"})
public class NoteController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NoteController.class.getName());
    private NoteDAO noteDao;
    private SubjectDAO subjectDao;
    private LessonDAO lessonDao;

    @Override
    public void init() throws ServletException {
        super.init();
        noteDao = new NoteDAO();
        subjectDao = new SubjectDAO();
        lessonDao = new LessonDAO();
        // Không cần Cloudinary cho ghi chú văn bản thông thường
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            switch (action != null ? action : "/display") { // Default action là display list
                case "/add":
                    displayAddForm(request, response, user.getId());
                    break;
                case "/display":
                    displayNotes(request, response, user.getId());
                    break;
                case "/edit":
                    displayEditForm(request, response, user.getId());
                    break;
                case "/detail":
                    displayNoteDetail(request, response, user.getId());
                    break;
                case "/delete":
                    deleteNote(request, response, user.getId());
                    break;
                case "/getLessonsBySubject": // Tái sử dụng cho AJAX
                    getLessonsBySubject(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action không hợp lệ.");
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu trong NoteController.doGet", ex);
            throw new ServletException("Lỗi truy vấn cơ sở dữ liệu.", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        request.setCharacterEncoding("UTF-8"); // Đảm bảo nhận tiếng Việt
        response.setCharacterEncoding("UTF-8"); // Đảm bảo gửi tiếng Việt

        try {
            switch (action != null ? action : "") {
                case "/add":
                    addNote(request, response, user.getId());
                    break;
                case "/edit":
                    editNote(request, response, user.getId());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action không hợp lệ.");
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu trong NoteController.doPost", ex);
            throw new ServletException("Lỗi truy vấn cơ sở dữ liệu.", ex);
        }
    }

    /**
     * Hiển thị danh sách các ghi chú của người dùng hiện tại, có thể lọc theo
     * môn học và buổi học.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void displayNotes(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {

        Integer filterSubjectId = null;
        Integer filterLessonId = null;

        String subjectIdParam = request.getParameter("subjectId");
        String lessonIdParam = request.getParameter("lessonId");

        if (subjectIdParam != null && !subjectIdParam.trim().isEmpty()) {
            try {
                filterSubjectId = Integer.parseInt(subjectIdParam);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Định dạng ID môn học không hợp lệ cho bộ lọc: {0}", subjectIdParam);
                request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
            }
        }

        if (lessonIdParam != null && !lessonIdParam.trim().isEmpty()) {
            try {
                filterLessonId = Integer.parseInt(lessonIdParam);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Định dạng ID buổi học không hợp lệ cho bộ lọc: {0}", lessonIdParam);
                request.setAttribute("errorMessage", "ID buổi học không hợp lệ.");
            }
        }

        List<Note> listNotes = noteDao.getFilteredNotes(userId, filterSubjectId, filterLessonId);

        List<Subject> allSubjects = subjectDao.getAllSubjects(null, null, null, 0, Integer.MAX_VALUE, null);
        Map<Integer, String> subjectNames = new HashMap<>();
        for (Subject s : allSubjects) {
            subjectNames.put(s.getId(), s.getName());
        }

        List<Lesson> lessonsOfSelectedSubject = new ArrayList<>();
        Map<Integer, String> lessonNames = new HashMap<>();
        if (filterSubjectId != null) {
            lessonsOfSelectedSubject = lessonDao.getAllLessonsBySubjectId(filterSubjectId, null, null, 1, Integer.MAX_VALUE);
            for (Lesson l : lessonsOfSelectedSubject) {
                lessonNames.put(l.getId(), l.getName());
            }
        } else {
            for (Note note : listNotes) {
                if (note.getLessonId() != null && !lessonNames.containsKey(note.getLessonId())) {
                    Lesson lesson = lessonDao.getLessonById(note.getLessonId());
                    if (lesson != null) {
                        lessonNames.put(lesson.getId(), lesson.getName());
                    }
                }
            }
        }

        request.setAttribute("listNotes", listNotes);
        request.setAttribute("subjects", allSubjects);
        request.setAttribute("subjectNames", subjectNames);
        request.setAttribute("lessonsOfSelectedSubject", lessonsOfSelectedSubject);
        request.setAttribute("lessonNames", lessonNames);
        request.setAttribute("selectedSubjectId", filterSubjectId);
        request.setAttribute("selectedLessonId", filterLessonId);

        request.getRequestDispatcher("/components/note/note-dashboard.jsp").forward(request, response);
    }

    /**
     * Hiển thị form để thêm ghi chú mới.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void displayAddForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        List<Subject> subjects = subjectDao.getAllSubjects(null, null, null, 0, Integer.MAX_VALUE, null);
        request.setAttribute("subjects", subjects);
        request.getRequestDispatcher("/components/note/note-add.jsp").forward(request, response);
    }

    /**
     * Hiển thị form để chỉnh sửa ghi chú.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void displayEditForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Note existingNote = noteDao.getNoteById(id, userId);

            if (existingNote != null) {
                request.setAttribute("note", existingNote);
                List<Subject> subjects = subjectDao.getAllSubjects(null, null, null, 0, Integer.MAX_VALUE, null);
                request.setAttribute("subjects", subjects);

                if (existingNote.getSubjectId() != null) {
                    List<Lesson> lessons = lessonDao.getAllLessonsBySubjectId(existingNote.getSubjectId(), null, null, 1, Integer.MAX_VALUE);
                    request.setAttribute("lessonsOfSelectedSubject", lessons);
                }

                request.getRequestDispatcher("/components/note/note-edit.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Ghi chú không tồn tại hoặc bạn không có quyền chỉnh sửa.");
                displayNotes(request, response, userId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID ghi chú không hợp lệ khi hiển thị form chỉnh sửa: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID ghi chú không hợp lệ.");
            displayNotes(request, response, userId);
        }
    }

    /**
     * Xử lý việc thêm ghi chú mới.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void addNote(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            String title = request.getParameter("title");
            String content = request.getParameter("content");

            Integer subjectId = null;
            Integer lessonId = null;
            String subjectIdStr = request.getParameter("subjectId");
            String lessonIdStr = request.getParameter("lessonId");

            if (subjectIdStr != null && !subjectIdStr.trim().isEmpty()) {
                try {
                    subjectId = Integer.parseInt(subjectIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Định dạng ID môn học không hợp lệ: {0}", subjectIdStr);
                    request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
                    displayAddForm(request, response, userId);
                    return;
                }
            }

            if (lessonIdStr != null && !lessonIdStr.trim().isEmpty()) {
                try {
                    lessonId = Integer.parseInt(lessonIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Định dạng ID buổi học không hợp lệ: {0}", lessonIdStr);
                    request.setAttribute("errorMessage", "ID buổi học không hợp lệ.");
                    displayAddForm(request, response, userId);
                    return;
                }
            }

            if (lessonId != null && subjectId == null) {
                request.setAttribute("errorMessage", "Một ghi chú gắn với buổi học phải có môn học tương ứng.");
                displayAddForm(request, response, userId);
                return;
            }

            Note newNote = new Note(title, content, userId, subjectId, lessonId);
            boolean success = noteDao.addNote(newNote);

            if (success) {
                LOGGER.log(Level.INFO, "Ghi chú '{0}' đã được thêm thành công bởi người dùng {1}.", new Object[]{title, userId});
                String redirectUrl;
                if (lessonId != null) {
                    redirectUrl = request.getContextPath() + "/lessons/detail?id=" + lessonId;
                } else if (subjectId != null) {
                    redirectUrl = request.getContextPath() + "/subjects/detail?id=" + subjectId;
                } else {
                    redirectUrl = request.getContextPath() + "/notes/display?message=addSuccess";
                }
                response.sendRedirect(redirectUrl);
            } else {
                LOGGER.log(Level.WARNING, "Không thể thêm ghi chú '{0}' vào DB cho người dùng {1}.", new Object[]{title, userId});
                request.setAttribute("errorMessage", "Lỗi khi lưu thông tin ghi chú vào cơ sở dữ liệu.");
                displayAddForm(request, response, userId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi thêm ghi chú.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi thêm ghi chú: " + e.getMessage());
            displayAddForm(request, response, userId);
        }
    }

    /**
     * Xử lý việc cập nhật thông tin ghi chú.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void editNote(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String title = request.getParameter("title");
            String content = request.getParameter("content");

            Integer subjectId = null;
            Integer lessonId = null;
            String subjectIdStr = request.getParameter("subjectId");
            String lessonIdStr = request.getParameter("lessonId");

            if (subjectIdStr != null && !subjectIdStr.trim().isEmpty()) {
                try {
                    subjectId = Integer.parseInt(subjectIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Định dạng ID môn học không hợp lệ khi cập nhật: {0}", subjectIdStr);
                    request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
                    displayEditForm(request, response, userId);
                    return;
                }
            }

            if (lessonIdStr != null && !lessonIdStr.trim().isEmpty()) {
                try {
                    lessonId = Integer.parseInt(lessonIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Định dạng ID buổi học không hợp lệ khi cập nhật: {0}", lessonIdStr);
                    request.setAttribute("errorMessage", "ID buổi học không hợp lệ.");
                    displayEditForm(request, response, userId);
                    return;
                }
            }

            if (lessonId != null && subjectId == null) {
                request.setAttribute("errorMessage", "Một ghi chú gắn với buổi học phải có môn học tương ứng.");
                displayEditForm(request, response, userId);
                return;
            }

            Note existingNote = noteDao.getNoteById(id, userId);

            if (existingNote != null) {
                existingNote.setTitle(title);
                existingNote.setContent(content);
                existingNote.setSubjectId(subjectId);
                existingNote.setLessonId(lessonId);

                boolean success = noteDao.editNote(existingNote, userId);

                if (success) {
                    LOGGER.log(Level.INFO, "Ghi chú với ID {0} đã được cập nhật thành công bởi người dùng {1}.", new Object[]{id, userId});
                    String redirectUrl;
                    if (lessonId != null) {
                        redirectUrl = request.getContextPath() + "/lessons/detail?id=" + lessonId;
                    } else if (subjectId != null) {
                        redirectUrl = request.getContextPath() + "/subjects/detail?id=" + subjectId;
                    } else {
                        redirectUrl = request.getContextPath() + "/notes/display?message=updateSuccess";
                    }
                    response.sendRedirect(redirectUrl);
                } else {
                    LOGGER.log(Level.WARNING, "Không thể cập nhật ghi chú với ID {0} cho người dùng {1}.", new Object[]{id, userId});
                    request.setAttribute("errorMessage", "Không thể cập nhật ghi chú. Vui lòng thử lại.");
                    displayEditForm(request, response, userId);
                }
            } else {
                LOGGER.log(Level.WARNING, "Cố gắng cập nhật ghi chú không tồn tại hoặc không được phép với ID {0} bởi người dùng {1}.", new Object[]{id, userId});
                request.setAttribute("errorMessage", "Ghi chú không tồn tại hoặc bạn không có quyền chỉnh sửa.");
                displayNotes(request, response, userId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID ghi chú không hợp lệ khi cập nhật: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID ghi chú không hợp lệ.");
            displayNotes(request, response, userId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật ghi chú.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi cập nhật ghi chú: " + e.getMessage());
            displayNotes(request, response, userId);
        }
    }

    /**
     * Hiển thị chi tiết của một ghi chú.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void displayNoteDetail(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Note note = noteDao.getNoteById(id, userId);
            if (note != null) {
                if (note.getSubjectId() != null) {
                    Subject subject = subjectDao.getSubjectById(note.getSubjectId());
                    request.setAttribute("associatedSubject", subject);
                }
                if (note.getLessonId() != null) {
                    Lesson lesson = lessonDao.getLessonById(note.getLessonId());
                    request.setAttribute("associatedLesson", lesson);
                }
                
                Integer subjectIdParam = note.getSubjectId();
                Integer lessonIdParam = note.getLessonId();

                String redirectUrl;
                if (lessonIdParam != null) {
                    redirectUrl = request.getContextPath() + "/lessons/detail?id=" + lessonIdParam;
                } else if (subjectIdParam != null) {
                    redirectUrl = request.getContextPath() + "/subjects/detail?id=" + subjectIdParam;
                } else {
                    redirectUrl = request.getContextPath() + "/notes/display"; // Mặc định quay lại danh sách ghi chú
                }
                request.setAttribute("redirectUrl", redirectUrl);

                request.setAttribute("note", note);
                request.getRequestDispatcher("/components/note/note-detail.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Ghi chú không tồn tại hoặc bạn không có quyền xem.");
                displayNotes(request, response, userId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID ghi chú không hợp lệ để xem chi tiết: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID ghi chú không hợp lệ.");
            displayNotes(request, response, userId);
        }
    }

    /**
     * Xử lý việc xóa ghi chú.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void deleteNote(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Note noteToDelete = noteDao.getNoteById(id, userId);

            if (noteToDelete == null) {
                LOGGER.log(Level.WARNING, "Cố gắng xóa ghi chú không tồn tại hoặc không được phép với ID {0} bởi người dùng {1}.", new Object[]{id, userId});
                request.setAttribute("errorMessage", "Không thể xóa ghi chú. Ghi chú không tồn tại hoặc bạn không có quyền.");
                displayNotes(request, response, userId);
                return;
            }

            boolean success = noteDao.deleteNote(id, userId);

            if (success) {
                LOGGER.log(Level.INFO, "Ghi chú với ID {0} đã được xóa thành công từ DB bởi người dùng {1}.", new Object[]{id, userId});
                String redirectUrl;
                Integer lessonId = noteToDelete.getLessonId();
                Integer subjectId = noteToDelete.getSubjectId();
                if (lessonId != null) {
                    redirectUrl = request.getContextPath() + "/lessons/detail?id=" + lessonId;
                } else if (subjectId != null) {
                    redirectUrl = request.getContextPath() + "/subjects/detail?id=" + subjectId;
                } else {
                    redirectUrl = request.getContextPath() + "/notes/display?message=deleteSuccess";
                }
                response.sendRedirect(redirectUrl);
            } else {
                LOGGER.log(Level.WARNING, "Không thể xóa ghi chú với ID {0} cho người dùng {1}. Ghi chú không tìm thấy hoặc không được phép.", new Object[]{id, userId});
                request.setAttribute("errorMessage", "Không thể xóa ghi chú. Ghi chú không tồn tại hoặc bạn không có quyền.");
                displayNotes(request, response, userId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID ghi chú không hợp lệ để xóa: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID ghi chú không hợp lệ.");
            displayNotes(request, response, userId);
        }
    }

    /**
     * Phương thức xử lý AJAX để lấy danh sách các buổi học (Lessons) dựa trên
     * SubjectId được chọn. Trả về JSON.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void getLessonsBySubject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();

        try {
            String subjectIdStr = request.getParameter("subjectId");
            if (subjectIdStr == null || subjectIdStr.trim().isEmpty()) {
                response.getWriter().write(gson.toJson(new ArrayList<>()));
                return;
            }

            int subjectId = Integer.parseInt(subjectIdStr);
            List<Lesson> lessons = lessonDao.getAllLessonsBySubjectId(subjectId, null, null, 1, Integer.MAX_VALUE);
            response.getWriter().write(gson.toJson(lessons));
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID môn học không hợp lệ cho AJAX: {0}", request.getParameter("subjectId"));
            response.getWriter().write(gson.toJson(new ArrayList<>()));
        }
    }
}