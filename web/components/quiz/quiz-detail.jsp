<%-- web/components/quiz/quiz-detail.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>       
        <jsp:include page="../navigation/navigation.jsp"/>
        <title>Chi tiết Quiz: ${quiz.title}</title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    </head>
    <body>
        <%-- Include navigation --%>
        <div class="container mt-4">
            <a href="${pageContext.request.contextPath}/subjects/detail?id=${quiz.subjectId}">
                <i class="fas fa-arrow-left"></i> Quay lại 
            </a>
            <h2 class="mt-2">${quiz.title}</h2>
            <p class="text-muted">${quiz.description}</p>
            <a href="${pageContext.request.contextPath}/quizzes/edit?id=${quiz.id}" class="btn btn-warning">Sửa Quiz</a>
            <a href="${pageContext.request.contextPath}/quizzes/delete-confirm?id=${quiz.id}" class="btn btn-danger">Xóa Quiz</a>
            <hr>

            <h3>Danh sách câu hỏi</h3>
            <%-- Lặp qua danh sách questions đã được load từ controller --%>
            <c:forEach var="question" items="${questions}" varStatus="loop">
                <div class="card mb-3">
                    <div class="card-body">
                        <h5 class="card-title">Câu ${loop.count}: ${question.questionText}</h5>
                        <ul class="list-group">
                            <c:forEach var="option" items="${question.answerOptions}">
                                <li class="list-group-item ${option.isCorrect() ? 'list-group-item-success' : ''}">
                                    ${option.optionText}
                                    <c:if test="${option.isCorrect()}">
                                        <span class="badge text-bg-success float-right">Đáp án đúng</span>
                                    </c:if>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty questions}">
                <p>Chưa có câu hỏi nào trong quiz này.</p>
            </c:if>
        </div>
    </body>
</html>