<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Xác minh mã - EduPlan</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: var(--light-color);
            margin: 0;
            padding: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .card {
            max-width: 400px;
            width: 100%;
        }
    </style>
</head>
<body>
    <div class="card shadow-lg border-0 rounded-lg">
        <div class="card-header bg-dark text-white text-center py-4">
            <h4 class="fw-bold mb-0"><i class="fas fa-envelope-open-text me-2"></i>Xác minh mã khôi phục</h4>
        </div>
        <div class="card-body p-4">
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger">${errorMessage}</div>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/auth/forgot-password" class="needs-validation" novalidate>
                <input type="hidden" name="email" value="${email}" />

                <div class="mb-3">
                    <label for="code" class="form-label">Nhập mã xác nhận đã gửi tới email</label>
                    <input type="text" class="form-control" id="code" name="code" required placeholder="Mã xác nhận gồm 6 chữ số">
                    <div class="invalid-feedback">Vui lòng nhập mã xác nhận.</div>
                </div>

                <div class="d-grid mt-4">
                    <button type="submit" class="btn btn-primary btn-lg">
                        <i class="fas fa-check-circle me-2"></i>Xác minh mã
                    </button>
                </div>
            </form>

            <div class="text-center mt-3">
                <a href="${pageContext.request.contextPath}/auth/forgot-password" class="text-muted text-decoration-none">
                    <i class="fas fa-arrow-left me-1"></i>Quay lại nhập email
                </a>
            </div>
        </div>
    </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
    <script>
        // Bootstrap validation
        (function () {
            'use strict';
            const form = document.querySelector('form');
            form.addEventListener('submit', function (event) {
                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            }, false);
        })();
    </script>
</body>
</html>