import Phaser from 'phaser';

const API_BASE = 'http://localhost:8093/api/account';

export default class AccountScene extends Phaser.Scene {
    constructor() {
        super({ key: 'AccountScene' });
        this.playerId = null;
        this.playerName = null;
        this.statusText = null;
    }

    create() {
        const { width, height } = this.scale;

        this.cameras.main.setBackgroundColor('#000000');

        // Titel
        this.add.text(width / 2, 80, 'Account', {
            fontFamily: 'Arial', fontSize: '48px', color: '#ffffff', stroke: '#000000', strokeThickness: 6
        }).setOrigin(0.5);

        // Statusanzeige
        this.statusText = this.add.text(width / 2, 160, 'Not logged in', {
            fontFamily: 'Arial', fontSize: '24px', color: '#ffff00'
        }).setOrigin(0.5);

        // Register Button
        const registerBtn = this.add.text(width / 2, 240, 'Register', {
            fontFamily: 'Arial', fontSize: '32px', color: '#00ff00'
        }).setOrigin(0.5).setInteractive({ useHandCursor: true });
        registerBtn.on('pointerdown', () => this.register());

        // Login Button
        const loginBtn = this.add.text(width / 2, 320, 'Login', {
            fontFamily: 'Arial', fontSize: '32px', color: '#00ff00'
        }).setOrigin(0.5).setInteractive({ useHandCursor: true });
        loginBtn.on('pointerdown', () => this.login());

        // Change Name Button
        const changeBtn = this.add.text(width / 2, 400, 'Change Name', {
            fontFamily: 'Arial', fontSize: '32px', color: '#00ff00'
        }).setOrigin(0.5).setInteractive({ useHandCursor: true });
        changeBtn.on('pointerdown', () => this.changeName());

        // Back Button
        const backBtn = this.add.text(20, 20, '< Back', {
            fontFamily: 'Arial', fontSize: '24px', color: '#ffffff'
        }).setOrigin(0).setInteractive({ useHandCursor: true });
        backBtn.on('pointerdown', () => this.scene.start('LobbyScene'));
    }

    updateStatus() {
        if (this.playerId) {
            this.statusText.setText(`Player: ${this.playerName} (ID: ${this.playerId})`);
            this.registry.set('playerId', this.playerId);
            this.registry.set('playerName', this.playerName);
        } else {
            this.statusText.setText('Not logged in');
        }
    }

    async register() {
        const name = window.prompt('Enter your name:');
        if (!name) return;
        try {
            const res = await fetch(`${API_BASE}/createPlayer?name=${encodeURIComponent(name)}`, {
                method: 'POST'
            });
            const player = await res.json();
            this.playerId = player.id;
            this.playerName = player.name;
            this.updateStatus();
        } catch (err) {
            console.error('Register error:', err);
        }
    }

    async login() {
        const id = window.prompt('Enter your Player ID:');
        if (!id) return;
        try {
            const res = await fetch(`${API_BASE}/player?id=${id}`);
            if (!res.ok) {
                window.alert('Player not found');
                return;
            }
            const player = await res.json();
            this.playerId = player.id;
            this.playerName = player.name;
            this.updateStatus();
        } catch (err) {
            console.error('Login error:', err);
        }
    }

    async changeName() {
        if (!this.playerId) {
            window.alert('Please login first');
            return;
        }
        const newName = window.prompt('Enter new name:', this.playerName);
        if (!newName) return;
        try {
            const res = await fetch(`${API_BASE}/updatePlayerName?id=${this.playerId}&name=${encodeURIComponent(newName)}`, {
                method: 'PUT'
            });
            const player = await res.json();
            this.playerName = player.name;
            this.updateStatus();
        } catch (err) {
            console.error('Update name error:', err);
        }
    }
}
