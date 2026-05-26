package com.mygame.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;

/**
 * FILE 5: MINI-GAME 1 - CATCH COINS (ĂN XU TỐC ĐỘ)
 * Kế thừa từ lớp MiniGame (File 4).
 * Nhiệm vụ của người chơi: Di chuyển thật nhanh để thu thập nhiều xu vàng nhất.
 */
public class GameCatchCoins extends MiniGame {

    // Danh sách lưu trữ khung va chạm của các đồng xu đang có trên màn hình
    private ArrayList<Rectangle> coins;
    
    // Cấu hình số lượng xu xuất hiện cùng một lúc trên bản đồ
    private final int MAX_COINS = 8;
    private final float COIN_SIZE = 30f;

    /**
     * Hàm khởi tạo Mini-game Ăn xu
     */
    public GameCatchCoins(Player[] activePlayers) {
        super(activePlayers);
        this.gameTitle = "TRÒ CHƠI 1: ĂN XU TỐC ĐỘ";
        this.coins = new ArrayList<Rectangle>();
    }

    /**
     * LỰC LƯỢNG KHỞI CHẠY: Được gọi tự động khi trò chơi này bắt đầu
     */
    @Override
    protected void onGameStart() {
        coins.clear();
        // Tạo sẵn các đồng xu ngẫu nhiên tại các vị trí trên màn hình 1920x1080
        for (int i = 0; i < MAX_COINS; i++) {
            spawnNewCoin();
        }
    }

    /**
     * Hàm tính toán vị trí ngẫu nhiên để tạo một đồng xu mới
     * Tránh việc xu sinh ra quá sát mép màn hình khiến người chơi khó ăn
     */
    private void spawnNewCoin() {
        float randomX = MathUtils.random(100, GameConfig.SCREEN_WIDTH - 100);
        float randomY = MathUtils.random(100, GameConfig.SCREEN_HEIGHT - 100);
        
        // Thêm một khung hình vuông đại diện cho đồng xu vào danh sách
        coins.add(new Rectangle(randomX, randomY, COIN_SIZE, COIN_SIZE));
    }

    /**
     * VÒNG LẶP LOGIC: Xử lý va chạm giữa 4 Người chơi và các Đồng xu
     */
    @Override
    protected void updateSpecificGame(float deltaTime, InputController input) {
        // Duyệt qua từng đồng xu trong danh sách (Duyệt ngược từ cuối lên để xóa không bị lỗi chỉ mục)
        for (int c = coins.size() - 1; c >= 0; c--) {
            Rectangle coinBounds = coins.get(c);

            // Kiểm tra xem có người chơi nào chạm vào đồng xu này không
            for (Player player : players) {
                if (player.isAlive && player.getBounds().overlaps(coinBounds)) {
                    
                    // 1. Cộng 10 điểm cho người chơi vừa ăn được xu
                    player.addScore(10);
                    
                    // 2. Xóa đồng xu vừa bị ăn khỏi danh sách
                    coins.remove(c);
                    
                    // 3. Lập tức bù lại một đồng xu mới ở vị trí ngẫu nhiên khác
                    spawnNewCoin();
                    
                    // Thoát vòng lặp kiểm tra người chơi cho xu này vì nó đã bị xóa
                    break; 
                }
            }
        }
    }

    /**
     * HÀM VẼ ĐỒ HỌA HÌNH HỌC: Vẽ các đồng xu vàng lên màn hình
     */
    @Override
    protected void drawSpecificGameGeometry(ShapeRenderer shapeRenderer) {
        // Bật chế độ vẽ hình học đặc (Filled)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Cài đặt màu vàng óng cho đồng xu
        shapeRenderer.setColor(Color.GOLD);
        
        // Duyệt qua danh sách và vẽ từng đồng xu dưới dạng hình tròn
        for (Rectangle coin : coins) {
            // Lấy tâm của khung hình vuông để vẽ hình tròn cho chuẩn xác
            float centerX = coin.x + COIN_SIZE / 2f;
            float centerY = coin.y + COIN_SIZE / 2f;
            float radius = COIN_SIZE / 2f;
            
            shapeRenderer.circle(centerX, centerY, radius);
        }
        
        shapeRenderer.end();
    }

    /**
     * HÀM VẼ HÌNH ẢNH (SPRITE): Tạm thời để trống vì chúng ta đang vẽ bằng ShapeRenderer
     */
    @Override
    protected void drawSpecificGameSprites(SpriteBatch batch) {
        // Nếu sau này bạn có file ảnh coin.png, bạn sẽ nạp vào đây để vẽ đè lên hình tròn
    }
}
