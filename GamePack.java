package com.mygame.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

/**
 * FILE 9: TỔNG HỢP 11 MINI-GAMES TIẾP THEO (ĐỦ BỘ 15 GAMES)
 * File này chứa lớp đăng ký GamePack và toàn bộ logic từ Game 5 đến Game 15.
 */
public class GamePack {
    // Hàm tiện ích để nạp nhanh 11 game này vào hệ thống quản lý
    public static void addRemainingGames(ArrayList<MiniGame> list, Player[] players) {
        list.add(new GameColorFloor(players));      // Game 5
        list.add(new GameBombPass(players));        // Game 6
        list.add(new GameBulletDodge(players));     // Game 7
        list.add(new GameChickenRun(players));      // Game 8
        list.add(new GameRingOut(players));         // Game 9
        list.add(new GameTankBattle(players));      // Game 10
        list.add(new GameCaptureFlag(players));     // Game 11
        list.add(new GameTerritoryHold(players));   // Game 12
        list.add(new GameTugOfWar(players));        // Game 13
        list.add(new GameBridgeCross(players));     // Game 14
        list.add(new GameLightSwitch(players));     // Game 15
    }
}

// =========================================================================
// GAME 5: SƠN MÀU GẠCH nền (COLOR FLOOR)
// =========================================================================
class GameColorFloor extends MiniGame {
    private int[][] tileOwners = new int[8][6]; // Lưới gạch 8x6 ô
    private float tileW = GameConfig.SCREEN_WIDTH / 8f;
    private float tileH = GameConfig.SCREEN_HEIGHT / 6f;

    public GameColorFloor(Player[] p) { super(p); this.gameTitle = "GAME 5: SƠN MÀU NỀN GẠCH"; }
    @Override protected void onGameStart() {
        for(int x=0; x<8; x++) for(int y=0; y<6; y++) tileOwners[x][y] = -1; // Chưa ai chiếm
    }
    @Override protected void updateSpecificGame(float dt, InputController in) {
        for (Player p : players) {
            if (!p.isAlive) continue;
            int tx = (int)((p.x + p.width/2.5f) / tileW);
            int ty = (int)((p.y + p.height/2.5f) / tileH);
            if (tx >= 0 && tx < 8 && ty >= 0 && ty < 6) {
                if (tileOwners[tx][ty] != p.id) {
                    tileOwners[tx][ty] = p.id;
                    p.addScore(1); // Chiếm được 1 ô cộng 1 điểm
                }
            }
        }
    }
    @Override protected void drawSpecificGameGeometry(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for(int x=0; x<8; x++) {
            for(int y=0; y<6; y++) {
                int owner = tileOwners[x][y];
                if (owner == -1) sr.setColor(0.2f, 0.2f, 0.2f, 1f);
                else sr.setColor(players[owner].getColor());
                sr.rect(x * tileW + 2, y * tileH + 2, tileW - 4, tileH - 4);
            }
        }
        sr.end();
    }
    @Override protected void drawSpecificGameSprites(SpriteBatch b) {}
}

// =========================================================================
// GAME 6: TRUYỀN BOM HẸN GIỜ (BOMB PASS)
// =========================================================================
class GameBombPass extends MiniGame {
    private int bombHolderId;
    private float passCooldown;
    public GameBombPass(Player[] p) { super(p); this.gameTitle = "GAME 6: TRUYỀN BOM HẸN GIỜ"; }
    @Override protected void onGameStart() { 
        bombHolderId = MathUtils.random(0, players.length - 1); 
        passCooldown = 0f;
    }
    @Override protected void updateSpecificGame(float dt, InputController in) {
        if (passCooldown > 0) passCooldown -= dt;
        Player holder = players[bombHolderId];
        holder.speed = GameConfig.PLAYER_SPEED * 1.2f; // Kẻ giữ bom được tăng tốc để đuổi bám
        
        // Cứ sống sót mỗi giây mà không giữ bom thì được cộng điểm
        for (Player p : players) {
            if (p.id != bombHolderId && p.isAlive) p.addScore(1);
        }

        if (passCooldown <= 0) {
            for (Player p : players) {
                if (p.id != bombHolderId && p.isAlive && p.getBounds().overlaps(holder.getBounds())) {
                    bombHolderId = p.id;
                    passCooldown = 0.7f; // Chống cướp lại ngay lập tức
                    break;
                }
            }
        }
        // Hình phạt cuối giờ: Ai ôm bom khi hết giờ sẽ bị trừ 100 điểm
        if (timeRemaining <= 0.1f) players[bombHolderId].addScore(-100);
    }
    @Override protected void drawSpecificGameGeometry(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.BLACK); // Vẽ quả bom đen trên đầu người giữ bom
        Player h = players[bombHolderId];
        sr.circle(h.x + h.width/2f, h.y + h.height + 25f, 20f);
        sr.setColor(Color.RED); // Ngòi nổ lập loè
        if (MathUtils.randomBoolean()) sr.circle(h.x + h.width/2f, h.y + h.height + 45f, 8f);
        sr.end();
    }
    @Override protected void drawSpecificGameSprites(SpriteBatch b) {}
}

