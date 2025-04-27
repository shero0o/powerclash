import Phaser from 'phaser';

export default class WaitingScene extends Phaser.Scene {
    constructor() {
        super({ key: 'WaitingScene' });
    }

    create() {
        const { width, height } = this.scale;
        this.add
            .text(width/2, height/2, 'Warte auf Mitspieler…', {
                fontSize: '32px',
                color: '#ffffff'
            })
            .setOrigin(0.5);

        console.log('[WaitingScene] Listening for startGame');
        this.socket.on('startGame', () => {
            console.log('[WaitingScene] Received startGame!');
            this.scene.start('PreloadScene', {
                roomId: this.registry.get('roomId'),
                playerId: localStorage.getItem('playerId')
            });
        });
    }
}
