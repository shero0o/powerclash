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
        this.bulletSprites = {};
        this.keys          = null;
        this.initialZoom   = 1;
        this.maxHealth     = 100;
    }

    preload() {
        this.load.image('player', '/assets/PNG/Hitman_1/hitman1_gun.png');
    }

    create() {
        // 1) Keyboard-Input
        this.keys = this.input.keyboard.addKeys({
            up: 'W', down: 'S', left: 'A', right: 'D'
        });

        // 2) Exit-Button, initially hidden
        this.exitButton = this.add
            .text(16, 16, 'Exit', { fontSize: '18px', fill: '#ff0000' })
            .setScrollFactor(0)
            .setInteractive()
            .setVisible(false);

        this.exitButton.on('pointerdown', () => {
            // optional: inform server you left
            if (this.socket && this.socket.connected) {
                this.socket.emit('leaveRoom', {
                    roomId:   this.roomId,
                    playerId: this.playerId
                });
                this.socket.disconnect();
            }
            this.scene.start('SplashScene');
        });

        // 3) Click → attack-Event senden (only if alive)
        this.input.on('pointerdown', pointer => {
            const me = this.latestState?.players.find(p => p.playerId === this.playerId);
            if (!me || me.currentHealth <= 0) return;

            const dirX = (this.keys.left.isDown  ? -1 : 0)
                + (this.keys.right.isDown ?  1 : 0);
            const dirY = (this.keys.up.isDown    ? -1 : 0)
                + (this.keys.down.isDown  ?  1 : 0);

            const world = this.cameras.main.getWorldPoint(pointer.x, pointer.y);
            const angle = Phaser.Math.Angle.Between(
                me.position.x, me.position.y,
                world.x,       world.y
            );

            this.socket.emit('attack', {
                roomId:   this.roomId,
                playerId: this.playerId,
                dirX,
                dirY,
                angle
            });
        });

        // 4) Server-Updates empfangen
        this.socket.on('stateUpdate', state => {
            this.latestState = state;
        });
    }

    update() {
        if (!this.latestState) return;

        const ptr = this.input.activePointer;
        const cam = this.cameras.main;

        // find our own state this frame
        const me = this.latestState.players.find(p => p.playerId === this.playerId);

        // if dead, show exit button
        if (me && me.currentHealth <= 0) {
            this.exitButton.setVisible(true);
        }

        // --- Spieler zeichnen & bewegen ---
        this.latestState.players.forEach(p => {
            let spr = this.playerSprites[p.playerId];
            if (!spr) {
                spr = this.physics.add.sprite(p.position.x, p.position.y, 'player')
                    .setOrigin(0.5);
                spr.healthBar = this.add.graphics();
                this.playerSprites[p.playerId] = spr;

                if (p.playerId === this.playerId) {
                    cam.startFollow(spr);
                    cam.setZoom(this.initialZoom);
                }
            }

            spr.setPosition(p.position.x, p.position.y);
            spr.setRotation(p.position.angle);
            spr.setVisible(p.visible);
            spr.healthBar.setVisible(p.visible);

            // Health‐Bar
            const barW = 40, barH = 6;
            const pct  = Phaser.Math.Clamp(p.currentHealth / this.maxHealth, 0, 1);
            spr.healthBar.clear();
            spr.healthBar.fillStyle(0x000000);
            spr.healthBar.fillRect(
                p.position.x - barW/2 - 1,
                p.position.y - spr.height/2 - barH - 9,
                barW + 2, barH + 2
            );
            spr.healthBar.fillStyle(0x00ff00);
            spr.healthBar.fillRect(
                p.position.x - barW/2,
                p.position.y - spr.height/2 - barH - 8,
                barW * pct, barH
            );

            // WASD-Bewegung senden (only if me and alive)
            if (p.playerId === this.playerId && p.currentHealth > 0) {
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

        // --- Bullets aus events zeichnen ---
        const seenBullets = new Set();
        (this.latestState.events || []).forEach(evt => {
            if (evt.type === 'ATTACK') {
                const b = evt.data; // { bulletId, x, y, angle }
                seenBullets.add(b.bulletId);

                let circ = this.bulletSprites[b.bulletId];
                if (!circ) {
                    circ = this.add.circle(b.x, b.y, 4, 0xFFFF00)
                        .setDepth(1);
                    this.bulletSprites[b.bulletId] = circ;
                }
                circ.setPosition(b.x, b.y);
            }
        });

        // Remove bullets no longer present
        Object.keys(this.bulletSprites).forEach(id => {
            if (!seenBullets.has(id)) {
                this.bulletSprites[id].destroy();
                delete this.bulletSprites[id];
            }
        });

        // --- Cleanup für Spieler, die gegangen sind ---
        Object.keys(this.playerSprites).forEach(id => {
            if (!this.latestState.players.find(p => p.playerId === id)) {
                this.playerSprites[id].healthBar.destroy();
                this.playerSprites[id].destroy();
                delete this.playerSprites[id];
            }
        });
    }
}
