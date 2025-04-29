import Phaser from 'phaser';

export default class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' });
    }

    init(data) {
        this.roomId           = data.roomId;
        this.playerId         = data.playerId;
        this.latestState      = null;
        this.playerSprites    = {};
        this.projectileSprites = {};
        this.keys             = null;
        this.initialZoom      = 1;
    }

    preload() {
        this.load.image('player',   '/assets/PNG/Hitman_1/hitman1_gun.png');
        this.load.tilemapTiledJSON('map1',   '/assets/map.json');
        this.load.image('tileset1', '/assets/Tilesheet/tilesheet_complete_2X.png');
    }

    create() {
        // 1) Tilemap & Layers
        const map = this.make.tilemap({ key: 'map1' });
        const tileset = map.addTilesetImage('tilesheet_complete_2X', 'tileset1');
        this.groundLayer = map.createLayer('ground', tileset, 0, 0);
        this.grassLayer  = map.createLayer('grass',  tileset, 0, 0);
        this.grassLayer.setCollisionByExclusion([-1]);

        // 2) World- und Kamera-Bounds
        const width  = map.widthInPixels;
        const height = map.heightInPixels;
        this.physics.world.setBounds(0, 0, width, height);

        const cam = this.cameras.main;
        cam.setBounds(0, 0, width, height);

        // 3) Zoom so einstellen, dass die gesamte Map in dein 1280×720‐Fenster passt
        const fitZoom = Math.min(cam.width / width, cam.height / height);
        cam.setZoom(fitZoom);

        // 4) Kamera sofort auf die Kartenzentrum setzen
        cam.centerOn(width / 2, height / 2);

        // Netzwerk-Update
        this.socket.on('stateUpdate', state => {
            this.latestState = state;
        });

        // WASD
        this.keys = this.input.keyboard.addKeys({
            up: 'W', down: 'S', left: 'A', right: 'D'
        });
    }

    update() {
        if (!this.latestState) return;

        // A) Bewegung senden
        const me = this.latestState.players.find(p => p.playerId === this.playerId);
        if (me) {
            let x = me.pos?.x ?? me.position?.x ?? me.x ?? 0;
            let y = me.pos?.y ?? me.position?.y ?? me.y ?? 0;
            const step = 10;
            if (this.keys.left.isDown)  x -= step;
            if (this.keys.right.isDown) x += step;
            if (this.keys.up.isDown)    y -= step;
            if (this.keys.down.isDown)  y += step;
            this.socket.emit('move', { roomId: this.roomId, playerId: this.playerId, x, y });
        }

        // B) Render & Kamera-Follow
        const ptr = this.input.activePointer;
        this.latestState.players.forEach(p => {
            const x = p.pos?.x ?? p.position?.x;
            const y = p.pos?.y ?? p.position?.y;
            if (x == null || y == null) return;

            let spr = this.playerSprites[p.playerId];
            if (!spr) {
                // Physics-Sprite, damit Collider & Follow funktionieren
                spr = this.physics.add.sprite(x, y, 'player')
                    .setOrigin(0.5, 0.5);
                this.playerSprites[p.playerId] = spr;

                if (p.playerId === this.playerId) {
                    // Kamera folgt exakt (kein Delay) und zentriert dich immer
                    this.cameras.main.startFollow(spr, false, 1, 1);
                    // setzt den weiterhin initialen (herausgezoomten) Zoom
                    this.cameras.main.setZoom(this.initialZoom);
                    // Kollision gegen Grass-Layer
                    this.physics.add.collider(spr, this.grassLayer);
                }
            }

            // Position und Drehung updaten
            spr.setPosition(x, y);
            if (p.playerId === this.playerId) {
                const angle = Phaser.Math.Angle.Between(x, y, ptr.worldX, ptr.worldY);
                spr.setRotation(angle);
            }
        });

        // C) Aufräumen
        Object.keys(this.playerSprites).forEach(id => {
            if (!this.latestState.players.find(p => p.playerId === id)) {
                this.playerSprites[id].destroy();
                delete this.playerSprites[id];
            }
        });
    }
}
