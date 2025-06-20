<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh Sửa Kỳ Học - EduPlan</title>
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: var(--light-color);
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }
        .container {
            flex-grow: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding-top: 20px;
            padding-bottom: 20px;
        }
    </style>
</head>
<body>
    <jsp:include page="../navigation/navigation.jsp" />

    <div class="container">
        <div class="row justify-content-center w-100">
            <div class="col-lg-8 col-md-10">
                <div class="card shadow-lg">
                    <div class="card-header bg-warning py-3">
                        <h4 class="mb-0 text-start"><i class="fas fa-edit me-2"></i>Chỉnh sửa kỳ học</h4>
                    </div>
                    <div class="card-body p-4">
                        <%-- Hiển thị thông báo lỗi từ backend nếu có --%>
                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <i class="fas fa-exclamation-triangle me-2"></i>${errorMessage}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                        
                        <%-- Thông báo nếu không tìm thấy kỳ học --%>
                        <c:if test="${empty semester}">
                            <div class="alert alert-warning text-center" role="alert">
                                <i class="fas fa-exclamation-circle me-2"></i>Không tìm thấy thông tin kỳ học để chỉnh sửa.
                                <p class="mt-2"><a href="${pageContext.request.contextPath}/semesters/dashboard" class="alert-link">Quay lại danh sách kỳ học</a></p>
                            </div>
                        </c:if>
                        
                        <%-- Hiển thị form chỉ khi có semester object --%>
                        <c:if test="${not empty semester}">
                            <form id="editSemesterForm" action="${pageContext.request.contextPath}/semesters/update" method="POST" class="needs-validation" novalidate>
                                <input type="hidden" id="id" name="id" value="${semester.id}">
                                <div class="row g-3">
                                    <div class="col-md-12">
                                        <label for="name" class="form-label">Tên kỳ học <span class="text-danger">*</span></label>
                                        <input type="text" class="form-control" id="name" name="name" value="${semester.name}" required maxlength="100">
                                        <div class="invalid-feedback">Vui lòng nhập tên kỳ học không quá 100 ký tự.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="startDate" class="form-label">Ngày bắt đầu <span class="text-danger">*</span></label>
                                        <input type="date" class="form-control" id="startDate" name="startDate" value="${semester.startDate}" required>
                                        <div class="invalid-feedback">Vui lòng chọn ngày bắt đầu.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="endDate" class="form-label">Ngày kết thúc <span class="text-danger">*</span></label>
                                        <input type="date" class="form-control" id="endDate" name="endDate" value="${semester.endDate}" required>
                                        <div class="invalid-feedback" id="endDateError">Vui lòng chọn ngày kết thúc.</div>
                                    </div>
                                    <div class="col-md-12">
                                        <label for="status" class="form-label">Trạng thái <span class="text-danger">*</span></label>
                                        <select class="form-select" id="status" name="status" required>
                                            <option value="">Chọn trạng thái</option>
                                            <option value="Active" <c:if test="${semester.status eq 'Active'}">selected</c:if>>Đang diễn ra</option>
                                            <option value="Inactive" <c:if test="${semester.status eq 'Inactive'}">selected</c:if>>Bảo lưu</option>
                                            <option value="Completed" <c:if test="${semester.status eq 'Completed'}">selected</c:if>>Hoàn thành</option>
                                        </select>
                                        <div class="invalid-feedback">Vui lòng chọn trạng thái kỳ học.</div>
                                    </div>
                                    <div class="col-md-12">
                                        <label for="description" class="form-label">Mô tả</label>
                                        <textarea class="form-control" id="description" name="description" rows="6" placeholder="Nhập mô tả cho kỳ học..." maxlength="300">${semester.description}</textarea>
                                        <div class="form-text">Mô tả chi tiết về kỳ học (không bắt buộc, tối đa 300 ký tự).</div>
                                        <div class="invalid-feedback" id="descriptionError">Mô tả không được vượt quá 300 ký tự.</div>
                                    </div>
                                </div>
                                <hr class="my-4">
                                <div class="d-flex justify-content-end">
                                    <a href="${pageContext.request.contextPath}/semesters/dashboard" class="btn btn-secondary me-2">
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
        (function () {
            'use strict';

            var form = document.getElementById('editSemesterForm');

            function validateDateRange() {
                var startDate = document.getElementById('startDate');
                var endDate = document.getElementById('endDate');
                var endDateError = document.getElementById('endDateError');

                startDate.setCustomValidity('');
                endDate.setCustomValidity('');

                if (startDate.value && endDate.value) {
                    var startDateValue = new Date(startDate.value);
                    var endDateValue = new Date(endDate.value);

                    if (endDateValue <= startDateValue) {
                        endDate.setCustomValidity('Ngày kết thúc phải sau ngày bắt đầu.');
                        if (endDateError) {
                            endDateError.textContent = 'Ngày kết thúc phải sau ngày bắt đầu.';
                        }
                        return false;
                    } else {
                        if (endDateError) {
                            endDateError.textContent = 'Vui lòng chọn ngày kết thúc.';
                        }
                    }
                }

                return true;
            }

            function validateDescriptionLength() {
                var description = document.getElementById('description');
                var descriptionError = document.getElementById('descriptionError');

                if (description.value.length > 300) {
                    description.setCustomValidity('Mô tả không được vượt quá 300 ký tự.');
                    if (descriptionError) {
                        descriptionError.textContent = 'Mô tả không được vượt quá 300 ký tự.';
                    }
                    return false;
                } else {
                    description.setCustomValidity('');
                    return true;
                }
            }

            function validateNameLength() {
                var name = document.getElementById('name');

                if (name.value.length > 100) {
                    name.setCustomValidity('Tên kỳ học không được vượt quá 100 ký tự.');
                    return false;
                } else {
                    name.setCustomValidity('');
                    return true;
                }
            }

            // Validation khi submit form
            if (form) {
                form.addEventListener('submit', function (event) {
                    validateDateRange();
                    validateDescriptionLength();
                    validateNameLength();

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
