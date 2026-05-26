package com.mygame.party;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * FILE 11: FILE KÍCH HOẠT CHẠY GAME (DESKTOP LAUNCHER)
 * File cuối cùng đóng vai trò như chìa khóa để mở cửa sổ trò chơi trên máy tính.
 */
public class DesktopLauncher {
    public static void main(String[] arg) {
        // 1. Khởi tạo cấu hình hệ thống phần cứng cho bên thứ 3
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        // 2. Ép tần số quét màn hình khóa ở mức 60 FPS để tiết kiệm pin và chạy mượt mà
        config.setForegroundFPS(60);
        
        // 3. Cài đặt tiêu đề thanh cửa sổ
        config.setTitle("SIÊU ĐẠI HỘI PARTY GAME - 15 MINI GAMES KHỦNG");
        
        // 4. Ép kích thước cửa sổ hiển thị giả lập đúng tỷ lệ 16:9 của điện thoại (1280x720 HD)
        config.setWindowedMode(1280, 720);

        // 5. Kích nổ vòng lặp: Chuyền cấu hình và chạy File 10 (MainGame)
        new Lwjgl3Application(new MainGame(), config);
    }
}
