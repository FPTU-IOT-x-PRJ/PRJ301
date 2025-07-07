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