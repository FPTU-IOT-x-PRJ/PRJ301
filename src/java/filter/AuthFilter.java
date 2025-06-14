package filter;

import entity.User;
import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

// Map filter cho các URL pattern cần bảo vệ
// "/user/*" sẽ áp dụng cho tất cả các request đến controller.UserController (vì UserController map "/user/*")
public class AuthFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Khởi tạo Filter (nếu cần)
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        LOGGER.log(Level.INFO, "Filtering request for path: {0}", path);

        // --- Kiểm tra đăng nhập ---
        HttpSession session = httpRequest.getSession(false); // Không tạo session mới

        boolean loggedIn = (session != null && session.getAttribute("loggedInUser") != null);
        User user = loggedIn ? (User) session.getAttribute("loggedInUser") : null;

        // --- Logic phân quyền cho admin-dashboard ---
        // Yêu cầu: Chỉ Admin mới vào được /user/dashboard và các chức năng của user (thêm, sửa, xóa)
        // UserController của bạn xử lý "/user/*", nên filter này sẽ bắt tất cả
        
        // Nếu cố gắng truy cập bất kỳ path nào bắt đầu bằng /user/
        if (path.startsWith(contextPath + "/user/")) {
            if (!loggedIn) {
                LOGGER.log(Level.WARNING, "Unauthorized access attempt to {0}. Redirecting to login.", path);
                httpResponse.sendRedirect(contextPath + "/auth/login?from=" + path); // Chuyển hướng về trang đăng nhập
                return;
            } else {
                // Đã đăng nhập, kiểm tra vai trò
                if (user == null || !"Admin".equalsIgnoreCase(user.getRole())) {
                    LOGGER.log(Level.WARNING, "User {0} (Role: {1}) attempted to access admin page {2}. Access Denied.",
                            new Object[]{user != null ? user.getUsername() : "N/A", user != null ? user.getRole() : "N/A", path});
                    
                    // Chuyển hướng về trang chủ hoặc trang lỗi "Access Denied"
                    httpResponse.sendRedirect(contextPath + "/?accessDenied=true");
                    return;
                }
            }
        }
        
        // Nếu không thuộc các trường hợp bị chặn, cho phép request tiếp tục
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Hủy Filter (nếu cần)
    }
}