// =========================================================================
// GAME 7: NÉ THIÊN THẠCH LÀM MƯA (BULLET DODGE)
// =========================================================================
class GameBulletDodge extends MiniGame {
    private ArrayList<Vector2> bullets = new ArrayList<>();
    private ArrayList<Vector2> velocities = new ArrayList<>();
    private float spawnTimer = 0f;
    public GameBulletDodge(Player[] p) { super(p); this.gameTitle = "GAME 7: NÉ THIÊN THẠCH"; }
    @Override protected void onGameStart() { bullets.clear(); velocities.clear(); }
    @Override protected void updateSpecificGame(float dt, InputController in) {
        spawnTimer += dt;
        if (spawnTimer >= 0.4f) { // Cứ 0.4 giây đẻ 1 viên đạn bắn từ trên xuống
            bullets.add(new Vector2(MathUtils.random(0, GameConfig.SCREEN_WIDTH), GameConfig.SCREEN_HEIGHT));
            velocities.add(new Vector2(MathUtils.random(-100f, 100f), -450f));
            spawnTimer = 0f;
        }
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Vector2 b = bullets.get(i); Vector2 v = velocities.get(i);
            b.x += v.x * dt; b.y += v.y * dt;
            if (b.y < -20) { bullets.remove(i); velocities.remove(i); continue; }
            
            Rectangle bRect = new Rectangle(b.x, b.y, 16, 16);
            for (Player p : players) {
                if (p.isAlive && p.getBounds().overlaps(bRect)) {
                    p.takeDamage(20); // Trúng đạn mất 20 máu
                    p.addScore(-10);
                    bullets.remove(i); velocities.remove(i);
                    break;
                }
            }
        }
        for(Player p : players) if(p.isAlive) p.addScore(1); // Thưởng điểm sinh tồn
    }
    @Override protected void drawSpecificGameGeometry(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.ORANGE);
        for (Vector2 b : bullets) sr.circle(b.x, b.y, 10f);
        sr.end();
    }
    @Override protected void drawSpecificGameSprites(SpriteBatch b) {}
}

