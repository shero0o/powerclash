import Phaser from 'phaser';

export default class SelectionScene extends Phaser.Scene {
    constructor() {
        super({ key: 'SelectionScene' });
        this.selectedWeapon = 'RIFLE_BULLET';
        this.selectedLevel  = 'level1';
    }

    create() {
        const { width } = this.scale;

        // Weapon‐Optionen: Label + korrespondierender ProjectileType
        const weapons = [
            { label: 'Rifle', value: 'RIFLE_BULLET' },
            { label: 'Sniper', value: 'SNIPER' },
            { label: 'Shotgun', value: 'SHOTGUN_PELLET' },
            { label: 'Mine', value: 'MINE' }
        ];

        // Level‐Optionen (wie gehabt)
        const levels = ['level1', 'level2', 'level3'];

        this.weaponTexts = [];
        this.levelTexts  = [];

        // --- Weapon Auswahl ---
        this.add.text(50, 30, 'Choose Weapon:', { fontSize: '24px', fill: '#ffffff' });
        weapons.forEach((w, i) => {
            const txt = this.add.text(50, 70 + i * 30, w.label, {
                fontSize: '20px',
                fill: w.value === this.selectedWeapon ? '#ffff00' : '#00ff00'
            }).setInteractive();

            txt.on('pointerdown', () => {
                this.selectedWeapon = w.value;
                this.updateWeaponHighlight();
            });

            this.weaponTexts.push({ txt, value: w.value });
        });

        // --- Level Auswahl ---
        this.add.text(width / 2, 30, 'Choose Map:', { fontSize: '24px', fill: '#ffffff' })
            .setOrigin(0.5);
        levels.forEach((lvl, i) => {
            const txt = this.add.text(width / 2, 70 + i * 30, lvl, {
                fontSize: '20px',
                fill: lvl === this.selectedLevel ? '#ffff00' : '#00ffff'
            }).setOrigin(0.5)
                .setInteractive();

            txt.on('pointerdown', () => {
                this.selectedLevel = lvl;
                this.updateLevelHighlight();
            });

            this.levelTexts.push({ txt, value: lvl });
        });

        // --- Play Button ---
        this.add.text(width / 2, 250, 'PLAY', { fontSize: '32px', fill: '#ffffff' })
            .setOrigin(0.5)
            .setInteractive()
            .on('pointerdown', () => {
                // PlayerId aus LocalStorage oder neu generieren
                const playerId = localStorage.getItem('playerId') || crypto.randomUUID();
                localStorage.setItem('playerId', playerId);

                console.log("🚀 Sending joinRoom with", {
                    playerId,
                    levelId: this.selectedLevel,
                    chosenWeapon: this.selectedWeapon
                });

                // Join-Payload mit chosenWeapon
                this.socket.emit('joinRoom', {

                    playerId,
                    brawlerId: null,              // Server wählt Default-Brawler
                    levelId: this.selectedLevel,
                    chosenWeapon: this.selectedWeapon
                }, (response) => {
                    console.log("📥 Server response from joinRoom:", response);
                    // Raum‐Daten im Registry speichern
                    this.registry.set('roomId',   response.roomId);
                    this.registry.set('playerId', playerId);
                    this.registry.set('levelId',  this.selectedLevel);
                    this.registry.set('weapon',   this.selectedWeapon);
                    this.scene.start('WaitingScene');
                });
            });

        // erste Hervorhebungen zeichnen
        this.updateWeaponHighlight();
        this.updateLevelHighlight();
    }

    updateWeaponHighlight() {
        this.weaponTexts.forEach(({ txt, value }) => {
            txt.setColor(value === this.selectedWeapon ? '#ffff00' : '#00ff00');
        });
    }

    updateLevelHighlight() {
        this.levelTexts.forEach(({ txt, value }) => {
            txt.setColor(value === this.selectedLevel ? '#ffff00' : '#00ffff');
        });
    }
}
