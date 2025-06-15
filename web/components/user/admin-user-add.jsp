<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm Người Dùng Mới - EduPlan</title>
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="../navigation/navigation.jsp" />

    <div class="container py-4">
        <div class="row justify-content-center">
            <div class="col-lg-8 col-md-10">
                <div class="card shadow-sm">
                    <div class="card-header bg-primary text-white">
                        <h4 class="mb-0"><i class="fas fa-user-plus me-2"></i>Thêm người dùng mới</h4>
                    </div>
                    <div class="card-body">
                        <%-- Hiển thị thông báo lỗi nếu có (từ backend) --%>
                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <i class="fas fa-exclamation-triangle me-2"></i>${errorMessage}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>

                        <form id="addUserForm" action="${pageContext.request.contextPath}/user/add" method="POST" class="needs-validation" novalidate>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label for="firstName" class="form-label">Họ <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="firstName" name="firstName" value="<c:out value="${formFirstName}"/>" required>
                                    <div class="invalid-feedback">Vui lòng nhập họ.</div>
                                </div>
                                <div class="col-md-6">
                                    <label for="lastName" class="form-label">Tên <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="lastName" name="lastName" value="<c:out value="${formLastName}"/>" required>
                                    <div class="invalid-feedback">Vui lòng nhập tên.</div>
                                </div>
                                <div class="col-md-6">
                                    <label for="username" class="form-label">Tên đăng nhập <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="username" name="username" value="<c:out value="${formUsername}"/>" required>
                                    <div class="invalid-feedback">Vui lòng nhập tên đăng nhập.</div>
                                </div>
                                <div class="col-md-6">
                                    <label for="email" class="form-label">Email <span class="text-danger">*</span></label>
                                    <input type="email" class="form-control" id="email" name="email" value="<c:out value="${formEmail}"/>" required>
                                    <div class="invalid-feedback">Vui lòng nhập email hợp lệ.</div>
                                </div>
                                <div class="col-md-6">
                                    <label for="password" class="form-label">Mật khẩu <span class="text-danger">*</span></label>
                                    <div class="input-group">
                                        <input type="password" class="form-control" id="password" name="password" required minlength="6">
                                        <button class="btn btn-outline-secondary" type="button" onclick="togglePassword('password')">
                                            <i class="fas fa-eye"></i>
                                        </button>
                                    </div>
                                    <div class="form-text">Mật khẩu phải có ít nhất 6 ký tự.</div>
                                    <div class="invalid-feedback" id="passwordFeedback">Mật khẩu phải có ít nhất 6 ký tự.</div>
                                </div>
                                <div class="col-md-6">
                                    <label for="confirmPassword" class="form-label">Xác nhận mật khẩu <span class="text-danger">*</span></label>
                                    <div class="input-group">
                                        <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
                                        <button class="btn btn-outline-secondary" type="button" onclick="togglePassword('confirmPassword')">
                                            <i class="fas fa-eye"></i>
                                        </button>
                                    </div>
                                    <div class="invalid-feedback" id="confirmPasswordFeedback">Mật khẩu xác nhận không khớp.</div>
                                </div>
                                <div class="col-md-12">
                                    <label for="role" class="form-label">Vai trò <span class="text-danger">*</span></label>
                                    <select class="form-select" id="role" name="role" required>
                                        <option value="">Chọn vai trò</option>
                                        <option value="User" <c:if test="${formRole eq 'User'}">selected</c:if>>User</option>
                                        <option value="Admin" <c:if test="${formRole eq 'Admin'}">selected</c:if>>Admin</option>
                                    </select>
                                    <div class="invalid-feedback">Vui lòng chọn vai trò.</div>
                                </div>
                            </div>
                            <hr class="my-4">
                            <div class="d-flex justify-content-end">
                                <a href="${pageContext.request.contextPath}/users" class="btn btn-secondary me-2">
                                    <i class="fas fa-times me-2"></i>Hủy
                                </a>
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-save me-2"></i>Thêm người dùng
                                </button>
                            </div>
                        </form>
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

            var form = document.getElementById('addUserForm');

            form.addEventListener('submit', function (event) {
                var password = document.getElementById('password');
                var confirmPassword = document.getElementById('confirmPassword');
                var passwordFeedback = document.getElementById('passwordFeedback');
                var confirmPasswordFeedback = document.getElementById('confirmPasswordFeedback');

                // Reset custom validity for re-evaluation
                password.setCustomValidity('');
                confirmPassword.setCustomValidity('');

                // Custom validation for password length (in addition to minlength attribute)
                if (password.value.length < 6) {
                    password.setCustomValidity('Mật khẩu phải có ít nhất 6 ký tự.');
                    passwordFeedback.textContent = 'Mật khẩu phải có ít nhất 6 ký tự.'; // Cập nhật tin nhắn feedback
                } else {
                    passwordFeedback.textContent = ''; // Xóa tin nhắn lỗi nếu hợp lệ
                }

                // Custom validation for confirm password match
                if (password.value !== confirmPassword.value) {
                    confirmPassword.setCustomValidity('Mật khẩu xác nhận không khớp.');
                    confirmPasswordFeedback.textContent = 'Mật khẩu xác nhận không khớp.'; // Cập nhật tin nhắn feedback
                } else {
                    confirmPasswordFeedback.textContent = ''; // Xóa tin nhắn lỗi nếu hợp lệ
                }

                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }

                form.classList.add('was-validated');
            }, false);
        })();

        // Toggle password visibility
        function togglePassword(id) {
            var input = document.getElementById(id);
            var icon = input.nextElementSibling.querySelector('i');
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        }
    </script>
</body>
</html>