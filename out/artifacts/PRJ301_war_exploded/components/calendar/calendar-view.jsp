<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.YearMonth" %>
<%@ page import="java.time.DayOfWeek" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="entity.Lesson" %>
<%@ page import="entity.Semester" %>
<%@ page import="java.time.temporal.ChronoUnit" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch Học - EduPlan</title>
    <link href="${pageContext.request.contextPath}/css/common.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        /* Tùy chỉnh CSS cho lịch */
        .calendar-month {
            border: 1px solid #ddd;
            border-radius: 8px;
            margin-bottom: 30px;
            overflow: hidden;
            background-color: #fff;
            box-shadow: 0 4px 10px rgba(0,0,0,0.08);
        }
        .calendar-header {
            background-color: #f8f9fa;
            padding: 15px;
            border-bottom: 1px solid #eee;
            font-size: 1.25rem;
            font-weight: bold;
            color: #343a40;
            text-align: center;
        }
        .calendar-weekdays {
            display: grid;
            grid-template-columns: repeat(7, 1fr);
            background-color: #e9ecef;
            font-weight: bold;
            text-align: center;
            padding: 10px 0;
            border-bottom: 1px solid #ddd;
        }
        .calendar-day-grid {
            display: grid;
            grid-template-columns: repeat(7, 1fr);
            border-top: 1px solid #ddd; /* Để đường kẻ ngang hiện lên */
        }
        .calendar-day {
            min-height: 120px; /* Chiều cao tối thiểu cho mỗi ô ngày */
            border: 1px solid #eee;
            padding: 8px;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            text-align: left;
            position: relative;
            background-color: #fff;
        }
        .calendar-day.other-month {
            background-color: #f8f9fa;
            color: #ccc;
        }
        .calendar-day .day-number {
            font-size: 1.1em;
            font-weight: bold;
            color: #495057;
            margin-bottom: 5px;
        }
        .calendar-day.today .day-number {
            color: var(--primary-color);
        }
        .calendar-day.today {
            background-color: #e0f7fa; /* Màu nổi bật cho ngày hiện tại */
            border: 2px solid var(--info-color);
        }
        .calendar-lessons {
            font-size: 0.85em;
            width: 100%; /* Đảm bảo danh sách lesson nằm gọn */
            margin-top: 5px; /* Thêm khoảng cách với số ngày */
        }
        .calendar-lessons .lesson-item {
            margin-bottom: 5px; /* Tăng khoảng cách giữa các khối buổi học */
            padding: 6px 8px; /* Tăng padding để khối to hơn */
            border-radius: 6px; /* Bo tròn góc */
            /* background-color: #e0e0e0; Màu nền mặc định sẽ bị ghi đè bởi các trạng thái cụ thể */
            color: var(--dark-color);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            font-size: 0.85em;
            font-weight: 500;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1); /* Thêm bóng nhẹ */
            display: flex; /* Dùng flexbox để căn chỉnh icon và text */
            align-items: center; /* Căn giữa theo chiều dọc */
            transition: background-color 0.2s ease, box-shadow 0.2s ease;
        }
        .calendar-lessons .lesson-item:hover {
            /* Màu hover có thể tinh chỉnh để phù hợp với màu nền của từng trạng thái */
            box-shadow: 0 2px 5px rgba(0,0,0,0.2);
        }
        .calendar-lessons .lesson-item .lesson-icon {
            margin-right: 5px; /* Khoảng cách giữa icon và text */
            /* color: #6c757d; Màu icon mặc định sẽ bị ghi đè bởi các trạng thái cụ thể */
        }

        /* Styles for different lesson statuses - ĐÃ CẬP NHẬT THEO YÊU CẦU MỚI */
        .calendar-lessons .lesson-item.completed {
            background-color: #d4edda; /* Light green for completed */
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .calendar-lessons .lesson-item.completed .lesson-icon {
            color: #198754; /* Success color for icon */
        }

        .calendar-lessons .lesson-item.inactive {
            background-color: #f8d7da; /* Light red for inactive (vắng) */
            color: #842029;
            border: 1px solid #f5c2c7;
        }
        .calendar-lessons .lesson-item.inactive .lesson-icon {
            color: #dc3545; /* Danger color for icon */
        }

        .calendar-lessons .lesson-item.active {
            background-color: #e2e3e5; /* Light gray for active/upcoming */
            color: #495057;
            border: 1px solid #d6d8db;
        }
        .calendar-lessons .lesson-item.active .lesson-icon {
            color: #6c757d; /* Secondary color for icon */
        }


        /* Thêm màu cho các loại buổi học cụ thể nếu cần (ví dụ: meeting, exam) */
        .calendar-lessons .lesson-item.meeting {
            background-color: #cfe2ff; /* Light blue for meetings */
            color: #055160;
            border: 1px solid #b6effb;
        }
        .calendar-lessons .lesson-item.meeting .lesson-icon {
            color: #0dcaf0;
        }

        .calendar-lessons .lesson-item.exam {
            background-color: #f8d7da; /* Light red for exams (đã có từ trước, giữ nguyên) */
            color: #842029;
            border: 1px solid #f5c2c7;
        }
        .calendar-lessons .lesson-item.exam .lesson-icon {
            color: #dc3545;
        }

        .calendar-lessons .lesson-item a {
            color: inherit;
            text-decoration: none;
            display: flex; /* Để link bao trọn nội dung và icon */
            align-items: center;
            flex-grow: 1; /* Cho phép link chiếm hết không gian còn lại */
        }
        .calendar-lessons .lesson-item a:hover {
            text-decoration: none; /* Bỏ gạch chân khi hover để trông gọn hơn */
        }
        .calendar-day:nth-child(7n), .calendar-weekdays > div:nth-child(7n) {
            color: var(--danger-color); /* Chủ nhật màu đỏ */
        }
        .calendar-day:nth-child(6n), .calendar-weekdays > div:nth-child(6n) {
            color: var(--info-color); /* Thứ 7 màu xanh */
        }
    </style>  
</head>
<body>
    <jsp:include page="../navigation/navigation.jsp" />

    <div class="container py-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2><i class="fas fa-calendar-alt me-2"></i>Lịch Học</h2>
            <div class="col-md-4">
                <select class="form-select" id="semesterSelect" onchange="location = this.value;">
                    <c:forEach var="sem" items="${semesters}">
                        <option value="${pageContext.request.contextPath}/calendar?semesterId=${sem.id}"
                                    ${sem.id == currentSemester.id ? 'selected' : ''}>
                            <c:out value="${sem.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <c:if test="${empty currentSemester}">
            <div class="alert alert-warning text-center" role="alert">
                <i class="fas fa-info-circle me-2"></i>Vui lòng chọn một kỳ học để xem lịch.
            </div>
        </c:if>

        <c:if test="${not empty currentSemester}">
            <h3 class="mb-4 text-center">Kỳ học: <c:out value="${currentSemester.name}"/> 
                (<fmt:formatDate value="${currentSemester.startDate}" pattern="dd/MM/yyyy"/> - 
                <fmt:formatDate value="${currentSemester.endDate}" pattern="dd/MM/yyyy"/>)
            </h3>

            <%
                Semester semester = (Semester) request.getAttribute("currentSemester");
                Map<LocalDate, List<Lesson>> lessonsByDate = (Map<LocalDate, List<Lesson>>) request.getAttribute("lessonsByDate");

                if (semester != null) {
                    LocalDate semesterStartDate = semester.getStartDate().toLocalDate();
                    LocalDate semesterEndDate = semester.getEndDate().toLocalDate();
                    LocalDate today = LocalDate.now();

                    YearMonth startMonth = YearMonth.from(semesterStartDate);
                    YearMonth endMonth = YearMonth.from(semesterEndDate);
                    
                    for (LocalDate currentMonthDate = semesterStartDate.withDayOfMonth(1); 
                         !currentMonthDate.isAfter(semesterEndDate.withDayOfMonth(semesterEndDate.lengthOfMonth())); 
                         currentMonthDate = currentMonthDate.plusMonths(1)) {
                        
                        YearMonth currentMonth = YearMonth.from(currentMonthDate);
            %>
                        <div class="calendar-month">
                            <div class="calendar-header">
                                <%= currentMonth.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("vi")) %> <%= currentMonth.getYear() %>
                            </div>
                            <div class="calendar-weekdays">
                                <div>T2</div>
                                <div>T3</div>
                                <div>T4</div>
                                <div>T5</div>
                                <div>T6</div>
                                <div>T7</div>
                                <div>CN</div>
                            </div>
                            <div class="calendar-day-grid">
                            <%
                                LocalDate firstDayOfMonth = currentMonth.atDay(1);
                                int daysToPadStart = firstDayOfMonth.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
                                if (daysToPadStart < 0) { 
                                    daysToPadStart = 6; 
                                }
                                
                                for (int i = 0; i < daysToPadStart; i++) {
                            %>
                                <div class="calendar-day other-month"></div>
                            <%
                                }

                                for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
                                    LocalDate currentDate = currentMonth.atDay(day);
                                    String todayClass = currentDate.equals(today) ? " today" : "";
                                    String otherMonthClass = (currentDate.isBefore(semesterStartDate) || currentDate.isAfter(semesterEndDate)) ? " other-month" : "";
                            %>
                                    <div class="calendar-day<%= todayClass %><%= otherMonthClass %>">
                                        <div class="day-number"><%= day %></div>
                                        <div class="calendar-lessons">
                                        <%
                                            if (lessonsByDate != null && lessonsByDate.containsKey(currentDate)) {
                                                List<Lesson> dailyLessons = lessonsByDate.get(currentDate);
                                                for (Lesson lesson : dailyLessons) {
                                                    String lessonStatusClass = "";
                                                    String iconClass = "fas fa-calendar-alt"; // Icon mặc định cho trạng thái Active
                                                    
                                                    if ("Completed".equals(lesson.getStatus())) {
                                                        lessonStatusClass = "completed";
                                                        iconClass = "fas fa-check-circle"; 
                                                    } else if ("Inactive".equals(lesson.getStatus())) {
                                                        lessonStatusClass = "inactive";
                                                        iconClass = "fas fa-times-circle"; 
                                                    } else { // Mặc định là Active (Chưa học)
                                                        lessonStatusClass = "active";
                                                        iconClass = "fas fa-circle-info"; // Hoặc icon phù hợp khác cho trạng thái Active
                                                    }
                                        %>
                                                    <div class="lesson-item <%= lessonStatusClass %>">
                                                        <a href="${pageContext.request.contextPath}/lessons/detail?id=<%= lesson.getId() %>" title="<%= lesson.getName() %>">
                                                            <i class="<%= iconClass %> lesson-icon"></i> 
                                                            <%= lesson.getName() %>
                                                        </a>
                                                    </div>
                                        <%
                                                }
                                            }
                                        %>
                                        </div>
                                    </div>
                            <%
                                }
                                LocalDate lastDayOfMonth = currentMonth.atEndOfMonth();
                                int daysToPadEnd = DayOfWeek.SUNDAY.getValue() - lastDayOfMonth.getDayOfWeek().getValue();
                                if (daysToPadEnd < 0) { 
                                    daysToPadEnd = 0; 
                                }
                                for (int i = 0; i < daysToPadEnd; i++) {
                            %>
                                <div class="calendar-day other-month"></div>
                            <%
                                }
                            %>
                            </div>
                        </div>
            <%
                    }
                }
            %>
        </c:if>
    </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
</body>
</html>