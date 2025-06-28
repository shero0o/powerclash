import Phaser from 'phaser';

const API_BASE = 'http://localhost:8091/api/shop_catalogue';
const API_BASE2 = 'http://localhost:8092/api/wallet';


export default class ShopScene extends Phaser.Scene {
    constructor() {
        super({ key: 'ShopScene' });
        this.playerId = null;
        this.playerName = null;

        this.coinText     = null;
        this.graphics     = null;
        this.rectX        = 0;
        this.rectY        = 0;
        this.rectHeight   = 40;
        this.cornerRadius = 4;
        this.strokeWidth  = 3;
        this.settingsOpen = true;
        this.shopContainer= null;
        this.scrollMask   = null;
        this.currentModal = null;
    }

    init() {
        this.playerId   = this.registry.get('playerId');
        this.playerName = this.registry.get('playerName') || 'Player';
    }

    preload() {
        this.load.svg('lobby_bg', '/assets/svg/lobby_bg.svg');
        this.load.svg('icon_profile', '/assets/svg/profile-icon.svg', { width:200, height:100 });
        this.load.svg('btn-settings', '/assets/svg/btn-settings.svg', { width:200, height:100 });
        this.load.svg('icon_shop', '/assets/svg/btn-shop.svg', { width:200, height:80 });
        this.load.svg('icon_brawlers', '/assets/svg/btn-brawlers.svg', { width:200, height:80 });
        this.load.svg('icon_coin', '/assets/svg/coin-icon.svg', { width:100, height:100 });
        this.load.svg('home', '/assets/svg/btn-navigation.svg', { width:130, height:115 });
        this.load.svg('exitButtonSvg', '/assets/svg/btn-exit.svg', { width:190, height:90 });
        this.load.image('avatar2','/assets/PNG/Characters/Character2.png');
        this.load.image('avatar3','/assets/PNG/Characters/Character3.png');
        this.load.image('avatar4','/assets/PNG/Characters/Character4.png');
        this.load.image('avatar5','/assets/PNG/Characters/Character5.png');
        this.load.svg('gadget_speed',  '/assets/svg/speedGadget.svg',{ width:400, height:200 });
        this.load.svg('gadget_damage', '/assets/svg/damageGadget.svg',{ width:400, height:200 });
        this.load.svg('gadget_health', '/assets/svg/healthGadget.svg',{ width:400, height:200 });


    }

    async create() {
        const { width, height } = this.scale;
        this.add.image(width / 2, height / 2, 'lobby_bg')
            .setOrigin(0.5)
            .setDisplaySize(width, height);

        this.add.image(60, 60, 'icon_profile')
            .setOrigin(0.5)
            .setScale(0.8)
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => {
                this.scene.start('AccountScene');
            });

        this._createCoinDisplay();

        const settingsBtn = this.add.image(1400, 52, 'btn-settings')
            .setOrigin(0.5)
            .setDisplaySize(140, 70)
            .setInteractive({ useHandCursor: true })
            .setAlpha(1)
            .setDepth(1000);

        settingsBtn.on('pointerdown', () => {
            this.openSettingsWindow();
        });

        const btnBrawlers = this.add.image(90, height / 2, 'icon_brawlers')
            .setOrigin(0.5)
            .setDisplaySize(200, 80)
            .setInteractive({ useHandCursor: true });
        btnBrawlers.on('pointerdown', () => this.scene.start('InventoryScene'));

