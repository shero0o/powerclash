// src/scenes/InventoryScene.js
import Phaser from 'phaser';

const API_BASE = 'http://localhost:8092/api/wallet';


export default class InventoryScene extends Phaser.Scene {
    constructor() {
        super({ key: 'InventoryScene' });
        this.playerId = 2;
        this.coins = 0;
        this.brawlers = [];
        this.gadgets = [];
        this.levels = [];
        this.selectedWeapon = null;
        this.selectedBrawler = null;
        this.selectedGadget = null;
        this.selectedLevel = null;
    }

    init(data) {
        this.selectedWeapon  = data.weapon;
        this.selectedBrawler = data.brawler;
        this.selectedGadget  = data.gadget;
        //this.playerId = data.playerId || 1;
    }

    preload() {
        // — Lobby-Assets (Hintergrund, UI) —
        this.load.svg('lobby_bg',      '/assets/svg/lobby_bg.svg');
        this.load.svg('icon_profile',  '/assets/svg/profile-icon.svg', { width:200, height:100 });
        this.load.svg('btn-settings',  '/assets/svg/btn-settings.svg',{ width:200, height:100 });
        this.load.svg('icon_shop',     '/assets/svg/btn-shop.svg',    { width:200, height:80 });
        this.load.svg('icon_coin',     '/assets/svg/coin-icon.svg',   { width:100, height:100 });

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
            const [coinsRes, brawlersRes, gadgetsRes, levelsRes, selectedRes] = await Promise.all([
                fetch(`${API_BASE}/coins?playerId=${this.playerId}`),
                fetch(`${API_BASE}/brawlers`),
                fetch(`${API_BASE}/gadgets`),
                fetch(`${API_BASE}/levels`),
                //fetch(`${API_BASE}/selected?playerId=${this.playerId}`)
            ]);

            this.coins    = await coinsRes.json();
            this.brawlers = await brawlersRes.json();
            this.gadgets  = await gadgetsRes.json();
            this.levels   = await levelsRes.json();
            //const sel     = await selectedRes.json();

           // this.selectedBrawler = sel?.brawlerId || this.brawlers[0]?.id;
           // this.selectedGadget  = sel?.gadgetId  || this.gadgets[0]?.id;
           // this.selectedLevel   = sel?.levelId   || this.levels[0]?.id;
           // this.selectedWeapon  = sel?.weaponId  || this.brawlers.find(b => b.id === this.selectedBrawler)?.defaultWeapon;
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
            .setOrigin(0.5).setScale(0.8)
            .setInteractive({ useHandCursor:true })
            .on('pointerdown', ()=> console.log('Profile öffnen'));

        const settingsBtn = this.add.image(1400, height / 2 - 330, 'btn-settings')
            .setOrigin(0.5)
            .setDisplaySize(140, 70)
            .setInteractive({ useHandCursor: true })
            .setDepth(500);

        settingsBtn.on('pointerdown', () => {
            console.log('Settings öffnen');
        });


        this._createCoinDisplay();

        // — Shop & Back to Lobby —
        const btnShop = this.add.image(90, height / 2 - 100, 'icon_shop')
            .setOrigin(0.5)
            .setDisplaySize(200, 80)
            .setInteractive({ useHandCursor: true });

        btnShop.on('pointerdown', () => {
            this.scene.start('ShopScene');
        });

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
        });
    }

    _showBrawlerOptions() {
        const { width, height } = this.scale;
        const avatars = [
            { key: 'avatar2', value: 'sniper' },
            { key: 'avatar3', value: 'tank' },
            { key: 'avatar4', value: 'mage' },
            { key: 'avatar5', value: 'healer' }
        ];
        avatars.forEach((entry,i) => {
            const x = width/2 -300 + i*200, y = height/2;
            const spr = this.add.image(x, y, entry.key)
                .setInteractive({useHandCursor:true})
                .setScale(0.4)
                .on('pointerdown', () => {
                    this.selectedBrawler = entry.value;  // <-- jetzt 'mage', 'tank', etc.
                    this.finish();
                });
            spr.tabged = true;
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
        });
    }

    finish() {
        // persist all three selections
        this.registry.set('weapon',  this.selectedWeapon);
        this.registry.set('brawler', this.selectedBrawler);
        this.registry.set('gadget',  this.selectedGadget);

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

}
