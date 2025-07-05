<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${lesson.name} - Chi tiết Buổi học</title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
        <style>
            body {
                font-family: 'Inter', sans-serif;
                background-color: #f8f9fa;
            }
            .container-fluid {
                padding-top: 20px;
                padding-bottom: 20px;
            }
            .card {
                border-radius: 15px;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
                margin-bottom: 20px;
            }
            .card-header {
                background-color: #007bff;
                color: white;
                border-top-left-radius: 15px;
                border-top-right-radius: 15px;
                padding: 15px 20px;
                font-size: 1.25rem;
                font-weight: bold;
            }
            .card-body {
                padding: 25px;
            }
            .list-group-item {
                border: none;
                padding: 8px 0;
            }
            .btn-primary {
                background-color: #007bff;
                border-color: #007bff;
                border-radius: 8px;
            }
            .btn-primary:hover {
                background-color: #0056b3;
                border-color: #0056b3;
            }
            .btn-outline-secondary {
                border-radius: 8px;
            }
            .document-item {
                border: 1px solid #e9ecef;
                border-radius: 10px;
                padding: 15px;
                margin-bottom: 10px;
                background-color: #ffffff;
                transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
            }
            .document-item:hover {
                transform: translateY(-3px);
                box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
            }
            .document-item .doc-title {
                font-weight: 500;
                color: #007bff;
            }
            .document-item .doc-date {
                font-size: 0.85rem;
                color: #6c757d;
            }
            .badge-status {
                font-size: 0.8em;
                padding: 0.4em 0.6em;
                border-radius: 0.5rem;
            }
            .badge-planned { background-color: #ffc107; color: #343a40; } /* Yellow */
            .badge-completed { background-color: #28a745; color: white; } /* Green */
            .badge-cancelled { background-color: #dc3545; color: white; } /* Red */
        </style>
    </head>
    <body>
        <jsp:include page="../navigation/navigation.jsp"/>

        <div class="container-fluid">
            <div class="row mb-4">
                <div class="col-12">
                    <a href="${pageContext.request.contextPath}/subjects/detail?id=${subject.id}" class="btn btn-outline-secondary rounded-pill">
                        <i class="fas fa-arrow-left me-2"></i>Quay lại môn học: ${subject.name}
                    </a>
                </div>
            </div>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <c:forEach var="entry" items="${errorMessage}">
                        <strong>${entry.key}:</strong> ${entry.value}<br>
                    </c:forEach>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <c:if test="${not empty successMessage}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    ${successMessage}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header">
                            <i class="fas fa-calendar-alt me-2"></i>Thông tin Buổi học: ${lesson.name}
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <p><strong>Môn học:</strong> ${subject.name}</p>
                                    <p><strong>Ngày học:</strong> <fmt:formatDate value="${lesson.lessonDate}" pattern="dd/MM/yyyy"/></p>
                                    <p><strong>Mô tả:</strong> ${lesson.description}</p>
                                </div>
                                <div class="col-md-6">
                                    <p><strong>Trạng thái:</strong>
                                        <c:choose>
                                            <c:when test="${lesson.status eq 'Planned'}">
                                                <span class="badge badge-planned">Đã lên kế hoạch</span>
                                            </c:when>
                                            <c:when test="${lesson.status eq 'Completed'}">
                                                <span class="badge badge-completed">Đã hoàn thành</span>
                                            </c:when>
                                            <c:when test="${lesson.status eq 'Cancelled'}">
                                                <span class="badge badge-cancelled">Đã hủy</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">${lesson.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </div>
                            <div class="d-flex justify-content-end mt-3">
                                <a href="${pageContext.request.contextPath}/lessons/edit?id=${lesson.id}" class="btn btn-outline-primary me-2 rounded-pill">
                                    <i class="fas fa-edit me-2"></i>Sửa buổi học
                                </a>
                                <a href="${pageContext.request.contextPath}/lessons/delete-confirm?id=${lesson.id}&subjectId=${lesson.subjectId}" class="btn btn-outline-danger rounded-pill">
                                    <i class="fas fa-trash-alt me-2"></i>Xóa buổi học
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <i class="fas fa-file-alt me-2"></i>Tài liệu liên quan
                            <button type="button" class="btn btn-light btn-sm rounded-pill" data-bs-toggle="modal" data-bs-target="#uploadDocumentModal">
                                <i class="fas fa-upload me-2"></i>Tải lên tài liệu mới
                            </button>
                        </div>
                        <div class="card-body">
                            <c:if test="${empty documents}">
                                <div class="alert alert-info text-center" role="alert">
                                    Chưa có tài liệu nào cho buổi học này.
                                </div>
                            </c:if>
                            <div class="row">
                                <c:forEach var="document" items="${documents}">
                                    <div class="col-md-6 col-lg-4 mb-3">
                                        <div class="document-item d-flex align-items-center justify-content-between">
                                            <div>
                                                <h6 class="doc-title mb-1">
                                                    <i class="fas fa-file-alt me-2"></i>
                                                    <a href="${pageContext.request.contextPath}/documents/download?id=${document.id}" target="_blank" class="text-decoration-none">${document.fileName}</a>
                                                </h6>
                                                <c:if test="${not empty document.description}">
                                                    <p class="text-muted mb-0" style="font-size: 0.85rem;">${document.description}</p>
                                                </c:if>
                                            </div>
                                            <div>
                                                <a href="${pageContext.request.contextPath}/documents/delete?id=${document.id}&subjectId=${lesson.subjectId}&lessonId=${lesson.id}" class="btn btn-outline-danger btn-sm">
                                                    <i class="fas fa-trash-alt"></i>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <i class="fas fa-clipboard me-2"></i>Ghi chú cá nhân
                            <button type="button" class="btn btn-light btn-sm rounded-pill" data-bs-toggle="modal" data-bs-target="#addNoteModal">
                                <i class="fas fa-plus-circle me-2"></i>Thêm ghi chú mới
                            </button>
                        </div>
                        <div class="card-body">
                            <%-- Nhúng partial hiển thị danh sách ghi chú --%>
                            <jsp:include page="/components/note/note-list-partial.jsp"/>
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <div class="modal fade" id="uploadDocumentModal" tabindex="-1" aria-labelledby="uploadDocumentModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="uploadDocumentModalLabel">Tải lên Tài liệu Mới</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form id="uploadDocumentForm" action="${pageContext.request.contextPath}/documents/add" method="post" enctype="multipart/form-data">
                        <div class="modal-body">
                            <input type="hidden" name="subjectId" value="${lesson.subjectId}">
                            <input type="hidden" name="lessonId" value="${lesson.id}">
                            <div class="mb-3">
                                <label for="file" class="form-label">Chọn tệp tài liệu:</label>
                                <input class="form-control" id="file" type="file" name="file" required>
                            </div>
                            <div class="mb-3">
                                <label for="description" class="form-label">Mô tả (tùy chọn):</label>
                                <textarea class="form-control" id="description" name="description" rows="3"></textarea>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary rounded-pill" data-bs-dismiss="modal">Hủy</button>
                            <button type="submit" class="btn btn-primary rounded-pill">Tải lên</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <%-- Modal Thêm Ghi chú Mới (MỚI) --%>
        <div class="modal fade" id="addNoteModal" tabindex="-1" aria-labelledby="addNoteModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="addNoteModalLabel">Thêm Ghi chú Mới</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form id="addNoteForm" action="${pageContext.request.contextPath}/notes/add" method="post">
                        <div class="modal-body">
                            <input type="hidden" name="lessonId" value="${lesson.id}">
                            <div class="mb-3">
                                <label for="noteTitle" class="form-label">Tiêu đề ghi chú:</label>
                                <input type="text" class="form-control" id="noteTitle" name="title" required maxlength="255">
                            </div>
                            <div class="mb-3">
                                <label for="noteContent" class="form-label">Nội dung ghi chú:</label>
                                <textarea class="form-control" id="noteContent" name="content" rows="5" required></textarea>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary rounded-pill" data-bs-dismiss="modal">Hủy</button>
                            <button type="submit" class="btn btn-primary rounded-pill">Lưu ghi chú</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>