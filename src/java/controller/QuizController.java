package controller;

import dao.QuizDAO;
import dao.SubjectDAO;
import entity.Quizz;
import entity.Quizzes;
import entity.Subject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebServlet(name = "QuizController", urlPatterns = {"/quizzes", "/quizzes/*"})
public class QuizController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(QuizController.class.getName());
    private QuizDAO quizDao = new QuizDAO();
    private SubjectDAO subjectDao = new SubjectDAO(); // Để lấy thông tin môn học

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            if (action == null || action.equals("/")) {
                displayQuizzes(request, response);
            } else if (action.equals("/add")) {
                showAddForm(request, response);
            } else if (action.equals("/detail")) {
                showQuizDetail(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action Not Found");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in QuizController doGet: " + action, e);
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

        try {
            if (action == null || action.equals("/") || action.equals("/add")) {
                addQuiz(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action Not Found");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in QuizController doPost: " + action, e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void displayQuizzes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String subjectIdStr = request.getParameter("subjectId");
        try {
            int subjectId = Integer.parseInt(subjectIdStr);
            Subject subject = subjectDao.getSubjectById(subjectId);
            request.setAttribute("subject", subject);
            // Bạn có thể thêm getAllQuizBySubjectId nếu cần
            request.getRequestDispatcher("/components/quiz/quiz-list.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid subject ID for quiz list");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID môn học không hợp lệ.");
        }
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String subjectId = request.getParameter("subjectId");
        Subject subject = subjectDao.getSubjectById(Integer.parseInt(subjectId));
        request.setAttribute("subjectId", subjectId);
        request.setAttribute("currentSubject", subject);
        request.getRequestDispatcher("/components/quiz/quiz-add.jsp").forward(request, response);
    }

    private void showQuizDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int quizId = Integer.parseInt(request.getParameter("id"));
            Quizz quiz = quizDao.getQuizById(quizId);
            if (quiz != null) {
                List<Quizzes> questions = quizDao.getQuestionsByQuizId(quizId);
                request.setAttribute("quiz", quiz);
                request.setAttribute("questions", questions);
                request.getRequestDispatcher("/components/quiz/quiz-detail.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Quiz không tồn tại.");
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid quiz ID for detail view");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID quiz không hợp lệ.");
        }
    }

    private void addQuiz(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int subjectId = Integer.parseInt(request.getParameter("subjectId"));
            String title = request.getParameter("title");
            String description = request.getParameter("description");

            Quizz quiz = new Quizz();
            quiz.setSubjectId(subjectId);
            quiz.setTitle(title);
            quiz.setDescription(description);

            if (quizDao.addQuiz(quiz)) {
                // Lấy dữ liệu từ form
                List<Quizzes> questions = new ArrayList<>();
                int numQuestions = Integer.parseInt(request.getParameter("numQuestions"));

                for (int i = 1; i <= numQuestions; i++) {
                    String question = request.getParameter("question_" + i);
                    String options = request.getParameter("options_" + i); // dạng "A|B|C|D"
                    String answers = request.getParameter("answers_" + i); // dạng "1|2" hoặc "2"

                    Quizzes q = new Quizzes();
                    q.setQuizId(quiz.getId());
                    q.setQuestion(question);
                    q.setOptions(options);
                    q.setAnswers(answers);
                    questions.add(q);
                }

                quizDao.addQuestions(quiz.getId(), questions);
            }

            response.sendRedirect(request.getContextPath() + "/quizzes?subjectId=" + subjectId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding quiz", e);
            request.setAttribute("errorMessage", "Lỗi khi thêm quiz.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}
