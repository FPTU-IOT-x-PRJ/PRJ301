package controller;

import dao.DocumentDAO;
import dao.LessonDAO; // Thêm import
import dao.SubjectDAO; // Thêm import
import entity.Document;
import entity.Lesson; // Thêm import
import entity.Subject; // Thêm import
import entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.SQLException;

// Cloudinary Imports
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import utils.ConfigManager;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class DocumentController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DocumentController.class.getName());
    private DocumentDAO documentDao;
    private SubjectDAO subjectDao; // Khởi tạo SubjectDAO
    private LessonDAO lessonDao;   // Khởi tạo LessonDAO
    private Cloudinary cloudinary;

    @Override
    public void init() throws ServletException {
        super.init();
        documentDao = new DocumentDAO();
        subjectDao = new SubjectDAO(); // Khởi tạo
        lessonDao = new LessonDAO();   // Khởi tạo

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", ConfigManager.getCloudinaryCloudName());
        config.put("api_key", ConfigManager.getCloudinaryApiKey());
        config.put("api_secret", ConfigManager.getCloudinaryApiSecret());
        cloudinary = new Cloudinary(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            switch (action != null ? action : "/list") {
                case "/new":
                    showNewForm(request, response, user.getId());
                    break;
                case "/list":
                    listDocuments(request, response, user.getId());
                    break;
                case "/edit":
                    showEditForm(request, response, user.getId());
                    break;
                case "/detail":
                    showDocumentDetail(request, response, user.getId());
                    break;
                case "/delete":
                    deleteDocument(request, response, user.getId());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error in DocumentController.doGet", ex);
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            switch (action != null ? action : "") {
                case "/upload":
                    uploadDocument(request, response, user.getId());
                    break;
                case "/update":
                    updateDocument(request, response, user.getId());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error in DocumentController.doPost", ex);
            throw new ServletException(ex);
        }
    }

    private void listDocuments(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        List<Document> listDocuments = documentDao.getAllDocuments(userId);
        
        // Lấy danh sách Subjects và Lessons để hiển thị tên thay vì chỉ ID
        // (Đây là ví dụ đơn giản, trong thực tế có thể cần tối ưu hơn nếu số lượng lớn)
        Map<Integer, String> subjectNames = new HashMap<>();
        Map<Integer, String> lessonNames = new HashMap<>();
        
        List<Subject> allSubjects = subjectDao.getAllSubjects(); // Giả sử có method này
        for (Subject s : allSubjects) {
            subjectNames.put(s.getId(), s.getName());
        }
        
        // Lấy tất cả lessons để ánh xạ, cần điều chỉnh nếu lessonDAO chỉ lấy theo subjectId
        // Hiện tại LessonDAO có method SELECT_LESSONS_BY_SUBJECT_ID_SQL
        // Cần một method getAllLessons() hoặc cải thiện logic để tránh N+1 query
        // Tạm thời, để đơn giản, sẽ không fetch lesson names ở đây mà chỉ subject names.
        // Nếu cần lesson names, cần fetch chúng từ lessonDao.
        // Hoặc truyền lessonId và subjectId sang JSP và để JSP tự xử lý hiển thị tên nếu có các Map tương ứng.

        request.setAttribute("listDocuments", listDocuments);
        request.setAttribute("subjectNames", subjectNames); // Truyền map tên môn học
        // Nếu muốn hiển thị tên buổi học, bạn sẽ cần một map tương tự cho lessons
        
        request.getRequestDispatcher("/views/documents/list.jsp").forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        // Lấy danh sách các môn học để điền vào dropdown
        List<Subject> subjects = subjectDao.getAllSubjects(); // Giả sử getAllSubjects() không cần userId
        request.setAttribute("subjects", subjects);
        request.getRequestDispatcher("/views/documents/new.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Document existingDocument = documentDao.getDocumentById(id, userId);
        
        if (existingDocument != null) {
            request.setAttribute("document", existingDocument);
            // Lấy danh sách các môn học để điền vào dropdown
            List<Subject> subjects = subjectDao.getAllSubjects();
            request.setAttribute("subjects", subjects);
            
            // Nếu tài liệu đã có subjectId, lấy danh sách lessons của subject đó
            if (existingDocument.getSubjectId() != null) {
                List<Lesson> lessons = lessonDao.getLessonsBySubjectId(existingDocument.getSubjectId());
                request.setAttribute("lessonsOfSelectedSubject", lessons);
            }
            
            request.getRequestDispatcher("/views/documents/edit.jsp").forward(request, response);
        } else {
            request.setAttribute("errorMessage", "Tài liệu không tồn tại hoặc bạn không có quyền chỉnh sửa.");
            listDocuments(request, response, userId); // Trở lại danh sách
        }
    }

    private void uploadDocument(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            Part filePart = request.getPart("file");
            String fileName = getFileName(filePart);
            String description = request.getParameter("description");
            
            // Lấy subjectId và lessonId từ request
            Integer subjectId = null;
            Integer lessonId = null;
            String subjectIdStr = request.getParameter("subjectId");
            String lessonIdStr = request.getParameter("lessonId");

            if (subjectIdStr != null && !subjectIdStr.trim().isEmpty()) {
                try {
                    subjectId = Integer.parseInt(subjectIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid subject ID format: {0}", subjectIdStr);
                    request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
                    showNewForm(request, response, userId);
                    return;
                }
            }
            
            if (lessonIdStr != null && !lessonIdStr.trim().isEmpty()) {
                try {
                    lessonId = Integer.parseInt(lessonIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid lesson ID format: {0}", lessonIdStr);
                    request.setAttribute("errorMessage", "ID buổi học không hợp lệ.");
                    showNewForm(request, response, userId);
                    return;
                }
            }
            
            // Logic: Nếu có lessonId thì subjectId phải có (vì lesson thuộc subject)
            if (lessonId != null && subjectId == null) {
                // Đây là một lỗi logic, nếu có lessonId thì phải có subjectId
                request.setAttribute("errorMessage", "Một tài liệu gắn với buổi học phải có môn học tương ứng.");
                showNewForm(request, response, userId);
                return;
            }

            // Upload to Cloudinary
            String storedFileName = UUID.randomUUID().toString() + "_" + fileName;
            String filePath = null;
            String fileType = filePart.getContentType();
            long fileSize = filePart.getSize();

            try (InputStream fileContent = filePart.getInputStream()) {
                Map uploadResult = cloudinary.uploader().upload(fileContent.readAllBytes(), ObjectUtils.asMap(
                        "public_id", storedFileName // Sử dụng tên file đã xử lý làm public_id
                ));
                filePath = (String) uploadResult.get("secure_url");
                LOGGER.log(Level.INFO, "Uploaded file to Cloudinary: {0}", filePath);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to upload file to Cloudinary.", e);
                request.setAttribute("errorMessage", "Tải file lên Cloudinary thất bại: " + e.getMessage());
                showNewForm(request, response, userId);
                return;
            }

            Document newDocument = new Document(fileName, storedFileName, filePath, fileType, fileSize, userId, description, subjectId, lessonId);
            boolean success = documentDao.addDocument(newDocument);

            if (success) {
                LOGGER.log(Level.INFO, "Document {0} uploaded successfully by user {1}.", new Object[]{fileName, userId});
                response.sendRedirect(request.getContextPath() + "/documents/list?message=uploadSuccess");
            } else {
                LOGGER.log(Level.WARNING, "Failed to add document {0} to DB for user {1}.", new Object[]{fileName, userId});
                request.setAttribute("errorMessage", "Lỗi khi lưu thông tin tài liệu vào cơ sở dữ liệu.");
                showNewForm(request, response, userId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error uploading document.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi tải lên tài liệu: " + e.getMessage());
            showNewForm(request, response, userId);
        }
    }

    private void updateDocument(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String fileName = request.getParameter("fileName"); // fileName không thay đổi
            String description = request.getParameter("description");
            
            // Lấy subjectId và lessonId từ request
            Integer subjectId = null;
            Integer lessonId = null;
            String subjectIdStr = request.getParameter("subjectId");
            String lessonIdStr = request.getParameter("lessonId");

            if (subjectIdStr != null && !subjectIdStr.trim().isEmpty()) {
                try {
                    subjectId = Integer.parseInt(subjectIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid subject ID format for update: {0}", subjectIdStr);
                    request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
                    showEditForm(request, response, userId);
                    return;
                }
            }
            
            if (lessonIdStr != null && !lessonIdStr.trim().isEmpty()) {
                try {
                    lessonId = Integer.parseInt(lessonIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid lesson ID format for update: {0}", lessonIdStr);
                    request.setAttribute("errorMessage", "ID buổi học không hợp lệ.");
                    showEditForm(request, response, userId);
                    return;
                }
            }
            
            // Logic: Nếu có lessonId thì subjectId phải có (vì lesson thuộc subject)
            if (lessonId != null && subjectId == null) {
                request.setAttribute("errorMessage", "Một tài liệu gắn với buổi học phải có môn học tương ứng.");
                showEditForm(request, response, userId);
                return;
            }

            Document existingDocument = documentDao.getDocumentById(id, userId);

            if (existingDocument != null) {
                // Cập nhật các trường có thể thay đổi
                existingDocument.setDescription(description);
                existingDocument.setSubjectId(subjectId);
                existingDocument.setLessonId(lessonId);

                boolean success = documentDao.updateDocument(existingDocument);

                if (success) {
                    LOGGER.log(Level.INFO, "Document with ID {0} updated successfully by user {1}.", new Object[]{id, userId});
                    response.sendRedirect(request.getContextPath() + "/documents/list?message=updateSuccess");
                } else {
                    LOGGER.log(Level.WARNING, "Failed to update document with ID {0} for user {1}.", new Object[]{id, userId});
                    request.setAttribute("errorMessage", "Không thể cập nhật tài liệu. Vui lòng thử lại.");
                    showEditForm(request, response, userId); // Trở lại form edit với lỗi
                }
            } else {
                LOGGER.log(Level.WARNING, "Attempt to update non-existent or unauthorized document with ID {0} by user {1}.", new Object[]{id, userId});
                request.setAttribute("errorMessage", "Tài liệu không tồn tại hoặc bạn không có quyền chỉnh sửa.");
                listDocuments(request, response, userId); // Trở lại danh sách
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid document ID format for update: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID tài liệu không hợp lệ.");
            listDocuments(request, response, userId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating document.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi cập nhật tài liệu: " + e.getMessage());
            listDocuments(request, response, userId);
        }
    }

    private void showDocumentDetail(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Document document = documentDao.getDocumentById(id, userId);
            if (document != null) {
                // Nếu có subjectId, lấy thông tin Subject
                if (document.getSubjectId() != null) {
                    Subject subject = subjectDao.getSubjectById(document.getSubjectId());
                    request.setAttribute("associatedSubject", subject);
                }
                // Nếu có lessonId, lấy thông tin Lesson
                if (document.getLessonId() != null) {
                    Lesson lesson = lessonDao.getLessonById(document.getLessonId());
                    request.setAttribute("associatedLesson", lesson);
                }
                request.setAttribute("document", document);
                request.getRequestDispatcher("/views/documents/detail.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Tài liệu không tồn tại hoặc bạn không có quyền xem.");
                listDocuments(request, response, userId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid document ID format for detail: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID tài liệu không hợp lệ.");
            listDocuments(request, response, userId);
        }
    }

    private void deleteDocument(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Document docToDelete = documentDao.getDocumentById(id, userId);

            if (docToDelete == null) {
                LOGGER.log(Level.WARNING, "Attempt to delete non-existent or unauthorized document with ID {0} by user {1}.", new Object[]{id, userId});
                request.setAttribute("errorMessage", "Không thể xóa tài liệu. Tài liệu không tồn tại hoặc bạn không có quyền.");
                listDocuments(request, response, userId);
                return;
            }

            // Xóa file khỏi Cloudinary trước
            if (docToDelete.getStoredFileName() != null && !docToDelete.getStoredFileName().isEmpty()) {
                try {
                    cloudinary.uploader().destroy(docToDelete.getStoredFileName(), ObjectUtils.emptyMap());
                    LOGGER.log(Level.INFO, "Deleted file {0} from Cloudinary.", docToDelete.getStoredFileName());
                } catch (Exception cloudinaryEx) {
                    LOGGER.log(Level.WARNING, "Failed to delete file {0} from Cloudinary: {1}",
                            new Object[]{docToDelete.getStoredFileName(), cloudinaryEx.getMessage()});
                    // Log lỗi nhưng vẫn tiếp tục xóa trong DB để tránh inconsistent state
                }
            }

            boolean success = documentDao.deleteDocument(id, userId);

            if (success) {
                LOGGER.log(Level.INFO, "Document with ID {0} deleted successfully from DB by user {1}..", new Object[]{id, userId});
                response.sendRedirect(request.getContextPath() + "/documents/list?message=deleteSuccess");
            } else {
                LOGGER.log(Level.WARNING, "Failed to delete document with ID {0} for user {1}. Document not found or unauthorized.", new Object[]{id, userId});
                request.setAttribute("errorMessage", "Không thể xóa tài liệu. Tài liệu không tồn tại hoặc bạn không có quyền.");
                listDocuments(request, response, userId); // Trở lại danh sách với lỗi
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid document ID format for delete: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID tài liệu không hợp lệ.");
            listDocuments(request, response, userId);
        }
    }

    private String getFileName(final Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}