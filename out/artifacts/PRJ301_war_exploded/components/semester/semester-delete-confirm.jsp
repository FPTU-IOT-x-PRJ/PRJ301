<%--
    Document   : semester-delete-confirm
    Created on : Jun 29, 2025, 7:00:00 PM
    Author     : Your Name
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác Nhận Xóa Kỳ Học - EduPlan</title>
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
</head>
<body>
    <jsp:include page="../navigation/navigation.jsp" />

    <div class="container py-4">
        <div class="row justify-content-center">
            <div class="col-lg-6 col-md-8">
                <div class="card shadow-sm border-danger">
                    <div class="card-header bg-danger text-white">
                        <h4 class="mb-0"><i class="fas fa-exclamation-triangle me-2"></i>Xác nhận xóa kỳ học</h4>
                    </div>
                    <div class="card-body text-center">
                        <%-- Icon lớn cho việc xóa kỳ học, có thể dùng fa-calendar-times hoặc fa-trash --%>
                        <i class="fas fa-calendar-times fa-5x text-danger mb-4"></i> 
                        <p class="lead mb-3">Bạn có chắc chắn muốn xóa kỳ học này không?</p>
                        <c:if test="${not empty semesterToDelete}">
                            <p class="fs-4 fw-bold text-dark mb-1">${semesterToDelete.name}</p>
                            <p class="text-danger small fst-italic">Hành động này sẽ xóa tất cả môn học và lịch trình liên quan đến kỳ học này và không thể hoàn tác!</p>
                        </c:if>
                        <c:if test="${empty semesterToDelete}">
                            <div class="alert alert-warning" role="alert">
                                <i class="fas fa-info-circle me-2"></i>Không tìm thấy thông tin kỳ học cần xóa.
                            </div>
                        </c:if>
                    </div>
                    <div class="card-footer d-flex justify-content-center">
                        <a href="${pageContext.request.contextPath}/semesters" class="btn btn-secondary me-3">
                            <i class="fas fa-times me-2"></i>Hủy bỏ
                        </a>
                        <c:if test="${not empty semesterToDelete}">
                            <%-- Form gửi yêu cầu POST đến /semesters/delete để thực hiện xóa --%>
                            <form action="${pageContext.request.contextPath}/semesters/delete" method="POST" style="display: inline;">
                                <input type="hidden" name="id" value="${semesterToDelete.id}">
                                <button type="submit" class="btn btn-danger">
                                    <i class="fas fa-trash-alt me-2"></i>Xóa ngay
                                </button>
                            </form>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
</body>
</html>