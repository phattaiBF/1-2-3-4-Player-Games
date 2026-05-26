package com.mygame.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * FILE 4: LỚP NỀN TẢNG MINI-GAME (ABSTRACT CLASS)
 * Đây là bộ khung chuẩn hóa cho mọi trò chơi trong hệ thống 15+ game.
 * Tự động quản lý thời gian, vẽ giao diện điểm số (HUD), và tính toán phe chiến thắng.
 */
public abstract class MiniGame {
    
    protected Player[] players;       // Danh sách người chơi tham gia game này
    protected float timeRemaining;    // Thời gian còn lại của trận đấu (tính bằng giây)
    protected boolean isFinished;     // Trạng thái game đã kết thúc hay chưa
    protected String gameTitle;       // Tên của mini-game hiện tại
    protected String winnerText;      // Chuỗi chữ hiển thị người chiến thắng khi hết giờ

    /**
     * Hàm khởi tạo một Mini-Game
     * @param activePlayers Mảng danh sách các người chơi truyền từ bộ điều khiển chính
     */
    public MiniGame(Player[] activePlayers) {
        this.players = activePlayers;
        this.timeRemaining = 30.0f;   // Mỗi mini-game mặc định diễn ra trong 30 giây tốc chiến
        this.isFinished = false;
        this.winnerText = "";
        this.gameTitle = "Mini Game";
    }

    /**
     * Hàm khởi động game (Reset điểm số, đặt lại vị trí nhân vật chuẩn bị chiến đấu)
     */
    public void start() {
        this.isFinished = false;
        this.timeRemaining = 30.0f;
        this.winnerText = "";
        
        // Đặt lại trạng thái ban đầu cho tất cả người chơi tham gia
        for (Player player : players) {
            player.reset();
        }
        
        // Gọi hàm khởi tạo riêng của từng trò chơi cụ thể
        onGameStart();
    }

    /**
     * Vòng lặp cập nhật logic chung của hệ thống (Thời gian, chuyển động cơ bản)
     */
    public void update(float deltaTime, InputController input) {
        if (isFinished) return;

        // 1. Trừ thời gian đếm ngược của game
        timeRemaining -= deltaTime;
        if (timeRemaining <= 0) {
            timeRemaining = 0;
            endGame();
        }

        // 2. Cập nhật vị trí di chuyển của từng người chơi dựa trên nút bấm Joystick từ File 3
        for (int i = 0; i < players.length; i++) {
            if (players[i].isAlive) {
                // Lấy tín hiệu điều khiển từ InputController gán vào cho từng Player di chuyển
                players[i].move(input.getMoveX(i), input.getMoveY(i), deltaTime);
            }
        }

        // 3. Chạy logic riêng biệt của từng Mini-game (Ví dụ: sinh vàng, tính va chạm đạn bay...)
        updateSpecificGame(deltaTime, input);
    }

    /**
     * Hàm vẽ giao diện chung lên màn hình (Màu nền, nhân vật, và bảng điểm HUD)
     */
    public void draw(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
        // 1. Vẽ các thành phần hình học của riêng từng game (Vẽ trước để nằm bên dưới nhân vật)
        drawSpecificGameGeometry(shapeRenderer);
        drawSpecificGameSprites(batch);

        // 2. Vẽ hình dáng nhân vật (Vẽ các khối hình vuông đại diện cho người chơi)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Player player : players) {
            if (player.isAlive) {
                shapeRenderer.setColor(player.getColor());
                shapeRenderer.rect(player.x, player.y, player.width, player.height);
            }
        }
        shapeRenderer.end();

        // 3. VẼ GIAO DIỆN ĐIỂM SỐ VÀ THỜI GIAN (HUD) LÊN TRÊN CÙNG MÀN HÌNH
        batch.begin();
        
        // Vẽ Tên Game và Thời gian còn lại ở chính giữa phía trên màn hình
        font.setColor(Color.WHITE);
        font.draw(batch, gameTitle, GameConfig.SCREEN_WIDTH / 2f - 100, GameConfig.SCREEN_HEIGHT - 20);
        font.draw(batch, "TIME: " + (int)timeRemaining + "s", GameConfig.SCREEN_WIDTH / 2f - 50, GameConfig.SCREEN_HEIGHT - 60);

