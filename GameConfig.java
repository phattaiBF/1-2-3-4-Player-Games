package com.mygame.party;

import com.badlogic.gdx.graphics.Color;

/**
 * FILE 1: Cấu hình hệ thống dữ liệu tập trung.
 * Chứa toàn bộ thông số về màn hình, chế độ chơi, danh sách game và màu sắc nền/nhân vật.
 * Đảm bảo tính ổn định và đồng bộ cho tất cả các file xử lý phía sau.
 */
public class GameConfig {
    
    // ==========================================
    // 1. CẤU HÌNH ĐỘ PHÂN GIẢI MÀN HÌNH MẪU
    // ==========================================
    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;

    // ==========================================
    // 2. ĐỊNH NGHĨA CÁC CHẾ ĐỘ NGƯỜI CHƠI (TÙY CHỌN RÕ RÀNG)
    // ==========================================
    public enum PlayerMode {
        ONE_PLAYER,     // 1 Người chơi (Chơi đơn/Đua điểm với máy)
        TWO_PLAYERS,    // 2 Người chơi (Đối kháng 1v1)
        THREE_PLAYERS,  // 3 Người chơi (Hỗn chiến 3 người)
        FOUR_PLAYERS,   // 4 Người chơi (Hỗn chiến 4 người)
        TEAM_2V2        // Đấu đội 2vs2 (P1 + P2 đấu với P3 + P4)
    }

    // ==========================================
    // 3. ĐỊNH NGHĨA DANH SÁCH MINI-GAME 
    // ==========================================
    public enum MiniGameType {
        MENU_SCREEN,    // Trạng thái đang ở Màn hình chính chọn chế độ
        CATCH_COINS,    // Game 1: Ăn xu tốc độ
        SURVIVAL,       // Game 2: Sống sót (Đẩy nhau ra ngoài vòng bo)
        CAPTURE_FLAG    // Game 3: Cướp cờ về căn cứ (Rất hay khi chơi 2V2)
    }

    // Tùy chọn hiện tại (Mặc định khi mở game lên sẽ ở màn hình MENU)
    public static PlayerMode currentMode = PlayerMode.FOUR_PLAYERS; 
    public static MiniGameType currentGame = MiniGameType.MENU_SCREEN; 

    // ==========================================
    // 4. MÀU SẮC NỀN ỔN ĐỊNH CHO TỪNG PHẦN GAME
    // ==========================================
    // Màu nền xám dịu mắt cho Màn hình chính (Menu) để nổi bật các nút bấm
    public static final Color COLOR_BG_MENU = new Color(0.15f, 0.15f, 0.18f, 1f); 
    
    // Màu nền xanh cỏ cho Game Ăn Xu
    public static final Color COLOR_BG_COINS = new Color(0.2f, 0.5f, 0.2f, 1f); 
    
    // Màu nền đỏ gạch tối cho Đấu trường Sinh Tồn
    public static final Color COLOR_BG_SURVIVAL = new Color(0.4f, 0.15f, 0.15f, 1f); 
    
    // Màu nền xanh biển tối cho Game Cướp Cờ
    public static final Color COLOR_BG_FLAG = new Color(0.15f, 0.25f, 0.4f, 1f); 

    // ==========================================
    // 5. THÔNG SỐ VÀ MÀU SẮC NHÂN VẬT
    // ==========================================
    public static final float PLAYER_SPEED = 380.0f; // Tốc độ di chuyển mượt mà
    public static final int PLAYER_SIZE = 70;       // Kích thước nhân vật vừa vặn trên mobile

    // Tùy chọn màu sắc Solo (4 màu hoàn toàn khác biệt để không bị lẫn)
    public static final Color[] SOLO_COLORS = {
        Color.RED,       // P1 - Đỏ
        Color.BLUE,      // P2 - Xanh dương
        Color.GREEN,     // P3 - Xanh lá
        Color.ORANGE     // P4 - Cam
    };

    // Tùy chọn màu sắc Đấu Đội 2V2 (Chia rõ thành 2 phe để phối hợp)
    public static final Color[] TEAM_COLORS = {
        Color.RED,       // P1 - Thuộc Đội Đỏ
        Color.RED,       // P2 - Thuộc Đội Đỏ
        Color.BLUE,      // P3 - Thuộc Đội Xanh
        Color.BLUE       // P4 - Thuộc Đội Xanh
    };

    // ==========================================
    // 6. CÁC HÀM TIỆN ÍCH TRẢ VỀ DỮ LIỆU CHUẨN XÁC
    // ==========================================
    
    /**
     * Tự động trả về số lượng nhân vật cần tạo dựa theo Tùy chọn người chơi
     */
    public static int getPlayerCount() {
        switch (currentMode) {
            case ONE_PLAYER: return 1;
            case TWO_PLAYERS: return 2;
            case THREE_PLAYERS: return 3;
            case FOUR_PLAYERS: 
            case TEAM_2V2: 
                return 4;
            default: return 1;
        }
    }

    /**
     * Tự động lấy màu nền chuẩn theo từng tùy chọn Mini-Game đang chơi
     */
    public static Color getCurrentBackgroundColor() {
        switch (currentGame) {
            case MENU_SCREEN: return COLOR_BG_MENU;
            case CATCH_COINS: return COLOR_BG_COINS;
            case SURVIVAL: return COLOR_BG_SURVIVAL;
            case CAPTURE_FLAG: return COLOR_BG_FLAG;
            default: return COLOR_BG_MENU;
        }
    }
}
