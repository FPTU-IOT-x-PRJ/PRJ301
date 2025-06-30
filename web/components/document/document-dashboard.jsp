<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Dashboard Tài liệu</title>
        <!-- Bootstrap CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <!-- Font Awesome for icons -->
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
        <style>
            body {
                font-family: 'Inter', sans-serif;
                background-color: #f8f9fa; /* Light background */
            }
            .container {
                padding-top: 2rem;
                padding-bottom: 2rem;
            }
            .card {
                border-radius: 0.75rem;
                box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.05);
            }
            .table-responsive {
                margin-top: 1.5rem;
            }
            .table th, .table td {
                vertical-align: middle;
            }
            .table thead th {
                background-color: #e9ecef; /* Light grey header */
                color: #495057;
                font-weight: 600;
                text-transform: uppercase;
                font-size: 0.85rem;
            }
            .table-hover tbody tr:hover {
                background-color: #f2f2f2;
            }
            .btn-action {
                margin-right: 0.5rem;
            }
            .alert-custom {
                padding: 1rem 1.5rem;
                margin-bottom: 1.5rem;
                border-radius: 0.5rem;
                font-size: 0.95rem;
            }
            .alert-success-custom {
                background-color: #d4edda;
                color: #155724;
                border-color: #c3e6cb;
            }
            .alert-danger-custom {
                background-color: #f8d7da;
                color: #721c24;
                border-color: #f5c6cb;
            }
            /* Modal fade in animation */
            .modal.fade .modal-dialog {
                transition: transform 0.3s ease-out;
                transform: translate(0, -50px);
            }
            .modal.show .modal-dialog {
                transform: translate(0, 0);
            }
        </style>
    </head>
    <body>
        <div class="container">
            <h1 class="mb-4 text-center text-primary">Quản lý Tài liệu</h1>

            <%-- Hiển thị thông báo --%>
            <c:if test="${not empty param.message}">
                <div class="alert alert-custom 
                     <c:if test="${param.message eq 'uploadSuccess' || param.message eq 'updateSuccess' || param.message eq 'deleteSuccess'}">alert-success-custom</c:if>
                     <c:if test="${not empty requestScope.errorMessage || param.message eq 'error'}">alert-danger-custom</c:if>">
                    <c:if test="${param.message eq 'uploadSuccess'}">
                        <i class="fas fa-check-circle me-2"></i>Tải tài liệu lên thành công!
                    </c:if>
                    <c:if test="${param.message eq 'updateSuccess'}">
                        <i class="fas fa-check-circle me-2"></i>Cập nhật tài liệu thành công!
                    </c:if>
                    <c:if test="${param.message eq 'deleteSuccess'}">
                        <i class="fas fa-check-circle me-2"></i>Xóa tài liệu thành công!
                    </c:if>
                    <c:if test="${not empty requestScope.errorMessage || param.message eq 'error'}">
                        <i class="fas fa-exclamation-triangle me-2"></i><c:out value="${requestScope.errorMessage != null ? requestScope.errorMessage : 'Đã xảy ra lỗi.'}"/>
                    </c:if>
                </div>
            </c:if>

            <%-- Form Lọc Tài liệu --%>
            <div class="card mb-4">
                <div class="card-header bg-light">
                    <h2 class="card-title h5 mb-0">Lọc Tài liệu</h2>
                </div>
                <div class="card-body">
                    <form id="filterForm" action="${pageContext.request.contextPath}/documents/display" method="GET">
                        <div class="row g-3">
                            <div class="col-md-6 col-lg-4">
                                <label for="subjectId" class="form-label">Môn học:</label>
                                <select id="subjectId" name="subjectId" class="form-select">
                                    <option value="">Tất cả môn học</option>
                                    <c:forEach var="subject" items="${subjects}">
                                        <option value="${subject.id}" ${subject.id == selectedSubjectId ? 'selected' : ''}>
                                            ${subject.name} (${subject.code})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6 col-lg-4">
                                <label for="lessonId" class="form-label">Buổi học:</label>
                                <select id="lessonId" name="lessonId" class="form-select">
                                    <option value="">Tất cả buổi học</option>
                                    <c:if test="${not empty lessonsOfSelectedSubject}">
                                        <c:forEach var="lesson" items="${lessonsOfSelectedSubject}">
                                            <option value="${lesson.id}" ${lesson.id == selectedLessonId ? 'selected' : ''}>
                                                ${lesson.name}
                                            </option>
                                        </c:forEach>
                                    </c:if>
                                </select>
                            </div>
                            <div class="col-12 col-lg-4 d-flex align-items-end">
                                <button type="submit" class="btn btn-primary me-2">
                                    <i class="fas fa-filter me-2"></i>Lọc
                                </button>
                                <a href="${pageContext.request.contextPath}/documents/display" class="btn btn-secondary">
                                    <i class="fas fa-redo-alt me-2"></i>Đặt lại
                                </a>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            <div class="d-flex justify-content-end mb-3">
                <a href="${pageContext.request.contextPath}/documents/add" class="btn btn-success">
                    <i class="fas fa-plus me-2"></i>Thêm Tài liệu mới
                </a>
            </div>

            <div class="card">
                <div class="card-body">
                    <div class="table-responsive">
                        <c:choose>
                            <c:when test="${not empty listDocuments}">
                                <table class="table table-hover table-striped caption-top">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Tên File</th>
                                            <th>Mô tả</th>
                                            <th>Môn học</th>
                                            <th>Buổi học</th>
                                            <th>Kích thước</th>
                                            <th>Ngày tải lên</th>
                                            <th>Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="document" items="${listDocuments}">
                                            <tr>
                                                <td><c:out value="${document.id}"/></td>
                                                <td>
                                                    <a href="${document.filePath}" target="_blank" class="text-primary text-decoration-none">
                                                        <i class="fas fa-file-alt me-1"></i> <c:out value="${document.fileName}"/>
                                                    </a>
                                                </td>
                                                <td><c:out value="${document.description != null && !document.description.isEmpty() ? document.description : 'Không có mô tả'}"/></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${document.subjectId != null}">
                                                            <c:out value="${subjectNames[document.subjectId]}"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted fst-italic">N/A</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${document.lessonId != null}">
                                                            <c:out value="${lessonNames[document.lessonId]}"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted fst-italic">N/A</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td><fmt:formatNumber value="${document.fileSize / 1024}" pattern="#,##0.00"/> KB</td>
                                                <td><fmt:formatDate value="${document.uploadDate}" pattern="dd-MM-yyyy HH:mm"/></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/documents/detail?id=${document.id}" class="btn btn-info btn-sm btn-action" title="Xem chi tiết">
                                                        <i class="fas fa-eye"></i>
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/documents/edit?id=${document.id}" class="btn btn-warning btn-sm btn-action" title="Chỉnh sửa">
                                                        <i class="fas fa-edit"></i>
                                                    </a>
                                                    <button type="button" onclick="confirmDelete(${document.id})" class="btn btn-danger btn-sm btn-action" title="Xóa">
                                                        <i class="fas fa-trash-alt"></i>
                                                    </button>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:when>
                            <c:otherwise>
                                <div class="alert alert-info text-center" role="alert">
                                    <i class="fas fa-info-circle me-2"></i>Chưa có tài liệu nào được tải lên hoặc không tìm thấy tài liệu phù hợp.
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>

        <%-- Confirmation Modal --%>
        <div class="modal fade" id="confirmationModal" tabindex="-1" aria-labelledby="confirmationModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="confirmationModalLabel">Xác nhận xóa tài liệu</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        Bạn có chắc chắn muốn xóa tài liệu này không? Hành động này không thể hoàn tác.
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="button" id="confirmDeleteBtn" class="btn btn-danger">Xóa</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Bootstrap Bundle with Popper -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            let documentIdToDelete = null;
            const confirmationModal = new bootstrap.Modal(document.getElementById('confirmationModal'));
            const confirmDeleteButton = document.getElementById('confirmDeleteBtn');

            function confirmDelete(id) {
                documentIdToDelete = id;
                confirmationModal.show();
            }

            confirmDeleteButton.onclick = function() {
                if (documentIdToDelete !== null) {
                    window.location.href = "${pageContext.request.contextPath}/documents/delete?id=" + documentIdToDelete;
                }
            };
            
            // JavaScript để load lessons động khi chọn subject
            document.getElementById('subjectId').addEventListener('change', function() {
                const subjectId = this.value;
                const lessonSelect = document.getElementById('lessonId');
                
                // Clear existing lessons
                lessonSelect.innerHTML = '<option value="">Tất cả buổi học</option>';

                if (subjectId) {
                    fetch('${pageContext.request.contextPath}/documents/getLessonsBySubject?subjectId=' + subjectId)
                        .then(response => response.json())
                        .then(data => {
                            data.forEach(lesson => {
                                const option = document.createElement('option');
                                option.value = lesson.id;
                                option.textContent = lesson.name;
                                lessonSelect.appendChild(option);
                            });
                            // Re-select the previously selected lesson if any
                            const selectedLessonId = ${selectedLessonId != null ? selectedLessonId : 'null'};
                            if (selectedLessonId && lessonSelect.querySelector(`option[value="${selectedLessonId}"]`)) {
                                lessonSelect.value = selectedLessonId;
                            }
                        })
                        .catch(error => console.error('Error fetching lessons:', error));
                }
            });

            // Trigger change event on page load if a subject is already selected
            document.addEventListener('DOMContentLoaded', function() {
                const selectedSubjectId = ${selectedSubjectId != null ? selectedSubjectId : 'null'};
                if (selectedSubjectId) {
                    document.getElementById('subjectId').dispatchEvent(new Event('change'));
                }
            });
        </script>
    </body>
</html>
