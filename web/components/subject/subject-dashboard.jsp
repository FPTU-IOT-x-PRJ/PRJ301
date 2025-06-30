<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quản lý Môn học - EduPlan</title>
        <link href="/css/common.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    </head>
    <body>
        <jsp:include page="../navigation/navigation.jsp" />
        <div class="container-fluid py-4">
            <div class="row mb-4">
                <div class="col-12 d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="mb-1">Quản lý Môn học</h2>
                        <p class="text-muted mb-0">Danh sách môn học trong hệ thống</p>
                        <!-- Hiển thị kỳ học hiện tại và dropdown chuyển kỳ học -->
                        <div class="mt-3 d-flex align-items-center gap-3">
                            <h5 class="mb-0 text-primary">
                                <i class="fas fa-calendar-alt me-1"></i>
                                <span>Đang xem kỳ học:</span> 
                                <strong>${currentSemester.name}</strong>
                            </h5>

                            <form method="get" action="${pageContext.request.contextPath}/subjects" class="d-flex align-items-center">
                                <input type="hidden" name="page" value="1" />
<!--                                <input type="hidden" name="semesterId" value="${semesterId}"/>-->
                                <label for="semesterId" class="me-2 mb-0 fw-bold text-muted">Chuyển kỳ:</label>
                                <select name="semesterId" id="semesterId" class="form-select form-select-sm" onchange="this.form.submit()">
                                    <c:forEach var="s" items="${allSemesters}">
                                        <option value="${s.id}" ${s.id == semesterId ? 'selected' : ''}>${s.name}</option>
                                    </c:forEach>
                                </select>
                            </form>
                        </div>

                    </div>
                    <a href="${pageContext.request.contextPath}/subjects/add?semesterId=${semesterId}" class="btn btn-primary">
                        <i class="fas fa-plus me-2"></i>Thêm môn học
                    </a>
                </div>
            </div>

            <div class="card shadow-sm">
                <div class="card-header bg-light">
                    <h5 class="mb-0"><i class="fas fa-book me-2"></i>Danh sách môn học</h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-striped table-hover mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th>#</th>
                                    <th>Tên môn</th>
                                    <th>Mã</th>
                                    <th>Tín chỉ</th>
                                    <th>Giảng viên</th>
                                    <th>Trạng thái</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="subject" items="${subjects}" varStatus="loop">
                                    <tr>
                                        <td>${loop.index + 1}</td>
                                        <td>${subject.name}</td>
                                        <td>${subject.code}</td>
                                        <td>${subject.credits}</td>
                                        <td>${subject.teacherName}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${subject.isActive}">
                                                    <span class="badge bg-success">Hoạt động</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">Ngưng</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="btn-group btn-group-sm">
                                                <a href="${pageContext.request.contextPath}/subjects/edit?id=${subject.id}&semesterId=${semesterId}" class="btn btn-outline-warning">
                                                    <i class="fas fa-edit"></i>
                                                </a>
                                                <a href="${pageContext.request.contextPath}/subjects/delete?id=${subject.id}" class="btn btn-outline-danger" onclick="return confirm('Xóa môn học này?');">
                                                    <i class="fas fa-trash"></i>
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty subjects}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-muted">
                                            <i class="fas fa-folder-open fa-2x mb-2"></i><br>
                                            Không có môn học nào được tìm thấy.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="d-flex justify-content-center mt-4">
                <nav>
                    <ul class="pagination">
                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/subjects?page=${i}&semesterId=${semesterId}">${i}</a>
                            </li>
                        </c:forEach>
                    </ul>
                </nav>
            </div>
        </div>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
