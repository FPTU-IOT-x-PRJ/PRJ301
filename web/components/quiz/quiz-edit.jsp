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
                let questionIndex = ${questions.size()}; // Bắt đầu index từ số câu hỏi đã có

                addQuestionBtn.addEventListener('click', function () {
                    // Tăng questionIndex trước khi sử dụng để đảm bảo chỉ số mới
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
                                    <textarea class="form-control" name="questionText_${questionIndex}" required></textarea>
                                </div>
                                <div class="form-group">
                                    <label>Loại câu hỏi</label>
                                    <select class="form-control" name="questionType_${questionIndex}">
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
                    // Cập nhật lại số thứ tự cho các câu hỏi sau khi thêm/xóa để hiển thị đúng
                    updateQuestionNumbers();
                });

                // Event listener để xóa câu hỏi (delegation)
                questionsContainer.addEventListener('click', function (e) {
                    if (e.target.classList.contains('remove-question-btn')) {
                        e.target.closest('.question-block').remove();
                        // Cập nhật lại số thứ tự cho các câu hỏi sau khi thêm/xóa để hiển thị đúng
                        updateQuestionNumbers();
                    }
                });

                // Event listener để thêm lựa chọn (delegation)
                questionsContainer.addEventListener('click', function (e) {
                    if (e.target.classList.contains('add-option-btn')) {
                        const optionsContainer = e.target.previousElementSibling;
                        const optionIndex = optionsContainer.children.length; // Chỉ số cho lựa chọn mới

                        // Lấy chỉ số câu hỏi từ thẻ cha `.question-block`
                        const currentQuestionBlock = e.target.closest('.question-block');
                        const currentQuestionId = currentQuestionBlock.id.replace('question-', ''); // Lấy "X" từ "question-X"
                        
                        // ĐÃ SỬA: Chỉ thêm HTML cho lựa chọn, không bao gồm thẻ select loại câu hỏi
                        const optionHtml = `
                            <div class="input-group mb-2">
                                <div class="input-group-prepend">
                                    <div class="input-group-text">
                                        <input type="radio" name="isCorrect_q${currentQuestionId}" value="${optionIndex}" required>
                                    </div>
                                </div>
                                <input type="text" class="form-control" name="optionText_q${currentQuestionId}" placeholder="Nội dung lựa chọn" required>
                                <div class="input-group-append">
                                    <button class="btn btn-outline-danger remove-option-btn" type="button">Xóa</button>
                                </div>
                            </div>
                        `;
                        optionsContainer.insertAdjacentHTML('beforeend', optionHtml);
                    }
                });
                
                // Event listener để xóa lựa chọn (delegation)
                questionsContainer.addEventListener('click', function (e) {
                    if (e.target.classList.contains('remove-option-btn')) {
                        e.target.closest('.input-group').remove();
                    }
                });

                // Hàm cập nhật số thứ tự câu hỏi hiển thị và name attributes
                function updateQuestionNumbers() {
                    const questionBlocks = questionsContainer.querySelectorAll('.question-block');
                    questionBlocks.forEach((block, index) => {
                        const newIndex = index; // Bắt đầu từ 0 cho chỉ mục, tương ứng với qLoop.index
                        const displayIndex = index + 1; // Bắt đầu từ 1 cho hiển thị
                        
                        // Cập nhật ID của block câu hỏi
                        block.id = `question-${newIndex}`;

                        // Cập nhật số hiển thị của câu hỏi
                        const headerSpan = block.querySelector('.card-header span');
                        if (headerSpan) {
                            headerSpan.textContent = `Câu hỏi ${displayIndex}`;
                        }

                        // Cập nhật name attributes bên trong câu hỏi
                        const questionTextarea = block.querySelector('textarea[name^="questionText_"]');
                        if (questionTextarea) {
                            questionTextarea.name = `questionText_${newIndex}`;
                        }

                        const questionTypeSelect = block.querySelector('select[name^="questionType_"]');
                        if (questionTypeSelect) {
                            questionTypeSelect.name = `questionType_${newIndex}`;
                        }
                        
                        // Cập nhật name attributes cho các lựa chọn trả lời
                        // Lấy tất cả các input radio và text option trong options-container của từng câu hỏi
                        const optionInputs = block.querySelectorAll('.options-container input[type="radio"], .options-container input[type="text"]');
                        let optionCounter = 0; // Đếm chỉ mục cho các lựa chọn trong câu hỏi hiện tại
                        optionInputs.forEach(input => {
                            if (input.type === 'radio') {
                                input.name = `isCorrect_q${newIndex}`;
                                input.value = optionCounter; // Cập nhật lại value cho radio button để phản ánh đúng index mới
                            } else if (input.type === 'text') {
                                input.name = `optionText_q${newIndex}`;
                            }
                            // Tăng bộ đếm chỉ mục nếu là radio hoặc text option (chỉ 1 trong 2 được tăng mỗi lần lặp qua 1 cặp input)
                            if (input.type === 'radio') { // Hoặc input.name.startsWith('optionText_')
                                optionCounter++;
                            }
                        });
                    });
                    // Đặt lại questionIndex cho lần thêm câu hỏi tiếp theo
                    // Tìm index lớn nhất hiện có hoặc bằng tổng số câu hỏi
                    questionIndex = questionBlocks.length > 0 ? parseInt(questionBlocks[questionBlocks.length - 1].id.replace('question-', '')) + 1 : 0;
                }
                // Gọi lần đầu để đảm bảo tính nhất quán sau khi DOM được tải
                updateQuestionNumbers();
                
                const quizForm = document.getElementById('quizForm');
                if (quizForm) {
                    quizForm.addEventListener('submit', function(e) {
                        console.log('=== EDIT QUIZ FORM SUBMIT DEBUG ===');

                        const formData = new FormData(quizForm);
                        console.log('Form data:');
                        for (let [key, value] of formData.entries()) {
                            console.log(`${key}: ${value}`);
                        }

                        // Kiểm tra câu hỏi
                        const questionBlocks = document.querySelectorAll('.question-block');
                        console.log(`Total questions: ${questionBlocks.length}`);

                        questionBlocks.forEach((block, index) => {
                            const questionId = block.id.replace('question-', '');
                            const questionText = block.querySelector(`textarea[name="questionText_${questionId}"]`);
                            const options = block.querySelectorAll(`input[name="optionText_q${questionId}"]`);
                            const correctAnswer = block.querySelector(`input[name="isCorrect_q${questionId}"]:checked`);

                            console.log(`Question ${index + 1} (ID: ${questionId}):`);
                            console.log(`- Text: ${questionText ? questionText.value : 'NOT FOUND'}`);
                            console.log(`- Options: ${options.length}`);
                            console.log(`- Correct answer: ${correctAnswer ? correctAnswer.value : 'NONE'}`);

                            // Kiểm tra tất cả radio buttons
                            const allRadios = block.querySelectorAll(`input[name="isCorrect_q${questionId}"]`);
                            allRadios.forEach((radio, radioIndex) => {
                                console.log(`  Radio ${radioIndex}: value=${radio.value}, checked=${radio.checked}`);
                            });
                        });
                        console.log('=== END DEBUG ===');
                    });
                }
            });
        </script>
    </body>
</html>