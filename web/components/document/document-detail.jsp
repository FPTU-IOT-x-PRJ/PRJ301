<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="../navigation/navigation.jsp"/>

        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Chi tiết Tài liệu</title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
        <style>
            body {
                font-family: 'Inter', sans-serif;
                background-color: #f8f9fa;
            }
            .container {
                padding-top: 2rem;
                padding-bottom: 2rem;
            }
            .card {
                border-radius: 0.75rem;
                box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.05);
            }
            .detail-item {
                display: flex;
                flex-wrap: wrap;
                margin-bottom: 1rem;
                padding-bottom: 0.75rem;
                border-bottom: 1px dashed #e9ecef;
            }
            .detail-item:last-child {
                border-bottom: none;
                margin-bottom: 0;
                padding-bottom: 0;
            }
            .detail-label {
                font-weight: 600;
                color: #495057;
                width: 150px;
                flex-shrink: 0;
            }
            .detail-value {
                color: #343a40;
                flex-grow: 1;
            }
            .btn-custom {
                padding: 0.625rem 1.25rem;
                border-radius: 0.375rem;
                font-weight: 500;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                transition: background-color 0.2s, transform 0.2s;
            }
            .btn-primary-custom {
                background-color: #007bff;
                color: white;
            }
            .btn-primary-custom:hover {
                background-color: #0056b3;
                transform: translateY(-1px);
            }
            .btn-secondary-custom {
                background-color: #6c757d;
                color: white;
            }
            .btn-secondary-custom:hover {
                background-color: #5a6268;
                transform: translateY(-1px);
            }
            .alert-custom {
                padding: 1rem 1.5rem;
                margin-bottom: 1.5rem;
                border-radius: 0.5rem;
                font-size: 0.95rem;
            }
            .alert-danger-custom {
                background-color: #f8d7da;
                color: #721c24;
                border-color: #f5c6cb;
            }
            .document-viewer {
                margin-top: 2rem;
                border: 1px solid #dee2e6;
                border-radius: 0.5rem;
                padding: 1rem;
                background-color: #ffffff;
                text-align: center; /* Để căn giữa iframe/video/img */
            }
            .document-viewer iframe, .document-viewer img, .document-viewer video {
                max-width: 100%;
                height: auto;
                border: none; /* Bỏ border mặc định cho iframe */
            }
        </style>
    </head>
    <body>
        <div class="container">
            <h1 class="mb-4 text-center text-primary">Chi tiết Tài liệu</h1>

            <c:if test="${not empty requestScope.errorMessage}">
                <div class="alert alert-danger-custom alert-custom" role="alert">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    <c:out value="${requestScope.errorMessage}"/>
                </div>
            </c:if>

            <c:if test="${document != null}">
                <div class="card">
                    <div class="card-body">
                        <div class="detail-item">
                            <span class="detail-label">ID Tài liệu:</span>
                            <span class="detail-value"><c:out value="${document.id}"/></span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Tên File:</span>
                            <span class="detail-value">
                                <a href="${document.filePath}" target="_blank" class="text-primary text-decoration-none">
                                    <i class="fas fa-file-alt me-1"></i> <c:out value="${document.fileName}"/>
                                </a>
                            </span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Mô tả:</span>
                            <span class="detail-value"><c:out value="${document.description != null && !document.description.isEmpty() ? document.description : 'Không có mô tả'}"/></span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Môn học:</span>
                            <span class="detail-value">
                                <c:choose>
                                    <c:when test="${associatedSubject != null}">
                                        <c:out value="${associatedSubject.name} (${associatedSubject.code})"/>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted fst-italic">Không liên kết</span>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Buổi học:</span>
                            <span class="detail-value">
                                <c:choose>
                                    <c:when test="${associatedLesson != null}">
                                        <c:out value="${associatedLesson.name}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted fst-italic">Không liên kết</span>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Loại File:</span>
                            <span class="detail-value"><c:out value="${document.fileType}"/></span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Kích thước File:</span>
                            <span class="detail-value"><fmt:formatNumber value="${document.fileSize / 1024}" pattern="#,##0.00"/> KB</span>
                        </div>


                        <%-- PHẦN XEM TÀI LIỆU TÍCH HỢP --%>
                        <h3 class="mt-4 mb-3 text-secondary">Nội dung Tài liệu</h3>
                        <div class="document-viewer">
                            <c:choose>
                                <c:when test="${document.fileType.startsWith('image/')}">
                                    <img src="${document.filePath}" alt="${document.fileName}" class="img-fluid"/>
                                </c:when>
                                <c:when test="${document.fileType.startsWith('video/')}">
                                    <video controls class="w-100">
                                        <source src="${document.filePath}" type="${document.fileType}">
                                        Trình duyệt của bạn không hỗ trợ xem video này.
                                    </video>
                                </c:when>
                                <c:when test="${document.fileType == 'application/pdf'}">
                                    <%-- Sử dụng Google Docs Viewer cho PDF --%>
                                    <iframe src="https://docs.google.com/viewer?url=${document.filePath}&embedded=true" style="width:100%; height:600px;" allowfullscreen webkitallowfullscreen></iframe>
                                    </c:when>
                                    <c:otherwise>
                                    <div class="alert alert-warning" role="alert">
                                        <i class="fas fa-exclamation-circle me-2"></i>
                                        Không thể xem trực tiếp định dạng file này (${document.fileType}).
                                        Vui lòng <a href="${pageContext.request.contextPath}/documents/download?id=${document.id}" class="alert-link">tải xuống</a> để xem.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <%-- KẾT THÚC PHẦN XEM TÀI LIỆU TÍCH HỢP --%>

                        <div class="d-flex justify-content-end mt-4">
                            <a href="${requestScope.redirectUrl}" class="btn btn-secondary-custom me-2">
                                <i class="fas fa-arrow-left me-2"></i>Quay lại
                            </a>
                            <a href="${pageContext.request.contextPath}/documents/edit?id=${document.id}" class="btn btn-primary-custom">
                                <i class="fas fa-edit me-2"></i>Chỉnh sửa
                            </a>
                        </div>
                    </div>
                </div>
            </c:if>
            <c:if test="${document == null && empty requestScope.errorMessage}">
                <div class="alert alert-info text-center" role="alert">
                    <i class="fas fa-info-circle me-2"></i>Không tìm thấy tài liệu.
                </div>
            </c:if>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>