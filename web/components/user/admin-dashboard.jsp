<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Người dùng - EduPlan</title>
    
    <link href="/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        .avatar-circle {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background-color: #e9ecef;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 1.2rem;
            color: #6c757d;
        }
        .avatar-large-profile {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            background-color: #e9ecef;
            display: inline-flex; /* Use inline-flex for centering */
            justify-content: center;
            align-items: center;
            font-size: 3rem;
            color: #6c757d;
        }
    </style>
</head>
<body>
    <jsp:include page="../navigation/navigation.jsp" />
    <div class="container-fluid py-4">
        <div class="row mb-4">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="mb-1">Quản lý Người dùng</h2>
                        <p class="text-muted mb-0">Quản lý thông tin người dùng trong hệ thống</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/user/add" class="btn btn-primary">
                        <i class="fas fa-plus me-2"></i>Thêm người dùng
                    </a>
                </div>
            </div>
        </div>

        <div class="row mb-4">
            <div class="col-md-3">
                <div class="card border-start border-primary shadow-sm h-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <div class="flex-grow-1">
                                <h4 class="mb-0">${userStatistics.totalUsers != null ? userStatistics.totalUsers : 0}</h4>
                                <p class="text-muted mb-0">Tổng người dùng</p>
                            </div>
                            <i class="fas fa-users fa-2x text-primary"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card border-start border-success shadow-sm h-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <div class="flex-grow-1">
                                <h4 class="mb-0">${userStatistics.activeUsers != null ? userStatistics.activeUsers : 0}</h4>
                                <p class="text-muted mb-0">Đang hoạt động</p>
                            </div>
                            <i class="fas fa-user-check fa-2x text-success"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card border-start border-warning shadow-sm h-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <div class="flex-grow-1">
                                <h4 class="mb-0">${userStatistics.adminUsers != null ? userStatistics.adminUsers : 0}</h4>
                                <p class="text-muted mb-0">Quản trị viên</p>
                            </div>
                            <i class="fas fa-user-shield fa-2x text-warning"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card border-start border-info shadow-sm h-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <div class="flex-grow-1">
                                <h4 class="mb-0">${userStatistics.newUsersThisMonth != null ? userStatistics.newUsersThisMonth : 0}</h4>
                                <p class="text-muted mb-0">Mới tháng này</p>
                            </div>
                            <i class="fas fa-user-plus fa-2x text-info"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="card mb-4 shadow-sm">
            <div class="card-body">
                <form method="GET" action="${pageContext.request.contextPath}/user/dashboard">
                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label visually-hidden" for="search">Tìm kiếm</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="fas fa-search"></i></span>
                                <input type="text" class="form-control" id="search" name="search" 
                                       placeholder="Tìm theo tên, email, tên đăng nhập..."
                                       value="${param.search}">
                            </div>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label visually-hidden" for="roleFilter">Vai trò</label>
                            <select class="form-select" id="roleFilter" name="role">
                                <option value="">Tất cả vai trò</option>
                                <option value="Admin" ${param.role == 'Admin' ? 'selected' : ''}>Admin</option>
                                <option value="User" ${param.role == 'User' ? 'selected' : ''}>User</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label visually-hidden" for="sortOrder">Sắp xếp</label>
                            <select class="form-select" id="sortOrder" name="sort">
                                <option value="createdAt_desc" ${param.sort == 'createdAt_desc' ? 'selected' : ''}>Mới nhất</option>
                                <option value="createdAt_asc" ${param.sort == 'createdAt_asc' ? 'selected' : ''}>Cũ nhất</option>
                                <option value="firstName_asc" ${param.sort == 'firstName_asc' ? 'selected' : ''}>Tên A-Z</option>
                                <option value="firstName_desc" ${param.sort == 'firstName_desc' ? 'selected' : ''}>Tên Z-A</option>
                            </select>
                        </div>
                        <div class="col-md-2 d-grid">
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-filter me-2"></i>Lọc
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <div class="card shadow-sm">
            <div class="card-header bg-light">
                <h5 class="mb-0">
                    <i class="fas fa-list-alt me-2"></i>Danh sách người dùng
                    <span class="badge bg-primary ms-2">${userList != null ? userList.size() : 0}</span>
                </h5>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover table-striped mb-0">
                        <thead class="table-light">
                            <tr>
                                <th scope="col" style="width: 5%;">#</th>
                                <th scope="col" style="width: 25%;">Thông tin</th>
                                <th scope="col" style="width: 25%;">Email</th>
                                <th scope="col" style="width: 15%;">Vai trò</th>
                                <th scope="col" style="width: 15%;">Ngày tạo</th>
                                <th scope="col" style="width: 10%;">Trạng thái</th>
                                <th scope="col" style="width: 5%;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty userList}">
                                    <c:forEach var="user" items="${userList}" varStatus="status">
                                        <tr>
                                            <td>${status.index + 1}</td>
                                            <td>
                                                <div class="d-flex align-items-center">
                                                    <div class="avatar-circle me-3">
                                                        <i class="fas fa-user"></i>
                                                    </div>
                                                    <div>
                                                        <div class="fw-semibold text-primary">${user.firstName} ${user.lastName}</div>
                                                        <div class="text-muted small">${user.username}</div>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>${user.email}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${user.role == 'Admin'}">
                                                        <span class="badge bg-danger">
                                                            <i class="fas fa-user-shield me-1"></i>Admin
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-success">
                                                            <i class="fas fa-user me-1"></i>User
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <fmt:formatDate value="${user.createdAt}" pattern="dd/MM/yyyy"/>
                                            </td>
                                            <td>
                                                <span class="badge bg-success">
                                                    <i class="fas fa-circle fa-xs me-1"></i>Hoạt động
                                                </span>
                                            </td>
                                            <td>
                                                <div class="d-flex gap-1">
                                                    <a href="${pageContext.request.contextPath}/user/detail?id=${user.id}" class="btn btn-sm btn-outline-info" title="Xem chi tiết">
                                                        <i class="fas fa-eye"></i>
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/user/edit?id=${user.id}" class="btn btn-sm btn-outline-warning" title="Chỉnh sửa">
                                                        <i class="fas fa-edit"></i>
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/user/delete-confirm?id=${user.id}" class="btn btn-sm btn-outline-danger" title="Xóa">
                                                        <i class="fas fa-trash"></i>
                                                    </a>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="7" class="text-center py-4">
                                            <i class="fas fa-user-slash fa-3x text-muted mb-3"></i>
                                            <p class="text-muted mb-0">Không có người dùng nào được tìm thấy theo tiêu chí lọc.</p>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
        </div>
        </div>
                
        <jsp:include page="../pagination/pagination.jsp"/>
    </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
</body>
</html>