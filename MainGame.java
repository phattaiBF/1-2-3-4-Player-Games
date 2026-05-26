package com.mygame.party;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;

/**
 * FILE 10: BỘ ĐIỀU KHIỂN TRUNG TÂM (MAIN GAME CONTROLLER)
 * Quản lý vòng đời trò chơi, chuyển đổi trạng thái Menu -> Chơi Game -> Kết quả.
 * Tự động bốc thăm ngẫu nhiên trọn bộ 15 Mini-Games.
 */
public class MainGame extends ApplicationAdapter {

    // Các trạng thái của trò chơi
    private enum GameState { MENU, PLAYING, GAME_OVER }
    private GameState currentState = GameState.MENU;

    // Thành phần đồ họa hệ thống
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    // Quản lý thực thể
    private Player[] players;
    private InputController inputController;
    
    // Quản lý danh sách 15 Mini-Games
    private ArrayList<MiniGame> miniGameList;
    private MiniGame currentMiniGame;
    private int gamePlayCount = 0; // Đếm số trận đã chơi để phân định thắng bại cuối cùng
    private final int TOTAL_ROUNDS = 4; // Cứ đá 4 hiệp ngẫu nhiên sẽ tìm ra nhà vô địch

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont(); // LibGDX mặc định dùng font chữ Arial màu trắng
        font.getData().setScale(2.5f); // Phóng to chữ lên để nhìn rõ trên màn hình điện thoại

        inputController = new InputController();
        // Đăng ký bộ lắng nghe sự kiện cảm ứng từ File 3 vào hệ thống LibGDX
        Gdx.input.setInputProcessor(inputController);

