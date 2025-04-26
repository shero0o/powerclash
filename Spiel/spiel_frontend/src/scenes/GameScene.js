// src/scenes/GameScene.js
import Phaser from 'phaser';

export default class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' });
        this.playerSprites     = {};
        this.projectileSprites = {};
    }

    create() {
        // left click → move; right click → attack
        this.input.on('pointerdown', pointer => {
            if (pointer.rightButtonDown()) {
                this.sendAttack(pointer.worldX, pointer.worldY);
            } else {
                this.sendMove(pointer.worldX, pointer.worldY);
            }
        });
    }

    update() {
        const state = this.getState();
        console.log('Game state in update:', state);
        if (!state) return;

        // render players
        state.players.forEach(p => {
            let s = this.playerSprites[p.playerId];
            if (!s) {
                s = this.add.circle(p.pos.x, p.pos.y, 20, 0x00ff00);
                this.playerSprites[p.playerId] = s;
            }
            s.setPosition(p.pos.x, p.pos.y);
            s.setVisible(p.visible);
        });

        // render projectiles
        state.projectiles.forEach(proj => {
            let b = this.projectileSprites[proj.id];
            if (!b) {
                b = this.add.circle(proj.pos.x, proj.pos.y, 5, 0xff0000);
                this.projectileSprites[proj.id] = b;
            }
            b.setPosition(proj.pos.x, proj.pos.y);
        });

        // cleanup old projectiles
        Object.keys(this.projectileSprites).forEach(id => {
            if (!state.projectiles.find(p => p.id === id)) {
                this.projectileSprites[id].destroy();
                delete this.projectileSprites[id];
            }
        });
    }
}
