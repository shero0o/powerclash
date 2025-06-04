// src/scenes/LobbyScene.js
import Phaser from 'phaser';

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
        this.load.image('avatar', '/assets/PNG/avatar/avatar.png');
    }

    create() {
        const { width, height } = this.scale;

        // --- Hintergrund ---
        this.add.image(width / 2, height / 2, 'lobby_bg')
            .setOrigin(0.5)
            .setDisplaySize(width, height);

        // --- Avatar + Kreis/Schatten ---
        const avatarSprite = this.add.image(width / 2, height / 2 - 20, 'avatar')
            .setOrigin(0.5)
            .setDisplaySize(200, 200);

        const kreisRadius = 110;
        // Schatten-Kreis (leicht weiches Schwarz hinter dem Avatar)
        this.add.circle(avatarSprite.x, avatarSprite.y, kreisRadius, 0x000000, 0.2);

        // Haupt-Kreis (nur Kontur)
        this.add.circle(avatarSprite.x, avatarSprite.y, kreisRadius)
            .setFillStyle(0x000000, 0)
            .setStrokeStyle(4, 0x000000);

        // --- Profil-Icon oben links ---
        this.add.image(60, 60, 'icon_profile')
            .setOrigin(0.5)
            .setScale(0.8);

        // --- Münzanzeige (Coin-Symbol + Box + Zahl) oben mittig ---
        this._createCoinDisplay();

        // --- Shop-Button (links) ---
        const btnShop = this.add.image(90, height / 2 - 100, 'icon_shop')
            .setOrigin(0.5)
            .setDisplaySize(200, 80)
            .setInteractive({ useHandCursor: true });
        btnShop.on('pointerdown', () => console.log('Shop öffnen'));

        this.add.image(1400, height / 2 - 330, 'btn-settings')
            .setOrigin(0.5)
            .setDisplaySize(140, 70)
            .setInteractive({ useHandCursor: true });

        // --- Brawlers-Button (links darunter) ---
        const btnBrawlers = this.add.image(90, height / 2, 'icon_brawlers')
            .setOrigin(0.5)
            .setDisplaySize(200, 80)
            .setInteractive({ useHandCursor: true });
        btnBrawlers.on('pointerdown', () => console.log('Brawlers öffnen'));

        // --- Name-Text + Weapon/Gadget-Box (mittig oben) ---
        this.add.text(width / 2, 185, 'Player', {
            fontFamily: 'Arial',
            fontSize: '28px',
            color: '#ffffff',
            stroke: '#000000',
            strokeThickness: 8,
            shadow: { offsetX: 4, offsetY: 4, color: '#000000', blur: 0, stroke: false, fill: false },
            resolution: 2
        }).setOrigin(0.5);

        this.add.rectangle(width / 2 - 40, 220, 80, 30, 0x000000, 0.5)
            .setStrokeStyle(2, 0xffffff)
            .setOrigin(0.5);
        this.add.text(width / 2 - 40, 220, 'Weapon', {
            fontFamily: 'Arial',
            fontSize: '20px',
            color: '#ffffff'
        }).setOrigin(0.5);

        this.add.rectangle(width / 2 + 40, 220, 80, 30, 0x000000, 0.5)
            .setStrokeStyle(2, 0xffff00)
            .setOrigin(0.5);
        this.add.text(width / 2 + 40, 220, 'Gadget', {
            fontFamily: 'Arial',
            fontSize: '20px',
            color: '#ffff00'
        }).setOrigin(0.5);

        // --- Großer PLAY-Button unten rechts ---
        this.playButton = this.add.image(width - 180, height - 110, 'btn_play')
            .setOrigin(0.5)
            .setScale(0.8)
            .setInteractive({ useHandCursor: true });
        // Klick auf PLAY = Daten an Server senden + Wechsel zu WaitingScene
        this.playButton.on('pointerdown', () => this.onPlayClicked());

        // --- Neuer, kleiner LEVEL-Label-Button direkt über PLAY ---
        this._createLevelLabel(width, height);

    }

    /**
     * Erstellt den Coin-Display-Bereich:
     *   - Coin-Icon an (coinX, coinY)
     *   - Schwarzes, abgerundetes Rechteck daneben
     *   - Weißen Text mittig über das Rechteck
     */
    _createCoinDisplay() {
        const { coinX, coinY, coinSize, rectHeight, cornerRadius, strokeWidth } = this;

        // 1) Coin-Icon
        const coinImage = this.add.image(coinX, coinY, 'icon_coin')
            .setOrigin(0, 0.5)
            .setDisplaySize(coinSize, coinSize);

        // 2) Schwarzes Rechteck daneben
        this.rectX = coinX + coinSize -12;
        this.rectY = coinY - rectHeight / 2;

        this.graphics = this.add.graphics();
        this.graphics.fillStyle(0x000000, 1);
        this.graphics.fillRoundedRect(
            this.rectX,
            this.rectY,
            this.rectWidth,
            this.rectHeight,
            cornerRadius
        );
        this.graphics.lineStyle(strokeWidth, 0x000000, 1);
        this.graphics.strokeRoundedRect(
            this.rectX,
            this.rectY,
            this.rectWidth,
            this.rectHeight,
            cornerRadius
        );

        // 3) Text „596“ (Startwert)
        const initialCoins = 596;
        const textStyle = {
            fontFamily: 'Arial, sans-serif',
            fontSize: '28px',
            color: '#ffffff',
            stroke: '#000000',
            strokeThickness: 4,
            align: 'center',
            resolution: 2
        };
        const textX = this.rectX + this.rectWidth / 2;
        const textY = this.rectY + this.rectHeight / 2;
        this.coinText = this.add.text(textX, textY, `${initialCoins}`, textStyle)
            .setOrigin(0.5);

        // 4) Container (optional), um all diese Elemente zusammenzuhalten
        this.coinContainer = this.add.container(0, 0, [ this.graphics, this.coinText, coinImage ]);
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
            fontFamily: 'Arial',
            fontSize: '20px',
            color: '#ffffff',
            stroke: '#000000',
            strokeThickness: 3,
            shadow: { offsetX: 2, offsetY: 2, color: '#000000', blur: 0, stroke: false, fill: false },
            resolution: 2
        };
        this.levelLabel = this.add.text(levelBtnX, levelBtnY, `Level: ${this.selectedLevel}`, labelStyle)
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
        this.levelLabel.setText(`Level: ${levelId}`);
        this.levelDropdown.setVisible(false);
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
        //    Hier könnte man bei Bedarf später erweitern, wenn du auch Waffen/Brawler im Lobby wählen möchtest.

        // 4) Gewählten Level in Registry/LocalStorage schreiben
        const levelToSend = this.selectedLevel || 'level1';
        localStorage.setItem('chosenMapLevel', levelToSend);

        // 5) Registry speichern (damit WaitingScene das auslesen kann)
        this.registry.set('playerName', enteredName);
        this.registry.set('playerId', playerId);
        this.registry.set('levelId', levelToSend);
        this.registry.set('weapon', this.selectedWeapon);
        this.registry.set('brawler', this.selectedBrawler);
        this.registry.set("gadget", this.selectedGadget);

        //6) Wenn du einen Socket‐Emit brauchst (wie in SelectionScene), z.B.:
           this.socket.emit('joinRoom', { playerId, brawlerId: this.selectedBrawler, levelId: levelToSend, chosenWeapon: this.selectedWeapon, playerName: enteredName, chosenGadget: this.selectedGadget }, (response) => {
               this.registry.set('roomId', response.roomId);
               this.scene.start('WaitingScene');
           });
        //
        //    Falls du das Socket-Protokoll genauso wie vorher möchtest, dekodiere es hier.
        //    Für dieses Beispiel starten wir jedoch direkt WaitingScene:

        console.log('JoinRoom: ', {
            playerId,
            playerName: enteredName,
            brawlerId: this.selectedBrawler,
            levelId: levelToSend,
            chosenWeapon: this.selectedWeapon,
            chosenGadget: this.selectedGadget
        });

        // 7) HTML-Input entfernen
        if (this.nameInput && this.nameInput.parentNode) {
            this.nameInput.parentNode.removeChild(this.nameInput);
        }

        // 8) Szene wechseln
        this.scene.start('WaitingScene');
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
}