// =========================================================================
// GAME 8: BẮT GÀ XỔNG CHUỒNG (CHICKEN RUN)
// =========================================================================
class GameChickenRun extends MiniGame {
    private Vector2 chickenPos = new Vector2();
    private Vector2 chickenDir = new Vector2();
    private float changeDirTimer = 0f;
    public GameChickenRun(Player[] p) { super(p); this.gameTitle = "GAME 8: BẮT GÀ XỔNG CHUỒNG"; }
    @Override protected void onGameStart() { resetChicken(); }
    private void resetChicken() {
        chickenPos.set(GameConfig.SCREEN_WIDTH/2f, GameConfig.SCREEN_HEIGHT/2f);
        chickenDir.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
    }
    @Override protected void updateSpecificGame(float dt, InputController in) {
        changeDirTimer += dt;
        if (changeDirTimer >= 0.8f) { // Gà liên tục đổi hướng chạy loạn
            chickenDir.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
            changeDirTimer = 0f;
        }
        chickenPos.x += chickenDir.x * 350f * dt;
        chickenPos.y += chickenDir.y * 350f * dt;
        
        // Giữ gà trong màn hình
        if(chickenPos.x<50||chickenPos.x>GameConfig.SCREEN_WIDTH-50) chickenDir.x *= -1;
        if(chickenPos.y<50||chickenPos.y>GameConfig.SCREEN_HEIGHT-50) chickenDir.y *= -1;

        Rectangle chkRect = new Rectangle(chickenPos.x, chickenPos.y, 40, 40);
        for (Player p : players) {
            if (p.isAlive && p.getBounds().overlaps(chkRect)) {
                p.addScore(30); // Đập trúng gà được cộng hẳn 30 điểm
                resetChicken();
                break;
            }
        }
    }
    @Override protected void drawSpecificGameGeometry(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.SALMON); // Con gà hình khối màu hồng cam bụi bặm
        sr.rect(chickenPos.x, chickenPos.y, 40f, 40f);
        sr.setColor(Color.YELLOW); // Cái mỏ gà
        sr.triangle(chickenPos.x+40, chickenPos.y+20, chickenPos.x+40, chickenPos.y+30, chickenPos.x+50, chickenPos.y+25);
        sr.end();
    }
    @Override protected void drawSpecificGameSprites(SpriteBatch b) {}
}

// =========================================================================
// GAME 9: ĐẤY NHAU RA VÒNG VỰC (RING OUT)
// =========================================================================
class GameRingOut extends MiniGame {
    private Rectangle arena = new Rectangle(300, 150, 1320, 780);
    public GameRingOut(Player[] p) { super(p); this.gameTitle = "GAME 9: ĐẨY NHAU RA VỰC THẲM"; }
    @Override protected void onGameStart() {}
    @Override protected void updateSpecificGame(float dt, InputController in) {
        // Xử lý húc văng nhau
        for (Player p1 : players) {
            for (Player p2 : players) {
                if (p1.id != p2.id && p1.isAlive && p2.isAlive && p1.getBounds().overlaps(p2.getBounds())) {
                    float forceX = (p2.x - p1.x) > 0 ? 15f : -15f;
                    float forceY = (p2.y - p1.y) > 0 ? 15f : -15f;
                    p2.move(forceX, forceY, 1f); // Húc văng đối thủ ra xa
                }
            }
        }
        // Kiểm tra ai lọt sàn đấu sẽ rơi xuống vực và bị loại ngay lập tức
        for (Player p : players) {
            if (p.isAlive && !arena.contains(p.x, p.y)) {
                p.isAlive = false;
                p.addScore(-50);
            }
            if (p.isAlive) p.addScore(2);
        }
    }
    @Override protected void drawSpecificGameGeometry(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.DARK_GRAY); // Sàn đấu an toàn màu xám
        sr.rect(arena.x, arena.y, arena.width, arena.height);
        sr.end();
    }
    @Override protected void drawSpecificGameSprites(SpriteBatch b) {}
}

// =========================================================================
// GAME 10: TRẬN CHIẾN XE TĂNG MINI (TANK BATTLE)
// =========================================================================
class GameTankBattle extends MiniGame {
    private ArrayList<Rectangle> shells = new ArrayList<>();
    private ArrayList<Vector2> shellDirs = new ArrayList<>();
    private float shootTimer = 0f;
    public GameTankBattle(Player[] p) { super(p); this.gameTitle = "GAME 10: ĐẤU TRƯỜNG XE TĂNG TỰ ĐỘNG BẮN"; }
    @Override protected void onGameStart() { shells.clear(); shellDirs.clear(); }
    @Override protected void updateSpecificGame(float dt, InputController in) {
        shootTimer += dt;
        if (shootTimer >= 0.7f) { // Tự động nạp đạn bắn ra theo hướng di chuyển hiện tại
            for (Player p : players) {
                if (!p.isAlive) continue;
                shells.add(new Rectangle(p.x + p.width/2f, p.y + p.height/2f, 12, 12));
                // Nếu đứng yên mặc định bắn sang phải, nếu di chuyển thì bắn theo hướng dịch chuyển
                float dx = in.getMoveX(p.id) == 0 && in.getMoveY(p.id) == 0 ? 1f : in.getMoveX(p.id);
                shellDirs.add(new Vector2(dx, in.getMoveY(p.id)).nor());
            }
            shootTimer = 0f;
        }
        // Di chuyển đạn bắn và tính toán trừ máu sát thương
        for (int i = shells.size() - 1; i >= 0; i--) {
            Rectangle s = shells.get(i); Vector2 d = shellDirs.get(i);
            s.x += d.x * 600f * dt; s.y += d.y * 600f * dt;
            if (s.x < 0 || s.x > GameConfig.SCREEN_WIDTH || s.y < 0 || s.y > GameConfig.SCREEN_HEIGHT) {
                shells.remove(i); shellDirs.remove(i); continue;
            }
            for (Player p : players) {
                if (p.isAlive && p.getBounds().overlaps(s)) {
                    p.takeDamage(25); // Trúng đạn pháo xe tăng trừ 25 HP
                    p.addScore(-15);
                    shells.remove(i); shellDirs.remove(i);
                    break;
                }
            }
        }
    }
    @Override protected void drawSpecificGameGeometry(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.LIGHT_GRAY);
        for (Rectangle s : shells) sr.rect(s.x, s.y, s.width, s.height);
        sr.end();
    }
    @Override protected void drawSpecificGameSprites(SpriteBatch b) {}
}

