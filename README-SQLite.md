# Library Management System — SQLite Edition

این نسخه از پروژه، علاوه بر نسخه فایل‌محور اصلی (`FileHandler.java`)، از یک
پایگاه داده **SQLite** برای ذخیرهٔ کتاب‌ها، دانشجویان و رکوردهای امانت استفاده
می‌کند.

## فایل‌های اضافه‌شده

| فایل | نقش |
|------|-----|
| `DatabaseHandler.java` | لایهٔ دسترسی به داده با JDBC، با همان API عمومیِ `FileHandler`. |
| `libs/sqlite-jdbc.jar` | درایور JDBC SQLite (نسخهٔ 3.46.1.3). |
| `schema.sql` | اسکیمای دیتابیس برای مرجع. |
| `compile.sh` | کامپایل همهٔ فایل‌های `.java` با classpath درست. |
| `run.sh`    | اجرای برنامه با درایور SQLite روی classpath. |
| `README-SQLite.md` | همین فایل. |

`Menu.java` به‌روزرسانی شده تا به‌جای `FileHandler` از `DatabaseHandler` استفاده
کند. بقیهٔ کلاس‌ها (`Library`، `Manager`، `Operator`، …) بدون تغییر باقی مانده‌اند
چون `DatabaseHandler` دقیقاً همان متدهای عمومیِ `FileHandler` را پیاده‌سازی می‌کند.
برای برگشت به نسخهٔ فایلی، کافی است در `Menu.java` این خط را برگردانید:

```java
library = new Library(new FileHandler("data"));
```

## نحوهٔ اجرا

```bash
cd LibraryManagementSystem
chmod +x compile.sh run.sh
./run.sh
```

دفعات اول، فایل دیتابیس در مسیر زیر ساخته می‌شود:

```
data/library.db
```

می‌توانید محتوای آن را با هر کلاینت SQLite (مثلاً `sqlite3 data/library.db`)
ببینید.

## نحوهٔ کامپایل دستی

```bash
mkdir -p out
javac -encoding UTF-8 -cp libs/sqlite-jdbc.jar -d out *.java
mkdir -p data
java -cp out:libs/sqlite-jdbc.jar Menu
```

روی ویندوز، جداکنندهٔ classpath به‌جای `:` از `;` استفاده کنید:

```cmd
java -cp "out;libs\sqlite-jdbc.jar" Menu
```

## اسکیمای دیتابیس

```sql
books    (id, title, author, year)
students (id, name, student_number UNIQUE)
borrows  (student_id, book_id, borrow_date, returned,
          PRIMARY KEY (student_id, book_id, borrow_date))
```

نکته: ستون `available` در جدول books نیست؛ مانند نسخهٔ فایلی، در زمان اجرا از
جدول borrows استخراج می‌شود (کتابی که رکورد امانت «برنگشته» داشته باشد،
غیرقابل دسترس است).

## وابستگی‌ها

- Java 21 (یا هر نسخهٔ 11+ با ماژول java.sql)
- درایور `sqlite-jdbc` (همراه در پوشهٔ `libs/`)
