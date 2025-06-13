<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    int totalPages = (int) request.getAttribute("totalPages");
    int currentPage = (int) request.getAttribute("currentPage");
%>                
<c:if test="${totalPages > 0}">
    <nav aria-label="Page navigation" class="mt-4">
        <ul class="pagination justify-content-center">
            <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                <a class="page-link" href="?page=${currentPage - 1}&search=${param.search}&role=${param.role}&sort=${param.sort}" aria-label="Previous">
                    <i class="fas fa-chevron-left"></i>
                </a>
            </li>

            <c:forEach begin="1" end="${totalPages}" var="pageNum">
                <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                    <a class="page-link" href="?page=${pageNum}&search=${param.search}&role=${param.role}&sort=${param.sort}">${pageNum}</a>
                </li>
            </c:forEach>

            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                <a class="page-link" href="?page=${currentPage + 1}&search=${param.search}&role=${param.role}&sort=${param.sort}" aria-label="Next">
                    <i class="fas fa-chevron-right"></i>
                </a>
            </li>
        </ul>
    </nav>
</c:if>