<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>404 - Không tìm thấy trang</title>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            /* Bỏ các thuộc tính flexbox ở đây */
            /* display: flex; */
            /* justify-content: center; */
            /* align-items: center; */
            min-height: 100vh; /* Giữ lại để body có chiều cao tối thiểu */
            background-color: var(--light-color);
            color: var(--dark-color);
            text-align: center;
            margin: 0; /* Đảm bảo không có margin mặc định */
            padding: 0; /* Đảm bảo không có padding mặc định */
            display: flex; /* Giữ lại flex để đẩy footer xuống cuối nếu có */
            flex-direction: column; /* Quan trọng: xếp các phần tử con theo chiều dọc */
        }
        .main-content-wrapper-404 { /* Tạo một wrapper mới cho nội dung chính */
            flex-grow: 1; /* Để nó chiếm hết không gian còn lại */
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px; /* Thêm padding nếu cần */
            box-sizing: border-box;
            width: 100%;
        }
        .error-container {
            background-color: var(--white-color);
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            max-width: 600px;
            width: 90%;
            /* Bỏ text-align ở đây vì đã có trên body */
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
    <jsp:include page="./navigation/navigation.jsp" />

    <div class="main-content-wrapper-404">
        <div class="error-container">
            <div class="error-code">404</div>
            <div class="error-message">Không tìm thấy trang</div>
            <p class="error-description">
                Chúng tôi xin lỗi, nhưng trang bạn đang tìm kiếm có thể đã bị xóa, đổi tên hoặc không bao giờ tồn tại.
            </p>
            <a href="${pageContext.request.contextPath}/" class="btn btn-home">Quay về Trang chủ</a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>