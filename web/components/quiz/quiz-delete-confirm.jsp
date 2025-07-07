<%-- web/components/quiz/quiz-delete-confirm.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Xác nhận xóa Quiz</title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    </head>
    <body>
        <div class="container mt-5">
            <div class="card border-danger">
                <div class="card-header bg-danger text-white">
                    <h4><i class="fas fa-exclamation-triangle"></i> Xác nhận xóa</h4>
                </div>
                <div class="card-body">
                    <p>Bạn có chắc chắn muốn xóa bài quiz này không?</p>
                    <h5>${quizToDelete.title}</h5>
                    <p class="text-muted">Hành động này không thể hoàn tác. Tất cả các câu hỏi và lựa chọn liên quan cũng sẽ bị xóa vĩnh viễn.</p>

                    <form action="${pageContext.request.contextPath}/quizzes/delete" method="post" class="mt-4">
                        <input type="hidden" name="id" value="${quizToDelete.id}">
                        <button type="submit" class="btn btn-danger">Có, tôi chắc chắn</button>
                        <a href="${pageContext.request.contextPath}/subjects/detail?id=${quizToDelete.subjectId}" class="btn btn-secondary">Không, quay lại</a>
                    </form>
                </div>
            </div>
        </div>
    </body>
</html>