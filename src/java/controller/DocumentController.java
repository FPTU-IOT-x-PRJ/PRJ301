package controller;

import dao.DocumentDAO;
import entity.Document;
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
    private Cloudinary cloudinary;

    @Override
    public void init() throws ServletException {
        super.init();
        documentDao = new DocumentDAO();

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", ConfigManager.getInstance().getProperty("CLOUDINARY_CLOUD_NAME"));
        config.put("api_key", ConfigManager.getInstance().getProperty("CLOUDINARY_API_KEY"));
        config.put("api_secret", ConfigManager.getInstance().getProperty("CLOUDINARY_API_SECRET"));

        LOGGER.log(Level.INFO, "Cloudinary config: cloud_name={0}, api_key={1}, api_secret={2}",
                new Object[]{config.get("cloud_name"), config.get("api_key"),
                    (config.get("api_secret") != null && !config.get("api_secret").isEmpty()) ? "******" : "null/empty"});

        cloudinary = new Cloudinary(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        if (action == null) {
            action = "/"; // Mặc định về list
        }

        HttpSession session = request.getSession(false);
        User currentUser = null;

        if (session != null) {
            currentUser = (User) session.getAttribute("loggedInUser");
        }

        if (currentUser == null) {
            LOGGER.log(Level.WARNING, "Unauthorized access attempt. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            switch (action) {
                case "/new":
                    showNewForm(request, response);
                    break;
                case "/list":
                case "/": // Mặc định khi truy cập /documents/
                    listDocuments(request, response, currentUser.getId());
                    break;
                case "/delete":
                    deleteDocument(request, response, currentUser.getId());
                    break;
                case "/edit":
                    showEditForm(request, response, currentUser.getId());
                    break;
                case "/detail":
                    showDocumentDetail(request, response, currentUser.getId());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action Not Found");
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error in DocumentController", ex);
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        if (action == null) {
            action = "/";
        }

        HttpSession session = request.getSession(false);
        User currentUser = null;

        if (session != null) {
            currentUser = (User) session.getAttribute("loggedInUser");
        }

        if (currentUser == null) {
            LOGGER.log(Level.WARNING, "Unauthorized access attempt. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            switch (action) {
                case "/upload": 
                    uploadDocument(request, response, currentUser);
                    break;
                case "/update":
                    updateDocument(request, response, currentUser.getId());
                    break;
                case "/delete": 
                    deleteDocument(request, response, currentUser.getId());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action Not Found");
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error in DocumentController", ex);
            throw new ServletException(ex);
        }
    }

    // --- CRUD Helper Methods ---
    // CREATE (Upload)
    private void uploadDocument(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException, SQLException {
        String description = request.getParameter("description");

        try {
            boolean anyFileUploaded = false;
            for (Part part : request.getParts()) {
                // Kiểm tra xem đây có phải là một file và có tên file hợp lệ không
                if (part.getName().equals("file") && part.getSubmittedFileName() != null && !part.getSubmittedFileName().isEmpty()) {
                    String originalFileName = part.getSubmittedFileName();
                    String publicId = "prj301_documents/" + UUID.randomUUID().toString(); // Thư mục/ID trên Cloudinary

                    Map<String, Object> uploadResult = null;
                    try (InputStream fileContent = part.getInputStream()) {
                        Map<String, Object> options = ObjectUtils.asMap(
                                "public_id", publicId,
                                "resource_type", "auto",
                                "folder", "prj301_documents"
                        );

                        uploadResult = cloudinary.uploader().upload(fileContent.readAllBytes(), options);
                    }

                    String fileUrl = (String) uploadResult.get("secure_url");
                    String storedFileNameOnCloudinary = (String) uploadResult.get("public_id");
                    String resourceType = (String) uploadResult.get("resource_type");

                    LOGGER.log(Level.INFO, "File uploaded to Cloudinary: Original Name={0}, Stored ID={1}, URL={2}, Type={3}",
                            new Object[]{originalFileName, storedFileNameOnCloudinary, fileUrl, resourceType});

                    Document doc = new Document();
                    doc.setFileName(originalFileName);
                    doc.setStoredFileName(storedFileNameOnCloudinary);
                    doc.setFilePath(fileUrl);
                    doc.setFileType(part.getContentType());
                    doc.setFileSize(part.getSize());
                    doc.setUploadedBy(currentUser.getId());
                    doc.setDescription(description);

                    boolean dbSuccess = documentDao.addDocument(doc);
                    if (dbSuccess) {
                        request.setAttribute("message", "File '" + originalFileName + "' đã được upload lên Cloudinary và lưu thông tin thành công!");
                        anyFileUploaded = true;
                    } else {
                        request.setAttribute("errorMessage", "File '" + originalFileName + "' đã upload nhưng không thể lưu thông tin vào CSDL.");
                    }
                }
            }
            if (anyFileUploaded) {
                response.sendRedirect(request.getContextPath() + "/documents/list?success=upload");
            } else {
                request.setAttribute("errorMessage", "Vui lòng chọn một file để upload.");
                request.getRequestDispatcher("/components/document/document-upload-form.jsp").forward(request, response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error uploading file to Cloudinary.", e);
            request.setAttribute("errorMessage", "Có lỗi xảy ra khi upload tài liệu lên Cloudinary: " + e.getMessage());
            request.getRequestDispatcher("/components/document/document-upload-form.jsp").forward(request, response);
        }
    }

    // READ (List)
    private void listDocuments(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException, SQLException {
        List<Document> listDocuments = documentDao.getAllDocumentsByUser(userId);
        request.setAttribute("listDocuments", listDocuments);
        if (request.getParameter("success") != null && request.getParameter("success").equals("upload")) {
            request.setAttribute("message", "Tài liệu đã được tải lên thành công!");
        } else if (request.getParameter("success") != null && request.getParameter("success").equals("update")) {
            request.setAttribute("message", "Tài liệu đã được cập nhật thành công!");
        } else if (request.getParameter("success") != null && request.getParameter("success").equals("delete")) {
            request.setAttribute("message", "Tài liệu đã được xóa thành công!");
        }
        request.getRequestDispatcher("/components/document/document-list.jsp").forward(request, response);
    }

    // READ (Detail)
    private void showDocumentDetail(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException, SQLException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Document existingDocument = documentDao.getDocumentById(id, userId);
            if (existingDocument == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tài liệu không tồn tại hoặc bạn không có quyền.");
                return;
            }
            request.setAttribute("document", existingDocument);
            request.getRequestDispatcher("/components/document/document-detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid document ID format for detail: {0}", request.getParameter("id"));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID tài liệu không hợp lệ.");
        }
    }

    // UPDATE (Show form)
    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/components/document/document-upload-form.jsp").forward(request, response);
    }

    // UPDATE (Show form for existing document)
    private void showEditForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException, SQLException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Document existingDocument = documentDao.getDocumentById(id, userId);
            if (existingDocument == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tài liệu không tồn tại hoặc bạn không có quyền.");
                return;
            }
            request.setAttribute("document", existingDocument);
            request.getRequestDispatcher("/components/document/document-edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid document ID format for edit: {0}", request.getParameter("id"));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID tài liệu không hợp lệ.");
        }
    }

    // UPDATE (Process form submission)
    private void updateDocument(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException, SQLException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String description = request.getParameter("description"); // Chỉ cho phép update mô tả

            Document existingDocument = documentDao.getDocumentById(id, userId);
            if (existingDocument == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tài liệu không tồn tại hoặc bạn không có quyền.");
                return;
            }

            // Cập nhật các trường cho phép (ở đây chỉ là description)
            existingDocument.setDescription(description);

            boolean success = documentDao.updateDocument(existingDocument);

            if (success) {
                LOGGER.log(Level.INFO, "Document with ID {0} updated successfully by user {1}.", new Object[]{id, userId});
                response.sendRedirect(request.getContextPath() + "/documents/list?success=update");
            } else {
                request.setAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật tài liệu.");
                request.setAttribute("document", existingDocument); // Giữ lại dữ liệu cũ trên form
                request.getRequestDispatcher("/components/document/document-edit.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid document ID format for update: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID tài liệu không hợp lệ.");
            request.getRequestDispatcher("/components/document/document-edit.jsp").forward(request, response);
        }
    }

    // DELETE (Process deletion)
    // DELETE (Process deletion)
    private void deleteDocument(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException, SQLException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            
            Document docToDelete = documentDao.getDocumentById(id, userId);
            if (docToDelete != null) {
                try {
                    // Cố gắng xóa trên Cloudinary
                    cloudinary.uploader().destroy(docToDelete.getStoredFileName(), ObjectUtils.emptyMap());
                    LOGGER.log(Level.INFO, "Deleted file {0} from Cloudinary.", docToDelete.getStoredFileName());
                } catch (Exception cloudinaryEx) {
                    // Log lỗi nếu không xóa được trên Cloudinary nhưng vẫn tiếp tục xóa trong DB
                    LOGGER.log(Level.WARNING, "Failed to delete file {0} from Cloudinary. Proceeding to delete from DB. Error: {1}",
                            new Object[]{docToDelete.getStoredFileName(), cloudinaryEx.getMessage()});
                }
            } else {
                LOGGER.log(Level.WARNING, "Document with ID {0} not found for user {1} or unauthorized. Cannot attempt Cloudinary deletion.", new Object[]{id, userId});
            }

            // Luôn cố gắng xóa tài liệu khỏi cơ sở dữ liệu, bất kể kết quả xóa trên Cloudinary
            boolean success = documentDao.deleteDocument(id, userId);

            if (success) {
                LOGGER.log(Level.INFO, "Document with ID {0} deleted successfully from DB by user {1}.", new Object[]{id, userId});
                response.sendRedirect(request.getContextPath() + "/documents/list?success=delete");
            } else {
                LOGGER.log(Level.WARNING, "Failed to delete document with ID {0} for user {1}. Document not found in DB or unauthorized.", new Object[]{id, userId});
                request.setAttribute("errorMessage", "Không thể xóa tài liệu. Tài liệu không tồn tại hoặc bạn không có quyền.");
                listDocuments(request, response, userId); // Trở lại danh sách với lỗi
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid document ID format for delete: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID tài liệu không hợp lệ.");
            listDocuments(request, response, userId);
        }
    }
}
