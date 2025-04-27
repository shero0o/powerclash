import Phaser from 'phaser';

export default class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' });
    }

    init(data) {
        this.roomId = data.roomId;
        this.playerId = data.playerId;
        this.latestState = null;
        this.playerSprites = {};
        this.projectileSprites = {};
    }

    create() {
        console.log('[GameScene] Registering stateUpdate listener');
        this.socket.on('stateUpdate', (state) => {
            console.log('🎮 stateUpdate vom Server:', state);
            this.latestState = state;
        });

        this.input.on('pointerdown', (pointer) => {
            const payload = {
                roomId: this.roomId,
                playerId: this.playerId,
                x: pointer.worldX,
                y: pointer.worldY
            };
            if (pointer.rightButtonDown()) {
                this.socket.emit('attack', payload);
            } else {
                this.socket.emit('move', payload);
            }
        });
    }

    update() {
        if (!this.latestState) return;
        // Hier dein bestehendes Render-Code …
        if (!this.latestState) return;
        // hier wie gehabt Sprites updaten …
        this.latestState.players.forEach(p => {
            let spr = this.playerSprites[p.playerId];
            if (!spr) {
                spr = this.add.circle(p.pos.x, p.pos.y, 20, 0x00ff00);
                this.playerSprites[p.playerId] = spr;
            }
            spr.setPosition(p.pos.x, p.pos.y);
        });
        this.latestState.projectiles.forEach(pr => {
            let spr = this.projectileSprites[pr.id];
            if (!spr) {
                spr = this.add.circle(pr.pos.x, pr.pos.y, 5, 0xff0000);
                this.projectileSprites[pr.id] = spr;
            }
            spr.setPosition(pr.pos.x, pr.pos.y);
        });
        // entferne nicht mehr vorhandene Sprites …
        Object.keys(this.playerSprites).forEach(id => {
            if (!this.latestState.players.find(p => p.playerId === id)) {
                this.playerSprites[id].destroy();
                delete this.playerSprites[id];
            }
        });
        Object.keys(this.projectileSprites).forEach(id => {
            if (!this.latestState.projectiles.find(pr => pr.id === id)) {
                this.projectileSprites[id].destroy();
                delete this.projectileSprites[id];
            }
        });
    }
}
