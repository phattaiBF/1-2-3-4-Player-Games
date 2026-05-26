package com.mygame.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;

/**
 * FILE 8: MINI-GAME 4 - APPLE HARVEST (HỨNG TÁO ĐỘC)
 * Kế thừa từ lớp MiniGame (File 4).
 * Vật phẩm rơi từ trên trời xuống. Táo đỏ cộng điểm, táo tím gây choáng 2 giây.
 */
public class GameAppleHarvest extends MiniGame {

    // Cấu trúc nội bộ để quản lý từng quả táo
    private static class AppleItem {
        Rectangle bounds;
        boolean isToxic; // true = Táo độc, false = Táo ngon
        float fallSpeed; // Tốc độ rơi riêng của từng quả

        AppleItem(float x, float y, float size, boolean isToxic, float speed) {
            this.bounds = new Rectangle(x, y, size, size);
            this.isToxic = isToxic;
            this.fallSpeed = speed;
        }
    }

    private ArrayList<AppleItem> apples;
    private float[] stunTimers = new float[4]; // Bộ đếm thời gian choáng của 4 người chơi

    private final int MAX_APPLES_ON_SCREEN = 12;
    private final float APPLE_SIZE = 35f;

    public GameAppleHarvest(Player[] activePlayers) {
        super(activePlayers);
        this.gameTitle = "TRÒ CHƠI 4: HỨNG TÁO ĐỘC TRÊN TRỜI";
        this.apples = new ArrayList<AppleItem>();
    }

    /**
     * LỰC LƯỢNG KHỞI CHẠY: Xóa táo cũ, giải trừ choáng cho tất cả người chơi
     */
    @Override
    protected void onGameStart() {
        apples.clear();
        for (int i = 0; i < stunTimers.length; i++) {
            stunTimers[i] = 0f;
        }

        // Tạo loạt táo đầu tiên ở các độ cao ngẫu nhiên phía trên màn hình
        for (int i = 0; i < MAX_APPLES_ON_SCREEN; i++) {
            spawnApple(MathUtils.random(GameConfig.SCREEN_HEIGHT, GameConfig.SCREEN_HEIGHT + 500));
        }
    }

    /**
     * Hàm tạo một quả táo mới rơi từ trên đỉnh màn hình
     * @param spawnY Tọa độ Y bắt đầu rơi
     */
    private void spawnApple(float spawnY) {
        float randomX = MathUtils.random(50, GameConfig.SCREEN_WIDTH - 50);
        
        // 30% tỷ lệ xuất hiện táo độc (màu tím), 70% táo thường (màu đỏ)
        boolean isToxic = MathUtils.random() < 0.30f; 
        
        // Tốc độ rơi ngẫu nhiên từ 200 đến 450 pixel/giây để tạo sự kịch tính
        float randomSpeed = MathUtils.random(200f, 450f);

        apples.add(new AppleItem(randomX, spawnY, APPLE_SIZE, isToxic, randomSpeed));
    }

    /**
     * VÒNG LẶP LOGIC: Cập nhật vị trí táo rơi, xử lý choáng và va chạm ăn táo
     */
    @Override
    protected void updateSpecificGame(float deltaTime, InputController input) {
        
        // 1. QUẢN LÝ TRẠNG THÁI CHOÁNG (STUN)
        for (int i = 0; i < players.length; i++) {
            if (stunTimers[i] > 0) {
                stunTimers[i] -= deltaTime;
                players[i].speed = 0f; // Khóa chặt không cho di chuyển bằng cách ép tốc độ về 0
                
                // Hiệu ứng hạt hoặc đổi màu nhân vật sang xám mờ khi bị choáng có thể xử lý ở đây
                if (stunTimers[i] <= 0) {
                    stunTimers[i] = 0f;
                    players[i].speed = GameConfig.PLAYER_SPEED; // Hết choáng, trả lại tốc độ gốc
                }
            }
        }

        // 2. DI CHUYỂN TÁO VÀ TÍNH VA CHẠM
        for (int a = apples.size() - 1; a >= 0; a--) {
            AppleItem apple = apples.get(a);
            
            // Táo rơi xuống theo thời gian
            apple.bounds.y -= apple.fallSpeed * deltaTime;

            // Nếu táo rơi quá đáy màn hình mà không ai hứng được, xóa đi và hồi lại quả mới trên đỉnh
            if (apple.bounds.y < -APPLE_SIZE) {
                apples.remove(a);
                spawnApple(GameConfig.SCREEN_HEIGHT);
                continue;
            }

            // Kiểm tra va chạm với những người chơi còn sống
            for (Player player : players) {
                if (player.isAlive && player.getBounds().overlaps(apple.bounds)) {
                    
                    if (apple.isToxic) {
                        // TRÚNG TÁO ĐỘC: Bị phạt choáng 2 giây lập tức
                        stunTimers[player.id] = 2.0f;
                        player.speed = 0f;
                        player.addScore(-5); // Bị trừ nhẹ 5 điểm tội không quan sát
                    } else {
                        // ĂN TÁO NGON: Cộng 15 điểm thưởng
                        player.addScore(15);
                    }

                    // Xóa quả táo vừa va chạm và bù quả mới
                    apples.remove(a);
                    spawnApple(GameConfig.SCREEN_HEIGHT + MathUtils.random(0, 200));
                    break;
                }
            }
        }
    }

    /**
     * HÀM VẼ ĐỒ HỌA HÌNH HỌC: Vẽ các quả táo tròn trên màn hình
     */
    @Override
    protected void drawSpecificGameGeometry(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (AppleItem apple : apples) {
            // Chỉ vẽ những quả táo đã lọt vào trong phạm vi nhìn thấy của màn hình
            if (apple.bounds.y <= GameConfig.SCREEN_HEIGHT) {
                
                if (apple.isToxic) {
                    shapeRenderer.setColor(Color.PURPLE); // Táo độc màu tím nguy hiểm
                } else {
                    shapeRenderer.setColor(Color.RED);    // Táo thường màu đỏ ngon lành
                }

                float radius = APPLE_SIZE / 2f;
                float centerX = apple.bounds.x + radius;
                float centerY = apple.bounds.y + radius;
                
                // Vẽ thân quả táo hình tròn
                shapeRenderer.circle(centerX, centerY, radius);

                // Vẽ thêm một cuống táo nhỏ màu nâu phía trên để nhận diện
                shapeRenderer.setColor(Color.BROWN);
                shapeRenderer.rect(centerX - 2, centerY + radius - 2, 4, 8);
            }
        }
        
        shapeRenderer.end();
    }

    @Override
    protected void drawSpecificGameSprites(SpriteBatch batch) {
        // Tạm để trống (Dành cho việc nạp file ảnh apple_red.png / apple_purple.png)
    }
}
