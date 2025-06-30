package controller;

import dao.SemesterDAO;
import dao.SubjectDAO;
import entity.Semester;
import entity.Subject;
import entity.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime; 
import java.util.HashMap; 
import java.util.List;
import java.util.Map; 
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller xử lý các thao tác liên quan đến môn học (Subject).
 */
public class SubjectsController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SubjectsController.class.getName());
    SubjectDAO subjectDao = new SubjectDAO();
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
                case "/detail": // Có thể thêm action để xem chi tiết môn học
                    displaySubjectDetail(request, response, user.getId());
                    break;
                default:
                    displaySubjects(request, response, user.getId()); // Mặc định là hiển thị danh sách
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong SubjectsController doGet: " + action, e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
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
        LOGGER.log(Level.INFO, "Action received in SubjectsController (POST): {0}", action);

        User user = (User) session.getAttribute("loggedInUser");
        request.setCharacterEncoding("UTF-8"); // Đảm bảo nhận tiếng Việt
        response.setCharacterEncoding("UTF-8"); // Đảm bảo gửi tiếng Việt

        try {
            switch (action != null ? action : "") {
                case "/add":
                    addSubject(request, response, user.getId());
                    break;
                case "/edit":
                    editSubject(request, response, user.getId());
                    break;
                case "/delete": 
                    deleteSubject(request, response, user.getId());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action không hợp lệ");
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong SubjectsController doPost: " + action, e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Hiển thị form xác nhận xóa môn học.
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
            Subject subject = subjectDao.getSubjectById(id); 

            if (subject != null) {
                // Kiểm tra xem môn học có thuộc về người dùng hiện tại không (qua semester)
                Semester associatedSemester = semesterDao.getSemesterById(subject.getSemesterId(), userId);
                if (associatedSemester == null) {
                    request.setAttribute("errorMessage", "Bạn không có quyền xóa môn học này.");
                    response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + subject.getSemesterId());
                    return;
                }
                
                request.setAttribute("subjectToDelete", subject);
                request.setAttribute("semesterId", subject.getSemesterId()); // Đảm bảo truyền semesterId
                request.getRequestDispatcher("/components/subject/subject-delete-confirm.jsp").forward(request, response);
            } else {
                int semesterId = -1; 
                try {
                    semesterId = Integer.parseInt(request.getParameter("semesterId"));
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Semester ID không tìm thấy hoặc không hợp lệ trong yêu cầu xác nhận xóa môn học", e);
                }
                
                request.setAttribute("errorMessage", "Không tìm thấy môn học bạn muốn xóa.");
                response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + semesterId); 
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID môn học không hợp lệ để xác nhận xóa", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID môn học không hợp lệ.");
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy môn học để xác nhận xóa", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Có lỗi xảy ra khi lấy thông tin môn học.");
        }
    }
    
    /**
     * Hiển thị danh sách các môn học cho người dùng hiện tại, có hỗ trợ tìm kiếm và phân trang.
     * Tên hàm cũ: `displaySubjectDashboard`
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void displaySubjects(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        String search = request.getParameter("search");
        String pageStr = request.getParameter("page");
        String semesterIdStr = request.getParameter("semesterId");
        String isActiveStr = request.getParameter("isActive");
        String teacherName = request.getParameter("teacherName");

        int semesterId;
        if (semesterIdStr == null || semesterIdStr.isEmpty()) {
            Semester latestSemester = semesterDao.getLatestSemester(userId); // Giả định getLatestSemester() đã có userId
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

        // Kiểm tra xem semesterId có thuộc về người dùng hiện tại không
        Semester currentSemester = semesterDao.getSemesterById(semesterId, userId);
        if (currentSemester == null) {
            request.setAttribute("errorMessage", "Kỳ học không tồn tại hoặc bạn không có quyền truy cập.");
            response.sendRedirect(request.getContextPath() + "/semesters"); // Chuyển hướng về danh sách kỳ học
            return;
        }

        int page = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
        int pageSize = 10;
        int offset = (page - 1) * pageSize;
        Boolean isActive = (isActiveStr != null && !isActiveStr.isEmpty()) ? Boolean.parseBoolean(isActiveStr) : null;

        List<Subject> subjects = subjectDao.getAllSubjects(search, semesterId, isActive, offset, pageSize, teacherName);
        int totalSubjects = subjectDao.countSubjects(search, semesterId, teacherName); 
        int totalPages = (int) Math.ceil((double) totalSubjects / pageSize);

        List<Semester> allSemesters = semesterDao.getAllSemesters(null, null, null, null, 0, Integer.MAX_VALUE, userId); // Lấy tất cả kỳ học của user

        request.setAttribute("currentSemester", currentSemester);
        request.setAttribute("allSemesters", allSemesters);
        request.setAttribute("semesterId", semesterId);
        request.setAttribute("search", search);
        request.setAttribute("teacherName", teacherName); 
        request.setAttribute("isActive", isActiveStr); // Giữ lại giá trị lọc trạng thái
        
        request.setAttribute("subjects", subjects);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);

        request.getRequestDispatcher("/components/subject/subject-dashboard.jsp").forward(request, response);
    }

    /**
     * Hiển thị chi tiết của một môn học (nếu có action này).
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void displaySubjectDetail(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Subject subject = subjectDao.getSubjectById(id);
            if (subject != null) {
                // Kiểm tra quyền truy cập của người dùng đối với môn học này
                Semester associatedSemester = semesterDao.getSemesterById(subject.getSemesterId(), userId);
                if (associatedSemester == null) {
                    request.setAttribute("errorMessage", "Bạn không có quyền xem môn học này.");
                    displaySubjects(request, response, userId);
                    return;
                }

                request.setAttribute("subject", subject);
                request.getRequestDispatcher("/components/subject/subject-detail.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Không tìm thấy môn học bạn muốn xem chi tiết.");
                displaySubjects(request, response, userId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID môn học không hợp lệ để xem chi tiết: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
            displaySubjects(request, response, userId);
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi hiển thị chi tiết môn học: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Lỗi cơ sở dữ liệu.");
            displaySubjects(request, response, userId);
        }
    }

    /**
     * Hiển thị form để thêm môn học mới.
     * Tên hàm cũ: `add` (trong doGet)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void displayAddForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        String semesterIdParam = request.getParameter("semesterId");
        if (semesterIdParam == null || semesterIdParam.isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng cung cấp ID kỳ học để thêm môn học.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }
        try {
            int semesterIdForAdd = Integer.parseInt(semesterIdParam);
            // Kiểm tra quyền truy cập của người dùng đối với kỳ học này
            Semester currentSemester = semesterDao.getSemesterById(semesterIdForAdd, userId);
            if (currentSemester == null) {
                request.setAttribute("errorMessage", "Kỳ học không tồn tại hoặc bạn không có quyền thêm môn học vào kỳ học này.");
                response.sendRedirect(request.getContextPath() + "/semesters"); // Quay về trang kỳ học
                return;
            }
            request.setAttribute("semesterId", semesterIdForAdd);
            request.setAttribute("currentSemester", currentSemester);
            request.getRequestDispatcher("/components/subject/subject-add.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "ID kỳ học không hợp lệ.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi hiển thị form thêm môn học: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Lỗi cơ sở dữ liệu.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý logic thêm môn học mới vào DB.
     * Tên hàm cũ: `addSubject` (trong doPost)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void addSubject(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();

        int semesterId = 0; 
        String semesterIdStr = request.getParameter("semesterId"); 
        try {
            semesterId = Integer.parseInt(semesterIdStr);
            if (semesterId <= 0) {
                errors.put("semesterId", "Vui lòng chọn học kỳ hợp lệ");
            } else {
                // Kiểm tra quyền truy cập của người dùng đối với kỳ học này
                Semester currentSemester = semesterDao.getSemesterById(semesterId, userId);
                if (currentSemester == null) {
                    errors.put("general", "Kỳ học không tồn tại hoặc bạn không có quyền thêm môn học vào kỳ học này.");
                }
            }
        } catch (NumberFormatException e) {
            errors.put("semesterId", "Học kỳ không hợp lệ");
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi kiểm tra kỳ học: {0}", e.getMessage());
            errors.put("general", "Lỗi cơ sở dữ liệu khi kiểm tra kỳ học.");
        }

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String description = request.getParameter("description");
        String creditsStr = request.getParameter("credits");
        String teacherName = request.getParameter("teacherName");
        String prerequisites = request.getParameter("prerequisites");
        String isActiveStr = request.getParameter("isActive");

        boolean isActive = true;
        if (isActiveStr != null && !isActiveStr.trim().isEmpty()) {
            isActive = Boolean.parseBoolean(isActiveStr);
        }
        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Tên môn học không được để trống");
        }
        if (code == null || code.trim().isEmpty()) {
            errors.put("code", "Mã môn học không được để trống");
        } else {
            try {
                if (subjectDao.isCodeExists(code, semesterId)) {
                    errors.put("code", "Mã môn học đã tồn tại trong kỳ học này.");
                }
            } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
                LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi kiểm tra mã môn học trùng lặp: {0}", e.getMessage());
                errors.put("general", "Lỗi cơ sở dữ liệu khi kiểm tra mã môn học.");
            }
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
            request.setAttribute("formName", name);
            request.setAttribute("formCode", code);
            request.setAttribute("formDescription", description);
            request.setAttribute("formCredits", creditsStr);
            request.setAttribute("formTeacherName", teacherName);
            request.setAttribute("formPrerequisites", prerequisites);
            request.setAttribute("formIsActive", isActiveStr);
            request.setAttribute("semesterId", semesterId); 
            request.getRequestDispatcher("/components/subject/subject-add.jsp").forward(request, response);
            return;
        }

        try {
            Subject subject = new Subject(semesterId, name, code, description, credits, teacherName, isActive, prerequisites,
                    LocalDateTime.now(), LocalDateTime.now());
            boolean success = subjectDao.addSubject(subject); // Sử dụng addSubject

            if (!success) {
                errors.put("general", "Có lỗi xảy ra khi thêm môn học.");
                request.setAttribute("errors", errors);
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
                response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + semesterId + "&message=addSuccess");
            }
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi thêm môn học: {0}", e.getMessage());
            errors.put("general", "Lỗi cơ sở dữ liệu khi thêm môn học.");
            request.setAttribute("errors", errors);
            request.setAttribute("formName", name);
            request.setAttribute("formCode", code);
            request.setAttribute("formDescription", description);
            request.setAttribute("formCredits", creditsStr);
            request.setAttribute("formTeacherName", teacherName);
            request.setAttribute("formPrerequisites", prerequisites);
            request.setAttribute("formIsActive", isActiveStr);
            request.setAttribute("semesterId", semesterId);
            request.getRequestDispatcher("/components/subject/subject-add.jsp").forward(request, response);
        }
    }

    /**
     * Hiển thị form chỉnh sửa môn học.
     * Tên hàm cũ: `edit` (trong doGet)
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
            Subject subject = subjectDao.getSubjectById(editId);
            
            if (subject != null) {
                // Kiểm tra quyền truy cập của người dùng đối với môn học này
                Semester currentSemester = semesterDao.getSemesterById(subject.getSemesterId(), userId);
                if (currentSemester == null) {
                    request.setAttribute("errorMessage", "Bạn không có quyền chỉnh sửa môn học này.");
                    response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + subject.getSemesterId());
                    return;
                }

                request.setAttribute("subject", subject);
                request.setAttribute("currentSemester", currentSemester); // Truyền currentSemester
                request.getRequestDispatcher("/components/subject/subject-edit.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Không tìm thấy môn học để chỉnh sửa.");
                // Cố gắng chuyển hướng về trang danh sách môn học của kỳ đó nếu biết semesterId
                int semesterId = -1;
                try {
                    semesterId = Integer.parseInt(request.getParameter("semesterId")); // Lấy từ request nếu có
                } catch (NumberFormatException e) { /* bỏ qua */ }
                response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + semesterId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID môn học không hợp lệ: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/semesters"); // Chuyển về trang kỳ học nếu ID không hợp lệ
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi hiển thị form chỉnh sửa môn học: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Lỗi cơ sở dữ liệu.");
            response.sendRedirect(request.getContextPath() + "/semesters");
        }
    }

    /**
     * Xử lý logic cập nhật môn học vào DB.
     * Tên hàm cũ: `editSubject` (trong doPost)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws ServletException
     * @throws IOException
     */
    private void editSubject(HttpServletRequest request, HttpServletResponse response, int userId)
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

        // Kiểm tra quyền truy cập của người dùng đối với kỳ học chứa môn học này
        try {
            Semester currentSemester = semesterDao.getSemesterById(semesterId, userId);
            if (currentSemester == null) {
                errors.put("general", "Kỳ học không tồn tại hoặc bạn không có quyền chỉnh sửa môn học này.");
            }
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi kiểm tra kỳ học: {0}", e.getMessage());
            errors.put("general", "Lỗi cơ sở dữ liệu khi kiểm tra kỳ học.");
        }

        boolean isActive = true;
        if (isActiveStr != null && !isActiveStr.trim().isEmpty()) {
            isActive = Boolean.parseBoolean(isActiveStr);
        }
        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Tên môn học không được để trống");
        }
        if (code == null || code.trim().isEmpty()) {
            errors.put("code", "Mã môn học không được để trống");
        } else {
            try {
                if (subjectDao.isCodeExistsExceptId(code, semesterId, id)) { // isCodeExistsExceptId
                    errors.put("code", "Mã môn học đã tồn tại cho một môn học khác trong kỳ học này.");
                }
            } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
                LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi kiểm tra mã môn học trùng lặp: {0}", e.getMessage());
                errors.put("general", "Lỗi cơ sở dữ liệu khi kiểm tra mã môn học.");
            }
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
            Subject subjectToEdit = new Subject(id, semesterId, name, code, description, credits, teacherName, isActive, prerequisites, null, null);
            request.setAttribute("subject", subjectToEdit); 
            request.setAttribute("errorMessage", errors);
            request.getRequestDispatcher("/components/subject/subject-edit.jsp").forward(request, response);
            return;
        }

        try {
            Subject subject = new Subject(id, semesterId, name, code, description, credits, teacherName, isActive, prerequisites,
                    null, LocalDateTime.now());
            boolean success = subjectDao.editSubject(subject); // Sử dụng editSubject

            if (!success) {
                errors.put("general", "Có lỗi xảy ra khi cập nhật môn học.");
                request.setAttribute("errors", errors);
                Subject existingSubject = subjectDao.getSubjectById(id);
                request.setAttribute("subject", existingSubject);
                request.getRequestDispatcher("/components/subject/subject-edit.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + semesterId + "&message=editSuccess");
            }
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu khi cập nhật môn học: {0}", e.getMessage());
            errors.put("general", "Lỗi cơ sở dữ liệu khi cập nhật môn học.");
            Subject subjectToEdit = new Subject(id, semesterId, name, code, description, credits, teacherName, isActive, prerequisites, null, null);
            request.setAttribute("subject", subjectToEdit); 
            request.setAttribute("errorMessage", errors);
            request.getRequestDispatcher("/components/subject/subject-edit.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý logic xóa môn học.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws IOException
     * @throws ServletException
     */
    private void deleteSubject(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, ServletException {
        try {
            int deleteId = Integer.parseInt(request.getParameter("id"));
            int semesterId = Integer.parseInt(request.getParameter("semesterId")); 

            Semester semester = semesterDao.getSemesterById(semesterId, userId);
            if (semester == null) {
                LOGGER.log(Level.WARNING, "Người dùng {0} đã cố gắng xóa môn học từ kỳ học không tồn tại hoặc không được phép ID {1}.", new Object[]{((User)request.getSession().getAttribute("loggedInUser")).getUsername(), semesterId});
                request.setAttribute("errorMessage", "Kỳ học không tồn tại hoặc bạn không có quyền xóa môn học này.");
                displaySubjects(request, response, userId); 
                return;
            }

            Subject subjectToDelete = subjectDao.getSubjectById(deleteId);
            if (subjectToDelete == null || subjectToDelete.getSemesterId() != semesterId) {
                LOGGER.log(Level.WARNING, "Người dùng {0} đã cố gắng xóa môn học không tồn tại ID {1} hoặc môn học không thuộc kỳ học {2}.", new Object[]{((User)request.getSession().getAttribute("loggedInUser")).getUsername(), deleteId, semesterId});
                request.setAttribute("errorMessage", "Môn học không tồn tại hoặc không thuộc kỳ học này.");
                displaySubjects(request, response, userId); 
                return;
            }

            boolean success = subjectDao.deleteSubject(deleteId); // Sử dụng deleteSubject
            if (success) {
                LOGGER.log(Level.INFO, "Môn học với ID {0} đã xóa thành công bởi người dùng {1} từ kỳ học {2}.", new Object[]{deleteId, ((User)request.getSession().getAttribute("loggedInUser")).getUsername(), semesterId});
                response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + semesterId + "&message=deleteSuccess");
            } else {
                LOGGER.log(Level.WARNING, "Không thể xóa môn học với ID {0} cho người dùng {1} từ kỳ học {2}.", new Object[]{deleteId, ((User)request.getSession().getAttribute("loggedInUser")).getUsername(), semesterId});
                request.setAttribute("errorMessage", "Không thể xóa môn học. Có thể môn học đang được sử dụng hoặc có lỗi xảy ra.");
                displaySubjects(request, response, userId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID môn học hoặc ID kỳ học không hợp lệ để xóa: {0}, {1}", new Object[]{request.getParameter("id"), request.getParameter("semesterId")});
            request.setAttribute("errorMessage", "ID môn học hoặc ID kỳ học không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/semesters"); 
        } catch (Exception e) { // Đã sửa: Thay SQLException bằng Exception để bắt mọi lỗi
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa môn học.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống khi cố gắng xóa môn học.");
            displaySubjects(request, response, userId);
        }
    }
}
