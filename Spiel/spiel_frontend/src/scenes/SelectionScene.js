import Phaser from 'phaser';

export default class SelectionScene extends Phaser.Scene {
    constructor() {
        super({ key: 'SelectionScene' });
        this.selectedWeapon = 'RIFLE_BULLET';
        this.selectedLevel  = 'level1';
        this.selectedBrawler = 'sniper';
        this.playerName = '';
    }

    create() {
        const { width } = this.scale;

        this.add.text(width / 2, 10, 'Enter Name:', { fontSize: '20px', fill: '#ffffff' }).setOrigin(0.5);
        const nameInput = document.createElement('input');
        nameInput.type = 'text';
        nameInput.maxLength = 12;
        nameInput.placeholder = 'Your name';
        nameInput.style.position = 'absolute';
        nameInput.style.top = '50px';
        nameInput.style.left = '50%';
        nameInput.style.transform = 'translateX(-50%)';
        nameInput.style.zIndex = '1000';
        document.body.appendChild(nameInput);

        // Weapon‐Optionen: Label + korrespondierender ProjectileType
        const weapons = [
            { label: 'Rifle', value: 'RIFLE_BULLET' },
            { label: 'Sniper', value: 'SNIPER' },
            { label: 'Shotgun', value: 'SHOTGUN_PELLET' },
            { label: 'Mine', value: 'MINE' }
        ];

        // Level‐Optionen (wie gehabt)
        const levels = ['level1', 'level2', 'level3'];

        const brawlers = [
            { label: 'Sniper', value: 'sniper' },
            { label: 'Tank', value: 'tank' },
            { label: 'Mage', value: 'mage' },
            { label: 'Healer', value: 'healer' }
        ];

        this.weaponTexts = [];
        this.levelTexts  = [];
        this.brawlerTexts = [];

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

        // Brawler Auswahl
        this.add.text(width - 300, 30, 'Choose Brawler:', { fontSize: '24px', fill: '#ffffff' });
        brawlers.forEach((b, i) => {
            const txt = this.add.text(width - 300, 70 + i * 30, b.label, {
                fontSize: '20px',
                fill: b.value === this.selectedBrawler ? '#ffff00' : '#ff00ff'
            }).setInteractive();

            txt.on('pointerdown', () => {
                this.selectedBrawler = b.value;
                this.updateBrawlerHighlight();
            });

            this.brawlerTexts.push({ txt, value: b.value });
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
                    chosenWeapon: this.selectedWeapon,
                    brawlerId: this.selectedBrawler
                });

                const enteredName = nameInput.value.trim() || 'Player';
                this.playerName = enteredName;
                localStorage.setItem('playerName', enteredName);


                document.body.removeChild(nameInput);

                // Join-Payload mit chosenWeapon
                this.socket.emit('joinRoom', {

                    playerId,
                    brawlerId: this.selectedBrawler,              // Server wählt Default-Brawler
                    levelId: this.selectedLevel,
                    chosenWeapon: this.selectedWeapon
                }, (response) => {
                    console.log("📥 Server response from joinRoom:", response);
                    // Raum‐Daten im Registry speichern
                    this.registry.set('roomId',   response.roomId);
                    this.registry.set('playerId', playerId);
                    this.registry.set('levelId',  this.selectedLevel);
                    this.registry.set('weapon',   this.selectedWeapon);
                    this.registry.set('brawler',  this.selectedBrawler);
                    this.registry.set('playerName', enteredName);
                    this.scene.start('WaitingScene');
                });
            });

        // erste Hervorhebungen zeichnen
        this.updateWeaponHighlight();
        this.updateLevelHighlight();
        this.updateBrawlerHighlight();
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

    updateBrawlerHighlight() {
        this.brawlerTexts.forEach(({ txt, value }) => {
            txt.setColor(value === this.selectedBrawler ? '#ffff00' : '#ff00ff');
        });
    }
}
