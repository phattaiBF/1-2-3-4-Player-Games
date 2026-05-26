package com.mygame.party;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * FILE 3: BỘ ĐIỀU KHIỂN CẢM ỨNG (INPUT CONTROLLER)
 * Xử lý đa điểm (Multi-touch) chia 4 góc màn hình cho 4 người chơi.
 * Tự động bật/tắt các cụm nút dựa theo số lượng người chơi được chọn từ cấu hình.
 */
public class InputController {

    // Mảng lưu trữ hướng di chuyển (X và Y) của cả 4 người chơi (Giá trị từ -1.0f đến 1.0f)
    private float[] moveX = new float[4];
    private float[] moveY = new float[4];

    // Tọa độ tâm của 4 nút Joystick ảo tại 4 góc màn hình
    private Vector2[] joystickCenters = new Vector2[4];
    
    // Vị trí hiện tại của con gạt điều khiển (Núm nhỏ bên trong nút tròn lớn)
    private Vector2[] joystickKnobs = new Vector2[4];

    // Bán kính vùng nhận diện nút bấm
    private final float JOYSTICK_RADIUS = 130f; 
    private final float KNOB_RADIUS = 50f;

    public InputController() {
        // Khởi tạo tâm cho 4 cụm Joystick ảo tại 4 góc màn hình (Hệ tọa độ ảo 1920x1080)
        joystickCenters[0] = new Vector2(200, 200);                             // P1: Dưới - Trái
        joystickCenters[1] = new Vector2(GameConfig.SCREEN_WIDTH - 200, 200);     // P2: Dưới - Phải
        joystickCenters[2] = new Vector2(200, GameConfig.SCREEN_HEIGHT - 200);    // P3: Trên - Trái
        joystickCenters[3] = new Vector2(GameConfig.SCREEN_WIDTH - 200, GameConfig.SCREEN_HEIGHT - 200); // P4: Trên - Phải

        // Ban đầu, con gạt nằm trùng với tâm nút
        for (int i = 0; i < 4; i++) {
            joystickKnobs[i] = new Vector2(joystickCenters[i]);
        }
    }

    /**
     * Hàm cập nhật trạng thái cảm ứng theo từng khung hình (Update Vòng lặp Game)
     */
    public void update() {
        // Đặt lại hướng di chuyển của tất cả người chơi về 0 và đưa con gạt về tâm
        for (int i = 0; i < 4; i++) {
            moveX[i] = 0;
            moveY[i] = 0;
            joystickKnobs[i].set(joystickCenters[i]);
        }

        // Lấy số lượng người chơi thực tế đang tham gia trò chơi từ File 1
        int activePlayers = GameConfig.getPlayerCount();

        // Vòng lặp quét qua toàn bộ các ngón tay đang chạm trên màn hình (Hỗ trợ tới 10 ngón cùng lúc)
        for (int pointer = 0; pointer < 10; pointer++) {
            if (Gdx.input.isTouched(pointer)) {
                
                // CHUYỂN ĐỔI TỌA ĐỘ: Đổi tọa độ pixel thật của điện thoại sang tọa độ ảo 1920x1080
                float touchX = ((float) Gdx.input.getX(pointer) / Gdx.graphics.getWidth()) * GameConfig.SCREEN_WIDTH;
                float touchY = (1.0f - ((float) Gdx.input.getY(pointer) / Gdx.graphics.getHeight())) * GameConfig.SCREEN_HEIGHT;

                // Kiểm tra xem điểm chạm này thuộc về vùng quản lý của Người chơi nào
                for (int playerIdx = 0; playerIdx < activePlayers; playerIdx++) {
                    Vector2 center = joystickCenters[playerIdx];
                    
                    // Tính khoảng cách từ điểm chạm đến tâm của Joystick người chơi đó
                    float distance = center.dst(touchX, touchY);

                    // Nếu ngón tay chạm nằm trong phạm vi của Joystick ảo
                    if (distance <= JOYSTICK_RADIUS + 100f) { // Thêm 100px vùng đệm để bấm dễ hơn
                        if (distance == 0) continue;

                        // Tính toán vector hướng di chuyển
                        float dirX = (touchX - center.x) / distance;
                        float dirY = (touchY - center.y) / distance;

                        // Nếu kéo quá xa bán kính, giới hạn núm gạt lại ở viền vòng tròn
                        float clampDist = Math.min(distance, JOYSTICK_RADIUS);
                        joystickKnobs[playerIdx].set(center.x + dirX * clampDist, center.y + dirY * clampDist);

                        // Trả về tỷ lệ di chuyển từ -1.0f đến 1.0f cho nhân vật
                        moveX[playerIdx] = dirX * (clampDist / JOYSTICK_RADIUS);
                        moveY[playerIdx] = dirY * (clampDist / JOYSTICK_RADIUS);
                        
                        break; // Ngón tay này đã xử lý xong cho người chơi này, chuyển sang ngón khác
                    }
                }
            }
        }
    }

    /**
     * Hàm vẽ giao diện các nút Joystick lên màn hình để người chơi nhìn thấy chỗ bấm
     * @param shapeRenderer Bộ vẽ hình học của LibGDX
     */
    public void draw(ShapeRenderer shapeRenderer) {
        // Chỉ vẽ số lượng nút tương ứng với chế độ chơi (Ví dụ chơi 2 người thì ẩn nút P3, P4 đi)
        int activePlayers = GameConfig.getPlayerCount();

        // Bật chế độ vẽ hình tròn mờ (Alpha)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < activePlayers; i++) {
            // 1. Vẽ vòng tròn lớn bên ngoài (Màu trắng mờ che bớt nền)
            shapeRenderer.setColor(1f, 1f, 1f, 0.2f);
            shapeRenderer.circle(joystickCenters[i].x, joystickCenters[i].y, JOYSTICK_RADIUS);

            // 2. Vẽ núm gạt nhỏ bên trong (Lấy màu áo đặc trưng của người chơi đó từ File 1)
            shapeRenderer.setColor(GameConfig.SOLO_COLORS[i].r, GameConfig.SOLO_COLORS[i].g, GameConfig.SOLO_COLORS[i].b, 0.6f);
            shapeRenderer.circle(joystickKnobs[i].x, joystickKnobs[i].y, KNOB_RADIUS);
        }
        
        shapeRenderer.end();
    }

    // =======================================================
    // CÁC HÀM GETTER ĐỂ FILE GAME LẤY DỮ LIỆU ĐIỀU KHIỂN
    // =======================================================
    
    public float getMoveX(int playerId) {
        return moveX[playerId];
    }

    public float getMoveY(int playerId) {
        return moveY[playerId];
    }
}
