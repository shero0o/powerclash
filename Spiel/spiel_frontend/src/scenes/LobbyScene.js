// src/scenes/LobbyScene.js
import Phaser from 'phaser';

const API_BASE = 'http://localhost:8092/api/wallet';

export default class LobbyScene extends Phaser.Scene {
    constructor() {
        super({ key: 'LobbyScene' });

        // PLAY-Button
        this.playButton = null;

        // Level-Dropdown und aktuell gewählter Level
        this.levelDropdown = null;
        this.selectedLevel = 'level1'; // Default Level

        this.selectedGadget = 'DAMAGE_BOOST'
        // Default-Werte für Waffe und Brawler (wie in SelectionScene)
        this.selectedWeapon  = 'RIFLE_BULLET';
        this.selectedBrawler = 'sniper';

        // Label über dem PLAY-Button ("Level: X")
        this.levelLabel = null;

        // Münzanzeige (Coin-Icon + schwarzes Rechteck + Text)
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

    preload() {
        // 1) SVGs laden
        this.load.svg('lobby_bg', '/assets/svg/lobby_bg.svg');
        this.load.svg('btn_play', '/assets/svg/button-play.svg', { width: 400, height: 160 });
        this.load.svg('icon_shop', '/assets/svg/btn-shop.svg', { width: 200, height: 80 });
        this.load.svg('icon_brawlers', '/assets/svg/btn-brawlers.svg', { width: 200, height: 80 });
        this.load.svg('icon_profile', '/assets/svg/profile-icon.svg', { width: 200, height: 100 });

        // Statt "icon_currency" laden wir nur das Coin-Symbol selbst
        this.load.svg('icon_coin', '/assets/svg/coin-icon.svg', { width: 100, height: 100 });
        this.load.svg("btn-settings", "/assets/svg/btn-settings.svg", { width: 200, height: 100 });

        // 2) Avatar-PNG laden
        //this.load.image('avatar', '/assets/PNG/Characters/Character1.png');
        this.load.image('avatar2', '/assets/PNG/Characters/Character2.png');
        this.load.image('avatar3', '/assets/PNG/Characters/Character3.png');
        this.load.image('avatar4', '/assets/PNG/Characters/Character4.png');
        this.load.image('avatar5', '/assets/PNG/Characters/Character5.png');

        // Waffen
        this.load.image('weapon_rifle',   '/assets/PNG/Weapons/Mashinegun.png');
        this.load.image('weapon_sniper',  '/assets/PNG/Weapons/Sniper.png');
        this.load.image('weapon_shotgun', '/assets/PNG/Weapons/Shotgun.png');
        this.load.image('weapon_mine',    '/assets/PNG/Weapons/weapon_bomb.png');

        this.load.svg("exitButtonSvg", "assets/svg/btn-exit.svg", { width: 190, height: 90 })

        this.load.svg("gadget_health", "assets/svg/healthGadget.svg", { width: 400, height: 200 });
        this.load.svg("gadget_damage", "assets/svg/damageGadget.svg", { width: 400, height: 200 });
        this.load.svg("gadget_speed", "assets/svg/speedGadget.svg", { width: 400, height: 200 });

        this.load.image("mashineGun", "assets/PNG/Weapons/Mashinegun.png")


    }

    init() {
        this.playerId   = this.registry.get('playerId')   || 1;
        this.playerName = this.registry.get('playerName') || 'Player';
    }

    create() {
        const { width, height } = this.scale;

        // --- Hintergrund ---
        this.add.image(width / 2, height / 2, 'lobby_bg')
            .setOrigin(0.5)
            .setDisplaySize(width, height);

        // --- Avatar + Kreis/Schatten ---
        // --- Dynamisches Brawler‐Icon ---
        this.brawlerIcon = this.add.image(width/2, height/2 + 140, this._mapBrawlerKey())
            .setOrigin(0.5)
            .setDisplaySize(400, 600);

        // Schatten-Kreis (leicht weiches Schwarz hinter dem Avatar)

        // Haupt-Kreis (nur Kontur)

        // --- Profil-Icon oben links ---
        this.add.image(60, 60, 'icon_profile')
            .setOrigin(0.5)
            .setScale(0.8)
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => {
                this.scene.start('AccountScene');
            });;

        // --- Münzanzeige (Coin-Symbol + Box + Zahl) oben mittig ---
        this._createCoinDisplay();

        // --- Shop-Button (links) ---
        const btnShop = this.add.image(90, height / 2 - 100, 'icon_shop')
            .setOrigin(0.5)
            .setDisplaySize(200, 80)
            .setInteractive({ useHandCursor: true });

        btnShop.on('pointerdown', () => {
            this.scene.start('ShopScene');
        });


        const settingsBtn = this.add.image(1400, height / 2 - 330, 'btn-settings')
            .setOrigin(0.5)
            .setDisplaySize(140, 70)
            .setInteractive({ useHandCursor: true })
            .setAlpha(1)
            .setDepth(1000);

        settingsBtn.on('pointerdown', () => {
            this.openSettingsWindow();
        });

        // --- Brawlers-Button (links darunter) ---
        const btnBrawlers = this.add.image(90, height / 2, 'icon_brawlers')
            .setOrigin(0.5)
            .setDisplaySize(200, 80)
            .setInteractive({ useHandCursor: true });
        btnBrawlers.on('pointerdown', () => console.log('Brawlers öffnen'));

        // --- Name-Text + Weapon/Gadget-Box (mittig oben) ---
        let currentGadgetKey = 'damageGadget';
        if (this.selectedGadget === "HEALTH_BOOST") {
            currentGadgetKey = "healthGadget";
        } else if (this.selectedGadget === "SPEED_BOOST") {
            currentGadgetKey = "speedGadget";
        }else{currentGadgetKey = "damageGadget"}

        this.add.text(width / 2, 165, this.playerName, {
            fontFamily: 'Arial',
            fontSize: '28px',
            color: '#ffffff',
            stroke: '#000000',
            strokeThickness: 8,
            shadow: { offsetX: 4, offsetY: 4, color: '#000000', blur: 0, stroke: false, fill: false },
            resolution: 2
        }).setOrigin(0.5);

        this.add.rectangle(width / 2 - 45, 220, 90, 55, 0x000000, 0.5)
            .setStrokeStyle(2, 0xffffff)
            .setOrigin(0.5);
        const weaponKey = this._mapWeaponKey();
        const weaponX = width / 2 - 40;
        const weaponY = 220;

        this.weaponIcon = this.add.image(weaponX, weaponY, weaponKey)
            .setOrigin(0.5)
            .setDisplaySize(60, 60)
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => {
                    this.scene.pause();
                this.scene.launch('InventoryScene', {
                        weapon: this.selectedWeapon,
                        brawler: this.selectedBrawler,
                        gadget: this.selectedGadget
                });
            });
            ;

