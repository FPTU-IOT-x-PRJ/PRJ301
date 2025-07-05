<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="mt-4">
    <c:choose>
        <c:when test="${empty notes}">
            <div class="alert alert-info text-center mt-3" role="alert">
                Chưa có ghi chú nào cho buổi học này.
            </div>
        </c:when>
        <c:otherwise>
            <div class="row">
                <c:forEach var="note" items="${notes}">
                    <div class="col-6 col-md-3 mb-3">
                        <div class="card note-item-compact h-100 d-flex flex-column">
                            <div class="card-body d-flex flex-column">
                                <h6 class="card-title text-primary">${note.title}</h6>
                                <p class="card-text text-muted small flex-grow-1">${note.content}</p>
                                <div class="mt-auto d-flex justify-content-end gap-2">
                                    <%-- Nút Sửa: Mở modal Bootstrap --%>
                                    <button type="button" class="btn btn-sm btn-outline-primary edit-note-btn"
                                            data-bs-toggle="modal"
                                            data-bs-target="#editNoteModal"
                                            data-id="${note.id}"
                                            data-title="${note.title}"
                                            data-content="${note.content}"
                                            data-subject-id="${note.subjectId}"
                                            data-lesson-id="${note.lessonId}">
                                        <i class="fas fa-edit"></i> Sửa
                                    </button>
                                    
                                    <%-- Nút Xóa: Sử dụng form ẩn để gửi yêu cầu POST với _method=DELETE --%>
                                    <form action="${pageContext.request.contextPath}/notes/delete" method="POST" style="display:inline;"
                                          onsubmit="return confirm('Bạn có chắc chắn muốn xóa ghi chú này không?');">
                                        <input type="hidden" name="id" value="${note.id}">
                                        <input type="hidden" name="lessonId" value="${lesson.id}">
                                        <input type="hidden" name="subjectId" value="${subject.id}">
                                        <input type="hidden" name="_method" value="DELETE"> <%-- Dùng để Servlet phân biệt là DELETE --%>
                                        <button type="submit" class="btn btn-sm btn-outline-danger">
                                            <i class="fas fa-trash-alt"></i> Xóa
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<div class="modal fade" id="editNoteModal" tabindex="-1" aria-labelledby="editNoteModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="editNoteModalLabel">Chỉnh Sửa Ghi Chú</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <%-- Đã chỉnh sửa action thành /notes/edit --%>
                <form id="editNoteForm" action="${pageContext.request.contextPath}/notes/edit" method="POST">
                    <input type="hidden" id="editNoteId" name="id">
                    <input type="hidden" id="editSubjectId" name="subjectId">
                    <input type="hidden" id="editLessonId" name="lessonId">
                    <input type="hidden" name="_method" value="PUT"> <%-- Dùng để Servlet phân biệt là PUT --%>
                    
                    <div class="mb-3">
                        <label for="editNoteTitle" class="form-label">Tiêu đề:</label>
                        <input type="text" class="form-control" id="editNoteTitle" name="title" required>
                    </div>
                    <div class="mb-3">
                        <label for="editNoteContent" class="form-label">Nội dung:</label>
                        <textarea class="form-control" id="editNoteContent" name="content" rows="5"></textarea>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-primary">Lưu thay đổi</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<style>
    .note-item-compact {
        border-radius: 8px;
        box-shadow: 0 2px 4px rgba(0,0,0,0.05);
        transition: transform 0.2s;
    }
    .note-item-compact:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 8px rgba(0,0,0,0.1);
    }
    .note-item-compact .card-title {
        font-weight: 600;
        margin-bottom: 0.5rem;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .note-item-compact .card-text {
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 1; /* Giới hạn 1 dòng */
        -webkit-box-orient: vertical;
        max-height: 1.5em;
    }
    .note-item-compact .card-body {
        display: flex;
        flex-direction: column;
    }
    .note-item-compact .card-text {
        flex-grow: 1;
    }
</style>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        const editNoteModal = document.getElementById('editNoteModal');
        const editNoteIdInput = document.getElementById('editNoteId');
        const editNoteTitleInput = document.getElementById('editNoteTitle');
        const editNoteContentInput = document.getElementById('editNoteContent');
        const editSubjectIdInput = document.getElementById('editSubjectId');
        const editLessonIdInput = document.getElementById('editLessonId');

        // Khi modal chỉnh sửa được hiển thị
        editNoteModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget; // Nút đã click để mở modal
            const noteId = button.getAttribute('data-id');
            const noteTitle = button.getAttribute('data-title');
            const noteContent = button.getAttribute('data-content');
            const subjectId = button.getAttribute('data-subject-id'); // Lấy subjectId
            const lessonId = button.getAttribute('data-lesson-id');   // Lấy lessonId

            // Điền dữ liệu vào form trong modal
            editNoteIdInput.value = noteId;
            editNoteTitleInput.value = noteTitle;
            editNoteContentInput.value = noteContent;
            editSubjectIdInput.value = subjectId; // Điền subjectId vào hidden input
            editLessonIdInput.value = lessonId;   // Điền lessonId vào hidden input
        });
    });
</script>