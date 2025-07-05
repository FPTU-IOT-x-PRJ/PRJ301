<%-- 
    Document   : forgot-password
    Created on : Jun 30, 2025, 10:55:26 PM
    Author     : Dung Ann
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quên mật khẩu - EduPlan</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
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
                <h4 class="fw-bold mb-0"><i class="fas fa-unlock-alt me-2"></i>Quên mật khẩu</h4>
            </div>
            <div class="card-body p-4">
                <c:if test="${not empty message}">
                    <div class="alert alert-info">${message}</div>
                </c:if>
                <form method="get" action="${pageContext.request.contextPath}/auth/verify-code" class="needs-validation" novalidate>
                    <div class="mb-3">
                        <label for="email" class="form-label">Nhập email của bạn</label>
                        <input type="email" class="form-control" id="email" name="email" required placeholder="abc@example.com">
                        <div class="invalid-feedback">Vui lòng nhập email hợp lệ.</div>
                    </div>
                    <div class="d-grid mt-4">
                        <button type="submit" class="btn btn-primary btn-lg">
                            <i class="fas fa-paper-plane me-2"></i>Gửi mã xác nhận
                        </button>
                    </div>
                </form>
                <div class="text-center mt-3">
                    <a href="${pageContext.request.contextPath}/auth/login" class="text-decoration-none text-muted">
                        <i class="fas fa-arrow-left me-1"></i>Quay lại trang đăng nhập
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