        // Vẽ Điểm/Máu của từng người chơi tại các góc màn hình để dễ quan sát
        for (int i = 0; i < players.length; i++) {
            Player p = players[i];
            font.setColor(p.getColor());
            
            // Tự động căn tọa độ chữ hiển thị điểm số theo góc của người chơi đó
            float textX = (i == 1 || i == 3) ? GameConfig.SCREEN_WIDTH - 250 : 50;
            float textY = (i == 2 || i == 3) ? GameConfig.SCREEN_HEIGHT - 50 : 80;
            
            String statusText = p.name + " Score: " + p.score;
            if (p.health < 100) {
                statusText += " (HP: " + p.health + ")";
            }
            if (!p.isAlive) {
                statusText += " [LOẠI]";
            }
            
            font.draw(batch, statusText, textX, textY);
        }

        // Nếu trận đấu kết thúc, vẽ bảng thông báo người thắng cuộc to giữa màn hình
        if (isFinished) {
            font.setColor(Color.GOLD);
            font.draw(batch, "TRẬN ĐẤU KẾT THÚC!", GameConfig.SCREEN_WIDTH / 2f - 150, GameConfig.SCREEN_HEIGHT / 2f + 50);
            font.draw(batch, winnerText, GameConfig.SCREEN_WIDTH / 2f - 200, GameConfig.SCREEN_HEIGHT / 2f - 100);
        }

        batch.end();
    }

    /**
     * Xử lý kết thúc mini-game và tự động tính toán xem ai hay đội nào thắng dựa theo điểm số
     */
    private void endGame() {
        this.isFinished = true;

        // KIỂM TRA THẮNG THUA CHO CHẾ ĐỘ ĐẤU ĐỘI 2V2
        if (GameConfig.currentMode == GameConfig.PlayerMode.TEAM_2V2) {
            int scoreTeamRed = 0;   // Tổng điểm P1 + P2
            int scoreTeamBlue = 0;  // Tổng điểm P3 + P4

            for (Player p : players) {
                if (p.teamId == 0) scoreTeamRed += p.score;
                else scoreTeamBlue += p.score;
            }

            if (scoreTeamRed > scoreTeamBlue) {
                winnerText = "ĐỘI ĐỎ CHIẾN THẮNG!";
            } else if (scoreTeamBlue > scoreTeamRed) {
                winnerText = "ĐỘI XANH DƯƠNG CHIẾN THẮNG!";
            } else {
                winnerText = "HAI ĐỘI HÒA NHAU!";
            }
        } 
        // KIỂM TRA THẮNG THUA CHO CHẾ ĐỘ ĐẤU ĐƠN (1P, 2P, 3P, 4P)
        else {
            int maxScore = -9999;
            String winnerName = "";
            boolean isTie = false;

            for (Player p : players) {
                if (p.isAlive && p.score > maxScore) {
                    maxScore = p.score;
                    winnerName = p.name;
                    isTie = false;
                } else if (p.isAlive && p.score == maxScore) {
                    isTie = true; // Trùng điểm cao nhất -> Hòa
                }
            }

            if (isTie || maxScore == -9999) {
                winnerText = "KẾT QUẢ: HÒA NHAU!";
            } else {
                winnerText = "NGƯỜI THẮNG: " + winnerName + " (" + maxScore + " Điểm)";
            }
        }
    }

    // =========================================================================
    // CÁC HÀM TRỪU TƯỢNG (MỖI MINI-GAME BẮT BUỘC PHẢI TỰ TRIỂN KHAI THEO LUẬT RIÊNG)
    // =========================================================================
    
    protected abstract void onGameStart(); 
    protected abstract void updateSpecificGame(float deltaTime, InputController input);
    protected abstract void drawSpecificGameGeometry(ShapeRenderer shapeRenderer);
    protected abstract void drawSpecificGameSprites(SpriteBatch batch);

    public boolean isFinished() {
        return isFinished;
    }
}
