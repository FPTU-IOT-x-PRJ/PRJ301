<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi Tiết Buổi Học - EduPlan</title>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        .lesson-description {
            white-space: pre-wrap; /* Giữ định dạng xuống dòng và khoảng trắng */
            word-wrap: break-word;  /* Ngắt từ dài để tránh tràn */
        }
    </style>
</head>
<body>
    <jsp:include page="../navigation/navigation.jsp" />

    <div class="container py-4">
        <div class="row justify-content-center">
            <div class="col-lg-7 col-md-9">
                <div class="card shadow-sm">
                    <div class="card-header bg-info text-white">
                        <h4 class="mb-0"><i class="fas fa-chalkboard me-2"></i>Thông tin buổi học</h4>
                    </div>
                    <div class="card-body">
                        <c:if test="${empty lesson}">
                            <div class="alert alert-warning text-center" role="alert">
                                <i class="fas fa-exclamation-circle me-2"></i>Không tìm thấy thông tin buổi học.
                            </div>
                        </c:if>
                        <c:if test="${not empty lesson}">
                            <div class="text-center mb-4">
                                <i class="fas fa-calendar-check fa-4x text-muted mb-3"></i> <%-- Icon cho buổi học --%>
                                <h4><c:out value="${lesson.name}"/></h4>
                                <%-- Cập nhật dòng này để thêm liên kết --%>
                                <p class="text-muted">Thuộc môn học: 
                                    <c:if test="${not empty subject}">
                                        <a href="${pageContext.request.contextPath}/lessons?subjectId=${subject.id}" class="text-decoration-none">
                                            <c:out value="${subject.name}"/>
                                        </a>
                                    </c:if>
                                    <c:if test="${empty subject}">
                                        Không xác định
                                    </c:if>
                                </p>
                            </div>
                                
                            <div class="row g-3">
                                <div class="col-md-12">
                                    <label class="form-label fw-bold">Tên buổi học:</label>
                                    <p class="form-control-plaintext"><c:out value="${lesson.name}"/></p>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Ngày diễn ra:</label>
                                    <p class="form-control-plaintext">
                                        <fmt:formatDate value="${lesson.lessonDate}" pattern="dd/MM/yyyy"/>
                                    </p>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Trạng thái:</label>
                                    <p class="form-control-plaintext">
                                    <c:choose>
                                        <c:when test="${lesson.status == 'Completed'}">
                                            <span class="badge bg-success"><i class="fas fa-check-circle me-1"></i>Đã hoàn thành</span>
                                        </c:when>
                                        <c:when test="${lesson.status == 'Inactive'}">
                                            <span class="badge bg-danger"><i class="fas fa-times-circle me-1"></i>Vắng</span>
                                        </c:when>
                                        <c:when test="${lesson.status == 'Active'}">
                                            <span class="badge bg-secondary"><i class="fas fa-info-circle me-1"></i>Chưa học</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary"><i class="fas fa-question-circle me-1"></i>Không xác định</span>
                                        </c:otherwise>
                                    </c:choose>
                                    </p>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label fw-bold">Mô tả:</label>
                                    <p class="form-control-plaintext lesson-description">
                                        <c:out value="${lesson.description}"/>
                                    </p>
                                </div>
                            </div>
                        </c:if>
                    </div>
                    <div class="card-footer text-end">
                        <%-- Quay lại danh sách buổi học của môn học tương ứng --%>
                        <a href="${pageContext.request.contextPath}/subjects/detail?id=${lesson.subjectId}" class="btn btn-secondary me-2">
                            <i class="fas fa-arrow-left me-2"></i>Quay lại
                        </a>
                        <c:if test="${not empty lesson}">
                            <%-- Chỉnh sửa buổi học --%>
                            <a href="${pageContext.request.contextPath}/lessons/edit?id=${lesson.id}" class="btn btn-warning">
                                <i class="fas fa-edit me-2"></i>Chỉnh sửa
                            </a>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
</body>
</html>