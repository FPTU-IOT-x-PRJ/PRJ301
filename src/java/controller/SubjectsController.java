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
    // Khai báo SemesterDAO ở đây để dùng chung
    SemesterDAO semesterDao = new SemesterDAO(); 

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

        switch (action == null ? "" : action) {
            case "/add":
                int semesterIdForAdd = Integer.parseInt(request.getParameter("semesterId"));
                request.setAttribute("semesterId", semesterIdForAdd);
                request.getRequestDispatcher("/components/subject/subject-add.jsp").forward(request, response);
                break;
            case "/edit":
                int editId = Integer.parseInt(request.getParameter("id"));
                Subject subject = subjectDao.getSubjectById(editId);
                request.setAttribute("subject", subject);
                request.getRequestDispatcher("/components/subject/subject-edit.jsp").forward(request, response);
                break;
            case "/delete":
                int deleteId = Integer.parseInt(request.getParameter("id"));
                int currentSemesterIdAfterDelete = Integer.parseInt(request.getParameter("semesterId")); // Lấy semesterId hiện tại để redirect về đúng trang
                subjectDao.deleteSubject(deleteId);
                response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + currentSemesterIdAfterDelete); // Chỉnh sửa để redirect đúng
                break;
            default:
                displaySubjectDashboard(request, response, user);
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
                addSubject(request, response);
                break;
            case "/edit":
                editSubject(request, response);
                break;
            default:
                // Xử lý POST mặc định nếu cần
                break;
        }
    }

    private void displaySubjectDashboard(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        // SemesterDAO semesterDao = new SemesterDAO(); // Đã khai báo ở trên
        String search = request.getParameter("search");
        String pageStr = request.getParameter("page");
        String semesterIdStr = request.getParameter("semesterId");
        String isActiveStr = request.getParameter("isActive");

        int semesterId;
        // Nếu chưa có semesterId trên URL
        if (semesterIdStr == null || semesterIdStr.isEmpty()) {
            // Lấy kỳ học mới nhất của người dùng
            Semester latestSemester = semesterDao.getLatestSemester(user.getId());
            if (latestSemester != null) {
                // Nếu tìm thấy kỳ học mới nhất, chuyển hướng về chính URL này
                // với tham số semesterId để hiển thị môn học của kỳ đó
                response.sendRedirect(request.getContextPath() + "/subjects?semesterId=" + latestSemester.getId());
                return; // Rất quan trọng: Dừng xử lý hiện tại và đợi redirect
            } else {
                // Nếu không có kỳ học nào, có thể chuyển hướng đến trang thông báo
                // hoặc hiển thị thông báo "chưa có kỳ học nào" trên dashboard
                // Hiện tại, tôi sẽ forward đến một JSP thông báo hoặc một trang dashboard trống
                request.setAttribute("errorMessage", "Bạn chưa có kỳ học nào. Vui lòng thêm kỳ học mới.");
                request.getRequestDispatcher("/components/subject/no-semester-found.jsp").forward(request, response);
                return;
            }
        } else {
            // Có semesterId trên URL, parse và tiếp tục xử lý
            semesterId = Integer.parseInt(semesterIdStr);
        }

        int page = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
        int pageSize = 10;
        int offset = (page - 1) * pageSize;
        Boolean isActive = (isActiveStr != null && !isActiveStr.isEmpty()) ? Boolean.parseBoolean(isActiveStr) : null;

        List<Subject> subjects = subjectDao.getFilteredAndPaginatedSubjects(search, semesterId, isActive, offset, pageSize, user.getId());
        int totalSubjects = subjectDao.getTotalSubjectCount(search, semesterId);
        int totalPages = (int) Math.ceil((double) totalSubjects / pageSize);

        // Lấy học kỳ hiện tại và danh sách tất cả học kỳ
        Semester currentSemester = semesterDao.getSemesterById(semesterId, user.getId());
        List<Semester> allSemesters = semesterDao.selectAllSemesters(user.getId());

        // Đặt attribute để truyền sang JSP
        request.setAttribute("currentSemester", currentSemester);
        request.setAttribute("allSemesters", allSemesters);
        request.setAttribute("semesterId", semesterId); // để giữ giá trị selected trong dropdown

        request.setAttribute("subjects", subjects);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);
        // request.setAttribute("semesterId", semesterId); // Đã đặt ở trên
        System.out.println("subjects: " + subjects);
        request.getRequestDispatcher("/components/subject/subject-dashboard.jsp").forward(request, response);
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
        } else if (subjectDao.isCodeExists(code)) { // isCodeExists cần kiểm tra thêm userId hoặc semesterId nếu muốn mã môn là duy nhất trong phạm vi đó
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
            request.setAttribute("errors", errors);
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
        boolean success = subjectDao.insertSubject(subject);

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
        } else if (subjectDao.isCodeExistsExceptId(code, id)) {
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
            request.setAttribute("errors", errors);

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
        boolean success = subjectDao.updateSubject(subject);

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
}