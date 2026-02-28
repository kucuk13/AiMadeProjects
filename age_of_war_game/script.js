/*
 * Main game logic for the simplified Age of Wars implementation.
 *
 * The goal is to destroy the enemy base while defending your own.  You
 * earn money over time and by defeating enemy units, which can be used
 * to spawn new troops.  Enemy units spawn automatically on a timer.
 *
 * Units move along a single lane towards the opposite base.  When a
 * unit encounters an opposing unit or base within its attack range it
 * will stop and deal damage at a fixed rate.  Ranged units have a
 * larger attack range and will attack from behind your own units.
 *
 * Audio:  A looping background track plays when the player first
 * interacts with the page, and a short attack sound is emitted
 * whenever a unit strikes an enemy.
 */
(function() {
    const canvas = document.getElementById('gameCanvas');
    const ctx = canvas.getContext('2d');

    // Adjust canvas dimensions to match CSS-defined size.  If the
    // element is resized via CSS, update the drawing surface accordingly.
    function resizeCanvas() {
        const rect = canvas.getBoundingClientRect();
        canvas.width = rect.width;
        canvas.height = rect.height;
    }
    // Call once at load and again on window resize
    resizeCanvas();
    window.addEventListener('resize', resizeCanvas);

    // Audio elements.  Sources are set to local .wav files; they
    // remain silent until the user interacts due to browser autoplay
    // restrictions.
    const bgMusic = document.getElementById('background-music');
    bgMusic.src = 'background_music.wav';
    bgMusic.volume = 0.4;
    const attackSound = document.getElementById('attack-sound');
    attackSound.src = 'attack.wav';
    attackSound.volume = 0.6;

    let musicStarted = false;
    function maybeStartMusic() {
        if (!musicStarted) {
            musicStarted = true;
            bgMusic.play().catch(() => {
                /* Autoplay restrictions may still block playback; in that
                 * case the user will need to click again. */
            });
        }
    }

    // HUD elements to display resources and health
    const moneyDisplay = document.getElementById('money-display');
    const playerHealthDisplay = document.getElementById('player-health');
    const enemyHealthDisplay = document.getElementById('enemy-health');
    const endMessage = document.getElementById('end-message');

    // Unit definitions.  Each object defines cost, base health,
    // damage per hit, movement speed (pixels per second) and attack
    // range (pixels).  The size property controls the drawn radius on
    // canvas.
    const unitTypes = {
        infantry: {
            name: 'Infantry',
            cost: 50,
            health: 100,
            damage: 15,
            speed: 60,      // px/s
            range: 12,
            color: '#4caf50',
            size: 12,
            attackRate: 0.8 // seconds between attacks
        },
        archer: {
            name: 'Archer',
            cost: 75,
            health: 80,
            damage: 12,
            speed: 55,
            range: 90,
            color: '#f39c12',
            size: 12,
            attackRate: 1.0
        },
        knight: {
            name: 'Knight',
            cost: 150,
            health: 200,
            damage: 25,
            speed: 45,
            range: 14,
            color: '#9b59b6',
            size: 15,
            attackRate: 0.7
        }
    };

    // Game state variables
    let playerMoney = 200;
    let playerBaseHealth = 1000;
    let enemyBaseHealth = 1000;
    const playerUnits = [];
    const enemyUnits = [];
    let lastEnemySpawn = 0;
    let enemySpawnInterval = 5.0; // initial seconds between spawns
    let enemySpawnDecrease = 0.05; // spawn interval decrease after each spawn (increasing difficulty)
    let lastMoneyAccrual = 0;
    const moneyPerSecond = 10; // passive income
    let gameOver = false;

    // Base positions: drawn at the left and right edges
    function getPlayerBaseX() {
        return 20;
    }
    function getEnemyBaseX() {
        return canvas.width - 40;
    }

    // Unit constructor
    // Helper to adjust colours.  Given a hex string and a factor < 1,
    // returns a lighter shade.  Used to differentiate enemy units.
    function lighten(hex, factor) {
        // Convert a hex colour to a lighter shade by multiplying each
        // channel by the given factor (>1).  Values are clamped to 255.
        const num = parseInt(hex.replace('#', ''), 16);
        const r = Math.min(255, Math.floor(((num >> 16) & 0xff) * factor));
        const g = Math.min(255, Math.floor(((num >> 8) & 0xff) * factor));
        const b = Math.min(255, Math.floor((num & 0xff) * factor));
        return '#' + [r, g, b].map(v => v.toString(16).padStart(2, '0')).join('');
    }

    function Unit(x, typeKey, side) {
        const type = unitTypes[typeKey];
        this.x = x;
        this.y = canvas.height - 60; // vertical position near the bottom
        this.type = type;
        this.health = type.health;
        this.damage = type.damage;
        this.speed = type.speed;
        this.range = type.range;
        this.attackRate = type.attackRate;
        this.size = type.size;
        // Colour depends on side: enemy units use a lighter shade of
        // the type's base colour to aid visual distinction.  Player
        // units retain the original colour.
        this.color = side === 'enemy' ? lighten(type.color, 1.6) : type.color;
        this.side = side; // 'player' or 'enemy'
        this.cooldown = 0; // time until next attack
    }

    // Spawn functions
    function spawnPlayerUnit(typeKey) {
        if (gameOver) return;
        const type = unitTypes[typeKey];
        if (!type) return;
        if (playerMoney < type.cost) return;
        playerMoney -= type.cost;
        const x = getPlayerBaseX() + 30; // spawn near player base
        const unit = new Unit(x, typeKey, 'player');
        playerUnits.push(unit);
        maybeStartMusic();
    }

    function spawnEnemyUnit() {
        if (gameOver) return;
        // Choose a random unit type; weight knights less to preserve fairness
        const keys = ['infantry', 'archer', 'knight'];
        const r = Math.random();
        let typeKey;
        if (r < 0.5) typeKey = 'infantry';
        else if (r < 0.85) typeKey = 'archer';
        else typeKey = 'knight';
        const x = getEnemyBaseX() - 30;
        const unit = new Unit(x, typeKey, 'enemy');
        enemyUnits.push(unit);
    }

    // Handle clicks on spawn buttons
    document.querySelectorAll('.unit-button').forEach(btn => {
        btn.addEventListener('click', () => {
            const typeKey = btn.getAttribute('data-type');
            spawnPlayerUnit(typeKey);
        });
    });

    // Update HUD each frame
    function updateHUD() {
        moneyDisplay.textContent = `Money: ${Math.floor(playerMoney)}`;
        playerHealthDisplay.textContent = `Your Base: ${Math.max(0, Math.floor(playerBaseHealth))} HP`;
        enemyHealthDisplay.textContent = `Enemy Base: ${Math.max(0, Math.floor(enemyBaseHealth))} HP`;

        // Enable or disable buttons based on affordability
        document.querySelectorAll('.unit-button').forEach(btn => {
            const typeKey = btn.getAttribute('data-type');
            const cost = unitTypes[typeKey].cost;
            btn.disabled = playerMoney < cost || gameOver;
        });
    }

    // Core update logic called each frame
    function update(dt) {
        if (gameOver) return;

        // Passive income accrual
        lastMoneyAccrual += dt;
        if (lastMoneyAccrual >= 1.0) {
            playerMoney += moneyPerSecond * Math.floor(lastMoneyAccrual);
            lastMoneyAccrual -= Math.floor(lastMoneyAccrual);
        }

        // Enemy spawning
        lastEnemySpawn += dt;
        if (lastEnemySpawn >= enemySpawnInterval) {
            spawnEnemyUnit();
            lastEnemySpawn = 0;
            // Increase difficulty by reducing spawn interval (but keep minimum)
            enemySpawnInterval = Math.max(1.0, enemySpawnInterval - enemySpawnDecrease);
        }

        // Update units for both sides
        updateUnits(playerUnits, enemyUnits, dt, +1);
        updateUnits(enemyUnits, playerUnits, dt, -1);

        // Check base destruction
        if (enemyBaseHealth <= 0 && !gameOver) {
            gameOver = true;
            endGame('You win!');
        }
        if (playerBaseHealth <= 0 && !gameOver) {
            gameOver = true;
            endGame('You lose!');
        }
    }

    // Update units for one side.  Parameter direction should be +1
    // for player units moving right and -1 for enemy units moving left.
    function updateUnits(units, opposingUnits, dt, direction) {
        for (let i = units.length - 1; i >= 0; i--) {
            const unit = units[i];
            // Skip if dead (will remove later)
            if (unit.health <= 0) continue;

            // Reduce cooldown timer
            if (unit.cooldown > 0) {
                unit.cooldown -= dt;
            }

            // Determine nearest opposing unit in attack range
            let target = null;
            let minDist = Infinity;
            for (const opp of opposingUnits) {
                if (opp.health <= 0) continue;
                const dx = (opp.x - unit.x) * direction; // positive if opp is ahead
                if (dx >= 0 && dx <= unit.range) {
                    if (dx < minDist) {
                        minDist = dx;
                        target = opp;
                    }
                }
            }

            // Check base attack: if no unit target and unit is at enemy base
            const baseX = direction === +1 ? getEnemyBaseX() : getPlayerBaseX();
            const distToBase = (baseX - unit.x) * direction - 20; // small margin

            if (target) {
                // Attack target
                if (unit.cooldown <= 0) {
                    attackSound.currentTime = 0;
                    attackSound.play().catch(() => {});
                    target.health -= unit.damage;
                    playerMoney += direction === +1 && target.side === 'enemy' ? unit.damage * 0.3 : 0;
                    unit.cooldown = unit.attackRate;
                }
                // Do not move while attacking
            } else if (distToBase <= unit.range) {
                // Attack base
                if (unit.cooldown <= 0) {
                    attackSound.currentTime = 0;
                    attackSound.play().catch(() => {});
                    if (direction === +1) {
                        enemyBaseHealth -= unit.damage;
                    } else {
                        playerBaseHealth -= unit.damage;
                    }
                    unit.cooldown = unit.attackRate;
                }
            } else {
                // Move forward
                unit.x += unit.speed * dt * direction;
            }

            // Remove dead units (health <= 0)
            if (unit.health <= 0) {
                // Remove this unit from array
                units.splice(i, 1);
            }
        }
    }

    // Draw the game state
    function draw() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Draw ground line
        ctx.fillStyle = '#333';
        ctx.fillRect(0, canvas.height - 50, canvas.width, 50);

        // Draw bases
        // Player base (left)
        ctx.fillStyle = '#2979ff';
        ctx.fillRect(getPlayerBaseX() - 10, canvas.height - 120, 40, 120);
        // Enemy base (right)
        ctx.fillStyle = '#e53935';
        ctx.fillRect(getEnemyBaseX() - 30, canvas.height - 120, 40, 120);

        // Draw units
        function drawUnits(units) {
            for (const unit of units) {
                ctx.beginPath();
                ctx.fillStyle = unit.color;
                ctx.arc(unit.x, unit.y - 20, unit.size, 0, Math.PI * 2);
                ctx.fill();
                // Health bar
                const barWidth = unit.size * 2;
                const healthRatio = Math.max(0, unit.health / unit.type.health);
                ctx.fillStyle = '#444';
                ctx.fillRect(unit.x - barWidth / 2, unit.y - 40, barWidth, 4);
                ctx.fillStyle = '#76ff03';
                ctx.fillRect(unit.x - barWidth / 2, unit.y - 40, barWidth * healthRatio, 4);
            }
        }
        drawUnits(playerUnits);
        drawUnits(enemyUnits);
    }

    function endGame(message) {
        endMessage.textContent = message;
        endMessage.style.display = 'block';
    }

    // Main loop using requestAnimationFrame
    let lastTime = performance.now();
    function loop(now) {
        const dt = (now - lastTime) / 1000; // seconds
        lastTime = now;
        update(dt);
        draw();
        updateHUD();
        requestAnimationFrame(loop);
    }
    requestAnimationFrame(loop);
})();