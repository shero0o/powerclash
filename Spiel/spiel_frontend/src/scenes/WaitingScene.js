import Phaser from 'phaser';

export default class WaitingScene extends Phaser.Scene {
    constructor() {
        super({ key: 'WaitingScene' });
    }

    preload(){
        this.load.svg("brawlStarsLogo", "/assets/svg/brawl-stars-logo.svg", {width: 600, height: 300});
        this.load.image("loadingScreen1", "/assets/PNG/Loading/Loadingscreen1.png");
    }

    create() {
        const { width, height } = this.scale;


        this.add.image(width/2, height/2, "loadingScreen1").setOrigin(0.5)
        this.add.image(width - 250, height - 600, "brawlStarsLogo").setOrigin(0.5)
        const waitingText = this.add
            .text(width/2 - 450, height/2 + 300, 'WAITING FOR PLAYERS', {
                fontSize: '42px',
                fontWeight: 'bold',
                fontFamily: 'Impact',
                color: '#ffffff',
                stroke: '#000000',         // schwarze Umrandung
                strokeThickness: 4
            })
            .setOrigin(0.5);

        const ellipses = ['', '.', '..', '...'];
        let idx = 0;

// Timer‐Event, das alle 500 ms die Punkt‐Anzahl hochzählt
        this.time.addEvent({
            delay: 500,
            loop: true,
            callback: () => {
                // idx rotiert durch 0,1,2,3
                idx = (idx + 1) % ellipses.length;
                waitingText.setText('WAITING FOR PLAYERS' + ellipses[idx]);
            }
        });

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
