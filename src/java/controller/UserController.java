package controller;

import DTO.User.UserStatistics;
import dao.UserDAO;
import entity.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException; // Vẫn cần import nếu các phương thức DAO có thể ném SQLException trong tương lai hoặc cho các mục đích khác
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Controller xử lý các thao tác liên quan đến người dùng (User), chủ yếu là quản lý người dùng bởi Admin.
 */
public class UserController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());
    UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "Action received in UserController (GET): {0}", action);
        
        // Kiểm tra quyền truy cập của Admin nếu đây là các tác vụ quản lý Admin
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        // Giả định các action này chỉ dành cho Admin, nếu không phải Admin thì chuyển hướng.
        if (loggedInUser == null || !"Admin".equalsIgnoreCase(loggedInUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/auth/login"); // Hoặc trang lỗi quyền
            return;
        }

        try {
            switch (action != null ? action : "") {
                case "/dashboard": // Hiển thị danh sách người dùng cho Admin
                    displayUsers(request, response);
                    break;
                case "/add": // Hiển thị form thêm người dùng
                    displayAddForm(request, response);
                    break;
                case "/edit": // Hiển thị form chỉnh sửa người dùng
                    displayEditForm(request, response);
                    break;
                case "/detail": // Hiển thị chi tiết người dùng
                    displayUserDetail(request, response);
                    break;
                case "/delete-confirm": // Hiển thị form xác nhận xóa
                    displayDeleteConfirm(request, response);
                    break;
                // Các action liên quan đến profile cá nhân của user (không phải admin quản lý)
                case "/profile-edit":
                    displayProfileEditForm(request, response, loggedInUser.getId());
                    break;
                case "/profile":
                    displayProfile(request, response, loggedInUser.getId());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action không hợp lệ");
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong UserController doGet: " + action, e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "Action received in UserController (POST): {0}", action);
        request.setCharacterEncoding("UTF-8"); // Đảm bảo nhận tiếng Việt
        response.setCharacterEncoding("UTF-8"); // Đảm bảo gửi tiếng Việt

        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            switch (action != null ? action : "") {
                case "/add": // Xử lý thêm người dùng
                    addUser(request, response);
                    break;
                case "/edit": // Xử lý cập nhật thông tin người dùng (admin)
                    editUser(request, response);
                    break;
                case "/delete": // Xử lý xóa người dùng
                    deleteUser(request, response);
                    break;
                case "/profile-update": // Cập nhật thông tin cá nhân
                    updateUserProfile(request, response, loggedInUser.getId());
                    break;
                case "/password-change": // Đổi mật khẩu cá nhân
                    changeUserPassword(request, response, loggedInUser.getId());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action không hợp lệ");
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong UserController doPost: " + action, e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }    
    
    /**
     * Hiển thị bảng điều khiển người dùng (danh sách người dùng) cho Admin.
     * Tên hàm cũ: `displayDashboard`
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void displayUsers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String search = request.getParameter("search");
        String roleFilter = request.getParameter("role"); 
        String sortOrder = request.getParameter("sort"); 
        String pageStr = request.getParameter("page");
 
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = "createdAt_desc"; 
        }
            
        int currentPage = 1;
        int recordsPerPage = 10; 

        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                currentPage = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                currentPage = 1; 
            }
        }

        int offset = (currentPage - 1) * recordsPerPage;

        // Các phương thức DAO này đã được refactor để tự xử lý SQLException và trả về kết quả
        int totalUsers = userDAO.countUsers(search, roleFilter); 
        List<User> userList = userDAO.getAllUsers(search, roleFilter, sortOrder, offset, recordsPerPage); 

        UserStatistics userStatistics = userDAO.getUserStatistics();

        request.setAttribute("userList", userList);
        request.setAttribute("userStatistics", userStatistics);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", (int) Math.ceil((double) totalUsers / recordsPerPage));
        request.setAttribute("totalUsersCount", totalUsers); 

        Map<String, String> paginationParams = new HashMap<>();
        paginationParams.put("search", search != null ? search : "");
        paginationParams.put("role", roleFilter != null ? roleFilter : "");
        paginationParams.put("sort", sortOrder != null ? sortOrder : "");
        request.setAttribute("paginationParams", paginationParams);
        request.setAttribute("baseUrl", request.getContextPath() + "/user/dashboard");

        request.getRequestDispatcher("/components/user/admin-dashboard.jsp").forward(request, response);
    }
    
    /**
     * Hiển thị form chỉnh sửa thông tin người dùng (cho Admin).
     * Tên hàm cũ: `displayEditUser`
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void displayEditForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            User user = userDAO.getUserById(id); // getUserById đã tự xử lý SQLException
            if (user != null) {
                request.setAttribute("user", user);
                request.getRequestDispatcher("/components/user/admin-user-edit.jsp").forward(request, response);        
            } else {
                request.setAttribute("errorMessage", "Không tìm thấy người dùng để chỉnh sửa.");
                displayUsers(request, response); // Quay lại dashboard
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID người dùng không hợp lệ để chỉnh sửa: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID người dùng không hợp lệ.");
            displayUsers(request, response);
        } catch (Exception e) { // Catch Exception chung cho các lỗi khác nếu có
            LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị form chỉnh sửa người dùng: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn.");
            displayUsers(request, response);
        }
    }
    
    /**
     * Hiển thị chi tiết của một người dùng.
     * Tên hàm cũ: `displayGetUser`
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void displayUserDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            User user = userDAO.getUserById(id); 
            if (user != null) {
                request.setAttribute("user", user);
                request.getRequestDispatcher("/components/user/admin-user-detail.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Không tìm thấy người dùng bạn muốn xem chi tiết.");
                displayUsers(request, response);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID người dùng không hợp lệ để xem chi tiết: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID người dùng không hợp lệ.");
            displayUsers(request, response);
        } catch (Exception e) { // Catch Exception chung cho các lỗi khác nếu có
            LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị chi tiết người dùng: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn.");
            displayUsers(request, response);
        }
    }
    
    /**
     * Hiển thị form xác nhận xóa người dùng.
     * Tên hàm cũ: `displayDeleteUser`
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void displayDeleteConfirm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            User user = userDAO.getUserById(id); 
            if (user != null) {
                request.setAttribute("userToDelete", user);
                request.getRequestDispatcher("/components/user/admin-user-delete-confirm.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Không tìm thấy người dùng bạn muốn xóa.");
                displayUsers(request, response);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID người dùng không hợp lệ để xác nhận xóa: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID người dùng không hợp lệ.");
            displayUsers(request, response);
        } catch (Exception e) { // Catch Exception chung cho các lỗi khác nếu có
            LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị form xác nhận xóa người dùng: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn.");
            displayUsers(request, response);
        }
    }    

    /**
     * Hiển thị form để thêm người dùng mới.
     * Tên hàm cũ: `add` (trong doGet)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void displayAddForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/components/user/admin-user-add.jsp").forward(request, response);
    }
    
    /**
     * Xử lý logic thêm người dùng mới vào DB.
     * Tên hàm cũ: `addUser` (trong doPost)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void addUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        Map<String, String> errors = new HashMap<>();

        // Basic Validation
        if (firstName == null || firstName.trim().isEmpty() ||
            lastName == null || lastName.trim().isEmpty() ||
            username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            role == null || role.trim().isEmpty()) {
            errors.put("general", "Vui lòng điền đầy đủ tất cả các trường.");
        }

        // Kiểm tra username và email đã tồn tại chưa
        if (userDAO.isUsernameExists(username)) { // isUsernameExists đã tự xử lý SQLException
            errors.put("username", "Tên đăng nhập đã tồn tại.");
        }
        if (userDAO.isEmailExists(email)) { // isEmailExists đã tự xử lý SQLException
            errors.put("email", "Email đã tồn tại.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập.");
            request.setAttribute("formFirstName", firstName);
            request.setAttribute("formLastName", lastName);
            request.setAttribute("formUsername", username);
            request.setAttribute("formEmail", email);
            request.setAttribute("formRole", role);
            request.getRequestDispatcher("/components/user/admin-user-add.jsp").forward(request, response);
            return;
        }

        try {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            User newUser = new User(username, email, hashedPassword, firstName, lastName, role);
            userDAO.addUser(newUser); // addUser đã tự xử lý SQLException
            response.sendRedirect(request.getContextPath() + "/user/dashboard?message=addSuccess");
        } catch (Exception e) { // Catch Exception chung cho các lỗi khác (bao gồm cả lỗi từ DAO nếu có)
            LOGGER.log(Level.SEVERE, "Đã xảy ra lỗi không mong muốn khi thêm người dùng.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi thêm người dùng.");
            request.setAttribute("formFirstName", firstName);
            request.setAttribute("formLastName", lastName);
            request.setAttribute("formUsername", username);
            request.setAttribute("formEmail", email);
            request.setAttribute("formRole", role);
            request.getRequestDispatcher("/components/user/admin-user-add.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý logic cập nhật thông tin người dùng (admin).
     * Tên hàm cũ: `editUser` (trong doPost)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void editUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String username = request.getParameter("username"); 
        String email = request.getParameter("email");
        String role = request.getParameter("role");

        Map<String, String> errors = new HashMap<>();

        // Lấy thông tin người dùng cũ để giữ lại username, email
        User existingUser = userDAO.getUserById(id); // getUserById đã tự xử lý SQLException
        if (existingUser == null) {
            errors.put("general", "Người dùng không tồn tại.");
        }

        if (firstName == null || firstName.trim().isEmpty() ||
            lastName == null || lastName.trim().isEmpty() ||
            role == null || role.trim().isEmpty()) {
            errors.put("general", "Vui lòng điền đầy đủ các trường Tên, Họ, Vai trò.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập.");
            // Đảm bảo user object được set đúng để điền lại form
            if (existingUser != null) {
                request.setAttribute("user", new User(id, existingUser.getUsername(), existingUser.getEmail(), firstName, lastName, role));
            } else {
                 request.setAttribute("user", new User(id, username, email, firstName, lastName, role));
            }
            request.getRequestDispatcher("/components/user/admin-user-edit.jsp").forward(request, response);
            return;
        }

        try {
            // Chỉ cập nhật firstName, lastName, role. Username và email giữ nguyên từ existingUser.
            User updatedUser = new User(id, existingUser.getUsername(), existingUser.getEmail(), firstName, lastName, role);
            boolean success = userDAO.editUser(updatedUser); // editUser đã tự xử lý SQLException

            if (success) {
                response.sendRedirect(request.getContextPath() + "/user/dashboard?message=editSuccess");
            } else {
                LOGGER.log(Level.SEVERE, "Không thể cập nhật người dùng với ID: {0}", id);
                request.setAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật người dùng. Vui lòng thử lại.");
                request.setAttribute("user", existingUser); // Quay lại với dữ liệu ban đầu nếu cập nhật thất bại
                request.getRequestDispatcher("/components/user/admin-user-edit.jsp").forward(request, response);
            }
        } catch (Exception e) { // Catch Exception chung cho các lỗi khác
            LOGGER.log(Level.SEVERE, "Đã xảy ra lỗi không mong muốn khi cập nhật người dùng.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi cập nhật người dùng.");
            request.setAttribute("user", existingUser);
            request.getRequestDispatcher("/components/user/admin-user-edit.jsp").forward(request, response);
        }
    }
    
    /**
     * Xử lý logic xóa người dùng.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException
     */
    private void deleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            boolean success = userDAO.deleteUser(id); // deleteUser đã tự xử lý SQLException

            if (success) {
                response.sendRedirect(request.getContextPath() + "/user/dashboard?message=deleteSuccess");
            } else {
                LOGGER.log(Level.WARNING, "Không thể xóa người dùng với ID {0}.", id);
                request.setAttribute("errorMessage", "Không thể xóa người dùng. Có thể người dùng không tồn tại hoặc có dữ liệu liên quan.");
                displayUsers(request, response);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID người dùng không hợp lệ để xóa: {0}", request.getParameter("id"));
            request.setAttribute("errorMessage", "ID người dùng không hợp lệ.");
            displayUsers(request, response);
        } catch (Exception e) { // Catch Exception chung cho các lỗi khác
            LOGGER.log(Level.SEVERE, "Đã xảy ra lỗi không mong muốn khi xóa người dùng.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi xóa người dùng.");
            displayUsers(request, response);
        }
    }

    /**
     * Hiển thị form chỉnh sửa hồ sơ cá nhân của người dùng.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng đang đăng nhập
     * @throws ServletException
     * @throws IOException
     */
    private void displayProfileEditForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        try {
            User user = userDAO.getUserById(userId);
            if (user != null) {
                request.setAttribute("user", user);
                request.getRequestDispatcher("/components/user/user-edit.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Không tìm thấy hồ sơ của bạn.");
                response.sendRedirect(request.getContextPath() + "/"); // Quay về trang chủ
            }
        } catch (Exception e) { // Catch Exception chung cho các lỗi khác
            LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị form chỉnh sửa hồ sơ: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Lỗi cơ sở dữ liệu khi tải hồ sơ.");
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

    /**
     * Hiển thị trang hồ sơ cá nhân của người dùng.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng đang đăng nhập
     * @throws ServletException
     * @throws IOException
     */
    private void displayProfile(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        try {
            User user = userDAO.getUserById(userId);
            if (user != null) {
                request.setAttribute("user", user);
                request.getRequestDispatcher("/components/user/user-profile.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Không tìm thấy hồ sơ của bạn.");
                response.sendRedirect(request.getContextPath() + "/");
            }
        } catch (Exception e) { // Catch Exception chung cho các lỗi khác
            LOGGER.log(Level.SEVERE, "Lỗi khi hiển thị hồ sơ: {0}", e.getMessage());
            request.setAttribute("errorMessage", "Lỗi cơ sở dữ liệu khi tải hồ sơ.");
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
    
    /**
     * Xử lý cập nhật hồ sơ cá nhân của người dùng (không đổi mật khẩu, email, username).
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng đang đăng nhập
     * @throws ServletException
     * @throws IOException
     */
    private void updateUserProfile(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        
        Map<String, String> errors = new HashMap<>();

        if (firstName == null || firstName.trim().isEmpty()) {
            errors.put("firstName", "Tên không được để trống.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            errors.put("lastName", "Họ không được để trống.");
        }

        User existingUser = userDAO.getUserById(userId); // getUserById đã tự xử lý SQLException
        if (existingUser == null) {
            errors.put("general", "Hồ sơ người dùng không tồn tại.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập.");
            request.setAttribute("user", new User(userId, existingUser.getUsername(), existingUser.getEmail(), firstName, lastName, existingUser.getRole()));
            request.getRequestDispatcher("/components/user/user-edit.jsp").forward(request, response);
            return;
        }

        try {
            // Cập nhật thông tin profile. Username, email, password, role sẽ giữ nguyên từ DB.
            User userToUpdate = new User(userId, existingUser.getUsername(), existingUser.getEmail(), firstName, lastName, existingUser.getRole());
            boolean success = userDAO.editUser(userToUpdate); // editUser đã tự xử lý SQLException

            if (success) {
                // Cập nhật lại session user nếu thông tin profile thay đổi
                existingUser.setFirstName(firstName);
                existingUser.setLastName(lastName);
                request.getSession().setAttribute("loggedInUser", existingUser);
                response.sendRedirect(request.getContextPath() + "/user/profile?message=updateSuccess");
            } else {
                LOGGER.log(Level.SEVERE, "Không thể cập nhật hồ sơ người dùng ID: {0}", userId);
                request.setAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật hồ sơ. Vui lòng thử lại.");
                request.setAttribute("user", existingUser);
                request.getRequestDispatcher("/components/user/user-edit.jsp").forward(request, response);
            }
        } catch (Exception e) { // Catch Exception chung cho các lỗi khác
            LOGGER.log(Level.SEVERE, "Đã xảy ra lỗi không mong muốn khi cập nhật hồ sơ người dùng.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi cập nhật hồ sơ người dùng.");
            request.setAttribute("user", existingUser);
            request.getRequestDispatcher("/components/user/user-edit.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý đổi mật khẩu của người dùng.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param userId ID của người dùng đang đăng nhập
     * @throws ServletException
     * @throws IOException
     */
    private void changeUserPassword(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmNewPassword = request.getParameter("confirmNewPassword");

        Map<String, String> errors = new HashMap<>();

        if (oldPassword == null || oldPassword.trim().isEmpty() ||
            newPassword == null || newPassword.trim().isEmpty() ||
            confirmNewPassword == null || confirmNewPassword.trim().isEmpty()) {
            errors.put("general", "Vui lòng điền đầy đủ tất cả các trường.");
        }

        if (!newPassword.equals(confirmNewPassword)) {
            errors.put("confirmNewPassword", "Mật khẩu mới và xác nhận mật khẩu mới không khớp.");
        }

        User user = userDAO.getUserById(userId); // getUserById đã tự xử lý SQLException
        if (user == null) {
            errors.put("general", "Người dùng không tồn tại.");
        } else if (!BCrypt.checkpw(oldPassword, user.getPassword())) { 
            errors.put("oldPassword", "Mật khẩu cũ không chính xác.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập.");
            request.getRequestDispatcher("/components/user/user-edit.jsp").forward(request, response); 
            return;
        }

        try {
            String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            boolean success = userDAO.updatePassword(userId, hashedNewPassword); // updatePassword đã tự xử lý SQLException

            if (success) {
                request.getSession().setAttribute("message", "Đổi mật khẩu thành công!");
                response.sendRedirect(request.getContextPath() + "/user/profile"); // Quay về trang profile
            } else {
                LOGGER.log(Level.SEVERE, "Không thể đổi mật khẩu cho người dùng ID: {0}", userId);
                request.setAttribute("errorMessage", "Có lỗi xảy ra khi đổi mật khẩu. Vui lòng thử lại.");
                request.getRequestDispatcher("/components/user/user-edit.jsp").forward(request, response);
            }
        } catch (Exception e) { // Catch Exception chung cho các lỗi khác
            LOGGER.log(Level.SEVERE, "Đã xảy ra lỗi không mong muốn khi đổi mật khẩu.", e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi đổi mật khẩu.");
            request.getRequestDispatcher("/components/user/user-edit.jsp").forward(request, response);
        }
    }
}
