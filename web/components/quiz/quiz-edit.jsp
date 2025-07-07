<%-- web/components/quiz/quiz-edit.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>Chỉnh sửa Quiz</title>
        <%-- Include CSS/JS --%>
        <jsp:include page="../navigation/navigation.jsp"/>

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
                                    <textarea class="form-control" name="questionText_${qLoop.index}" required>${question.questionText}</textarea>
                                </div>
                                <div class="form-group">
                                    <label>Loại câu hỏi</label>
                                    <select class="form-control" name="questionType_${qLoop.index}">
                                        <option value="MULTIPLE_CHOICE" ${question.questionType == 'MULTIPLE_CHOICE' ? 'selected' : ''}>Trắc nghiệm nhiều lựa chọn</option>
                                        <%-- Thêm các loại câu hỏi khác nếu có --%>
                                    </select>
                                </div>
                                <h5>Các lựa chọn trả lời</h5>
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
            document.addEventListener('DOMContentLoaded', function () {
                const questionsContainer = document.getElementById('questions-container');
                const addQuestionBtn = document.getElementById('add-question-btn');
                
                // Khởi tạo questionIndex dựa trên số lượng câu hỏi hiện có
                let questionIndex = questionsContainer.querySelectorAll('.question-block').length; 

                addQuestionBtn.addEventListener('click', function () {
                    // Cập nhật chỉ số hiển thị của câu hỏi mới
                    const currentQuestionDisplayIndex = questionsContainer.querySelectorAll('.question-block').length + 1;
                    
                    const questionHtml =
                            '<div class="card mb-3 question-block" id="question-' + questionIndex + '">' +
                            '<div class="card-header d-flex justify-content-between">' +
                            '<span>Câu hỏi ' + currentQuestionDisplayIndex + '</span>' +
                            '<button type="button" class="btn btn-danger btn-sm remove-question-btn">Xóa</button>' +
                            '</div>' +
                            '<div class="card-body">' +
                            '<div class="form-group">' +
                            '<label>Nội dung câu hỏi</label>' +
                            '<textarea class="form-control" name="questionText_' + questionIndex + '" required></textarea>' +
                            '</div>' +
                            '<div class="form-group">' +
                            '<label>Loại câu hỏi</label>' +
                            '<select class="form-control" name="questionType_' + questionIndex + '"> ' +
                            '<option value="MULTIPLE_CHOICE">Trắc nghiệm nhiều lựa chọn</option>' +
                            '</select>' +
                            '</div>' +
                            '<h5>Các lựa chọn trả lời</h5>' +
                            '<div class="options-container">' +
                            '</div>' +
                            '<button type="button" class="btn btn-info btn-sm add-option-btn">Thêm lựa chọn</button>' +
                            '</div>' +
                            '</div>';
                    questionsContainer.insertAdjacentHTML('beforeend', questionHtml);
                    
                    // Tăng questionIndex cho câu hỏi tiếp theo
                    questionIndex++; 
                    updateQuestionNumbers(); // Cập nhật lại số thứ tự và name attributes
                });

                // Event listener để xóa câu hỏi (delegation)
                questionsContainer.addEventListener('click', function (e) {
                    if (e.target.classList.contains('remove-question-btn')) {
                        e.target.closest('.question-block').remove();
                        updateQuestionNumbers(); // Cập nhật lại số thứ tự và name attributes
                    }
                });

                // Event listener để thêm lựa chọn (delegation)
                questionsContainer.addEventListener('click', function (e) {
                    if (e.target.classList.contains('add-option-btn')) {
                        const optionsContainer = e.target.previousElementSibling;
                        const optionIndex = optionsContainer.children.length; // Chỉ số cho lựa chọn mới

                        const currentQuestionBlock = e.target.closest('.question-block');
                        const currentQuestionDOMIndex = Array.from(questionsContainer.children).indexOf(currentQuestionBlock); // Lấy index trong DOM
                        
                        const optionHtml =
                                '<div class="input-group mb-2">' +
                                '<div class="input-group-prepend">' +
                                '<div class="input-group-text">' +
                                '<input type="radio" name="isCorrect_q' + currentQuestionDOMIndex + '" value="' + optionIndex + '" required>' +
                                '</div>' +
                                '</div>' +
                                '<input type="text" class="form-control" name="optionText_q' + currentQuestionDOMIndex + '" placeholder="Nội dung lựa chọn" required>' +
                                '<div class="input-group-append">' +
                                '<button class="btn btn-outline-danger remove-option-btn" type="button">Xóa</button>' +
                                '</div>' +
                                '</div>';
                        optionsContainer.insertAdjacentHTML('beforeend', optionHtml);
                    }
                });

                // Event listener để xóa lựa chọn (delegation)
                questionsContainer.addEventListener('click', function (e) {
                    if (e.target.classList.contains('remove-option-btn')) {
                        const optionGroup = e.target.closest('.input-group');
                        const optionsContainer = optionGroup.closest('.options-container');
                        optionGroup.remove();
                        // Sau khi xóa một lựa chọn, cần cập nhật lại giá trị 'value' của các radio button còn lại
                        // để đảm bảo chúng vẫn liên tục (0, 1, 2, ...)
                        Array.from(optionsContainer.children).forEach((child, index) => {
                            const radioInput = child.querySelector('input[type="radio"]');
                            if (radioInput) {
                                radioInput.value = index;
                            }
                        });
                    }
                });

                // Hàm cập nhật số thứ tự câu hỏi hiển thị và name attributes
                function updateQuestionNumbers() {
                    const questionBlocks = questionsContainer.querySelectorAll('.question-block');
                    questionBlocks.forEach((block, index) => {
                        const newIndex = index; // Chỉ mục mới dựa trên vị trí trong DOM
                        const displayIndex = index + 1; // Số thứ tự hiển thị

                        // Cập nhật ID của block câu hỏi
                        block.id = 'question-' + newIndex;

                        // Cập nhật số hiển thị của câu hỏi
                        const headerSpan = block.querySelector('.card-header span');
                        if (headerSpan) {
                            headerSpan.textContent = 'Câu hỏi ' + displayIndex;
                        }

                        // Cập nhật name attributes của textarea và select
                        const questionTextarea = block.querySelector('textarea[name^="questionText_"]');
                        if (questionTextarea) {
                            questionTextarea.name = 'questionText_' + newIndex;
                        }

                        const questionTypeSelect = block.querySelector('select[name^="questionType_"]');
                        if (questionTypeSelect) {
                            questionTypeSelect.name = 'questionType_' + newIndex;
                        }

                        // Cập nhật name attributes và value cho các lựa chọn trả lời
                        const optionRadioInputs = block.querySelectorAll('.options-container input[type="radio"]');
                        optionRadioInputs.forEach((radioInput, optionIdx) => {
                            radioInput.name = 'isCorrect_q' + newIndex;
                            radioInput.value = optionIdx; // Cập nhật lại value cho radio button
                        });

                        const optionTextInputs = block.querySelectorAll('.options-container input[type="text"]');
                        optionTextInputs.forEach(textInput => {
                            textInput.name = 'optionText_q' + newIndex;
                        });
                    });
                    // Điều chỉnh questionIndex sau khi cập nhật tất cả các khối
                    questionIndex = questionBlocks.length > 0 ? parseInt(questionBlocks[questionBlocks.length - 1].id.replace('question-', '')) + 1 : 0;
                }

                // Gọi lần đầu để đảm bảo các name attributes của câu hỏi ban đầu là đúng (nếu có)
                // và thiết lập questionIndex chính xác.
                updateQuestionNumbers();
            });
        </script>
    </body>
</html>