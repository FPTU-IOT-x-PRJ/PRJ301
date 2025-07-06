<!-- File: /components/quiz/quiz-add.jsp -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Thêm Quiz</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
</head>
<body>
<div class="container mt-5">
    <h2>Thêm Quiz cho môn: ${currentSubject.name}</h2>
    <form method="post" action="${pageContext.request.contextPath}/quizzes/add">
        <input type="hidden" name="subjectId" value="${subjectId}"/>

        <div class="mb-3">
            <label class="form-label">Tiêu đề Quiz</label>
            <input type="text" name="title" class="form-control" required />
        </div>

        <div class="mb-3">
            <label class="form-label">Mô tả Quiz</label>
            <textarea name="description" class="form-control" rows="3"></textarea>
        </div>

        <div id="questionContainer"></div>

        <input type="hidden" name="numQuestions" id="numQuestions" value="0"/>

        <button type="button" class="btn btn-outline-primary" onclick="addQuestion()">+ Thêm câu hỏi</button>
        <button type="submit" class="btn btn-success">Lưu Quiz</button>
    </form>
</div>

<script>
    let questionCount = 0;

    function addQuestion() {
        questionCount++;
        document.getElementById("numQuestions").value = questionCount;

        const container = document.getElementById("questionContainer");

        const html = `
            <div class="border p-3 my-3">
                <h5>Câu hỏi \${questionCount}</h5>
                <div class="mb-2">
                    <label class="form-label">Nội dung</label>
                    <textarea name="question_\${questionCount}" class="form-control" required></textarea>
                </div>
                <div class="mb-2">
                    <label class="form-label">Tùy chọn (ngăn cách bằng |)</label>
                    <input name="options_\${questionCount}" class="form-control" required />
                </div>
                <div class="mb-2">
                    <label class="form-label">Đáp án đúng (chỉ số, VD: 0|2)</label>
                    <input name="answers_\${questionCount}" class="form-control" required />
                </div>
            </div>
        `;

        container.insertAdjacentHTML('beforeend', html);
    }
</script>
</body>
</html>
