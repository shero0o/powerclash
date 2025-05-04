// src/scenes/GameScene.js
import Phaser from 'phaser';

export default class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' });
    }

    init(data) {
        this.roomId        = data.roomId;
        this.playerId      = data.playerId;
        this.latestState   = null;
        this.playerSprites = {};

        // fixed spawn points (if you ever need them)
        this.spawnPoints = [
            { x: 100, y: 100 },
            { x: 700, y: 100 },
            { x: 100, y: 500 },
            { x: 700, y: 500 }
        ];

        this.keys        = null;
        this.initialZoom = 1;

        // your game's maximum health
        this.maxHealth = 100;
    }

    preload() {
        this.load.image('player',   '/assets/PNG/Hitman_1/hitman1_gun.png');
        this.load.tilemapTiledJSON('map1',   '/assets/map.json');
        this.load.image('tileset1', '/assets/Tilesheet/spritesheet_tiles.png');
    }

    create() {
        // --- map & layers ---
        const map   = this.make.tilemap({ key: 'map1' });
        const tiles = map.addTilesetImage('tilesheet_tiles', 'tileset1');
        this.groundLayer = map.createLayer('ground', tiles, 0, 0);
        this.grassLayer  = map.createLayer('grass',  tiles, 0, 0);

        // world & camera bounds
        const W = map.widthInPixels, H = map.heightInPixels;
        this.physics.world.setBounds(0, 0, W, H);
        const cam = this.cameras.main;
        cam.setBounds(0, 0, W, H);
        cam.setZoom(Math.min(cam.width / W, cam.height / H));
        cam.centerOn(W/2, H/2);

        // --- input & socket ---
        this.keys = this.input.keyboard.addKeys({
            up: 'W', down: 'S', left: 'A', right: 'D'
        });

        this.socket.on('stateUpdate', state => {
            this.latestState = state;
        });
    }

    update() {
        if (!this.latestState) return;

        const ptr = this.input.activePointer;
        const cam = this.cameras.main;

        this.latestState.players.forEach(p => {
            // 1) get or create sprite + healthBar
            let spr = this.playerSprites[p.playerId];
            if (!spr) {
                spr = this.physics.add.sprite(p.position.x, p.position.y, 'player')
                    .setOrigin(0.5);
                spr.healthBar = this.add.graphics();
                this.playerSprites[p.playerId] = spr;

                if (p.playerId === this.playerId) {
                    cam.startFollow(spr, false, 1, 1);
                    cam.setZoom(this.initialZoom);
                    this.physics.add.collider(spr, this.grassLayer);
                }
            }

            // 2) snap to server position + angle
            spr.setPosition(p.position.x, p.position.y);
            spr.setRotation(p.position.angle);

            // 3) update visibility
            spr.setVisible(p.visible);
            spr.healthBar.setVisible(p.visible);

            // 4) redraw health bar (green, 5px higher)
            const barWidth  = 40;
            const barHeight = 6;
            const healthPct = Phaser.Math.Clamp(p.currentHealth / this.maxHealth, 0, 1);

            spr.healthBar.clear();

            // border/background
            spr.healthBar.fillStyle(0x000000);
            spr.healthBar.fillRect(
                p.position.x - barWidth/2 - 1,
                // moved 5px higher: original offset -4 → now -9
                p.position.y - spr.height/2 - barHeight - 9,
                barWidth + 2,
                barHeight + 2
            );

            // green health fill
            spr.healthBar.fillStyle(0x00ff00);
            spr.healthBar.fillRect(
                p.position.x - barWidth/2,
                // moved 5px higher: original offset -3 → now -8
                p.position.y - spr.height/2 - barHeight - 8,
                barWidth * healthPct,
                barHeight
            );

            // 5) your input & move logic
            if (p.playerId === this.playerId) {
                const worldPt = cam.getWorldPoint(ptr.x, ptr.y);
                const angle   = Phaser.Math.Angle.Between(
                    p.position.x, p.position.y,
                    worldPt.x,    worldPt.y
                );
                const dirX = (this.keys.left.isDown  ? -1 : 0)
                    + (this.keys.right.isDown ?  1 : 0);
                const dirY = (this.keys.up.isDown    ? -1 : 0)
                    + (this.keys.down.isDown  ?  1 : 0);

                this.socket.emit('move', {
                    roomId:   this.roomId,
                    playerId: this.playerId,
                    dirX,
                    dirY,
                    angle
                });
            }
        });

        // 6) cleanup departed players & their bars
        Object.keys(this.playerSprites).forEach(id => {
            if (!this.latestState.players.find(p => p.playerId === id)) {
                this.playerSprites[id].healthBar.destroy();
                this.playerSprites[id].destroy();
                delete this.playerSprites[id];
            }
        });
    }
}
