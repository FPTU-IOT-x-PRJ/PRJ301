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
        
        <!-- CSS tùy chỉnh cho table -->
        <style>
            .table-custom {
                font-size: 14px;
            }
            
            .table-custom th,
            .table-custom td {
                vertical-align: middle;
                padding: 12px 8px;
            }
            
            /* Căn chỉnh độ rộng cột */
            .table-custom th:nth-child(1),
            .table-custom td:nth-child(1) {
                width: 5%;
                text-align: center;
            }
            
            .table-custom th:nth-child(2),
            .table-custom td:nth-child(2) {
                width: 25%;
                font-weight: 500;
            }
            
            .table-custom th:nth-child(3),
            .table-custom td:nth-child(3) {
                width: 15%;
                text-align: center;
            }
            
            .table-custom th:nth-child(4),
            .table-custom td:nth-child(4) {
                width: 15%;
                text-align: center;
            }
            
            .table-custom th:nth-child(5),
            .table-custom td:nth-child(5) {
                width: 15%;
                text-align: center;
            }
            
            .table-custom th:nth-child(6),
            .table-custom td:nth-child(6) {
                width: 25%;
                text-align: center;
            }
            
            /* Style cho badge */
            .badge {
                font-size: 0.75em;
                padding: 0.5em 0.75em;
            }
            
            /* Style cho buttons */
            .btn-group-actions {
                display: flex;
                gap: 4px;
                justify-content: center;
            }
            
            .btn-sm {
                padding: 0.25rem 0.5rem;
                font-size: 0.875rem;
            }
            
            /* Responsive */
            @media (max-width: 768px) {
                .table-custom {
                    font-size: 12px;
                }
                
                .table-custom th,
                .table-custom td {
                    padding: 8px 4px;
                }
                
                .btn-group-actions {
                    flex-direction: column;
                    gap: 2px;
                }
            }
            .badge-fixed-width {
                display: inline-block;
                width: 120px;
                text-align: center;
            }

            .badge-fixed-width i {
                width: 1em;
                text-align: center;
            }

            /* Style cho form lọc */
            .input-group-text {
                background-color: #f8f9fa;
                border-color: #dee2e6;
                color: #6c757d;
            }
            
            .form-control:focus,
            .form-select:focus {
                border-color: #0d6efd;
                box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.25);
            }
        </style>
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
                    <form method="GET" action="${pageContext.request.contextPath}/semesters/dashboard" id="filterForm">
                        <div class="row g-3">
                            <!-- Tìm kiếm theo tên -->
                            <div class="col-md-4">
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-search"></i></span>
                                    <input type="text" class="form-control" name="search" placeholder="Tìm theo tên kỳ học..."
                                           value="${param.search}">
                                </div>
                            </div>
                            
                            <!-- Lọc theo trạng thái -->
                            <div class="col-md-3">
                                <select class="form-select" name="status">
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="Active" ${param.status == 'Active' ? 'selected' : ''}>Đang diễn ra</option>
                                    <option value="Inactive" ${param.status == 'Inactive' ? 'selected' : ''}>Bảo lưu</option>
                                    <option value="Completed" ${param.status == 'Completed' ? 'selected' : ''}>Hoàn thành</option>
                                </select>
                            </div>
                            
                            <!-- Lọc theo thời gian bắt đầu -->
                            <div class="col-md-2">
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-calendar-plus"></i></span>
                                    <input type="date" class="form-control" name="startDate" id="startDate"
                                           value="${param.startDate}" title="Từ ngày">
                                </div>
                            </div>
                            
                            <!-- Lọc theo thời gian kết thúc -->
                            <div class="col-md-2">
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-calendar-minus"></i></span>
                                    <input type="date" class="form-control" name="endDate" id="endDate"
                                           value="${param.endDate}" title="Đến ngày">
                                </div>
                            </div>
                            
                            <!-- Nút lọc -->
                            <div class="col-md-1 d-grid">
                                <button type="submit" class="btn btn-primary" title="Áp dụng bộ lọc">
                                    <i class="fas fa-filter"></i>
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Danh sách kỳ học -->
            <div class="card shadow-sm">
                <div class="card-header bg-light d-flex align-items-center">
                    <h5 class="mb-0"><i class="fas fa-calendar-alt me-2"></i>Danh sách kỳ học</h5>
                    <span class="badge bg-primary ms-2">${semesterList != null ? semesterList.size() : 0}</span>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover table-striped table-custom mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th class="text-center">#</th>
                                    <th><i class="fas fa-graduation-cap me-2"></i>Tên kỳ học</th>
                                    <th class="text-center"><i class="fas fa-calendar-plus me-2"></i>Ngày bắt đầu</th>
                                    <th class="text-center"><i class="fas fa-calendar-minus me-2"></i>Ngày kết thúc</th>
                                    <th class="text-center"><i class="fas fa-info-circle me-2"></i>Trạng thái</th>
                                    <th class="text-center"><i class="fas fa-cogs me-2"></i>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty semesterList}">
                                        <c:forEach items="${semesterList}" var="semester" varStatus="status">
                                            <tr>
                                                <td class="text-center fw-bold">${status.index + 1}</td>
                                                <td>
                                                    <div class="d-flex align-items-center">
                                                        <i class="fas fa-book text-primary me-2"></i>
                                                        <span>${semester.name}</span>
                                                    </div>
                                                </td>
                                                <td class="text-center">
                                                    <span class="text-success">
                                                        <fmt:formatDate value="${semester.startDate}" pattern="dd/MM/yyyy"/>
                                                    </span>
                                                </td>
                                                <td class="text-center">
                                                    <span class="text-danger">
                                                        <fmt:formatDate value="${semester.endDate}" pattern="dd/MM/yyyy"/>
                                                    </span>
                                                </td>
                                                <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${semester.status == 'Active'}">
                                                        <span class="badge bg-success badge-fixed-width">
                                                            <i class="fas fa-play me-1"></i>Đang diễn ra
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${semester.status == 'Inactive'}">
                                                        <span class="badge bg-warning badge-fixed-width">
                                                            <i class="fas fa-pause me-1"></i>Bảo lưu
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${semester.status == 'Completed'}">
                                                        <span class="badge bg-primary badge-fixed-width">
                                                            <i class="fas fa-check me-1"></i>Hoàn thành
                                                        </span>
                                                    </c:when>
                                                </c:choose>
                                                </td>
                                                <td>
                                                    <div class="btn-group-actions">
                                                        <a href="${pageContext.request.contextPath}/semesters/detail?id=${semester.id}" 
                                                           class="btn btn-sm btn-outline-info" 
                                                           title="Xem chi tiết"
                                                           data-bs-toggle="tooltip">
                                                            <i class="fas fa-eye"></i>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/semesters/edit?id=${semester.id}" 
                                                           class="btn btn-sm btn-outline-warning" 
                                                           title="Chỉnh sửa"
                                                           data-bs-toggle="tooltip">
                                                            <i class="fas fa-edit"></i>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/semesters/delete?id=${semester.id}" 
                                                           class="btn btn-sm btn-outline-danger"
                                                           title="Xóa"
                                                           data-bs-toggle="tooltip"
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
                                            <td colspan="6" class="text-center py-5">
                                                <div class="d-flex flex-column align-items-center">
                                                    <i class="fas fa-calendar-times fa-3x text-muted mb-3"></i>
                                                    <h5 class="text-muted mb-2">Không có dữ liệu</h5>
                                                    <p class="text-muted mb-0">Không tìm thấy kỳ học nào theo tiêu chí lọc.</p>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <jsp:include page="../pagination/pagination.jsp"/>
    </body>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
    <script>
        // Khởi tạo tooltips
        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl)
        })

        // Validation form lọc
        document.getElementById('filterForm').addEventListener('submit', function(e) {
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;

            if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
                e.preventDefault();
                alert('Ngày bắt đầu không thể lớn hơn ngày kết thúc!');
                return false;
            }
        });

        // Xóa validation khi thay đổi ngày
        document.getElementById('startDate').addEventListener('change', function() {
            validateDateRange();
        });

        document.getElementById('endDate').addEventListener('change', function() {
            validateDateRange();
        });

        function validateDateRange() {
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;
            const startInput = document.getElementById('startDate');
            const endInput = document.getElementById('endDate');

            if (startDate && endDate) {
                if (new Date(startDate) > new Date(endDate)) {
                    startInput.classList.add('is-invalid');
                    endInput.classList.add('is-invalid');
                } else {
                    startInput.classList.remove('is-invalid');
                    endInput.classList.remove('is-invalid');
                }
            } else {
                startInput.classList.remove('is-invalid');
                endInput.classList.remove('is-invalid');
            }
        }
    </script>
</html>
