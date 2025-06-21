/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import dao.SemesterDAO;
import dao.SubjectDAO;
import entity.Semester;
import entity.Subject;
import entity.User;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class SubjectsController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SubjectsController.class.getName());
    SubjectDAO subjectDao = new SubjectDAO();

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
                int semesterId = Integer.parseInt(request.getParameter("semesterId"));
                request.setAttribute("semesterId", semesterId);
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
                subjectDao.deleteSubject(deleteId);
                response.sendRedirect("subjects");
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
                break;
        }
    }

    private void displaySubjectDashboard(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        SemesterDAO semesterDao = new SemesterDAO();
        String search = request.getParameter("search");
        String pageStr = request.getParameter("page");
        String semesterIdStr = request.getParameter("semesterId");
        String isActiveStr = request.getParameter("isActive");

        // Nếu chưa có semesterId → yêu cầu chọn học kỳ
        if (semesterIdStr == null || semesterIdStr.isEmpty()) {
            // Lấy danh sách học kỳ của user
            List<Semester> semesters = semesterDao.selectAllSemesters(user.getId());
            request.setAttribute("semesters", semesters);
            request.getRequestDispatcher("/components/subject/semester-select.jsp").forward(request, response);
            return;
        }

        // Có semesterId → chuyển sang load subject
        int semesterId = Integer.parseInt(semesterIdStr);
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
        request.setAttribute("semesterId", semesterId);
        System.out.println("subjects: " + subjects);
        request.getRequestDispatcher("/components/subject/subject-dashboard.jsp").forward(request, response);
    }

    private void addSubject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String> errors = new HashMap<>();

        int semesterId = Integer.parseInt(request.getParameter("semesterId"));
        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String description = request.getParameter("description");
        String creditsStr = request.getParameter("credits");
        String teacherName = request.getParameter("teacherName");
        String prerequisites = request.getParameter("prerequisites");
        String isActiveStr = request.getParameter("isActive");

       
        try {
            String semesterIdStr = request.getParameter("semesterId");
            semesterId = Integer.parseInt(semesterIdStr);
            if (semesterId <= 0) {
                errors.put("semesterId", "Vui lòng chọn học kỳ hợp lệ");
            }
        } catch (NumberFormatException e) {
            errors.put("semesterId", "Học kỳ không hợp lệ");
        }

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
        } else if (subjectDao.isCodeExists(code)) {
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
            request.setAttribute("errors", errors);
            request.getRequestDispatcher("/components/subject/subject-add.jsp").forward(request, response);
            return;
        }

        Subject subject = new Subject(semesterId, name, code, description, credits, teacherName, isActive, prerequisites,
                LocalDateTime.now(), LocalDateTime.now());
        boolean success = subjectDao.insertSubject(subject);

        if (!success) {
            errors.put("general", "Có lỗi xảy ra khi thêm môn học");
            request.setAttribute("errors", errors);
            request.getRequestDispatcher("/components/subject/subject-add.jsp").forward(request, response);
        } else {
            response.sendRedirect("subjects?semesterId=" + semesterId);
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
        // Validate dữ liệu
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
            Subject subject = subjectDao.getSubjectById(id);
            request.setAttribute("subject", subject);
            request.setAttribute("errors", errors);
            request.getRequestDispatcher("/components/subject/subject-edit.jsp").forward(request, response);
            return;
        }

        Subject subject = new Subject(id, semesterId, name, code, description, credits, teacherName, isActive, prerequisites,
                LocalDateTime.now(), LocalDateTime.now());
        boolean success = subjectDao.updateSubject(subject);

        if (!success) {
            errors.put("general", "Có lỗi xảy ra khi cập nhật môn học");
            request.setAttribute("errors", errors);
            request.getRequestDispatcher("/components/subject/subject-edit.jsp").forward(request, response);
        } else {
            response.sendRedirect("subjects?semesterId=" + semesterId);
        }
    }
}