        if (this.selectedWeapon === 'MINE') {
            this.weaponIcon.setDisplaySize(60, 60); // oder 90×90 je nach Bildgröße
        } else {
            this.weaponIcon.setDisplaySize(60, 60);
        }

        this.add.rectangle(width / 2 + 45, 220, 90, 55, 0x000000, 0.5)
            .setStrokeStyle(2, 0xffff00)
            .setOrigin(0.5);
        this.gadgetIcon = this.add.image(width/2 + 40, 220, this._mapGadgetKey())
            .setOrigin(0.5)
            .setDisplaySize(100, 50)
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => {
                this.scene.pause();
                this.scene.launch('InventoryScene', {
                        weapon: this.selectedWeapon,
                        brawler: this.selectedBrawler,
                        gadget: this.selectedGadget
                });
            });    
        ;

        // --- Großer PLAY-Button unten rechts ---
        this.playButton = this.add.image(width - 180, height - 110, 'btn_play')
            .setOrigin(0.5)
            .setScale(0.8)
            .setInteractive({ useHandCursor: true });

        this.playButton.on('pointerover', () => {
            // Mit tint abdunkeln (z.B. 0x999999 ist ein dunkleres Grau)
            this.playButton.setTint(0x999999);
            // ODER: stattdessen alpha ändern
            // this.playButton.setAlpha(0.8);
        });

