<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Thêm Tài liệu mới</title>
        <!-- Bootstrap CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <!-- Font Awesome for icons -->
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
        <style>
            body {
                font-family: 'Inter', sans-serif;
                background-color: #f8f9fa;
            }
            .container {
                padding-top: 2rem;
                padding-bottom: 2rem;
            }
            .card {
                border-radius: 0.75rem;
                box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.05);
            }
            .form-label {
                font-weight: 500;
                color: #343a40;
            }
            .form-control, .form-select {
                border-radius: 0.375rem;
            }
            .form-control:focus, .form-select:focus {
                border-color: #80bdff;
                box-shadow: 0 0 0 0.25rem rgba(0, 123, 255, 0.25);
            }
            .btn-custom {
                padding: 0.625rem 1.25rem;
                border-radius: 0.375rem;
                font-weight: 500;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                transition: background-color 0.2s, transform 0.2s;
            }
            .btn-primary-custom {
                background-color: #007bff;
                color: white;
            }
            .btn-primary-custom:hover {
                background-color: #0056b3;
                transform: translateY(-1px);
            }
            .btn-secondary-custom {
                background-color: #6c757d;
                color: white;
            }
            .btn-secondary-custom:hover {
                background-color: #5a6268;
                transform: translateY(-1px);
            }
            .alert-custom {
                padding: 1rem 1.5rem;
                margin-bottom: 1.5rem;
                border-radius: 0.5rem;
                font-size: 0.95rem;
            }
            .alert-danger-custom {
                background-color: #f8d7da;
                color: #721c24;
                border-color: #f5c6cb;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <h1 class="mb-4 text-center text-primary">Thêm Tài liệu mới</h1>

            <c:if test="${not empty requestScope.errorMessage}">
                <div class="alert alert-danger-custom alert-custom" role="alert">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    <c:out value="${requestScope.errorMessage}"/>
                </div>
            </c:if>

            <div class="card">
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/documents/add" method="POST" enctype="multipart/form-data">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="file" class="form-label">File Tài liệu:</label>
                                    <input type="file" id="file" name="file" required class="form-control">
                                    <div class="form-text">Tối đa 10MB</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="description" class="form-label">Mô tả:</label>
                                    <textarea id="description" name="description" rows="3" class="form-control" placeholder="Thêm mô tả cho tài liệu của bạn..."></textarea>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="subjectId" class="form-label">Môn học (Tùy chọn):</label>
                                    <select id="subjectId" name="subjectId" class="form-select">
                                        <option value="">Không có môn học</option>
                                        <c:forEach var="subject" items="${subjects}">
                                            <option value="${subject.id}">${subject.name} (${subject.code})</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="lessonId" class="form-label">Buổi học (Tùy chọn):</label>
                                    <select id="lessonId" name="lessonId" class="form-select" disabled>
                                        <option value="">Chọn buổi học</option>
                                    </select>
                                    <div class="form-text">Chọn môn học trước để thấy danh sách buổi học.</div>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-end mt-4">
                            <a href="${pageContext.request.contextPath}/documents/display" class="btn btn-secondary-custom me-2">
                                <i class="fas fa-arrow-left me-2"></i>Hủy
                            </a>
                            <button type="submit" class="btn btn-primary-custom">
                                <i class="fas fa-upload me-2"></i>Tải lên Tài liệu
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- Bootstrap Bundle with Popper -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            // JavaScript để load lessons động khi chọn subject
            document.getElementById('subjectId').addEventListener('change', function() {
                const subjectId = this.value;
                const lessonSelect = document.getElementById('lessonId');
                
                // Clear existing lessons and disable if no subject is selected
                lessonSelect.innerHTML = '<option value="">Chọn buổi học</option>';
                lessonSelect.disabled = true;

                if (subjectId) {
                    fetch('${pageContext.request.contextPath}/documents/getLessonsBySubject?subjectId=' + subjectId)
                        .then(response => response.json())
                        .then(data => {
                            if (data.length > 0) {
                                data.forEach(lesson => {
                                    const option = document.createElement('option');
                                    option.value = lesson.id;
                                    option.textContent = lesson.name;
                                    lessonSelect.appendChild(option);
                                });
                                lessonSelect.disabled = false; // Enable if lessons are available
                            } else {
                                lessonSelect.innerHTML = '<option value="">Không có buổi học nào</option>';
                                lessonSelect.disabled = true;
                            }
                        })
                        .catch(error => {
                            console.error('Error fetching lessons:', error);
                            lessonSelect.innerHTML = '<option value="">Lỗi tải buổi học</option>';
                            lessonSelect.disabled = true;
                        });
                }
            });
        </script>
    </body>
</html>
