<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
    <div class="container">
        <%-- Logo: Giữ icon cho EduPlan --%>
        <a class="navbar-brand fw-bold text-dark" href="${pageContext.request.contextPath}/">
            <i class="fas fa-graduation-cap me-2"></i>EduPlan
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarNav">
            <%-- Các mục bên trái (me-auto) --%>
            <ul class="navbar-nav me-auto">
                <c:if test="${sessionScope.loggedInUser.role == 'Admin'}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/user/dashboard">
                            <i class="fas fa-home me-1"></i>Dashboard
                        </a>
                    </li>
                </c:if>
                <%-- Gạch dọc dùng Bootstrap VR --%>
                <div class="vr mx-2"></div>

                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/semesters">
                        <i class="fas fa-calendar-alt me-1"></i>Kỳ học
                    </a>
                </li>
                <%-- Gạch dọc dùng Bootstrap VR --%>
                <div class="vr mx-2"></div>

                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/subjects">
                        <i class="fas fa-book me-1"></i>Môn học
                    </a>
                </li>
                <%-- Gạch dọc dùng Bootstrap VR --%>
                <div class="vr mx-2"></div>
                
                <%-- Thêm ô "Lịch học" với icon mới --%>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/calendar">
                        <i class="fas fa-chalkboard-teacher me-1"></i>Lịch học
                    </a>
                </li>
            </ul>

            <c:if test="${sessionScope.loggedInUser != null}">
                <%-- Các mục bên phải (ms-auto) --%>
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="fas fa-user-circle me-1"></i>
                            <c:out value="${sessionScope.user.firstName} ${sessionScope.user.lastName}" />
                        </a>
                        <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/user/profile">
                                <i class="fas fa-user me-2"></i>Hồ sơ
                            </a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/settings">
                                <i class="fas fa-cog me-2"></i>Cài đặt
                            </a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/auth/logout">
                                <i class="fas fa-sign-out-alt me-2"></i>Đăng xuất
                            </a></li>
                        </ul>
                    </li>
                </ul>
            </c:if>
                        
            <c:if test="${sessionScope.loggedInUser == null}">
                <ul class="navbar-nav ms-auto align-items-center">
                    <li class="nav-item me-2">
                        <a class="nav-link border rounded px-3 py-1 text-dark" href="${pageContext.request.contextPath}/auth/login">
                            <i class="fas fa-sign-in-alt me-1"></i>Đăng nhập
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link border rounded px-3 py-1 text-dark" href="${pageContext.request.contextPath}/auth/register">
                            <i class="fas fa-user-plus me-1"></i>Đăng ký
                        </a>
                    </li>
                </ul>
            </c:if>


                        
        </div>
    </div>
</nav>