package com.example.manager_room.util;

import android.text.Spanned;
import android.text.TextUtils;

import androidx.core.text.HtmlCompat;

import com.example.manager_room.data.entity.Contract;
import com.example.manager_room.data.entity.RentalRoom;
import com.example.manager_room.data.entity.Tenant;
import com.example.manager_room.data.entity.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ContractTextBuilder {
    private static final String DOTS = "………………";
    private static final String[] DIGITS = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};

    public static class Data {
        public Contract contract;
        public RentalRoom room;
        public User tenantUser;
        public Tenant tenant;
        public LandlordInfo landlord;
    }

    public static Spanned build(Data d) {
        Contract c = d.contract;
        LandlordInfo l = d.landlord;
        String ownerName = v(l.get(LandlordInfo.NAME));
        String tenantName = v(d.tenantUser != null ? d.tenantUser.getFullName() : null);
        String place = v(l.get(LandlordInfo.PROPERTY));
        String rent = money(c.getRentalPrice());
        String deposit = money(c.getDeposit());

        StringBuilder h = new StringBuilder();
        h.append("<div align='center'><b>CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</b><br>")
                .append("<b>Độc lập – Tự do – Hạnh phúc</b><br>—————<br><br>")
                .append("<b>HỢP ĐỒNG THUÊ NHÀ Ở (PHÒNG TRỌ)</b><br>")
                .append("Số: HĐ-").append(c.getId()).append("/").append(year(c.getStartDate()))
                .append("</div><br>");
        h.append("Căn cứ Bộ luật Dân sự năm 2015;<br>Căn cứ Luật Nhà ở năm 2023;<br>")
                .append("Căn cứ nhu cầu và khả năng của các bên.<br><br>");
        h.append("Hôm nay, ").append(dateText(c.getStartDate())).append(", tại ").append(place)
                .append(", chúng tôi gồm:<br><br>");

        h.append("<b>BÊN CHO THUÊ (sau đây gọi là Bên A):</b><br>")
                .append("Ông/Bà: <b>").append(ownerName).append("</b><br>")
                .append("CCCD số: ").append(v(l.get(LandlordInfo.CCCD))).append("<br>")
                .append("Địa chỉ thường trú: ").append(v(l.get(LandlordInfo.ADDRESS))).append("<br>")
                .append("Điện thoại: ").append(v(l.get(LandlordInfo.PHONE))).append("<br><br>");

        h.append("<b>BÊN THUÊ (sau đây gọi là Bên B):</b><br>")
                .append("Ông/Bà: <b>").append(tenantName).append("</b><br>")
                .append("CCCD số: ").append(v(d.tenant != null ? d.tenant.getIdentityCard() : null)).append("<br>")
                .append("Địa chỉ thường trú: ").append(v(d.tenant != null ? d.tenant.getAddress() : null)).append("<br>")
                .append("Điện thoại: ").append(v(d.tenantUser != null ? d.tenantUser.getPhone() : null)).append("<br><br>");

        h.append("Hai bên thống nhất ký kết hợp đồng thuê nhà ở với các điều khoản sau:<br><br>");

        h.append("<b>Điều 1. Nhà ở cho thuê</b><br>")
                .append("1.1. Bên A đồng ý cho Bên B thuê phòng số <b>").append(v(d.room.getRoomNumber()))
                .append("</b> thuộc nhà trọ tại địa chỉ: ").append(place).append(".<br>")
                .append("1.2. Diện tích sử dụng: ").append(area(d.room.getArea())).append(" m².<br>")
                .append("1.3. Giấy tờ pháp lý của nhà ở và thửa đất gắn với nhà (Giấy chứng nhận số): ").append(DOTS).append(".<br>")
                .append("1.4. Mục đích thuê: để ở. Tình trạng phòng và trang thiết bị được hai bên kiểm tra và bàn giao theo hiện trạng.<br><br>");

        h.append("<b>Điều 2. Thời hạn thuê và giao nhận nhà</b><br>")
                .append("2.1. Thời hạn thuê: ").append(months(c.getStartDate(), c.getEndDate()))
                .append(" tháng, từ ngày ").append(date(c.getStartDate()))
                .append(" đến ngày ").append(date(c.getEndDate())).append(".<br>")
                .append("2.2. Ngày giao nhận phòng: ").append(date(c.getStartDate())).append(".<br>")
                .append("2.3. Hết thời hạn, nếu Bên B có nhu cầu thuê tiếp, hai bên thỏa thuận gia hạn bằng văn bản hoặc phụ lục hợp đồng; ")
                .append("Bên B thông báo cho Bên A trước ít nhất 30 ngày.<br><br>");

        h.append("<b>Điều 3. Giá thuê và phương thức thanh toán</b><br>")
                .append("3.1. Giá thuê: <b>").append(rent).append(" đồng/tháng</b> (Bằng chữ: ").append(words(c.getRentalPrice())).append(").<br>")
                .append("3.2. Giá thuê chưa bao gồm tiền điện, nước và các chi phí dịch vụ khác.<br>")
                .append("3.3. Tiền điện: ").append(rate(l.get(LandlordInfo.ELEC))).append(" đồng/kWh; tiền nước: ")
                .append(rate(l.get(LandlordInfo.WATER))).append(" đồng/m³, tính theo chỉ số công tơ hằng tháng.<br>")
                .append("3.4. Bên B thanh toán vào ngày ").append(v(l.get(LandlordInfo.PAY_DAY)))
                .append(" hằng tháng, bằng tiền mặt hoặc chuyển khoản.<br>")
                .append("3.5. Bên A chỉ được điều chỉnh giá thuê khi thông báo bằng văn bản trước ít nhất 30 ngày và được Bên B đồng ý.<br><br>");

        h.append("<b>Điều 4. Tiền đặt cọc</b><br>")
                .append("4.1. Bên B đặt cọc: <b>").append(deposit).append(" đồng</b> (Bằng chữ: ").append(words(c.getDeposit()))
                .append(") để bảo đảm thực hiện hợp đồng.<br>")
                .append("4.2. Khi hợp đồng chấm dứt đúng thỏa thuận, Bên A hoàn trả tiền cọc sau khi trừ các khoản Bên B còn nợ ")
                .append("(tiền thuê, điện, nước, thiệt hại tài sản nếu có).<br>")
                .append("4.3. Trường hợp một bên vi phạm khiến hợp đồng chấm dứt trước hạn trái thỏa thuận, việc xử lý tiền cọc thực hiện theo quy định của pháp luật về đặt cọc.<br><br>");

        h.append("<b>Điều 5. Quyền và nghĩa vụ của Bên A</b><br>")
                .append("5.1. Giao phòng đúng hiện trạng, bảo đảm quyền sử dụng ổn định cho Bên B trong suốt thời hạn thuê.<br>")
                .append("5.2. Bảo trì, sửa chữa phòng và hệ thống điện, nước theo quy định của pháp luật về nhà ở, trừ hư hỏng do lỗi của Bên B.<br>")
                .append("5.3. Không đơn phương chấm dứt hợp đồng, thu hồi phòng trong thời hạn thuê, trừ các trường hợp pháp luật hoặc hợp đồng cho phép.<br>")
                .append("5.4. Phối hợp để Bên B đăng ký cư trú theo quy định của pháp luật.<br>")
                .append("5.5. Được nhận tiền thuê đúng hạn; yêu cầu bồi thường thiệt hại do lỗi của Bên B; kiểm tra phòng sau khi thông báo trước cho Bên B.<br><br>");

        h.append("<b>Điều 6. Quyền và nghĩa vụ của Bên B</b><br>")
                .append("6.1. Thanh toán tiền thuê, điện, nước đúng hạn; sử dụng phòng đúng mục đích.<br>")
                .append("6.2. Giữ gìn tài sản, bồi thường hư hỏng do lỗi của mình.<br>")
                .append("6.3. Không tự ý sửa chữa, cải tạo, cho thuê lại hoặc cho người khác ở nhờ khi chưa có sự đồng ý bằng văn bản của Bên A.<br>")
                .append("6.4. Chấp hành nội quy nhà trọ, quy định về an ninh trật tự, phòng cháy chữa cháy; thực hiện đăng ký cư trú theo quy định.<br>")
                .append("6.5. Trả phòng và bàn giao đúng hiện trạng khi hết hạn hoặc chấm dứt hợp đồng; được yêu cầu Bên A bảo trì, sửa chữa phòng theo thỏa thuận.<br><br>");

        h.append("<b>Điều 7. Chấm dứt hợp đồng</b><br>")
                .append("7.1. Hợp đồng chấm dứt khi hết thời hạn, khi hai bên thỏa thuận chấm dứt, khi phòng hư hỏng nặng không thể sử dụng, ")
                .append("hoặc khi một bên đơn phương chấm dứt theo quy định tại Điều 171 và Điều 172 Luật Nhà ở năm 2023.<br>")
                .append("7.2. Bên đơn phương chấm dứt phải thông báo trước cho bên kia theo thời hạn pháp luật quy định (tối thiểu 30 ngày nếu pháp luật không quy định khác).<br>")
                .append("7.3. Khi chấm dứt, Bên B bàn giao phòng; hai bên quyết toán tiền thuê, điện, nước và tiền cọc trong vòng 07 ngày.<br><br>");

        h.append("<b>Điều 8. Cam kết chung và giải quyết tranh chấp</b><br>")
                .append("8.1. Bên A cam kết có quyền cho thuê hợp pháp, nhà ở không có tranh chấp và không bị kê biên; Bên B cam kết thông tin cá nhân khai trong hợp đồng là đúng sự thật.<br>")
                .append("8.2. Hai bên cam kết thực hiện đúng hợp đồng. Tranh chấp phát sinh được giải quyết bằng thương lượng, hòa giải; ")
                .append("nếu không thành thì đưa ra Tòa án nhân dân có thẩm quyền giải quyết theo quy định của pháp luật.<br><br>");

        h.append("<b>Điều 9. Thỏa thuận khác</b><br>")
                .append(TextUtils.isEmpty(c.getTerms()) ? "Không có." : TextUtils.htmlEncode(c.getTerms()))
                .append("<br><br>");

        h.append("<b>Điều 10. Hiệu lực hợp đồng</b><br>")
                .append("10.1. Hợp đồng có hiệu lực kể từ ngày ").append(date(c.getStartDate())).append(".<br>")
                .append("10.2. Hợp đồng lập thành 02 bản có giá trị pháp lý như nhau, mỗi bên giữ 01 bản. Mọi sửa đổi, bổ sung phải lập thành phụ lục có chữ ký của hai bên.<br>")
                .append("10.3. Các bên có thể yêu cầu công chứng hoặc chứng thực hợp đồng nếu có nhu cầu.<br><br>");

        h.append("<div align='center'>")
                .append("<b>BÊN CHO THUÊ (BÊN A)</b><br><i>(Ký, ghi rõ họ tên)</i><br><br><br><br><b>").append(ownerName).append("</b><br><br><br>")
                .append("<b>BÊN THUÊ (BÊN B)</b><br><i>(Ký, ghi rõ họ tên)</i><br><br><br><br><b>").append(tenantName).append("</b>")
                .append("</div>");

        return HtmlCompat.fromHtml(h.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    // ---------- Helpers ----------
    private static String v(String s) {
        if (s == null) return DOTS;
        String t = s.trim();
        if (t.isEmpty() || t.startsWith("Chưa cập nhật")) return DOTS;
        return TextUtils.htmlEncode(t);
    }

    private static String money(double a) {
        return a > 0 ? String.format(new Locale("vi", "VN"), "%,.0f", a) : DOTS;
    }

    private static String words(double a) {
        return a > 0 ? moneyToWords((long) a) : DOTS;
    }

    private static String rate(String s) {
        try {
            return money(Double.parseDouble(s.trim()));
        } catch (Exception e) {
            return DOTS;
        }
    }

    private static String area(double a) {
        if (a <= 0) return DOTS;
        return a == (long) a ? String.valueOf((long) a) : String.valueOf(a);
    }

    private static Calendar parse(String iso) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso));
            return cal;
        } catch (Exception e) {
            return null;
        }
    }

    private static String date(String iso) {
        Calendar c = parse(iso);
        if (c == null) return DOTS;
        return String.format(Locale.US, "%02d/%02d/%d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
    }

    private static String dateText(String iso) {
        Calendar c = parse(iso);
        if (c == null) return "ngày …… tháng …… năm ……";
        return "ngày " + c.get(Calendar.DAY_OF_MONTH) + " tháng " + (c.get(Calendar.MONTH) + 1) + " năm " + c.get(Calendar.YEAR);
    }

    private static String year(String iso) {
        Calendar c = parse(iso);
        return c == null ? "……" : String.valueOf(c.get(Calendar.YEAR));
    }

    private static String months(String start, String end) {
        Calendar a = parse(start), b = parse(end);
        if (a == null || b == null) return DOTS;
        int m = (b.get(Calendar.YEAR) - a.get(Calendar.YEAR)) * 12 + b.get(Calendar.MONTH) - a.get(Calendar.MONTH);
        if (b.get(Calendar.DAY_OF_MONTH) < a.get(Calendar.DAY_OF_MONTH)) m--;
        return m > 0 ? String.valueOf(m) : DOTS;
    }

    // ---------- Đọc số tiền thành chữ ----------
    private static String readThree(int n, boolean full) {
        int tram = n / 100, chuc = (n % 100) / 10, donvi = n % 10;
        StringBuilder sb = new StringBuilder();
        boolean hasHundred = full || tram > 0;
        if (hasHundred) sb.append(DIGITS[tram]).append(" trăm");
        if (chuc > 1) {
            sb.append(" ").append(DIGITS[chuc]).append(" mươi");
            if (donvi == 1) sb.append(" mốt");
            else if (donvi == 5) sb.append(" lăm");
            else if (donvi > 0) sb.append(" ").append(DIGITS[donvi]);
        } else if (chuc == 1) {
            sb.append(" mười");
            if (donvi == 5) sb.append(" lăm");
            else if (donvi > 0) sb.append(" ").append(DIGITS[donvi]);
        } else if (donvi > 0) {
            if (hasHundred) sb.append(" lẻ");
            sb.append(" ").append(DIGITS[donvi]);
        }
        return sb.toString().trim();
    }

    private static String moneyToWords(long amount) {
        if (amount <= 0) return "Không đồng";
        String[] units = {"", "nghìn", "triệu", "tỷ"};
        List<Integer> groups = new ArrayList<>();
        long n = amount;
        while (n > 0) {
            groups.add((int) (n % 1000));
            n /= 1000;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = groups.size() - 1; i >= 0; i--) {
            int g = groups.get(i);
            if (g == 0) continue;
            boolean full = sb.length() > 0;
            if (full) sb.append(" ");
            sb.append(readThree(g, full));
            if (i < units.length && !units[i].isEmpty()) sb.append(" ").append(units[i]);
        }
        String s = sb.toString().trim();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1) + " đồng";
    }
}