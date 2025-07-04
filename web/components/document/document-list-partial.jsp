<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<div class="list-group">
    <c:forEach var="document" items="${documents}">
        <div class="list-group-item d-flex justify-content-between align-items-center document-item">
            <div>
                <h6 class="mb-1 doc-title">${document.fileName}</h6>
                <p class="mb-1 doc-description text-muted">${document.description}</p>
               
            </div>
            <div class="d-flex flex-wrap justify-content-end">
                <a href="${pageContext.request.contextPath}/documents/detail?id=${document.id}" class="btn btn-sm btn-outline-info me-2 mb-1 rounded-pill">
                    <i class="fas fa-eye"></i> Xem
                </a>
                <a href="${pageContext.request.contextPath}/documents/download?id=${document.id}" class="btn btn-sm btn-outline-success me-2 mb-1 rounded-pill">
                    <i class="fas fa-download"></i> Tải xuống
                </a>
                <a href="${pageContext.request.contextPath}/documents/delete?id=${document.id}" class="btn btn-sm btn-outline-danger mb-1 rounded-pill">
                    <i class="fas fa-trash-alt"></i> Xóa
                </a>
            </div>
        </div>
    </c:forEach>
</div>
