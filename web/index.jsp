<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <title>Trang chủ - EduPlan</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
        <style>
            body {
                background-color: var(--light-color); /* Nền trắng nhạt theo common.css */
            }
            .welcome-card .card-header {
                background-color: var(--dark-color); /* Tông màu chủ đạo giống header */
                color: var(--white-color);
            }
        </style>
    </head>
    <body>
        <jsp:include page="components/navigation/navigation.jsp"/>

        <div class="container py-5"> <%-- Container với padding trên và dưới --%>
            <div class="row justify-content-center">
                <div class="col-lg-8 col-md-10">
                    <div class="card shadow-lg welcome-card"> <%-- Sử dụng Card component với đổ bóng --%>
                        <div class="card-header text-center py-3">
                            <h2 class="mb-0 fw-bold"><i class="fas fa-home me-2"></i>Chào mừng đến với EduPlan!</h2>
                        </div>
                        <div class="card-body p-4 text-center"> <%-- Căn giữa nội dung trong body --%>
                            <%-- Hiển thị thông báo truy cập bị từ chối --%>
                            <c:if test="${param.accessDenied eq 'true'}">
                                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                    <i class="fas fa-exclamation-triangle me-2"></i>Bạn không có quyền truy cập vào tài nguyên này!
                                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                                </div>
                            </c:if>

                            <c:if test="${not empty sessionScope.loggedInUser}">
                                <p class="lead">Xin chào, <span class="fw-bold text-primary">${sessionScope.loggedInUser.firstName} ${sessionScope.loggedInUser.lastName}</span> (<span class="fw-bold">${sessionScope.loggedInUser.username}</span>)!</p>
                                <p>Vai trò của bạn: <span class="badge bg-primary">${sessionScope.loggedInUser.role}</span></p>
                                
                                <div class="d-grid gap-2 col-md-6 mx-auto mt-4"> <%-- Nút hành động, căn giữa --%>
                                    <c:if test="${sessionScope.loggedInUser.role == 'Admin'}">
                                        <a href="${pageContext.request.contextPath}/user/dashboard" class="btn btn-primary btn-lg">
                                            <i class="fas fa-tachometer-alt me-2"></i>Đi tới Admin Dashboard
                                        </a>
                                    </c:if>
                                    <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-outline-danger btn-lg">
                                        <i class="fas fa-sign-out-alt me-2"></i>Đăng xuất
                                    </a>
                                </div>
                            </c:if>
                            
                            <c:if test="${empty sessionScope.loggedInUser}">
                                <p class="lead">Bạn chưa đăng nhập. Vui lòng đăng nhập hoặc đăng ký để tiếp tục.</p>
                                <div class="d-grid gap-2 col-md-6 mx-auto mt-4"> <%-- Nút hành động, căn giữa --%>
                                    <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-primary btn-lg">
                                        <i class="fas fa-sign-in-alt me-2"></i>Đăng nhập
                                    </a>
                                    <a href="${pageContext.request.contextPath}/auth/register" class="btn btn-outline-primary btn-lg">
                                        <i class="fas fa-user-plus me-2"></i>Đăng ký
                                    </a>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
             <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
    
             
    </body>
</html>