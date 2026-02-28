// Monopoly Game Implementation

// Data for the 40 squares on a Monopoly board
const boardData = [
  { name: 'GO', type: 'go' },
  { name: 'Mediterranean Avenue', type: 'property', group: 'brown', price: 60, rent: 2 },
  { name: 'Community Chest', type: 'community' },
  { name: 'Baltic Avenue', type: 'property', group: 'brown', price: 60, rent: 4 },
  { name: 'Income Tax', type: 'tax', amount: 200 },
  { name: 'Reading Railroad', type: 'railroad', price: 200, baseRent: 25 },
  { name: 'Oriental Avenue', type: 'property', group: 'lightblue', price: 100, rent: 6 },
  { name: 'Chance', type: 'chance' },
  { name: 'Vermont Avenue', type: 'property', group: 'lightblue', price: 100, rent: 6 },
  { name: 'Connecticut Avenue', type: 'property', group: 'lightblue', price: 120, rent: 8 },
  { name: 'Jail / Just Visiting', type: 'jail' },
  { name: 'St. Charles Place', type: 'property', group: 'pink', price: 140, rent: 10 },
  { name: 'Electric Company', type: 'utility', price: 150 },
  { name: 'States Avenue', type: 'property', group: 'pink', price: 140, rent: 10 },
  { name: 'Virginia Avenue', type: 'property', group: 'pink', price: 160, rent: 12 },
  { name: 'Pennsylvania Railroad', type: 'railroad', price: 200, baseRent: 25 },
  { name: 'St. James Place', type: 'property', group: 'orange', price: 180, rent: 14 },
  { name: 'Community Chest', type: 'community' },
  { name: 'Tennessee Avenue', type: 'property', group: 'orange', price: 180, rent: 14 },
  { name: 'New York Avenue', type: 'property', group: 'orange', price: 200, rent: 16 },
  { name: 'Free Parking', type: 'free-parking' },
  { name: 'Kentucky Avenue', type: 'property', group: 'red', price: 220, rent: 18 },
  { name: 'Chance', type: 'chance' },
  { name: 'Indiana Avenue', type: 'property', group: 'red', price: 220, rent: 18 },
  { name: 'Illinois Avenue', type: 'property', group: 'red', price: 240, rent: 20 },
  { name: 'B. & O. Railroad', type: 'railroad', price: 200, baseRent: 25 },
  { name: 'Atlantic Avenue', type: 'property', group: 'yellow', price: 260, rent: 22 },
  { name: 'Ventnor Avenue', type: 'property', group: 'yellow', price: 260, rent: 22 },
  { name: 'Water Works', type: 'utility', price: 150 },
  { name: 'Marvin Gardens', type: 'property', group: 'yellow', price: 280, rent: 24 },
  { name: 'Go To Jail', type: 'go-to-jail' },
  { name: 'Pacific Avenue', type: 'property', group: 'green', price: 300, rent: 26 },
  { name: 'North Carolina Avenue', type: 'property', group: 'green', price: 300, rent: 26 },
  { name: 'Community Chest', type: 'community' },
  { name: 'Pennsylvania Avenue', type: 'property', group: 'green', price: 320, rent: 28 },
  { name: 'Short Line Railroad', type: 'railroad', price: 200, baseRent: 25 },
  { name: 'Chance', type: 'chance' },
  { name: 'Park Place', type: 'property', group: 'darkblue', price: 350, rent: 35 },
  { name: 'Luxury Tax', type: 'tax', amount: 100 },
  { name: 'Boardwalk', type: 'property', group: 'darkblue', price: 400, rent: 50 }
];

// Chance and Community Chest cards simplified
const chanceCards = [
  {
    text: 'GO alanına ilerle ve 200$ al.',
    action: (player) => {
      player.position = 0;
      player.money += 200;
      addMessage(`${player.name} GO alanına gitti ve 200$ aldı.`);
    }
  },
  {
    text: 'Bankadan 50$ temettü ödemesi al.',
    action: (player) => {
      player.money += 50;
      addMessage(`${player.name} bankadan 50$ aldı.`);
    }
  },
  {
    text: '3 kare geri git.',
    action: (player) => {
      movePlayerRelative(player, -3);
      addMessage(`${player.name} 3 kare geri gitti.`);
    }
  },
  {
    text: 'Fakirler vergisi: 15$ öde.',
    action: (player) => {
      player.money -= 15;
      addMessage(`${player.name} 15$ ödedi.`);
    }
  },
  {
    text: 'Hapse git!',
    action: (player) => {
      sendToJail(player);
    }
  },
  {
    text: 'Yatırım tahsilatı: 150$ al.',
    action: (player) => {
      player.money += 150;
      addMessage(`${player.name} 150$ aldı.`);
    }
  }
];

