package controller;

import dao.LessonDAO; 
import dao.SemesterDAO; 
import entity.Lesson;
import entity.Semester;
import entity.User; 
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CalendarController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CalendarController.class.getName());
    private SemesterDAO semesterDao;
    private LessonDAO lessonDao; 

    @Override
    public void init() throws ServletException {
        super.init();
        semesterDao = new SemesterDAO();
        lessonDao = new LessonDAO(); 
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        User user = (User) session.getAttribute("loggedInUser");

        String action = request.getPathInfo();
        if (action == null || action.equals("/")) {
            displayCalendar(request, response, user);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void displayCalendar(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        try {
            String semesterIdStr = request.getParameter("semesterId");
            int selectedSemesterId;
            Semester currentSemester = null;
            
            // Lấy danh sách tất cả kỳ học để hiển thị trong dropdown
            List<Semester> allSemesters = semesterDao.selectAllSemesters(user.getId());
            request.setAttribute("semesters", allSemesters);

            // Xử lý logic nếu không có semesterId được truyền
            if (semesterIdStr == null || semesterIdStr.isEmpty()) {
                Semester latestSemester = semesterDao.getLatestSemester(user.getId()); // Cần phương thức này trong SemesterDAO
                if (latestSemester != null) {
                    // Chuyển hướng để URL có semesterId, tránh lỗi khi người dùng refresh
                    response.sendRedirect(request.getContextPath() + "/calendar?semesterId=" + latestSemester.getId());
                    return; // Dừng xử lý hiện tại
                } else {
                    // Nếu không có kỳ học nào, hiển thị thông báo lỗi
                    request.setAttribute("errorMessage", "Bạn chưa có kỳ học nào. Vui lòng thêm kỳ học mới.");
                    request.getRequestDispatcher("/components/calendar/no-semester-found.jsp").forward(request, response);
                    return; // Dừng xử lý hiện tại
                }
            } else {
                // Nếu có semesterId được truyền, parse nó
                selectedSemesterId = Integer.parseInt(semesterIdStr);
            }

            // Lấy thông tin kỳ học hiện tại
            currentSemester = semesterDao.getSemesterById(selectedSemesterId, user.getId());
            if (currentSemester == null) {
                // Nếu semesterId không hợp lệ hoặc không thuộc về user
                request.setAttribute("errorMessage", "Không tìm thấy kỳ học bạn muốn xem lịch hoặc bạn không có quyền truy cập.");
                request.getRequestDispatcher("/components/calendar/no-semester-found.jsp").forward(request, response);
                return;
            }
            request.setAttribute("currentSemester", currentSemester);

            // Lấy và nhóm lessons theo ngày
            Map<LocalDate, List<Lesson>> lessonsByDate = new HashMap<>();
            List<Lesson> lessons = lessonDao.getAllLessonsForSemester(currentSemester.getId(), user.getId()); 
            for (Lesson lesson : lessons) {
                LocalDate lessonLocalDate = lesson.getLessonDate().toLocalDate(); 
                lessonsByDate.computeIfAbsent(lessonLocalDate, k -> new ArrayList<>()).add(lesson);
            }
            request.setAttribute("lessonsByDate", lessonsByDate);

            // Forward tới JSP để hiển thị
            request.getRequestDispatcher("/components/calendar/calendar-view.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid semester ID provided.", e);
            request.setAttribute("errorMessage", "ID kỳ học không hợp lệ.");
            try {
                // Thử chuyển hướng lại về lịch không có semesterId để nó tìm latest
                response.sendRedirect(request.getContextPath() + "/calendar");
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error redirecting after invalid semester ID", ex);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi không mong muốn.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error when displaying calendar.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi cơ sở dữ liệu khi tải lịch.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi không mong muốn.");
        }
    }
}