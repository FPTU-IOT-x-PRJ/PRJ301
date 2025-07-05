<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm Buổi Học - EduPlan</title>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        /* Thêm CSS để hiển thị invalid-feedback khi có lỗi từ backend */
        .is-invalid + .invalid-feedback {
            display: block;
        }
    </style>
</head>
<body>
<jsp:include page="../navigation/navigation.jsp"/>

<div class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-8 col-md-10">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white">
                    <h4 class="mb-0"><i class="fas fa-calendar-alt me-2"></i>Thêm buổi học mới</h4>
                    <%-- Sử dụng 'subject' object đã được set từ Controller --%>
                    <c:if test="${not empty subject}">
                        <p class="mb-0">
                            <small>Cho môn: <strong><c:out value="${subject.name}"/></strong></small>
                        </p>
                    </c:if>
                </div>
                <div class="card-body">

                    <%-- Hiển thị thông báo lỗi từ Map 'errors' --%>
                    <c:if test="${not empty errors}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="fas fa-exclamation-triangle me-2"></i>
                            <%-- Lặp qua các lỗi nếu có lỗi chung hoặc nhiều lỗi --%>
                            <c:if test="${not empty errors.general}">
                                <p class="mb-0">${errors.general}</p>
                            </c:if>
                            <%-- Có thể lặp qua tất cả các entry nếu muốn hiển thị từng lỗi riêng biệt --%>
                            <%-- <c:forEach var="entry" items="${errors}">
                                <p class="mb-0">${entry.value}</p>
                            </c:forEach> --%>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>
                    
                    <%-- Hiển thị thông báo lỗi chung từ errorMessage (nếu có trường hợp controller set riêng) --%>
                    <c:if test="${not empty errorMessage && empty errors.general}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="fas fa-exclamation-circle me-2"></i>${errorMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>


                    <form id="addLessonForm" action="${pageContext.request.contextPath}/lessons/add" method="POST" class="needs-validation" novalidate>
                        <%-- Đã sửa: Truyền subject.id thay vì subjectId --%>
                        <input type="hidden" name="subjectId" value="${subject.id}"/>

                        <div class="row g-3">
                            <div class="col-md-12">
                                <label for="name" class="form-label">Tên buổi học <span class="text-danger">*</span></label>
                                <%-- Đã sửa: Thêm lớp is-invalid dựa trên errors.name --%>
                                <input type="text" class="form-control ${not empty errors.name ? 'is-invalid' : ''}" id="name" name="name" value="${formName}" required maxlength="100">
                                <div class="invalid-feedback">
                                    <%-- Hiển thị lỗi từ backend nếu có, nếu không thì hiển thị lỗi mặc định của HTML5 --%>
                                    <c:choose>
                                        <c:when test="${not empty errors.name}">${errors.name}</c:when>
                                        <c:otherwise>Vui lòng nhập tên buổi học.</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <div class="col-md-12">
                                <label for="lessonDate" class="form-label">Ngày học <span class="text-danger">*</span></label>
                                <%-- Đã sửa: Thêm lớp is-invalid dựa trên errors.lessonDate --%>
                                <input type="date" class="form-control ${not empty errors.lessonDate ? 'is-invalid' : ''}" id="lessonDate" name="lessonDate" value="${formLessonDate}" required>
                                <div class="invalid-feedback">
                                    <c:choose>
                                        <c:when test="${not empty errors.lessonDate}">${errors.lessonDate}</c:when>
                                        <c:otherwise>Vui lòng chọn ngày học.</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <div class="col-md-12">
                                <label for="description" class="form-label">Mô tả</label>
                                <%-- Đã sửa: Thêm lớp is-invalid dựa trên errors.description (nếu có lỗi về maxlength từ backend) --%>
                                <textarea class="form-control ${not empty errors.description ? 'is-invalid' : ''}" id="description" name="description" rows="4" maxlength="300">${formDescription}</textarea>
                                <div class="form-text">Tối đa 300 ký tự.</div>
                                <div class="invalid-feedback">
                                    <c:if test="${not empty errors.description}">${errors.description}</c:if>
                                </div>
                            </div>

                            <div class="col-md-12">
                                <label class="form-label">Trạng thái <span class="text-danger">*</span></label>
                                <%-- Đã sửa: Thêm lớp is-invalid dựa trên errors.status --%>
                                <select class="form-select ${not empty errors.status ? 'is-invalid' : ''}" name="status" required>
                                    <option value="" <c:if test="${empty formStatus}">selected</c:if>>-- Chọn trạng thái --</option>
                                    <option value="Active" <c:if test="${formStatus eq 'Active'}">selected</c:if>>Chưa học</option>
                                    <option value="Completed" <c:if test="${formStatus eq 'Completed'}">selected</c:if>>Hoàn thành</option>
                                    <option value="Inactive" <c:if test="${formStatus eq 'Inactive'}">selected</c:if>>Vắng</option>
                                </select>
                                <div class="invalid-feedback">
                                    <c:choose>
                                        <c:when test="${not empty errors.status}">${errors.status}</c:when>
                                        <c:otherwise>Vui lòng chọn trạng thái.</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                        <hr class="my-4">
                        <div class="d-flex justify-content-end">
                            <a href="${pageContext.request.contextPath}/subjects/detail?id=${subject.id}" class="btn btn-secondary me-2">
                                <i class="fas fa-arrow-left me-2"></i>Quay lại
                            </a>
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-save me-2"></i>Thêm buổi học
                            </button>
                        </div>
                    </form>

                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
<script>
    (() => {
        'use strict';
        const form = document.getElementById('addLessonForm');

        form.addEventListener('submit', function (event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    })();
</script>

</body>
</html>
