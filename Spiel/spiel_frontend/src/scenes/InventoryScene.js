import Phaser from 'phaser';

const API_BASE = 'http://localhost:8092/api/wallet';

const weaponAssetMap = {
    Rifle: 'weapon_rifle',
    Sniper: 'weapon_sniper',
    Shotgun: 'weapon_shotgun',
    Mine: 'weapon_mine'
};

const gadgetAssetMap = {
    "Speed Boost": 'gadget_speed',
    "Damage Boost": 'gadget_damage',
    "HP Boost": 'gadget_health'
};



export default class InventoryScene extends Phaser.Scene {
    constructor() {
        super({ key: 'InventoryScene' });
        this.coins = 0;
        this.brawlers = [];
        this.gadgets = [];
        this.levels = [];
        this.weapons = [];
        this.selectedWeapon = null;
        this.selectedBrawler = null;
        this.selectedGadget = null;
        this.selectedLevel = null;
    }

    init(data) {
        this.selectedWeapon  = data.weapon;
        this.selectedBrawler = data.brawler;
        this.selectedGadget  = data.gadget;
        this.selectedLevel   = data.level;
        this.playerId   = this.registry.get('playerId');
        this.playerName = this.registry.get('playerName') || 'Player';
    }

    preload() {
        this.load.svg('lobby_bg',      '/assets/svg/lobby_bg.svg');
        this.load.svg('icon_profile',  '/assets/svg/profile-icon.svg', { width:200, height:100 });
        this.load.svg('btn-settings',  '/assets/svg/btn-settings.svg',{ width:200, height:100 });
        this.load.svg('icon_shop',     '/assets/svg/btn-shop.svg',    { width:200, height:80 });
        this.load.svg('icon_coin',     '/assets/svg/coin-icon.svg',   { width:100, height:100 });
        this.load.svg('home', '/assets/svg/btn-navigation.svg',{width:130,height:115})
        this.load.image('weapon_rifle',   '/assets/PNG/Weapons/Mashinegun.png');
        this.load.image('weapon_sniper',  '/assets/PNG/Weapons/Sniper.png');
        this.load.image('weapon_shotgun', '/assets/PNG/Weapons/Shotgun.png');
        this.load.image('weapon_mine',    '/assets/PNG/explosion/bomb.png');
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
        try {
            const [coinsRes, brawlersRes, gadgetsRes, levelsRes, weaponsRes, selectedRes] = await Promise.all([
                fetch(`${API_BASE}/coins?playerId=${this.playerId}`),
                fetch(`${API_BASE}/brawlers/player?playerId=${this.playerId}`),
                fetch(`${API_BASE}/gadgets/player?playerId=${this.playerId}`),
                fetch(`${API_BASE}/levels/player?playerId=${this.playerId}`),
                fetch(`${API_BASE}/weapons`),
                fetch(`${API_BASE}/selected?playerId=${this.playerId}`)
            ]);

            this.coins    = await coinsRes.json();
            this.brawlers = await brawlersRes.json();
            this.gadgets  = await gadgetsRes.json();
            this.levels   = await levelsRes.json();
            this.weapons  = await weaponsRes.json();
            const sel     = await selectedRes.json();

            this.selectedBrawler = sel?.brawlerId || this.brawlers[0]?.id;
            this.selectedGadget  = sel?.gadgetId  || this.gadgets[0]?.id;
            this.selectedLevel   = sel?.levelId   || this.levels[0]?.id;
            this.selectedWeapon  = sel?.weaponId  || this.brawlers.find(b => b.id === this.selectedBrawler)?.defaultWeapon;
            console.log('Loaded wallet data');
        } catch (err) {
            console.error('Fetch-Error:', err);
            this.add.text(width/2, height/2, 'Daten konnten nicht geladen werden', {
                fontSize: '20px', fill: '#ff4444'
            }).setOrigin(0.5);
        }
        this.add.image(width/2, height/2, 'lobby_bg')
            .setOrigin(0.5)
            .setDisplaySize(width, height);

        this.add.image(60, 60, 'icon_profile')
            .setOrigin(0.5)
            .setScale(0.8)
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => {
                this.scene.start('AccountScene');
            });

        const settingsBtn = this.add.image(1400, 52, 'btn-settings')
            .setOrigin(0.5)
            .setDisplaySize(140, 70)
            .setInteractive({ useHandCursor: true })
            .setAlpha(1)
            .setDepth(1000);

        settingsBtn.on('pointerdown', () => {
            this.openSettingsWindow();
        });


        this._createCoinDisplay();
        const shopX = 90, shopY = height/2 - 100;
        const btnShop = this.add.image(90, height / 2 - 100, 'icon_shop')
            .setOrigin(0.5)
            .setDisplaySize(200, 80)
            .setInteractive({ useHandCursor: true });

        btnShop.on('pointerdown', () => {
            this.scene.start('ShopScene');
        });

