package com.example.manager_room.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.Toast;

import com.example.manager_room.data.entity.Invoice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Locale;

public class PdfExporter {

    public static void exportInvoiceToPdf(Context context, Invoice invoice, String roomName, String tenantName) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // Kích thước khổ A4
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Tiêu đề
        paint.setTextSize(22);
        paint.setFakeBoldText(true);
        paint.setColor(Color.BLACK);
        canvas.drawText("HÓA ĐƠN TIỀN PHÒNG TRỌ", 140, 60, paint);

        // Thông tin chung
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        int startY = 110;
        canvas.drawText("Tháng / Năm: " + invoice.getMonth() + "/" + invoice.getYear(), 50, startY, paint);
        canvas.drawText("Phòng: " + roomName, 50, startY + 30, paint);
        canvas.drawText("Khách thuê: " + tenantName, 50, startY + 60, paint);
        canvas.drawText("Ngày lập: " + invoice.getCreatedDate(), 50, startY + 90, paint);

        // Chi tiết dịch vụ
        int tableY = startY + 140;
        paint.setFakeBoldText(true);
        canvas.drawText("Chi tiết khoản chi", 50, tableY, paint);
        canvas.drawText("Số tiền", 400, tableY, paint);

        paint.setFakeBoldText(false);
        canvas.drawLine(50, tableY + 10, 545, tableY + 10, paint);

        canvas.drawText("1. Tiền phòng", 50, tableY + 40, paint);
        canvas.drawText(formatter.format(invoice.getRoomAmount()), 400, tableY + 40, paint);

        canvas.drawText("2. Tiền điện", 50, tableY + 70, paint);
        canvas.drawText(formatter.format(invoice.getElectricityAmount()), 400, tableY + 70, paint);

        canvas.drawText("3. Tiền nước", 50, tableY + 100, paint);
        canvas.drawText(formatter.format(invoice.getWaterAmount()), 400, tableY + 100, paint);

        canvas.drawText("4. Tiền dịch vụ khác", 50, tableY + 130, paint);
        canvas.drawText(formatter.format(invoice.getServiceAmount()), 400, tableY + 130, paint);

        canvas.drawLine(50, tableY + 150, 545, tableY + 150, paint);

        // Tổng tiền
        paint.setFakeBoldText(true);
        paint.setTextSize(16);
        canvas.drawText("TỔNG CỘNG:", 50, tableY + 180, paint);
        canvas.drawText(formatter.format(invoice.getTotalAmount()), 400, tableY + 180, paint);

        canvas.drawText("Trạng thái:", 50, tableY + 210, paint);
        canvas.drawText(invoice.isPaid() ? "ĐÃ THANH TOÁN" : "CHƯA THANH TOÁN", 400, tableY + 210, paint);

        document.finishPage(page);

        // Lưu file tương thích Android 10+
        String fileName = "HoaDon_Thang_" + invoice.getMonth() + "_" + invoice.getYear() + ".pdf";
        savePdfToStorage(context, document, fileName);
    }

    public static void exportContractToPdf(Context context, CharSequence content, String fileName) {
        final int pageW = 595, pageH = 842, margin = 50;
        final int textW = pageW - 2 * margin;
        final int usableH = pageH - 2 * margin;

        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(11f);
        paint.setTypeface(Typeface.SERIF);

        StaticLayout layout = StaticLayout.Builder
                .obtain(content, 0, content.length(), paint, textW)
                .setLineSpacing(0f, 1.2f)
                .build();

        PdfDocument document = new PdfDocument();
        int top = 0, pageNumber = 1;
        while (top < layout.getHeight()) {
            int limit = top + usableH;
            int line = layout.getLineForVertical(limit);
            int bottom = layout.getLineBottom(line) <= limit ? layout.getLineBottom(line) : layout.getLineTop(line);
            if (bottom <= top) bottom = limit;

            PdfDocument.Page page = document.startPage(
                    new PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber++).create());
            Canvas canvas = page.getCanvas();
            canvas.save();
            canvas.translate(margin, margin - top);
            canvas.clipRect(0, top, textW, bottom);
            layout.draw(canvas);
            canvas.restore();
            document.finishPage(page);
            top = bottom;
        }

        savePdfToStorage(context, document, fileName);
    }

    private static void savePdfToStorage(Context context, PdfDocument document, String fileName) {
        try {
            OutputStream out;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new IOException("Không thể tạo file qua MediaStore");
                out = context.getContentResolver().openOutputStream(uri);
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!dir.exists()) dir.mkdirs();
                out = new FileOutputStream(new File(dir, fileName));
            }

            if (out != null) {
                document.writeTo(out);
                out.close();
                Toast.makeText(context, "Đã lưu vào thư mục Tải xuống: " + fileName, Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi khi xuất PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }
}