import Phaser from 'phaser';

export default class SelectionScene extends Phaser.Scene {
    constructor() {
        super({ key: 'SelectionScene' });
        this.selectedBrawler = 'sniper';
        this.selectedLevel = 'level1';
    }

    create() {
        const { width } = this.scale;

        const brawlers = ['sniper', 'shotgun', 'ar', 'mine'];
        const levels   = ['level1', 'level2', 'level3'];

        this.brawlerTexts = [];
        this.levelTexts   = [];

        // --- Brawler Auswahl ---
        this.add.text(50, 30, 'Choose Brawler:', { fontSize: '24px', fill: '#ffffff' });
        brawlers.forEach((b, i) => {
            const txt = this.add.text(50, 70 + i * 30, b, {
                fontSize: '20px',
                fill: b === this.selectedBrawler ? '#ffff00' : '#00ff00' // Gelb bei Auswahl
            }).setInteractive();

            txt.on('pointerdown', () => {
                this.selectedBrawler = b;
                this.updateBrawlerHighlight();
            });

            this.brawlerTexts.push(txt);
        });

        // --- Level Auswahl ---
        this.add.text(width / 2, 30, 'Choose Map:', { fontSize: '24px', fill: '#ffffff' }).setOrigin(0.5);
        levels.forEach((lvl, i) => {
            const txt = this.add.text(width / 2, 70 + i * 30, lvl, {
                fontSize: '20px',
                fill: lvl === this.selectedLevel ? '#ffff00' : '#00ffff'
            }).setOrigin(0.5).setInteractive();

            txt.on('pointerdown', () => {
                this.selectedLevel = lvl;
                this.updateLevelHighlight();
            });

            this.levelTexts.push(txt);
        });

        // --- Play Button ---
        this.add.text(width / 2, 250, 'PLAY', { fontSize: '32px', fill: '#ffffff' })
            .setOrigin(0.5)
            .setInteractive()
            .on('pointerdown', () => {
                const playerId = localStorage.getItem('playerId') || crypto.randomUUID();
                localStorage.setItem('playerId', playerId);

                this.socket.emit('joinRoom', {
                    playerId,
                    brawlerId: this.selectedBrawler,
                    levelId: this.selectedLevel
                }, (response) => {
                    this.registry.set('roomId', response.roomId);
                    this.registry.set('playerId', playerId);
                    this.registry.set('levelId', this.selectedLevel);
                    this.scene.start('WaitingScene');
                });
            });
    }

    updateBrawlerHighlight() {
        this.brawlerTexts.forEach(txt => {
            txt.setColor(txt.text === this.selectedBrawler ? '#ffff00' : '#00ff00');
        });
    }

    updateLevelHighlight() {
        this.levelTexts.forEach(txt => {
            txt.setColor(txt.text === this.selectedLevel ? '#ffff00' : '#00ffff');
        });
    }
}
