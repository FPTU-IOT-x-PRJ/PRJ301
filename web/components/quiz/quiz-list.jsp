<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Danh sách Quiz</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
</head>
<body>
<div class="container mt-5">
    <h2>Danh sách Quiz của môn: ${subject.name}</h2>

    <a href="${pageContext.request.contextPath}/quizzes/add?subjectId=${subject.id}" class="btn btn-primary mb-3">+ Thêm Quiz</a>

    <table class="table table-bordered">
        <thead>
            <tr>
                <th>ID</th>
                <th>Tiêu đề</th>
                <th>Mô tả</th>
                <th>Hành động</th>
            </tr>
        </thead>
        <tbody>
        <c:forEach var="quiz" items="${quizzes}">
            <tr>
                <td>${quiz.id}</td>
                <td>${quiz.title}</td>
                <td>${quiz.description}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/quizzes/detail?id=${quiz.id}" class="btn btn-info btn-sm">Chi tiết</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>