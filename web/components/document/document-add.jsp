<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Tải lên Tài liệu Mới</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { padding-top: 20px; background-color: #f8f9fa; }
        .container { max-width: 600px; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }
        .form-group { margin-bottom: 15px; }
    </style>
</head>
<body>
    <div class="container">
        <h2 class="mb-4">Tải lên Tài liệu Mới</h2>

        <% if (request.getAttribute("errorMessage") != null) { %>
            <div class="alert alert-danger" role="alert">
                <%= request.getAttribute("errorMessage") %>
            </div>
        <% } %>

        <form action="${pageContext.request.contextPath}/documents/add" method="post" enctype="multipart/form-data">
            <div class="form-group">
                <label for="file">Chọn tệp tài liệu:</label>
                <input type="file" class="form-control" id="file" name="file" required>
                <small class="form-text text-muted">Hỗ trợ các định dạng: JPG, PNG, PDF, DOC, DOCX, XLSX. Kích thước tối đa 10MB.</small>
            </div>
            <div class="form-group">
                <label for="description">Mô tả tài liệu (tùy chọn):</label>
                <textarea class="form-control" id="description" name="description" rows="3" placeholder="Nhập mô tả tài liệu..."></textarea>
            </div>
            <button type="submit" class="btn btn-primary mt-3">Tải lên</button>
            <a href="${pageContext.request.contextPath}/documents/display" class="btn btn-secondary mt-3">Quay lại Danh sách</a>
        </form>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.7/dist/umd/popper.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.min.js"></script>
</body>
</html>