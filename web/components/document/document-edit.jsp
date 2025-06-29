<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Chỉnh sửa Tài liệu</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { padding-top: 20px; background-color: #f8f9fa; }
        .container { max-width: 600px; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }
        .form-group { margin-bottom: 15px; }
    </style>
</head>
<body>
    <div class="container">
        <h2 class="mb-4">Chỉnh sửa Tài liệu</h2>

        <c:if test="${document == null}">
            <div class="alert alert-warning" role="alert">
                Không tìm thấy tài liệu để chỉnh sửa hoặc bạn không có quyền.
            </div>
            <a href="${pageContext.request.contextPath}/documents/list" class="btn btn-secondary">Quay lại Danh sách</a>
        </c:if>

        <c:if test="${document != null}">
            <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="alert alert-danger" role="alert">
                    <%= request.getAttribute("errorMessage") %>
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/documents/update" method="post">
                <input type="hidden" name="id" value="${document.id}">
                
                <div class="form-group">
                    <label for="fileName">Tên file gốc:</label>
                    <input type="text" class="form-control" id="fileName" name="fileName" value="${document.fileName}" readonly>
                </div>
                
                <div class="form-group">
                    <label for="fileUrl">URL file:</label>
                    <input type="text" class="form-control" id="fileUrl" name="fileUrl" value="${document.filePath}" readonly>
                </div>
                
                <div class="form-group">
                    <label for="description">Mô tả tài liệu:</label>
                    <textarea class="form-control" id="description" name="description" rows="5" required>${document.description}</textarea>
                </div>
                
                <button type="submit" class="btn btn-primary mt-3">Lưu thay đổi</button>
                <a href="${pageContext.request.contextPath}/documents/detail?id=${document.id}" class="btn btn-secondary mt-3">Hủy</a>
            </form>
        </c:if>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.7/dist/umd/popper.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.min.js"></script>
</body>
</html>