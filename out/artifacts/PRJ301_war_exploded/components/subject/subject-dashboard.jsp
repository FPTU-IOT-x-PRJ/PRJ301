<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quản lý Môn học - EduPlan</title>
        <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">

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
                width: 20%; /* Cột mã môn học */
                text-align: center;
            }

            .table-custom th:nth-child(4),
            .table-custom td:nth-child(4) {
                width: 15%; /* Cột tín chỉ */
                text-align: center;
            }
            
            .table-custom th:nth-child(5),
            .table-custom td:nth-child(5) {
                width: 15%; /* Cột trạng thái */
                text-align: center;
            }

            .table-custom th:nth-child(6),
            .table-custom td:nth-child(6) {
                width: 20%; /* Cột hành động */
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

            /* CSS mới cho màn hình không có dữ liệu (khi không có kỳ học nào) */
            .no-data-container {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                min-height: calc(100vh - 250px); /* Điều chỉnh dựa trên chiều cao header/footer của bạn */
                text-align: center;
            }
            .no-data-container .icon-large {
                font-size: 6rem; /* Kích thước icon lớn */
                color: #ced4da; /* Màu xám nhạt */
                margin-bottom: 1.5rem;
            }
            .no-data-container h4 {
                font-size: 1.8rem;
                color: #343a40;
                margin-bottom: 0.75rem;
            }
            .no-data-container p {
                font-size: 1.1rem;
                color: #6c757d;
                max-width: 600px;
                margin-bottom: 2rem;
            }
            .no-data-container .btn-create-item {
                padding: 0.75rem 2rem;
                font-size: 1.1rem;
                background-color: #0d6efd; /* Blue */
                border-color: #0d6efd;
                display: flex;
                align-items: center;
                gap: 0.5rem;
            }
            .no-data-container .btn-create-item:hover {
                background-color: #0b5ed7;
                border-color: #0a58ca;
            }
        </style>
    </head>
    <body>
        <jsp:include page="../navigation/navigation.jsp" />

        <div class="container-fluid py-4">
            <div class="row mb-4">
                <div class="col-12 d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="mb-1">Quản lý Môn học</h2>
                        <p class="text-muted mb-0">Xem và quản lý các môn học được tạo trong hệ thống</p>
                    </div>
                    <%-- Nút thêm môn học chỉ hiển thị khi có ít nhất một kỳ học --%>
                    <c:if test="${not empty semesterList}">
                        <a href="${pageContext.request.contextPath}/subjects/add" class="btn btn-primary">
                            <i class="fas fa-plus me-2"></i>Thêm môn học
                        </a>
                    </c:if>
                </div>
            </div>

            <c:choose>
                <%-- Kiểm tra nếu không có kỳ học nào trong semesterList --%>
                <c:when test="${empty semesterList}">
                    <div class="no-data-container">
                        <i class="fas fa-calendar-times icon-large"></i>
                        <h4>Chưa có kỳ học nào được tạo</h4>
                        <p>Để quản lý môn học, bạn cần phải có ít nhất một kỳ học trong hệ thống. Vui lòng tạo kỳ học mới để bắt đầu.</p>
                        <a href="${pageContext.request.contextPath}/semesters/add" class="btn btn-primary btn-create-item">
                            <i class="fas fa-plus"></i> Tạo kỳ học mới
                        </a>
                        <div class="alert alert-info mt-4" role="alert">
                             Chưa có kỳ học nào. Vui lòng thêm kỳ học mới để quản lý môn học.
                        </div>
                    </div>
                </c:when>
                <%-- Nếu có kỳ học, hiển thị giao diện quản lý môn học --%>
                <c:otherwise>
                    <div class="card mb-4 shadow-sm">
                        <div class="card-body">
                            <form method="GET" action="${pageContext.request.contextPath}/subjects/dashboard" id="filterForm">
                                <div class="row g-3 align-items-end">
                                    <div class="col-md-4">
                                        <label for="semesterSelect" class="form-label mb-1">Chọn Kỳ học:</label>
                                        <select class="form-select" name="semesterId" id="semesterSelect" onchange="this.form.submit()">
                                            <option value="">Tất cả kỳ học</option>
                                            <c:forEach items="${semesterList}" var="sem">
                                                <option value="${sem.id}" ${param.semesterId == sem.id ? 'selected' : ''}>
                                                    ${sem.name}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>

                                    <div class="col-md-4">
                                        <label for="searchSubject" class="form-label mb-1">Tìm kiếm môn học:</label>
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="fas fa-search"></i></span>
                                            <input type="text" class="form-control" name="search" id="searchSubject" placeholder="Tìm theo tên hoặc mã môn học..."
                                                   value="${param.search}">
                                        </div>
                                    </div>
                                    
                                    <div class="col-md-2">
                                        <label for="statusSelect" class="form-label mb-1">Trạng thái:</label>
                                        <select class="form-select" name="status" id="statusSelect">
                                            <option value="">Tất cả</option>
                                            <option value="Active" ${param.status == 'Active' ? 'selected' : ''}>Đang học</option>
                                            <option value="Completed" ${param.status == 'Completed' ? 'selected' : ''}>Hoàn thành</option>
                                            <option value="Dropped" ${param.status == 'Dropped' ? 'selected' : ''}>Bỏ học</option>
                                        </select>
                                    </div>

                                    <div class="col-md-2 d-grid">
                                        <button type="submit" class="btn btn-primary" title="Áp dụng bộ lọc">
                                            <i class="fas fa-filter"></i> Lọc
                                        </button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="card shadow-sm">
                        <div class="card-header bg-light d-flex align-items-center">
                            <h5 class="mb-0"><i class="fas fa-book-open me-2"></i>Danh sách môn học</h5>
                            <span class="badge bg-primary ms-2">${subjectList != null ? subjectList.size() : 0}</span>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover table-striped table-custom mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th class="text-center">#</th>
                                            <th>Tên môn học</th>
                                            <th class="text-center">Mã môn học</th>
                                            <th class="text-center">Tín chỉ</th>
                                            <th class="text-center">Trạng thái</th>
                                            <th class="text-center">Hành động</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${not empty subjectList}">
                                                <c:forEach items="${subjectList}" var="subject" varStatus="status">
                                                    <tr>
                                                        <td class="text-center fw-bold">${status.index + 1}</td>
                                                        <td>
                                                            <div class="d-flex align-items-center">
                                                                <i class="fas fa-book-reader text-success me-2"></i>
                                                                <span>${subject.name}</span>
                                                            </div>
                                                        </td>
                                                        <td class="text-center">${subject.code}</td>
                                                        <td class="text-center">${subject.credits}</td>
                                                        <td class="text-center">
                                                            <c:choose>
                                                                <c:when test="${subject.status == 'Active'}">
                                                                    <span class="badge bg-success badge-fixed-width">
                                                                        Đang học
                                                                    </span>
                                                                </c:when>
                                                                <c:when test="${subject.status == 'Completed'}">
                                                                    <span class="badge bg-primary badge-fixed-width">
                                                                        Hoàn thành
                                                                    </span>
                                                                </c:when>
                                                                <c:when test="${subject.status == 'Dropped'}">
                                                                    <span class="badge bg-danger badge-fixed-width">
                                                                        Bỏ học
                                                                    </span>
                                                                </c:when>
                                                            </c:choose>
                                                        </td>
                                                        <td>
                                                            <div class="btn-group-actions">
                                                                <a href="${pageContext.request.contextPath}/subjects/detail?id=${subject.id}"
                                                                   class="btn btn-sm btn-outline-info"
                                                                   title="Xem chi tiết"
                                                                   data-bs-toggle="tooltip">
                                                                    <i class="fas fa-eye"></i>
                                                                </a>
                                                                <a href="${pageContext.request.contextPath}/subjects/edit?id=${subject.id}"
                                                                   class="btn btn-sm btn-outline-warning"
                                                                   title="Chỉnh sửa"
                                                                   data-bs-toggle="tooltip">
                                                                    <i class="fas fa-edit"></i>
                                                                </a>
                                                                <a href="${pageContext.request.contextPath}/subjects/delete-confirm?id=${subject.id}"
                                                                   class="btn btn-sm btn-outline-danger"
                                                                   title="Xóa"
                                                                   data-bs-toggle="tooltip">
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
                                                            <i class="fas fa-file-excel fa-3x text-muted mb-3"></i>
                                                            <h5 class="text-muted mb-2">Không tìm thấy môn học nào</h5>
                                                            <p class="text-muted mb-0">Không có môn học nào theo tiêu chí lọc hoặc cho kỳ học này.</p>
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
                    <%-- Bạn có thể thêm include pagination ở đây nếu cần --%>
                    <%-- <jsp:include page="../pagination/pagination.jsp"/> --%>
                </c:otherwise>
            </c:choose>
        </div>

        <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
        <script>
            // Khởi tạo tooltips
            var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
            var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
                return new bootstrap.Tooltip(tooltipTriggerEl)
            })
        </script>
    </body>
</html>