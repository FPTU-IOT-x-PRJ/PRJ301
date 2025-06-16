<%-- 
    Document   : semester-update
    Created on : Jun 16, 2025, 5:35:56 PM
    Author     : Dung Ann
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chỉnh Sửa Kỳ Học - EduPlan</title>
    <link href="/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
</head>
<body>
<jsp:include page="../navigation/navigation.jsp" />

<div class="container py-4">
    <div class="row justify-content-center">
        <div class="col-lg-8 col-md-10">
            <div class="card shadow-sm">
                <div class="card-header bg-warning text-dark">
                    <h4 class="mb-0"><i class="fas fa-calendar-edit me-2"></i>Chỉnh sửa kỳ học</h4>
                </div>
                <div class="card-body">
                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="fas fa-exclamation-triangle me-2"></i>${errorMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <c:if test="${empty semester}">
                        <div class="alert alert-warning text-center" role="alert">
                            <i class="fas fa-exclamation-circle me-2"></i>Không tìm thấy thông tin kỳ học để chỉnh sửa.
                        </div>
                    </c:if>

                    <c:if test="${not empty semester}">
                        <form id="editSemesterForm" action="${pageContext.request.contextPath}/semesters/edit" method="POST" class="needs-validation" novalidate>
                            <input type="hidden" name="id" value="${semester.id}">
                            <div class="row g-3">
                                <div class="col-md-12">
                                    <label for="name" class="form-label">Tên kỳ học <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="name" name="name" value="${semester.name}" required>
                                    <div class="invalid-feedback">Vui lòng nhập tên kỳ học.</div>
                                </div>
                                <div class="col-md-6">
                                    <label for="startDate" class="form-label">Ngày bắt đầu <span class="text-danger">*</span></label>
                                    <input type="date" class="form-control" id="startDate" name="startDate" value="${semester.startDate}" required>
                                    <div class="invalid-feedback">Vui lòng chọn ngày bắt đầu.</div>
                                </div>
                                <div class="col-md-6">
                                    <label for="endDate" class="form-label">Ngày kết thúc <span class="text-danger">*</span></label>
                                    <input type="date" class="form-control" id="endDate" name="endDate" value="${semester.endDate}" required>
                                    <div class="invalid-feedback">Vui lòng chọn ngày kết thúc.</div>
                                </div>
                                <div class="col-md-12">
                                    <label for="status" class="form-label">Trạng thái <span class="text-danger">*</span></label>
                                    <select class="form-select" id="status" name="status" required>
                                        <option value="">Chọn trạng thái</option>
                                        <option value="Active" <c:if test="${semester.status eq 'Active'}">selected</c:if>>Đang diễn ra</option>
                                        <option value="Inactive" <c:if test="${semester.status eq 'Inactive'}">selected</c:if>>Đã kết thúc</option>
                                    </select>
                                    <div class="invalid-feedback">Vui lòng chọn trạng thái.</div>
                                </div>
                            </div>

                            <hr class="my-4">
                            <div class="d-flex justify-content-end">
                                <a href="${pageContext.request.contextPath}/semesters" class="btn btn-secondary me-2">
                                    <i class="fas fa-times me-2"></i>Hủy
                                </a>
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-save me-2"></i>Cập nhật kỳ học
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
    (function () {
        'use strict';
        const form = document.getElementById('editSemesterForm');
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
