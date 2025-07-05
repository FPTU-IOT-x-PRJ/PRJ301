<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${totalPages > 0}">
    <nav aria-label="Page navigation" class="mt-4">
        <ul class="pagination justify-content-center">
            <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                <a class="page-link" href="${baseUrl}?page=${currentPage - 1}<c:forEach items='${paginationParams}' var='p'><c:if test='${not empty p.value}'>&${p.key}=${p.value}</c:if></c:forEach>" aria-label="Previous">
                    <i class="fas fa-chevron-left"></i>
                </a>
            </li>

            <c:forEach begin="1" end="${totalPages}" var="pageNum">
                <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                    <a class="page-link" href="${baseUrl}?page=${pageNum}<c:forEach items='${paginationParams}' var='p'>&${p.key}=${p.value}</c:forEach>">${pageNum}</a>
                </li>
            </c:forEach>

            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                <a class="page-link" href="${baseUrl}?page=${currentPage + 1}<c:forEach items='${paginationParams}' var='p'><c:if test='${not empty p.value}'>&${p.key}=${p.value}</c:if></c:forEach>" aria-label="Next">
                    <i class="fas fa-chevron-right"></i>
                </a>
            </li>
        </ul>
    </nav>
</c:if>
