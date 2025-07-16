package config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import dal.DBInitializer;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class AppListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.log(Level.INFO, "Ứng dụng web đang khởi tạo. Bắt đầu khởi tạo cơ sở dữ liệu...");
        DBInitializer initializer = new DBInitializer();
        initializer.initializeDatabase(false);
        LOGGER.log(Level.INFO, "Khởi tạo cơ sở dữ liệu hoàn tất.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.log(Level.INFO, "Ứng dụng web đang tắt. Dọn dẹp tài nguyên...");
        // Không cần làm gì nhiều ở đây cho DBInitializer,
        // vì Connection đã được đóng trong DBContext hoặc tự động khi ứng dụng tắt.
    }
}