// Wenn Maus wieder weg: Ursprungszustand zurücksetzen
        this.playButton.on('pointerout', () => {
            // Entfernt den Tint, Original-Farbe wird wiederhergestellt
            this.playButton.clearTint();
            // ODER: wenn alpha verwendet wurde:
            // this.playButton.setAlpha(1);
        });

        // Klick auf PLAY = Daten an Server senden + Wechsel zu WaitingScene
        this.playButton.on('pointerdown', () => this.onPlayClicked());

        // --- Neuer, kleiner LEVEL-Label-Button direkt über PLAY ---
        this._createLevelLabel(width, height);
        btnBrawlers.on('pointerdown', () => {
            // pause lobby so it doesn’t render underneath
            this.scene.pause();
            // pass current selections into InventoryScene
            this.scene.launch('InventoryScene', {
                weapon: this.selectedWeapon,
                brawler: this.selectedBrawler,
                gadget: this.selectedGadget
            });
        });
        this.events.on('inventoryDone', this.onInventoryDone, this);


    }

    /**
     * Erstellt den Coin-Display-Bereich:
     *   - Coin-Icon an (coinX, coinY)
     *   - Schwarzes, abgerundetes Rechteck daneben
     *   - Weißen Text mittig über das Rechteck
     */
    _mapBrawlerKey() {
        switch (this.selectedBrawler) {
                       case 'mage':   return 'avatar3'; // Soldier → Character3
                           case 'healer': return 'avatar4'; // WomanGreen → Character4
                           case 'tank':   return 'avatar5'; // Robot → Character5
                           default:       return 'avatar2'; // Hitman → Character2
                       }
    }

    _mapWeaponKey() {
        switch (this.selectedWeapon) {
            case 'SNIPER':         return 'weapon_sniper';
            case 'SHOTGUN_PELLET': return 'weapon_shotgun';
            case 'MINE':           return 'weapon_mine';
            default:               return 'weapon_rifle';
        }
    }

    _mapGadgetKey() {
        switch (this.selectedGadget) {
            case 'SPEED_BOOST':  return 'gadget_speed';
            case 'HEALTH_BOOST': return 'gadget_health';
            default:             return 'gadget_damage';
        }
    }

    // ─── Inventory‐done callback ─────────────────────────────────────────────
    onInventoryDone() {
        // pull back everything from the registry
        this.selectedBrawler = this.registry.get('brawler');
        this.selectedWeapon  = this.registry.get('weapon');
        this.selectedGadget  = this.registry.get('gadget');

        // update textures:
        this.brawlerIcon.setTexture(this._mapBrawlerKey());
        this.weaponIcon .setTexture(this._mapWeaponKey());
        this.gadgetIcon .setTexture(this._mapGadgetKey());

        if (this.selectedWeapon === 'MINE') {
            this.weaponIcon.setDisplaySize(60, 60);
        } else {
            this.weaponIcon.setDisplaySize(60, 60);
        }
    }
    async _createCoinDisplay() {
        const {coinX, coinY, coinSize, rectWidth, rectHeight, cornerRadius, strokeWidth} = this;

        // 1) Coin-Icon
        const coinImage = this.add.image(coinX, coinY, 'icon_coin')
            .setOrigin(0, 0.5)
            .setDisplaySize(coinSize, coinSize);

        // 2) Schwarzes Rechteck
        this.rectX = coinX + coinSize - 12;
        this.rectY = coinY - rectHeight / 2;
        this.graphics = this.add.graphics();
        this.graphics.fillStyle(0x000000, 1);
        this.graphics.fillRoundedRect(this.rectX, this.rectY, rectWidth, rectHeight, cornerRadius);
        this.graphics.lineStyle(strokeWidth, 0x000000, 1);
        this.graphics.strokeRoundedRect(this.rectX, this.rectY, rectWidth, rectHeight, cornerRadius);

        // 3) Text mit initialem Münzwert
        const coins = await fetch(`${API_BASE}/coins?playerId=${this.playerId}`)
            .then(r => r.json());
        this.coinText = this.add.text(
            this.rectX + rectWidth/2,
            this.rectY + rectHeight/2,
            `${coins}`,
            { fontFamily: 'Arial', fontSize: '28px', color: '#ffffff', stroke: '#000000', strokeThickness: 4 }
        ).setOrigin(0.5);

        // 4) Container mit allen Elementen
        this.coinContainer = this.add.container(0, 0, [
            this.graphics,
            this.coinText,
            coinImage
        ]);
    }

    /**
     * Erstellt den kleinen Level-Label-Button direkt über dem PLAY-Button
     * samt Interaktion, um das Dropdown zu öffnen.
     */
    _createLevelLabel() {
        // Position direkt oberhalb des PLAY-Buttons
        const yOffset    = 57;                   // Abstand über PLAY
        const levelBtnX  = this.playButton.x;    // gleiche X-Koordinate
        const levelBtnY  = this.playButton.y - yOffset;

        // a) Rechteck (gelb mit schwarzer Umrandung)
        const levelBtnWidth  = 150;
        const levelBtnHeight = 40;
        const fillColor      = 0xF7C500;  // gelb
        const strokeColor    = 0x000000;  // schwarz
        const strokeW        = 3;
        const cornerRad      = 8;

        const lvlGraphics = this.add.graphics();
        lvlGraphics.fillStyle(fillColor, 1);
        lvlGraphics.lineStyle(strokeW, strokeColor, 1);
        lvlGraphics.fillRoundedRect(
            levelBtnX - levelBtnWidth / 2,
            levelBtnY - levelBtnHeight / 2,
            levelBtnWidth,
            levelBtnHeight,
            cornerRad
        );
        lvlGraphics.strokeRoundedRect(
            levelBtnX - levelBtnWidth / 2,
            levelBtnY - levelBtnHeight / 2,
            levelBtnWidth,
            levelBtnHeight,
            cornerRad
        );

        // b) Text „Level: X“ (startet mit Default 'level1' → Anzeige "Level: level1")
        const labelStyle = {
            fontFamily: 'Arial Black',
            fontSize: '20px',
            color: '#ffffff',
            stroke: '#000000',
            strokeThickness: 3,
            shadow: { offsetX: 2, offsetY: 2, color: '#000000', blur: 0, stroke: false, fill: false },
            resolution: 2
        };
        const numberOnly = this.selectedLevel.replace(/^level/, '');
        this.levelLabel = this.add.text(levelBtnX, levelBtnY, `Level: ${numberOnly}`, labelStyle)
            .setOrigin(0.5);

        // c) Unsichtbare Hitbox über dem Label
        const labelHitbox = this.add.rectangle(
            levelBtnX,
            levelBtnY,
            levelBtnWidth,
            levelBtnHeight,
            0x000000,
            0 // komplett transparent
        );
        labelHitbox.setOrigin(0.5);
        labelHitbox.setInteractive({ useHandCursor: true });
        labelHitbox.on('pointerdown', () => {
            this.toggleLevelDropdown();
        });

        // Sicherstellen, dass die Hitbox und der Text über dem Rechteck liegen:
        labelHitbox.setDepth(lvlGraphics.depth + 1);
        this.levelLabel.setDepth(lvlGraphics.depth + 1);

        // d) Dropdown-Menü erstellen (unsichtbar), direkt über dem Label
        const dropdownX = levelBtnX - levelBtnWidth / 2;
        const dropdownY = levelBtnY - levelBtnHeight / 2 - 140; // 140px Höhe für 3 Einträge
        this.createLevelDropdown(dropdownX, dropdownY);
    }

    /**
     * Erstellt das Dropdown-Menü mit den drei Level-Auswahlmöglichkeiten.
     * Das Menü ist in einem Container und startet als invisible.
     * x/y sind die Koordinaten (oben links) des Dropdown-Rechtecks.
     */
    createLevelDropdown(x, y) {
        this.levelDropdown = this.add.container(x, y).setVisible(false);

        // Größe des Dropdowns (Breite, Höhe)
        const boxWidth  = 200;
        const boxHeight = 140; // Platz für 3 Einträge à ca. 40px + Puffer

        // a) Halbtransparenter schwarzer Hintergrund
        const bgRect = this.add.rectangle(0, 0, boxWidth, boxHeight, 0x000000, 0.8)
            .setOrigin(0);

        // b) Texte „Level 1“, „Level 2“, „Level 3“
        const textStyle = { fontFamily: 'Arial', fontSize: '18px', color: '#ffffff' };
        const lineHeight = 40;

        const lvl1 = this.add.text(10, 10 + 0 * lineHeight, 'Level 1', textStyle)
            .setInteractive({ useHandCursor: true });
        const lvl2 = this.add.text(10, 10 + 1 * lineHeight, 'Level 2', textStyle)
            .setInteractive({ useHandCursor: true });
        const lvl3 = this.add.text(10, 10 + 2 * lineHeight, 'Level 3', textStyle)
            .setInteractive({ useHandCursor: true });

        // c) Klick-Handler: Wenn man einen Eintrag klickt, speichert er selectedLevel,
        //    aktualisiert den Text oben („Level: X“) und blendet das Menü aus.
        lvl1.on('pointerdown', () => this.selectLevel('level1'));
        lvl2.on('pointerdown', () => this.selectLevel('level2'));
        lvl3.on('pointerdown', () => this.selectLevel('level3'));

        this.levelDropdown.add([ bgRect, lvl1, lvl2, lvl3 ]);
    }

    /**
     * Zeigt oder versteckt das Level-Dropdown.
     */
    toggleLevelDropdown() {
        if (!this.levelDropdown) return;
        this.levelDropdown.setVisible(!this.levelDropdown.visible);
    }

    /**
     * Wird aufgerufen, wenn ein Level (Level 1/2/3) aus dem Dropdown gewählt wurde.
     * - Speichert die Wahl in this.selectedLevel,
     * - aktualisiert den Label-Text oben,
     * - blendet das Dropdown wieder aus.
     */
    selectLevel(levelId) {
        this.selectedLevel = levelId;
        const numberOnly = levelId.replace(/^level/, '');
        this.levelLabel.setText(`Level: ${numberOnly}`);
        this.levelDropdown.setVisible(false);
        this.registry.set('levelId', levelId);
    }

    /**
     * Wenn auf PLAY geklickt wird:
     * - Liest den Spielernamen aus dem Input (Default „Player“),
     * - Speichert playerId, playerName, selectedWeapon, selectedBrawler, selectedLevel
     *   im LocalStorage / Registry und sendet sie an den Server,
     * - Wechselt dann in WaitingScene.
     */
    onPlayClicked() {
        // 1) PlayerID generieren/fetchen
        const playerId = localStorage.getItem('playerId') || crypto.randomUUID();
        localStorage.setItem('playerId', playerId);

        // 2) PlayerName aus HTML-Input lesen (oder „Player“, falls leer)
        const enteredName = this.nameInput?.value.trim() || 'Player';
        localStorage.setItem('playerName', enteredName);

        // 3) Default-Waffen und Brawler verarbeiten (stehen schon in this.selectedWeapon, this.selectedBrawler)
        //    – hier nichts ändern, wir lesen gleich aus der Registry

        // 4) Gewählten Level in Registry/LocalStorage schreiben
        //    (wurde schon über selectLevel gesetzt)
        const levelToSend = this.selectedLevel || 'level1';
        localStorage.setItem('chosenMapLevel', levelToSend);

        // 5) Registry speichern (damit WaitingScene das auslesen kann)
        this.registry.set('playerName', enteredName);
        this.registry.set('playerId', playerId);
        this.registry.set('levelId', levelToSend);
        this.registry.set('weapon', this.selectedWeapon);
        this.registry.set('brawler', this.selectedBrawler);
        this.registry.set('gadget', this.selectedGadget);



        // 6) Socket-Emit an den Spiel-Server mit allen Auswahlparametern
        this.socket.connect();
            this.socket.once('connect', () => {
            this.socket.emit('joinRoom', {
                playerId,
                playerName:   enteredName,
                brawlerId:    this.selectedBrawler,
                levelId:      levelToSend,
                chosenWeapon: this.selectedWeapon,
                chosenGadget: this.selectedGadget
            }, (response) => {
                // Raum-ID zurück in die Registry schreiben …
                this.registry.set('roomId', response.roomId);
                // … und dann in die Warteschleife wechseln
                this.scene.start('WaitingScene');
            });
        });
        this.socket.once('connect_error', err => {
            console.error('[LobbyScene] Socket connect_error', err);
            alert('Verbindung fehlgeschlagen!');
        });

        console.log('JoinRoom:', {
            playerId,
            playerName:   enteredName,
            brawlerId:    this.selectedBrawler,
            levelId:      levelToSend,
            chosenWeapon: this.selectedWeapon,
            chosenGadget: this.selectedGadget
        });

        // 7) HTML-Input entfernen
        if (this.nameInput && this.nameInput.parentNode) {
            this.nameInput.parentNode.removeChild(this.nameInput);
        }

        // 8) Szene wechseln (wird durch das Callback der socket.emit ohnehin gemacht,
        //    aber falls kein Callback mehr kommen sollte, sorgen wir hier nochmal vor)
        // this.scene.start('WaitingScene');
    }


    shutdown() {
        if (this.nameInput && this.nameInput.parentNode) {
            this.nameInput.parentNode.removeChild(this.nameInput);
        }
    }

    /**
     * Optional: Diese Methode kannst du später aufrufen, um die Münzanzeige zu aktualisieren.
     * Beispiel: this.updateCoinValue(1234);
     */
    updateCoinValue(newCoins) {
        // 1) Text austauschen
        this.coinText.setText(`${newCoins}`);

        // 2) Neue Breite berechnen: Textbreite + 20px Padding
        const metrics = this.coinText.getTextMetrics();
        const newTextWidth = metrics.local.width;
        const desiredWidth = newTextWidth + 20; // 10px Padding links/rechts

        // 3) Grafik neu zeichnen: clear + fill + stroke
        this.graphics.clear();
        this.graphics.fillStyle(0x000000, 1);
        this.graphics.fillRoundedRect(this.rectX, this.rectY, desiredWidth, this.rectHeight, this.cornerRadius);
        this.graphics.lineStyle(this.strokeWidth, 0x000000, 1);
        this.graphics.strokeRoundedRect(this.rectX, this.rectY, desiredWidth, this.rectHeight, this.cornerRadius);

        // 4) Text horizontal neu zentrieren
        const newTextX = this.rectX + desiredWidth / 2;
        this.coinText.setX(newTextX);
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
}
