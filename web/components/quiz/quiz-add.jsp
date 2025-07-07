<%-- web/components/quiz/quiz-add.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <jsp:include page="../navigation/navigation.jsp"/>

        <title>Thêm Quiz mới</title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    </head>
    <body>
        <%-- Include navigation của bạn --%>
        <div class="container mt-4">
            <h2>Thêm Quiz cho môn học: ${subject.name}</h2>
            <hr>
            <form action="${pageContext.request.contextPath}/quizzes/add" method="post" id="quizForm">
                <input type="hidden" id="subjectId" name="subjectId" value="${subject.id}">

                <%-- Thông tin chung của Quiz --%>
                <div class="form-group">
                    <label for="title">Tiêu đề Quiz</label>
                    <input type="text" class="form-control" id="title" name="title" required>
                </div>
                <div class="form-group">
                    <label for="description">Mô tả</label>
                    <textarea class="form-control" id="description" name="description" rows="3"></textarea>
                </div>

                <hr>
                <h4>Các câu hỏi</h4>
                <div id="questions-container">
                    <%-- Khu vực chứa các câu hỏi sẽ được thêm bằng JavaScript --%>
                </div>

                <button type="button" class="btn btn-success" id="add-question-btn">
                    <i class="fas fa-plus"></i> Thêm câu hỏi
                </button>
                <hr>
                <button type="submit" class="btn btn-primary">Lưu Quiz</button>
                <a href="${pageContext.request.contextPath}/subjects/detail?id=${subject.id}" class="btn btn-secondary">Hủy</a>
            </form>
        </div>

        <script>
            // JavaScript để thêm/xóa câu hỏi và lựa chọn một cách động
            document.addEventListener('DOMContentLoaded', function () {
                const questionsContainer = document.getElementById('questions-container');
                const addQuestionBtn = document.getElementById('add-question-btn');
                let questionIndex = 0;

                addQuestionBtn.addEventListener('click', function () {
                    questionIndex++;
                    const questionHtml =
                            '<div class="card mb-3 question-block" id="question-' + questionIndex + '">' +
                            '<div class="card-header d-flex justify-content-between">' +
                            '<span>Câu hỏi ' + questionIndex + '</span>' +
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
                });

                // Event listener để xóa câu hỏi (delegation)
                questionsContainer.addEventListener('click', function (e) {
                    if (e.target.classList.contains('remove-question-btn')) {
                        e.target.closest('.question-block').remove();
                    }
                });

                // Event listener để thêm lựa chọn (delegation)
                questionsContainer.addEventListener('click', function (e) {
                    if (e.target.classList.contains('add-option-btn')) {
                        const optionsContainer = e.target.previousElementSibling;
                        const questionBlock = e.target.closest('.question-block');
                        // Lấy chỉ số câu hỏi từ data attribute hoặc id của khối câu hỏi
                        // Đảm bảo questionIndex này khớp với index đã dùng để đặt tên questionText
                        const currentQuestionIndex = questionBlock.id.replace('question-', '');

                        const optionIndex = optionsContainer.children.length; // Chỉ số cho lựa chọn
                        // Hoặc thử cách này:
                        const optionHtml = '<div class="input-group mb-2">' +
                                '<div class="input-group-prepend">' +
                                '<div class="input-group-text">' +
                                '<input type="radio" name="isCorrect_q' + currentQuestionIndex + '" value="' + optionIndex + '" required>' +
                                '</div>' +
                                '</div>' +
                                '<input type="text" class="form-control" name="optionText_q' + currentQuestionIndex + '" placeholder="Nội dung lựa chọn" required>' +
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
                        e.target.closest('.input-group').remove();
                    }
                });

            });
        </script>
    </body>
</html>