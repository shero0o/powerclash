// import Phaser from 'phaser';
//
// export default class SelectionScene extends Phaser.Scene {
//     constructor() {
//         super({ key: 'SelectionScene' });
//         this.selectedWeapon = 'RIFLE_BULLET';
//         this.selectedLevel  = 'level1';
//         this.selectedGadget = 'DAMAGE_BOOST'
//         this.selectedLevel = 'level1';
//         this.selectedBrawler = 'sniper';
//         this.playerName = '';
//         this.nameInput = null;
//     }
//
//     create() {
//         const {width, height} = this.scale;
//
//         // Entferne ggf. alte Inputs (bei Refresh oder erneutem Eintritt)
//         Array.from(document.querySelectorAll('input[type="text"]')).forEach(el => el.remove());
//
//         // Eingabefeld für Namen
//         // Eingabefeld für Namen
//         const nameInput = document.createElement('input');
//         nameInput.type = 'text';
//         nameInput.maxLength = 12;
//         nameInput.placeholder = 'Your name';
//         nameInput.style.position = 'absolute';
//         nameInput.style.top = '5%';
//         nameInput.style.left = '50%';
//         nameInput.style.transform = 'translateX(-50%)';
//         nameInput.style.zIndex = '1000';
//         nameInput.style.fontSize = '2vh';        nameInput.autofocus = true;
//         document.body.appendChild(nameInput);
//         this.nameInput = nameInput;
//
//         this.add.text(width / 2, 10, 'Enter Name:', {
//             fontSize: '20px',
//             fill: '#ffffff'
//         }).setOrigin(0.5);
//
//         const weapons = [
//             { label: 'Rifle', value: 'RIFLE_BULLET' },
//             { label: 'Sniper', value: 'SNIPER' },
//             { label: 'Shotgun', value: 'SHOTGUN_PELLET' },
//             { label: 'Mine', value: 'MINE' }
//         ];
//
//         const gadgets = [
//             {label: 'Damage Boost', value: 'DAMAGE_BOOST'},
//             {label: 'HP Boost', value: 'HP_BOOST'},
//             {label: 'Speed Boost', value: 'SPEED_BOOST'}
//         ]
//
//
//         const levels = ['level1', 'level2', 'level3'];
//
//         const brawlers = [
//             { label: 'Sniper', value: 'sniper' },
//             { label: 'Tank', value: 'tank' },
//             { label: 'Mage', value: 'mage' },
//             { label: 'Healer', value: 'healer' }
//         ];
//
//         this.weaponTexts = [];
//         this.levelTexts = [];
//         this.brawlerTexts = [];
//         this.gadgetTexts = [];
//
//         // Weapon Auswahl
//         this.add.text(50, 30, 'Choose Weapon:', { fontSize: '24px', fill: '#ffffff' });
//         weapons.forEach((w, i) => {
//             const txt = this.add.text(50, 70 + i * 30, w.label, {
//                 fontSize: '20px',
//                 fill: w.value === this.selectedWeapon ? '#ffff00' : '#00ff00'
//             }).setInteractive();
//
//             txt.on('pointerdown', () => {
//                 this.selectedWeapon = w.value;
//                 this.updateWeaponHighlight();
//             });
//
//             this.weaponTexts.push({ txt, value: w.value });
//         });
//
//         // Level Auswahl
//         this.add.text(width / 2, 110, 'Choose Map:', { fontSize: '24px', fill: '#ffffff' }).setOrigin(0.5);
//         levels.forEach((lvl, i) => {
//             const txt = this.add.text(width / 2, 150 + i * 30, lvl, {
//                 fontSize: '20px',
//                 fill: lvl === this.selectedLevel ? '#ffff00' : '#00ffff'
//             }).setOrigin(0.5).setInteractive();
//
//             txt.on('pointerdown', () => {
//                 this.selectedLevel = lvl;
//                 this.updateLevelHighlight();
//             });
//
//             this.levelTexts.push({ txt, value: lvl });
//         });
//
//         // Brawler Auswahl
//         this.add.text(width - 300, 30, 'Choose Brawler:', { fontSize: '24px', fill: '#ffffff' });
//         brawlers.forEach((b, i) => {
//             const txt = this.add.text(width - 300, 70 + i * 30, b.label, {
//                 fontSize: '20px',
//                 fill: b.value === this.selectedBrawler ? '#ffff00' : '#ff00ff'
//             }).setInteractive();
//
//             txt.on('pointerdown', () => {
//                 this.selectedBrawler = b.value;
//                 this.updateBrawlerHighlight();
//             });
//
//             this.brawlerTexts.push({ txt, value: b.value });
//         });
//
//         // --- Gadget Auswahl ---
//         this.add.text(50, height / 3, 'Choose Gadget:', {fontSize: '24px', fill: '#ffffff'});
//         gadgets.forEach((w, i) => {
//             const txt = this.add.text(50, height / 3 + 40 + i * 30, w.label, {
//                 fontSize: '20px',
//                 fill: w.value === this.selectedGadget ? '#ffff00' : '#ff0000'
//             }).setInteractive();
//
//             txt.on('pointerdown', () => {
//                 this.selectedGadget = w.value;
//                 this.updateGadgetHighlight();
//             });
//
//             this.gadgetTexts.push({txt, value: w.value});
//         });
//
//         // --- Play Button ---
//         this.add.text(width / 2, 250, 'PLAY', { fontSize: '32px', fill: '#ffffff' })
//             .setOrigin(0.5)
//             .setInteractive()
//             .on('pointerdown', () => {
//                 const playerId = localStorage.getItem('playerId') || crypto.randomUUID();
//                 localStorage.setItem('playerId', playerId);
//
//                 const enteredName = nameInput.value.trim() || 'Player';
//                 this.playerName = enteredName;
//                 localStorage.setItem('playerName', enteredName);
//
//                 if (this.nameInput && this.nameInput.parentNode) {
//                     this.nameInput.parentNode.removeChild(this.nameInput);
//                 }
//
//                 this.socket.emit('joinRoom', {
//                     playerId,
//                     brawlerId: this.selectedBrawler,
//                     levelId: this.selectedLevel,
//                     chosenWeapon: this.selectedWeapon,
//                     playerName: this.playerName,
//                     chosenGadget: this.selectedGadget
//                 }, (response) => {
//                     this.registry.set('roomId', response.roomId);
//                     this.registry.set('playerId', playerId);
//                     this.registry.set('levelId', this.selectedLevel);
//                     this.registry.set('weapon', this.selectedWeapon);
//                     this.registry.set('brawler', this.selectedBrawler);
//                     this.registry.set('playerName', enteredName);
//                     this.registry.set('gadget', this.selectedGadget);
//                     this.scene.start('WaitingScene');
//                 });
//             });
//
//         this.updateWeaponHighlight();
//         this.updateLevelHighlight();
//         this.updateBrawlerHighlight();
//         this.updateGadgetHighlight();
//
//     }
//
//     shutdown() {
//         if (this.nameInput && this.nameInput.parentNode) {
//             this.nameInput.parentNode.removeChild(this.nameInput);
//         }
//     }
//
//     updateWeaponHighlight() {
//         this.weaponTexts.forEach(({ txt, value }) => {
//             txt.setColor(value === this.selectedWeapon ? '#ffff00' : '#00ff00');
//         });
//     }
//
//     updateLevelHighlight() {
//         this.levelTexts.forEach(({ txt, value }) => {
//             txt.setColor(value === this.selectedLevel ? '#ffff00' : '#00ffff');
//         });
//     }
//
//     updateBrawlerHighlight() {
//         this.brawlerTexts.forEach(({ txt, value }) => {
//             txt.setColor(value === this.selectedBrawler ? '#ffff00' : '#ff00ff');
//         });
//     }
//
//     updateGadgetHighlight() {
//         this.gadgetTexts.forEach(({txt, value}) => {
//             txt.setColor(value === this.selectedGadget ? '#ffff00' : '#ff0000');
//         });
//     }
// }
