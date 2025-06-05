import Phaser from 'phaser';

export default class WaitingScene extends Phaser.Scene {
    constructor() {
        super({ key: 'WaitingScene' });
    }

    preload(){
        this.load.svg("brawlStarsLogo", "/assets/svg/brawl-stars-logo.svg", {width: 600, height: 300});
    }

    create() {
        const { width, height } = this.scale;

        this.add.image(width/2, height/2, "brawlStarsLogo").setOrigin(0.5)
        this.add
            .text(width/2, height/2 + 200, 'Warte auf Mitspieler…', {
                fontSize: '32px',
                color: '#ffffff'
            })
            .setOrigin(0.5);

        console.log('[WaitingScene] Listening for startGame');

        // 1) Listener registrieren
        this.socket.on('startGame', () => {
            this.scene.start('GameScene', {
                roomId: this.registry.get('roomId'),
                playerId: localStorage.getItem('playerId')
            });
        });

        // 2) Sobald WaitingScene aktiv ist -> Bestätigung an Server
        const roomId = this.registry.get('roomId');
        const playerId = localStorage.getItem('playerId');
        console.log('[WaitingScene] Sending waitingReady to server');
        this.socket.emit('waitingReady', { roomId, playerId });
    }
}
