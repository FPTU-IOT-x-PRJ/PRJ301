<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thêm Môn Học - EduPlan</title>
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
                    <h4 class="mb-0"><i class="fas fa-book me-2"></i>Thêm môn học mới</h4>
                    <%-- THÊM PHẦN NÀY ĐỂ HIỂN THỊ TÊN KỲ HỌC --%>
                    <c:if test="${not empty currentSemester}">
                        <p class="mb-0">
                            <small>Cho Kỳ: <strong><c:out value="${currentSemester.name}"/></strong></small>
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

                    <form id="addSubjectForm" action="${pageContext.request.contextPath}/subjects/add" method="POST" class="needs-validation" novalidate>
                        <input type="hidden" name="semesterId" value="${semesterId}"/>

                        <div class="row g-3">
                            <div class="col-md-12">
                                <label for="name" class="form-label">Tên môn học <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="name" name="name" value="${formName}" required maxlength="100">
                                <div class="invalid-feedback">Vui lòng nhập tên môn học.</div>
                            </div>

                            <div class="col-md-6">
                                <label for="code" class="form-label">Mã môn <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="code" name="code" value="${formCode}" required maxlength="50">
                                <div class="invalid-feedback">Vui lòng nhập mã môn học (tối đa 50 ký tự).</div>
                            </div>

                            <div class="col-md-6">
                                <label for="credits" class="form-label">Số tín chỉ <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" id="credits" name="credits" min="1" value="${formCredits}" required>
                                <div class="invalid-feedback">Tín chỉ phải lớn hơn 0.</div>
                            </div>

                            <div class="col-md-12">
                                <label for="teacherName" class="form-label">Tên giảng viên</label>
                                <input type="text" class="form-control" id="teacherName" name="teacherName" value="${formTeacherName}" maxlength="255">
                            </div>

                            <div class="col-md-12">
                                <label for="prerequisites" class="form-label">Môn học tiên quyết</label>
                                <input type="text" class="form-control" id="prerequisites" name="prerequisites" value="${formPrerequisites}" maxlength="300">
                            </div>

                            <div class="col-md-12">
                                <label for="description" class="form-label">Mô tả</label>
                                <textarea class="form-control" id="description" name="description" rows="4" maxlength="300">${formDescription}</textarea>
                                <div class="form-text">Tối đa 300 ký tự.</div>
                            </div>

                            <div class="col-md-12">
                                <label class="form-label">Trạng thái <span class="text-danger">*</span></label>
                                <select class="form-select" name="isActive" required>
                                    <option value="true" <c:if test="${formIsActive eq true}">selected</c:if>>Đang học</option>
                                    <option value="false" <c:if test="${formIsActive eq false}">selected</c:if>>Đã hoàn thành</option>
                                </select>
                                <div class="invalid-feedback">Vui lòng chọn trạng thái.</div>
                            </div>
                        </div>

                        <hr class="my-4">
                        <div class="d-flex justify-content-end">
                            <a href="${pageContext.request.contextPath}/subjects?semesterId=${semesterId}" class="btn btn-secondary me-2">
                                <i class="fas fa-arrow-left me-2"></i>Quay lại
                            </a>
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-save me-2"></i>Thêm môn học
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
        const form = document.getElementById('addSubjectForm');

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