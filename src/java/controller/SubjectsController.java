package controller;

import dao.DocumentDAO;
import dao.LessonDAO;
import dao.SemesterDAO;
import dao.SubjectDAO;
import entity.Document;
import entity.Lesson;
import entity.Semester;
import entity.Subject;
import entity.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Date; // Giữ nguyên, có thể dùng ở các hàm khác
import java.time.LocalDateTime; // Giữ nguyên
import java.util.HashMap; // Giữ nguyên
import java.util.List;
import java.util.Map; // Giữ nguyên
import java.util.logging.Level;
import java.util.logging.Logger;

public class SubjectsController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SubjectsController.class.getName());
    SubjectDAO subjectDao = new SubjectDAO();
    SemesterDAO semesterDao = new SemesterDAO();
    LessonDAO lessonDao = new LessonDAO();
    DocumentDAO documentDao = new DocumentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            // Nên dùng filter AuthFilter để xử lý, nhưng vẫn giữ ở đây phòng trường hợp không có filter
            throw new ServletException("Không có quyền truy cập");
        }

        User user = (User) session.getAttribute("loggedInUser");
        Semester currentSemester = null;
        switch (action == null ? "" : action) {
            case "/add":
                int semesterIdForAdd = Integer.parseInt(request.getParameter("semesterId"));
                currentSemester = semesterDao.getSemesterById(semesterIdForAdd, user.getId());
                request.setAttribute("semesterId", semesterIdForAdd);
                request.setAttribute("currentSemester", currentSemester);
                request.getRequestDispatcher("/components/subject/subject-add.jsp").forward(request, response);
                break;
            case "/edit":
                int editId = Integer.parseInt(request.getParameter("id"));
                Subject subject = subjectDao.getSubjectById(editId);
                request.setAttribute("subject", subject);
                currentSemester = semesterDao.getSemesterById(subject.getSemesterId(), user.getId());
                request.setAttribute("currentSemester", currentSemester);
                request.getRequestDispatcher("/components/subject/subject-edit.jsp").forward(request, response);
                break;
            case "/delete-confirm":
                displayDeleteSubjectConfirm(request, response);
                break;
            case "/detail": 
                displaySubjectDetail(request, response, user);
                break;
            default:
                displaySubjects(request, response, user);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        LOGGER.log(Level.INFO, "action: {0}", action);

        User user = (User) session.getAttribute("loggedInUser");

        switch (action != null ? action : "") {
            case "/add":
                addSubject(request, response);
                break;
            case "/edit":
                editSubject(request, response);
                break;
            case "/delete": // Xử lý DELETE qua POST (ĐƯỢC KHUYẾN NGHỊ)
                deleteSubject(request, response, user);
                break;
            default:
                // Xử lý POST mặc định nếu cần
                break;
        }
    }

    private void displayDeleteSubjectConfirm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Subject subject = subjectDao.getSubjectById(id); // Cần method getSubjectById(id, userId)
            if (subject != null) {
                request.setAttribute("subjectToDelete", subject);
                request.getRequestDispatcher("/components/subject/subject-delete-confirm.jsp").forward(request, response);
            } else {
                // 404
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid subject ID for delete confirmation", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID môn học không hợp lệ.");
        } catch (Exception e) { // Bắt các lỗi khác có thể xảy ra trong DAO
            LOGGER.log(Level.SEVERE, "Error fetching subject for delete confirmation", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Có lỗi xảy ra khi lấy thông tin môn học.");
        }
    }
    
    private void displaySubjects(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String search = request.getParameter("search");
        String pageStr = request.getParameter("page");
        String semesterIdStr = request.getParameter("semesterId");
        String isActiveStr = request.getParameter("isActive");
        String teacherName = request.getParameter("teacherName");

        int semesterId;
        if (semesterIdStr == null || semesterIdStr.isEmpty()) {
            Semester latestSemester = semesterDao.getLatestSemester(user.getId());
            if (latestSemester != null) {
                response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + latestSemester.getId());
                return;
            } else {
                request.setAttribute("errorMessage", "Bạn chưa có kỳ học nào. Vui lòng thêm kỳ học mới.");
                request.getRequestDispatcher("/components/subject/no-semester-found.jsp").forward(request, response);
                return;
            }
        } else {
            semesterId = Integer.parseInt(semesterIdStr);
        }

        int page = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
        int pageSize = 10;
        int offset = (page - 1) * pageSize;
        Boolean isActive = (isActiveStr != null && !isActiveStr.isEmpty()) ? Boolean.parseBoolean(isActiveStr) : null;

        // Truyền teacherName vào phương thức DAO (không thay đổi)
        List<Subject> subjects = subjectDao.getAllSubjects(search, semesterId, isActive, offset, pageSize, teacherName);
        // Truyền teacherName vào phương thức getTotalSubjectCount (không thay đổi)
        int totalSubjects = subjectDao.countSubjects(search, semesterId, teacherName); 
        int totalPages = (int) Math.ceil((double) totalSubjects / pageSize);

        Semester currentSemester = semesterDao.getSemesterById(semesterId, user.getId());
        List<Semester> allSemesters = semesterDao.getAllSemesters(null, null, null, null, 0, Integer.MAX_VALUE, user.getId());

        request.setAttribute("currentSemester", currentSemester);
        request.setAttribute("allSemesters", allSemesters);
        request.setAttribute("semesterId", semesterId);
        request.setAttribute("search", search);
        request.setAttribute("teacherName", teacherName); // Vẫn giữ lại giá trị teacherName trên form

        request.setAttribute("subjects", subjects);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);
        //System.out.println("subjects: " + subjects);
        request.getRequestDispatcher("/components/subject/subject-dashboard.jsp").forward(request, response);
    }
    
    private void displaySubjectDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        try {
            int subjectId = Integer.parseInt(request.getParameter("id"));
            Subject subject = subjectDao.getSubjectById(subjectId);

            if (subject == null) {
                LOGGER.log(Level.WARNING, "Subject with ID {0} not found for detail view.", subjectId);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy môn học.");
                return;
            }

            // Kiểm tra quyền: Đảm bảo môn học thuộc về một kỳ học mà người dùng sở hữu
            Semester semester = semesterDao.getSemesterById(subject.getSemesterId(), user.getId());
            if (semester == null) {
                LOGGER.log(Level.WARNING, "User {0} attempted to access subject {1} from unauthorized semester {2}.", new Object[]{user.getUsername(), subjectId, subject.getSemesterId()});
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập vào môn học này.");
                return;
            }

            List<Lesson> lessons = lessonDao.getAllLessonsBySubjectId(subjectId, null, null, 1, Integer.MAX_VALUE); // Lấy tất cả buổi học
            List<Document> documents = documentDao.getDocumentsBySubjectId(subjectId, user.getId()); // Lấy tất cả tài liệu của môn học này

            request.setAttribute("subject", subject);
            request.setAttribute("lessons", lessons);
            request.setAttribute("documents", documents);
            request.setAttribute("currentSemester", semester); // Đẩy semester vào để dùng cho link quay lại

            request.getRequestDispatcher("/components/subject/subject-detail.jsp").forward(request, response);
            return;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid subject ID format for detail view: {0}", request.getParameter("id"));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID môn học không hợp lệ.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in displaySubjectDetail", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống khi cố gắng hiển thị chi tiết môn học.");
        }
    }

    private void addSubject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();

        int semesterId = 0; // Khởi tạo với giá trị mặc định
        String semesterIdStr = request.getParameter("semesterId"); // Lấy từ request
        try {
            semesterId = Integer.parseInt(semesterIdStr);
            if (semesterId <= 0) {
                errors.put("semesterId", "Vui lòng chọn học kỳ hợp lệ");
            }
        } catch (NumberFormatException e) {
            errors.put("semesterId", "Học kỳ không hợp lệ");
        }

        // --- Giữ nguyên các phần validate và logic thêm môn học khác ---
        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String description = request.getParameter("description");
        String creditsStr = request.getParameter("credits");
        String teacherName = request.getParameter("teacherName");
        String prerequisites = request.getParameter("prerequisites");
        String isActiveStr = request.getParameter("isActive");

        // Validate dữ liệu
        boolean isActive = true;
        if (isActiveStr != null && !isActiveStr.trim().isEmpty()) {
            isActive = Boolean.parseBoolean(isActiveStr);
        }
        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Tên môn học không được để trống");
        }
        if (code == null || code.trim().isEmpty()) {
            errors.put("code", "Mã môn học không được để trống");
        } else if (subjectDao.isCodeExists(code, semesterId)) { // isCodeExists cần kiểm tra thêm userId hoặc semesterId nếu muốn mã môn là duy nhất trong phạm vi đó
            errors.put("code", "Mã môn học đã tồn tại");
        }
        int credits = 0;
        try {
            credits = Integer.parseInt(creditsStr);
            if (credits < 1 || credits > 10) {
                errors.put("credits", "Số tín chỉ phải từ 1 đến 10");
            }
        } catch (NumberFormatException e) {
            errors.put("credits", "Số tín chỉ không hợp lệ");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errorMessage", errors);
            // Giữ lại các giá trị form đã nhập để người dùng không phải nhập lại
            request.setAttribute("formName", name);
            request.setAttribute("formCode", code);
            request.setAttribute("formDescription", description);
            request.setAttribute("formCredits", creditsStr);
            request.setAttribute("formTeacherName", teacherName);
            request.setAttribute("formPrerequisites", prerequisites);
            request.setAttribute("formIsActive", isActiveStr);
            request.setAttribute("semesterId", semesterId); // Rất quan trọng để add.jsp biết semesterId hiện tại

            request.getRequestDispatcher("/components/subject/subject-add.jsp").forward(request, response);
            return;
        }

        Subject subject = new Subject(semesterId, name, code, description, credits, teacherName, isActive, prerequisites,
                LocalDateTime.now(), LocalDateTime.now());
        boolean success = subjectDao.addSubject(subject);

        if (!success) {
            errors.put("general", "Có lỗi xảy ra khi thêm môn học");
            request.setAttribute("errors", errors);
            // Giữ lại các giá trị form đã nhập nếu có lỗi
            request.setAttribute("formName", name);
            request.setAttribute("formCode", code);
            request.setAttribute("formDescription", description);
            request.setAttribute("formCredits", creditsStr);
            request.setAttribute("formTeacherName", teacherName);
            request.setAttribute("formPrerequisites", prerequisites);
            request.setAttribute("formIsActive", isActiveStr);
            request.setAttribute("semesterId", semesterId);

            request.getRequestDispatcher("/components/subject/subject-add.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + semesterId);
        }
    }

    private void editSubject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();

        int id = Integer.parseInt(request.getParameter("id"));
        int semesterId = Integer.parseInt(request.getParameter("semesterId"));
        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String description = request.getParameter("description");
        String creditsStr = request.getParameter("credits");
        String teacherName = request.getParameter("teacherName");
        String prerequisites = request.getParameter("prerequisites");
        String isActiveStr = request.getParameter("isActive");

        // Validate dữ liệu
        boolean isActive = true;
        if (isActiveStr != null && !isActiveStr.trim().isEmpty()) {
            isActive = Boolean.parseBoolean(isActiveStr);
        }
        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Tên môn học không được để trống");
        }
        if (code == null || code.trim().isEmpty()) {
            errors.put("code", "Mã môn học không được để trống");
        } else if (subjectDao.isCodeExistsExceptId(code, id, semesterId)) {
            errors.put("code", "Mã môn học đã tồn tại");
        }
        int credits = 0;
        try {
            credits = Integer.parseInt(creditsStr);
            if (credits < 1 || credits > 10) {
                errors.put("credits", "Số tín chỉ phải từ 1 đến 10");
            }
        } catch (NumberFormatException e) {
            errors.put("credits", "Số tín chỉ không hợp lệ");
        }

        if (!errors.isEmpty()) {
            // Trả lỗi về jsp
            Subject subject = subjectDao.getSubjectById(id); // Lấy lại subject gốc
            request.setAttribute("subject", subject); // Để giữ lại các thông tin khác của subject
            request.setAttribute("errorMessage", errors);

            // Giữ lại các giá trị form đã nhập để người dùng không phải nhập lại
            request.setAttribute("formName", name);
            request.setAttribute("formCode", code);
            request.setAttribute("formDescription", description);
            request.setAttribute("formCredits", creditsStr);
            request.setAttribute("formTeacherName", teacherName);
            request.setAttribute("formPrerequisites", prerequisites);
            request.setAttribute("formIsActive", isActiveStr);
            request.setAttribute("semesterId", semesterId); // Quan trọng

            request.getRequestDispatcher("/components/subject/subject-edit.jsp").forward(request, response);
            return;
        }

        Subject subject = new Subject(id, semesterId, name, code, description, credits, teacherName, isActive, prerequisites,
                LocalDateTime.now(), LocalDateTime.now());
        boolean success = subjectDao.editSubject(subject);

        if (!success) {
            errors.put("general", "Có lỗi xảy ra khi cập nhật môn học");
            request.setAttribute("errors", errors);
            // Giữ lại các giá trị form đã nhập nếu có lỗi
            Subject existingSubject = subjectDao.getSubjectById(id);
            request.setAttribute("subject", existingSubject);
            request.setAttribute("formName", name);
            request.setAttribute("formCode", code);
            request.setAttribute("formDescription", description);
            request.setAttribute("formCredits", creditsStr);
            request.setAttribute("formTeacherName", teacherName);
            request.setAttribute("formPrerequisites", prerequisites);
            request.setAttribute("formIsActive", isActiveStr);
            request.setAttribute("semesterId", semesterId);

            request.getRequestDispatcher("/components/subject/subject-edit.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + semesterId);
        }
    }

    private void deleteSubject(HttpServletRequest request, HttpServletResponse response, User user) throws IOException, ServletException {
        try {
            int deleteId = Integer.parseInt(request.getParameter("id"));
            int semesterId = Integer.parseInt(request.getParameter("semesterId")); // Lấy semesterId để chuyển hướng về đúng trang

            // Bước 1: Kiểm tra quyền của người dùng đối với kỳ học chứa môn học này
            Semester semester = semesterDao.getSemesterById(semesterId, user.getId());
            if (semester == null) {
                LOGGER.log(Level.WARNING, "User {0} attempted to delete subject from non-existent or unauthorized semester ID {1}.", new Object[]{user.getUsername(), semesterId});
                request.setAttribute("errorMessage", "Kỳ học không tồn tại hoặc bạn không có quyền xóa môn học này.");
                displaySubjects(request, response, user); // Quay lại trang danh sách môn học với lỗi
                return;
            }

            // Bước 2: Kiểm tra xem môn học có tồn tại và thuộc về kỳ học đã chọn không
            Subject subjectToDelete = subjectDao.getSubjectById(deleteId);
            if (subjectToDelete == null || subjectToDelete.getSemesterId() != semesterId) {
                LOGGER.log(Level.WARNING, "User {0} attempted to delete non-existent subject ID {1} or subject not in semester {2}.", new Object[]{user.getUsername(), deleteId, semesterId});
                request.setAttribute("errorMessage", "Môn học không tồn tại hoặc không thuộc kỳ học này.");
                displaySubjects(request, response, user); // Quay lại trang danh sách môn học với lỗi
                return;
            }

            // Bước 3: Thực hiện xóa
            boolean success = subjectDao.deleteSubject(deleteId); // Gọi phương thức xóa từ DAO
            if (success) {
                LOGGER.log(Level.INFO, "Subject with ID {0} deleted successfully by user {1} from semester {2}.", new Object[]{deleteId, user.getUsername(), semesterId});
                // Chuyển hướng về danh sách môn học của kỳ học đó sau khi xóa thành công
                response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + semesterId);
            } else {
                LOGGER.log(Level.WARNING, "Failed to delete subject with ID {0} for user {1} from semester {2}.", new Object[]{deleteId, user.getUsername(), semesterId});
                request.setAttribute("errorMessage", "Không thể xóa môn học. Có thể môn học đang được sử dụng hoặc có lỗi xảy ra.");
                // Quay lại dashboard với thông báo lỗi
                displaySubjects(request, response, user);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid subject ID or semester ID format for delete: {0}, {1}", new Object[]{request.getParameter("id"), request.getParameter("semesterId")});
            request.setAttribute("errorMessage", "ID môn học hoặc ID kỳ học không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/semesters"); // Quay về trang kỳ học nếu ID không hợp lệ
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in deleteSubject", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống khi cố gắng xóa môn học.");
            displaySubjects(request, response, user);
        }
    }
}
