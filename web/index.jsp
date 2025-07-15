<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <title>Trang chủ - EduPlan</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/favicon.ico" type="image/x-icon">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
        <link href="https://unpkg.com/aos@2.3.1/dist/aos.css" rel="stylesheet">
        <style>
            body {
                background-color: var(--light-color);
                /* Loại bỏ padding mặc định nếu bạn muốn hero section sát cạnh */
                padding-top: 0;
            }
            .welcome-card .card-header {
                background-color: var(--dark-color);
                color: var(--white-color);
            }

            /* --- Custom Styles cho trang chủ mới --- */
            .hero-section {
                /* Đảm bảo chiều cao đủ lớn để hiển thị ảnh nền và nội dung */
                min-height: 100vh; /* Chiều cao tối thiểu là 100% của viewport để full màn hình */

                /* Thiết lập ảnh nền */
                background: linear-gradient(rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.6)), 
                            url('${pageContext.request.contextPath}/public/images/hero-bg.png') no-repeat center center;
                background-size: cover; /* Đảm bảo ảnh bao phủ toàn bộ phần tử */
                background-attachment: scroll; /* Giữ ảnh cố định khi cuộn nếu muốn parallax, hoặc scroll */

                /* Căn giữa nội dung (flexbox) */
                display: flex;
                align-items: center; /* Căn giữa theo chiều dọc */
                justify-content: center; /* Căn giữa theo chiều ngang */
                flex-direction: column; /* Sắp xếp các mục con theo cột */

                color: white; /* Màu chữ trắng cho dễ đọc trên nền tối */
                padding: 100px 0; /* Khoảng cách trên dưới cho nội dung */
                text-align: center; /* Đảm bảo text được căn giữa */
            }

            .hero-section h1 {
                font-size: 3.5rem;
                font-weight: 700;
                margin-bottom: 20px;
                color: var(--white-color); /* Đảm bảo màu trắng cho tiêu đề */
            }

            .hero-section p {
                font-size: 1.25rem;
                max-width: 800px; /* Giới hạn chiều rộng của đoạn văn */
                margin: 0 auto 30px auto; /* Căn giữa khối p và thêm margin-bottom */
                line-height: 1.6; /* Tăng khoảng cách dòng để dễ đọc hơn (tùy chọn) */
            }

            .section-padding {
                padding: 80px 0;
            }
            .section-heading {
                font-size: 2.5rem;
                font-weight: 700;
                color: var(--dark-color);
                margin-bottom: 50px;
                text-align: center;
                position: relative;
            }
            .section-heading::after {
                content: '';
                display: block;
                width: 80px;
                height: 4px;
                background-color: var(--primary-color);
                margin: 15px auto 0;
                border-radius: 2px;
            }

            /* Team Section */
            .team-member {
                text-align: center;
                margin-bottom: 30px;
            }
            .team-member img {
                width: 180px;
                height: 180px;
                border-radius: 50%;
                object-fit: cover;
                border: 5px solid var(--primary-color);
                margin-bottom: 20px;
                transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
            }
            .team-member img:hover {
                transform: scale(1.05);
                box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
            }
            .team-member h4 {
                color: var(--dark-color);
                font-weight: 600;
                margin-bottom: 5px;
            }
            .team-member p.text-muted {
                font-size: 0.95rem;
                margin-bottom: 15px;
            }
            .team-member .social-links a {
                color: var(--secondary-color);
                margin: 0 8px;
                font-size: 1.2rem;
                transition: color 0.3s ease;
            }
            .team-member .social-links a:hover {
                color: var(--primary-color);
            }

            /* Features Section */
            .feature-item {
                text-align: center;
                padding: 30px 20px;
                border-radius: 8px;
                background-color: var(--white-color);
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
                transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
                height: 100%; /* Đảm bảo chiều cao đồng nhất */
            }
            .feature-item:hover {
                transform: translateY(-5px);
                box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
            }
            .feature-item i {
                font-size: 3.5rem;
                color: var(--primary-color);
                margin-bottom: 20px;
            }
            .feature-item h3 {
                font-size: 1.5rem;
                font-weight: 600;
                margin-bottom: 15px;
            }
            .feature-item p {
                color: var(--secondary-color);
            }

        /* Tùy chọn: Thêm một gradient nhẹ hoặc pattern để tạo chiều sâu */
        .cta-section::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: radial-gradient(circle at center, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0) 70%); /* Hiệu ứng ánh sáng nhẹ ở giữa */
            opacity: 0.8;
            z-index: 1;
            pointer-events: none; /* Không chặn tương tác với nội dung */
        }

        .cta-section .container {
            max-width: 960px;
            margin: 0 auto;
            position: relative;
            z-index: 2;
            text-align: center;
        }


        .cta-section h2 {
            font-size: 3rem; /* Tăng kích thước tiêu đề */
            font-weight: 700;
            color: var(--white-color);
            margin-bottom: 30px; /* Tăng khoảng cách */
            line-height: 1.3;
        }

        .cta-section p.lead {
            font-size: 1.3rem; /* Tăng kích thước đoạn văn */
            max-width: 700px; /* Giới hạn chiều rộng đoạn văn */
            margin: 0 auto 50px auto; /* Tăng margin-bottom */
            line-height: 1.7;
            color: rgba(255, 255, 255, 0.9); /* Màu trắng nhạt hơn */
        }

        .cta-section .btn {
            margin: 0 15px; /* Tăng khoảng cách giữa các nút */
            padding: 15px 30px; /* Tăng padding nút để nút lớn hơn */
            font-size: 1.2rem; /* Tăng font size của chữ trong nút */
            border-radius: 8px; /* Bo góc nhẹ hơn cho nút */
        }

        /* Điều chỉnh nút Đăng nhập cụ thể */
        .cta-section .btn-info {
            /* Sử dụng màu từ var(--info-color) trong common.css */
            background-color: var(--info-color); /* #0dcaf0 */
            border-color: var(--info-color);
            color: var(--dark-color); /* Đảm bảo màu chữ đen để nổi bật trên nền xanh */
        }

        .cta-section .btn-info:hover {
            background-color: #0abde3; /* Màu đậm hơn khi hover */
            border-color: #0abde3;
            transform: translateY(-2px); /* Hiệu ứng nhấc nhẹ */
            box-shadow: 0 5px 15px rgba(13, 202, 240, 0.3); /* Thêm bóng nhẹ */
        }

        .cta-section .btn-warning {
            /* Sử dụng màu từ var(--warning-color) trong common.css */
            background-color: var(--warning-color); /* #ffc107 */
            border-color: var(--warning-color);
            color: var(--dark-color);
        }

        .cta-section .btn-warning:hover {
            background-color: #e0a800; /* Màu đậm hơn khi hover */
            border-color: #e0a800;
            transform: translateY(-2px); /* Hiệu ứng nhấc nhẹ */
            box-shadow: 0 5px 15px rgba(255, 193, 7, 0.3); /* Thêm bóng nhẹ */
        }


        /* Responsive adjustments */
        @media (max-width: 768px) {
            .hero-section h1 {
                font-size: 2.8rem;
            }
            .hero-section p {
                font-size: 1.1rem;
                max-width: 90%;
            }
            .cta-section h2 {
                font-size: 2.2rem;
                margin-bottom: 20px;
            }
            .cta-section p.lead {
                font-size: 1rem;
                max-width: 90%;
                margin-bottom: 30px;
            }
            .cta-section .btn {
                padding: 12px 25px;
                font-size: 1rem;
                margin: 10px 0; /* Nút xếp chồng lên nhau trên mobile */
                width: 80%; /* Nút chiếm gần hết chiều rộng trên mobile */
                display: block; /* Đảm bảo mỗi nút là một khối riêng */
            }
            .d-flex.justify-content-center {
                flex-direction: column; /* Xếp chồng các nút trên mobile */
                align-items: center; /* Căn giữa các nút */
            }
        }

            @media (max-width: 768px) {
                .hero-section h1 {
                    font-size: 2.5rem;
                }
                .hero-section p {
                    font-size: 1rem;
                }
                .section-heading {
                    font-size: 2rem;
                    margin-bottom: 30px;
                }
                .section-padding {
                    padding: 50px 0;
                }
            }
        </style>
    </head>
    <body>
        <%-- Navigation Bar --%>
        <jsp:include page="components/navigation/navigation.jsp"/>

        <%-- Hero Section --%>
        <section class="hero-section">
            <div class="container" data-aos="fade-up">
                <h1 class="display-3">Tối Ưu Hóa Hành Trình Học Tập Của Bạn Với EduPlan</h1>
                <p class="lead">EduPlan là nền tảng toàn diện giúp bạn quản lý kỳ học, môn học, lịch trình và tài liệu một cách thông minh,
                    đồng thời nâng cao hiệu quả ôn luyện với AI.</p>
            </div>
        </section>

        <%-- About Section --%>
        <section id="about" class="section-padding bg-white">
            <div class="container">
                <h2 class="section-heading" data-aos="fade-down">Về EduPlan</h2>
                <div class="row align-items-center">
                    <div class="col-md-6" data-aos="fade-right">
                        <img src="${pageContext.request.contextPath}/public/images/about-eduplan.png" class="img-fluid rounded shadow-sm" alt="Về EduPlan">
                    </div>
                    <div class="col-md-6 mt-4 mt-md-0" data-aos="fade-left">
                        <p class="lead">EduPlan là một dự án ứng dụng web được xây dựng nhằm hỗ trợ sinh viên và người học
                            trong việc quản lý các hoạt động học tập một cách hiệu quả và thông minh.</p>
                        <p>Với EduPlan, bạn có thể dễ dàng quản lý các kỳ học, môn học, và lập lịch trình học tập hàng ngày. Đặc biệt,
                            chúng tôi tích hợp công nghệ AI để giúp bạn tạo bài kiểm tra nhanh chóng từ tài liệu và luyện tập tự do.</p>
                        <p>Mục tiêu của chúng tôi là tạo ra một môi trường học tập và làm việc trực tuyến tiện lợi, an toàn,
                            giúp bạn tối ưu hóa thời gian, nâng cao năng suất và đạt được kết quả tốt nhất.</p>
                    </div>
                </div>
            </div>
        </section>

        <%-- Features Section --%>
        <section id="features" class="section-padding">
            <div class="container">
                <h2 class="section-heading" data-aos="fade-down">Các Tính Năng Nổi Bật</h2>
                <div class="row g-4">
                    <div class="col-lg-3 col-md-6" data-aos="fade-up" data-aos-delay="100">
                        <div class="feature-item">
                            <i class="fas fa-calendar-alt"></i>
                            <h3>Quản Lý Kỳ Học</h3>
                            <p>Tạo và theo dõi các kỳ học của bạn một cách có tổ chức, giúp bạn dễ dàng nắm bắt tiến độ.</p>
                        </div>
                    </div>
                    <div class="col-lg-3 col-md-6" data-aos="fade-up" data-aos-delay="200">
                        <div class="feature-item">
                            <i class="fas fa-book-open"></i>
                            <h3>Tổ Chức Môn Học</h3>
                            <p>Tạo và quản lý danh sách môn học chi tiết cho từng kỳ, sắp xếp mọi thứ ngăn nắp.</p>
                        </div>
                    </div>
                    <div class="col-lg-3 col-md-6" data-aos="fade-up" data-aos-delay="300">
                        <div class="feature-item">
                            <i class="fas fa-clock"></i>
                            <h3>Lập Lịch Học Cá Nhân</h3>
                            <p>Tạo lịch học hàng ngày cho từng môn học, quản lý thời gian chuẩn chỉ và hiệu quả.</p>
                        </div>
                    </div>
                    <div class="col-lg-3 col-md-6" data-aos="fade-up" data-aos-delay="400">
                        <div class="feature-item">
                            <i class="fas fa-question-circle"></i>
                            <h3>Tạo Quiz Tùy Chỉnh</h3>
                            <p>Dễ dàng tạo các bài quiz riêng cho từng môn học để ôn tập và kiểm tra kiến thức.</p>
                        </div>
                    </div>
                </div>
                <div class="row g-4 mt-4"> <div class="col-lg-3 col-md-6" data-aos="fade-up" data-aos-delay="500">
                        <div class="feature-item">
                            <i class="fas fa-file-upload"></i>
                            <h3>Đăng Tải Tài Liệu</h3>
                            <p>Upload và quản lý tài liệu học tập của bạn cho mỗi môn học, truy cập mọi lúc mọi nơi.</p>
                        </div>
                    </div>
                    <div class="col-lg-3 col-md-6" data-aos="fade-up" data-aos-delay="600">
                        <div class="feature-item">
                            <i class="fas fa-robot"></i>
                            <h3>Quiz AI Thông Minh</h3>
                            <p>Tạo quiz tự động từ tài liệu đã đăng tải với sức mạnh của AI, giúp ôn luyện hiệu quả hơn.</p>
                        </div>
                    </div>
                    <div class="col-lg-3 col-md-6" data-aos="fade-up" data-aos-delay="700">
                        <div class="feature-item">
                            <i class="fas fa-running"></i>
                            <h3>Luyện Tập Tự Do</h3>
                            <p>Thực hành kiến thức hàng ngày với các bài tập tự do, nâng cao kỹ năng không giới hạn.</p>
                        </div>
                    </div>
                    <div class="col-lg-3 col-md-6" data-aos="fade-up" data-aos-delay="800">
                        <div class="feature-item">
                            <i class="fas fa-dollar-sign"></i>
                            <h3>Hoàn Toàn Miễn Phí</h3>
                            <p>Tất cả các tính năng mạnh mẽ đều được cung cấp miễn phí, hỗ trợ tối đa cho hành trình học tập của bạn.</p>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        
        <%-- Team Section --%>
        <section id="team" class="section-padding bg-light">
            <div class="container">
                <h2 class="section-heading" data-aos="fade-down">Đội Ngũ Phát Triển</h2>
                <div class="row justify-content-center">
                    <%-- Member 1 --%>
                    <div class="col-lg-4 col-md-6 mb-4" data-aos="fade-up" data-aos-delay="100">
                        <div class="team-member">
                            <img src="${pageContext.request.contextPath}/public/images/Vu.jpg" alt="Thành viên 1">
                            <h4>Nguyễn Tuấn Vũ</h4>
                            <p class="text-muted">Fullstack Developer</p>
                            <p>Thiết kế hệ thống là niềm vui của tôi</p>
                            <div class="social-links">
                                <a href="https://www.facebook.com/ntuanvu89"><i class="fab fa-facebook"></i></a>
                                <a href="https://github.com/TuanVuNguyen89"><i class="fab fa-github"></i></a>
                            </div>
                        </div>
                    </div>
                    <%-- Member 2 --%>
                    <div class="col-lg-4 col-md-6 mb-4" data-aos="fade-up" data-aos-delay="200">
                        <div class="team-member">
                            <img src="${pageContext.request.contextPath}/public/images/An.jpg" alt="Thành viên 2">
                            <h4>Khuất Thị Dung An</h4>
                            <p class="text-muted">Fullstack Developer</p>
                            <p>Tôi yêu cái đẹp và chưng cầu sự hoàn hảo</p>
                            <div class="social-links">
                                <a href="https://www.facebook.com/dungan.khuat"><i class="fab fa-facebook"></i></a>
                                <a href="https://github.com/khuatdungan"><i class="fab fa-github"></i></a>
                            </div>
                        </div>
                    </div>
                    <%-- Member 3 --%>
                    <div class="col-lg-4 col-md-6 mb-4" data-aos="fade-up" data-aos-delay="300">
                        <div class="team-member">
                            <img src="${pageContext.request.contextPath}/public/images/Tung.jpg" alt="Thành viên 3">
                            <h4>Nguyễn Thanh Tùng</h4>
                            <p class="text-muted">Fullstack Developer</p>
                            <p>Vận hành một hệ thống trơn tru là trách nhiệm của một lập trình viên</p>
                            <div class="social-links">
                                <a href="https://www.facebook.com/ngthtung42"><i class="fab fa-facebook"></i></a>
                                <a href="https://github.com/tungcbh"><i class="fab fa-github"></i></a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <%-- Call to Action Section --%>
        <section class="cta-section section-padding">
            <div class="container" data-aos="zoom-in">
                <h2 class="display-5 fw-bold">Sẵn sàng để bắt đầu hành trình học tập của bạn?</h2>
                <p class="lead mb-4">Đăng ký ngay hôm nay để trải nghiệm tất cả các tính năng mạnh mẽ của EduPlan!</p>
                <div class="d-flex justify-content-center">
                    <c:if test="${empty sessionScope.loggedInUser}">
                        <a href="${pageContext.request.contextPath}/auth/register" class="btn btn-warning btn-lg me-3"><i class="fas fa-pencil-alt me-2"></i>Đăng ký miễn phí</a>
                        <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-info btn-lg"><i class="fas fa-sign-in-alt me-2"></i>Đăng nhập</a> <%-- Đã đổi từ btn-outline-light sang btn-info --%>
                    </c:if>
                    <c:if test="${not empty sessionScope.loggedInUser}">
                        <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-outline-light btn-lg">
                            <i class="fas fa-sign-out-alt me-2"></i>Đăng xuất
                        </a>
                    </c:if>
                </div>
            </div>
        </section>

        <%-- Footer (Bạn có thể tạo file footer.jsp riêng và include vào) --%>
        <footer class="bg-dark text-white py-4 mt-auto">
            <div class="container text-center">
                <p class="mb-0">&copy; 2025 EduPlan. All rights reserved.</p>
            </div>
        </footer>

        <%-- Bootstrap Bundle with Popper --%>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
        <script src="https://unpkg.com/aos@2.3.1/dist/aos.js"></script>
        <script>
            // Khởi tạo AOS
            AOS.init({
                duration: 1000, // global duration for animations
                once: true, // whether animation should happen only once - default
            });

            // Smooth scrolling cho các anchor links (tùy chọn)
            document.querySelectorAll('a[href^="#"]').forEach(anchor => {
                anchor.addEventListener('click', function (e) {
                    e.preventDefault();

                    document.querySelector(this.getAttribute('href')).scrollIntoView({
                        behavior: 'smooth'
                    });
                });
            });

            // Có thể thêm JavaScript cho các hiệu ứng khác ở đây (nếu muốn)
        </script>
    </body>
</html>