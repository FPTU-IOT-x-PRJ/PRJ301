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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());
    
    // Danh sách các đường dẫn cụ thể chỉ cần đăng nhập (không cần Admin)
    private static final List<String> USER_ENDPOINTS = Collections.unmodifiableList(Arrays.asList(
        "/user/profile",
        "/user/profile-edit"
    ));
    
    // Danh sách các prefix pattern chỉ cần đăng nhập (không cần Admin)
    private static final List<String> USER_ENDPOINT_PREFIXES = Collections.unmodifiableList(Arrays.asList(
        "/semesters"
    ));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.log(Level.INFO, "AuthFilter initialized. User endpoints: {0}, User prefixes: {1}", 
                  new Object[]{USER_ENDPOINTS, USER_ENDPOINT_PREFIXES});
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

        // Kiểm tra đăng nhập
        HttpSession session = httpRequest.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("loggedInUser") != null);
        User user = loggedIn ? (User) session.getAttribute("loggedInUser") : null;

        // Kiểm tra xem có phải endpoint chỉ cần đăng nhập không
        boolean isUserEndpoint = isUserEndpoint(relativePath);

        if (isUserEndpoint) {
            // Các trang chỉ yêu cầu đã đăng nhập (không cần Admin)
            if (!loggedIn) {
                LOGGER.log(Level.WARNING, "Unauthorized access attempt to user endpoint {0}. Redirecting to login.", path);
                httpResponse.sendRedirect(contextPath + "/auth/login?from=" + path); 
                return;
            }
            // Đã đăng nhập -> cho phép truy cập
        } else {
            // Các trang khác yêu cầu quyền Admin
            if (!loggedIn) {
                LOGGER.log(Level.WARNING, "Unauthorized access attempt to admin endpoint {0}. Redirecting to login.", path);
                httpResponse.sendRedirect(contextPath + "/auth/login?from=" + path); 
                return;
            } else {
                // Đã đăng nhập, kiểm tra vai trò Admin
                if (user == null || !"Admin".equalsIgnoreCase(user.getRole())) {
                    LOGGER.log(Level.WARNING, "User {0} (Role: {1}) attempted to access admin page {2}. Access Denied.",
                            new Object[]{user != null ? user.getUsername() : "N/A", user != null ? user.getRole() : "N/A", path});

                    httpResponse.sendRedirect(contextPath + "/?accessDenied=true");
                    return;
                }
            }
        }
        
        // Cho phép request tiếp tục
        chain.doFilter(request, response);
    }

    /**
     * Kiểm tra xem đường dẫn có phải là endpoint chỉ cần đăng nhập không
     */
    private boolean isUserEndpoint(String relativePath) {
        // Kiểm tra exact match
        if (USER_ENDPOINTS.contains(relativePath)) {
            return true;
        }
        
        // Kiểm tra prefix match
        for (String prefix : USER_ENDPOINT_PREFIXES) {
            if (relativePath.startsWith(prefix)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void destroy() {
        // Hủy Filter (nếu cần)
    }
}
