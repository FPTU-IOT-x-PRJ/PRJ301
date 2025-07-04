<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông tin hồ sơ - EduPlan</title>
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
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
        .avatar-large-profile {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            background-color: var(--gray-light-color); /* Màu nền avatar */
            display: inline-flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
            border: 2px solid var(--primary-color); /* Viền màu chủ đạo */
        }
        .avatar-large-profile i {
            color: var(--dark-color); /* Màu icon */
        }
    </style>
</head>
<body>
    <jsp:include page="../navigation/navigation.jsp" />
    
    <div class="container">
        <div class="row justify-content-center w-100">
            <div class="col-lg-7 col-md-9">
                <div class="card shadow-lg">
                    <div class="card-header bg-dark text-white py-3">
                        <h4 class="mb-0 text-center"><i class="fas fa-id-badge me-2"></i>Thông tin hồ sơ của bạn</h4> <%-- Đổi tiêu đề --%>
                    </div>
                    <div class="card-body p-4">
                        <%-- Kiểm tra nếu người dùng chưa đăng nhập --%>
                        <c:if test="${empty sessionScope.loggedInUser}">
                            <div class="alert alert-warning text-center" role="alert">
                                <i class="fas fa-exclamation-circle me-2"></i>Bạn cần đăng nhập để xem hồ sơ.
                                <p class="mt-2"><a href="${pageContext.request.contextPath}/auth/login" class="alert-link">Đăng nhập ngay</a></p>
                            </div>
                        </c:if>

                        <c:if test="${not empty sessionScope.loggedInUser}">
                            <div class="text-center mb-4">
                                <div class="avatar-large-profile mb-3 mx-auto">
                                    <i class="fas fa-user fa-4x"></i>
                                </div>
                                <h4><span class="text-primary">${sessionScope.loggedInUser.firstName} ${sessionScope.loggedInUser.lastName}</span></h4>
                                <p class="text-muted mb-0">@${sessionScope.loggedInUser.username}</p>
                            </div>

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Email:</label>
                                    <p class="form-control-plaintext">${sessionScope.loggedInUser.email}</p>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Vai trò:</label>
                                    <p class="form-control-plaintext">
                                        <c:choose>
                                            <c:when test="${sessionScope.loggedInUser.role == 'Admin'}">
                                                <span class="badge bg-danger"><i class="fas fa-user-shield me-1"></i>Admin</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-success"><i class="fas fa-user me-1"></i>User</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Ngày tạo tài khoản:</label>
                                    <p class="form-control-plaintext">
                                        <fmt:formatDate value="${sessionScope.loggedInUser.createdAt}" pattern="dd/MM/yyyy"/>
                                    </p>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Trạng thái:</label>
                                    <p class="form-control-plaintext">
                                        <span class="badge bg-success"><i class="fas fa-circle fa-xs me-1"></i>Hoạt động</span>
                                    </p>
                                </div>
                            </div>
                        </c:if>
                    </div>
                    <div class="card-footer text-end p-3">
                        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary me-2">
                            <i class="fas fa-arrow-left me-2"></i>Quay lại trang chủ
                        </a>
                        <%-- Nút chỉnh sửa luôn hiển thị khi người dùng đã đăng nhập --%>
                        <c:if test="${not empty sessionScope.loggedInUser}">
                            <a href="${pageContext.request.contextPath}/user/profile-edit" class="btn btn-warning">
                                <i class="fas fa-edit me-2"></i>Chỉnh sửa hồ sơ
                            </a>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
</body>
</html>