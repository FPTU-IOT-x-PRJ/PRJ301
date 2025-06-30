<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Danh sách Tài liệu</title>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                padding-top: 20px;
                background-color: #f8f9fa;
            }
            .container {
                background-color: #ffffff;
                padding: 30px;
                border-radius: 8px;
                box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            }
            .action-buttons {
                display: flex;
                gap: 5px;
            }
        </style>
    </head>
    <body>
        <jsp:include page="../navigation/navigation.jsp" />

        <div class="container">
            <h2 class="mb-4">Danh sách Tài liệu của bạn</h2>

            <%-- Hiển thị thông báo thành công --%>
            <% if (request.getAttribute("message") != null) { %>
            <div class="alert alert-success" role="alert">
                <%= request.getAttribute("message") %>
            </div>
            <% } %>
            <%-- Hiển thị thông báo lỗi --%>
            <% if (request.getAttribute("errorMessage") != null) { %>
            <div class="alert alert-danger" role="alert">
                <%= request.getAttribute("errorMessage") %>
            </div>
            <% } %>

            <div class="mb-3">
                <a href="${pageContext.request.contextPath}/documents/new" class="btn btn-primary">Tải lên Tài liệu mới</a>
            </div>

            <c:if test="${empty listDocuments}">
                <div class="alert alert-info" role="alert">
                    Bạn chưa có tài liệu nào được tải lên.
                </div>
            </c:if>

            <c:if test="${not empty listDocuments}">
                <div class="table-responsive">
                    <table class="table table-striped table-hover">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Tên File Gốc</th>
                                <th>Mô tả</th>
                                <th>Kích thước</th>
                                <th>Kiểu File</th>
                                <th>Ngày tải lên</th>
                                <th>Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="doc" items="${listDocuments}">
                                <tr>
                                    <td>${doc.id}</td>
                                    <td><a href="${doc.filePath}" target="_blank">${doc.fileName}</a></td>
                                    <td>${doc.description}</td>
                                    <td>${doc.fileSize / (1024 * 1024) >= 1 ? String.format("%.2f MB", doc.fileSize / (1024.0 * 1024.0)) : String.format("%.2f KB", doc.fileSize / 1024.0)}</td>
                                    <td>${doc.fileType}</td>
                                    <td>${doc.uploadDate}</td>
                                    <td class="action-buttons">
                                        <a href="${pageContext.request.contextPath}/documents/edit?id=${doc.id}" class="btn btn-warning btn-sm">Sửa</a>
                                        <a href="${pageContext.request.contextPath}/documents/delete?id=${doc.id}" class="btn btn-danger btn-sm"
                                           onclick="return confirm('Bạn có chắc chắn muốn xóa tài liệu này không?');">Xóa</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.7/dist/umd/popper.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.min.js"></script>
    </body>
</html>