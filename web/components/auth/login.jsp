<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng nhập - EduPlan</title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background-color: var(--light-color); /* Nền trắng nhạt theo common.css */
                margin: 0;
                padding: 0;
                min-height: 100vh;
                display: flex;
                flex-direction: column;
            }

            .main-content-wrapper {
                flex-grow: 1;
                display: flex;
                justify-content: center;
                align-items: center;
                padding: 20px;
                box-sizing: border-box;
                width: 100%;
            }

            .login-card-wrapper {
                max-width: 400px;
                width: 100%;
            }

            .alert {
                margin-bottom: 1rem;
            }
        </style>
    </head>
    <body>
        <jsp:include page="../navigation/navigation.jsp" />    

        <div class="main-content-wrapper"> <%-- Wrapper mới để căn giữa form --%>
            <div class="login-card-wrapper"> <%-- Thêm một wrapper div để giới hạn chiều rộng của card --%>
                <div class="card shadow-lg border-0 rounded-lg">
                    <div class="card-header bg-dark text-white text-center py-4">
                        <h3 class="mb-0 fw-bold"><i class="fas fa-lock me-2"></i>Đăng nhập EduPlan</h3>
                    </div>
                    <div class="card-body p-4">
                        <%-- Hiển thị thông báo lỗi --%>
                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <i class="fas fa-exclamation-circle me-2"></i>${errorMessage}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                        <%-- Hiển thị thông báo thành công (logout) --%>
                        <c:if test="${not empty successMessage}">
                            <div class="alert alert-success alert-dismissible fade show" role="alert">
                                <i class="fas fa-check-circle me-2"></i>${successMessage}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                        <c:if test="${param.logout eq 'true'}">
                            <div class="alert alert-info alert-dismissible fade show" role="alert">
                                <i class="fas fa-info-circle me-2"></i>Bạn đã đăng xuất thành công.
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/auth/login" method="post" class="needs-validation" novalidate>
                            <div class="mb-3">
                                <label for="identifier" class="form-label">Tên đăng nhập / Email</label>
                                <input type="text" class="form-control" id="identifier" name="identifier" 
                                       value="${not empty cookie.rememberIdentifier.value ? cookie.rememberIdentifier.value : (param.from == null ? requestScope.formIdentifier : '')}" required
                                       placeholder="Nhập tên đăng nhập hoặc email của bạn">
                                <div class="invalid-feedback">Vui lòng nhập tên đăng nhập hoặc email.</div>
                            </div>
                            <div class="mb-3">
                                <label for="password" class="form-label">Mật khẩu</label>

                                <div class="input-group">
                                    <input type="password" class="form-control" id="password" name="password" required
                                           value="${not empty cookie.rememberPassword.value ? cookie.rememberPassword.value : ''}"
                                           placeholder="Nhập mật khẩu của bạn">
                                    <button class="btn btn-outline-secondary" type="button" id="togglePassword">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                </div>
                                <div class="invalid-feedback">Vui lòng nhập mật khẩu.</div>
                            </div>
                            
                            <div class="form-check mb-3">
                                <input class="form-check-input" type="checkbox" id="rememberMe" name="rememberMe"
                                       <c:if test="${not empty cookie.rememberIdentifier.value}">checked</c:if>>
                                <label class="form-check-label" for="rememberMe">
                                    Ghi nhớ tôi
                                </label>
                            </div>

                            <div class="mt-2 text-end">
                                <a href="${pageContext.request.contextPath}/auth/forgot-password" class="text-decoration-none text-primary fw-semibold">
                                    <i class="fas fa-unlock-alt me-1"></i>Quên mật khẩu?
                                </a>
                            </div>

                            <div class="d-grid mt-4">
                                <button type="submit" class="btn btn-primary btn-lg">
                                    <i class="fas fa-sign-in-alt me-2"></i>Đăng nhập
                                </button>
                            </div>
                        </form>
                        <div class="text-center mt-3">
                            <p class="text-muted">Bạn chưa có tài khoản? <a href="${pageContext.request.contextPath}/auth/register" class="text-primary fw-bold">Đăng ký ngay</a></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
        <script>
            // Form validation
            (function () {
                'use strict';
                var form = document.querySelector('form');
                form.addEventListener('submit', function (event) {
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
        </script>
    </body>
</html>