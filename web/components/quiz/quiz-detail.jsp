<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
    <title>Chi tiết Quiz</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
</head>
<body>
<div class="container mt-5">
    <h2>Chi tiết Quiz: ${quiz.title}</h2>
    <p><strong>Mô tả:</strong> ${quiz.description}</p>
    <hr/>
    <h4>Các câu hỏi:</h4>
    <c:forEach var="q" items="${questions}" varStatus="loop">
        <div class="mb-4">
            <h5>Câu ${loop.index + 1}: ${q.question}</h5>
            <ul>
                <c:forEach var="opt" items="${fn:split(q.options, '|')}" varStatus="o">
                    <li>
                        <c:if test="${fn:contains(q.answers, o.index)}">
                            <strong class="text-success">${opt} ✔</strong>
                        </c:if>
                        <c:if test="${!fn:contains(q.answers, o.index)}">
                            ${opt}
                        </c:if>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </c:forEach>
</div>
</body>
</html>
