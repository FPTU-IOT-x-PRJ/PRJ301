<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh Sửa Buổi Học - EduPlan</title>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
</head>
<body>
<jsp:include page="../navigation/navigation.jsp" />

<div class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-8 col-md-10">
            <div class="card shadow-lg">
                <div class="card-header bg-warning text-white">
                    <h4 class="mb-0"><i class="fas fa-edit me-2"></i>Chỉnh sửa buổi học</h4>
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

                    <c:if test="${empty lesson}">
                        <div class="alert alert-warning text-center">
                            <i class="fas fa-info-circle me-2"></i>Không tìm thấy buổi học.
                            <br>
                            <a href="${pageContext.request.contextPath}/lessons?subjectId=${subjectId}" class="btn btn-outline-secondary mt-3">Quay lại danh sách</a>
                        </div>
                    </c:if>

                    <c:if test="${not empty lesson}">
                        <form action="${pageContext.request.contextPath}/lessons/edit" method="POST" class="needs-validation" novalidate>
                            <input type="hidden" name="id" value="${lesson.id}" />
                            <input type="hidden" name="subjectId" value="${lesson.subjectId}" />
                            
                            <div class="mb-3">
                                <label for="name" class="form-label">Tên buổi học <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="name" name="name" 
                                       value="${not empty formName ? formName : lesson.name}" required maxlength="100">
                                <div class="invalid-feedback">Vui lòng nhập tên buổi học.</div>
                            </div>

                            <div class="mb-3">
                                <label for="lessonDate" class="form-label">Ngày học <span class="text-danger">*</span></label>
                                <fmt:formatDate value="${lesson.lessonDate}" pattern="yyyy-MM-dd" var="formattedLessonDate"/>
                                <input type="date" class="form-control" id="lessonDate" name="lessonDate" 
                                       value="${not empty formLessonDate ? formLessonDate : formattedLessonDate}" required>
                                <div class="invalid-feedback">Vui lòng chọn ngày học.</div>
                            </div>

                            <div class="mb-3">
                                <label for="description" class="form-label">Mô tả</label>
                                <textarea class="form-control" id="description" name="description" rows="4" maxlength="300">${not empty formDescription ? formDescription : lesson.description}</textarea>
                                <div class="form-text">Tối đa 300 ký tự.</div>
                            </div>

                            <div class="mb-3">
                                <label for="status" class="form-label">Trạng thái <span class="text-danger">*</span></label>
                                <select class="form-select" id="status" name="status" required>
                                    <option value="" <c:if test="${empty formStatus && empty lesson.status}">selected</c:if>>-- Chọn trạng thái --</option>
                                    <option value="Active" ${ (not empty formStatus ? formStatus : lesson.status) == 'Active' ? 'selected' : ''}>Chưa học</option>
                                    <option value="Completed" ${ (not empty formStatus ? formStatus : lesson.status) == 'Completed' ? 'selected' : ''}>Hoàn thành</option>
                                    <option value="Inactive" ${ (not empty formStatus ? formStatus : lesson.status) == 'Inactive' ? 'selected' : ''}>Vắng</option>
                                </select>
                                <div class="invalid-feedback">Vui lòng chọn trạng thái.</div>
                            </div>

                            <div class="d-flex justify-content-end">
                                <a href="${pageContext.request.contextPath}/lessons/detail?id=${lesson.id}" class="btn btn-secondary me-2">
                                    <i class="fas fa-times me-2"></i>Hủy
                                </a>
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-save me-2"></i>Lưu thay đổi
                                </button>
                            </div>
                        </form>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
<script>
    (() => {
        'use strict';
        const form = document.querySelector('.needs-validation');
        if (form) {
            form.addEventListener('submit', function (event) {
                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            }, false);
        }
    })();
</script>
</body>
</html>