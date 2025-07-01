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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;
import utils.MailUtils;

/**
 * Controller xử lý các tác vụ liên quan đến xác thực người dùng: Đăng ký, Đăng
 * nhập, Đăng xuất.
 */
public class AuthController extends HttpServlet {

    UserDAO userDAO = new UserDAO();
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "Action received in AuthController (GET): {0}", action);
        switch (action != null ? action : "") {
            case "/register":
                displayRegisterForm(request, response);
                break;
            case "/login":
                displayLoginForm(request, response);
                break;
            case "/logout":
                logoutUser(request, response);
                break;
            case "/forgot-password":
                forgotPasswordForm(request, response);
                break;
            case "/verify-code":
                verifyCodeForm(request, response);
                break;
            default:
                // Nếu không có action cụ thể, chuyển hướng về trang đăng nhập
                response.sendRedirect(request.getContextPath() + "/auth/login");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.log(Level.INFO, "Action received in AuthController (POST): {0}", action);
        request.setCharacterEncoding("UTF-8"); // Đảm bảo nhận tiếng Việt
        response.setCharacterEncoding("UTF-8"); // Đảm bảo gửi tiếng Việt

        switch (action != null ? action : "") {
            case "/register":
                addRegisterUser(request, response);
                break;
            case "/login":
                authenticateUser(request, response);
                break;
            case "/forgot-password":
                sendVerifyCode(request, response);
                break;
            case "/reset-password":
                resetPassword(request, response);
                break;
            default:
                // Nếu POST đến URL không xác định, có thể chuyển hướng về trang đăng nhập hoặc báo lỗi
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action không hợp lệ");
                break;
        }
    }

    /**
     * Hiển thị form đăng ký.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void displayRegisterForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/components/auth/register.jsp").forward(request, response);
    }

    /**
     * Hiển thị form đăng nhập. Kiểm tra nếu đã đăng nhập thì chuyển hướng.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException
     * @throws ServletException
     */
    private void displayLoginForm(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false); // false để không tạo session mới nếu chưa có
        if (session != null && session.getAttribute("loggedInUser") != null) {
            User user = (User) session.getAttribute("loggedInUser");
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/user/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }
            return;
        }

        if (request.getParameter("registerSuccess") != null) {
            request.setAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        request.getRequestDispatcher("/components/auth/login.jsp").forward(request, response);
    }

    /**
     * Xử lý logic đăng ký người dùng mới.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void addRegisterUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Basic Validation
        if (firstName == null || firstName.trim().isEmpty()
                || lastName == null || lastName.trim().isEmpty()
                || username == null || username.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng điền đầy đủ tất cả các trường.");
            forwardToRegisterPageWithInput(request, response, firstName, lastName, username, email);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
            forwardToRegisterPageWithInput(request, response, firstName, lastName, username, email);
            return;
        }

        // Kiểm tra username và email đã tồn tại chưa bằng các phương thức mới trong DAO
        if (userDAO.isUsernameExists(username)) {
            request.setAttribute("errorMessage", "Tên đăng nhập đã tồn tại.");
            forwardToRegisterPageWithInput(request, response, firstName, lastName, username, email);
            return;
        }
        if (userDAO.isEmailExists(email)) {
            request.setAttribute("errorMessage", "Email đã tồn tại.");
            forwardToRegisterPageWithInput(request, response, firstName, lastName, username, email);
            return;
        }

        try {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            User newUser = new User(username, email, hashedPassword, firstName, lastName, "User"); // Mặc định role là "User"
            userDAO.addUser(newUser); // Sử dụng phương thức addUsers

            request.setAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/auth/login?registerSuccess=true");
        } catch (Exception e) { // Bắt Exception chung để xử lý lỗi DAO hoặc bất ngờ
            LOGGER.log(Level.SEVERE, "Lỗi khi đăng ký người dùng: " + e.getMessage(), e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình đăng ký. Vui lòng thử lại.");
            forwardToRegisterPageWithInput(request, response, firstName, lastName, username, email);
        }
    }

    /**
     * Chuyển tiếp đến trang đăng ký, giữ lại các giá trị đã nhập trên form.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param firstName
     * @param lastName
     * @param username
     * @param email
     * @throws ServletException
     * @throws IOException
     */
    private void forwardToRegisterPageWithInput(HttpServletRequest request, HttpServletResponse response,
            String firstName, String lastName, String username, String email)
            throws ServletException, IOException {
        request.setAttribute("formFirstName", firstName);
        request.setAttribute("formLastName", lastName);
        request.setAttribute("formUsername", username);
        request.setAttribute("formEmail", email);
        request.getRequestDispatcher("/components/auth/register.jsp").forward(request, response);
    }

    /**
     * Xử lý logic xác thực người dùng khi đăng nhập.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    private void authenticateUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String identifier = request.getParameter("identifier");
        String password = request.getParameter("password");

        if (identifier == null || identifier.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng nhập tên đăng nhập/email và mật khẩu.");
            request.getRequestDispatcher("/components/auth/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.authenticateUser(identifier, password); // Sử dụng phương thức authenticateUser

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("loggedInUser", user);
            session.setMaxInactiveInterval(60 * 60);

            LOGGER.log(Level.INFO, "User logged in: {0} with role {1}", new Object[]{user.getUsername(), user.getRole()});

            if ("Admin".equalsIgnoreCase(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/user/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }
        } else {
            request.setAttribute("errorMessage", "Tên đăng nhập/email hoặc mật khẩu không chính xác.");
            request.setAttribute("formIdentifier", identifier);
            request.getRequestDispatcher("/components/auth/login.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý đăng xuất người dùng.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException
     */
    private void logoutUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (session.getAttribute("loggedInUser") != null)
                    ? ((User) session.getAttribute("loggedInUser")).getUsername() : "Unknown User";
            session.invalidate();
            LOGGER.log(Level.INFO, "User {0} logged out.", username);
        }
        response.sendRedirect(request.getContextPath() + "/auth/login?logout=true");
    }

    private void forgotPasswordForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/components/auth/forgot-password.jsp").forward(request, response);
    }

    private void sendVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String email = request.getParameter("email");
        String code = request.getParameter("code");

        // Get the reset code from the session
        String storedResetCode = (String) request.getSession().getAttribute("resetCode");
        Long storedTimestamp = (Long) request.getSession().getAttribute("resetCodeTimestamp");

        if (storedResetCode != null && storedTimestamp != null) {
            // Check if the reset code is valid and not expired (e.g., 15 minutes validity)
            long currentTime = System.currentTimeMillis();
            if (currentTime - storedTimestamp < 15 * 60 * 1000 && storedResetCode.equals(code)) {
                // Reset code is valid
//                response.sendRedirect("/components/auth/re-password.jsp?email=" + email);
                request.setAttribute("email", email);
                request.getRequestDispatcher("/components/auth/re-password.jsp?email=" + email).forward(request, response);

            } else {
                // Invalid or expired code
                response.getWriter().println("Invalid or expired reset code.");
            }
        } else {
            // No reset code in session, possibly expired or not generated
            response.getWriter().println("No reset code found.");
        }

    }

    private void verifyCodeForm(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String email = request.getParameter("email");

        // Generate a reset code
        String resetCode = generateResetCode();

        // Store the reset code in the session (with an expiration time)
        request.getSession().setAttribute("resetCode", resetCode);
        request.getSession().setAttribute("resetCodeTimestamp", System.currentTimeMillis());

        // Send the reset code to the user's email
        MailUtils.send(email, "Password Reset", "Your reset code is: " + resetCode);

        // Redirect to enter code page
//        response.sendRedirect("/components/auth/verify-code.jsp");
        request.getRequestDispatcher("/components/auth/verify-code.jsp").forward(request, response);

    }

    private String generateResetCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;  // 6-digit code
        return String.valueOf(code);
    }

    private void resetPassword(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String email = request.getParameter("email");
        String newPassword = request.getParameter("newPassword");
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        // Update the user's password in the database
        UserDAO userdao = new UserDAO();
        userdao.updatePasswordByEmail(email, hashedPassword);

        // Clear the reset code from the session
        request.getSession().removeAttribute("resetCode");
        request.getSession().removeAttribute("resetCodeTimestamp");

        // Redirect to the login page or a success page
//        response.sendRedirect("/components/auth/login.jsp"); 
        request.getRequestDispatcher("/components/auth/login.jsp").forward(request, response);

    }

}