const communityCards = [
  {
    text: 'Bankadan 200$ al.',
    action: (player) => {
      player.money += 200;
      addMessage(`${player.name} 200$ aldı.`);
    }
  },
  {
    text: 'Doktor ücretleri: 50$ öde.',
    action: (player) => {
      player.money -= 50;
      addMessage(`${player.name} doktora 50$ ödedi.`);
    }
  },
  {
    text: 'Hapse git!',
    action: (player) => {
      sendToJail(player);
    }
  },
  {
    text: 'Stok satışından 50$ al.',
    action: (player) => {
      player.money += 50;
      addMessage(`${player.name} 50$ aldı.`);
    }
  },
  {
    text: 'Fon olgunlaştı: 100$ al.',
    action: (player) => {
      player.money += 100;
      addMessage(`${player.name} 100$ aldı.`);
    }
  },
  {
    text: 'Zenginler vergisi: 100$ öde.',
    action: (player) => {
      player.money -= 100;
      addMessage(`${player.name} 100$ ödedi.`);
    }
  }
];

let players = [];
let currentPlayerIndex = 0;
let gameRunning = false;
let awaitingDecision = false;
let consecutiveDoubles = 0;

// HTML element references
const setupPanel = document.getElementById('setup');
const startButton = document.getElementById('start-button');
const playerCountSelect = document.getElementById('player-count');
const gameContainer = document.getElementById('game');
const boardEl = document.getElementById('board');
const rollButton = document.getElementById('roll-button');
const endTurnButton = document.getElementById('end-turn-button');
const playersInfoEl = document.getElementById('players-info');
const messageEl = document.getElementById('message');
const dice1El = document.getElementById('die1');
const dice2El = document.getElementById('die2');
const actionPanelEl = document.getElementById('action-panel');
const actionTextEl = document.getElementById('action-text');
const actionButtonsEl = document.getElementById('action-buttons');

// Player colors (max 6 players)
const playerColors = ['#e74c3c', '#2980b9', '#27ae60', '#8e44ad', '#d35400', '#16a085'];

// Initialize event listeners
startButton.addEventListener('click', () => {
  const numPlayers = parseInt(playerCountSelect.value);
  if (numPlayers < 2 || numPlayers > 6) {
    alert('Oyuncu sayısı 2 ile 6 arasında olmalı.');
    return;
  }
  startGame(numPlayers);
});

rollButton.addEventListener('click', () => {
  if (!gameRunning || awaitingDecision) return;
  handleRoll();
});

endTurnButton.addEventListener('click', () => {
  if (!gameRunning || awaitingDecision) return;
  endTurn();
});

// Shuffle arrays helper
function shuffle(array) {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]];
  }
  return array;
}

// Start a new game
function startGame(numPlayers) {
  setupPanel.classList.add('hidden');
  gameContainer.classList.remove('hidden');
  gameRunning = true;
  awaitingDecision = false;
  consecutiveDoubles = 0;
  // Initialize players
  players = [];
  for (let i = 0; i < numPlayers; i++) {
    players.push({
      id: i,
      name: `Oyuncu ${i + 1}`,
      money: 1500,
      position: 0,
      properties: [],
      inJail: false,
      color: playerColors[i],
      bankrupt: false
    });
  }
  currentPlayerIndex = 0;
  // Shuffle cards
  shuffle(chanceCards);
  shuffle(communityCards);
  // Build board UI
  buildBoard();
  updateUI();
  addMessage(`${players[currentPlayerIndex].name} sırada. Zar atmak için butona tıklayın.`);
}

