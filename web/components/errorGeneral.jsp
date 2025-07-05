<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Lỗi hệ thống</title>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
    <link href="${pageContext.request.contextPath}/index.css" rel="stylesheet" type="text/css"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background-color: var(--light-color); /* Sử dụng biến CSS từ index.css */
            color: var(--dark-color);
            text-align: center;
        }
        .error-container {
            background-color: var(--white-color);
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            max-width: 700px;
            width: 90%;
        }
        .error-code {
            font-size: 6em;
            font-weight: bold;
            color: var(--danger-color);
            margin-bottom: 0px;
        }
        .error-message {
            font-size: 1.8em;
            margin-bottom: 20px;
            color: var(--secondary-color);
        }
        .error-description {
            font-size: 1.1em;
            margin-bottom: 30px;
        }
        .btn-home {
            background-color: var(--dark-color);
            border-color: var(--dark-color);
            color: white;
            padding: 10px 25px;
            font-size: 1.1em;
            border-radius: 6px;
            transition: all 0.3s ease;
        }
        .btn-home:hover {
            background-color: #000000;
            border-color: #000000;
            transform: translateY(-1px);
        }
        .error-details {
            text-align: left;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid var(--border-color);
            font-size: 0.9em;
            color: var(--text-muted);
        }
        .error-details pre {
            background-color: var(--light-color);
            padding: 15px;
            border-radius: 5px;
            overflow-x: auto;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-code">Lỗi!</div>
        <div class="error-message">Đã xảy ra lỗi hệ thống</div>
        <p class="error-description">
            Rất tiếc, có vẻ như một sự cố đã xảy ra trên máy chủ của chúng tôi. Vui lòng thử lại sau hoặc liên hệ với quản trị viên nếu sự cố vẫn tiếp diễn.
        </p>

        <%-- Chỉ hiển thị thông tin chi tiết lỗi nếu trong môi trường phát triển --%>
        <% if (application.getInitParameter("environment") != null && application.getInitParameter("environment").equals("development")) { %>
            <div class="error-details">
                <h3>Chi tiết lỗi (Chỉ trong môi trường phát triển):</h3>
                <p><strong>Thông báo:</strong> <%= exception != null ? exception.getMessage() : "Không có thông báo lỗi cụ thể." %></p>
                <% if (exception != null) { %>
                    <p><strong>Loại lỗi:</strong> <%= exception.getClass().getName() %></p>
                    <p><strong>Stack Trace:</strong></p>
                    <pre><% exception.printStackTrace(new java.io.PrintWriter(out)); %></pre>
                <% } %>
            </div>
        <% } %>

        <a href="${pageContext.request.contextPath}/" class="btn btn-home">Quay về Trang chủ</a>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>