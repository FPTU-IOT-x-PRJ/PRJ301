<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Quản Lý Buổi Học của Môn ${requestScope.subject.name}</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <style>
            .container {
                margin-top: 20px;
            }
            .table th, .table td {
                vertical-align: middle;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <h1 class="mb-4">
                Buổi Học của Môn: <span class="text-primary">${requestScope.subject.name} (${requestScope.subject.code})</span>
                <a href="${pageContext.request.contextPath}/subjects?semesterId=${requestScope.subject.semesterId}" class="btn btn-secondary btn-sm float-end">
                    <i class="fas fa-arrow-left"></i> Quay lại Môn học
                </a>
            </h1>

            <c:if test="${not empty requestScope.errorMessage}">
                <div class="alert alert-danger" role="alert">
                    ${requestScope.errorMessage}
                </div>
            </c:if>
            <c:if test="${param.message == 'addSuccess'}">
                <div class="alert alert-success" role="alert">
                    Thêm buổi học thành công!
                </div>
            </c:if>
            <c:if test="${param.message == 'editSuccess'}">
                <div class="alert alert-success" role="alert">
                    Cập nhật buổi học thành công!
                </div>
            </c:if>
            <c:if test="${param.message == 'deleteSuccess'}">
                <div class="alert alert-success" role="alert">
                    Xóa buổi học thành công!
                </div>
            </c:if>

            <div class="card mb-3">
                <div class="card-header">Tìm kiếm & Lọc</div>
                <div class="card-body">
                    <form class="row g-3" action="${pageContext.request.contextPath}/lessons" method="get">
                        <input type="hidden" name="subjectId" value="${requestScope.subject.id}">
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="search" placeholder="Tìm kiếm theo tên hoặc mô tả" value="${requestScope.search != null ? requestScope.search : ''}">
                        </div>
                        <div class="col-md-4">
                            <select class="form-select" name="status">
                                <option value="">Tất cả trạng thái</option>
                                <option value="Planned" ${requestScope.statusFilter == 'Planned' ? 'selected' : ''}>Kế hoạch</option>
                                <option value="Completed" ${requestScope.statusFilter == 'Completed' ? 'selected' : ''}>Hoàn thành</option>
                                <option value="Cancelled" ${requestScope.statusFilter == 'Cancelled' ? 'selected' : ''}>Hủy bỏ</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <button type="submit" class="btn btn-primary me-2">Tìm kiếm</button>
                            <a href="${pageContext.request.contextPath}/lessons?subjectId=${requestScope.subject.id}" class="btn btn-outline-secondary">Xóa lọc</a>
                        </div>
                    </form>
                </div>
            </div>

            <a href="${pageContext.request.contextPath}/lessons/add?subjectId=${requestScope.subject.id}" class="btn btn-success mb-3">
                <i class="fas fa-plus-circle"></i> Thêm Buổi Học Mới
            </a>

            <c:if test="${empty requestScope.lessons}">
                <div class="alert alert-info" role="alert">
                    Chưa có buổi học nào cho môn học này.
                </div>
            </c:if>

            <c:if test="${not empty requestScope.lessons}">
                <div class="table-responsive">
                    <table class="table table-bordered table-striped table-hover">
                        <thead class="table-dark">
                            <tr>
                                <th>ID</th>
                                <th>Tên Buổi Học</th>
                                <th>Ngày Học</th>
                                <th>Mô Tả</th>
                                <th>Trạng Thái</th>
                                <th>Tạo Lúc</th>
                                <th>Cập Nhật Lúc</th>
                                <th>Hành Động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="lesson" items="${requestScope.lessons}">
                                <tr>
                                    <td>${lesson.id}</td>
                                    <td>${lesson.name}</td>
                                    <td>${lesson.lessonDate}</td>
                                    <td>${lesson.description}</td>
                                    <td>${lesson.status}</td>
                                    <td>${lesson.createdAt}</td>
                                    <td>${lesson.updatedAt}</td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/lessons/edit?id=${lesson.id}&subjectId=${requestScope.subject.id}" class="btn btn-warning btn-sm me-2">
                                            <i class="fas fa-edit"></i> Sửa
                                        </a>
                                        <a href="#" class="btn btn-danger btn-sm"
                                           onclick="confirmDelete(${lesson.id}, '${requestScope.subject.id}', '${lesson.name}');">
                                            <i class="fas fa-trash-alt"></i> Xóa
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <nav aria-label="Page navigation">
                    <ul class="pagination justify-content-center">
                        <li class="page-item ${requestScope.currentPage == 1 ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/lessons?subjectId=${requestScope.subject.id}&page=${requestScope.currentPage - 1}&search=${requestScope.search}&status=${requestScope.statusFilter}">Trước</a>
                        </li>
                        <c:forEach begin="1" end="${requestScope.totalPages}" var="i">
                            <li class="page-item ${requestScope.currentPage == i ? 'active' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/lessons?subjectId=${requestScope.subject.id}&page=${i}&search=${requestScope.search}&status=${requestScope.statusFilter}">${i}</a>
                            </li>
                        </c:forEach>
                        <li class="page-item ${requestScope.currentPage == requestScope.totalPages ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/lessons?subjectId=${requestScope.subject.id}&page=${requestScope.currentPage + 1}&search=${requestScope.search}&status=${requestScope.statusFilter}">Tiếp</a>
                        </li>
                    </ul>
                </nav>
            </c:if>
        </div>

        <script src="https://kit.fontawesome.com/a076d05399.js" crossorigin="anonymous"></script>
        <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.8/dist/umd/popper.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.min.js"></script>

        <script>
            function confirmDelete(lessonId, subjectId, lessonName) {
                if (confirm(`Bạn có chắc chắn muốn xóa buổi học "${lessonName}" không?`)) {
                    window.location.href = "${pageContext.request.contextPath}/lessons/delete?id=" + lessonId + "&subjectId=" + subjectId;
                }
            }
        </script>
    </body>
</html>