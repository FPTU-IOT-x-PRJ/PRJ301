// src/java/controller/QuizController.java
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dao.AnswerOptionDAO;
import dao.DocumentDAO;

import dao.QuestionDAO;
import dao.QuizDAO;
import dao.SubjectDAO;
import dao.SubmissionDAO;
import entity.AnswerOption;
import entity.Document;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(QuizController.class.getName());

    private final SubjectDAO subjectDao = new SubjectDAO();
    private final DocumentDAO documentDao = new DocumentDAO();
    private final QuizDAO quizDao = new QuizDAO();
    private final QuestionDAO questionDao = new QuestionDAO();
    private final AnswerOptionDAO answerOptionDao = new AnswerOptionDAO();
    private final SubmissionDAO submissionDao = new SubmissionDAO();
    private final Gson gson = new Gson();

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
                case "/take":
                    processTakeQuiz(request, response, user);
                    break;
                case "/generateQuiz":
                    processGenerateQuizFromDocument(request, response, user);
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
                        if (option.getId() == userAnswerId && option.isCorrect()) {
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
            LOGGER.log(Level.INFO, "Parameter name received: {0}", paramName);

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

        // Sắp xếp các chỉ số để xử lý câu hỏi theo thứ tự
        Collections.sort(submittedQuestionIndices);

        if (submittedQuestionIndices.isEmpty()) {
            return; // Không có câu hỏi nào để thêm
        }

        // Duyệt qua từng chỉ số câu hỏi đã tìm được
        for (int qIdx : submittedQuestionIndices) {
            String qText = request.getParameter("questionText_" + qIdx);
            String questionType = request.getParameter("questionType_" + qIdx);

            if (qText == null || qText.trim().isEmpty()) {
                continue; // Bỏ qua khối câu hỏi rỗng
            }

            // Mặc định là MULTIPLE_CHOICE nếu questionType là null hoặc rỗng
            if (questionType == null || questionType.trim().isEmpty()) {
                questionType = "MULTIPLE_CHOICE";
            }

            Question newQuestion = new Question(quizId, qText, questionType);
            int questionId = questionDao.addQuestion(newQuestion);

            if (questionId > 0) {
                // FIX: Lấy các lựa chọn và đáp án đúng cho câu hỏi này
                String[] optionTexts = request.getParameterValues("optionText_q" + qIdx);
                String correctOptionIndexStr = request.getParameter("isCorrect_q" + qIdx);

                LOGGER.log(Level.INFO, "Processing question {0}: optionTexts count = {1}, correctOptionIndexStr = {2}",
                        new Object[]{qIdx, optionTexts != null ? optionTexts.length : 0, correctOptionIndexStr});

                int correctOptionIndex = -1;
                if (correctOptionIndexStr != null && !correctOptionIndexStr.isEmpty()) {
                    try {
                        correctOptionIndex = Integer.parseInt(correctOptionIndexStr);
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.WARNING, "Invalid correctOptionIndexStr for qIdx {0}: {1}", new Object[]{qIdx, correctOptionIndexStr});
                    }
                }

                if (optionTexts != null && optionTexts.length > 0) {
                    // FIX: Lọc bỏ các lựa chọn rỗng trước khi xử lý
                    List<String> validOptions = new ArrayList<>();
                    for (String oText : optionTexts) {
                        if (oText != null && !oText.trim().isEmpty()) {
                            validOptions.add(oText.trim());
                        }
                    }

                    LOGGER.log(Level.INFO, "Valid options count: {0}", validOptions.size());

                    // FIX: Xử lý với danh sách đã được lọc
                    for (int j = 0; j < validOptions.size(); j++) {
                        String oText = validOptions.get(j);
                        boolean isCorrect = (j == correctOptionIndex);

                        LOGGER.log(Level.INFO, "Adding option {0}: text = {1}, isCorrect = {2}",
                                new Object[]{j, oText, isCorrect});

                        AnswerOption newOption = new AnswerOption(questionId, oText, isCorrect);
                        boolean added = answerOptionDao.addAnswerOption(newOption);
                        if (!added) {
                            LOGGER.log(Level.SEVERE, "Failed to add answer option: {0} for questionId: {1}",
                                    new Object[]{oText, questionId});
                        } else {
                            LOGGER.log(Level.INFO, "Successfully added answer option: {0} for questionId: {1}",
                                    new Object[]{oText, questionId});
                        }
                    }
                } else {
                    LOGGER.log(Level.WARNING, "No valid options found for question {0}", qIdx);
                }
            } else {
                LOGGER.log(Level.SEVERE, "Failed to add question for quizId: {0} with text: {1}",
                        new Object[]{quizId, qText});
            }
        }
    }

    private void processGenerateQuizFromDocument(HttpServletRequest request, HttpServletResponse response, User user) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        Path subjectTempDirPath = null; // Đường dẫn thư mục tạm
        File inputFile = null;          // File tài liệu tải về (input cho Python)
        File outputFile = null;         // File JSON do Python tạo ra (output)

        try {
            // === BƯỚC 1: LẤY THAM SỐ VÀ VALIDATE ===
            String documentIdParam = request.getParameter("documentId");
            if (documentIdParam == null || documentIdParam.trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Tham số documentId bị thiếu.");
                return;
            }

            int documentId = Integer.parseInt(documentIdParam);
            Document document = documentDao.getDocumentById(documentId, user.getId());
            if (document == null || document.getFilePath() == null || document.getFilePath().trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài liệu hoặc tài liệu không có URL file đính kèm.");
                return;
            }

            // === BƯỚC 2: CHUẨN BỊ THƯ MỤC TẠM, FILE INPUT VÀ FILE OUTPUT ===
            String scriptsBasePath = getServletContext().getRealPath("/scripts");
            subjectTempDirPath = Paths.get(scriptsBasePath, "documents", String.valueOf(document.getSubjectId()));
            Files.createDirectories(subjectTempDirPath);

            // Chuẩn bị file input
            String rawFileName = Paths.get(new URL(document.getFilePath()).getPath()).getFileName().toString();
            String inputFileName = URLDecoder.decode(rawFileName, "UTF-8");
            inputFile = subjectTempDirPath.resolve(inputFileName).toFile();
            downloadFileFromUrl(document.getFilePath(), inputFile.getAbsolutePath());

            // Chuẩn bị file output với tên duy nhất để tránh xung đột
            String outputFileName = UUID.randomUUID().toString() + ".json";
            outputFile = subjectTempDirPath.resolve(outputFileName).toFile();

            // === BƯỚC 3: THỰC THI SCRIPT PYTHON ===
            String pythonScriptPath = getServletContext().getRealPath("/scripts/quiz-generator.py");
            int exitCode = executePythonScript(pythonScriptPath, subjectTempDirPath.toFile(), inputFileName, outputFileName);

            if (exitCode != 0) {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "AI không thể tạo quiz do lỗi từ script Python. Vui lòng kiểm tra log của server.");
                return;
            }

            // === BƯỚC 4: ĐỌC KẾT QUẢ TỪ FILE OUTPUT VÀ XỬ LÝ ===
            if (!outputFile.exists()) {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Script Python chạy thành công nhưng không tạo ra file kết quả.");
                return;
            }
            String jsonOutput = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);

            // Phần parse JSON và lưu DB giữ nguyên
            JsonObject generatedQuizJson = gson.fromJson(jsonOutput, JsonObject.class);
            String parsedQuizTitle = generatedQuizJson.has("title") ? generatedQuizJson.get("title").getAsString() : document.getFileName();
            String parsedQuizDescription = generatedQuizJson.has("description") ? generatedQuizJson.get("description").getAsString() : "Quiz được tạo tự động cho tài liệu: " + document.getFileName();
            JsonArray questionsJsonArray = generatedQuizJson.getAsJsonArray("questions");

            if (questionsJsonArray == null || questionsJsonArray.size() == 0) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "AI không tạo ra câu hỏi nào hoặc định dạng không hợp lệ.");
                return;
            }

            Quiz newQuiz = new Quiz(document.getSubjectId(), parsedQuizTitle, parsedQuizDescription);
            int quizId = quizDao.addQuiz(newQuiz);

            if (quizId <= 0) {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể lưu quiz vào cơ sở dữ liệu.");
                return;
            }

            saveQuestionsAndOptions(questionsJsonArray, quizId);

            // === BƯỚC 5: PHẢN HỒI THÀNH CÔNG ===
            
            out.print("{\"success\": true, \"message\": \"Quiz đã được tạo thành công!\", \"quizId\": " + quizId + "}");
            LOGGER.log(Level.INFO, "Successfully generated quiz with ID: {0} for document ID: {1}", new Object[]{quizId, documentId});
            response.sendRedirect(request.getContextPath() + "/quizzes/detail?id=" + quizId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during quiz generation process", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Đã có lỗi hệ thống xảy ra: " + e.getMessage());
            request.getRequestDispatcher("/components/errorGeneral.jsp").forward(request, response);
        } finally {
            // === BƯỚC 6: DỌN DẸP TẤT CẢ CÁC FILE TẠM (RẤT QUAN TRỌNG) ===
            if (inputFile != null && inputFile.exists()) {
                inputFile.delete();
            }
            if (outputFile != null && outputFile.exists()) {
                outputFile.delete();
            }
            if (subjectTempDirPath != null && Files.exists(subjectTempDirPath)) {
                try {
                    Files.delete(subjectTempDirPath);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Could not delete temporary directory (it might not be empty): {0}", subjectTempDirPath);
                }
            }
        }
    }

    /**
     * Thực thi script Python. Script sẽ ghi output vào một file.
     *
     * @return Mã thoát (exit code) của tiến trình. 0 là thành công.
     */
    private int executePythonScript(String scriptPath, File workingDir, String inputFileName, String outputFileName) throws IOException, InterruptedException {
        String pythonExecutable = "python";
        // <<< THAY ĐỔI: Thêm outputFileName làm tham số thứ 3 >>>
        ProcessBuilder pb = new ProcessBuilder(pythonExecutable, scriptPath, inputFileName, outputFileName);
        pb.directory(workingDir);

        // Không cần đọc output nữa, nhưng vẫn nên xem log lỗi để debug
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = pb.start();

        // Chờ tiến trình kết thúc. Không cần timeout nếu không lo script bị treo vô tận.
        return process.waitFor();
    }

    /**
     * Tải file từ một URL và lưu vào đường dẫn đích.
     *
     * @param urlString URL của file
     * @param destinationPath Đường dẫn tuyệt đối để lưu file
     * @throws IOException
     */
    private void downloadFileFromUrl(String urlString, String destinationPath) throws IOException {
        URL url = new URL(urlString);
        try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(destinationPath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Lưu các câu hỏi và lựa chọn vào DB từ mảng JSON.
     *
     * @param questionsJsonArray Mảng JSON chứa các câu hỏi
     * @param quizId ID của quiz cha
     */
    private void saveQuestionsAndOptions(JsonArray questionsJsonArray, int quizId) {
        for (int i = 0; i < questionsJsonArray.size(); i++) {
            JsonObject questionJson = questionsJsonArray.get(i).getAsJsonObject();
            String questionText = questionJson.get("questionText").getAsString();
            String questionType = questionJson.has("questionType") ? questionJson.get("questionType").getAsString() : "MULTIPLE_CHOICE";

            Question newQuestion = new Question(quizId, questionText, questionType);
            int questionId = questionDao.addQuestion(newQuestion); // Giả sử addQuestion trả về ID

            if (questionId > 0) {
                JsonArray optionsJsonArray = questionJson.getAsJsonArray("options");
                if (optionsJsonArray != null) {
                    for (int j = 0; j < optionsJsonArray.size(); j++) {
                        JsonObject optionJson = optionsJsonArray.get(j).getAsJsonObject();
                        String optionText = optionJson.get("optionText").getAsString();
                        boolean isCorrect = optionJson.get("isCorrect").getAsBoolean();
                        AnswerOption newOption = new AnswerOption(questionId, optionText, isCorrect);
                        answerOptionDao.addAnswerOption(newOption);
                    }
                }
            } else {
                LOGGER.log(Level.SEVERE, "Failed to save question: {0} for quizId: {1}", new Object[]{questionText, quizId});
            }
        }
    }

    /**
     * Gửi một phản hồi lỗi chuẩn hóa về client.
     *
     * @param response HttpServletResponse
     * @param statusCode Mã trạng thái HTTP
     * @param errorMessage Nội dung lỗi
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.print("{\"error\": \"" + errorMessage.replace("\"", "\\\"") + "\"}");
    }
}
