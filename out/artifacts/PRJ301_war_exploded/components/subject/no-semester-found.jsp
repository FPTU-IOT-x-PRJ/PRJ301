<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Không tìm thấy Học kỳ - EduPlan</title>
        <link href="/css/common.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
        <style>
            .empty-state {
                text-align: center;
                padding: 50px 0;
            }
            .empty-state i {
                font-size: 5rem;
                color: #ccc;
                margin-bottom: 20px;
            }
        </style>
    </head>
    <body>
        <jsp:include page="../navigation/navigation.jsp" />
        <div class="container py-5">
            <div class="empty-state">
                <i class="fas fa-calendar-times"></i>
                <h3>Chưa có kỳ học nào</h3>
                <p class="text-muted">Bạn chưa tạo kỳ học nào trong hệ thống. Vui lòng tạo một kỳ học để bắt đầu quản lý môn học.</p>
                <a href="${pageContext.request.contextPath}/semesters/add" class="btn btn-primary mt-3">
                    <i class="fas fa-plus me-2"></i>Tạo kỳ học mới
                </a>
                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-warning mt-3" role="alert">
                        ${errorMessage}
                    </div>
                </c:if>
            </div>
        </div>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
    </body>
</html>