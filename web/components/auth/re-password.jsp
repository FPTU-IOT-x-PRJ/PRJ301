<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đặt lại mật khẩu - EduPlan</title>
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
            <h4 class="fw-bold mb-0"><i class="fas fa-key me-2"></i>Đặt lại mật khẩu mới</h4>
        </div>
        <div class="card-body p-4">
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger">${errorMessage}</div>
            </c:if>
            <c:if test="${not empty successMessage}">
                <div class="alert alert-success">${successMessage}</div>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/auth/reset-password" class="needs-validation" novalidate>
                <input type="hidden" name="email" value="${email}" />

                <div class="mb-3">
                    <label for="newPassword" class="form-label">Mật khẩu mới</label>
                    <input type="password" class="form-control" id="newPassword" name="newPassword" required placeholder="Nhập mật khẩu mới">
                    <div class="invalid-feedback" id="newPasswordFeedback">Vui lòng nhập mật khẩu mới.</div>
                </div>

                <div class="mb-3">
                    <label for="confirmNewPassword" class="form-label">Xác nhận mật khẩu mới</label>
                    <input type="password" class="form-control" id="confirmNewPassword" name="confirmNewPassword" required placeholder="Nhập lại mật khẩu mới">
                    <div class="invalid-feedback" id="confirmNewPasswordFeedback">Vui lòng xác nhận mật khẩu mới.</div>
                </div>

                <div class="d-grid mt-4">
                    <button type="submit" class="btn btn-success btn-lg">
                        <i class="fas fa-redo me-2"></i>Đặt lại mật khẩu
                    </button>
                </div>
            </form>

            <div class="text-center mt-3">
                <a href="${pageContext.request.contextPath}/auth/login" class="text-muted text-decoration-none">
                    <i class="fas fa-arrow-left me-1"></i>Quay lại đăng nhập
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
                const newPasswordInput = document.getElementById('newPassword');
                const confirmNewPasswordInput = document.getElementById('confirmNewPassword');
                const newPasswordFeedback = document.getElementById('newPasswordFeedback');
                const confirmNewPasswordFeedback = document.getElementById('confirmNewPasswordFeedback');

                // Reset custom validity messages and validity state
                newPasswordInput.setCustomValidity('');
                confirmNewPasswordInput.setCustomValidity('');
                newPasswordFeedback.textContent = 'Vui lòng nhập mật khẩu mới.'; // Reset default message
                confirmNewPasswordFeedback.textContent = 'Vui lòng xác nhận mật khẩu mới.'; // Reset default message


                let isValid = true;

                // Validate new password length
                if (newPasswordInput.value.length < 6) {
                    newPasswordInput.setCustomValidity('Mật khẩu phải có ít nhất 6 ký tự.');
                    newPasswordFeedback.textContent = 'Mật khẩu phải có ít nhất 6 ký tự.';
                    isValid = false;
                }

                // Validate if confirm password matches new password
                if (newPasswordInput.value !== confirmNewPasswordInput.value) {
                    confirmNewPasswordInput.setCustomValidity('Mật khẩu xác nhận không khớp.');
                    confirmNewPasswordFeedback.textContent = 'Mật khẩu xác nhận không khớp.';
                    isValid = false;
                }

                // If any custom validation failed, prevent form submission
                if (!isValid || !form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }

                form.classList.add('was-validated');
            }, false);
        })();
    </script>
</body>
</html>