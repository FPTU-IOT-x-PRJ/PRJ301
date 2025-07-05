<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh Sửa Hồ Sơ Cá Nhân - EduPlan</title> <%-- Thay đổi tiêu đề --%>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet"> <%-- Sửa đường dẫn CSS --%>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: var(--light-color); /* Nền trắng nhạt theo common.css */
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }
        .container {
            flex-grow: 1; /* Cho phép container mở rộng để chiếm không gian còn lại */
            display: flex; /* Dùng flexbox để căn giữa nội dung bên trong */
            align-items: center; /* Căn giữa theo chiều dọc */
            justify-content: center; /* Căn giữa theo chiều ngang */
            padding-top: 20px; /* Đảm bảo có padding sau navigation */
            padding-bottom: 20px;
        }
    </style>
</head>
<body>
    <jsp:include page="../navigation/navigation.jsp" />

    <div class="container">
        <div class="row justify-content-center w-100">
            <div class="col-lg-8 col-md-10">
                <div class="card shadow-lg"> <%-- Thêm shadow-lg --%>
                    <div class="card-header bg-dark text-white py-3"> <%-- Đổi màu header sang bg-dark --%>
                        <h4 class="mb-0 text-center"><i class="fas fa-user-edit me-2"></i>Chỉnh sửa hồ sơ của bạn</h4> <%-- Thay đổi tiêu đề header --%>
                    </div>
                    <div class="card-body p-4"> <%-- Thêm padding --%>
                        <%-- Hiển thị thông báo lỗi từ backend nếu có (ví dụ: lỗi khi cập nhật) --%>
                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <i class="fas fa-exclamation-triangle me-2"></i>${errorMessage}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                        
                        <%-- Thông báo nếu người dùng chưa đăng nhập --%>
                        <c:if test="${empty sessionScope.loggedInUser}">
                            <div class="alert alert-warning text-center" role="alert">
                                <i class="fas fa-exclamation-circle me-2"></i>Bạn cần đăng nhập để chỉnh sửa hồ sơ cá nhân.
                                <p class="mt-2"><a href="${pageContext.request.contextPath}/auth/login" class="alert-link">Đăng nhập ngay</a></p>
                            </div>
                        </c:if>
                        
                        <%-- Hiển thị form chỉ khi có user object trong session --%>
                        <c:if test="${not empty sessionScope.loggedInUser}">
                            <form id="editUserProfileForm" action="${pageContext.request.contextPath}/user/updateProfile" method="POST" class="needs-validation" novalidate> <%-- Đổi action --%>
                                <input type="hidden" id="id" name="id" value="${sessionScope.loggedInUser.id}"> <%-- Lấy ID từ session --%>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label for="firstName" class="form-label">Tên <span class="text-danger">*</span></label> <%-- Đổi Họ -> Tên, Tên -> Họ đệm --%>
                                        <input type="text" class="form-control" id="firstName" name="firstName" value="${sessionScope.loggedInUser.firstName}" required>
                                        <div class="invalid-feedback">Vui lòng nhập tên của bạn.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="lastName" class="form-label">Họ đệm <span class="text-danger">*</span></label>
                                        <input type="text" class="form-control" id="lastName" name="lastName" value="${sessionScope.loggedInUser.lastName}" required>
                                        <div class="invalid-feedback">Vui lòng nhập họ đệm của bạn.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="username" class="form-label">Tên đăng nhập</label>
                                        <input type="text" class="form-control" id="username" name="username" value="${sessionScope.loggedInUser.username}" readonly>
                                        <div class="form-text">Tên đăng nhập không thể thay đổi.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="email" class="form-label">Email</label>
                                        <input type="email" class="form-control" id="email" name="email" value="${sessionScope.loggedInUser.email}" readonly>
                                        <div class="form-text">Email không thể thay đổi.</div>
                                    </div>
                                    <%-- Bỏ phần chọn Role vì người dùng không thể tự thay đổi vai trò của mình --%>
                                    <div class="col-md-12">
                                        <label class="form-label">Vai trò hiện tại:</label>
                                        <p class="form-control-plaintext">
                                            <c:choose>
                                                <c:when test="${sessionScope.loggedInUser.role eq 'Admin'}">
                                                    <span class="badge bg-danger"><i class="fas fa-user-shield me-1"></i>Admin</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-success"><i class="fas fa-user me-1"></i>User</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </p>
                                    </div>
                                </div>
                                <hr class="my-4">
                                <div class="d-flex justify-content-end">
                                    <a href="${pageContext.request.contextPath}/user/profile" class="btn btn-secondary me-2"> <%-- Sửa đường dẫn Hủy --%>
                                        <i class="fas fa-times me-2"></i>Hủy
                                    </a>
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-save me-2"></i>Lưu thay đổi
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
            var form = document.getElementById('editUserProfileForm'); // Sửa ID form
            if (form) { // Đảm bảo form tồn tại trước khi thêm event listener
                form.addEventListener('submit', function (event) {
                    // Kiểm tra validation của Bootstrap
                    if (!form.checkValidity()) {
                        event.preventDefault(); // Ngăn chặn gửi form nếu có lỗi
                        event.stopPropagation(); // Ngăn chặn sự kiện nổi bọt
                    }
                    form.classList.add('was-validated'); // Thêm class để hiển thị lỗi validation của Bootstrap
                }, false);
            }
        })();
        // Các hàm togglePassword không còn cần thiết cho trang này
    </script>
</body>
</html>