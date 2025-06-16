<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quản lý Kỳ học - EduPlan</title>
        <link href="/css/common.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    </head>
    <body>
        <jsp:include page="../navigation/navigation.jsp" />

        <div class="container-fluid py-4">
            <div class="row mb-4">
                <div class="col-12 d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="mb-1">Quản lý Kỳ học</h2>
                        <p class="text-muted mb-0">Theo dõi các kỳ học được tạo trong hệ thống</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/semesters/add" class="btn btn-primary">
                        <i class="fas fa-plus me-2"></i>Thêm kỳ học
                    </a>
                </div>
            </div>

            <!-- Bộ lọc -->
            <div class="card mb-4 shadow-sm">
                <div class="card-body">
                    <form method="GET" action="${pageContext.request.contextPath}/semesters/dashboard">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-search"></i></span>
                                    <input type="text" class="form-control" name="search" placeholder="Tìm theo tên kỳ học..."
                                           value="${param.search}">
                                </div>
                            </div>
                            <div class="col-md-4">
                                <select class="form-select" name="status">
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="Active" ${param.status == 'Active' ? 'selected' : ''}>Đang diễn ra</option>
                                    <option value="Inactive" ${param.status == 'Inactive' ? 'selected' : ''}>Đã kết thúc</option>
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

            <!-- Danh sách kỳ học -->
            <div class="card shadow-sm">
                <div class="card-header bg-light">
                    <h5 class="mb-0"><i class="fas fa-calendar-alt me-2"></i>Danh sách kỳ học</h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover table-striped mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th>ID</th>
                                    <th>Tên kỳ học</th>
                                    <th>Ngày bắt đầu</th>
                                    <th>Ngày kết thúc</th>
                                    <th>Trạng thái</th>
                                    <th>Ngày tạo</th>
                                    <th>Ngày cập nhật</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty semesterList}">
                                        <c:forEach items="${semesterList}" var="semester">
                                            <tr>
                                                <td>${semester.id}</td>
                                                <td>${semester.name}</td>
                                                <td><fmt:formatDate value="${semester.startDate}" pattern="dd/MM/yyyy"/></td>
                                                <td><fmt:formatDate value="${semester.endDate}" pattern="dd/MM/yyyy"/></td>
                                                <td>
                                                    <span class="badge ${semester.status == 'Active' ? 'bg-success' : 'bg-secondary'}">
                                                        ${semester.status == 'Active' ? 'Starting' : 'Ended'}
                                                    </span>
                                                </td>
                                                <td>${semester.createdAt.toLocalDate()}</td>
                                                <td>${semester.updatedAt.toLocalDate()}</td>

                                                <td>
                                                    <div class="d-flex gap-1">
                                                        <a href="${pageContext.request.contextPath}/semesters/update?id=${semester.id}" class="btn btn-sm btn-outline-warning" title="Chỉnh sửa">
                                                            <i class="fas fa-edit"></i>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/semesters/delete?id=${semester.id}" 
                                                           class="btn btn-sm btn-outline-danger"
                                                           onclick="return confirm('Bạn có chắc chắn muốn xóa kỳ học này?');">
                                                            <i class="fas fa-trash"></i>
                                                        </a>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="8" class="text-center py-4">
                                                <i class="fas fa-calendar-times fa-3x text-muted mb-3"></i>
                                                <p class="text-muted mb-0">Không tìm thấy kỳ học nào theo tiêu chí lọc.</p>
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