        miniGameList = new ArrayList<>();
    }

    /**
     * Hàm khởi tạo cấu hình người chơi dựa trên lựa chọn ở màn hình Menu
     */
    private void setupPlayersAndGames() {
        int count = GameConfig.getPlayerCount();
        players = new Player[count];

        for (int i = 0; i < count; i++) {
            // Khởi tạo người chơi tại các vị trí rải rác ban đầu
            float startX = 200 + (i * 300);
            float startY = 400;
            players[i] = new Player(i, "PLAYER " + (i + 1), startX, startY);
        }

        // Làm sạch danh sách và nạp đầy đủ 15 game vào bộ nhớ
        miniGameList.clear();
        miniGameList.add(new GameCatchCoins(players));   // Game 1 (File 5)
        miniGameList.add(new GameSurvival(players));     // Game 2 (File 6)
        miniGameList.add(new GameDiamondThief(players)); // Game 3 (File 7)
        miniGameList.add(new GameAppleHarvest(players)); // Game 4 (File 8)
        
        // Nạp tiếp 11 game từ siêu file tổng hợp GamePack (File 9)
        GamePack.addRemainingGames(miniGameList, players);

        gamePlayCount = 0;
        pickRandomMiniGame();
    }

    /**
     * Thuật toán bốc thăm ngẫu nhiên một trong số các mini-game chưa chơi
     */
    private void pickRandomMiniGame() {
        if (miniGameList.isEmpty() || gamePlayCount >= TOTAL_ROUNDS) {
            currentState = GameState.GAME_OVER;
            return;
        }

        // Quay số ngẫu nhiên lấy chỉ mục từ 0 đến kích thước danh sách game đang có
        int randomIndex = MathUtils.random(0, miniGameList.size() - 1);
        currentMiniGame = miniGameList.remove(randomIndex); // Lấy ra chơi và xóa khỏi hàng đợi tránh trùng lặp
        currentMiniGame.start(); // Kích hoạt chạy hàm start của game được chọn
        gamePlayCount++;
    }

    @Override
    public void render() {
        // Xóa sạch màn hình sau mỗi khung hình để tránh hiện tượng bóng ma đồ họa
        Gdx.gl.glClearColor(0.1f, 0.11f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();

        // ĐIỀU HƯỚNG VÒNG LẶP THEO TRẠNG THÁI GAME
        switch (currentState) {
            case MENU:
                updateMenu();
                drawMenu();
                break;
                
            case PLAYING:
                // Cập nhật logic di chuyển và luật chơi riêng của Mini-Game hiện tại
                currentMiniGame.update(deltaTime, inputController);
                
                // Vẽ toàn bộ đồ họa của Mini-Game đó lên màn hình
                currentMiniGame.draw(batch, shapeRenderer, font);
                
                // Vẽ đè các nút Joystick cảm ứng ảo (File 3) lên trên cùng để người chơi điều khiển
                inputController.drawJoysticks(shapeRenderer);

                // Nếu mini-game hiện tại báo kết thúc (hết 30 giây hoặc có người bị loại sạch)
                if (currentMiniGame.isFinished()) {
                    // Cho người chơi nghỉ xả hơi 3 giây ngắm bảng điểm trước khi đổi game tiếp theo
                    if (currentMiniGame.timeRemaining <= -3.0f) {
                        pickRandomMiniGame();
                    } else {
                        currentMiniGame.timeRemaining -= deltaTime; // Trừ âm thời gian để làm bộ đếm chờ
                    }
                }
                break;
                
            case GAME_OVER:
                updateGameOver();
                drawGameOver();
                break;
        }
    }

    // =========================================================================
    // XỬ LÝ TRẠNG THÁI 1: MÀN HÌNH MENU CHÍNH
    // =========================================================================
    private void updateMenu() {
        // Kiểm tra xem người chơi bấm vào vùng màn hình nào để chọn chế độ
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            // LibGDX có tọa độ Y bị đảo ngược so với hệ đồ họa, cần quy đổi chuẩn bằng công thức này
            float touchY = GameConfig.SCREEN_HEIGHT - Gdx.input.getY();

            // Vùng nút chọn chế độ Đấu Đơn (Solo)
            if (touchX > 100 && touchX < 500 && touchY > 400 && touchY < 550) {
                GameConfig.currentMode = GameConfig.PlayerMode.SOLO_4P; // Mặc định 4 người chơi solo
                setupPlayersAndGames();
                currentState = GameState.PLAYING;
            }
            // Vùng nút chọn chế độ Đấu Đội (Team 2v2)
            else if (touchX > 600 && touchX < 1000 && touchY > 400 && touchY < 550) {
                GameConfig.currentMode = GameConfig.PlayerMode.TEAM_2V2; // Chuyển sang chế độ chia đội
                setupPlayersAndGames();
                currentState = GameState.PLAYING;
            }
        }
    }

    private void drawMenu() {
        // Vẽ các khối nút bấm bằng ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.CORAL);
        shapeRenderer.rect(100, 400, 400, 150); // Nút Solo
        shapeRenderer.setColor(Color.SKY);
        shapeRenderer.rect(600, 400, 400, 150); // Nút Đấu Đội
        shapeRenderer.end();

        // Viết chữ tiêu đề lên nút bấm
        batch.begin();
        font.setColor(Color.GOLD);
        font.draw(batch, "SIÊU ĐẠI HỘI PARTY GAME (15-IN-1)", GameConfig.SCREEN_WIDTH / 3f, GameConfig.SCREEN_HEIGHT - 150);
        
        font.setColor(Color.WHITE);
        font.draw(batch, "CHẾ ĐỘ: SOLO 4P", 160, 490);
        font.draw(batch, "CHẾ ĐỘ: ĐỘI 2V2", 660, 490);
        
        font.setColor(Color.GRAY);
        font.draw(batch, "Bấm trực tiếp vào ô màu để bắt đầu trận đấu", GameConfig.SCREEN_WIDTH / 3f, 200);
        batch.end();
    }

    // =========================================================================
    // XỬ LÝ TRẠNG THÁI 3: MÀN HÌNH TỔNG KẾT (BẢNG VÀNG VÔ ĐỊCH)
    // =========================================================================
    private void updateGameOver() {
        // Nếu người chơi chạm tay vào màn hình kết quả, tự động đá về màn hình Menu ban đầu để làm ván mới
        if (Gdx.input.justTouched()) {
            currentState = GameState.MENU;
        }
    }

    private void drawGameOver() {
        batch.begin();
        font.setColor(Color.GOLD);
        font.draw(batch, "BẢNG XẾP HẠNG CHUNG CUỘC SAU " + TOTAL_ROUNDS + " HIỆP ĐẤU", GameConfig.SCREEN_WIDTH / 4f, GameConfig.SCREEN_HEIGHT - 150);

        // Duyệt danh sách in điểm tích lũy tổng của tất cả trận đấu để xem ai cao điểm nhất
        float scoreY = GameConfig.SCREEN_HEIGHT - 300;
        for (Player p : players) {
            font.setColor(p.getColor());
            font.draw(batch, p.name + " tổng điểm tích lũy: " + p.score + "đ", GameConfig.SCREEN_WIDTH / 3f, scoreY);
            scoreY -= 80;
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "Chạm tay vào màn hình để quay lại Menu chính...", GameConfig.SCREEN_WIDTH / 4f, 150);
        batch.end();
    }

    @Override
    public void dispose() {
        // Giải phóng bộ nhớ RAM của card đồ họa khi tắt ứng dụng, tránh rò rỉ bộ nhớ (Memory Leak)
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
