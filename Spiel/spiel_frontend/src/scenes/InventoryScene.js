// src/scenes/InventoryScene.js
import Phaser from 'phaser';

const API_BASE = 'http://localhost:8092/api/wallet';

const weaponAssetMap = {
    RIFLE_BULLET: 'weapon_rifle',
    SNIPER: 'weapon_sniper',
    SHOTGUN_PELLET: 'weapon_shotgun',
    MINE: 'weapon_mine'
};

const weaponLabels = {
    RIFLE_BULLET: 'Rifle',
    SNIPER: 'Sniper',
    SHOTGUN_PELLET: 'Shotgun',
    MINE: 'Mine'
};

const gadgetAssetMap = {
    SPEED_BOOST: 'gadget_speed',
    DAMAGE_BOOST: 'gadget_damage',
    HEALTH_BOOST: 'gadget_health'
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
        this.playerId   = this.registry.get('playerId')   || 1;
        this.playerName = this.registry.get('playerName') || 'Player';
    }

    preload() {
        // — Lobby-Assets (Hintergrund, UI) —
        this.load.svg('lobby_bg',      '/assets/svg/lobby_bg.svg');
        this.load.svg('icon_profile',  '/assets/svg/profile-icon.svg', { width:200, height:100 });
        this.load.svg('btn-settings',  '/assets/svg/btn-settings.svg',{ width:200, height:100 });
        this.load.svg('icon_shop',     '/assets/svg/btn-shop.svg',    { width:200, height:80 });
        this.load.svg('icon_coin',     '/assets/svg/coin-icon.svg',   { width:100, height:100 });
        this.load.svg('home', '/assets/svg/btn-navigation.svg',{width:130,height:115})
        // — Waffen-Assets —
        this.load.image('weapon_rifle',   '/assets/PNG/Weapons/Mashinegun.png');
        this.load.image('weapon_sniper',  '/assets/PNG/Weapons/Sniper.png');
        this.load.image('weapon_shotgun', '/assets/PNG/Weapons/Shotgun.png');
        this.load.image('weapon_mine',    '/assets/PNG/explosion/bomb.png');

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

        // ─── Daten via GET laden ─────────────────────────────────────────
        try {
            const [coinsRes, brawlersRes, gadgetsRes, levelsRes, weaponsRes, selectedRes] = await Promise.all([
                fetch(`${API_BASE}/coins?playerId=${this.playerId}`),
                fetch(`${API_BASE}/brawlers?playerId=${this.playerId}`),
                fetch(`${API_BASE}/gadgets?playerId=${this.playerId}`),
                fetch(`${API_BASE}/levels?playerId=${this.playerId}`),
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

        // — Hintergrund & Header UI auf y=60 —
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

        // — Shop & Back to Lobby —
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

        // — Tab-Leiste unter y=120 —
        const tabs = ['Weapons','Brawlers','Gadgets'];
        this.tabTexts = {};
        tabs.forEach((label,i) => {
            const x = width/2 - 200 + i*200;
            this.tabTexts[label] = this.add.text(x, 180, label, {
                fontFamily:'Arial', fontSize:'24px',
                color: label==='Weapons' ? '#ffff00' : '#ffffff'
            })
                .setOrigin(0.5)
                .setInteractive({ useHandCursor:true })
                .on('pointerdown', ()=> this.switchTab(label));
        });

        // Erst-Tab
        this.currentTab = 'Weapons';
        this._showWeaponOptions();
    }

    switchTab(label) {
        if (this.currentTab === label) return;
        // alte Inhalte löschen
        this.children.list.filter(ch => ch.tabged).forEach(ch => ch.destroy());
        // Tab-Text färben
        Object.entries(this.tabTexts).forEach(([l,txt]) => {
            txt.setColor(l===label ? '#ffff00' : '#ffffff');
        });
        this.currentTab = label;
        if (label==='Weapons') this._showWeaponOptions();
        if (label==='Brawlers') this._showBrawlerOptions();
        if (label==='Gadgets')  this._showGadgetOptions();
    }

    _showWeaponOptions() {
        const { width, height } = this.scale;
        const weapons = [
            { key:'weapon_rifle',   value:'RIFLE_BULLET',    label:'Rifle'   },
            { key:'weapon_sniper',  value:'SNIPER',          label:'Sniper'  },
            { key:'weapon_shotgun', value:'SHOTGUN_PELLET',  label:'Shotgun' },
            { key:'weapon_mine',    value:'MINE',            label:'Mine'    }
        ];
        weapons.forEach((w,i) => {
            const x = width/2 -300 + i*200, y = height/2;
            const spr = this.add.image(x, y, w.key)
                .setInteractive({useHandCursor:true})
                .setDisplaySize(120,120)
                .on('pointerdown', () => {
                    // update local state
                    this.selectedWeapon = w.value;
                    this.finish();
                });
            spr.tabged = true;
            this.add.text(x, y+80, w.label, {
                fontFamily:'Arial', fontSize:'20px', color:'#fff'
            }).setOrigin(0.5).tabged = true;

            // 1) Finde die zugehörigen Daten
            const info = this.brawlers.find(b => b.id === w.value);
            // 2) Erstelle den Infotext
            if (info) {
                const infoTxt = `
                Damage: ${info.damage}
                Proj.Speed:${info.projectileSpeed}
                Range: ${info.range}
                Cooldown: ${info.weaponCooldown}
                Mag.Size: ${info.magazineSize}
                `.trim();
                this.add.text(x, y + 110, infoTxt, {
                    fontFamily: 'Arial', fontSize: '14px', color: '#00ff00', align: 'center'
                })
                    .setOrigin(0.5, 0)
                    .tabged = true;
            }

            if (w.id === this.selectedWeapon) {
                this._drawFrame(x, y, 120, 120);
            }
        });

    }

    _showBrawlerOptions() {
        const { width, height } = this.scale;
        // avatars enthält nur die Keys & Werte fürs Event-Handling
        const avatars = [
                       { key: 'avatar2', value: 'sniper' }, // Hitman
                       { key: 'avatar3', value: 'mage'   }, // Soldier
                       { key: 'avatar4', value: 'healer' }, // WomanGreen
                       { key: 'avatar5', value: 'tank'   }  // Robot
                   ];
        avatars.forEach((entry,i) => {
            const x = width/2 -300 + i*200, y = height/2;
            const spr = this.add.image(x, y, entry.key)
                .setInteractive({useHandCursor:true})
                .setScale(0.4)
                .on('pointerdown', () => {
                    this.selectedBrawler = entry.value;
                    this.finish();
                });
            spr.tabged = true;

            // 1) Finde die zugehörigen Daten
            const info = this.brawlers.find(b => b.id === entry.value);
            // 2) Erstelle den Infotext
            if (info) {
                const txt = `
                    Name: ${info.name}
                    HP:   ${info.health}
                    Speed:${info.speed}
                    `;
                this.add.text(x, y + 80, txt.trim(), {
                    fontFamily:'Arial', fontSize:'16px', color:'#ffff00', align: 'center'
                })
                    .setOrigin(0.5, 0)
                    .tabged = true;
            }

            if (info.id === this.selectedBrawler) {
                const displayWidth = spr.displayWidth;
                const displayHeight = spr.displayHeight;
                this._drawFrame(x, y, displayWidth, displayHeight);
            }
        });

    }

    _showGadgetOptions() {
        const { width, height } = this.scale;
        const gadgets = [
            { key:'gadget_speed',  value:'SPEED_BOOST',  label:'Speed'  },
            { key:'gadget_damage', value:'DAMAGE_BOOST', label:'Damage' },
            { key:'gadget_health', value:'HEALTH_BOOST', label:'Health' }
        ];
        gadgets.forEach((g,i) => {
            const x = width/2 -300 + i*200, y = height/2;
            const spr = this.add.image(x, y, g.key)
                .setInteractive({useHandCursor:true})
                .setScale(0.7)
                .on('pointerdown', () => {
                    this.selectedGadget = g.value;
                    this.finish();
                });
            spr.tabged = true;
            this.add.text(x, y+80, g.label, {
                fontFamily:'Arial', fontSize:'20px', color:'#fff'
            }).setOrigin(0.5).tabged = true;

            // Gadget-Info aus this.gadgets holen
            const info = this.gadgets.find(item => item.id === g.value);
            if (info) {
                const gadgetTxt = `
                    Cooldown: ${info.cooldown}s
                    Effect:   ${info.effect}
                    `;
                this.add.text(x, y + 110, gadgetTxt.trim(), {
                    fontFamily:'Arial', fontSize:'16px', color:'#00ff00', align: 'center'
                })
                    .setOrigin(0.5, 0)
                    .tabged = true;
            }

            if (g.id === this.selectedGadget) {
                const displayWidth = spr.displayWidth;
                const displayHeight = spr.displayHeight;
                this._drawFrame(x, y, displayWidth, displayHeight);
            }
        });
    }

    async finish() {
        // Auswahl speichern auf Server
        try {
            await fetch(`${API_BASE}/selected`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    playerId: this.playerId,
                    weaponId: this.selectedWeapon,
                    brawlerId: this.selectedBrawler,
                    gadgetId: this.selectedGadget,
                    levelId: this.selectedLevel
                })
            });
        } catch (err) {
            console.error('Save selection error:', err);
        }

        // persist all three selections
        this.registry.set('weapon', this.selectedWeapon);
        this.registry.set('brawler', this.selectedBrawler);
        this.registry.set('gadget', this.selectedGadget);

        // notify lobby and close
        this.scene.get('LobbyScene').events.emit('inventoryDone');
        this.scene.stop();
        this.scene.resume('LobbyScene');
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
        if (this.settingsOpen) return;
        this.settingsOpen = true;

        const vw = this.scale.width;
        const vh = this.scale.height;

        // a) Schwarzes halbtransparentes Overlay, das den ganzen Viewport abdeckt
        const overlay = this.add.rectangle(
            vw / 2,    // in der Mitte des Viewports
            vh / 2,
            vw,        // volle Breite des Viewports
            vh,        // volle Höhe des Viewports
            0x000000   // Farbe: Schwarz
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setAlpha(0.8)     // 80% Opazität → leicht durchsichtig
            .setDepth(1000);   // sehr hoher Depth, damit alles darunter verdeckt ist

        // b) Schwarzes Rechteck in der Mitte für den Settings‐Dialog
        //    Beispiel: halbe Breite, halbe Höhe, zentriert
        const winWidth  = vw * 0.6;
        const winHeight = vh * 0.6;
        const dialogBg = this.add.rectangle(
            vw / 2, vh / 2,
            winWidth, winHeight,
            0x111111   // dunkles Grau/Schwarz
        )
            .setOrigin(0.5)
            .setScrollFactor(0)
            .setDepth(1001)    // eine Stufe über dem Overlay

        // Optional: Rahmen um das Dialogfeld (weiß oder helle Farbe)
        const border = this.add.graphics()
            .lineStyle(4, 0xffffff)  // 4px weißer Rahmen
            .strokeRect(
                (vw - winWidth) / 2,
                (vh - winHeight) / 2,
                winWidth,
                winHeight
            )
            .setScrollFactor(0)
            .setDepth(1002);         // noch eine Stufe höher, damit es über dialogBg liegt

        // c) Titel „Settings“ oben im Dialog
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

        // d) Beispiel‐Checkbox oder Textfeld im Dialog
        //    Du kannst hier beliebige Phaser‐UI‐Elemente oder Textfelder hinzufügen.
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

        // Gadget-Key Eingabefeld
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

        gadgetKeyInput.value = localStorage.getItem('gadgetKey') || 'Q'; // Standard

        document.body.appendChild(gadgetKeyInput);


        // Beispiel‐Button „Close“, um das Fenster zu schließen
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
                    localStorage.setItem('gadgetKey', newKey);        // dauerhaft speichern
                    this.registry.set('gadgetKey', newKey);           // session-übergreifend speichern
                    console.log(`Gadget-Taste gespeichert: ${newKey}`);
                } else {
                    alert('Ungültige Taste! Bitte nur einen Buchstaben A-Z eingeben.');
                }

                // HTML-Input entfernen
                gadgetKeyInput.remove();

                // Phaser-Elemente löschen
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


}
