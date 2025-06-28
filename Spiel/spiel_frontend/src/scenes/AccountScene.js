import Phaser from 'phaser';

const API_BASE = 'http://localhost:8093/api/account';

export default class AccountScene extends Phaser.Scene {
    constructor() {
        super({ key: 'AccountScene' });
        this.playerId = null;
        this.playerName = null;
        this.statusText = null;
    }

    preload() {
        this.load.svg('lobby_bg',      '/assets/svg/lobby_bg.svg');
        this.load.svg('icon_profile',  '/assets/svg/profile-icon.svg', { width:200, height:100 });
        this.load.svg('btn-settings',  '/assets/svg/btn-settings.svg',{ width:200, height:100 });
        this.load.svg('icon_shop',     '/assets/svg/btn-shop.svg',    { width:200, height:80 });
        this.load.svg('icon_coin',     '/assets/svg/coin-icon.svg',   { width:100, height:100 });
        this.load.svg('home', '/assets/svg/btn-navigation.svg',{width:130,height:115})
        }

    init() {
        this.playerId   = this.registry.get('playerId');
        this.playerName = this.registry.get('playerName') || 'Not logged in';
    }

    create() {
        const { width, height } = this.scale;

        this.cameras.main.setBackgroundColor('#000000');

        this.add.image(width/2, height/2, 'lobby_bg')
            .setOrigin(0.5)
            .setDisplaySize(width, height);

        this.add.text(width / 2, 80, 'Account', {
            fontFamily: 'Arial', fontSize: '48px', color: '#ffffff', stroke: '#000000', strokeThickness: 6
        }).setOrigin(0.5);

        this.statusText = this.add.text(width / 2, 160, this.playerName, {
            fontFamily: 'Arial', fontSize: '24px', color: '#ffffff'
        }).setOrigin(0.5);

        const registerBtn = this.add.text(width / 2, 240, 'Register', {
            fontFamily: 'Arial', fontSize: '32px', color: '#ffffff'
        }).setOrigin(0.5).setInteractive({ useHandCursor: true });
        registerBtn.on('pointerdown', () => this.register());

        const loginBtn = this.add.text(width / 2, 320, 'Login', {
            fontFamily: 'Arial', fontSize: '32px', color: '#ffffff'
        }).setOrigin(0.5).setInteractive({ useHandCursor: true });
        loginBtn.on('pointerdown', () => this.login());

        const changeBtn = this.add.text(width / 2, 400, 'Change Name', {
            fontFamily: 'Arial', fontSize: '32px', color: '#ffffff'
        }).setOrigin(0.5).setInteractive({ useHandCursor: true });
        changeBtn.on('pointerdown', () => this.changeName());

        const shopX = 90, shopY = height/2 - 100;
        const btnShop = this.add.image(90, height / 2 - 100, 'icon_shop')
            .setOrigin(0.5)
            .setDisplaySize(200, 80)
            .setInteractive({ useHandCursor: true });

        btnShop.on('pointerdown', () => {
            this.scene.start('ShopScene');
        });

        const btnHome = this.add.image(shopX, shopY + 200, 'home')
            .setOrigin(0.5)
            .setDisplaySize(100, 100)
            .setInteractive({ useHandCursor: true })

        btnHome.on('pointerdown', () => {
            this.scene.start('LobbyScene');
        });

        const btnBrawlers = this.add.image(90, height / 2, 'icon_brawlers')
            .setOrigin(0.5)
            .setDisplaySize(200, 80)
            .setInteractive({ useHandCursor: true });
        btnBrawlers.on('pointerdown', () => {
            this.scene.start('InventoryScene');
        });

    }

    updateStatus() {
        if (this.playerId) {
            this.statusText.setText(`Player: ${this.playerName}`);
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
            const res = await fetch(`${API_BASE}/createPlayer?name=${name}`, {
                method: 'POST'
            });
            if (res.status === 409) {
                window.alert('Name already in use');
                return;
            }
            if (!res.ok) {
                window.alert('Registration failed');
                return;
                }
            const player = await res.json();
            this.playerId = player.id;
            this.playerName = player.name;
            this.updateStatus();
        } catch (err) {
            console.error('Register error:', err);
            window.alert('Could not register, please try again later');
        }
    }

    async login() {
        const name = window.prompt('Enter your unique Player Name:');
        if (!name) return;
        try {
            const res = await fetch(`${API_BASE}/playerByName?name=${name}`);
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
