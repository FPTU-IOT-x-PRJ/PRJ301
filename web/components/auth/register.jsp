<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký tài khoản mới - EduPlan</title>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: var(--light-color); /* Nền trắng nhạt theo common.css */
            /* Loại bỏ display: flex, justify-content, align-items từ body */
            margin: 0; /* Đảm bảo không có margin mặc định trên body */
            padding: 0; /* Đảm bảo không có padding mặc định trên body */
            min-height: 100vh; /* Vẫn giữ min-height để footer có thể ở cuối trang nếu có */
            display: flex; /* Sử dụng flexbox cho body để xếp chồng navigation và main content */
            flex-direction: column; /* Xếp các phần tử con theo chiều dọc */
        }

        .main-content-wrapper {
            flex-grow: 1; /* Cho phép main-content-wrapper mở rộng để chiếm không gian còn lại */
            display: flex; /* Kích hoạt Flexbox cho wrapper này */
            justify-content: center; /* Căn giữa form theo chiều ngang */
            align-items: center; /* Căn giữa form theo chiều dọc */
            padding: 20px; /* Thêm padding để tránh form dính sát mép trên/dưới trên màn hình nhỏ */
            box-sizing: border-box; /* Đảm bảo padding không làm tràn */
            width: 100%; /* Đảm bảo chiếm toàn bộ chiều rộng */
        }

        .register-card-wrapper {
            max-width: 450px; /* Giới hạn chiều rộng tối đa của card đăng ký */
            width: 100%; /* Đảm bảo nó co giãn trên màn hình nhỏ */
        }
        
        /* Đảm bảo các alert hiển thị đúng style của common.css */
        .alert {
            margin-bottom: 1rem;
        }
    </style>
</head>
<body>
    <jsp:include page="../navigation/navigation.jsp" />    
    
    <div class="main-content-wrapper"> <%-- Wrapper mới để căn giữa form --%>
        <div class="register-card-wrapper">
            <div class="card shadow-lg border-0 rounded-lg">
                <div class="card-header bg-dark text-white text-center py-4">
                    <h3 class="mb-0 fw-bold"><i class="fas fa-user-plus me-2"></i>Đăng ký tài khoản mới</h3>
                </div>
                <div class="card-body p-4">
                    <%-- Hiển thị thông báo lỗi --%>
                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="fas fa-exclamation-circle me-2"></i>${errorMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>
                    <%-- Hiển thị thông báo thành công --%>
                    <c:if test="${not empty successMessage}">
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <i class="fas fa-check-circle me-2"></i>${successMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/auth/register" method="post" class="needs-validation" novalidate>
                        <div class="mb-3">
                            <label for="firstName" class="form-label">Tên</label>
                            <input type="text" class="form-control" id="firstName" name="firstName" 
                                   value="${requestScope.formFirstName}" required placeholder="Ví dụ: Nguyễn">
                            <div class="invalid-feedback">Vui lòng nhập tên của bạn.</div>
                        </div>
                        <div class="mb-3">
                            <label for="lastName" class="form-label">Họ đệm</label>
                            <input type="text" class="form-control" id="lastName" name="lastName" 
                                   value="${requestScope.formLastName}" required placeholder="Ví dụ: Văn A">
                            <div class="invalid-feedback">Vui lòng nhập họ đệm của bạn.</div>
                        </div>
                        <div class="mb-3">
                            <label for="username" class="form-label">Tên đăng nhập</label>
                            <input type="text" class="form-control" id="username" name="username" 
                                   value="${requestScope.formUsername}" required placeholder="Tên đăng nhập duy nhất">
                            <div class="invalid-feedback">Vui lòng nhập tên đăng nhập.</div>
                        </div>
                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <input type="email" class="form-control" id="email" name="email" 
                                   value="${requestScope.formEmail}" required placeholder="Địa chỉ email hợp lệ">
                            <div class="invalid-feedback">Vui lòng nhập email hợp lệ.</div>
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">Mật khẩu</label>
                            <div class="input-group">
                                <input type="password" class="form-control" id="password" name="password" required
                                       placeholder="Mật khẩu của bạn">
                                <button class="btn btn-outline-secondary" type="button" id="togglePassword">
                                    <i class="fas fa-eye"></i>
                                </button>
                            </div>
                            <div class="invalid-feedback">Vui lòng nhập mật khẩu.</div>
                        </div>
                        <div class="mb-3">
                            <label for="confirmPassword" class="form-label">Xác nhận mật khẩu</label>
                            <div class="input-group">
                                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required
                                       placeholder="Nhập lại mật khẩu">
                                <button class="btn btn-outline-secondary" type="button" id="toggleConfirmPassword">
                                    <i class="fas fa-eye"></i>
                                </button>
                            </div>
                            <div class="invalid-feedback" id="confirmPasswordFeedback">Vui lòng xác nhận mật khẩu.</div>
                        </div>
                        <div class="d-grid mt-4">
                            <button type="submit" class="btn btn-primary btn-lg">
                                <i class="fas fa-user-plus me-2"></i>Đăng ký
                            </button>
                        </div>
                    </form>
                    <div class="text-center mt-3">
                        <p class="text-muted">Bạn đã có tài khoản? <a href="${pageContext.request.contextPath}/auth/login" class="text-primary fw-bold">Đăng nhập</a></p>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
    <script>
        // Form validation và kiểm tra mật khẩu trùng khớp
        (function () {
            'use strict';
            var form = document.querySelector('form');
            var passwordInput = document.getElementById('password');
            var confirmPasswordInput = document.getElementById('confirmPassword');
            var confirmPasswordFeedback = document.getElementById('confirmPasswordFeedback');

            function checkPasswordMatch() {
                if (passwordInput.value !== confirmPasswordInput.value) {
                    confirmPasswordInput.setCustomValidity("Mật khẩu xác nhận không khớp.");
                    confirmPasswordFeedback.textContent = "Mật khẩu xác nhận không khớp.";
                } else {
                    confirmPasswordInput.setCustomValidity("");
                }
            }

            passwordInput.addEventListener('change', checkPasswordMatch);
            confirmPasswordInput.addEventListener('input', checkPasswordMatch); // Dùng 'input' để kiểm tra ngay khi gõ

            form.addEventListener('submit', function (event) {
                checkPasswordMatch(); // Kiểm tra lần cuối trước khi submit

                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            }, false);
        })();

        // Toggle password visibility
        document.getElementById('togglePassword').addEventListener('click', function () {
            const passwordInput = document.getElementById('password');
            const icon = this.querySelector('i');
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                passwordInput.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });

        // Toggle confirm password visibility
        document.getElementById('toggleConfirmPassword').addEventListener('click', function () {
            const confirmPasswordInput = document.getElementById('confirmPassword');
            const icon = this.querySelector('i');
            if (confirmPasswordInput.type === 'password') {
                confirmPasswordInput.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                confirmPasswordInput.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    </script>
</body>
</html>