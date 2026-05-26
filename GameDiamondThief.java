package com.mygame.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * FILE 7: MINI-GAME 3 - DIAMOND THIEF (CƯỚP KIM CƯƠNG)
 * Kế thừa từ lớp MiniGame (File 4).
 * Luật chơi: Ôm kim cương để được nhảy điểm liên tục. 
 * Người ôm kim cương sẽ bị chạy chậm. Người khác chạm vào sẽ cướp được quà.
 */
public class GameDiamondThief extends MiniGame {

    private Rectangle diamondBounds;      // Khung vật lý của viên kim cương
    private int holderId;                 // ID của người đang giữ kim cương (-1 nghĩa là đang rơi tự do)
    private float pointAccumulator;       // Bộ đếm thời gian để cộng điểm theo chu kỳ
    private float stealCooldown;          // Thời gian hồi sau khi cướp (tránh việc 2 người đứng đè lên nhau cướp qua lại liên tục)

    private final float DIAMOND_SIZE = 45f;
    private final float POINTS_INTERVAL = 0.2f; // Cứ mỗi 0.2 giây ôm kim cương sẽ được cộng điểm

    public GameDiamondThief(Player[] activePlayers) {
        super(activePlayers);
        this.gameTitle = "TRÒ CHƠI 3: CƯỚP KIM CƯƠNG KHỔNG LỒ";
        // Khởi tạo khung kim cương nằm ở tâm màn hình
        this.diamondBounds = new Rectangle(
            GameConfig.SCREEN_WIDTH / 2f - DIAMOND_SIZE / 2f,
            GameConfig.SCREEN_HEIGHT / 2f - DIAMOND_SIZE / 2f,
            DIAMOND_SIZE,
            DIAMOND_SIZE
        );
    }

    /**
     * LỰC LƯỢNG KHỞI CHẠY: Đặt kim cương về giữa, chưa ai được cầm
     */
    @Override
    protected void onGameStart() {
        this.holderId = -1; 
        this.pointAccumulator = 0f;
        this.stealCooldown = 0f;

        // Đặt lại tọa độ kim cương về chính giữa bản đồ 1920x1080
        this.diamondBounds.setPosition(
            GameConfig.SCREEN_WIDTH / 2f - DIAMOND_SIZE / 2f,
            GameConfig.SCREEN_HEIGHT / 2f - DIAMOND_SIZE / 2f
        );
    }

    /**
     * VÒNG LẶP LOGIC: Xử lý nhặt, giữ, cướp và tính điểm kim cương
     */
    @Override
    protected void updateSpecificGame(float deltaTime, InputController input) {
        // Giảm thời gian hồi cướp kim cương theo thời gian thực
        if (stealCooldown > 0) {
            stealCooldown -= deltaTime;
        }

        // TRƯỜNG HỢP 1: Chưa có ai nhặt được kim cương
        if (holderId == -1) {
            for (Player p : players) {
                if (p.isAlive && p.getBounds().overlaps(diamondBounds)) {
                    holderId = p.id; // Người này chính thức trở thành chủ nhân kim cương
                    stealCooldown = 0.5f; // Cho 0.5 giây an toàn để chạy đi
                    break;
                }
            }
        } 
        // TRƯỜNG HỢP 2: Đã có người đang ôm kim cương chạy trốn
        else {
            Player holder = players[holderId];

            // A. Cập nhật vị trí kim cương chạy theo tâm của người giữ
            diamondBounds.setPosition(
                holder.x + holder.width / 2f - DIAMOND_SIZE / 2f,
                holder.y + holder.height / 2f - DIAMOND_SIZE / 2f
            );

            // B. TÍNH ĐIỂM: Ôm càng lâu điểm nhảy càng nhiều
            pointAccumulator += deltaTime;
            if (pointAccumulator >= POINTS_INTERVAL) {
                holder.addScore(3); // Cộng 3 điểm sau mỗi 0.2 giây giữ được ngọc
                pointAccumulator = 0f;
            }

            // C. CƠ CHẾ CHẠY CHẬM: Giảm 30% tốc độ của người cầm kim cương, những người khác chạy bình thường
            for (Player p : players) {
                if (p.id == holderId) {
                    p.speed = GameConfig.PLAYER_SPEED * 0.7f; // Chạy chậm lại vì vác nặng
                } else {
                    p.speed = GameConfig.PLAYER_SPEED; // Các đối thủ khác giữ nguyên tốc độ săn đuổi
                }
            }

            // D. CƠ CHẾ CƯỚP ĐOẠT: Nếu đối thủ va chạm vào người đang giữ kim cương
            if (stealCooldown <= 0) {
                for (Player attacker : players) {
                    // Nếu kẻ tấn công còn sống, không phải là chủ xe, và húc trúng chủ xe
                    if (attacker.isAlive && attacker.id != holderId && attacker.getBounds().overlaps(holder.getBounds())) {
                        
                        // Nếu đang chơi chế độ 2V2, đồng đội chạm vào nhau thì không cướp của nhau
                        if (GameConfig.currentMode == GameConfig.PlayerMode.TEAM_2V2 && attacker.teamId == holder.teamId) {
                            continue; 
                        }

                        // Tiến hành đổi chủ kim cương
                        holderId = attacker.id;
                        pointAccumulator = 0f;
                        stealCooldown = 0.6f; // Cho kẻ vừa cướp 0.6 giây bảo hộ để kịp quay đầu chạy
                        break;
                    }
                }
            }

            // E. Nếu người ôm kim cương đen đủi bị chết (ở các game có bom/đạn sau này), rơi kim cương ra tại chỗ
            if (!holder.isAlive) {
                holderId = -1;
                pointAccumulator = 0f;
            }
        }
    }

    /**
     * HÀM VẼ ĐỒ HỌA HÌNH HỌC: Vẽ viên kim cương hình thoi lấp lánh
     */
    @Override
    protected void drawSpecificGameGeometry(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Kim cương đổi màu liên tục từ Xanh ngọc sang Trắng tùy theo trạng thái hồi cướp để tạo hiệu ứng lấp lánh
        if (stealCooldown > 0) {
            shapeRenderer.setColor(Color.WHITE); // Trạng thái đang được bảo hộ
        } else {
            shapeRenderer.setColor(Color.CYAN);  // Trạng thái sẵn sàng bị cướp
        }

        // Tính toán tọa độ để vẽ hình thoi (Kim cương hình học)
        float centerX = diamondBounds.x + DIAMOND_SIZE / 2f;
        float centerY = diamondBounds.y + DIAMOND_SIZE / 2f;
        float half = DIAMOND_SIZE / 2f;

        // Vẽ hình thoi bằng cách ghép 2 hình tam giác (Trên và Dưới) lại với nhau
        shapeRenderer.triangle(centerX, centerY + half, centerX - half, centerY, centerX + half, centerY);
        shapeRenderer.triangle(centerX, centerY - half, centerX - half, centerY, centerX + half, centerY);

        shapeRenderer.end();
    }

    @Override
    protected void drawSpecificGameSprites(SpriteBatch batch) {
        // Tạm để trống (Dành cho việc nạp file ảnh diamond.png sau này)
    }
}
