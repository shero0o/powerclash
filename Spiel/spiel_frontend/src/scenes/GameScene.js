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
        if (!this.socket) {
            console.error('Socket connection not available.');
            return;
        }
        console.log('[GameScene] Registering stateUpdate listener');
        this.socket.on('stateUpdate', (state) => {
            console.log('🎮 State update from server:', state);
            this.latestState = state;
            this.updateSprites(state);
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

    updateSprites(state) {
        // Update player and projectile sprites based on the state
        console.log('Updating sprites based on the latest state...');
        // Similar update logic as previously shown...
    }
}
