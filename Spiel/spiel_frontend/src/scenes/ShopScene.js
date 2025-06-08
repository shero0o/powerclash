import Phaser from 'phaser';

export default class ShopScene extends Phaser.Scene {
    constructor() {
        super({ key: 'ShopScene' });

        this.items = [];
        this.purchases = [];
        this.playerId = null;
    }

    preload() {
        // NUR was wir brauchen → minimal!
        this.load.svg('icon_shop', '/assets/svg/btn-shop.svg', { width: 200, height: 80 });
        this.load.svg('icon_brawlers', '/assets/svg/btn-brawlers.svg', { width: 200, height: 80 });
        this.load.svg('icon_coin', '/assets/svg/coin-icon.svg', { width: 100, height: 100 });
    }

    create() {
        const { width, height } = this.scale;

        // PlayerId holen
        this.playerId = localStorage.getItem('playerId');

        // --- Coins Anzeige oben ---
        this._createCoinDisplay();

        // --- HOME Button (links oben) ---
        this.add.text(50, 100, 'HOME', { font: '24px Arial', fill: '#fff' })
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => {
                this.scene.start('LobbyScene');
            });

        // --- BRAWLERS Button (links darunter) ---
        this.add.text(50, 150, 'BRAWLERS', { font: '24px Arial', fill: '#fff' })
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => {
                this.scene.start('BrawlerScene'); // deine Scene ggf. anpassen
            });

        // --- Items Grid ---
        this.itemsGroup = this.add.group();

        // Laden → Items + Purchases
        this.loadShopData();
    }

    _createCoinDisplay() {
        const { width } = this.scale;

        this.add.image(width / 2 - 50, 50, 'icon_coin')
            .setOrigin(0.5)
            .setDisplaySize(50, 50);

        this.coinsText = this.add.text(width / 2 + 10, 50, 'Coins: ???', { font: '24px Arial', fill: '#fff' })
            .setOrigin(0, 0.5);
    }

    async loadShopData() {
        try {
            // 1. Items laden
            const itemsRes = await fetch('/api/shop_catalogue/items');
            this.items = await itemsRes.json();

            // 2. Purchases laden
            const purchasesRes = await fetch(`/api/shop_catalogue/player/${this.playerId}/purchases`);
            this.purchases = await purchasesRes.json();

            // 3. Coins laden
            const coinsRes = await fetch(`/api/wallet/coins/?playerId=${this.playerId}`);
            const coins = await coinsRes.json();
            this.coinsText.setText(`Coins: ${coins}`);

            // 4. Items anzeigen
            this.displayItems();
        } catch (error) {
            console.error('Fehler beim Laden der Shop-Daten:', error);
        }
    }

    displayItems() {
        this.itemsGroup.clear(true, true);

        const startX = 300;
        const startY = 150;
        const rowHeight = 100;
        const colWidth = 350;

        let row = 0;
        let col = 0;

        this.items.forEach(item => {
            const x = startX + col * colWidth;
            const y = startY + row * rowHeight;

            // Name + Preis
            this.add.text(x, y, `${item.name} (${item.price} Coins)`, { font: '20px Arial', fill: '#fff' });

            // Gekauft prüfen
            const isPurchased = this.purchases.some(p => p.itemType === item.type && p.itemId === item.id);
            const btnText = isPurchased ? 'GEKAUFT' : 'KAUFEN';

            // Sichtbarer Button (Hintergrund + Text)
            const buttonWidth = 100;
            const buttonHeight = 40;

            const buttonBg = this.add.rectangle(x + buttonWidth / 2, y + 60, buttonWidth, buttonHeight, 0xaaaaaa)
                .setStrokeStyle(2, 0x000000)
                .setInteractive({ useHandCursor: true });

            const buttonText = this.add.text(x + buttonWidth / 2, y + 60, btnText, {
                font: '16px Arial',
                color: '#000000'
            }).setOrigin(0.5);

            if (!isPurchased) {
                buttonBg.on('pointerdown', () => this.buyItem(item));
                buttonText.setInteractive({ useHandCursor: true })
                    .on('pointerdown', () => this.buyItem(item));
            }

            this.itemsGroup.addMultiple([buttonBg, buttonText]);

            // 2 Spalten Logik
            col++;
            if (col >= 2) {
                col = 0;
                row++;
            }
        });
    }

    async buyItem(item) {
        let endpoint = '';
        if (item.type === 'BRAWLER') {
            endpoint = `/api/shop_catalogue/brawler/${item.id}/buy?playerId=${this.playerId}`;
        } else if (item.type === 'GADGET') {
            endpoint = `/api/shop_catalogue/gadget/${item.id}/buy?playerId=${this.playerId}`;
        } else if (item.type === 'LEVEL') {
            endpoint = `/api/shop_catalogue/level/${item.id}/buy?playerId=${this.playerId}`;
        }

        try {
            await fetch(endpoint, { method: 'POST' });

            // Nach dem Kauf → neu laden
            await this.loadShopData();
        } catch (error) {
            console.error('Fehler beim Kauf:', error);
        }
    }
}
