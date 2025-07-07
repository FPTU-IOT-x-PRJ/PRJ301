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

            <div class="btn-group-custom">
                <a href="${pageContext.request.contextPath}/subjects/detail?id=${quiz.subjectId}" class="btn btn-outline-secondary btn-lg me-2">
                    <i class="fas fa-arrow-left me-2"></i>Về trang môn học
                </a>
                <a href="${pageContext.request.contextPath}/quizzes/submissions?id=${quiz.id}" class="btn btn-info btn-lg">
                    <i class="fas fa-history me-2"></i>Lịch sử làm bài
                </a>
            </div>
        </div>
    </body>
</html>