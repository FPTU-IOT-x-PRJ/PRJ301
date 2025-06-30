package controller;

import dao.DocumentDAO;
import dao.LessonDAO;
import dao.SubjectDAO;
import entity.Document;
import entity.Lesson;
import entity.Subject;
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
import com.google.gson.Gson; // Import Gson

// Cloudinary Imports
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.ArrayList;
import utils.ConfigManager;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)
/**
 * Controller xử lý các thao tác liên quan đến tài liệu (Document).
 */
public class DocumentController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DocumentController.class.getName());
    private DocumentDAO documentDao;
    private SubjectDAO subjectDao;
    private LessonDAO lessonDao;
    private Cloudinary cloudinary;

    @Override
    public void init() throws ServletException {
        super.init();
        documentDao = new DocumentDAO();
        subjectDao = new SubjectDAO();
        lessonDao = new LessonDAO();

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", ConfigManager.getInstance().getProperty("CLOUDINARY_CLOUD_NAME"));
        config.put("api_key", ConfigManager.getInstance().getProperty("CLOUDINARY_API_KEY"));
        config.put("api_secret", ConfigManager.getInstance().getProperty("CLOUDINARY_API_SECRET"));

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
            switch (action != null ? action : "/display") { // Default action là display list
                case "/add":
                    displayAddForm(request, response, user.getId());
                    break;
                case "/display":
                    displayDocuments(request, response, user.getId());
                    break;
                case "/edit":
                    displayEditForm(request, response, user.getId());
                    break;
                case "/detail":
                    displayDocumentDetail(request, response, user.getId());
                    break;
                case "/delete": // Xử lý xóa qua GET (ít được khuyến nghị hơn POST cho xóa)
                    deleteDocument(request, response, user.getId());
                    break;
                case "/getLessonsBySubject": // New action for AJAX request
                    getLessonsBySubject(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action không hợp lệ.");
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu trong DocumentController.doGet", ex);
            throw new ServletException("Lỗi truy vấn cơ sở dữ liệu.", ex);
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
        request.setCharacterEncoding("UTF-8"); // Đảm bảo nhận tiếng Việt
        response.setCharacterEncoding("UTF-8"); // Đảm bảo gửi tiếng Việt

        try {
            switch (action != null ? action : "") {
                case "/add": // Dùng POST cho việc thêm mới (upload)
                    addDocument(request, response, user.getId());
                    break;
                case "/edit": // Dùng POST cho việc cập nhật
                    editDocument(request, response, user.getId());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action không hợp lệ.");
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu trong DocumentController.doPost", ex);
            throw new ServletException("Lỗi truy vấn cơ sở dữ liệu.", ex);
        }
    }

    /**
     * Hiển thị danh sách các tài liệu của người dùng hiện tại, có thể lọc theo môn học và buổi học.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void displayDocuments(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        
        Integer filterSubjectId = null;
        Integer filterLessonId = null;

        String subjectIdParam = request.getParameter("subjectId");
        String lessonIdParam = request.getParameter("lessonId");

        if (subjectIdParam != null && !subjectIdParam.trim().isEmpty()) {
            try {
                filterSubjectId = Integer.parseInt(subjectIdParam);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Định dạng ID môn học không hợp lệ cho bộ lọc: {0}", subjectIdParam);
                request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
            }
        }

        if (lessonIdParam != null && !lessonIdParam.trim().isEmpty()) {
            try {
                filterLessonId = Integer.parseInt(lessonIdParam);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Định dạng ID buổi học không hợp lệ cho bộ lọc: {0}", lessonIdParam);
                request.setAttribute("errorMessage", "ID buổi học không hợp lệ.");
            }
        }
        
        // Lấy danh sách tài liệu đã lọc
        List<Document> listDocuments = documentDao.getFilteredDocuments(userId, filterSubjectId, filterLessonId);

        // Lấy danh sách Subjects để hiển thị trong bộ lọc và bảng
        List<Subject> allSubjects = subjectDao.getAllSubjects(null, null, null, 0, Integer.MAX_VALUE, null); // Giả định này nếu không có user filter cho subjects
        Map<Integer, String> subjectNames = new HashMap<>();
        for (Subject s : allSubjects) {
            subjectNames.put(s.getId(), s.getName());
        }

        // Lấy danh sách Lessons nếu có subjectId được chọn, để hiển thị trong bộ lọc và bảng
        List<Lesson> lessonsOfSelectedSubject = new ArrayList<>();
        Map<Integer, String> lessonNames = new HashMap<>();
        if (filterSubjectId != null) {
             lessonsOfSelectedSubject = lessonDao.getAllLessonsBySubjectId(filterSubjectId, null, null, 1, Integer.MAX_VALUE);
             for(Lesson l : lessonsOfSelectedSubject) {
                 lessonNames.put(l.getId(), l.getName());
             }
        } else {
            // Nếu không có subjectId nào được chọn, bạn có thể chọn hiển thị tất cả lessons hoặc không hiển thị gì.
            // Để đơn giản, sẽ không populate lessonNames nếu không có subject được chọn.
            // Tuy nhiên, nếu một tài liệu có lessonId nhưng không có subjectId, cần đảm bảo tên lesson vẫn được hiển thị.
            // Cần một cách để lấy tên lesson từ lessonId mà không cần subjectId. Hiện tại LessonDAO chưa có getLessonById(int lessonId).
            // UPDATE: lessonDao.getLessonById đã có.
            for (Document doc : listDocuments) {
                if (doc.getLessonId() != null && !lessonNames.containsKey(doc.getLessonId())) {
                    Lesson lesson = lessonDao.getLessonById(doc.getLessonId());
                    if (lesson != null) {
                        lessonNames.put(lesson.getId(), lesson.getName());
                    }
                }
            }
        }


        request.setAttribute("listDocuments", listDocuments);
        request.setAttribute("subjects", allSubjects); // Dùng cho dropdown bộ lọc
        request.setAttribute("subjectNames", subjectNames); // Dùng để hiển thị tên trong bảng
        request.setAttribute("lessonsOfSelectedSubject", lessonsOfSelectedSubject); // Dùng cho dropdown bộ lọc lessons
        request.setAttribute("lessonNames", lessonNames); // Dùng để hiển thị tên trong bảng

        // Giữ lại các giá trị đã chọn để hiển thị trên form sau khi submit
        request.setAttribute("selectedSubjectId", filterSubjectId);
        request.setAttribute("selectedLessonId", filterLessonId);


        request.getRequestDispatcher("/components/document/document-dashboard.jsp").forward(request, response);
    }

    /**
     * Hiển thị form để thêm tài liệu mới.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void displayAddForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        // Lấy danh sách các môn học để điền vào dropdown (cần điều chỉnh getAllSubjects để lọc theo userId nếu có)
        // Hiện tại SubjectDAO.getAllSubjects() không nhận userId, cần giả định hoặc điều chỉnh DAO nếu cần.
        List<Subject> subjects = subjectDao.getAllSubjects(null, null, null, 0, Integer.MAX_VALUE, null); 
        request.setAttribute("subjects", subjects);
        request.getRequestDispatcher("/components/document/document-add.jsp").forward(request, response);
    }

    /**
     * Hiển thị form để chỉnh sửa tài liệu.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void displayEditForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Document existingDocument = documentDao.getDocumentById(id, userId); // Sử dụng getDocumentById

            if (existingDocument != null) {
                request.setAttribute("document", existingDocument);
                // Lấy danh sách các môn học để điền vào dropdown
                List<Subject> subjects = subjectDao.getAllSubjects(null, null, null, 0, Integer.MAX_VALUE, null);
                request.setAttribute("subjects", subjects);

                // Nếu tài liệu đã có subjectId, lấy danh sách lessons của subject đó
                if (existingDocument.getSubjectId() != null) {
                    List<Lesson> lessons = lessonDao.getAllLessonsBySubjectId(existingDocument.getSubjectId(), null, null, 1, Integer.MAX_VALUE); // getAllLessonsBySubjectId
                    request.setAttribute("lessonsOfSelectedSubject", lessons);
                }

                request.getRequestDispatcher("/components/document/document-edit.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Tài liệu không tồn tại hoặc bạn không có quyền chỉnh sửa.");
                displayDocuments(request, response, userId); // Trở lại danh sách
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID tài liệu không hợp lệ khi hiển thị form chỉnh sửa: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID tài liệu không hợp lệ.");
            displayDocuments(request, response, userId);
        }
    }

    /**
     * Xử lý việc thêm tài liệu mới (bao gồm upload file).
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void addDocument(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            Part filePart = request.getPart("file");
            String fileName = getFileName(filePart);
            String description = request.getParameter("description");

            Integer subjectId = null;
            Integer lessonId = null;
            String subjectIdStr = request.getParameter("subjectId");
            String lessonIdStr = request.getParameter("lessonId");

            if (subjectIdStr != null && !subjectIdStr.trim().isEmpty()) {
                try {
                    subjectId = Integer.parseInt(subjectIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Định dạng ID môn học không hợp lệ: {0}", subjectIdStr);
                    request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
                    displayAddForm(request, response, userId);
                    return;
                }
            }

            if (lessonIdStr != null && !lessonIdStr.trim().isEmpty()) {
                try {
                    lessonId = Integer.parseInt(lessonIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Định dạng ID buổi học không hợp lệ: {0}", lessonIdStr);
                    request.setAttribute("errorMessage", "ID buổi học không hợp lệ.");
                    displayAddForm(request, response, userId);
                    return;
                }
            }

            if (lessonId != null && subjectId == null) {
                request.setAttribute("errorMessage", "Một tài liệu gắn với buổi học phải có môn học tương ứng.");
                displayAddForm(request, response, userId);
                return;
            }

            // Xử lý upload file lên Cloudinary
            String storedFileName = UUID.randomUUID().toString() + "_" + fileName;
            String filePath = null;
            String fileType = filePart.getContentType();
            long fileSize = filePart.getSize();

            try (InputStream fileContent = filePart.getInputStream()) {
                Map uploadResult = cloudinary.uploader().upload(fileContent.readAllBytes(), ObjectUtils.asMap(
                        "public_id", storedFileName
                ));
                filePath = (String) uploadResult.get("secure_url");
                LOGGER.log(Level.INFO, "Đã tải file lên Cloudinary: {0}", filePath);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Không thể tải file lên Cloudinary.", e);
                request.setAttribute("errorMessage", "Tải file lên Cloudinary thất bại: " + e.getMessage());
                displayAddForm(request, response, userId);
                return;
            }

            Document newDocument = new Document(fileName, storedFileName, filePath, fileType, fileSize, userId, description, subjectId, lessonId);
            boolean success = documentDao.addDocument(newDocument); // Sử dụng addDocument

            if (success) {
                LOGGER.log(Level.INFO, "Tài liệu {0} đã được tải lên thành công bởi người dùng {1}.", new Object[]{fileName, userId});
                response.sendRedirect(request.getContextPath() + "/documents/display?message=uploadSuccess");
            } else {
                LOGGER.log(Level.WARNING, "Không thể thêm tài liệu {0} vào DB cho người dùng {1}.", new Object[]{fileName, userId});
                request.setAttribute("errorMessage", "Lỗi khi lưu thông tin tài liệu vào cơ sở dữ liệu.");
                displayAddForm(request, response, userId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải lên tài liệu.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi tải lên tài liệu: " + e.getMessage());
            displayAddForm(request, response, userId);
        }
    }

    /**
     * Xử lý việc cập nhật thông tin tài liệu.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void editDocument(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String fileName = request.getParameter("fileName"); // fileName không thay đổi khi edit
            String description = request.getParameter("description");

            Integer subjectId = null;
            Integer lessonId = null;
            String subjectIdStr = request.getParameter("subjectId");
            String lessonIdStr = request.getParameter("lessonId");

            if (subjectIdStr != null && !subjectIdStr.trim().isEmpty()) {
                try {
                    subjectId = Integer.parseInt(subjectIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Định dạng ID môn học không hợp lệ khi cập nhật: {0}", subjectIdStr);
                    request.setAttribute("errorMessage", "ID môn học không hợp lệ.");
                    displayEditForm(request, response, userId);
                    return;
                }
            }

            if (lessonIdStr != null && !lessonIdStr.trim().isEmpty()) {
                try {
                    lessonId = Integer.parseInt(lessonIdStr);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Định dạng ID buổi học không hợp lệ khi cập nhật: {0}", lessonIdStr);
                    request.setAttribute("errorMessage", "ID buổi học không hợp lệ.");
                    displayEditForm(request, response, userId);
                    return;
                }
            }

            if (lessonId != null && subjectId == null) {
                request.setAttribute("errorMessage", "Một tài liệu gắn với buổi học phải có môn học tương ứng.");
                displayEditForm(request, response, userId);
                return;
            }

            Document existingDocument = documentDao.getDocumentById(id, userId);

            if (existingDocument != null) {
                // Chỉ cập nhật các trường có thể thay đổi qua form edit
                existingDocument.setFileName(fileName); // Cập nhật lại fileName từ request
                existingDocument.setDescription(description);
                existingDocument.setSubjectId(subjectId);
                existingDocument.setLessonId(lessonId);

                // Nếu có file mới được upload (kiểm tra part "file" có dữ liệu không)
                Part newFilePart = request.getPart("file");
                if (newFilePart != null && newFilePart.getSize() > 0) {
                    // Xóa file cũ trên Cloudinary nếu có
                    if (existingDocument.getStoredFileName() != null && !existingDocument.getStoredFileName().isEmpty()) {
                        try {
                            cloudinary.uploader().destroy(existingDocument.getStoredFileName(), ObjectUtils.emptyMap());
                            LOGGER.log(Level.INFO, "Đã xóa file cũ {0} từ Cloudinary để cập nhật.", existingDocument.getStoredFileName());
                        } catch (Exception cloudinaryEx) {
                            LOGGER.log(Level.WARNING, "Không thể xóa file cũ {0} từ Cloudinary: {1}",
                                    new Object[]{existingDocument.getStoredFileName(), cloudinaryEx.getMessage()});
                            // Không return ở đây, vẫn tiếp tục cập nhật DB nếu chỉ lỗi xóa file Cloudinary cũ
                        }
                    }
                    
                    // Upload file mới
                    String newFileName = getFileName(newFilePart);
                    String newStoredFileName = UUID.randomUUID().toString() + "_" + newFileName;
                    String newFilePath = null;
                    String newFileType = newFilePart.getContentType();
                    long newFileSize = newFilePart.getSize();

                    try (InputStream fileContent = newFilePart.getInputStream()) {
                        Map uploadResult = cloudinary.uploader().upload(fileContent.readAllBytes(), ObjectUtils.asMap(
                                "public_id", newStoredFileName
                        ));
                        newFilePath = (String) uploadResult.get("secure_url");
                        LOGGER.log(Level.INFO, "Đã tải file mới lên Cloudinary: {0}", newFilePath);
                        
                        // Cập nhật thông tin file mới vào đối tượng document
                        existingDocument.setFileName(newFileName);
                        existingDocument.setStoredFileName(newStoredFileName);
                        existingDocument.setFilePath(newFilePath);
                        existingDocument.setFileType(newFileType);
                        existingDocument.setFileSize(newFileSize);

                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Không thể tải file mới lên Cloudinary trong khi cập nhật.", e);
                        request.setAttribute("errorMessage", "Tải file mới lên Cloudinary thất bại: " + e.getMessage());
                        displayEditForm(request, response, userId);
                        return;
                    }
                }

                boolean success = documentDao.editDocument(existingDocument); // Sử dụng editDocument

                if (success) {
                    LOGGER.log(Level.INFO, "Tài liệu với ID {0} đã được cập nhật thành công bởi người dùng {1}.", new Object[]{id, userId});
                    response.sendRedirect(request.getContextPath() + "/documents/display?message=updateSuccess");
                } else {
                    LOGGER.log(Level.WARNING, "Không thể cập nhật tài liệu với ID {0} cho người dùng {1}.", new Object[]{id, userId});
                    request.setAttribute("errorMessage", "Không thể cập nhật tài liệu. Vui lòng thử lại.");
                    displayEditForm(request, response, userId);
                }
            } else {
                LOGGER.log(Level.WARNING, "Cố gắng cập nhật tài liệu không tồn tại hoặc không được phép với ID {0} bởi người dùng {1}.", new Object[]{id, userId});
                request.setAttribute("errorMessage", "Tài liệu không tồn tại hoặc bạn không có quyền chỉnh sửa.");
                displayDocuments(request, response, userId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID tài liệu không hợp lệ khi cập nhật: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID tài liệu không hợp lệ.");
            displayDocuments(request, response, userId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật tài liệu.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi cập nhật tài liệu: " + e.getMessage());
            displayDocuments(request, response, userId);
        }
    }

    /**
     * Hiển thị chi tiết của một tài liệu.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void displayDocumentDetail(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Document document = documentDao.getDocumentById(id, userId); // Sử dụng getDocumentById
            if (document != null) {
                if (document.getSubjectId() != null) {
                    Subject subject = subjectDao.getSubjectById(document.getSubjectId());
                    request.setAttribute("associatedSubject", subject);
                }
                if (document.getLessonId() != null) {
                    Lesson lesson = lessonDao.getLessonById(document.getLessonId());
                    request.setAttribute("associatedLesson", lesson);
                }
                request.setAttribute("document", document);
                request.getRequestDispatcher("/components/document/document-detail.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Tài liệu không tồn tại hoặc bạn không có quyền xem.");
                displayDocuments(request, response, userId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID tài liệu không hợp lệ để xem chi tiết: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID tài liệu không hợp lệ.");
            displayDocuments(request, response, userId);
        }
    }

    /**
     * Xử lý việc xóa tài liệu.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng.
     * @throws SQLException
     * @throws ServletException
     * @throws IOException
     */
    private void deleteDocument(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Document docToDelete = documentDao.getDocumentById(id, userId);

            if (docToDelete == null) {
                LOGGER.log(Level.WARNING, "Cố gắng xóa tài liệu không tồn tại hoặc không được phép với ID {0} bởi người dùng {1}.", new Object[]{id, userId});
                request.setAttribute("errorMessage", "Không thể xóa tài liệu. Tài liệu không tồn tại hoặc bạn không có quyền.");
                displayDocuments(request, response, userId);
                return;
            }

            if (docToDelete.getStoredFileName() != null && !docToDelete.getStoredFileName().isEmpty()) {
                try {
                    cloudinary.uploader().destroy(docToDelete.getStoredFileName(), ObjectUtils.emptyMap());
                    LOGGER.log(Level.INFO, "Đã xóa file {0} từ Cloudinary.", docToDelete.getStoredFileName());
                } catch (Exception cloudinaryEx) {
                    LOGGER.log(Level.WARNING, "Không thể xóa file {0} từ Cloudinary: {1}",
                            new Object[]{docToDelete.getStoredFileName(), cloudinaryEx.getMessage()});
                }
            }

            boolean success = documentDao.deleteDocument(id, userId); // Sử dụng deleteDocument

            if (success) {
                LOGGER.log(Level.INFO, "Tài liệu với ID {0} đã được xóa thành công từ DB bởi người dùng {1}.", new Object[]{id, userId});
                response.sendRedirect(request.getContextPath() + "/documents/display?message=deleteSuccess");
            } else {
                LOGGER.log(Level.WARNING, "Không thể xóa tài liệu với ID {0} cho người dùng {1}. Tài liệu không tìm thấy hoặc không được phép.", new Object[]{id, userId});
                request.setAttribute("errorMessage", "Không thể xóa tài liệu. Tài liệu không tồn tại hoặc bạn không có quyền.");
                displayDocuments(request, response, userId);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID tài liệu không hợp lệ để xóa: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID tài liệu không hợp lệ.");
            displayDocuments(request, response, userId);
        }
    }

    /**
     * Phương thức xử lý AJAX để lấy danh sách các buổi học (Lessons) dựa trên SubjectId được chọn.
     * Trả về JSON.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void getLessonsBySubject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new Gson(); // Khởi tạo Gson

        try {
            String subjectIdStr = request.getParameter("subjectId");
            if (subjectIdStr == null || subjectIdStr.trim().isEmpty()) {
                response.getWriter().write(gson.toJson(new ArrayList<>())); // Trả về mảng rỗng nếu không có subjectId
                return;
            }

            int subjectId = Integer.parseInt(subjectIdStr);
            // Lấy tất cả lessons cho subjectId đó. Có thể thêm tham số userId nếu LessonDAO hỗ trợ
            List<Lesson> lessons = lessonDao.getAllLessonsBySubjectId(subjectId, null, null, 1, Integer.MAX_VALUE);
            response.getWriter().write(gson.toJson(lessons));
        }catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Định dạng ID môn học không hợp lệ cho AJAX: {0}", request.getParameter("subjectId"));
            response.getWriter().write(gson.toJson(new ArrayList<>())); // Trả về mảng rỗng hoặc lỗi
        }        
    }

    /**
     * Phương thức trợ giúp để lấy tên file từ Part.
     *
     * @param part Part chứa file upload.
     * @return Tên file.
     */
    private String getFileName(final Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}
