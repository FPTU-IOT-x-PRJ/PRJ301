<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
    <div class="container">
        <%-- Logo đã sát trái theo mặc định trong container --%>
        <a class="navbar-brand fw-bold text-dark" href="${pageContext.request.contextPath}/">
            <i class="fas fa-graduation-cap me-2"></i>EduPlan
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarNav">
            <%-- me-auto đẩy các mục này sang trái (còn lại sẽ sang phải) --%>
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/user/dashboard">
                        <i class="fas fa-home me-1"></i>Dashboard
                    </a>
                </li>

                <c:if test="${sessionScope.user.role == 'admin'}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/users">
                            <i class="fas fa-users me-1"></i>Quản lý User
                        </a>
                    </li>
                </c:if>

                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/semesters">
                        <i class="fas fa-calendar-alt me-1"></i>Kỳ học
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/subjects">
                        <i class="fas fa-book me-1"></i>Môn học
                    </a>
                </li>
            </ul>

            <%-- ms-auto đẩy các mục này sang phải --%>
            <ul class="navbar-nav ms-auto">
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="fas fa-user-circle me-1"></i>
                        <c:out value="${sessionScope.user.firstName} ${sessionScope.user.lastName}" />
                    </a>
                    <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
                        <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile">
                            <i class="fas fa-user me-2"></i>Hồ sơ
                        </a></li>
                        <li><a class="dropdown-item" href="${pageContext.request.contextPath}/settings">
                            <i class="fas fa-cog me-2"></i>Cài đặt
                        </a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="${pageContext.request.contextPath}/logout">
                            <i class="fas fa-sign-out-alt me-2"></i>Đăng xuất
                        </a></li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</nav>