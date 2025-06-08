import Phaser from 'phaser';

export default class DressingRoomScene extends Phaser.Scene {
    constructor() {
        super({ key: 'DressingRoomScene' });
        // Default selections
        this.selectedWeapon = 'RIFLE_BULLET';
        this.selectedLevel = 'level1';
        this.selectedGadget = 'DAMAGE_BOOST';
        this.selectedBrawler = 'sniper';
    }

    init(data) {
        // Load current coin count from registry or passed data
        this.coins = data.coins !== undefined ? data.coins : this.registry.get('coins') || 0;
    }

    create() {
        const { width, height } = this.scale;

        // Display coins at top-right
        this.coinIcon = this.add.image(width - 100, 50, 'coin').setScale(0.5);
        this.coinText = this.add.text(width - 70, 40, this.coins.toString(), {
            fontSize: '24px',
            fill: '#ffff00'
        });

        // Back button to Main Menu
        this.backButton = this.add.text(20, height - 40, '← Hauptmenü', {
            fontSize: '24px',
            fill: '#ff4444'
        }).setInteractive({ useHandCursor: true });
        this.backButton.on('pointerdown', () => {
            this.scene.start('MainMenuScene');
        });

        // Option arrays
        const weapons = [
            { label: 'Rifle', value: 'RIFLE_BULLET' },
            { label: 'Sniper', value: 'SNIPER' },
            { label: 'Shotgun', value: 'SHOTGUN_PELLET' },
            { label: 'Mine', value: 'MINE' }
        ];
        const gadgets = [
            { label: 'Damage Boost', value: 'DAMAGE_BOOST' },
            { label: 'HP Boost', value: 'HP_BOOST' },
            { label: 'Speed Boost', value: 'SPEED_BOOST' }
        ];
        const levels = ['level1', 'level2', 'level3'];
        const brawlers = [
            { label: 'Sniper', value: 'sniper' },
            { label: 'Tank', value: 'tank' },
            { label: 'Mage', value: 'mage' },
            { label: 'Healer', value: 'healer' }
        ];

        // Create UI groups
        this.weaponTexts = [];
        this.gadgetTexts = [];
        this.levelTexts = [];
        this.brawlerTexts = [];

        // Weapon selection
        this.add.text(50, 30, 'Waffe wählen:', { fontSize: '20px', fill: '#ffffff' });
        weapons.forEach((w, i) => {
            const txt = this.add.text(50, 60 + i * 30, w.label, {
                fontSize: '18px',
                fill: w.value === this.selectedWeapon ? '#ffff00' : '#00ff00'
            }).setInteractive({ useHandCursor: true });
            txt.on('pointerdown', () => {
                this.selectedWeapon = w.value;
                this.updateWeaponHighlight();
                this.registry.set('selectedWeapon', w.value);
            });
            this.weaponTexts.push({ txt, value: w.value });
        });

        // Gadget selection
        this.add.text(50, 200, 'Gadget wählen:', { fontSize: '20px', fill: '#ffffff' });
        gadgets.forEach((g, i) => {
            const txt = this.add.text(50, 230 + i * 30, g.label, {
                fontSize: '18px',
                fill: g.value === this.selectedGadget ? '#ffff00' : '#ff0000'
            }).setInteractive({ useHandCursor: true });
            txt.on('pointerdown', () => {
                this.selectedGadget = g.value;
                this.updateGadgetHighlight();
                this.registry.set('selectedGadget', g.value);
            });
            this.gadgetTexts.push({ txt, value: g.value });
        });

        // Level selection
        this.add.text(width / 2, 30, 'Map wählen:', { fontSize: '20px', fill: '#ffffff' }).setOrigin(0.5);
        levels.forEach((lvl, i) => {
            const txt = this.add.text(width / 2, 60 + i * 30, lvl, {
                fontSize: '18px',
                fill: lvl === this.selectedLevel ? '#ffff00' : '#00ffff'
            }).setOrigin(0.5).setInteractive({ useHandCursor: true });
            txt.on('pointerdown', () => {
                this.selectedLevel = lvl;
                this.updateLevelHighlight();
                this.registry.set('selectedLevel', lvl);
            });
            this.levelTexts.push({ txt, value: lvl });
        });

        // Brawler selection
        this.add.text(width - 300, 30, 'Brawler wählen:', { fontSize: '20px', fill: '#ffffff' });
        brawlers.forEach((b, i) => {
            const txt = this.add.text(width - 300, 60 + i * 30, b.label, {
                fontSize: '18px',
                fill: b.value === this.selectedBrawler ? '#ffff00' : '#ff00ff'
            }).setInteractive({ useHandCursor: true });
            txt.on('pointerdown', () => {
                this.selectedBrawler = b.value;
                this.updateBrawlerHighlight();
                this.registry.set('selectedBrawler', b.value);
            });
            this.brawlerTexts.push({ txt, value: b.value });
        });

        // Update initial highlights
        this.updateWeaponHighlight();
        this.updateGadgetHighlight();
        this.updateLevelHighlight();
        this.updateBrawlerHighlight();
    }

    updateWeaponHighlight() {
        this.weaponTexts.forEach(({ txt, value }) => {
            txt.setColor(value === this.selectedWeapon ? '#ffff00' : '#00ff00');
        });
    }

    updateGadgetHighlight() {
        this.gadgetTexts.forEach(({ txt, value }) => {
            txt.setColor(value === this.selectedGadget ? '#ffff00' : '#ff0000');
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