        this.add.image(90, height / 2 + 100, 'home')
            .setOrigin(0.5)
            .setDisplaySize(100, 100)
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => {
                this.scene.start('LobbyScene');
            });

        try {
            await this._refreshShopItems();
        } catch (err) {
            console.error('Fehler beim Laden der Shop-Items:', err);
        }

    }

    async _refreshCoinDisplay() {
        const coins = await fetch(`${API_BASE2}/coins?playerId=${this.playerId}`).then(r => r.json());
        this.coinText.setText(`${coins}`);
    }

    async _createCoinDisplay() {
        const coinX = 1020;
        const coinY = 50;
        const coinSize = 60;
        const rectHeight = 40;
        const rectWidth = 100;
        const cornerRadius = 4;
        const strokeWidth = 3;
        const rectX = coinX + coinSize - 12;
        const rectY = coinY - rectHeight / 2;
        const graphics = this.add.graphics();
        graphics.fillStyle(0x000000, 1);
        graphics.fillRoundedRect(rectX, rectY, rectWidth, rectHeight, cornerRadius);
        graphics.lineStyle(strokeWidth, 0x000000, 1);
        graphics.strokeRoundedRect(rectX, rectY, rectWidth, rectHeight, cornerRadius);
        const coins = await fetch(`${API_BASE2}/coins?playerId=${this.playerId}`).then(r => r.json());

        const textX = rectX + rectWidth / 2;
        const textY = rectY + rectHeight / 2;
        this.coinText = this.add.text(textX, textY, `${coins}`, {
            fontFamily: 'Arial',
            fontSize: '28px',
            color: '#ffffff',
            stroke: '#000000',
            strokeThickness: 4
        }).setOrigin(0.5);
    }

    openSettingsWindow() {
        const vw = this.scale.width;
        const vh = this.scale.height;

        const overlay = this.add.rectangle(vw/2, vh/2, vw, vh, 0x000000)
            .setOrigin(0.5)
            .setAlpha(0.8)
            .setDepth(1000);

        const dialog = this.add.rectangle(vw/2, vh/2, vw * 0.6, vh * 0.6, 0x111111)
            .setOrigin(0.5)
            .setDepth(1001);

        const title = this.add.text(vw / 2, vh / 2 - 100, 'Settings', {
            fontSize: '36px',
            fontFamily: 'Arial',
            color: '#ffffff',
            stroke: '#000000',
            strokeThickness: 6
        }).setOrigin(0.5).setDepth(1002);

        const closeBtn = this.add.image(vw / 2, vh / 2 + 120, 'exitButtonSvg')
            .setOrigin(0.5)
            .setInteractive({ useHandCursor: true })
            .setDepth(1002)
            .on('pointerdown', () => {
                overlay.destroy();
                dialog.destroy();
                title.destroy();
                closeBtn.destroy();
            });
    }

    async _loadNotOwnedItems() {
        const paths = [
            ['BRAWLER', 'brawlers/player/notOwned'],
            ['GADGET',  'gadgets/player/notOwned'],
            ['LEVEL',   'levels/player/notOwned']
        ];
        const results = {};
        await Promise.all(paths.map(async ([type, path]) => {
            const res = await fetch(`${API_BASE2}/${path}?playerId=${this.playerId}`);
            const data = await res.json();
            results[type] = data;
        }));
        return results;
    }

    _showShopItems(itemsByType) {
        const { width, height } = this.scale;
        const startX = 300;
        const startY = 200;
        const listWidth  = width - startX - 50;
        const listHeight = height - startY - 50;
        this.shopContainer.setPosition(startX, startY);
        const maskShape = this.make.graphics();
        maskShape.fillRect(startX, startY, listWidth, listHeight);
        const mask = maskShape.createGeometryMask();
        this.shopContainer.setMask(mask);
        let offsetY = 0;
        const rowHeight = 120;
        Object.entries(itemsByType).forEach(([type, items]) => {
            const title = this.add.text(0, offsetY, type, { fontSize: '40px', color: '#0ffff0' })
                .setOrigin(0, 0);
            this.shopContainer.add(title);
            offsetY += 40;

            items.forEach(item => {
                const imageKey = (type === 'BRAWLER')
                    ? { sniper:'avatar2', mage:'avatar3', healer:'avatar4', tank:'avatar5' }[item.id] || 'avatar2'
                    : (type === 'GADGET')
                        ? { SPEED_BOOST:'gadget_speed', DAMAGE_BOOST:'gadget_damage', HEALTH_BOOST:'gadget_health' }[item.id] || 'gadget_speed'
                        : null;
                if (imageKey) {
                        const img = this.add.image(-100, offsetY + rowHeight/2, imageKey)
                            .setOrigin(0.5)
                            .setDisplaySize(80, 80);
                        this.shopContainer.add(img);
                    }
                const nameText = this.add.text(0, offsetY, `${item.name} (${type})`, {
                fontFamily: 'Arial', fontSize: '24px', color: '#ffffff'
                });
                const priceText = this.add.text(0, offsetY + 30, `Price: ${item.cost} Coins`, {
                    fontFamily: 'Arial', fontSize: '20px', color: '#ffff00'
                });
                this.shopContainer.add([nameText, priceText]);
                const buyButton = this.add.text(400, offsetY + 15, 'BUY', {
                    fontFamily: 'Arial', fontSize: '24px', color: '#ffffff',
                    backgroundColor: '#333', padding: { x: 10, y: 5 }
                })
                    .setInteractive({ useHandCursor: true })
                    .on('pointerdown', async () => this.showPurchaseConfirmation(item, type));
                this.shopContainer.add(buyButton);
                offsetY += rowHeight;
            });
        });
        this.shopContainer._totalHeight = offsetY;
        this.input.on('wheel', (pointer, gameObjects, deltaX, deltaY) => {
            if (pointer.x < startX || pointer.x > startX + listWidth || pointer.y < startY || pointer.y > startY + listHeight) {
                return;
            }
            const maxY = startY;
            const minY = startY - (this.shopContainer._totalHeight - listHeight);
            this.shopContainer.y = Phaser.Math.Clamp(this.shopContainer.y - deltaY, minY, maxY);
        });
    }

    async _refreshShopItems() {
        const itemsByType = await this._loadNotOwnedItems();
        if (this.shopContainer) {
            this.shopContainer.removeAll(true);
        }

        this.shopContainer = this.add.container(300, 200);
        this._showShopItems(itemsByType);
    }

    showPurchaseConfirmation(item, type) {
        const vw = this.scale.width, vh = this.scale.height;
        const overlay = this.add.rectangle(vw/2, vh/2, vw, vh, 0x000000)
            .setOrigin(0.5).setAlpha(0.8).setDepth(2000);
        const dialog = this.add.rectangle(vw/2, vh/2, vw * 0.5, vh * 0.3, 0x111111)
            .setOrigin(0.5).setDepth(2001);
        const text = this.add.text(vw/2, vh/2 - 40, `Do you really want to buy ${item.name}?`, {
            fontFamily: 'Arial', fontSize: '24px', color: '#ffffff', stroke: '#000000', strokeThickness: 4
        }).setOrigin(0.5).setDepth(2002);

        const yesBtn = this.add.text(vw/2 - 60, vh/2 + 40, 'Yes', {
            fontFamily: 'Arial', fontSize: '24px', color: '#ffffff', backgroundColor: '#333', padding: { x:10, y:5 }
        }).setOrigin(0.5).setInteractive({ useHandCursor: true }).setDepth(2002);
        yesBtn.on('pointerdown', async () => {
            await this._confirmPurchase(item, type);
            this._destroyModal();
        });

        const noBtn = this.add.text(vw/2 + 60, vh/2 + 40, 'No', {
            fontFamily: 'Arial', fontSize: '24px', color: '#ffffff', backgroundColor: '#333', padding: { x:10, y:5 }
        }).setOrigin(0.5).setInteractive({ useHandCursor: true }).setDepth(2002);
        noBtn.on('pointerdown', () => this._destroyModal());

        this.currentModal = { overlay, dialog, text, yesBtn, noBtn };
    }

    async _confirmPurchase(item, type) {
        try {
            const res = await fetch(
                `${API_BASE2}/${type.toLowerCase()}s/buy?playerId=${this.playerId}&${type.toLowerCase()}Id=${item.id}`,
                { method: 'POST' }
            );
            if (res.ok) {
                alert(`Purchase successfull: ${item.name}`);
                await this._refreshCoinDisplay();
                await this._refreshShopItems();
            } else {
                const error = await res.text();
                alert(`Not enough Coins`, error);
            }
        } catch (err) {
            console.error('Purchasing Error:', err);
        }
    }

    _destroyModal() {
        if (!this.currentModal) return;
        Object.values(this.currentModal).forEach(obj => obj.destroy());
        this.currentModal = null;
    }



}