// Build board squares and append to DOM
function buildBoard() {
  boardEl.innerHTML = '';
  boardData.forEach((square, index) => {
    const sq = document.createElement('div');
    sq.classList.add('square');
    // Assign classes based on type for styling
    sq.classList.add(square.type.replace(/\s+/g, '-'));
    // Property color bar
    if (square.type === 'property') {
      const colorBar = document.createElement('div');
      colorBar.classList.add('property-color');
      colorBar.classList.add(square.group);
      sq.appendChild(colorBar);
    }
    // Name
    const nameEl = document.createElement('div');
    nameEl.classList.add('name');
    nameEl.textContent = square.name;
    sq.appendChild(nameEl);
    // Price or amount
    const priceEl = document.createElement('div');
    priceEl.classList.add('price');
    if (square.type === 'property') {
      priceEl.textContent = `$${square.price}`;
    } else if (square.type === 'railroad') {
      priceEl.textContent = `$${square.price}`;
    } else if (square.type === 'utility') {
      priceEl.textContent = `$${square.price}`;
    } else if (square.type === 'tax') {
      priceEl.textContent = `-${square.amount}$`;
    } else {
      priceEl.textContent = '';
    }
    sq.appendChild(priceEl);
    // Tokens container
    const tokensEl = document.createElement('div');
    tokensEl.classList.add('tokens');
    sq.appendChild(tokensEl);
    // Store reference
    boardData[index].el = sq;
    boardData[index].tokensEl = tokensEl;
    // Position on grid
    const pos = getGridPosition(index);
    sq.style.gridRowStart = pos.row;
    sq.style.gridColumnStart = pos.col;
    boardEl.appendChild(sq);
  });
}

// Compute row and column for a given index (0-39)
function getGridPosition(index) {
  // Rows and cols are 1-based
  if (index <= 10) {
    return { row: 11, col: 11 - index };
  } else if (index <= 20) {
    return { row: 11 - (index - 10), col: 1 };
  } else if (index <= 30) {
    return { row: 1, col: (index - 20) + 1 };
  } else {
    return { row: (index - 30) + 1, col: 11 };
  }
}

// Update entire UI (players, tokens, dice, message)
function updateUI() {
  // Update tokens
  // Clear all tokens
  boardData.forEach((square) => {
    const container = square.tokensEl;
    while (container.firstChild) {
      container.removeChild(container.firstChild);
    }
  });
  players.forEach((player) => {
    if (player.bankrupt) return;
    const tokenEl = document.createElement('div');
    tokenEl.classList.add('token');
    tokenEl.style.backgroundColor = player.color;
    boardData[player.position].tokensEl.appendChild(tokenEl);
  });
  // Update players info
  playersInfoEl.innerHTML = '';
  players.forEach((player, idx) => {
    if (player.bankrupt) return;
    const infoDiv = document.createElement('div');
    infoDiv.classList.add('player-info');
    if (idx === currentPlayerIndex) infoDiv.classList.add('current-player');
    const colorDot = document.createElement('span');
    colorDot.classList.add('player-color');
    colorDot.style.backgroundColor = player.color;
    infoDiv.appendChild(colorDot);
    const text = document.createElement('span');
    text.textContent = `${player.name}: $${player.money}`;
    if (player.inJail) {
      text.textContent += ' (Hapiste)';
    }
    infoDiv.appendChild(text);
    playersInfoEl.appendChild(infoDiv);
  });
  // Update dice display is done via roll function
}

// Append message to message area
function addMessage(msg) {
  messageEl.textContent = msg;
}

// Handle dice roll
function handleRoll() {
  const player = players[currentPlayerIndex];
  // If player is in jail, skip one turn and free
  if (player.inJail) {
    player.inJail = false;
    addMessage(`${player.name} hapisten çıkıyor. Sıradaki oyuncuya geçiliyor.`);
    endTurn();
    return;
  }
  const d1 = Math.floor(Math.random() * 6) + 1;
  const d2 = Math.floor(Math.random() * 6) + 1;
  dice1El.textContent = d1;
  dice2El.textContent = d2;
  addMessage(`${player.name} ${d1} ve ${d2} attı.`);
  // Check for doubles
  let isDouble = (d1 === d2);
  if (isDouble) {
    consecutiveDoubles += 1;
  } else {
    consecutiveDoubles = 0;
  }
  // If three doubles in a row, go to jail
  if (consecutiveDoubles >= 3) {
    addMessage(`${player.name} art arda üç çift attı ve hapise gönderildi!`);
    sendToJail(player);
    consecutiveDoubles = 0;
    endTurnButton.classList.remove('hidden');
    rollButton.classList.add('hidden');
    updateUI();
    return;
  }
  movePlayer(player, d1 + d2);
  // If rolled double, allow another roll; else enable end turn button
  if (isDouble) {
    addMessage(`${player.name} çift attı ve tekrar zar atabilir.`);
    // Player still can roll again; keep same player index
  } else {
    rollButton.classList.add('hidden');
    endTurnButton.classList.remove('hidden');
  }
}

