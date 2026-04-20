ĐẶT FILE JAR CỦA MYSQL CONNECTOR VÀO THƯ MỤC NÀY
=================================================

1. Tải driver:
     https://dev.mysql.com/downloads/connector/j/
   Chọn "Platform Independent (.zip)" -> giải nén được file
       mysql-connector-j-8.x.x.jar

2. Đổi tên hoặc không đều được, copy/paste vào thư mục này
   với tên CHÍNH XÁC: mysql-connector-j.jar
   (hoặc đổi file.reference.mysql-connector-j.jar trong
    nbproject/project.properties cho khớp tên thật)

3. Trong NetBeans:
     Chuột phải ServerApp -> Properties -> Libraries
     -> Add JAR/Folder -> chọn file vừa copy.

XAMPP đã cài sẵn MySQL. Chỉ cần đảm bảo:
  - Mở XAMPP Control Panel -> Start Apache và MySQL.
  - Mở http://localhost/phpmyadmin -> chạy file db/schema.sql
    (đã nằm ở thư mục db/ gốc repo).
