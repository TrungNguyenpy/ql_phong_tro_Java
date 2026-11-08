package com.example.manager_room.data.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.manager_room.data.dao.ContractDao;
import com.example.manager_room.data.dao.InvoiceDao;
import com.example.manager_room.data.dao.MeterReadingDao;
import com.example.manager_room.data.dao.RoomDao;
import com.example.manager_room.data.dao.TenantDao;
import com.example.manager_room.data.dao.UserDao;
import com.example.manager_room.data.entity.Contract;
import com.example.manager_room.data.entity.Invoice;
import com.example.manager_room.data.entity.MeterReading;
import com.example.manager_room.data.entity.RentalRoom;
import com.example.manager_room.data.entity.Tenant;
import com.example.manager_room.data.entity.User;
import com.example.manager_room.util.PasswordHasher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {User.class, RentalRoom.class, Tenant.class, Contract.class, MeterReading.class, Invoice.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract RoomDao roomDao();
    public abstract TenantDao tenantDao();
    public abstract ContractDao contractDao();
    public abstract MeterReadingDao meterReadingDao();
    public abstract InvoiceDao invoiceDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // Đổi tên phòng trùng số (giữ phòng có id nhỏ nhất) trước khi tạo ràng buộc duy nhất
            db.execSQL("UPDATE rooms SET roomNumber = roomNumber || '-' || id "
                    + "WHERE id NOT IN (SELECT MIN(id) FROM rooms GROUP BY roomNumber)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_rooms_roomNumber` ON `rooms` (`roomNumber`)");
        }
    };

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "quanlyphongtro_db"
                            )
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration() // Tự động xóa CSDL cũ nếu thay đổi cấu trúc/version
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Callback khởi tạo CSDL lần đầu tiên
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                UserDao userDao = INSTANCE.userDao();

                // Tạo duy nhất 1 tài khoản Admin mặc định để hệ thống có thể đăng nhập
                String saltAdmin = PasswordHasher.generateSalt();
                User admin = new User(
                        "admin",
                        PasswordHasher.hashPassword("admin123", saltAdmin),
                        saltAdmin,
                        "Quản Trị Viên",
                        "0987654321",
                        "ADMIN"
                );
                userDao.insert(admin);
            });
        }
    };
}