// Move player by steps
function movePlayer(player, steps) {
  let oldPos = player.position;
  let newPos = (oldPos + steps) % 40;
  // Passed or landed on GO
  if (oldPos + steps >= 40) {
    player.money += 200;
    addMessage(`${player.name} GO üzerinden geçti ve 200$ aldı.`);
  }
  player.position = newPos;
  updateUI();
  handleSquare(player);
}

// Move player relatively (used in chance card)
function movePlayerRelative(player, delta) {
  let oldPos = player.position;
  let newPos = oldPos + delta;
  if (newPos < 0) newPos += 40;
  if (newPos >= 40) newPos %= 40;
  player.position = newPos;
  updateUI();
  handleSquare(player);
}

// Send player to jail
function sendToJail(player) {
  player.position = 10; // jail position
  player.inJail = true;
  addMessage(`${player.name} hapise gönderildi!`);
  updateUI();
}

// Handle landing on a square
function handleSquare(player) {
  const square = boardData[player.position];
  switch (square.type) {
    case 'go':
      // Already handled when passing GO
      break;
    case 'property':
      if (!square.owner) {
        // Offer to buy
        awaitingDecision = true;
        showAction(`${square.name} satın almak ister misin? Maliyet: $${square.price}`, [
          { label: 'Satın Al', action: () => buyProperty(player, square) },
          { label: 'Alma', action: () => { awaitingDecision = false; hideAction(); checkBankruptcy(player); } }
        ]);
      } else if (square.owner !== player.id) {
        // Pay rent
        const ownerPlayer = players[square.owner];
        let rent = square.rent;
        // If owner owns all properties of this color group, double rent (simplified rule)
        const groupProperties = boardData.filter(
          (sq) => sq.type === 'property' && sq.group === square.group
        );
        const ownerOwnsAll = groupProperties.every(
          (sq) => sq.owner !== undefined && sq.owner === ownerPlayer.id
        );
        if (ownerOwnsAll && groupProperties.length > 1) {
          rent *= 2;
        }
        player.money -= rent;
        ownerPlayer.money += rent;
        addMessage(`${player.name}, ${square.name} için ${ownerPlayer.name}'e $${rent} kira ödedi.`);
        checkBankruptcy(player);
      } else {
        // Owned by current player
        addMessage(`${player.name}, kendi mülkü olan ${square.name} üzerinde.`);
      }
      break;
    case 'railroad':
      if (!square.owner) {
        awaitingDecision = true;
        showAction(`${square.name} satın almak ister misin? Maliyet: $${square.price}`, [
          { label: 'Satın Al', action: () => buyProperty(player, square) },
          { label: 'Alma', action: () => { awaitingDecision = false; hideAction(); checkBankruptcy(player); } }
        ]);
      } else if (square.owner !== player.id) {
        // Rent depends on number of railroads owner has
        const ownerPlayer = players[square.owner];
        const ownedRailroads = boardData.filter(sq => sq.type === 'railroad' && sq.owner === ownerPlayer.id).length;
        const rent = square.baseRent * ownedRailroads;
        player.money -= rent;
        ownerPlayer.money += rent;
        addMessage(`${player.name}, ${square.name} için ${ownerPlayer.name}'e $${rent} kira ödedi.`);
        checkBankruptcy(player);
      } else {
        addMessage(`${player.name}, kendi demiryolu ${square.name} üzerinde.`);
      }
      break;
    case 'utility':
      if (!square.owner) {
        awaitingDecision = true;
        showAction(`${square.name} satın almak ister misin? Maliyet: $${square.price}`, [
          { label: 'Satın Al', action: () => buyProperty(player, square) },
          { label: 'Alma', action: () => { awaitingDecision = false; hideAction(); checkBankruptcy(player); } }
        ]);
      } else if (square.owner !== player.id) {
        const ownerPlayer = players[square.owner];
        // rent is 4×dice total if owner has one utility, 10× if both
        const ownedUtilities = boardData.filter(sq => sq.type === 'utility' && sq.owner === ownerPlayer.id).length;
        const lastRollTotal = parseInt(dice1El.textContent) + parseInt(dice2El.textContent);
        const rent = lastRollTotal * (ownedUtilities === 2 ? 10 : 4);
        player.money -= rent;
        ownerPlayer.money += rent;
        addMessage(`${player.name}, ${square.name} için ${ownerPlayer.name}'e $${rent} kira ödedi.`);
        checkBankruptcy(player);
      } else {
        addMessage(`${player.name}, kendi kamu hizmeti ${square.name} üzerinde.`);
      }
      break;
    case 'tax':
      player.money -= square.amount;
      addMessage(`${player.name}, vergi olarak $${square.amount} ödedi.`);
      checkBankruptcy(player);
      break;
    case 'chance':
      drawChance(player);
      break;
    case 'community':
      drawCommunity(player);
      break;
    case 'free-parking':
      addMessage(`${player.name} Ücretsiz Park alanında.`);
      break;
    case 'go-to-jail':
      sendToJail(player);
      break;
    case 'jail':
      addMessage(`${player.name} Hapiste/Ziyaret Et alanında.\nHapiste değilsiniz, oyuna devam edin.`);
      break;
    default:
      break;
  }
}

