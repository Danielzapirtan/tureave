(function() {
        // ----- State management (mirrors Bash variables) -----
        // shiftDays holds days that are work shifts (12h each)
        let shiftDays = [];
        // leaveDays holds days that are leave (8h each)
        let leaveDays = [];
	let sept = false;
	let maxDay = 31;
	const septElement = document.getElementById("sept");
	septElement.addEventListener("click", (e) => {
		sept = septElement.checked;
		maxDay = 31;
    if (sept)
      maxDay = 30;
		resetToDefaults();
	});

        // ----- Initialize default shift days according to script: for n in $(seq 1 4 31); do n2=$(($n + 1)); shiftDays="$shiftDays $n $n2"; done
        function buildDefaultShiftDays() {
            const days = [];
           // console.log("abc");
      	    if (!sept) {
          		days.push(1);
        	  }
            let minDay = 4;
            if (sept)
              minDay = 2;
            for (let n = minDay; n <= maxDay; n += 4) {
                days.push(n);
                const n2 = n + 1;
                if (n2 <= maxDay) {
                    days.push(n2);
                }
            }
            // Ensure uniqueness and sorted numerically (already sorted by generation)
            return [...new Set(days)].sort((a, b) => a - b);
        }

        // Initialize leaveDays as empty array (as in script)
        function resetToDefaults() {
            shiftDays = buildDefaultShiftDays();
            leaveDays = [];
            renderAll();
        }

        // ----- Helper: check if a day is in an array (like grep -qw)
        function arrayIncludesDay(arr, day) {
            return arr.includes(day);
        }

        // ----- Toggle function: mirrors the bash toggle logic exactly
        function toggleDay(day) {
            const dayNum = Number(day);
            if (isNaN(dayNum) || dayNum < 1 || dayNum > maxDay) return;

            if (arrayIncludesDay(shiftDays, dayNum)) {
                // Day is in shiftDays -> move to leaveDays
                shiftDays = shiftDays.filter(d => d !== dayNum);
                // Add to leaveDays and sort
                leaveDays.push(dayNum);
                leaveDays = leaveDays.sort((a, b) => a - b);
            } else if (arrayIncludesDay(leaveDays, dayNum)) {
                // Day is in leaveDays -> move back to shiftDays
                leaveDays = leaveDays.filter(d => d !== dayNum);
                shiftDays.push(dayNum);
                shiftDays = shiftDays.sort((a, b) => a - b);
            }
            // If day not in either (should not happen with UI, but safe to ignore)
            renderAll();
        }

        // ----- Compute statistics (showStats equivalent)
        function computeStats() {
            const numShift = shiftDays.length;
            const numLeave = leaveDays.length;
            const numHours = numShift * 12 + numLeave * 8;
            return {
                numShift,
                numLeave,
                numHours
            };
        }

        // ----- UI rendering -----
        const dayGridEl = document.getElementById('dayGrid');
        const leaveCountEl = document.getElementById('leaveCount');
        const workedHoursEl = document.getElementById('workedHours');
        const shiftCountEl = document.getElementById('shiftCount');

        // Create day buttons for days 1..31
        function renderDayGrid() {
            if (!dayGridEl) return;
            dayGridEl.innerHTML = '';

            for (let day = 1; day <= maxDay; day++) {
                const button = document.createElement('button');
                button.className = 'day-btn';
                button.setAttribute('data-day', day);

                // Determine if day is shift or leave
                const isShift = arrayIncludesDay(shiftDays, day);
                const isLeave = arrayIncludesDay(leaveDays, day);

                if (isShift) {
                    button.classList.add('shift');
                } else if (isLeave) {
                    button.classList.add('leave');
                }

                // Add day number and small label
                button.innerHTML = `${day}<small>${isShift ? 'tură' : isLeave ? 'concediu' : '—'}</small>`;

		let supressClick = false;
                button.addEventListener('contextmenu', (e) => {
		    e.preventDefault();
		    supressClick = true;
                    const dayValue = parseInt(e.currentTarget.getAttribute('data-day'), 10);
                    toggleDay(dayValue);
                });

                button.addEventListener('click', (e) => {
		    if (supressClick) {
			    supressClick = false;
			    return;
		    }
                    const dayValue = parseInt(e.currentTarget.getAttribute('data-day'), 10);
                    toggleDay(dayValue);
                });

                dayGridEl.appendChild(button);
            }
        }

        // Update statistics display
        function updateStatsDisplay() {
            const stats = computeStats();
            if (leaveCountEl) leaveCountEl.textContent = stats.numLeave;
            if (workedHoursEl) workedHoursEl.innerHTML = `${stats.numHours} <small>ore</small>`;
            if (shiftCountEl) shiftCountEl.textContent = stats.numShift;
        }

        // Refresh entire UI (grid + stats)
        function renderAll() {
            renderDayGrid();
            updateStatsDisplay();
        }

        // ----- Reset button handler -----
        document.getElementById('resetButton').addEventListener('click', () => {
            resetToDefaults();
        });

        // ----- Initial load: set default state and render -----
        resetToDefaults();

        // Optional: ensure any leftover state consistency (though reset covers it)
        // Also show active state on first paint.
    })();
