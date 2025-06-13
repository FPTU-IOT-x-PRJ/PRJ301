package DTO.User;

public class UserStatistics {
    private int newUsersThisMonth;
    private int adminUsers;
    private int activeUsers;
    private int totalUsers;

    // Constructor không đối số (quan trọng cho JavaBean)
    public UserStatistics() {
    }

    // Constructor với tất cả các trường (có thể giữ lại nếu bạn vẫn muốn tạo đối tượng dễ dàng)
    public UserStatistics(int newUsersThisMonth, int adminUsers, int activeUsers, int totalUsers) {
        this.newUsersThisMonth = newUsersThisMonth;
        this.adminUsers = adminUsers;
        this.activeUsers = activeUsers;
        this.totalUsers = totalUsers;
    }

    // Getters cho các thuộc tính
    public int getNewUsersThisMonth() {
        return newUsersThisMonth;
    }

    // Setters (tùy chọn cho DTO nếu không cần sửa đổi sau khi tạo)
    public void setNewUsersThisMonth(int newUsersThisMonth) {
        this.newUsersThisMonth = newUsersThisMonth;
    }

    public int getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(int adminUsers) {
        this.adminUsers = adminUsers;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }
}
