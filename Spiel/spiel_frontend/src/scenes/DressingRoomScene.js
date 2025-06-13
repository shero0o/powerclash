import Phaser from 'phaser';

const API_BASE = 'http://localhost:8092/api/wallet';

export default class DressingRoomScene extends Phaser.Scene {
    constructor() {
        super({ key: 'DressingRoomScene' });
        this.playerId = null;
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
        // Für Testzwecke ist playerId = 1
        this.playerId = data.playerId || 1;
        console.log('[DressingRoomScene] init with playerId=', this.playerId);
    }

    async create() {
        const { width, height } = this.scale;
        this.cameras.main.setBackgroundColor('#1d1d1d');
        console.log('[DressingRoomScene] create() start');

        // ─── Seed Wallet via Backend-POSTs (playerId=1) ─────────────────


        // ─── Daten via GET laden ─────────────────────────────────────────
        try {
            const [coinsRes, brawlersRes, gadgetsRes, levelsRes, selectedRes] = await Promise.all([
                fetch(`${API_BASE}/coins?playerId=${this.playerId}`),
                fetch(`${API_BASE}/brawlers`),
                fetch(`${API_BASE}/gadgets`),
                fetch(`${API_BASE}/levels`),
                fetch(`${API_BASE}/selected?playerId=${this.playerId}`)
            ]);

            this.coins    = await coinsRes.json();
            this.brawlers = await brawlersRes.json();
            this.gadgets  = await gadgetsRes.json();
            this.levels   = await levelsRes.json();
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

        // ─── UI-Aufbau (immer) ────────────────────────────────────────────
        // Coins oben rechts
        this.add.image(width - 100, 50, 'coin').setScale(0.5);
        this.add.text(width - 70, 40, this.coins.toString(), { fontSize: '24px', fill: '#ffff00' });

        // Back-Button
        this.add.text(20, height - 40, '← Hauptmenü', { fontSize: '24px', fill: '#ff4444' })
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => this.scene.start('LobbyScene'));

        // Option-Listen
        this.createOptions('Brawler', this.brawlers, this.selectedBrawler, 50, 100, item => this.selectedBrawler = item.id);
        this.createOptions('Gadget',  this.gadgets,  this.selectedGadget,  50, 250, item => this.selectedGadget  = item.id);
        this.createOptions('Level',   this.levels,   this.selectedLevel,   width/2 - 50, 100, item => this.selectedLevel   = item.id);
        this.createOptions('Weapon', [
            ...new Set(this.brawlers.map(b => b.defaultWeapon))
        ].map(w => ({ id: w, name: w })), this.selectedWeapon, width/2 + 50, 100, item => this.selectedWeapon  = item.id);

        // Select-Button
        this.add.text(width - 150, height - 40, 'Auswählen', { fontSize: '24px', fill: '#00ff00' })
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', async () => {
                try {
                    await Promise.all([
                        fetch(`${API_BASE}/selected/brawler?playerId=${this.playerId}&brawlerId=${this.selectedBrawler}`, { method: 'POST' }),
                        fetch(`${API_BASE}/selected/gadget?playerId=${this.playerId}&gadgetId=${this.selectedGadget}`,   { method: 'POST' }),
                        fetch(`${API_BASE}/selected/level?playerId=${this.playerId}&levelId=${this.selectedLevel}`,       { method: 'POST' }),
                        fetch(`${API_BASE}/selected/weapon?playerId=${this.playerId}&weaponId=${this.selectedWeapon}`,    { method: 'POST' })
                    ]);
                    this.scene.start('GameScene');
                } catch (err) {
                    console.error('Save-Error:', err);
                }
            });
    }

    createOptions(label, items, selectedId, startX, startY, onSelect) {
        this.add.text(startX, startY - 30, `Wähle ${label}:`, { fontSize: '20px', fill: '#ffffff' });
        items.forEach((item, i) => {
            const y = startY + i * 40;
            const txt = this.add.text(startX, y, item.name || item.label || item.id, {
                fontSize: '18px',
                fill: item.id === selectedId ? '#ffff00' : '#888888'
            }).setInteractive({ useHandCursor: true });
            txt.on('pointerdown', () => {
                onSelect(item);
                this.children.list.filter(c => c.text).forEach(c => {
                    if (c.text === (item.name || item.label || item.id)) c.setColor('#ffff00');
                    else if (items.some(it => it.name === c.text || it.label === c.text || it.id === c.text)) c.setColor('#888888');
                });
            });
        });
    }
}
