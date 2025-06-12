import Phaser from 'phaser';

const API_BASE = 'http://localhost:8092/api/shop_catalogue';

export default class ShopScene extends Phaser.Scene {
    constructor() {
        super({ key: 'ShopScene' });
        this.playerId = null;
        this.playerName = null;

        this.coinText     = null;
        this.graphics     = null;
        this.rectX        = 0;
        this.rectY        = 0;
        this.rectWidth    = 100;
        this.rectHeight   = 40;
        this.cornerRadius = 4;
        this.strokeWidth  = 3;
        this.coinX        = 1020;  // x-pos für Coin-Icon
        this.coinY        = 50;  // y-pos für Coin-Icon
        this.coinSize     = 60;  // Größe (60×60) für Coin-Icon
        this.coinContainer= null;
        this.settingsOpen = false;
    }

    init() {
        this.playerId   = this.registry.get('playerId')   || 1;
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

        // — Brawler-Avatare —
        this.load.image('avatar2','/assets/PNG/Characters/Character2.png');
        this.load.image('avatar3','/assets/PNG/Characters/Character3.png');
        this.load.image('avatar4','/assets/PNG/Characters/Character4.png');
        this.load.image('avatar5','/assets/PNG/Characters/Character5.png');


        // — Gadget-Icons —
        this.load.svg('gadget_speed',  '/assets/svg/speedGadget.svg',{ width:400, height:200 });
        this.load.svg('gadget_damage', '/assets/svg/damageGadget.svg',{ width:400, height:200 });
        this.load.svg('gadget_health', '/assets/svg/healthGadget.svg',{ width:400, height:200 });


    }

    async create() {
        const { width, height } = this.scale;

        // --- dein bestehender Code ---
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

        const settingsBtn = this.add.image(1400, height / 2 - 330, 'btn-settings')
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
        btnBrawlers.on('pointerdown', () => console.log('Brawlers öffnen'));

        this.add.image(90, height / 2 + 100, 'home')
            .setOrigin(0.5)
            .setDisplaySize(100, 100)
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => {
                this.scene.start('LobbyScene');
            });

        // --- NEU: Shop Items laden und anzeigen ---
        try {
            const catalogueRes = await fetch(`http://localhost:8092/api/shop/catalogue`);
            const catalogue = await catalogueRes.json();
            this._showShopCatalogue(catalogue);
        } catch (err) {
            console.error('Fehler beim Laden der Shop-Items:', err);
        }

    }

    _showShopCatalogue(catalogue) {
        const { width, height } = this.scale;
        const startX = 300;
        const startY = 200;
        const rowHeight = 120;

        const brawlerImageMap = {
            'sniper': 'avatar2',
            'mage': 'avatar3',
            'healer': 'avatar4',
            'tank': 'avatar5'
        };

        const gadgetImageMap = {
            'SPEED_BOOST': 'gadget_speed',
            'DAMAGE_BOOST': 'gadget_damage',
            'HEALTH_BOOST': 'gadget_health'
        };

        catalogue.forEach((item, index) => {
            const y = startY + index * rowHeight;

            // → Image Key ermitteln (nur für Brawler & Gadget)
            let imageKey = null;
            if (item.type === 'BRAWLER') {
                imageKey = brawlerImageMap[item.id] || 'avatar2';
            } else if (item.type === 'GADGET') {
                imageKey = gadgetImageMap[item.id] || 'gadget_speed';
            }

            // → LEVEL → nur Text, KEIN Bild
            if (item.type === 'LEVEL') {
                this.add.text(startX, y, `${item.name} (${item.type})`, {
                    fontFamily: 'Arial',
                    fontSize: '24px',
                    color: '#ffffff'
                });

                this.add.text(startX, y + 30, `Preis: ${item.price} Coins`, {
                    fontFamily: 'Arial',
                    fontSize: '20px',
                    color: '#ffff00'
                });
            } else {
                // → Brawler oder Gadget → Bild + Text
                if (imageKey) {
                    this.add.image(startX - 100, y, imageKey)
                        .setOrigin(0.5)
                        .setDisplaySize(80, 80);
                }

                this.add.text(startX, y, `${item.name} (${item.type})`, {
                    fontFamily: 'Arial',
                    fontSize: '24px',
                    color: '#ffffff'
                });

                this.add.text(startX, y + 30, `Preis: ${item.price} Coins`, {
                    fontFamily: 'Arial',
                    fontSize: '20px',
                    color: '#ffff00'
                });
            }

            // → Kaufen-Button
            const buyButton = this.add.text(startX + 500, y + 15, 'KAUFEN', {
                fontFamily: 'Arial',
                fontSize: '24px',
                color: '#00ff00',
                backgroundColor: '#333',
                padding: { x: 10, y: 5 }
            })
                .setInteractive({ useHandCursor: true })
                .on('pointerdown', async () => {
                    try {
                        const res = await fetch(`http://localhost:8092/api/wallet/purchase`, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({
                                playerId: this.playerId,
                                shopItemId: item.id
                            })
                        });

                        if (res.ok) {
                            alert(`Erfolgreich gekauft: ${item.name}`);
                            this._refreshCoinDisplay();
                        } else {
                            const error = await res.text();
                            alert(`Fehler beim Kauf: ${error}`);
                        }
                    } catch (err) {
                        console.error('Fehler beim Kauf:', err);
                    }
                });
        });
    }




    async _refreshCoinDisplay() {
        const coins = await fetch(`${API_BASE}/coins?playerId=${this.playerId}`).then(r => r.json());
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

        // Hintergrund Rechteck
        const rectX = coinX + coinSize - 12;
        const rectY = coinY - rectHeight / 2;
        const graphics = this.add.graphics();
        graphics.fillStyle(0x000000, 1);
        graphics.fillRoundedRect(rectX, rectY, rectWidth, rectHeight, cornerRadius);
        graphics.lineStyle(strokeWidth, 0x000000, 1);
        graphics.strokeRoundedRect(rectX, rectY, rectWidth, rectHeight, cornerRadius);

        // Text
        const coins = await fetch(`${API_BASE}/coins?playerId=${this.playerId}`).then(r => r.json());

        const textX = rectX + rectWidth / 2;
        const textY = rectY + rectHeight / 2;
        this.coinText = this.add.text(textX, textY, `${coins}`, {
            fontFamily: 'Arial',
            fontSize: '28px',
            color: '#ffffff',
            stroke: '#000000',
            strokeThickness: 4
        }).setOrigin(0.5);
        // Coin Icon
        const coinImage = this.add.image(coinX, coinY, 'icon_coin')
            .setOrigin(0, 0.5)
            .setDisplaySize(coinSize, coinSize);


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
}
