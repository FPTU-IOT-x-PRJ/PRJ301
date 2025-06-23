<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Thêm Buổi Học Mới cho Môn ${requestScope.subject.name}</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <style>
            .container {
                margin-top: 20px;
            }
            .form-group {
                margin-bottom: 15px;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <h1 class="mb-4">Thêm Buổi Học Mới cho Môn: <span class="text-primary">${requestScope.subject.name}</span></h1>

            <c:if test="${not empty requestScope.errors.general}">
                <div class="alert alert-danger" role="alert">
                    ${requestScope.errors.general}
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/lessons/add" method="post">
                <input type="hidden" name="subjectId" value="${requestScope.subject.id}">

                <div class="mb-3">
                    <label for="name" class="form-label">Tên Buổi Học <span class="text-danger">*</span></label>
                    <input type="text" class="form-control ${not empty requestScope.errors.name ? 'is-invalid' : ''}" id="name" name="name" value="${requestScope.formName}" required>
                    <c:if test="${not empty requestScope.errors.name}">
                        <div class="invalid-feedback">
                            ${requestScope.errors.name}
                        </div>
                    </c:if>
                </div>

                <div class="mb-3">
                    <label for="lessonDate" class="form-label">Ngày Học <span class="text-danger">*</span></label>
                    <input type="date" class="form-control ${not empty requestScope.errors.lessonDate ? 'is-invalid' : ''}" id="lessonDate" name="lessonDate" value="${requestScope.formLessonDate}" required>
                    <c:if test="${not empty requestScope.errors.lessonDate}">
                        <div class="invalid-feedback">
                            ${requestScope.errors.lessonDate}
                        </div>
                    </c:if>
                </div>

                <div class="mb-3">
                    <label for="description" class="form-label">Mô Tả <span class="text-danger">*</span></label>
                    <textarea class="form-control ${not empty requestScope.errors.description ? 'is-invalid' : ''}" id="description" name="description" rows="3" required>${requestScope.formDescription}</textarea>
                    <c:if test="${not empty requestScope.errors.description}">
                        <div class="invalid-feedback">
                            ${requestScope.errors.description}
                        </div>
                    </c:if>
                </div>

                <div class="mb-3">
                    <label for="status" class="form-label">Trạng Thái <span class="text-danger">*</span></label>
                    <select class="form-select ${not empty requestScope.errors.status ? 'is-invalid' : ''}" id="status" name="status" required>
                        <option value="">-- Chọn trạng thái --</option>
                        <option value="Planned" ${requestScope.formStatus == 'Planned' ? 'selected' : ''}>Kế hoạch</option>
                        <option value="Completed" ${requestScope.formStatus == 'Completed' ? 'selected' : ''}>Hoàn thành</option>
                        <option value="Cancelled" ${requestScope.formStatus == 'Cancelled' ? 'selected' : ''}>Hủy bỏ</option>
                    </select>
                    <c:if test="${not empty requestScope.errors.status}">
                        <div class="invalid-feedback">
                            ${requestScope.errors.status}
                        </div>
                    </c:if>
                </div>

                <button type="submit" class="btn btn-primary me-2">Thêm Buổi Học</button>
                <a href="${pageContext.request.contextPath}/lessons?subjectId=${requestScope.subject.id}" class="btn btn-secondary">Hủy</a>
            </form>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>