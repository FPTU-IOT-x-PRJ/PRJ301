<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="entity.*" %>
<%
    User user = (User) request.getAttribute("user");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi Tiết Người Dùng - EduPlan</title>
    <link href="/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="../navigation/navigation.jsp" />

    <div class="container py-4">
        <div class="row justify-content-center">
            <div class="col-lg-7 col-md-9">
                <div class="card shadow-sm">
                    <div class="card-header bg-info text-white">
                        <h4 class="mb-0"><i class="fas fa-user me-2"></i>Thông tin người dùng</h4>
                    </div>
                    <div class="card-body">
                        <c:if test="${empty user}">
                            <div class="alert alert-warning text-center" role="alert">
                                <i class="fas fa-exclamation-circle me-2"></i>Không tìm thấy thông tin người dùng.
                            </div>
                        </c:if>
                        <c:if test="${not empty user}">
                            <div class="text-center mb-4">
                                <div class="avatar-large-profile mb-3">
                                    <i class="fas fa-user fa-4x text-muted"></i>
                                </div>
                                <h4>${user.firstName} ${user.lastName}</h4>
                                <p class="text-muted">${user.username}</p>
                            </div>

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">ID:</label>
                                    <p class="form-control-plaintext">${user.id}</p>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Email:</label>
                                    <p class="form-control-plaintext">${user.email}</p>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Vai trò:</label>
                                    <p class="form-control-plaintext">
                                        <c:choose>
                                            <c:when test="${user.role == 'Admin'}">
                                                <span class="badge bg-danger"><i class="fas fa-user-shield me-1"></i>Admin</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-success"><i class="fas fa-user me-1"></i>User</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Ngày tạo:</label>
                                    <p class="form-control-plaintext">
                                        <fmt:formatDate value="${user.createdAt}" pattern="dd/MM/yyyy"/>
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
                    <div class="card-footer text-end">
                        <a href="/user" class="btn btn-secondary me-2">
                            <i class="fas fa-arrow-left me-2"></i>Quay lại
                        </a>
                        <c:if test="${not empty user}">
                            <a href="${pageContext.request.contextPath}/user/edit?id=${user.id}" class="btn btn-warning">
                                <i class="fas fa-edit me-2"></i>Chỉnh sửa
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