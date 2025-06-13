<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.util.*" %>
<%@page import="entity.*" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh Sửa Người Dùng - EduPlan</title>
    <link href="/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
</head>
<%
    User user = (User) request.getAttribute("user");
%>
<body>
    <jsp:include page="../navigation/navigation.jsp" />

    <div class="container py-4">
        <div class="row justify-content-center">
            <div class="col-lg-8 col-md-10">
                <div class="card shadow-sm">
                    <div class="card-header bg-warning text-dark">
                        <h4 class="mb-0"><i class="fas fa-user-edit me-2"></i>Chỉnh sửa người dùng</h4>
                    </div>
                    <div class="card-body">
                        <%-- Hiển thị thông báo lỗi từ backend nếu có (ví dụ: lỗi trùng email khi update) --%>
                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <i class="fas fa-exclamation-triangle me-2"></i>${errorMessage}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                        
                        <%-- Thông báo nếu không tìm thấy người dùng (user object rỗng) --%>
                        <c:if test="${empty user}">
                            <div class="alert alert-warning text-center" role="alert">
                                <i class="fas fa-exclamation-circle me-2"></i>Không tìm thấy thông tin người dùng để chỉnh sửa.
                            </div>
                        </c:if>
                        
                        <%-- Hiển thị form chỉ khi có user object --%>
                        <c:if test="${not empty user}">
                            Role: ${user.role}
                            <form id="editUserForm" action="${pageContext.request.contextPath}/user/edit" method="POST" class="needs-validation" novalidate>
                                <input type="hidden" id="id" name="id" value="${user.id}">
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label for="firstName" class="form-label">Họ <span class="text-danger">*</span></label>
                                        <input type="text" class="form-control" id="firstName" name="firstName" value="${user.firstName}" required>
                                        <div class="invalid-feedback">Vui lòng nhập họ.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="lastName" class="form-label">Tên <span class="text-danger">*</span></label>
                                        <input type="text" class="form-control" id="lastName" name="lastName" value="${user.lastName}" required>
                                        <div class="invalid-feedback">Vui lòng nhập tên.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="username" class="form-label">Tên đăng nhập</label>
                                        <input type="text" class="form-control" id="username" name="username" value="${user.username}" readonly>
                                        <div class="form-text">Tên đăng nhập không thể thay đổi.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="email" class="form-label">Email <span class="text-danger">*</span></label>
                                        <input type="email" class="form-control" id="email" name="email" value="${user.email}" readonly>
                                        <div class="form-text">Email không thể thay đổi</div>
                                    </div>
                                    <div class="col-md-12">
                                        <label for="role" class="form-label">Vai trò <span class="text-danger">*</span></label>
                                        <select class="form-select" id="role" name="role" required>
                                            <option value="">Chọn vai trò</option>
                                            <option value="user" <c:if test="${user.role eq 'User'}">selected</c:if>>User</option>
                                            <option value="admin" <c:if test="${user.role eq 'Admin'}">selected</c:if>>Admin</option>
                                        </select>
                                        <div class="invalid-feedback">Vui lòng chọn vai trò.</div>
                                    </div>
                                </div>
                                <hr class="my-4">
                                <div class="d-flex justify-content-end">
                                    <a href="${pageContext.request.contextPath}/user/dashboard" class="btn btn-secondary me-2">
                                        <i class="fas fa-times me-2"></i>Hủy
                                    </a>
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-save me-2"></i>Cập nhật
                                    </button>
                                </div>
                            </form>
                        </c:if>
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
            var form = document.getElementById('editUserForm');
            form.addEventListener('submit', function (event) {
                // Kiểm tra validation của Bootstrap
                if (!form.checkValidity()) {
                    event.preventDefault(); // Ngăn chặn gửi form nếu có lỗi
                    event.stopPropagation(); // Ngăn chặn sự kiện nổi bọt
                }
                form.classList.add('was-validated'); // Thêm class để hiển thị lỗi validation của Bootstrap
            }, false);
        })();

        // Các hàm togglePassword không còn cần thiết cho trang edit này vì đã bỏ phần mật khẩu.
        // Tuy nhiên, tôi vẫn để lại ở đây nếu bạn muốn sử dụng lại trong tương lai hoặc ở trang khác.
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