<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quản lý Môn học - EduPlan</title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">

        <style>
            /* Các style đã có */
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
                width: 15%; /* Điều chỉnh cho tên môn */
                font-weight: 500;
            }

            .table-custom th:nth-child(3),
            .table-custom td:nth-child(3) {
                width: 8%; /* Mã môn */
                text-align: center;
            }

            .table-custom th:nth-child(4),
            .table-custom td:nth-child(4) {
                width: 7%; /* Tín chỉ */
                text-align: center;
            }

            .table-custom th:nth-child(5),
            .table-custom td:nth-child(5) {
                width: 15%; /* Giảng viên */
            }

            .table-custom th:nth-child(6),
            .table-custom td:nth-child(6) {
                width: 10%; /* Trạng thái */
                text-align: center;
            }

            /* Thêm CSS cho cột "Buổi học" và "Hành động" */
            .table-custom th:nth-child(7),
            .table-custom td:nth-child(7) {
                width: 20%; /* Buổi học - Thay đổi từ cũ là "Hành động" */
            }
            .table-custom th:nth-child(8),
            .table-custom td:nth-child(8) {
                width: 20%; /* Hành động - Cột mới */
                text-align: center;
            }
            /* End Thêm CSS */

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
                            <div class="col-md-3">
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-search"></i></span>
                                    <input type="text" class="form-control" name="search" placeholder="Tìm theo tên môn học hoặc mã..."
                                           value="${param.search != null ? param.search : ''}">
                                </div>
                            </div>

                            <div class="col-md-3">
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-calendar-alt"></i></span>
                                    <select name="semesterId" id="semesterFilterSelect" class="form-select">
                                        <c:forEach var="s" items="${allSemesters}">
                                            <option value="${s.id}" ${s.id == currentSemester.id ? 'selected' : ''}>${s.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>

                            <div class="col-md-3">
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-user-tie"></i></span>
                                    <input type="text" class="form-control" name="teacherName" placeholder="Tìm theo tên giảng viên..."
                                           value="${param.teacherName != null ? param.teacherName : ''}">
                                </div>
                            </div>

                            <div class="col-3 d-grid">
                                <button type="submit" class="btn btn-primary" title="Áp dụng bộ lọc">
                                    <i class="fas fa-filter me-2"></i>Lọc
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>


            <div class="card shadow-sm">
                <div class="card-header bg-light d-flex align-items-center">
                    <h5 class="mb-0"><i class="fas fa-book me-2"></i>Danh sách môn học</h5>
                    <span class="badge bg-primary ms-2">${totalSubjects}</span>
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
                                        <c:forEach var="subjectWithLessonsDTO" items="${subjects}" varStatus="loop">
                                            <%-- Hàng hiển thị thông tin môn học --%>
                                            <tr>
                                                <td class="text-center fw-bold">${loop.index + 1 + (currentPage - 1) * pageSize}</td>
                                                <td>${subjectWithLessonsDTO.subject.name}</td>
                                                <td class="text-center">${subjectWithLessonsDTO.subject.code}</td>
                                                <td class="text-center">${subjectWithLessonsDTO.subject.credits}</td>
                                                <td>${subjectWithLessonsDTO.subject.teacherName}</td>
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${subjectWithLessonsDTO.subject.isActive}">
                                                            <span class="badge bg-warning badge-fixed-width">Đang học</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-success badge-fixed-width">Đã hoàn thành</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <div class="btn-group-actions">
                                                        <a href="${pageContext.request.contextPath}/subjects/detail?id=${subjectWithLessonsDTO.subject.id}"
                                                           class="btn btn-sm btn-outline-info"
                                                           title="Xem chi tiết" data-bs-toggle="tooltip">
                                                            <i class="fas fa-eye"></i>
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
            });

            // Tự động submit form khi chọn kỳ học từ dropdown trong bộ lọc
            document.getElementById('semesterFilterSelect').addEventListener('change', function() {
                // Đặt giá trị page về 1 khi thay đổi kỳ học để bắt đầu lại từ trang đầu tiên
                document.querySelector('#filterForm input[name="page"]').value = 1;
                document.getElementById('filterForm').submit();
            });
            
            // Giữ trạng thái của các collapse khi phân trang hoặc lọc
            document.addEventListener('DOMContentLoaded', function() {
                // Lấy trạng thái collapse từ session storage
                const collapsedStates = JSON.parse(sessionStorage.getItem('subjectCollapseStates')) || {};

                // Áp dụng trạng thái cho các collapse
                for (const id in collapsedStates) {
                    const collapseElement = document.getElementById(id);
                    if (collapseElement) {
                        const bsCollapse = new bootstrap.Collapse(collapseElement, { toggle: false });
                        if (collapsedStates[id]) {
                            bsCollapse.show();
                            // Cập nhật icon mũi tên
                            const button = document.querySelector(`[data-bs-target="#${id}"] i`);
                            if (button) {
                                button.classList.remove('fa-chevron-down');
                                button.classList.add('fa-chevron-up');
                            }
                        } else {
                            bsCollapse.hide();
                            // Cập nhật icon mũi tên
                            const button = document.querySelector(`[data-bs-target="#${id}"] i`);
                            if (button) {
                                button.classList.remove('fa-chevron-up');
                                button.classList.add('fa-chevron-down');
                            }
                        }
                    }
                }

                // Lưu trạng thái collapse khi chúng thay đổi
                document.querySelectorAll('.collapse').forEach(collapseEl => {
                    collapseEl.addEventListener('show.bs.collapse', function () {
                        collapsedStates[this.id] = true;
                        sessionStorage.setItem('subjectCollapseStates', JSON.stringify(collapsedStates));
                    });
                    collapseEl.addEventListener('hide.bs.collapse', function () {
                        collapsedStates[this.id] = false;
                        sessionStorage.setItem('subjectCollapseStates', JSON.stringify(collapsedStates));
                    });
                });
            });
        </script>
    </body>
</html>