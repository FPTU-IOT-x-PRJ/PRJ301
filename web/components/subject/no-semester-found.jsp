<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Chưa có Kỳ học - EduPlan</title>
        <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
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
            .card.shadow-sm {
                box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075) !important;
            }
        </style>
    </head>
    <body>
        <jsp:include page="../navigation/navigation.jsp" />
        <div class="container-fluid py-4">
            <div class="row mb-4">
                <div class="col-12 d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="mb-1">Quản lý Kỳ học</h2>
                        <p class="text-muted mb-0">Thiết lập các kỳ học để quản lý môn học và buổi học.</p>
                    </div>
                </div>
            </div>

            <div class="card shadow-sm">
                <div class="card-body">
                    <div class="empty-state">
                        <i class="fas fa-calendar-times"></i>
                        <h3 class="mt-3 mb-2">Chưa có kỳ học nào</h3>
                        <p class="text-muted mb-4">Bạn chưa tạo kỳ học nào trong hệ thống. Vui lòng tạo một kỳ học để bắt đầu quản lý môn học.</p>
                        <a href="${pageContext.request.contextPath}/semesters/add" class="btn btn-primary btn-lg">
                            Tạo kỳ học mới
                        </a>
                    </div>
                </div>
            </div>
        </div>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
    </body>
</html>