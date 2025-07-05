<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:forEach var="lesson" items="${lessons}">
    <div class="col-md-4 mb-4">
        <div class="card h-100 lesson-card">
            <div class="card-body d-flex flex-column">
                <h5 class="card-title">${lesson.name}</h5>
                <h6 class="card-subtitle mb-2 text-muted">
                    <fmt:formatDate value="${lesson.lessonDate}" pattern="dd/MM/yyyy"/>
                    <c:choose>
                        <c:when test="${lesson.status eq 'Active'}">
                            <span class="badge bg-secondary ms-2">Chưa học</span>
                        </c:when>
                        <c:when test="${lesson.status eq 'Completed'}">
                            <span class="badge badge-completed ms-2">Hoàn thành</span>
                        </c:when>
                        <c:when test="${lesson.status eq 'Inactive'}">
                            <span class="badge badge-cancelled ms-2">Vắng</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-secondary ms-2">${lesson.status}</span>
                        </c:otherwise>
                    </c:choose>
                </h6>
                <p class="card-text flex-grow-1">${lesson.description != null && lesson.description.length() > 100 ? lesson.description.substring(0, 100).concat("...") : lesson.description}</p>
                <div class="mt-auto d-flex justify-content-between align-items-center">
                    <a href="${pageContext.request.contextPath}/lessons/detail?id=${lesson.id}" class="btn btn-sm btn-outline-info rounded-pill">
                        <i class="fas fa-info-circle me-1"></i>Chi tiết
                    </a>
                    <div>
                        <a href="${pageContext.request.contextPath}/lessons/edit?id=${lesson.id}" class="btn btn-sm btn-outline-primary me-2 rounded-pill">
                            <i class="fas fa-edit"></i> Sửa
                        </a>
                        <a href="${pageContext.request.contextPath}/lessons/delete-confirm?id=${lesson.id}" class="btn btn-sm btn-outline-danger rounded-pill">
                            <i class="fas fa-trash-alt"></i> Xóa
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</c:forEach>
