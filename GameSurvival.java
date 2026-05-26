package com.mygame.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * FILE 6: MINI-GAME 2 - SURVIVAL (ĐẤU TRƯỜNG SINH TỒN)
 * Kế thừa từ lớp MiniGame (File 4).
 * Luật chơi: Vòng bo thu nhỏ dần. Nằm ngoài bo bị trừ máu. 
 * Người chơi có thể húc nhau để đẩy đối thủ ra ngoài vực chết.
 */
public class GameSurvival extends MiniGame {

    private Vector2 center;           // Tọa độ tâm của vòng bo (Chính giữa màn hình)
    private float safeZoneRadius;     // Bán kính vùng an toàn hiện tại
    private final float SHRINK_SPEED = 18f; // Tốc độ co lại của vòng bo (pixel/giây)
    private float damageTimer = 0f;   // Bộ đếm thời gian để trừ máu theo chu kỳ

    /**
     * Hàm khởi tạo Mini-game Sinh tồn
     */
    public GameSurvival(Player[] activePlayers) {
        super(activePlayers);
        this.gameTitle = "TRÒ CHƠI 2: ĐẤU TRƯỜNG SINH TỒN";
        this.center = new Vector2(GameConfig.SCREEN_WIDTH / 2f, GameConfig.SCREEN_HEIGHT / 2f);
    }

    /**
     * LỰC LƯỢNG KHỞI CHẠY: Cài đặt lại máu và kích thước bo ban đầu
     */
    @Override
    protected void onGameStart() {
        this.safeZoneRadius = 520f;   // Bán kính vòng bo lúc mới vào game cực rộng
        this.damageTimer = 0f;

        // Đảm bảo tất cả người chơi đều hồi đầy 100 máu khi bắt đầu trận
        for (Player p : players) {
            p.health = 100;
        }
    }

    /**
     * VÒNG LẶP LOGIC: Tính toán co bo, va chạm húc nhau và trừ máu ngoài bo
     */
    @Override
    protected void updateSpecificGame(float deltaTime, InputController input) {
        // 1. Co rút vòng bo sinh tồn theo thời gian (Giới hạn không thu nhỏ quá mức 100px)
        if (safeZoneRadius > 100f) {
            safeZoneRadius -= SHRINK_SPEED * deltaTime;
        }

        // 2. CƠ CHẾ HÚC NHAU (VẬT LÝ VA CHẠM): Đẩy lùi đối thủ khi chạm vào nhau
        for (int i = 0; i < players.length; i++) {
            for (int j = i + 1; j < players.length; j++) {
                Player p1 = players[i];
                Player p2 = players[j];

                // Nếu cả 2 còn sống và va chạm khung hình vuông với nhau
                if (p1.isAlive && p2.isAlive && p1.getBounds().overlaps(p2.getBounds())) {
                    // Tính toán vector hướng đẩy dựa trên khoảng cách giữa tâm 2 nhân vật
                    float midX1 = p1.x + p1.width / 2f;
                    float midY1 = p1.y + p1.height / 2f;
                    float midX2 = p2.x + p2.width / 2f;
                    float midY2 = p2.y + p2.height / 2f;

                    float pushX = midX1 - midX2;
                    float pushY = midY1 - midY2;

                    // Chuẩn hóa vector để lực đẩy ổn định, không bị giật
                    float distance = (float) Math.sqrt(pushX * pushX + pushY * pushY);
                    if (distance == 0) distance = 1f;
                    pushX /= distance;
                    pushY /= distance;

                    // Áp dụng lực húc văng ngược chiều nhau (Bật ra xa)
                    float pushForce = 200f * deltaTime;
                    p1.move(pushX * pushForce, pushY * pushForce, 1f);
                    p2.move(-pushX * pushForce, -pushY * pushForce, 1f);
                }
            }
        }

        // 3. XỬ LÝ SÁT THƯƠNG VÒNG BO: Quét và trừ máu mỗi 0.5 giây một lần
        damageTimer += deltaTime;
        boolean triggerDamage = false;
        if (damageTimer >= 0.5f) {
            triggerDamage = true;
            damageTimer = 0f;
        }

        int aliveCount = 0;
        Player lastSurvivor = null;

        for (Player p : players) {
            if (!p.isAlive) continue;

            aliveCount++;
            lastSurvivor = p;

            // Tính khoảng cách từ tâm nhân vật đến tâm vòng bo
            float pCenterX = p.x + p.width / 2f;
            float pCenterY = p.y + p.height / 2f;
            float distanceToCenter = center.dst(pCenterX, pCenterY);

            // Nếu khoảng cách lớn hơn bán kính -> Người chơi đang đứng ngoài rìa bo nguy hiểm
            if (distanceToCenter > safeZoneRadius) {
                if (triggerDamage) {
                    p.takeDamage(12); // Trừ 12 máu mỗi chu kỳ ngoài bo
                }
            } else {
                // Sống sót an toàn bên trong bo được thưởng điểm tích lũy theo thời gian
                p.addScore(1); 
            }
        }

        // 4. KIỂM TRA ĐIỀU KIỆN THẮNG SỚM (Nếu chỉ còn 1 phe sống sót, kết thúc game luôn)
        if (GameConfig.currentMode == GameConfig.PlayerMode.TEAM_2V2) {
            boolean teamRedAlive = players[0].isAlive || players[1].isAlive;
            boolean teamBlueAlive = players[2].isAlive || players[3].isAlive;
            
            // Nếu 1 trong 2 đội bị quét sạch, cho dừng thời gian trận đấu
            if (!teamRedAlive || !teamBlueAlive) {
                timeRemaining = 0f;
            }
        } else {
            // Chế độ Solo: Nếu tổng số người chơi lớn hơn 1 và chỉ còn duy nhất 1 người sống sót
            if (GameConfig.getPlayerCount() > 1 && aliveCount <= 1) {
                if (lastSurvivor != null) {
                    lastSurvivor.addScore(100); // Thưởng nóng 100 điểm cho nhà vô địch sinh tồn
                }
                timeRemaining = 0f; // Ép thời gian về 0 để kích hoạt hàm kết thúc trận đấu của lớp cha
            }
        }
    }

    /**
     * HÀM VẼ ĐỒ HỌA HÌNH HỌC: Vẽ ranh giới vòng bo sinh tồn nguy hiểm
     */
    @Override
    protected void drawSpecificGameGeometry(ShapeRenderer shapeRenderer) {
        // Vẽ vùng an toàn màu xanh mờ dịu mắt bên trong
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.8f, 0.1f, 0.04f); 
        shapeRenderer.circle(center.x, center.y, safeZoneRadius);
        shapeRenderer.end();
        
        // Vẽ đường viền vòng bo màu đỏ rực cảnh báo nguy hiểm bằng các nét lặp dày
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        for (int i = 0; i < 4; i++) {
            shapeRenderer.circle(center.x, center.y, safeZoneRadius + i);
        }
        shapeRenderer.end();
    }

    /**
     * HÀM VẼ HÌNH ẢNH (SPRITE): Để trống
     */
    @Override
    protected void drawSpecificGameSprites(SpriteBatch batch) {
        // Có thể bổ sung hiệu ứng khói bụi đỏ tại đây nếu có texture ảnh
    }
}
