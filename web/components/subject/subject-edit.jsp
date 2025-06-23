<%-- 
    Document   : subject-edit
    Created on : Jun 21, 2025, 10:23:32 AM
    Author     : Dung Ann
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh Sửa Môn Học - EduPlan</title>
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
                    <h4 class="mb-0"><i class="fas fa-edit me-2"></i>Chỉnh sửa môn học</h4>
                </div>
                <div class="card-body">

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger" role="alert">
                            <i class="fas fa-exclamation-circle me-2"></i>${errorMessage}
                        </div>
                    </c:if>

                    <c:if test="${empty subject}">
                        <div class="alert alert-warning text-center">
                            <i class="fas fa-info-circle me-2"></i>Không tìm thấy môn học.
                            <br>
                            <a href="${pageContext.request.contextPath}/subjects" class="btn btn-outline-secondary mt-3">Quay lại danh sách</a>
                        </div>
                    </c:if>

                    <c:if test="${not empty subject}">
                        <form action="${pageContext.request.contextPath}/subjects/edit" method="POST" class="needs-validation" novalidate>
                            <input type="hidden" name="id" value="${subject.id}" />
                            <input type="hidden" name="semesterId" value="${subject.semesterId}" />
                            
                            <div class="mb-3">
                                <label for="name" class="form-label">Tên môn học <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="name" name="name" value="${subject.name}" required maxlength="255">
                                <div class="invalid-feedback">Vui lòng nhập tên môn học.</div>
                            </div>

                            <div class="mb-3">
                                <label for="code" class="form-label">Mã môn học <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="code" name="code" value="${subject.code}" required maxlength="50">
                                <div class="invalid-feedback">Vui lòng nhập mã môn học hợp lệ.</div>
                            </div>

                            <div class="mb-3">
                                <label for="credits" class="form-label">Số tín chỉ <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" id="credits" name="credits" value="${subject.credits}" required min="1">
                                <div class="invalid-feedback">Số tín chỉ phải lớn hơn 0.</div>
                            </div>

                            <div class="mb-3">
                                <label for="teacherName" class="form-label">Tên giảng viên</label>
                                <input type="text" class="form-control" id="teacherName" name="teacherName" value="${subject.teacherName}" maxlength="255">
                            </div>

                            <div class="mb-3">
                                <label for="description" class="form-label">Mô tả</label>
                                <textarea class="form-control" id="description" name="description" rows="4" maxlength="300">${subject.description}</textarea>
                                <div class="form-text">Tối đa 300 ký tự.</div>
                            </div>

                            <div class="mb-3">
                                <label for="prerequisites" class="form-label">Môn học tiên quyết</label>
                                <input type="text" class="form-control" id="prerequisites" name="prerequisites" value="${subject.prerequisites}" maxlength="300">
                            </div>

                            <div class="mb-3">
                                <label for="isActive" class="form-label">Trạng thái</label>
                                <select class="form-select" id="isActive" name="isActive">
                                    <option value="true" <c:if test="${subject.isActive}">selected</c:if>>Đang mở</option>
                                    <option value="false" <c:if test="${!subject.isActive}">selected</c:if>>Tạm ẩn</option>
                                </select>
                            </div>

                            <div class="d-flex justify-content-end">
                                <a href="${pageContext.request.contextPath}/subjects" class="btn btn-secondary me-2">
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