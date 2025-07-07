<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Lịch sử làm bài: ${quiz.title}</title>
        <jsp:include page="../navigation/navigation.jsp"/>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background-color: #f8f9fa;
            }
            .submissions-container {
                max-width: 800px;
                margin: 40px auto;
                padding: 30px;
                background-color: #ffffff;
                border-radius: 8px;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            }
            .submission-item {
                border-bottom: 1px solid #eee;
                padding: 15px 0;
            }
            .submission-item:last-child {
                border-bottom: none;
            }
            .score-badge {
                font-size: 1.1rem;
                padding: .5em .75em;
                border-radius: .25rem;
            }
            .score-badge-success {
                background-color: #28a745;
                color: white;
            }
            .score-badge-warning {
                background-color: #ffc107;
                color: #343a40;
            }
            .score-badge-danger {
                background-color: #dc3545;
                color: white;
            }
        </style>
    </head>
    <body>
        <div class="container submissions-container">
            <h2 class="text-center mb-4"><i class="fas fa-history me-2"></i>Lịch sử làm bài: ${quiz.title}</h2>
            <p class="text-muted text-center mb-4">Mô tả: ${quiz.description}</p>

            <a href="${pageContext.request.contextPath}/subjects/detail?id=${quiz.subjectId}" class="btn btn-outline-secondary mb-3">
                <i class="fas fa-arrow-left me-2"></i>Quay lại môn học 
            </a>

            <c:if test="${empty submissions}">
                <div class="alert alert-info text-center" role="alert">
                    Chưa có lượt làm bài nào cho quiz này.
                </div>
            </c:if>

            <c:if test="${not empty submissions}">
                <ul class="list-group list-group-flush">
                    <c:forEach var="submission" items="${submissions}">
                        <li class="list-group-item d-flex justify-content-between align-items-center submission-item">
                            <div>
                                <h5 class="mb-1">Lượt làm bài # ${submission.id}</h5>
                                <p class="mb-1 text-muted">
                                    Thời gian làm: <strong>${submission.timeTakenMinutes} phút</strong>
                                </p>
                                <p class="mb-0 text-muted">
                                    Nộp lúc: 
                                    ${submission.submissionTime}
                                </p>
                            </div>
                            <div>
                                <span class="score-badge
                                      <c:choose>
                                          <c:when test="${submission.score >= 80}">score-badge-success</c:when>
                                          <c:when test="${submission.score >= 50}">score-badge-warning</c:when>
                                          <c:otherwise>score-badge-danger</c:otherwise>
                                      </c:choose>">
                                    Điểm: ${submission.score}
                                </span>
                            </div>
                        </li>
                    </c:forEach>
                </ul>
            </c:if>
        </div>
    </body>
</html>