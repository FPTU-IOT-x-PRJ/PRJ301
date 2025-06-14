
package controller;

import DTO.User.UserStatistics;
import dao.UserDAO;
import entity.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class UserController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());
    UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "action: {0}", action);
        
        switch (action != null ? action : "") {
            case "/dashboard":
                displayDashboard(request, response);
                break;
            case "/add":
                request.getRequestDispatcher("/components/user/admin-user-add.jsp").forward(request, response);
                break;
            case "/edit":
                displayEditUser(request, response);
                break;
            case "/detail":
                displayGetUser(request, response);
                break;
            case "/delete-confirm":
                displayDeleteUser(request, response);
                break;
            case "/profile-edit":
                request.getRequestDispatcher("/components/user/user-edit.jsp").forward(request, response);
                break;
            case "/profile":
                request.getRequestDispatcher("/components/user/user-profile.jsp").forward(request, response);
                break;
            default:
                break;
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "action: {0}", action);
        switch (action != null ? action : "") {
            case "/add":
                addUser(request, response);
                break;
            case "/edit":
                editUser(request, response);
                break;
            case "/delete":
                deleteUser(request, response);
            default:
                break;
        }
    }    
    
    private void displayDashboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Lấy các tham số từ request
        String search = request.getParameter("search");
        String roleFilter = request.getParameter("role"); // "Admin" hoặc "User"
        String sortOrder = request.getParameter("sort"); // "createdAt_desc", "firstName_asc", v.v.
        String pageStr = request.getParameter("page");

        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = "createdAt_desc"; // Giá trị mặc định bạn muốn
        }
            
        // Thiết lập giá trị mặc định cho phân trang
        int currentPage = 1;
        int recordsPerPage = 10; // Số người dùng mỗi trang

        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                currentPage = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                currentPage = 1; // Mặc định về trang 1 nếu page không hợp lệ
            }
        }

        int offset = (currentPage - 1) * recordsPerPage;

        // 2. Lấy tổng số người dùng (cho phân trang)
        int totalUsers = userDAO.getTotalUserCount(search, roleFilter);
        int totalPages = (int) Math.ceil((double) totalUsers / recordsPerPage);

        // 3. Lấy danh sách người dùng đã lọc và phân trang
        List<User> userList = userDAO.getFilteredAndPaginatedUsers(search, roleFilter, sortOrder, offset, recordsPerPage);

        // 4. Lấy thống kê người dùng (nếu cần trên dashboard)
        UserStatistics userStatistics = userDAO.getUserStatistics();

        // 5. Đặt các thuộc tính vào request để JSP hiển thị
        request.setAttribute("userList", userList);
        request.setAttribute("userStatistics", userStatistics);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalUsersCount", totalUsers); // Tổng số người dùng sau khi lọc

        // Đặt lại các tham số lọc vào request để giữ trạng thái trên form
        // (Hoặc bạn có thể dùng ${param.search} trực tiếp trong JSP như bạn đã làm, cũng rất tốt)
        request.setAttribute("searchParam", search);
        request.setAttribute("roleFilterParam", roleFilter);
        request.setAttribute("sortOrderParam", sortOrder);
        
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", currentPage);

        // 6. Chuyển tiếp đến JSP
        request.getRequestDispatcher("/components/user/admin-dashboard.jsp").forward(request, response);
    }
    
    private void displayEditUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt((String) request.getParameter("id"));
        User user = userDAO.selectUserById(id);
        request.setAttribute("user", user);
        request.getRequestDispatcher("/components/user/admin-user-edit.jsp").forward(request, response);        
    }
    
    private void displayGetUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        User user = userDAO.selectUserById(id);
        request.setAttribute("user", user);
        request.getRequestDispatcher("/components/user/admin-user-detail.jsp").forward(request, response);
    }
    
    private void displayDeleteUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        User user = userDAO.selectUserById(id);
        request.setAttribute("userToDelete", user);
        request.getRequestDispatcher("/components/user/admin-user-delete-confirm.jsp").forward(request, response);
    }    
    
    private void addUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        try {
            String salt = BCrypt.gensalt();
            String hashedPassword = BCrypt.hashpw(password, salt);
            
            LOGGER.log(Level.INFO, "Password from form: {0}", password);
            LOGGER.log(Level.INFO, "Generated Salt: {0}", salt);
            LOGGER.log(Level.INFO, "Hashed Password: {0}", hashedPassword);
            
            userDAO.insertUser(new User(username, email, hashedPassword, firstName, lastName, role));
            response.sendRedirect(request.getContextPath() + "/user/dashboard");
        } catch (SQLException e) {
            // Nếu lỗi là do trùng UNIQUE key
            if (e.getMessage().contains("UQ__Users")) {
                LOGGER.log(Level.WARNING, "Lỗi trùng email hoặc username: {0}", e.getMessage());
                request.setAttribute("errorMessage", "Email hoặc tên đăng nhập đã tồn tại. Vui lòng chọn giá trị khác.");
            } else {
                LOGGER.log(Level.SEVERE, "Lỗi thêm người dùng", e);
                request.setAttribute("errorMessage", "Đã xảy ra lỗi khi thêm người dùng.");
            }

            // Giữ lại dữ liệu đã nhập (nếu muốn)
            request.setAttribute("formFirstName", firstName);
            request.setAttribute("formLastName", lastName);
            request.setAttribute("formUsername", username);
            request.setAttribute("formEmail", email);
            request.setAttribute("formRole", role);

            request.getRequestDispatcher("/components/user/admin-user-add.jsp").forward(request, response);
        }
    }

    private void editUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String role = request.getParameter("role");

        userDAO.updateUserProfile(new User(id, username, email, firstName, lastName, role));
        response.sendRedirect(request.getContextPath() + "/user/dashboard");
    }
    
    private void deleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        userDAO.deleteUser(id);
        response.sendRedirect(request.getContextPath() + "/user/dashboard");
    }
}
