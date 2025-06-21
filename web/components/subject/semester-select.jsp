<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chọn học kỳ - EduPlan</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .form-container {
            margin-top: 100px;
        }
    </style>
</head>
<body>
<jsp:include page="../navigation/navigation.jsp"/>

<div class="container form-container">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white">
                    <h5 class="mb-0"><i class="fas fa-filter me-2"></i>Chọn học kỳ để xem môn học</h5>
                </div>
                <div class="card-body">
                    <form method="get" action="${pageContext.request.contextPath}/subjects">
                        <div class="mb-3">
                            <label for="semesterId" class="form-label">Học kỳ <span class="text-danger">*</span></label>
                            <select id="semesterId" name="semesterId" class="form-select" required>
                                <option value="" disabled selected>-- Chọn học kỳ --</option>
                                <c:forEach var="semester" items="${semesters}">
                                    <option value="${semester.id}">${semester.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="d-grid">
                            <button type="submit" class="btn btn-success">
                                <i class="fas fa-eye me-2"></i>Xem danh sách môn học
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
</body>
</html>
