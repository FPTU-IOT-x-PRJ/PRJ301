<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quản lý Buổi học - EduPlan</title>
        <%-- Link tới common.css nếu có --%>
        <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
        <%-- Bootstrap 5.3.2 từ CDN (đã cập nhật) --%>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <%-- Font Awesome 6.4.0 từ CDN (đã cập nhật) --%>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">

        <style>
            /* Custom styles from subject-dashboard.jsp */
            .table-custom {
                font-size: 14px;
            }

            .table-custom th,
            .table-custom td {
                vertical-align: middle;
                padding: 12px 8px;
            }

            /* Căn chỉnh độ rộng cột cho Lessons */
            .table-custom th:nth-child(1),
            .table-custom td:nth-child(1) {
                width: 5%;
                text-align: center;
            }

            .table-custom th:nth-child(2),
            .table-custom td:nth-child(2) {
                width: 20%; /* Tên Buổi Học */
                font-weight: 500;
            }
            
            .table-custom th:nth-child(3),
            .table-custom td:nth-child(3) {
                width: 10%; /* Ngày Học */
                text-align: center;
            }
            
            .table-custom th:nth-child(4),
            .table-custom td:nth-child(4) {
                width: 25%; /* Mô Tả (đã tăng độ rộng) */
            }

            .table-custom th:nth-child(5),
            .table-custom td:nth-child(5) {
                width: 10%; /* Trạng thái */
                text-align: center;
            }
            
            .table-custom th:nth-child(6),
            .table-custom td:nth-child(6),
            .table-custom th:nth-child(7),
            .table-custom td:nth-child(7) {
                width: 10%; /* Tạo lúc, Cập Nhật Lúc */
                font-size: 12px; /* Font nhỏ hơn cho cột ngày */
            }

            .table-custom th:nth-child(8),
            .table-custom td:nth-child(8) {
                width: 10%; /* Hành động */
                text-align: center;
            }

            /* Style cho badge */
            .badge {
                font-size: 0.75em;
                padding: 0.5em 0.75em;
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
        </style>
    </head>
    <body>
        <%-- Bao gồm thanh điều hướng --%>
        <jsp:include page="../navigation/navigation.jsp" />

        <div class="container-fluid py-4">
            <div class="row mb-4">
                <div class="col-12 d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="mb-1">Buổi học của môn: <span class="text-primary">${requestScope.subject.name} (${requestScope.subject.code})</span></h2>
                        <p class="text-muted mb-0">Danh sách các buổi học cho môn học này</p>
                    </div>
                    <div class="d-flex align-items-center">
                        <a href="${pageContext.request.contextPath}/lessons/add?subjectId=${requestScope.subject.id}" class="btn btn-primary me-2" title="Thêm Buổi Học Mới" data-bs-toggle="tooltip">
                            <i class="fas fa-plus me-2"></i>Thêm buổi học
                        </a>
                    </div>
                </div>
            </div>

            <%-- Alert Messages --%>
            <c:if test="${not empty requestScope.errorMessage}">
                <div class="alert alert-danger shadow-sm" role="alert">
                    ${requestScope.errorMessage}
                </div>
            </c:if>
            <c:if test="${param.message == 'addSuccess'}">
                <div class="alert alert-success shadow-sm" role="alert">
                    Thêm buổi học thành công!
                </div>
            </c:if>
            <c:if test="${param.message == 'editSuccess'}">
                <div class="alert alert-success shadow-sm" role="alert">
                    Cập nhật buổi học thành công!
                </div>
            </c:if>
            <c:if test="${param.message == 'deleteSuccess'}">
                <div class="alert alert-success shadow-sm" role="alert">
                    Xóa buổi học thành công!
                </div>
            </c:if>

            <%-- Search & Filter Card --%>
            <div class="card mb-4 shadow-sm">
                <div class="card-body">
                    <form class="row g-3" action="${pageContext.request.contextPath}/lessons" method="get" id="filterForm">
                        <input type="hidden" name="subjectId" value="${requestScope.subject.id}">
                        <input type="hidden" name="page" value="${requestScope.currentPage}" />
                        <div class="col-md-5">
                            <div class="input-group">
                                <span class="input-group-text"><i class="fas fa-search"></i></span>
                                <input type="text" class="form-control" name="search" placeholder="Tìm kiếm theo tên hoặc mô tả" value="${requestScope.search != null ? requestScope.search : ''}">
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="input-group">
                                <span class="input-group-text"><i class="fas fa-tasks"></i></span>
                                <select class="form-select" name="status" id="statusFilter">
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="Active" ${requestScope.statusFilter == 'Active' ? 'selected' : ''}>Chưa học</option>
                                    <option value="Inactive" ${requestScope.statusFilter == 'Inactive' ? 'selected' : ''}>Vắng</option>
                                    <option value="Completed" ${requestScope.statusFilter == 'Completed' ? 'selected' : ''}>Hoàn thành</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-3 d-grid">
                            <button type="submit" class="btn btn-primary" title="Áp dụng bộ lọc">
                                <i class="fas fa-filter me-2"></i>Lọc
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <%-- Lessons Table Card --%>
            <div class="card shadow-sm">
                <div class="card-header bg-light d-flex align-items-center">
                    <h5 class="mb-0"><i class="fas fa-chalkboard-teacher me-2"></i>Danh sách buổi học</h5>
                    <span class="badge bg-primary ms-2">${requestScope.lessons != null ? requestScope.lessons.size() : 0}</span>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-striped table-hover table-custom mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th class="text-center">#</th>
                                    <th>Tên Buổi Học</th>
                                    <th class="text-center">Ngày Học</th>
                                    <th>Mô Tả</th>
                                    <th class="text-center">Trạng Thái</th>
                                    <th class="text-center">Hành Động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty requestScope.lessons}">
                                        <c:forEach var="lesson" items="${requestScope.lessons}" varStatus="loop">
                                            <tr>
                                                <td class="text-center fw-bold">${loop.index + 1 + (requestScope.currentPage - 1) * requestScope.itemsPerPage}</td>
                                                <td>${lesson.name}</td>
                                                <td class="text-center">
                                                    <fmt:formatDate value="${lesson.lessonDate}" pattern="dd/MM/yyyy"/>
                                                </td>
                                                <td>${lesson.description}</td>
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${lesson.status == 'Active'}">
                                                            <span class="badge bg-secondary badge-fixed-width">Chưa học</span>
                                                        </c:when>
                                                        <c:when test="${lesson.status == 'Inactive'}">
                                                            <span class="badge bg-danger badge-fixed-width">Vắng</span>
                                                        </c:when>
                                                        <c:when test="${lesson.status == 'Completed'}">
                                                            <span class="badge bg-success badge-fixed-width">Hoàn thành</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-secondary badge-fixed-width">${lesson.status}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <div class="btn-group-actions">
                                                        <%-- Không có trang chi tiết cho lesson trong ví dụ này, có thể thêm sau nếu cần --%>
                                                        <a href="${pageContext.request.contextPath}/lessons/detail?id=${lesson.id}"
                                                             class="btn btn-sm btn-outline-info"
                                                             title="Xem chi tiết" data-bs-toggle="tooltip">
                                                             <i class="fas fa-eye"></i>
                                                         </a>
                                                        <a href="${pageContext.request.contextPath}/lessons/edit?id=${lesson.id}"
                                                             class="btn btn-sm btn-outline-warning"
                                                             title="Chỉnh sửa" data-bs-toggle="tooltip">
                                                             <i class="fas fa-edit"></i>
                                                         </a>
                                                        <a href="${pageContext.request.contextPath}/lessons/delete-confirm?id=${lesson.id}" class="btn btn-sm btn-outline-danger"
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
                                            <td colspan="8" class="text-center py-5">
                                                <div class="d-flex flex-column align-items-center">
                                                    <i class="fas fa-folder-open fa-3x text-muted mb-3"></i>
                                                    <h5 class="text-muted mb-2">Không có dữ liệu</h5>
                                                    <p class="text-muted mb-0">Không tìm thấy buổi học nào theo tiêu chí lọc.</p>
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

            <%-- Pagination --%>
            <%-- Giả định pagination.jsp có thể xử lý các tham số `subjectId`, `search`, `status` một cách linh hoạt --%>
            <jsp:include page="../pagination/pagination.jsp">
                   <jsp:param name="baseUrl" value="${pageContext.request.contextPath}/lessons"/>
                   <jsp:param name="param1Name" value="subjectId"/>
                   <jsp:param name="param1Value" value="${requestScope.subject.id}"/>
                   <jsp:param name="param2Name" value="search"/>
                   <jsp:param name="param2Value" value="${requestScope.search}"/>
                   <jsp:param name="param3Name" value="status"/>
                   <jsp:param name="param3Value" value="${requestScope.statusFilter}"/>
            </jsp:include>
        </div>

        <%-- Bootstrap Bundle with Popper --%>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
        
        <script>
            // Khởi tạo tooltips
            var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
            var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
                return new bootstrap.Tooltip(tooltipTriggerEl)
            })
            
            // Tự động submit form khi chọn trạng thái từ dropdown trong bộ lọc
            document.getElementById('statusFilter').addEventListener('change', function() {
                document.getElementById('filterForm').submit();
            });

            // Hàm xác nhận xóa buổi học
            function confirmDelete(lessonId, subjectId, lessonName) {
                if (confirm(`Bạn có chắc chắn muốn xóa buổi học "${lessonName}" không?`)) {
                    window.location.href = "${pageContext.request.contextPath}/lessons/delete?id=" + lessonId + "&subjectId=" + subjectId;
                }
            }
        </script>
    </body>
</html>