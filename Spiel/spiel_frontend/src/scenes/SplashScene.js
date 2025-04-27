// src/scenes/SplashScene.js
import Phaser from 'phaser';
import playButtonImage from '../assets/play_button.png';

export default class SplashScene extends Phaser.Scene {
    constructor() {
        super({ key: 'SplashScene' });
    }

    preload() {
        this.load.image('playButton', playButtonImage);
    }

    create() {
        const { width, height } = this.scale;
        const btn = this.add.image(width/2, height/2, 'playButton')
            .setInteractive()
            .setScale(0.5);

        btn.on('pointerdown', () => {
            // <-- this.socket ist jetzt vorhanden, weil wir es später injecten
            const playerId = localStorage.getItem('playerId');
            this.socket.emit('joinRoom', { playerId }, (response) => {
                this.registry.set('roomId', response.roomId);
                this.scene.start('WaitingScene');
            });
        });
    }
}
