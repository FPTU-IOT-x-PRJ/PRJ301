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
import java.util.Arrays;     // Import Arrays
import java.util.Collections; // Import Collections
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Không cần annotation @WebFilter nếu đã cấu hình trong web.xml
public class AuthFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());
    
    // Danh sách các đường dẫn /user/* không yêu cầu quyền Admin (chỉ cần đăng nhập)
    // Khởi tạo là hằng số (final) và tĩnh (static)
    private static final List<String> PUBLIC_ENDPOINTS_FOR_USER_PREFIX = Collections.unmodifiableList(Arrays.asList(
        "/user/profile",
        "/user/profile-edit"
    ));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.log(Level.INFO, "AuthFilter initialized. Public endpoints under /user/: {0}", PUBLIC_ENDPOINTS_FOR_USER_PREFIX);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String relativePath = path.substring(contextPath.length()); 

        LOGGER.log(Level.INFO, "Filtering request for path: {0}, relativePath: {1}", new Object[]{path, relativePath});

        // --- Kiểm tra đăng nhập ---
        HttpSession session = httpRequest.getSession(false); // Không tạo session mới

        boolean loggedIn = (session != null && session.getAttribute("loggedInUser") != null);
        User user = loggedIn ? (User) session.getAttribute("loggedInUser") : null;

        // Nếu cố gắng truy cập bất kỳ path nào bắt đầu bằng /user/
        if (relativePath.startsWith("/user/")) {
            // Kiểm tra xem đường dẫn có nằm trong danh sách các public endpoints không
            boolean isPublicUserEndpoint = PUBLIC_ENDPOINTS_FOR_USER_PREFIX.contains(relativePath);

            if (isPublicUserEndpoint) {
                // Các trang công khai (profile, editProfile) chỉ yêu cầu đã đăng nhập
                if (!loggedIn) {
                    LOGGER.log(Level.WARNING, "Unauthorized access attempt to public user endpoint {0}. Redirecting to login.", path);
                    httpResponse.sendRedirect(contextPath + "/auth/login?from=" + path); 
                    return;
                }
                // Nếu đã đăng nhập, cho phép truy cập. Không cần kiểm tra vai trò Admin cho các trang này.
            } else {
                // Đối với tất cả các đường dẫn /user/* khác, yêu cầu quyền Admin
                if (!loggedIn) {
                    LOGGER.log(Level.WARNING, "Unauthorized access attempt to protected user endpoint {0}. Redirecting to login.", path);
                    httpResponse.sendRedirect(contextPath + "/auth/login?from=" + path); 
                    return;
                } else {
                    // Đã đăng nhập, kiểm tra vai trò cho các trang chỉ dành cho Admin
                    if (user == null || !"Admin".equalsIgnoreCase(user.getRole())) {
                        LOGGER.log(Level.WARNING, "User {0} (Role: {1}) attempted to access admin page {2}. Access Denied.",
                                new Object[]{user != null ? user.getUsername() : "N/A", user != null ? user.getRole() : "N/A", path});
                        
                        httpResponse.sendRedirect(contextPath + "/?accessDenied=true");
                        return;
                    }
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