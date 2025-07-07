<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<c:if test="${empty quizzes}">
    <div class="alert alert-info text-center" role="alert">
        Chưa có quiz nào cho môn này.
    </div>
</c:if>

<c:if test="${not empty quizzes}">
    <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
        <c:forEach var="quiz" items="${quizzes}">
            <div class="col">
                <div class="card h-100 quiz-card">
                    <div class="card-body d-flex align-items-center">
                        <i class="fas fa-question-circle me-3 text-primary fa-2x"></i>
                        <h5 class="card-title mb-0">${quiz.title}</h5>
                    </div>
                    <div class="card-footer d-flex justify-content-end bg-transparent border-top-0">
                        <%-- Nút "Làm bài" mới, kích hoạt modal --%>
                        <button type="button" class="btn btn-success btn-sm rounded-pill me-2" 
                                data-bs-toggle="modal" data-bs-target="#quizDurationModal" 
                                data-quiz-id="${quiz.id}">
                            <i class="fas fa-play me-1"></i> Làm bài
                        </button>
                        <%-- Các nút hiện có --%>
                        <a href="${pageContext.request.contextPath}/quizzes/detail?id=${quiz.id}" class="btn btn-outline-info btn-sm rounded-pill me-2">Chi tiết</a>
                        <a href="${pageContext.request.contextPath}/quizzes/edit?id=${quiz.id}" class="btn btn-outline-warning btn-sm rounded-pill me-2">Sửa</a>
                        <a href="${pageContext.request.contextPath}/quizzes/delete-confirm?id=${quiz.id}" class="btn btn-outline-danger btn-sm rounded-pill">Xóa</a>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</c:if>

<style>
    /* CSS bổ sung cho phần quiz card, có thể đặt trong file CSS chính hoặc <style> của trang chi tiết môn học */
    .quiz-card {
        border-radius: 10px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
    }
    .quiz-card:hover {
        transform: translateY(-3px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    .quiz-card .card-body {
        padding-bottom: 10px; /* Giảm padding dưới để có khoảng cách tốt hơn với footer */
    }
    .quiz-card .card-title {
        font-size: 1.1rem;
        font-weight: 600;
        color: #343a40;
    }
    .quiz-card .card-footer {
        padding-top: 5px; /* Giảm padding trên của footer */
        padding-bottom: 15px; /* Giữ padding dưới của footer */
    }
</style>

<div class="modal fade" id="quizDurationModal" tabindex="-1" aria-labelledby="quizDurationModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="startQuizForm" action="${pageContext.request.contextPath}/quizzes/take" method="GET">
                <div class="modal-header">
                    <h5 class="modal-title" id="quizDurationModalLabel">Chọn thời gian làm bài</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="id" id="modalQuizId">
                    <div class="mb-3">
                        <label for="quizDurationSelect" class="form-label">Thời gian (phút):</label>
                        <select class="form-select" id="quizDurationSelect" name="duration" required>
                            <option value="5">5 phút</option>
                            <option value="10" selected>10 phút</option>
                            <option value="15">15 phút</option>
                            <option value="20">20 phút</option>
                            <option value="30">30 phút</option>
                            <option value="45">45 phút</option>
                            <option value="60">60 phút</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-primary">Bắt đầu Quiz</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const quizDurationModal = document.getElementById('quizDurationModal');
    quizDurationModal.addEventListener('show.bs.modal', event => {
        // Button that triggered the modal
        const button = event.relatedTarget;
        // Extract info from data-bs-* attributes
        const quizId = button.getAttribute('data-quiz-id');
        // Update the modal's content.
        const modalQuizIdInput = quizDurationModal.querySelector('#modalQuizId');
        modalQuizIdInput.value = quizId;
    });
</script>