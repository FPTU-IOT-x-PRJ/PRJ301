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
            
            /* Thêm CSS cho phần Lessons */
            .lesson-list {
                list-style: none;
                padding-left: 0;
                margin-bottom: 0;
                border-top: 1px solid var(--border-color); /* Đường kẻ phân cách với tiêu đề */
                padding-top: 10px;
            }
            .lesson-list li {
                background-color: var(--white-color); /* Nền trắng cho mỗi item */
                border: 1px solid var(--border-color); /* Viền nhẹ */
                border-radius: 6px; /* Bo góc nhẹ */
                margin-bottom: 8px; /* Khoảng cách giữa các item */
                padding: 10px 15px; /* Padding bên trong */
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05); /* Bóng đổ nhẹ nhàng */
                transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out; /* Hiệu ứng hover */
                display: flex; /* Dùng flexbox cho nội dung và nút actions */
                justify-content: space-between;
                align-items: center;
                flex-wrap: wrap; /* Cho phép xuống dòng trên màn hình nhỏ */
            }
            .lesson-list li:hover {
                transform: translateY(-2px); /* Nâng nhẹ khi hover */
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); /* Bóng đổ đậm hơn khi hover */
            }
            .lesson-list li:last-child {
                margin-bottom: 0; /* Bỏ margin ở item cuối cùng */
            }
            .lesson-item-details {
                flex-grow: 1; /* Chiếm hết không gian còn lại */
                margin-right: 10px; /* Khoảng cách với nút actions */
            }
            .lesson-item-details strong {
                color: var(--dark-color);
                font-size: 1.05em;
            }
            .lesson-item-details span {
                font-size: 0.9em;
                color: var(--secondary-color);
                margin-left: 5px;
            }
            .lesson-item-details .lesson-description,
            .lesson-item-details .lesson-status {
                display: block; /* Đưa mô tả và trạng thái xuống dòng mới */
                font-size: 0.85em;
                color: var(--text-muted);
                margin-top: 3px;
            }

            .lesson-toggle-button {
                cursor: pointer;
                border: none;
                background: none;
                padding: 0;
                margin: 0;
                font-size: 0.9em;
                color: var(--info-color); /* Màu xanh info */
                display: flex; /* Để icon và chữ cùng trên một dòng và căn giữa */
                align-items: center;
                justify-content: center; /* Căn giữa nội dung nút */
                width: 100%; /* Đảm bảo nút chiếm toàn bộ chiều rộng ô */
            }
            .lesson-toggle-button:hover {
                text-decoration: underline;
                color: var(--primary-color); /* Đậm hơn khi hover */
            }
            .collapse-content {
                margin-top: 5px;
                background-color: #f9f9f9; /* Nền nhạt cho nội dung collapse */
                border-radius: 4px;
                padding: 8px; /* Thêm padding cho nội dung */
            }
            .collapse-row td {
                padding-top: 0; /* Giảm padding trên để collapse liền mạch hơn */
                padding-bottom: 0;
                border-top: none; /* Bỏ border trên của hàng chứa collapse */
            }
            
            .lesson-cards-container {
                padding: 10px; /* Thêm padding cho container của các card */
            }

            .lesson-card {
                border: 1px solid var(--border-color);
                border-radius: 8px;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
                transition: box-shadow 0.3s ease, transform 0.3s ease;
                margin-bottom: 15px; /* Khoảng cách giữa các card */
                background-color: var(--white-color);
                display: flex; /* Dùng flexbox cho nội dung bên trong card */
                flex-direction: column;
                justify-content: space-between;
                height: 100%; /* Đảm bảo các card cùng hàng có chiều cao bằng nhau */
            }

            .lesson-card:hover {
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
                transform: translateY(-2px);
            }

            .lesson-card .card-body {
                padding: 15px;
                flex-grow: 1; /* Cho phép nội dung card chiếm hết không gian */
            }

            .lesson-card .card-title {
                font-size: 1.1em;
                font-weight: 600;
                color: var(--dark-color);
                margin-bottom: 5px;
            }

            .lesson-card .card-subtitle {
                font-size: 0.85em;
                color: var(--secondary-color);
                margin-bottom: 10px;
            }

            .lesson-card .lesson-description {
                font-size: 0.9em;
                color: var(--text-muted);
                margin-bottom: 10px;
                /* Optional: Limit lines for description */
                display: -webkit-box;
                -webkit-line-clamp: 2; /* Show up to 2 lines */
                -webkit-box-orient: vertical;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .lesson-card .lesson-status-badge {
                font-size: 0.8em;
                padding: 0.4em 0.8em;
                border-radius: 5px;
                font-weight: 600;
                display: inline-block; /* Để badge không chiếm hết chiều rộng */
                margin-top: auto; /* Đẩy trạng thái xuống dưới cùng nếu card có nhiều nội dung */
            }

            /* Colors for lesson statuses */
            .lesson-status-red {
                background-color: #dc3545; /* Đỏ cho Inactive */
                color: var(--white-color);
            }

            .lesson-status-white {
                background-color: #e9ecef; /* Trắng cho Active */
                color: var(--dark-color);
                border: 1px solid var(--border-color); /* Thêm border để dễ nhìn trên nền trắng */
            }

            .lesson-status-green {
                background-color: #d4edda; /* Xanh cho Completed */
                color: var(--white-color);
            }

            .lesson-card .card-footer {
                background-color: var(--light-color);
                border-top: 1px solid var(--border-color);
                padding: 10px 15px;
                display: flex;
                justify-content: flex-end; /* Nút actions ở bên phải */
                gap: 5px; /* Khoảng cách giữa các nút */
            }

            .card {
                border: 2px solid #dee2e6; /* Thay đổi từ 1px thành 2px */
                border-radius: 8px;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                transition: box-shadow 0.3s ease;
            }

            /* Responsive adjustments for lesson cards */
            @media (max-width: 992px) { /* On medium devices, make it 2 cards per row */
                .lesson-col {
                    flex: 0 0 auto;
                    width: 50%;
                }
            }

            @media (max-width: 576px) { /* On small devices, make it 1 card per row */
                .lesson-col {
                    flex: 0 0 auto;
                    width: 100%;
                }
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
                                    <th class="text-center">Buổi học</th>
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
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${not empty subjectWithLessonsDTO.lessons}">
                                                            <button class="lesson-toggle-button" type="button" 
                                                                    data-bs-toggle="collapse" 
                                                                    data-bs-target="#collapseLessons${subjectWithLessonsDTO.subject.id}" 
                                                                    aria-expanded="false" 
                                                                    aria-controls="collapseLessons${subjectWithLessonsDTO.subject.id}">
                                                                ${subjectWithLessonsDTO.lessons.size()} buổi <i class="fas fa-chevron-down ms-1"></i>
                                                            </button>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span>(Chưa có)</span>
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
                                            <%-- Hàng ẩn chứa danh sách lessons (sẽ hiện khi click nút) --%>
                                            <tr class="collapse-row">
                                                <td colspan="8"> <%-- Cần colspan bằng tổng số cột để chiếm toàn bộ chiều ngang --%>
                                                    <div class="collapse collapse-content" id="collapseLessons${subjectWithLessonsDTO.subject.id}">
                                                        <div class="d-flex justify-content-end mb-2">
                                                            <a href="${pageContext.request.contextPath}/lessons/add?subjectId=${subjectWithLessonsDTO.subject.id}" class="btn btn-sm btn-success">
                                                                <i class="fas fa-plus me-1"></i> Thêm buổi học
                                                            </a>
                                                        </div>
                                                        <c:choose>
                                                            <c:when test="${not empty subjectWithLessonsDTO.lessons}">
                                                                <div class="lesson-cards-container">
                                                                    <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-3"> <%-- 3 cards per row on large, 2 on medium, 1 on small --%>
                                                                        <c:forEach var="lesson" items="${subjectWithLessonsDTO.lessons}">
                                                                            <div class="col lesson-col"> <%-- Add lesson-col for responsive adjustments --%>
                                                                                <div class="card lesson-card">
                                                                                    <div class="card-body">
                                                                                        <h6 class="card-title">${lesson.name}</h6>
                                                                                        <h6 class="card-subtitle mb-2 text-muted">
                                                                                            <i class="far fa-calendar-alt me-1"></i>
                                                                                            <fmt:formatDate value="${lesson.lessonDate}" pattern="dd/MM/yyyy"/>
                                                                                        </h6>
                                                                                        <c:if test="${not empty lesson.description}">
                                                                                            <p class="card-text lesson-description">${lesson.description}</p>
                                                                                        </c:if>
                                                                                            <c:set var="statusClass">
                                                                                                <c:choose>
                                                                                                    <c:when test="${lesson.status eq 'Inactive'}">lesson-status-red</c:when>
                                                                                                    <c:when test="${lesson.status eq 'Active'}">lesson-status-white</c:when>
                                                                                                    <c:when test="${lesson.status eq 'Completed'}">lesson-status-green</c:when>
                                                                                                    <c:otherwise>text-muted border</c:otherwise>
                                                                                                </c:choose>
                                                                                            </c:set>

                                                                                            <span class="lesson-status-badge ${statusClass}">
                                                                                                <c:choose>
                                                                                                    <c:when test="${lesson.status eq 'Inactive'}"><i class="fas fa-times-circle me-1"></i>Vắng</c:when>
                                                                                                    <c:when test="${lesson.status eq 'Active'}"><i class="fas fa-hourglass-half me-1"></i>Chưa học</c:when>
                                                                                                    <c:when test="${lesson.status eq 'Completed'}"><i class="fas fa-check-circle me-1"></i>Hoàn thành</c:when>
                                                                                                    <c:otherwise>${lesson.status}</c:otherwise>
                                                                                                </c:choose>
                                                                                            </span>

                                                                                    </div>
                                                                                    <div class="card-footer">
                                                                                        <a href="${pageContext.request.contextPath}/lessons/detail?id=${lesson.id}"
                                                                                            class="btn btn-sm btn-outline-info"
                                                                                            title="Xem chi tiết" data-bs-toggle="tooltip">
                                                                                            <i class="fas fa-eye"></i>
                                                                                        </a>
                                                                                        <a href="${pageContext.request.contextPath}/lessons/edit?id=${lesson.id}"
                                                                                           class="btn btn-sm btn-outline-warning" title="Chỉnh sửa buổi học" data-bs-toggle="tooltip">
                                                                                            <i class="fas fa-edit"></i>
                                                                                        </a>
                                                                                        <a href="${pageContext.request.contextPath}/lessons/delete-confirm?id=${lesson.id}"
                                                                                           class="btn btn-sm btn-outline-danger" title="Xóa buổi học" data-bs-toggle="tooltip">
                                                                                            <i class="fas fa-trash"></i>
                                                                                        </a>
                                                                                    </div>
                                                                                </div>
                                                                            </div>
                                                                        </c:forEach>
                                                                    </div>
                                                                </div>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <p class="text-muted text-center">Chưa có buổi học nào cho môn này.</p>
                                                            </c:otherwise>
                                                        </c:choose>
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

            // Xử lý mũi tên xoay khi collapse mở/đóng
            document.querySelectorAll('.lesson-toggle-button').forEach(button => {
                const icon = button.querySelector('i'); // Lấy icon bên trong nút
                const targetId = button.getAttribute('data-bs-target');
                const collapseElement = document.querySelector(targetId);

                // Cập nhật icon ban đầu dựa trên trạng thái collapse
                if (collapseElement.classList.contains('show')) {
                    icon.classList.remove('fa-chevron-down');
                    icon.classList.add('fa-chevron-up');
                } else {
                    icon.classList.remove('fa-chevron-up');
                    icon.classList.add('fa-chevron-down');
                }

                button.addEventListener('click', function() {
                    // Toggle class for rotation
                    if (icon.classList.contains('fa-chevron-down')) {
                        icon.classList.remove('fa-chevron-down');
                        icon.classList.add('fa-chevron-up');
                    } else {
                        icon.classList.remove('fa-chevron-up');
                        icon.classList.add('fa-chevron-down');
                    }
                });
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