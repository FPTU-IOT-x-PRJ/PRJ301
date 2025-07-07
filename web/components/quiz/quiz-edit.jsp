<%-- web/components/quiz/quiz-edit.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Chỉnh sửa Quiz</title>
    <%-- Include CSS/JS --%>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-4">
        <h2>Chỉnh sửa Quiz: ${quiz.title}</h2>
        <hr>
        <form action="${pageContext.request.contextPath}/quizzes/edit" method="post" id="quizForm">
            <input type="hidden" name="quizId" value="${quiz.id}">
            
            <div class="form-group">
                <label for="title">Tiêu đề Quiz</label>
                <input type="text" class="form-control" id="title" name="title" value="${quiz.title}" required>
            </div>
            <div class="form-group">
                <label for="description">Mô tả</label>
                <textarea class="form-control" id="description" name="description" rows="3">${quiz.description}</textarea>
            </div>

            <hr>
            <h4>Các câu hỏi</h4>
            <div id="questions-container">
                <%-- Load các câu hỏi đã có --%>
                <c:forEach var="question" items="${questions}" varStatus="qLoop">
                    <div class="card mb-3 question-block" id="question-${qLoop.index}">
                        <div class="card-header d-flex justify-content-between">
                            <span>Câu hỏi ${qLoop.count}</span>
                            <button type="button" class="btn btn-danger btn-sm remove-question-btn">Xóa</button>
                        </div>
                        <div class="card-body">
                            <div class="form-group">
                                <label>Nội dung câu hỏi</label>
                                <textarea class="form-control" name="questionText" required>${question.questionText}</textarea>
                            </div>
                            <div class="options-container">
                                <c:forEach var="option" items="${question.answerOptions}" varStatus="oLoop">
                                    <div class="input-group mb-2">
                                        <div class="input-group-prepend">
                                            <div class="input-group-text">
                                                <input type="radio" name="isCorrect_q${qLoop.index}" value="${oLoop.index}" ${option.isCorrect() ? 'checked' : ''} required>
                                            </div>
                                        </div>
                                        <input type="text" class="form-control" name="optionText_q${qLoop.index}" value="${option.optionText}" required>
                                        <div class="input-group-append">
                                            <button class="btn btn-outline-danger remove-option-btn" type="button">Xóa</button>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                            <button type="button" class="btn btn-info btn-sm add-option-btn">Thêm lựa chọn</button>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <button type="button" class="btn btn-success" id="add-question-btn"><i class="fas fa-plus"></i> Thêm câu hỏi</button>
            <hr>
            <button type="submit" class="btn btn-primary">Lưu thay đổi</button>
            <a href="${pageContext.request.contextPath}/quizzes/detail?id=${quiz.id}" class="btn btn-secondary">Hủy</a>
        </form>
    </div>

<script>
// Script này cần được điều chỉnh một chút so với trang add
document.addEventListener('DOMContentLoaded', function() {
    const questionsContainer = document.getElementById('questions-container');
    const addQuestionBtn = document.getElementById('add-question-btn');
    let questionIndex = ${questions.size()}; // Bắt đầu index từ số câu hỏi đã có

    // ... Toàn bộ script từ quiz-add.jsp được copy vào đây ...
    // ... Cần sửa lại cách đặt tên cho các trường input của câu hỏi mới ...
    // ví dụ `name="isCorrect_q${questionIndex}"` và `name="optionText_q${questionIndex}"`
    // để đảm bảo không trùng với các câu hỏi đã có.
    
    // Phần script copy từ quiz-add.jsp sẽ hoạt động gần như tương tự, 
    // chỉ cần đảm bảo biến `questionIndex` được khởi tạo đúng
    addQuestionBtn.addEventListener('click', function() {
            questionIndex++;
            const questionHtml = `
                <div class="card mb-3 question-block" id="question-${questionIndex}">
                    <div class="card-header d-flex justify-content-between">
                        <span>Câu hỏi ${questionIndex}</span>
                        <button type="button" class="btn btn-danger btn-sm remove-question-btn">Xóa</button>
                    </div>
                    <div class="card-body">
                        <div class="form-group">
                            <label>Nội dung câu hỏi</label>
                            <textarea class="form-control" name="questionText" required></textarea>
                        </div>
                        <div class="form-group">
                             <label>Loại câu hỏi</label>
                             <select class="form-control" name="questionType">
                                <option value="MULTIPLE_CHOICE">Trắc nghiệm nhiều lựa chọn</option>
                             </select>
                        </div>
                        <h5>Các lựa chọn trả lời</h5>
                        <div class="options-container">
                           <%-- Nơi chứa các lựa chọn --%>
                        </div>
                        <button type="button" class="btn btn-info btn-sm add-option-btn">Thêm lựa chọn</button>
                    </div>
                </div>
            `;
            questionsContainer.insertAdjacentHTML('beforeend', questionHtml);
        });

        // Event listener để xóa câu hỏi (delegation)
        questionsContainer.addEventListener('click', function(e) {
            if (e.target.classList.contains('remove-question-btn')) {
                e.target.closest('.question-block').remove();
            }
        });
        
        // Event listener để thêm lựa chọn (delegation)
        questionsContainer.addEventListener('click', function(e) {
            if (e.target.classList.contains('add-option-btn')) {
                const optionsContainer = e.target.previousElementSibling;
                const optionIndex = optionsContainer.children.length;
                const optionHtml = `
                    <div class="input-group mb-2">
                        <div class="input-group-prepend">
                            <div class="input-group-text">
                                <input type="radio" name="isCorrect_q${questionIndex}" value="${optionIndex}" required>
                            </div>
                        </div>
                        <input type="text" class="form-control" name="optionText_q${questionIndex}" placeholder="Nội dung lựa chọn" required>
                         <div class="input-group-append">
                            <button class="btn btn-outline-danger remove-option-btn" type="button">Xóa</button>
                        </div>
                    </div>
                `;
                optionsContainer.insertAdjacentHTML('beforeend', optionHtml);
            }
        });
         // Event listener để xóa lựa chọn (delegation)
        questionsContainer.addEventListener('click', function(e) {
            if (e.target.classList.contains('remove-option-btn')) {
                 e.target.closest('.input-group').remove();
            }
        });
});
</script>
</body>
</html>