// =========================================================================
// GAME 11: CƯỚP CỜ PHE ĐỐI LẬP (CAPTURE FLAG)
// =========================================================================
class GameCaptureFlag extends MiniGame {
    private Vector2 flagPos = new Vector2(GameConfig.SCREEN_WIDTH/2f, GameConfig.SCREEN_HEIGHT/2f);
    private int flagHolderId = -1;
    public GameCaptureFlag(Player[] p) { super(p); this.gameTitle = "GAME 11: TRANH GIÀNH CỜ TRUNG TÂM"; }
    @Override protected void onGameStart() { flagHolderId = -1; flagPos.set(GameConfig.SCREEN_WIDTH/2f, GameConfig.SCREEN_HEIGHT/2f); }
    @Override protected void updateSpecificGame(float dt, InputController in) {
        if (flagHolderId == -1) {
            Rectangle flagRect = new Rectangle(flagPos.x, flagPos.y, 30, 30);
            for(Player p : players) {
                if (p.isAlive && p.getBounds().overlaps(flagRect)) { flagHolderId = p.id; break; }
            }
        } else {
            Player h = players[flagHolderId];
            flagPos.set(h.x, h.y + h.height);
            h.addScore(2); // Cầm cờ chạy được tăng điểm liên tục
            
            // Nếu bị đối thủ húc trúng, rớt cờ ra ngay tại vị trí đó
            for(Player attacker : players) {
                if (attacker.isAlive && attacker.id != flagHolderId && attacker.getBounds().overlaps(h.getBounds())) {
                    flagHolderId = -1;
                    break;
                }
            }
        }
    }
    @Override protected void drawSpecificGameGeometry(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.MAGENTA); // Cờ màu hồng đậm rực rỡ
        sr.rect(flagPos.x, flagPos.y, 25f, 40f);
        sr.setColor(Color.WHITE);
        sr.rect(flagPos.x, flagPos.y, 4f, 55f); // Cột cờ trắng
        sr.end();
    }
    @Override protected void drawSpecificGameSprites(SpriteBatch b) {}
}

// =========================================================================
// GAME 12: CHIẾM GIỮ CỨ ĐIỂM HOÀNG GIA (TERRITORY HOLD)
// =========================================================================
class GameTerritoryHold extends MiniGame {
    private Vector2 zone = new Vector2(GameConfig.SCREEN_WIDTH/2f, GameConfig.SCREEN_HEIGHT/2f);
    private float zoneRadius = 160f;
    public GameTerritoryHold(Player[] p) { super(p); this.gameTitle = "GAME 12: ĐỨNG TRONG VÒNG CHIẾM CỨ ĐIỂM"; }
    @Override protected void onGameStart() {}
    @Override protected void updateSpecificGame(float dt, InputController in) {
        for (Player p : players) {
            if (!p.isAlive) continue;
            float dist = zone.dst(p.x + p.width/2f, p.y + p.height/2f);
            if (dist < zoneRadius) {
                p.addScore(4); // Cứ đứng lỳ trong vòng tròn vàng ở tâm sẽ được nhảy điểm cực lẹ
            }
        }
    }
    @Override protected void drawSpecificGameGeometry(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(1f, 0.84f, 0f, 0.15f); // Vòng hào quang vàng nhạt nhấp nháy
        sr.circle(zone.x, zone.y, zoneRadius);
        sr.end();
    }
    @Override protected void drawSpecificGameSprites(SpriteBatch b) {}
}

