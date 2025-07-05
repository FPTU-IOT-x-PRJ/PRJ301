<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${subject.name} - Chi tiết Môn học</title>
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
            .lesson-card, .document-item {
                transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
            }
            .lesson-card:hover, .document-item:hover {
                transform: translateY(-3px);
                box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
            }
            .lesson-card .card-title {
                font-size: 1.1rem;
                font-weight: 600;
                color: #343a40;
            }
            .lesson-card .card-text {
                font-size: 0.9rem;
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

            .document-item {
                border: 1px solid #e9ecef;
                border-radius: 10px;
                padding: 15px;
                margin-bottom: 10px;
                background-color: #ffffff;
            }
            .document-item .doc-title {
                font-weight: 500;
                color: #007bff;
            }
            .document-item .doc-date {
                font-size: 0.85rem;
                color: #6c757d;
            }
            .quiz-section {
                background-color: #e2f0ff;
                border: 1px dashed #007bff;
                padding: 30px;
                border-radius: 15px;
                text-align: center;
                color: #007bff;
            }
            .quiz-section h4 {
                font-weight: bold;
                margin-bottom: 15px;
            }
        </style>
    </head>
    <body>
        <jsp:include page="../navigation/navigation.jsp"/>

        <div class="container-fluid">
            <div class="row mb-4">
                <div class="col-12">
                    <a href="${pageContext.request.contextPath}/subjects?semesterId=${currentSemester.id}" class="btn btn-outline-secondary rounded-pill">
                        <i class="fas fa-arrow-left me-2"></i>Quay lại danh sách môn học
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
                            <i class="fas fa-book me-2"></i>Thông tin Môn học: ${subject.name}
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <p><strong>Mã môn:</strong> ${subject.code}</p>
                                    <p><strong>Số tín chỉ:</strong> ${subject.credits}</p>
                                    <p><strong>Giáo viên:</strong> ${subject.teacherName}</p>
                                </div>
                                <div class="col-md-6">
                                    <p><strong>Mô tả:</strong> ${subject.description}</p>
                                    <p><strong>Điều kiện tiên quyết:</strong> ${subject.prerequisites}</p>
                                    <p><strong>Trạng thái:</strong>
                                        <c:choose>
                                            <c:when test="${subject.isActive}">
                                                <span class="badge bg-warning">Đang học</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-success">Đã hoàn thành</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </div>
                            <div class="d-flex justify-content-end mt-3">
                                <a href="${pageContext.request.contextPath}/subjects/edit?id=${subject.id}" class="btn btn-outline-primary me-2 rounded-pill">
                                    <i class="fas fa-edit me-2"></i>Sửa môn học
                                </a>
                                <a href="${pageContext.request.contextPath}/subjects/delete-confirm?id=${subject.id}&semesterId=${subject.semesterId}" class="btn btn-outline-danger rounded-pill">
                                    <i class="fas fa-trash-alt me-2"></i>Xóa môn học
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
                            <i class="fas fa-chalkboard-teacher me-2"></i>Buổi học
                            <a href="${pageContext.request.contextPath}/lessons/add?subjectId=${subject.id}" class="btn btn-light btn-sm rounded-pill">
                                <i class="fas fa-plus-circle me-2"></i>Thêm buổi học mới
                            </a>
                        </div>
                        <div class="card-body">
                            <c:if test="${empty lessons}">
                                <div class="alert alert-info text-center" role="alert">
                                    Chưa có buổi học nào cho môn này.
                                </div>
                            </c:if>
                            <div class="row">
                                <jsp:include page="../../components/lesson/lesson-list-partial.jsp"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <i class="fas fa-file-alt me-2"></i>Tài liệu
                            <button type="button" class="btn btn-light btn-sm rounded-pill" data-bs-toggle="modal" data-bs-target="#uploadDocumentModal">
                                <i class="fas fa-upload me-2"></i>Tải lên tài liệu mới
                            </button>
                        </div>
                        <div class="card-body">
                            <c:if test="${empty documents}">
                                <div class="alert alert-info text-center" role="alert">
                                    Chưa có tài liệu nào cho môn này.
                                </div>
                            </c:if>
                            <jsp:include page="../../components/document/document-list-partial.jsp"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="card quiz-section">
                        <h4><i class="fas fa-hourglass-half me-2"></i>Tính năng Quiz</h4>
                        <p class="lead">Sắp ra mắt: Tạo và quản lý các bài kiểm tra (quiz) cho môn học này!</p>
                        <p class="text-muted">Hãy theo dõi các bản cập nhật tiếp theo.</p>
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
                            <input type="hidden" id="subjectId" name="subjectId" value="${subject.id}">
                            <input type="hidden" name="action" value="upload">
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

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        </body>
</html>