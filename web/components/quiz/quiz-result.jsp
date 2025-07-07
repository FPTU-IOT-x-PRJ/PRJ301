<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Kết quả Quiz: ${quiz.title}</title>
        <jsp:include page="../navigation/navigation.jsp"/>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background-color: #f8f9fa;
            }
            .result-container {
                max-width: 600px;
                margin: 40px auto;
                padding: 30px;
                background-color: #ffffff;
                border-radius: 8px;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                text-align: center;
            }
            .result-container h2 {
                color: #28a745; /* Green for success */
                margin-bottom: 20px;
            }
            .score-display {
                font-size: 3rem;
                font-weight: bold;
                color: #007bff;
                margin-bottom: 20px;
            }
            .details p {
                font-size: 1.1rem;
                margin-bottom: 10px;
            }
            .details strong {
                color: #343a40;
            }
            .btn-group-custom {
                margin-top: 30px;
            }
        </style>
    </head>
    <body>
        <div class="container result-container">
            <h2><i class="fas fa-check-circle me-2"></i>Hoàn thành bài Quiz!</h2>
            <p class="text-muted">Bài: <strong>${quiz.title}</strong></p>

            <div class="score-display">
                Điểm của bạn: ${score}/100
            </div>

            <div class="details">
                <p>Số câu đúng: <strong>${correctAnswers} / ${totalQuestions}</strong></p>
                <p>Thời gian làm bài: <strong>${timeTakenMinutes} phút</strong></p>
            </div>

            <div class="btn-group-custom d-flex justify-content-center gap-3">
                <a href="${pageContext.request.contextPath}/subjects/detail?id=${quiz.subjectId}" class="btn btn-outline-secondary btn-lg">
                    <i class="fas fa-arrow-left me-2"></i>Về trang môn học
                </a>
                <a href="${pageContext.request.contextPath}/quizzes/submissions?id=${quiz.id}" class="btn btn-info btn-lg">
                    <i class="fas fa-history me-2"></i>Lịch sử làm bài
                </a>
                <button type="button" class="btn btn-primary btn-lg" 
                        data-bs-toggle="modal" data-bs-target="#quizDurationModal" 
                        data-quiz-id="${quiz.id}">
                    <i class="fas fa-redo me-1"></i> Làm lại
                </button>
            </div>
        </div>

        <div class="modal fade" id="quizDurationModal" tabindex="-1" aria-labelledby="quizDurationModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <form id="startQuizForm" action="${pageContext.request.contextPath}/quizzes/take" method="GET">
                        <div class="modal-header">
                            <h5 class="modal-title" id="quizDurationModalLabel">Chọn thời gian làm bài</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <input type="hidden" name="id" id="modalQuizId">
                            <div class="mb-3">
                                <label for="quizDurationSelect" class="form-label">Thời gian (phút):</label>
                                <select class="form-select" id="quizDurationSelect" name="duration" required>
                                    <option value="5">5 phút</option>
                                    <option value="10" selected>10 phút</option>
                                    <option value="15">15 phút</option>
                                    <option value="20">20 phút</option>
                                    <option value="30">30 phút</option>
                                    <option value="45">45 phút</option>
                                    <option value="60">60 phút</option>
                                </select>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                            <button type="submit" class="btn btn-primary">Bắt đầu Quiz</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            // Lắng nghe sự kiện khi modal được hiển thị
            const quizDurationModal = document.getElementById('quizDurationModal');
            quizDurationModal.addEventListener('show.bs.modal', event => {
                // Nút đã kích hoạt modal
                const button = event.relatedTarget;
                // Lấy quizId từ thuộc tính data-quiz-id của nút
                const quizId = button.getAttribute('data-quiz-id');
                // Cập nhật giá trị quizId vào input hidden trong modal
                const modalQuizIdInput = quizDurationModal.querySelector('#modalQuizId');
                modalQuizIdInput.value = quizId;
            });
        </script>
    </body>
</html>