<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm Kỳ Học Mới - EduPlan</title>
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
                        <%-- Hiển thị thông báo lỗi nếu có (từ backend) --%>
                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <i class="fas fa-exclamation-triangle me-2"></i>${errorMessage}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>

                        <form id="addSemesterForm" action="${pageContext.request.contextPath}/semesters/add" method="POST" class="needs-validation" novalidate>
                            <div class="row g-3">
                                <div class="col-md-12">
                                    <label for="name" class="form-label">Tên kỳ học <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="name" name="name" value="<c:out value="${formName}"/>" required maxlength="100">
                                    <div class="invalid-feedback">Vui lòng nhập tên kỳ học không quá 100 ký tự.</div>
                                </div>
                                <div class="col-md-6">
                                    <label for="startDate" class="form-label">Ngày bắt đầu <span class="text-danger">*</span></label>
                                    <input type="date" class="form-control" id="startDate" name="startDate" value="<c:out value="${formStartDate}"/>" required>
                                    <div class="invalid-feedback">Vui lòng chọn ngày bắt đầu.</div>
                                </div>
                                <div class="col-md-6">
                                    <label for="endDate" class="form-label">Ngày kết thúc <span class="text-danger">*</span></label>
                                    <input type="date" class="form-control" id="endDate" name="endDate" value="<c:out value="${formEndDate}"/>" required>
                                    <div class="invalid-feedback" id="endDateError">Vui lòng chọn ngày kết thúc.</div>
                                </div>
                                <div class="col-md-12">
                                    <label for="status" class="form-label">Trạng thái <span class="text-danger">*</span></label>
                                    <select class="form-select" id="status" name="status" required>
                                        <option value="">Chọn trạng thái</option>
                                        <option value="Active" <c:if test="${formStatus eq 'Active'}">selected</c:if>>Đang diễn ra</option>
                                        <option value="Inactive" <c:if test="${formStatus eq 'Inactive'}">selected</c:if>>Bảo lưu</option>
                                        <option value="Completed" <c:if test="${formStatus eq 'Completed'}">selected</c:if>>Hoàn thành</option>
                                    </select>
                                    <div class="invalid-feedback">Vui lòng chọn trạng thái kỳ học.</div>
                                </div>
                                <div class="col-md-12">
                                    <label for="description" class="form-label">Mô tả</label>
                                    <textarea class="form-control" id="description" name="description" rows="6" placeholder="Nhập mô tả cho kỳ học..." maxlength="300"><c:out value="${formDescription}"/></textarea>
                                    <div class="form-text">Mô tả chi tiết về kỳ học (không bắt buộc, tối đa 300 ký tự).</div>
                                    <div class="invalid-feedback" id="descriptionError">Mô tả không được vượt quá 300 ký tự.</div>
                                </div>
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
        (function () {
            'use strict';

            var form = document.getElementById('addSemesterForm');

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

            // Chỉ validation khi submit form
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
        })();
    </script>

</body>
</html>
