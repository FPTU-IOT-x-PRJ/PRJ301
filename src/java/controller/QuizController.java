// src/java/controller/QuizController.java
package controller;

import dao.AnswerOptionDAO;

import dao.QuestionDAO;
import dao.QuizDAO;
import dao.SubjectDAO;
import entity.AnswerOption;

import entity.Question;
import entity.Quiz;
import entity.Subject;
import entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(QuizController.class.getName());

    private final QuizDAO quizDao = new QuizDAO();
    private final SubjectDAO subjectDao = new SubjectDAO();
    private final QuestionDAO questionDao = new QuestionDAO();
    private final AnswerOptionDAO answerOptionDao = new AnswerOptionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                    showAddQuizForm(request, response, user);
                    break;
                case "/detail":
                    showQuizDetail(request, response, user);
                    break;
                case "/edit":
                    showEditQuizForm(request, response, user);
                    break;
                case "/delete-confirm":
                    showDeleteConfirm(request, response, user);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in QuizController doGet", e);
            request.setAttribute("errorMessage", "Đã có lỗi xảy ra. Vui lòng thử lại.");
            request.getRequestDispatcher("/components/errorGeneral.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getPathInfo();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        User user = (User) session.getAttribute("loggedInUser");

        try {
            switch (action != null ? action : "") {
                case "/add":
                    processAddQuiz(request, response, user);
                    break;
                case "/edit":
                    processEditQuiz(request, response, user);
                    break;
                case "/delete":
                    processDeleteQuiz(request, response, user);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in QuizController doPost", e);
            request.setAttribute("errorMessage", "Thao tác thất bại do lỗi hệ thống.");
            request.getRequestDispatcher("/components/errorGeneral.jsp").forward(request, response);
        }
    }

    private void showAddQuizForm(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        String subjectIdParam = request.getParameter("subjectId");
        int subjectId;

        if (subjectIdParam != null && !subjectIdParam.trim().isEmpty()) {
            try {
                subjectId = Integer.parseInt(subjectIdParam);
            } catch (NumberFormatException e) {
                // Xử lý lỗi: subjectId không phải là số hợp lệ
                // Ví dụ: log lỗi, chuyển hướng đến trang báo lỗi hoặc trang danh sách môn học
                System.err.println("Lỗi: subjectId không hợp lệ: " + subjectIdParam);
                response.sendRedirect(request.getContextPath() + "/errorPage.jsp"); // hoặc một URL phù hợp
                return;
            }
        } else {
            // Xử lý lỗi: subjectId bị thiếu
            // Ví dụ: log lỗi, chuyển hướng đến trang báo lỗi hoặc trang danh sách môn học
            System.err.println("Lỗi: subjectId bị thiếu trong yêu cầu.");
            response.sendRedirect(request.getContextPath() + "/errorPage.jsp"); // hoặc một URL phù hợp
            return;
        }

        Subject subject = subjectDao.getSubjectById(subjectId);
        // TODO: Check ownership
        request.setAttribute("subject", subject);
        request.getRequestDispatcher("/components/quiz/quiz-add.jsp").forward(request, response);
    }

    private void showQuizDetail(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        int quizId = Integer.parseInt(request.getParameter("id"));
        Quiz quiz = quizDao.getQuizById(quizId);
        // TODO: Check ownership
        if (quiz == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        List<Question> questions = questionDao.getQuestionsByQuizId(quizId);
        for (Question q : questions) {
            q.setAnswerOptions(answerOptionDao.getAnswerOptionsByQuestionId(q.getId()));
        }

        request.setAttribute("quiz", quiz);
        request.setAttribute("questions", questions);
        request.getRequestDispatcher("/components/quiz/quiz-detail.jsp").forward(request, response);
    }

    private void showEditQuizForm(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        int quizId = Integer.parseInt(request.getParameter("id"));
        Quiz quiz = quizDao.getQuizById(quizId);
        if (quiz == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // TODO: Check ownership

        List<Question> questions = questionDao.getQuestionsByQuizId(quizId);
        for (Question q : questions) {
            q.setAnswerOptions(answerOptionDao.getAnswerOptionsByQuestionId(q.getId()));
        }

        request.setAttribute("quiz", quiz);
        request.setAttribute("questions", questions);
        request.getRequestDispatcher("/components/quiz/quiz-edit.jsp").forward(request, response);
    }

    private void showDeleteConfirm(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        int quizId = Integer.parseInt(request.getParameter("id"));
        Quiz quiz = quizDao.getQuizById(quizId);
        if (quiz == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // TODO: Check ownership
        request.setAttribute("quizToDelete", quiz);
        request.getRequestDispatcher("/components/quiz/quiz-delete-confirm.jsp").forward(request, response);
    }

    private void processAddQuiz(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        int subjectId = Integer.parseInt(request.getParameter("subjectId"));
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        // TODO: Check ownership

        // Step 1: Add Quiz to get ID
        Quiz newQuiz = new Quiz(subjectId, title, description);
        int quizId = quizDao.addQuiz(newQuiz);

        if (quizId > 0) {
            // Step 2: Add Questions and Options
            saveQuestionsAndOptions(request, quizId);
            LOGGER.log(Level.INFO, "Quiz added with ID: {0}", quizId);
            response.sendRedirect(request.getContextPath() + "/quizzes/detail?id=" + quizId);
        } else {
            response.sendRedirect(request.getContextPath() + "/subjects/detail?id=" + subjectId + "&error=addFailed");
        }
    }

    private void processEditQuiz(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        int quizId = Integer.parseInt(request.getParameter("quizId"));
        String title = request.getParameter("title");
        String description = request.getParameter("description");

        Quiz quiz = quizDao.getQuizById(quizId);
        if (quiz == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // TODO: Check ownership

        // Step 1: Update Quiz info
        quiz.setTitle(title);
        quiz.setDescription(description);
        quizDao.updateQuiz(quiz);

        // Step 2: Delete all old questions and options
        // This is the simplest strategy. For complex apps, you might track changes.
        List<Question> oldQuestions = questionDao.getQuestionsByQuizId(quizId);
        for (Question q : oldQuestions) {
            answerOptionDao.deleteAnswerOptionsByQuestionId(q.getId());
        }
        questionDao.deleteQuestionsByQuizId(quizId);

        // Step 3: Add the questions and options from the form as if they are new
        saveQuestionsAndOptions(request, quizId);

        LOGGER.log(Level.INFO, "Quiz updated with ID: {0}", quizId);
        response.sendRedirect(request.getContextPath() + "/quizzes/detail?id=" + quizId);
    }

    private void processDeleteQuiz(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        int quizId = Integer.parseInt(request.getParameter("id"));
        Quiz quiz = quizDao.getQuizById(quizId);
        if (quiz == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // TODO: Check ownership

        // The DB is set to ON DELETE CASCADE, so this is enough.
        // If not using cascade, you must delete children first:
        // List<Question> questions = questionDao.getQuestionsByQuizId(quizId);
        // for (Question q : questions) {
        //     answerOptionDao.deleteAnswerOptionsByQuestionId(q.getId());
        // }
        // questionDao.deleteQuestionsByQuizId(quizId);
        quizDao.deleteQuiz(quizId);

        LOGGER.log(Level.INFO, "Quiz deleted with ID: {0}", quizId);
        response.sendRedirect(request.getContextPath() + "/subjects/detail?id=" + quiz.getSubjectId());
    }

    /**
     * Helper method to parse dynamic form data and save questions/options. This
     * is used by both add and edit actions.
     */
    private void saveQuestionsAndOptions(HttpServletRequest request, int quizId) {
        String[] questionTexts = request.getParameterValues("questionText");

        if (questionTexts == null || questionTexts.length == 0) {
            return; // No questions to add
        }

        for (int i = 0; i < questionTexts.length; i++) {
            String qText = questionTexts[i];
            if (qText == null || qText.trim().isEmpty()) {
                continue; // Skip empty question blocks
            }

            // Step 2a: Add the question to get its ID
            Question newQuestion = new Question(quizId, qText, "MULTIPLE_CHOICE");
            int questionId = questionDao.addQuestion(newQuestion);

            if (questionId > 0) {
                // Step 2b: Get options and correct answer for this question
                // The name of the option text inputs is "optionText_q" + (i)
                String[] optionTexts = request.getParameterValues("optionText_q" + i);
                // The name of the radio button for the correct answer is "isCorrect_q" + (i)
                String correctOptionIndexStr = request.getParameter("isCorrect_q" + i);
                int correctOptionIndex = -1;
                if (correctOptionIndexStr != null) {
                    correctOptionIndex = Integer.parseInt(correctOptionIndexStr);
                }

                if (optionTexts != null) {
                    for (int j = 0; j < optionTexts.length; j++) {
                        String oText = optionTexts[j];
                        if (oText != null && !oText.trim().isEmpty()) {
                            boolean isCorrect = (j == correctOptionIndex);
                            AnswerOption newOption = new AnswerOption(questionId, oText, isCorrect);
                            answerOptionDao.addAnswerOption(newOption);
                        }
                    }
                }
            }
        }
    }
}
