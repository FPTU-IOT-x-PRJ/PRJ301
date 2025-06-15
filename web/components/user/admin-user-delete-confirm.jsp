<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác Nhận Xóa Người Dùng - EduPlan</title>
    <link href="$/css/common.css" rel="stylesheet">
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
                        <h4 class="mb-0"><i class="fas fa-exclamation-triangle me-2"></i>Xác nhận xóa người dùng</h4>
                    </div>
                    <div class="card-body text-center">
                        <i class="fas fa-user-times fa-5x text-danger mb-4"></i>
                        <p class="lead mb-3">Bạn có chắc chắn muốn xóa người dùng này không?</p>
                        <c:if test="${not empty userToDelete}">
                            <p class="fs-4 fw-bold text-dark mb-1">${userToDelete.firstName} ${userToDelete.lastName} (${userToDelete.username})</p>
                            <p class="text-muted">ID: ${userToDelete.id}</p>
                            <p class="text-danger small fst-italic">Hành động này không thể hoàn tác!</p>
                        </c:if>
                        <c:if test="${empty userToDelete}">
                            <div class="alert alert-warning" role="alert">
                                <i class="fas fa-info-circle me-2"></i>Không tìm thấy thông tin người dùng cần xóa.
                            </div>
                        </c:if>
                    </div>
                    <div class="card-footer d-flex justify-content-center">
                        <a href="${pageContext.request.contextPath}/user/dashboard" class="btn btn-secondary me-3">
                            <i class="fas fa-times me-2"></i>Hủy bỏ
                        </a>
                        <c:if test="${not empty userToDelete}">
                            <form action="${pageContext.request.contextPath}/user/delete" method="POST" style="display: inline;">
                                <input type="hidden" name="id" value="${userToDelete.id}">
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