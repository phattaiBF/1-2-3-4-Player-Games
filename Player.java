package com.mygame.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

/**
 * FILE 2: QUẢN LÝ NHÂN VẬT (PLAYER)
 * Chứa toàn bộ logic dữ liệu của một người chơi: Vị trí, vận tốc, mạng sống, điểm số.
 * Thiết kế đa năng để tương thích với tất cả 15+ mini-games phía sau.
 */
public class Player {
    // 1. Thuộc tính định danh
    public int id;             // ID từ 0 đến 3 (Tương ứng Người chơi 1 đến 4)
    public String name;        // Tên hiển thị (P1, P2, P3, P4)
    
    // 2. Thuộc tính vị trí và kích cỡ (Dùng cho vật lý và vẽ hình)
    public float x, y;
    public float width, height;
    public float speed;
    
    // 3. Trạng thái sinh tồn và thi đấu
    public int score;          // Điểm số hiện tại trong mini-game
    public int health;         // Lượng máu (Dùng cho các game sinh tồn/bắn nhau)
    public boolean isAlive;    // Trạng thái còn sống hay đã chết
    public int teamId;         // ID Đội: 0 = Đội Đỏ (P1, P2), 1 = Đội Xanh (P3, P4)

    // 4. Khung va chạm ẩn (Dùng để kiểm tra đụng độ vật phẩm/đối thủ)
    private Rectangle bounds;

    /**
     * Hàm khởi tạo nhân vật mới
     * @param id Thứ tự người chơi (0 -> 3)
     */
    public Player(int id) {
        this.id = id;
        this.name = "P" + (id + 1);
        this.width = GameConfig.PLAYER_SIZE;
        this.height = GameConfig.PLAYER_SIZE;
        this.bounds = new Rectangle(x, y, width, height);
        
        // Phân chia đội mặc định (Dùng cho chế độ 2V2)
        this.teamId = (id < 2) ? 0 : 1; 
        
        // Đặt lại các thông số về trạng thái ban đầu
        reset();
    }

    /**
     * Đặt lại nhân vật về trạng thái gốc khi bắt đầu một Mini-Game mới
     */
    public void reset() {
        this.score = 0;
        this.health = 100; // Mặc định đầy 100 máu
        this.isAlive = true;
        this.speed = GameConfig.PLAYER_SPEED;
        
        // Đặt vị trí xuất phát dựa theo cấu hình chuẩn ở File 1
        // Tạm thời xếp 4 góc màn hình để không dính vào nhau
        float[][] startPositions = {
            {150, 150},
            {GameConfig.SCREEN_WIDTH - 150 - width, 150},
            {150, GameConfig.SCREEN_HEIGHT - 150 - height},
            {GameConfig.SCREEN_WIDTH - 150 - width, GameConfig.SCREEN_HEIGHT - 150 - height}
        };
        
        this.x = startPositions[id][0];
        this.y = startPositions[id][1];
        this.bounds.setPosition(this.x, this.y);
    }

    /**
     * Hàm xử lý di chuyển nhân vật và giữ không cho chạy ra khỏi màn hình điện thoại
     * @param deltaX Hướng ngang (-1: Trái, 1: Phải, 0: Đứng yên)
     * @param deltaY Hướng dọc (-1: Xuống, 1: Lên, 0: Đứng yên)
     * @param deltaTime Thời gian trôi qua giữa các khung hình (để di chuyển mượt)
     */
    public void move(float deltaX, float deltaY, float deltaTime) {
        if (!isAlive) return; // Nếu đã chết thì không cho di chuyển

        // Cập nhật tọa độ mới dựa trên tốc độ
        this.x += deltaX * speed * deltaTime;
        this.y += deltaY * speed * deltaTime;

        // GIỚI HẠN BIÊN: Không cho nhân vật chạy vượt quá viền màn hình 1920x1080
        if (this.x < 0) this.x = 0;
        if (this.x > GameConfig.SCREEN_WIDTH - this.width) this.x = GameConfig.SCREEN_WIDTH - this.width;
        
        if (this.y < 0) this.y = 0;
        if (this.y > GameConfig.SCREEN_HEIGHT - this.height) this.y = GameConfig.SCREEN_HEIGHT - this.height;

        // Cập nhật lại khung va chạm theo vị trí mới
        this.bounds.setPosition(this.x, this.y);
    }

    /**
     * Hàm xử lý khi nhân vật bị tấn công hoặc dính bẫy
     */
    public void takeDamage(int damage) {
        if (!isAlive) return;
        
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            this.isAlive = false; // Nhân vật bị loại khỏi mini-game hiện tại
        }
    }

    /**
     * Hàm cộng điểm khi thực hiện nhiệm vụ thành công (Ví dụ: Ăn xu, chiếm cứ điểm)
     */
    public void addScore(int points) {
        if (isAlive) {
            this.score += points;
        }
    }

    /**
     * Lấy màu sắc thực tế của nhân vật dựa vào chế độ chơi hiện tại
     */
    public Color getColor() {
        if (GameConfig.currentMode == GameConfig.PlayerMode.TEAM_2V2) {
            return GameConfig.TEAM_COLORS[id]; // Trả về màu theo đội (Đỏ / Xanh)
        }
        return GameConfig.SOLO_COLORS[id]; // Trả về 4 màu riêng biệt độc lập
    }

    /**
     * Trả về khung va chạm (Dùng để tính toán xem có chạm vào vàng, bom, hoặc người khác không)
     */
    public Rectangle getBounds() {
        return this.bounds;
    }
}
