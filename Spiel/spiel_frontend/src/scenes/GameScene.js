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

        // === set your fixed spawn points here ===
        this.spawnPoints = [
            { x: 100, y: 100 },
            { x: 700, y: 100 },
            { x: 100, y: 500 },
            { x: 700, y: 500 }
        ];

        this.keys        = null;
        this.initialZoom = 1;
    }

    preload() {
        this.load.image('player',   '/assets/PNG/Hitman_1/hitman1_gun.png');
        this.load.tilemapTiledJSON('map1',   '/assets/map.json');
        this.load.image('tileset1', '/assets/Tilesheet/tilesheet_complete_2X.png');
    }

    create() {
        // --- map & layers ---
        const map   = this.make.tilemap({ key: 'map1' });
        const tiles = map.addTilesetImage('tilesheet_complete_2X', 'tileset1');
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

    // src/scenes/GameScene.js (inside your GameScene class)

    update() {
        if (!this.latestState) return;

        const ptr   = this.input.activePointer;
        const cam   = this.cameras.main;

        this.latestState.players.forEach(p => {
            // 1) get or create the sprite
            let spr = this.playerSprites[p.playerId];
            if (!spr) {
                spr = this.physics.add.sprite(p.position.x, p.position.y, 'player')
                    .setOrigin(0.5);
                this.playerSprites[p.playerId] = spr;
                if (p.playerId === this.playerId) {
                    cam.startFollow(spr, false, 1, 1);
                    cam.setZoom(this.initialZoom);
                    this.physics.add.collider(spr, this.grassLayer);
                }
            }

            // 2) **Always** snap to the server's authoritative pos+angle:
            spr.setPosition(p.position.x, p.position.y);
            spr.setRotation(p.position.angle);

            // 3) If this is *you*, compute your next input & tell the server:
            if (p.playerId === this.playerId) {
                // aim at pointer
                const worldPt = cam.getWorldPoint(ptr.x, ptr.y);
                const angle   = Phaser.Math.Angle.Between(
                    p.position.x, p.position.y,
                    worldPt.x,    worldPt.y
                );
                // compute WASD direction
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

        // 4) cleanup any departed players
        Object.keys(this.playerSprites).forEach(id => {
            if (!this.latestState.players.find(p => p.playerId === id)) {
                this.playerSprites[id].destroy();
                delete this.playerSprites[id];
            }
        });
    }

}