// Show action panel with buttons
function showAction(text, buttons) {
  actionTextEl.textContent = text;
  actionButtonsEl.innerHTML = '';
  buttons.forEach(btn => {
    const buttonEl = document.createElement('button');
    buttonEl.textContent = btn.label;
    buttonEl.addEventListener('click', () => {
      hideAction();
      btn.action();
      updateUI();
      awaitingDecision = false;
      // After decision, if roll button is hidden (i.e., non-double), show end turn button
      if (rollButton.classList.contains('hidden')) {
        endTurnButton.classList.remove('hidden');
      }
    });
    actionButtonsEl.appendChild(buttonEl);
  });
  actionPanelEl.classList.remove('hidden');
}

function hideAction() {
  actionPanelEl.classList.add('hidden');
}

// Buy property
function buyProperty(player, square) {
  if (player.money >= square.price) {
    player.money -= square.price;
    square.owner = player.id;
    player.properties.push(player.position);
    addMessage(`${player.name}, ${square.name} mülkünü $${square.price} karşılığında satın aldı.`);
    updateUI();
  } else {
    addMessage(`${player.name}, yeterli parası olmadığı için ${square.name} mülkünü satın alamadı.`);
  }
  checkBankruptcy(player);
}

// Draw a chance card
function drawChance(player) {
  const card = chanceCards.shift();
  chanceCards.push(card);
  addMessage(`Şans Kartı: ${card.text}`);
  card.action(player);
  updateUI();
  checkBankruptcy(player);
}

// Draw a community chest card
function drawCommunity(player) {
  const card = communityCards.shift();
  communityCards.push(card);
  addMessage(`Topluluk Sandığı: ${card.text}`);
  card.action(player);
  updateUI();
  checkBankruptcy(player);
}

// Check if player is bankrupt and remove them if necessary
function checkBankruptcy(player) {
  if (player.money < 0) {
    player.bankrupt = true;
    addMessage(`${player.name} iflas etti ve oyundan elendi!`);
    // Release properties
    boardData.forEach(square => {
      if (square.owner === player.id) {
        square.owner = undefined;
      }
    });
    // Check for game over
    const activePlayers = players.filter(p => !p.bankrupt);
    if (activePlayers.length === 1) {
      const winner = activePlayers[0];
      addMessage(`${winner.name} oyunu kazandı!`);
      rollButton.disabled = true;
      endTurnButton.disabled = true;
      gameRunning = false;
    }
  }
  updateUI();
}

// End the current player's turn
function endTurn() {
  // Move to next active player
  do {
    currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
  } while (players[currentPlayerIndex].bankrupt);
  consecutiveDoubles = 0;
  rollButton.classList.remove('hidden');
  endTurnButton.classList.add('hidden');
  addMessage(`${players[currentPlayerIndex].name} sırada. Zar atmak için butona tıklayın.`);
  updateUI();
}