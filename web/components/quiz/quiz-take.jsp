<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Làm bài Quiz: ${quiz.title}</title>
        <jsp:include page="../navigation/navigation.jsp"/>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background-color: #f8f9fa;
            }
            .quiz-container {
                max-width: 800px;
                margin: 40px auto;
                padding: 30px;
                background-color: #ffffff;
                border-radius: 8px;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            }
            .question-card {
                margin-bottom: 25px;
                border: 1px solid #e0e0e0;
                border-radius: 8px;
                overflow: hidden;
            }
            .question-card .card-header {
                background-color: #007bff;
                color: white;
                font-weight: bold;
                padding: 15px 20px;
                border-bottom: 1px solid #0056b3;
            }
            .question-card .card-body {
                padding: 20px;
            }
            .option-item {
                margin-bottom: 10px;
                padding: 10px 15px;
                border: 1px solid #ddd;
                border-radius: 5px;
                cursor: pointer;
                transition: background-color 0.2s ease;
            }
            .option-item:hover {
                background-color: #f0f0f0;
            }
            .option-item input[type="radio"] {
                margin-right: 10px;
            }
            .timer-box {
                background-color: #dc3545;
                color: white;
                padding: 10px 15px;
                border-radius: 5px;
                font-size: 1.2rem;
                font-weight: bold;
                text-align: center;
                margin-bottom: 20px;
            }
            .submit-btn-container {
                text-align: center;
                margin-top: 30px;
            }
        </style>
    </head>
    <body>
        <div class="container quiz-container">
            <h2 class="text-center mb-4">Làm bài: ${quiz.title}</h2>
            <p class="text-muted text-center">${quiz.description}</p>

            <div class="timer-box">
                Thời gian còn lại: <span id="demnguoc"></span>
            </div>

            <form id="quizForm" action="${pageContext.request.contextPath}/quizzes/take" method="POST">
                <input type="hidden" name="quizId" value="${quiz.id}">
                <%
                    long currentTime = System.currentTimeMillis();
                    request.setAttribute("startTimeMillis", currentTime);
                %>
                <input type="hidden" name="startTimeMillis" id="startTimeMillis" value="${startTimeMillis}">

                <c:if test="${empty questions}">
                    <div class="alert alert-warning text-center" role="alert">
                        Quiz này chưa có câu hỏi nào.
                    </div>
                </c:if>

                <c:if test="${not empty questions}">
                    <c:forEach var="question" items="${questions}" varStatus="qLoop">
                        <div class="card question-card">
                            <div class="card-header">
                                Câu ${qLoop.index + 1}: ${question.questionText}
                            </div>
                            <div class="card-body">
                                <div class="options-group">
                                    <c:if test="${empty question.answerOptions}">
                                        <p class="text-muted">Chưa có lựa chọn nào cho câu hỏi này.</p>
                                    </c:if>
                                    <c:forEach var="option" items="${question.answerOptions}" varStatus="oLoop">
                                        <div class="form-check option-item">
                                            <input class="form-check-input" type="radio" 
                                                   name="question_${question.id}" 
                                                   id="question_${question.id}_option_${option.id}" 
                                                   value="${option.id}" required>
                                            <label class="form-check-label w-100" for="question_${question.id}_option_${option.id}">
                                                ${option.optionText}
                                            </label>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                    </c:forEach>

                    <div class="submit-btn-container">
                        <button type="submit" class="btn btn-primary btn-lg" id="submitQuizBtn">Nộp bài</button>
                    </div>
                </c:if>
            </form>
        </div>D

        <script>
            // Timer logic
            const quizDurationMinutes = parseInt('${quizDuration}');
//            console.log('Giá trị quizDurationMinutes từ JSP:', quizDurationMinutes);

            if (isNaN(quizDurationMinutes) || quizDurationMinutes <= 0) {
                console.error("Lỗi: Thời gian làm bài không hợp lệ hoặc không được thiết lập. Đảm bảo 'quizDuration' được truyền từ Controller.");
                document.getElementById('demnguoc').innerHTML = "Lỗi thời gian!";
                document.getElementById('submitQuizBtn').disabled = true;
            }

            const quizDurationMillis = quizDurationMinutes * 60 * 1000;
//            console.log('Tổng thời gian làm bài (millisecs):', quizDurationMillis);

            const startTimeMillisString = document.getElementById('startTimeMillis').value;
//            console.log('Thời gian bắt đầu (startTimeMillisString):', startTimeMillisString);
            const startTimeMillis = parseInt(startTimeMillisString);
//            console.log('Thời gian bắt đầu (startTimeMillis parsed):', startTimeMillis);

            if (isNaN(startTimeMillis)) {
                console.error("Lỗi: Giá trị startTimeMillis không hợp lệ.");
                document.getElementById('demnguoc').innerHTML = "Lỗi khởi tạo thời gian!";
                document.getElementById('submitQuizBtn').disabled = true;
            }

            const endTimeMillis = startTimeMillis + quizDurationMillis;
            console.log('Thời gian kết thúc (endTimeMillis):', endTimeMillis);

            if (isNaN(endTimeMillis)) {
                console.error("Lỗi: Giá trị endTimeMillis không hợp lệ, có thể do quizDurationMillis hoặc startTimeMillis lỗi.");
                document.getElementById('demnguoc').innerHTML = "Lỗi tính toán thời gian kết thúc!";
                document.getElementById('submitQuizBtn').disabled = true;
            }

            const demnguocElement = document.getElementById('demnguoc');
            const quizForm = document.getElementById('quizForm');
            const submitQuizBtn = document.getElementById('submitQuizBtn');

            function updateCountdown() {
                const now = new Date().getTime();
                const timeLeft = endTimeMillis - now;

//                console.log('Inside updateCountdown - timeLeft:', timeLeft);
//                console.log('Inside updateCountdown - Type of timeLeft:', typeof timeLeft);

                if (timeLeft <= 0 || isNaN(timeLeft)) {
                    demnguocElement.innerHTML = "Hết giờ!";
                    submitQuizBtn.disabled = true;
                    clearInterval(timerInterval);
                    if (!isNaN(timeLeft) && timeLeft <= 0) {
                        alert("Đã hết thời gian làm bài. Hệ thống sẽ tự động nộp bài của bạn.");
                        quizForm.submit();
                    }
                } else {
                    const mins = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));
                    const secs = Math.floor((timeLeft % (1000 * 60)) / 1000);

//                    console.log('Inside updateCountdown - Calculated mins:', mins);
//                    console.log('Inside updateCountdown - Calculated secs:', secs);

                    // Sử dụng string concatenation thay vì template literal
                    const timeDisplay = mins + " phút " + secs + " giây";

//                    console.log('Time display string:', timeDisplay);
//                    console.log('mins type:', typeof mins, 'secs type:', typeof secs);

                    demnguocElement.innerHTML = timeDisplay;

//                    console.log('DOM element content after update:', demnguocElement.innerHTML);
//                    console.log('DOM element exists:', !!demnguocElement);
                }
            }

            const timerInterval = setInterval(updateCountdown, 1000);
            updateCountdown(); // Gọi ngay lập tức để tránh độ trễ ban đầu

            // Ngăn chặn việc nộp bài ngẫu nhiên nếu người dùng điều hướng hoặc làm mới
            window.onbeforeunload = function () {
                return "Bạn có chắc muốn rời khỏi trang này? Bài làm của bạn có thể không được lưu.";
            };

            // Xóa onbeforeunload khi form được nộp bài bình thường
            quizForm.addEventListener('submit', function () {
                window.onbeforeunload = null;
            });
        </script>
    </body>
</html>