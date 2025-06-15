package controller;

import dao.UserDAO;
import entity.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class AuthController extends HttpServlet {
    UserDAO userDAO = new UserDAO();
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "action: {0}", action);
        switch (action != null ? action : "") {
            case "/register":
                displayRegister(request, response);
                break;
            case "/login":
                displayLogin(request, response);
                break;
            case "/logout":
            {
                HttpSession session = request.getSession(false); // Không tạo session mới
                if (session != null) {
                    String username = (session.getAttribute("loggedInUser") != null) ?
                                      ((User) session.getAttribute("loggedInUser")).getUsername() : "Unknown User";
                    session.invalidate(); // Hủy session
                    LOGGER.log(Level.INFO, "User {0} logged out.", username);
                }
                response.sendRedirect(request.getContextPath() + "/auth/login?logout=true"); // Chuyển hướng về trang đăng nhập
            }
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
            case "/register":
            {
                try {
                    register(request, response);
                } catch (SQLException ex) {
                    Logger.getLogger(AuthController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                break;
            case "/login":
                login(request, response);
                break;
            default:
                break;
        }
    }

    private void displayRegister(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/components/auth/register.jsp").forward(request, response);
    }

    private void displayLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        HttpSession session = request.getSession(false); // false để không tạo session mới nếu chưa có
        if (session != null && session.getAttribute("loggedInUser") != null) {
            // Đã đăng nhập, chuyển hướng về trang chủ hoặc dashboard tùy role
            User user = (User) session.getAttribute("loggedInUser");
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/user/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }
            return;
        }

        // Hiển thị trang đăng nhập
        if (request.getParameter("registerSuccess") != null) {
            request.setAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        request.getRequestDispatcher("/components/auth/login.jsp").forward(request, response);
    }
    
    private void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Basic Validation
        if (firstName == null || firstName.trim().isEmpty() ||
            lastName == null || lastName.trim().isEmpty() ||
            username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng điền đầy đủ tất cả các trường.");
            forwardToRegisterPage(request, response, firstName, lastName, username, email);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
            forwardToRegisterPage(request, response, firstName, lastName, username, email);
            return;
        }

        // Kiểm tra username và email đã tồn tại chưa
        if (userDAO.usernameExists(username)) {
            request.setAttribute("errorMessage", "Tên đăng nhập đã tồn tại.");
            forwardToRegisterPage(request, response, firstName, lastName, username, email);
            return;
        }
        if (userDAO.emailExists(email)) {
            request.setAttribute("errorMessage", "Email đã tồn tại.");
            forwardToRegisterPage(request, response, firstName, lastName, username, email);
            return;
        }

        try {
            // Hash mật khẩu trước khi lưu vào DB
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // Mặc định role là "User" cho người dùng mới đăng ký
            User newUser = new User(username, email, hashedPassword, firstName, lastName, "User");
            userDAO.insertUser(newUser);

            request.setAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/auth/login?registerSuccess=true"); // Chuyển hướng đến trang Login
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đăng ký người dùng: " + e.getMessage(), e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình đăng ký. Vui lòng thử lại.");
            forwardToRegisterPage(request, response, firstName, lastName, username, email);
        }        
    }

    private void forwardToRegisterPage(HttpServletRequest request, HttpServletResponse response,
                                       String firstName, String lastName, String username, String email)
            throws ServletException, IOException {
        request.setAttribute("formFirstName", firstName);
        request.setAttribute("formLastName", lastName);
        request.setAttribute("formUsername", username);
        request.setAttribute("formEmail", email);
        request.getRequestDispatcher("/components/auth/register.jsp").forward(request, response);
    }    
    
    private void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String identifier = request.getParameter("identifier"); // Có thể là username hoặc email
        String password = request.getParameter("password");

        if (identifier == null || identifier.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng nhập tên đăng nhập/email và mật khẩu.");
            request.getRequestDispatcher("/components/auth/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.authenticateUser(identifier, password);

        if (user != null) {
            // Đăng nhập thành công, tạo session
            HttpSession session = request.getSession(); // true by default, will create if not exists
            session.setAttribute("loggedInUser", user); // Lưu đối tượng User vào session
            session.setMaxInactiveInterval(60 * 60); // Session timeout sau 60 phút

            LOGGER.log(Level.INFO, "User logged in: {0} with role {1}", new Object[]{user.getUsername(), user.getRole()});

            // Chuyển hướng người dùng dựa trên vai trò
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/user/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }
        } else {
            // Đăng nhập thất bại
            request.setAttribute("errorMessage", "Tên đăng nhập/email hoặc mật khẩu không chính xác.");
            request.setAttribute("formIdentifier", identifier); // Giữ lại giá trị đã nhập
            request.getRequestDispatcher("/components/auth/login.jsp").forward(request, response);
        }        
    }
}