        this.add.image(shopX, shopY + 100, 'home')
            .setOrigin(0.5)
            .setDisplaySize(100, 100)
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => this.finish());

        this.currentTab = 'Weapons';
        this._setupTabs();
        this._showWeaponOptions();
    }

    switchTab(label) {
        if (this.currentTab === label) return;
        this.children.list.filter(ch => ch.tabged).forEach(ch => ch.destroy());
        Object.entries(this.tabTexts).forEach(([l, txt]) => {
            txt.setColor(l === label ? '#ffff00' : '#ffffff');
        });
        this.currentTab = label;

        if (label === 'Weapons') this._showWeaponOptions();
        if (label === 'Brawlers') this._showBrawlerOptions();
        if (label === 'Gadgets') this._showGadgetOptions();
    }

    _showWeaponOptions() {
        const { width, height } = this.scale;
        this.weapons.forEach((w, i) => {
            const x = width/2 - 300 + i*200;
            const y = height/2;
            console.log(w);
            const assetKey = weaponAssetMap[w.weaponType] || 'weapon_rifle';

            const spr = this.add.image(x, y, assetKey)
                .setInteractive({ useHandCursor: true })
                .setDisplaySize(120, 120)
                .on('pointerdown', async () => {
                    this.selectedWeapon = w.id;
                    await this.finish();
                });
            spr.tabged = true;
            const infoTxt = `Damage: ${w.damage}\nSpeed: ${w.projectileSpeed}\nRange: ${w.range}`;
            this.add.text(x, y + 120, infoTxt, {
                fontFamily: 'Arial', fontSize: '20px', color: '#ffff00', align: 'center'
            }).setOrigin(0.5, 0).tabged = true;

            if (w.id === this.selectedWeapon) {
                this._drawFrame(x, y, 160, 220);
            }
        });
    }

    _showBrawlerOptions() {
        const { width, height } = this.scale;
        this.brawlers.forEach((b, i) => {
            const x = width/2 - 300 + i*200;
            const y = height/2;
            const assetKey = `avatar${b.id+1}`;

            const spr = this.add.image(x, y, assetKey)
                .setInteractive({ useHandCursor: true })
                .setScale(0.4)
                .on('pointerdown', async () => {
                    this.selectedBrawler = b.id;
                    await this.finish();
                });
            spr.tabged = true;
            const infoTxt = `Name: ${b.name}\nHP: ${b.healthPoints}\nCost: ${b.cost}`;
            this.add.text(x, y + 120, infoTxt, {
                fontFamily: 'Arial', fontSize: '20px', color: '#ffff00', align: 'center'
            }).setOrigin(0.5, 0).tabged = true;

            if (b.id === this.selectedBrawler) {
                this._drawFrame(x, y,160,220);
            }
        });
    }

    _showGadgetOptions() {
        const { width, height } = this.scale;
        this.gadgets.forEach((g, i) => {
            const x = width/2 - 300 + i*200;
            const y = height/2;
            console.log(g);
            const assetKey = gadgetAssetMap[g.name];

            const spr = this.add.image(x, y, assetKey)
                .setInteractive({ useHandCursor: true })
                .setScale(0.7)
                .on('pointerdown', async () => {
                    this.selectedGadget = g.id;
                    await this.finish();
                });
            spr.tabged = true;
            const infoTxt = `Name: ${g.name}\nCost: ${g.cost}\nDescription:\n${this.addLineBreaks(g.description, 20)}`;
            this.add.text(x, y + 120, infoTxt, {
                fontFamily: 'Arial', fontSize: '20px', color: '#ffff00', align: 'center'
            }).setOrigin(0.5, 0).tabged = true;

            if (g.id === this.selectedGadget) {
                this._drawFrame(x, y, 160, 220);
            }
        });
    }

    addLineBreaks(str, maxLen) {
        const words = str.split(' ');
        const lines = [];
        let current = '';

        for (const w of words) {
            if ((current + ' ' + w).trim().length > maxLen) {
                lines.push(current.trim());
                current = w;
            } else {
                current += ' ' + w;
            }
        }
        if (current) lines.push(current.trim());

        return lines.join('\n');
    }

    async finish() {
        try {
            await Promise.all([
                fetch(`${API_BASE}/selected/weapon?playerId=${this.playerId}&weaponId=${this.selectedWeapon}`, {
                    method: 'POST', headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({ playerId: this.playerId, weaponId: this.selectedWeapon })
                }),
                fetch(`${API_BASE}/selected/brawler?playerId=${this.playerId}&brawlerId=${this.selectedBrawler}`, {
                    method: 'POST', headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({ playerId: this.playerId, brawlerId: this.selectedBrawler })
                }),
                fetch(`${API_BASE}/selected/gadget?playerId=${this.playerId}&gadgetId=${this.selectedGadget}`, {
                    method: 'POST', headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({ playerId: this.playerId, gadgetId: this.selectedGadget })
                }),
                fetch(`${API_BASE}/selected/level?playerId=${this.playerId}&levelId=${this.selectedLevel}`, {
                    method: 'POST', headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({ playerId: this.playerId, levelId: this.selectedLevel })
                })
            ]);
        } catch (err) {
            console.error('Save selection error:', err);
        }
        this.registry.set('weapon', this.selectedWeapon);
        this.registry.set('brawler', this.selectedBrawler);
        this.registry.set('gadget', this.selectedGadget);
        this.registry.set('level', this.selectedLevel);
        this.scene.resume('LobbyScene');
        this.scene.get('LobbyScene').events.emit('inventoryDone');
        this.scene.stop();
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



    }

    openSettingsWindow() {
        if (this.settingsOpen) return;
        this.settingsOpen = true;

        const vw = this.scale.width;
        const vh = this.scale.height;
        const overlay = this.add.rectangle(
            vw / 2,
            vh / 2,
            vw,
            vh,
            0x000000
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setAlpha(0.8)
            .setDepth(1000);
        const winWidth  = vw * 0.6;
        const winHeight = vh * 0.6;
        const dialogBg = this.add.rectangle(
            vw / 2, vh / 2,
            winWidth, winHeight,
            0x111111
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setDepth(1001)
        const border = this.add.graphics()
            .lineStyle(4, 0xffffff)
            .strokeRect(
                (vw - winWidth) / 2,
                (vh - winHeight) / 2,
                winWidth,
                winHeight
            )
            .setScrollFactor(0)
            .setDepth(1002);
        const title = this.add.text(
            vw / 2,
            (vh - winHeight) / 2 + 30,
            'Settings',
            {
                fontSize: '36px',
                fontFamily: 'Arial',
                color: '#ffffff',
                stroke: '#000000',
                strokeThickness: 6
            }
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setDepth(1003);
        const exampleText = this.add.text(
            vw / 2,
            vh / 2 - 20,
            'Key for Gadget:',
            {
                fontSize: '24px',
                fontFamily: 'Arial',
                color: '#ffffff'
            }
        )
            .setOrigin(0.5, 0.5)
            .setScrollFactor(0)
            .setDepth(1003);
        const gadgetKeyInput = document.createElement('input');
        gadgetKeyInput.type = 'text';
        gadgetKeyInput.maxLength = 1;
        gadgetKeyInput.placeholder = 'Taste für Gadget';
        gadgetKeyInput.style.position = 'absolute';
        const textBounds = exampleText.getBounds();

        gadgetKeyInput.style.left = `${textBounds.right + 10}px`;
        gadgetKeyInput.style.top  = `${textBounds.top - 5}px`;

        Object.assign(gadgetKeyInput.style, {
            position: 'absolute',
            left: `${textBounds.right + 10}px`,
            top: `${textBounds.top - 5}px`,
            width: '30px',
            height: '30px',
            fontSize: '16px',
            textAlign: 'center',
            padding: '2px',
            border: '2px solid #ccc',
            borderRadius: '4px',
            backgroundColor: '#222',
            color: '#fff',
            zIndex: 10000
        });

        gadgetKeyInput.value = localStorage.getItem('gadgetKey') || 'Q';

        document.body.appendChild(gadgetKeyInput);
        const closeBtn = this.add.image(
            vw / 2,
            (vh + winHeight) / 2 - 30, "exitButtonSvg"
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setDepth(1003)
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => {
                const newKey = gadgetKeyInput.value.toUpperCase();

                if (/^[A-Z]$/.test(newKey)) {
                    localStorage.setItem('gadgetKey', newKey);
                    this.registry.set('gadgetKey', newKey);
                    console.log(`Gadget-Taste gespeichert: ${newKey}`);
                } else {
                    alert('Ungültige Taste! Bitte nur einen Buchstaben A-Z eingeben.');
                }
                gadgetKeyInput.remove();
                overlay.destroy();
                dialogBg.destroy();
                border.destroy();
                title.destroy();
                exampleText.destroy();
                closeBtn.destroy();
                this.settingsOpen = false;
            });


    }

    _drawFrame(x, y, width, height) {
        const gfx = this.add.graphics();
        gfx.lineStyle(4, 0xffff00);
        gfx.strokeRect(x - width/2, y - height/2, width, height);
        gfx.tabged = true;
    }

    _setupTabs() {
        const tabs = ['Weapons', 'Brawlers', 'Gadgets'];
        this.tabTexts = {};

        tabs.forEach((label, i) => {
            const x = this.scale.width / 2 - 200 + i * 200;
            this.tabTexts[label] = this.add.text(x, 180, label, {
                fontFamily: 'Arial', fontSize: '24px',
                color: label === this.currentTab ? '#ffff00' : '#ffffff'
            })
                .setOrigin(0.5)
                .setInteractive({ useHandCursor: true })
                .on('pointerdown', () => this.switchTab(label));
        });
    }



}
