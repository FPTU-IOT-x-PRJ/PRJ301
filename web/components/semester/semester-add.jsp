<%-- 
    Document   : semester-add
    Created on : Jun 16, 2025, 5:15:15 PM
    Author     : Dung Ann
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thêm Kỳ Học Mới - EduPlan</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="../navigation/navigation.jsp" />

    <div class="container py-4">
        <div class="row justify-content-center">
            <div class="col-lg-8 col-md-10">
                <div class="card shadow-sm">
                    <div class="card-header bg-success text-white">
                        <h4 class="mb-0"><i class="fas fa-calendar-plus me-2"></i>Thêm kỳ học mới</h4>
                    </div>
                    <div class="card-body">
                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <i class="fas fa-exclamation-triangle me-2"></i>${errorMessage}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/semesters/add" method="post" class="needs-validation" novalidate>
                            <div class="mb-3">
                                <label for="name" class="form-label">Tên kỳ học <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="name" name="name" required value="<c:out value='${formName}'/>">
                                <div class="invalid-feedback">Vui lòng nhập tên kỳ học.</div>
                            </div>

                            <div class="mb-3">
                                <label for="startDate" class="form-label">Ngày bắt đầu <span class="text-danger">*</span></label>
                                <input type="date" class="form-control" id="startDate" name="startDate" required value="<c:out value='${formStartDate}'/>">
                                <div class="invalid-feedback">Vui lòng chọn ngày bắt đầu.</div>
                            </div>

                            <div class="mb-3">
                                <label for="endDate" class="form-label">Ngày kết thúc <span class="text-danger">*</span></label>
                                <input type="date" class="form-control" id="endDate" name="endDate" required value="<c:out value='${formEndDate}'/>">
                                <div class="invalid-feedback">Vui lòng chọn ngày kết thúc.</div>
                            </div>

                            <div class="mb-3">
                                <label for="status" class="form-label">Trạng thái <span class="text-danger">*</span></label>
                                <select class="form-select" id="status" name="status" required>
                                    <option value="">Chọn trạng thái</option>
                                    <option value="Active" <c:if test="${formStatus eq 'Active'}">selected</c:if>>Đang diễn ra</option>
                                    <option value="Inactive" <c:if test="${formStatus eq 'Inactive'}">selected</c:if>>Kết thúc</option>
                                </select>
                                <div class="invalid-feedback">Vui lòng chọn trạng thái kỳ học.</div>
                            </div>

                            <hr class="my-4">
                            <div class="d-flex justify-content-end">
                                <a href="${pageContext.request.contextPath}/semesters" class="btn btn-secondary me-2">
                                    <i class="fas fa-times me-2"></i>Hủy
                                </a>
                                <button type="submit" class="btn btn-success">
                                    <i class="fas fa-save me-2"></i>Thêm kỳ học
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
        // Validate Bootstrap form
        (function () {
            'use strict';
            var forms = document.querySelectorAll('.needs-validation');
            Array.prototype.slice.call(forms).forEach(function (form) {
                form.addEventListener('submit', function (event) {
                    if (!form.checkValidity()) {
                        event.preventDefault();
                        event.stopPropagation();
                    }
                    form.classList.add('was-validated');
                }, false);
            });
        })();
    </script>
</body>
</html>