// =========================================================================
// GAME 13: KÉO CO TỐC ĐỘ DI CHUYỂN (TUG OF WAR)
// =========================================================================
class GameTugOfWar extends MiniGame {
    private float ropeX = GameConfig.SCREEN_WIDTH / 2f;
    public GameTugOfWar(Player[] p) { super(p); this.gameTitle = "GAME 13: KÉO CO ĐỒNG ĐỘI (KÉO JOYSTICK)"; }
    @Override protected void onGameStart() { ropeX = GameConfig.SCREEN_WIDTH / 2f; }
    @Override protected void updateSpecificGame(float dt, InputController in) {
        // Đội bên trái (P1, P3) cố ghì Joystick sang trái, Đội bên phải (P2, P4) cố ghì sang phải
        for (Player p : players) {
            if (!p.isAlive) continue;
            float moveInputX = in.getMoveX(p.id);
            ropeX += moveInputX * 120f * dt; // Lực kéo tác động dịch chuyển nút thắt dây thừng
        }
        // Giới hạn biên tính điểm danh dự
        if (ropeX < 200) ropeX = 200;
        if (ropeX > GameConfig.SCREEN_WIDTH - 200) ropeX = GameConfig.SCREEN_WIDTH - 200;
        
        // Thưởng điểm cuối trận dựa trên vị trí sợi dây nghiêng về bên nào
        if (ropeX < GameConfig.SCREEN_WIDTH / 2f) {
            players[0].addScore(1); if(players.length>2) players[2].addScore(1);
        } else {
            players[1].addScore(1); if(players.length>3) players[3].addScore(1);
        }
    }
    @Override protected void drawSpecificGameGeometry(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.BROWN); // Sợi dây thừng kéo co bắc ngang
        sr.rect(100, GameConfig.SCREEN_HEIGHT/2f - 10, GameConfig.SCREEN_WIDTH - 200, 20);
        sr.setColor(Color.RED); // Nút thắt ruy-băng đỏ ở giữa dây đánh dấu
        sr.rect(ropeX - 15, GameConfig.SCREEN_HEIGHT/2f - 25, 30, 50);
        sr.end();
    }
    @Override protected void drawSpecificGameSprites(SpriteBatch b) {}
}

// =========================================================================
// GAME 14: VƯỢT CẦU GẠCH TỬ THẦN (BRIDGE CROSS)
// =========================================================================
class GameBridgeCross extends MiniGame {
    private ArrayList<Rectangle> traps = new ArrayList<>();
    public GameBridgeCross(Player[] p) { super(p); this.gameTitle = "GAME 14: CHẠY QUA CẦU NÉ BẪY SẬP GẠCH"; }
    @Override protected void onGameStart() {
        traps.clear();
        // Tạo 4 dải chướng ngại vật gạch sập di động quét dọc màn hình
        for (int i = 0; i < 4; i++) {
            traps.add(new Rectangle(400 + i * 300, 0, 80, 250));
        }
    }
    @Override protected void updateSpecificGame(float dt, InputController in) {
        for (Rectangle r : traps) {
            r.y += 400f * dt; // Bẫy chạy từ dưới lên trên liên tục
            if (r.y > GameConfig.SCREEN_HEIGHT) r.y = -r.height;
            
            for (Player p : players) {
                if (p.isAlive && p.getBounds().overlaps(r)) {
                    p.x = 50; // Bị rơi sập gạch đày lùi về vạch xuất phát trái màn hình
                    p.addScore(-10);
                }
            }
        }
        for (Player p : players) {
            if (p.isAlive && p.x > GameConfig.SCREEN_WIDTH - 150) {
                p.addScore(50);