<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>404 - Không tìm thấy trang</title>
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
            max-width: 600px;
            width: 90%;
        }
        .error-code {
            font-size: 8em;
            font-weight: bold;
            color: var(--primary-color);
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
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-code">404</div>
        <div class="error-message">Không tìm thấy trang</div>
        <p class="error-description">
            Chúng tôi xin lỗi, nhưng trang bạn đang tìm kiếm có thể đã bị xóa, đổi tên hoặc không bao giờ tồn tại.
        </p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-home">Quay về Trang chủ</a>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>