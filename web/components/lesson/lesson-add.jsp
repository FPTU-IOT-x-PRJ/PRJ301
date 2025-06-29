<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thêm Buổi Học - EduPlan</title>
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
</head>
<body>
<jsp:include page="../navigation/navigation.jsp"/>

<div class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-8 col-md-10">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white">
                    <h4 class="mb-0"><i class="fas fa-calendar-alt me-2"></i>Thêm buổi học mới</h4>
                    <c:if test="${not empty currentSubject}">
                        <p class="mb-0">
                            <small>Cho môn: <strong><c:out value="${currentSubject.name}"/></strong></small>
                        </p>
                    </c:if>
                </div>
                <div class="card-body">

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <c:forEach var="entry" items="${errorMessage}">
                                <p class="mb-0">${entry.value}</p>
                            </c:forEach>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <form id="addLessonForm" action="${pageContext.request.contextPath}/lessons/add" method="POST" class="needs-validation" novalidate>
                        <input type="hidden" name="subjectId" value="${subjectId}"/>

                        <div class="row g-3">
                            <div class="col-md-12">
                                <label for="name" class="form-label">Tên buổi học <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="name" name="name" value="${formName}" required maxlength="100">
                                <div class="invalid-feedback">Vui lòng nhập tên buổi học.</div>
                            </div>

                            <div class="col-md-12">
                                <label for="lessonDate" class="form-label">Ngày học <span class="text-danger">*</span></label>
                                <input type="date" class="form-control" id="lessonDate" name="lessonDate" value="${formLessonDate}" required>
                                <div class="invalid-feedback">Vui lòng chọn ngày học.</div>
                            </div>

                            <div class="col-md-12">
                                <label for="description" class="form-label">Mô tả</label>
                                <textarea class="form-control" id="description" name="description" rows="4" maxlength="300">${formDescription}</textarea>
                                <div class="form-text">Tối đa 300 ký tự.</div>
                            </div>

                            <div class="col-md-12">
                                <label class="form-label">Trạng thái <span class="text-danger">*</span></label>
                                <select class="form-select" name="status" required>
                                    <option value="" <c:if test="${empty formStatus}">selected</c:if>>-- Chọn trạng thái --</option>
                                    <option value="Active" <c:if test="${formStatus eq 'Active'}">selected</c:if>>Chưa học</option>
                                    <option value="Completed" <c:if test="${formStatus eq 'Completed'}">selected</c:if>>Hoàn thành</option>
                                    <option value="Inactive" <c:if test="${formStatus eq 'Inactive'}">selected</c:if>>Vắng</option>
                                </select>
                                <div class="invalid-feedback">Vui lòng chọn trạng thái.</div>
                            </div>
                        </div>

                        <hr class="my-4">
                        <div class="d-flex justify-content-end">
                            <a href="${pageContext.request.contextPath}/lessons?subjectId=${subjectId}" class="btn btn-secondary me-2">
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