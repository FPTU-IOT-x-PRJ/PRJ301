// src/java/controller/QuizController.java
package controller;

import dao.AnswerOptionDAO;

import dao.QuestionDAO;
import dao.QuizDAO;
import dao.SubjectDAO;
import dao.SubmissionDAO;
import entity.AnswerOption;

import entity.Question;
import entity.Quiz;
import entity.Subject;
import entity.Submission;
import entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(QuizController.class.getName());

    private final QuizDAO quizDao = new QuizDAO();
    private final SubjectDAO subjectDao = new SubjectDAO();
    private final QuestionDAO questionDao = new QuestionDAO();
    private final AnswerOptionDAO answerOptionDao = new AnswerOptionDAO();
    private final SubmissionDAO submissionDao = new SubmissionDAO();

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
                case "/take": // Thêm case này
                    showTakeQuizForm(request, response, user);
                    break;
                case "/submissions": // Thêm case này
                    showQuizSubmissions(request, response, user);
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
                case "/take": // Thêm case này
                    processTakeQuiz(request, response, user);
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

    private void showTakeQuizForm(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        int quizId = Integer.parseInt(request.getParameter("id"));
        Quiz quiz = quizDao.getQuizById(quizId);
        if (quiz == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Lấy thời gian làm bài từ request
        String durationParam = request.getParameter("duration");
        int quizDuration = 10; // Giá trị mặc định nếu không có hoặc lỗi
        if (durationParam != null && !durationParam.trim().isEmpty()) {
            try {
                quizDuration = Integer.parseInt(durationParam);
                if (quizDuration <= 0) { // Đảm bảo thời gian là số dương
                    quizDuration = 10;
                }
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Không thể parse thời gian làm bài từ param: {0}", durationParam);
                // Giữ giá trị mặc định là 10 phút
            }
        }

        // Tải các câu hỏi và đáp án cho quiz
        List<Question> questions = questionDao.getQuestionsByQuizId(quizId);
        for (Question q : questions) {
            q.setAnswerOptions(answerOptionDao.getAnswerOptionsByQuestionId(q.getId()));
        }

        request.setAttribute("quiz", quiz);
        request.setAttribute("questions", questions);
        request.setAttribute("quizDuration", quizDuration); // Sử dụng thời gian làm bài từ request
        request.getRequestDispatcher("/components/quiz/quiz-take.jsp").forward(request, response);
    }

    private void processTakeQuiz(HttpServletRequest request, HttpServletResponse response, User user) throws IOException, ServletException {
        int quizId = Integer.parseInt(request.getParameter("quizId"));
        long startTimeMillis = Long.parseLong(request.getParameter("startTimeMillis"));
        long endTimeMillis = System.currentTimeMillis();
        int timeTakenMinutes = (int) Math.ceil((endTimeMillis - startTimeMillis) / (1000.0 * 60)); // Tính thời gian làm bài

        Quiz quiz = quizDao.getQuizById(quizId);
        if (quiz == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        List<Question> questions = questionDao.getQuestionsByQuizId(quizId);
        int correctAnswers = 0;
        int totalQuestions = questions.size();

        for (Question q : questions) {
            // Lấy các lựa chọn đáp án đúng cho câu hỏi này từ DB
            List<AnswerOption> correctOptions = answerOptionDao.getAnswerOptionsByQuestionId(q.getId());

            // Lấy câu trả lời của người dùng từ form
            // Tên của input radio cho mỗi câu hỏi là "question_" + question.getId()
            String userAnswerIdStr = request.getParameter("question_" + q.getId());

            if (userAnswerIdStr != null) {
                try {
                    int userAnswerId = Integer.parseInt(userAnswerIdStr);
                    boolean isCorrect = false;
                    for (AnswerOption option : correctOptions) {
                        if (option.getId() == userAnswerId && option.isIsCorrect()) {
                            isCorrect = true;
                            break;
                        }
                    }
                    if (isCorrect) {
                        correctAnswers++;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid answer ID for question {0}: {1}", new Object[]{q.getId(), userAnswerIdStr});
                }
            }
        }

        // Tính điểm (có thể làm phức tạp hơn, ví dụ: điểm trên tổng số câu hỏi)
        int score = (totalQuestions > 0) ? (int) Math.round((double) correctAnswers / totalQuestions * 100) : 0; // Điểm theo phần trăm

        // Lưu submission
        Submission submission = new Submission(quizId, user.getId(), score, timeTakenMinutes);
        int submissionId = submissionDao.addSubmission(submission);

        if (submissionId > 0) {
            request.setAttribute("quiz", quiz);
            request.setAttribute("score", score);
            request.setAttribute("correctAnswers", correctAnswers);
            request.setAttribute("totalQuestions", totalQuestions);
            request.setAttribute("timeTakenMinutes", timeTakenMinutes);
            request.getRequestDispatcher("/components/quiz/quiz-result.jsp").forward(request, response);
        } else {
            // Xử lý lỗi khi không lưu được submission
            request.setAttribute("errorMessage", "Không thể lưu kết quả bài làm.");
            request.getRequestDispatcher("/components/errorGeneral.jsp").forward(request, response);
        }
    }

    private void showQuizSubmissions(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        int quizId = Integer.parseInt(request.getParameter("id"));
        Quiz quiz = quizDao.getQuizById(quizId);
        if (quiz == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Lấy tất cả submission của quiz này
        List<Submission> submissions = submissionDao.getSubmissionsByQuizId(quizId);

        request.setAttribute("quiz", quiz);
        request.setAttribute("submissions", submissions);
        request.getRequestDispatcher("/components/quiz/quiz-submissions.jsp").forward(request, response);
    }

    /**
     * Helper method to parse dynamic form data and save questions/options. This
     * is used by both add and edit actions.
     */
    private void saveQuestionsAndOptions(HttpServletRequest request, int quizId) {
        // Lấy tất cả các tên tham số từ request
        Enumeration<String> parameterNames = request.getParameterNames();
        List<Integer> submittedQuestionIndices = new ArrayList<>();

        LOGGER.log(Level.INFO, "Start processing parameters for quizId: {0}", quizId);

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            LOGGER.log(Level.INFO, "Parameter name received: {0}", paramName); // Thêm dòng này để log tất cả tên tham số

            if (paramName.startsWith("questionText_")) {
                try {
                    int qIdx = Integer.parseInt(paramName.substring("questionText_".length()));
                    submittedQuestionIndices.add(qIdx);
                    LOGGER.log(Level.INFO, "Found question index: {0} from param: {1}", new Object[]{qIdx, paramName});
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid questionText parameter name format: {0}", paramName);
                }
            }
        }
        LOGGER.log(Level.INFO, "Submitted question indices found: {0}", submittedQuestionIndices);

        // Sắp xếp các chỉ số để xử lý câu hỏi theo thứ tự (tùy chọn nhưng nên làm)
        Collections.sort(submittedQuestionIndices);

        if (submittedQuestionIndices.isEmpty()) {
            return; // Không có câu hỏi nào để thêm
        }

        // Bước 2: Duyệt qua từng chỉ số câu hỏi đã tìm được
        for (int qIdx : submittedQuestionIndices) {
            String qText = request.getParameter("questionText_" + qIdx);
            String questionType = request.getParameter("questionType_" + qIdx); // Lấy loại câu hỏi tương ứng

            if (qText == null || qText.trim().isEmpty()) {
                continue; // Bỏ qua khối câu hỏi rỗng
            }

            // Bước 2a: Thêm câu hỏi để lấy ID
            // Mặc định là MULTIPLE_CHOICE nếu questionType là null hoặc rỗng
            if (questionType == null || questionType.trim().isEmpty()) {
                questionType = "MULTIPLE_CHOICE";
            }

            Question newQuestion = new Question(quizId, qText, questionType);
            int questionId = questionDao.addQuestion(newQuestion);

            if (questionId > 0) {
                // Bước 2b: Lấy các lựa chọn và đáp án đúng cho câu hỏi này
                // Tên của các input text cho lựa chọn là "optionText_q" + qIdx
                String[] optionTexts = request.getParameterValues("optionText_q" + qIdx);
                // Tên của radio button cho đáp án đúng là "isCorrect_q" + qIdx
                String correctOptionIndexStr = request.getParameter("isCorrect_q" + qIdx);
                int correctOptionIndex = -1;
                if (correctOptionIndexStr != null && !correctOptionIndexStr.isEmpty()) {
                    try {
                        correctOptionIndex = Integer.parseInt(correctOptionIndexStr);
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.WARNING, "Invalid correctOptionIndexStr for qIdx {0}: {1}", new Object[]{qIdx, correctOptionIndexStr});
                    }
                }

                if (optionTexts != null) {
                    for (int j = 0; j < optionTexts.length; j++) {
                        String oText = optionTexts[j];
                        if (oText != null && !oText.trim().isEmpty()) {
                            boolean isCorrect = (j == correctOptionIndex);
                            AnswerOption newOption = new AnswerOption(questionId, oText, isCorrect);
                            boolean added = answerOptionDao.addAnswerOption(newOption);
                            if (!added) {
                                // Log lỗi nếu không thêm được lựa chọn
                                LOGGER.log(Level.SEVERE, "Failed to add answer option: {0} for questionId: {1}", new Object[]{oText, questionId});
                            }
                        }
                    }
                }
            } else {
                // Log lỗi nếu không thêm được câu hỏi
                LOGGER.log(Level.SEVERE, "Failed to add question for quizId: {0} with text: {1}", new Object[]{quizId, qText});
            }
        }
    }

}
