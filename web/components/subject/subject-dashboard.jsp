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
                width: 20%; /* Điều chỉnh cho tên môn */
                font-weight: 500;
            }
            
            .table-custom th:nth-child(3),
            .table-custom td:nth-child(3) {
                width: 10%; /* Mã môn */
                text-align: center;
            }
            
            .table-custom th:nth-child(4),
            .table-custom td:nth-child(4) {
                width: 10%; /* Tín chỉ */
                text-align: center;
            }

            .table-custom th:nth-child(5),
            .table-custom td:nth-child(5) {
                width: 20%; /* Giảng viên */
            }

            .table-custom th:nth-child(6),
            .table-custom td:nth-child(6) {
                width: 15%; /* Trạng thái */
                text-align: center;
            }

            .table-custom th:nth-child(7),
            .table-custom td:nth-child(7) {
                width: 20%; /* Hành động */
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
                width: 100px; /* Adjust width as needed */
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
                        <h2 class="mb-1">Quản lý Môn học</h2>
                        <p class="text-muted mb-0">Danh sách môn học trong hệ thống</p>
                        <!-- Hiển thị kỳ học hiện tại và dropdown chuyển kỳ học -->
                        <div class="mt-3 d-flex align-items-center gap-3">
                            <h5 class="mb-0 text-primary">
                                <i class="fas fa-calendar-alt me-1"></i>
                                <span>Đang xem kỳ học:</span> 
                                <strong>${currentSemester.name}</strong>
                            </h5>

                            <form method="get" action="${pageContext.request.contextPath}/subjects" class="d-flex align-items-center">
                                <input type="hidden" name="page" value="1" />
<!--                                <input type="hidden" name="semesterId" value="${semesterId}"/>-->
                                <label for="semesterId" class="me-2 mb-0 fw-bold text-muted">Chuyển kỳ:</label>
                                <select name="semesterId" id="semesterId" class="form-select form-select-sm" onchange="this.form.submit()">
                                    <c:forEach var="s" items="${allSemesters}">
                                        <option value="${s.id}" ${s.id == semesterId ? 'selected' : ''}>${s.name}</option>
                                    </c:forEach>
                                </select>
                            </form>
                        </div>

                    </div>
                    <a href="${pageContext.request.contextPath}/subjects/add?semesterId=${semesterId}" class="btn btn-primary">
                        <i class="fas fa-plus me-2"></i>Thêm môn học
                    </a>
                </div>
            </div>

            <div class="card mb-4 shadow-sm">
                <div class="card-body">
                    <form method="GET" action="${pageContext.request.contextPath}/subjects" id="filterForm">
                        <input type="hidden" name="page" value="${currentPage}" />
                        <div class="row g-3">
                            <div class="col-md-4">
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-search"></i></span>
                                    <input type="text" class="form-control" name="search" placeholder="Tìm theo tên môn học hoặc mã..."
                                           value="${param.search}">
                                </div>
                            </div>

                            <div class="col-md-3">
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-calendar-alt"></i></span>
                                    <select name="semesterId" id="semesterId" class="form-select">
                                        <c:forEach var="s" items="${allSemesters}">
                                            <option value="${s.id}" ${s.id == semesterId ? 'selected' : ''}>${s.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            
                            <div class="col-md-3">
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-user-tie"></i></span>
                                    <%-- Đã chuyển từ select sang input text cho tìm kiếm giảng viên --%>
                                    <input type="text" class="form-control" name="teacherName" placeholder="Tìm theo tên giảng viên..."
                                           value="${param.teacherName}">
                                </div>
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
                    <h5 class="mb-0"><i class="fas fa-book me-2"></i>Danh sách môn học</h5>
                    <span class="badge bg-primary ms-2">${subjects != null ? subjects.size() : 0}</span>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-striped table-hover table-custom mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th class="text-center">#</th>
                                    <th>Tên môn</th>
                                    <th class="text-center">Mã</th>
                                    <th class="text-center">Tín chỉ</th>
                                    <th>Giảng viên</th>
                                    <th class="text-center">Trạng thái</th>
                                    <th class="text-center">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty subjects}">
                                        <c:forEach var="subject" items="${subjects}" varStatus="loop">
                                            <tr>
                                                <td class="text-center fw-bold">${loop.index + 1 + (currentPage - 1) * itemsPerPage}</td>
                                                <td>${subject.name}</td>
                                                <td class="text-center">${subject.code}</td>
                                                <td class="text-center">${subject.credits}</td>
                                                <td>${subject.teacherName}</td>
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${subject.isActive}">
                                                            <span class="badge bg-warning badge-fixed-width">Đang học</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-success badge-fixed-width">Đã hoàn thành</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <div class="btn-group-actions">
                                                        <a href="${pageContext.request.contextPath}/lessons?subjectId=${subject.id}"
                                                           class="btn btn-sm btn-outline-info"
                                                           title="Xem buổi học" data-bs-toggle="tooltip">
                                                            <i class="fas fa-eye"></i>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/subjects/edit?id=${subject.id}"
                                                           class="btn btn-sm btn-outline-warning"
                                                           title="Chỉnh sửa" data-bs-toggle="tooltip">
                                                            <i class="fas fa-edit"></i>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/subjects/delete-confirm?id=${subject.id}"
                                                           class="btn btn-sm btn-outline-danger"
                                                           title="Xóa" data-bs-toggle="tooltip">
                                                            <i class="fas fa-trash"></i>
                                                        </a>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="7" class="text-center py-5">
                                                <div class="d-flex flex-column align-items-center">
                                                    <i class="fas fa-folder-open fa-3x text-muted mb-3"></i>
                                                    <h5 class="text-muted mb-2">Không có dữ liệu</h5>
                                                    <p class="text-muted mb-0">Không tìm thấy môn học nào theo tiêu chí lọc.</p>
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

            <jsp:include page="../pagination/pagination.jsp"/>
        </div>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
        <script>
            // Khởi tạo tooltips
            var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
            var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
                return new bootstrap.Tooltip(tooltipTriggerEl)
            })
            
            // Tự động submit form khi chọn kỳ học từ dropdown trong bộ lọc
            document.getElementById('semesterId').addEventListener('change', function() {
                document.getElementById('filterForm').submit();
            });
        </script>
    </body>
</html>