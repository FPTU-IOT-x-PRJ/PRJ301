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
        /* Style cho invalid feedback khi dùng JSTL errors map */
        .is-invalid + .invalid-feedback {
            display: block;
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
                        <%-- Hiển thị thông báo lỗi chung từ backend nếu có --%>
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
                                <p class="mt-2"><a href="${pageContext.request.contextPath}/semesters" class="alert-link">Quay lại danh sách kỳ học</a></p>
                            </div>
                        </c:if>
                        
                        <%-- Hiển thị form chỉ khi có semester object --%>
                        <c:if test="${not empty semester}">
                            <form id="editSemesterForm" action="${pageContext.request.contextPath}/semesters/edit" method="POST" class="needs-validation" novalidate>
                                <input type="hidden" id="id" name="id" value="${semester.id}">
                                <div class="row g-3">
                                    <div class="col-md-12">
                                        <label for="name" class="form-label">Tên kỳ học <span class="text-danger">*</span></label>
                                        <input type="text" class="form-control ${not empty errors.name ? 'is-invalid' : ''}" id="name" name="name" value="${semester.name}" required maxlength="100">
                                        <div class="invalid-feedback">
                                            <c:choose>
                                                <c:when test="${not empty errors.name}">${errors.name}</c:when>
                                                <c:otherwise>Vui lòng nhập tên kỳ học không quá 100 ký tự.</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="startDate" class="form-label">Ngày bắt đầu <span class="text-danger">*</span></label>
                                        <%-- Convert java.sql.Date sang định dạng yyyy-MM-dd cho input type="date" --%>
                                        <input type="date" class="form-control ${not empty errors.startDate || not empty errors.date ? 'is-invalid' : ''}" id="startDate" name="startDate" value="${semester.startDate}" required>
                                        <div class="invalid-feedback">
                                            <c:choose>
                                                <c:when test="${not empty errors.startDate}">${errors.startDate}</c:when>
                                                <c:when test="${not empty errors.date}">${errors.date}</c:when>
                                                <c:otherwise>Vui lòng chọn ngày bắt đầu.</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="endDate" class="form-label">Ngày kết thúc <span class="text-danger">*</span></label>
                                        <input type="date" class="form-control ${not empty errors.endDate || not empty errors.date ? 'is-invalid' : ''}" id="endDate" name="endDate" value="${semester.endDate}" required>
                                        <div class="invalid-feedback" id="endDateError">
                                            <c:choose>
                                                <c:when test="${not empty errors.endDate}">${errors.endDate}</c:when>
                                                <c:when test="${not empty errors.date}">${errors.date}</c:when>
                                                <c:otherwise>Vui lòng chọn ngày kết thúc.</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                    <div class="col-md-12">
                                        <label for="status" class="form-label">Trạng thái <span class="text-danger">*</span></label>
                                        <select class="form-select ${not empty errors.status ? 'is-invalid' : ''}" id="status" name="status" required>
                                            <option value="">Chọn trạng thái</option>
                                            <option value="Active" <c:if test="${semester.status eq 'Active'}">selected</c:if>>Đang diễn ra</option>
                                            <option value="Inactive" <c:if test="${semester.status eq 'Inactive'}">selected</c:if>>Bảo lưu</option>
                                            <option value="Completed" <c:if test="${semester.status eq 'Completed'}">selected</c:if>>Hoàn thành</option>
                                        </select>
                                        <div class="invalid-feedback">
                                            <c:choose>
                                                <c:when test="${not empty errors.status}">${errors.status}</c:when>
                                                <c:otherwise>Vui lòng chọn trạng thái kỳ học.</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                    <div class="col-md-12">
                                        <label for="description" class="form-label">Mô tả</label>
                                        <textarea class="form-control ${not empty errors.description ? 'is-invalid' : ''}" id="description" name="description" rows="6" placeholder="Nhập mô tả cho kỳ học..." maxlength="300">${semester.description}</textarea>
                                        <div class="form-text">Mô tả chi tiết về kỳ học (không bắt buộc, tối đa 300 ký tự).</div>
                                        <div class="invalid-feedback" id="descriptionError">
                                            <c:choose>
                                                <c:when test="${not empty errors.description}">${errors.description}</c:when>
                                                <c:otherwise>Mô tả không được vượt quá 300 ký tự.</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                                <hr class="my-4">
                                <div class="d-flex justify-content-end">
                                    <a href="${pageContext.request.contextPath}/semesters" class="btn btn-secondary me-2">
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
                var startDateInput = document.getElementById('startDate');
                var endDateInput = document.getElementById('endDate');
                var endDateErrorDiv = document.getElementById('endDateError');

                startDateInput.setCustomValidity('');
                endDateInput.setCustomValidity('');

                if (startDateInput.value && endDateInput.value) {
                    var startDate = new Date(startDateInput.value);
                    var endDate = new Date(endDateInput.value);

                    if (endDate <= startDate) {
                        endDateInput.setCustomValidity('Ngày kết thúc phải sau ngày bắt đầu.');
                        endDateErrorDiv.textContent = 'Ngày kết thúc phải sau ngày bắt đầu.';
                        return false;
                    } else {
                        // Clear custom validity if valid, reset default message
                        endDateErrorDiv.textContent = 'Vui lòng chọn ngày kết thúc.'; 
                    }
                }
                return true;
            }

            function validateDescriptionLength() {
                var descriptionInput = document.getElementById('description');
                var descriptionErrorDiv = document.getElementById('descriptionError');

                if (descriptionInput.value.length > 300) {
                    descriptionInput.setCustomValidity('Mô tả không được vượt quá 300 ký tự.');
                    descriptionErrorDiv.textContent = 'Mô tả không được vượt quá 300 ký tự.';
                    return false;
                } else {
                    descriptionInput.setCustomValidity('');
                    descriptionErrorDiv.textContent = 'Mô tả không được vượt quá 300 ký tự.'; // Reset default message
                    return true;
                }
            }

            function validateNameLength() {
                var nameInput = document.getElementById('name');

                if (nameInput.value.length > 100) {
                    nameInput.setCustomValidity('Tên kỳ học không được vượt quá 100 ký tự.');
                    return false;
                } else {
                    nameInput.setCustomValidity('');
                    return true;
                }
            }

            // Validation khi submit form
            if (form) {
                form.addEventListener('submit', function (event) {
                    // Chạy tất cả các validations để hiển thị feedback đầy đủ
                    var isDateValid = validateDateRange();
                    var isDescriptionValid = validateDescriptionLength();
                    var isNameValid = validateNameLength();

                    if (!form.checkValidity() || !isDateValid || !isDescriptionValid || !isNameValid) {
                        event.preventDefault();
                        event.stopPropagation();
                    }

                    form.classList.add('was-validated');
                }, false);

                // Add event listeners for real-time validation feedback
                document.getElementById('startDate').addEventListener('change', validateDateRange);
                document.getElementById('endDate').addEventListener('change', validateDateRange);
                document.getElementById('description').addEventListener('input', validateDescriptionLength);
                document.getElementById('name').addEventListener('input', validateNameLength);
            }
        })();
    </script>
</body>
</html>