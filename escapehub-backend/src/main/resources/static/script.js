// Active state (persisted in localStorage so page refresh keeps login/team)
var currentUser = null;    // { id, username, email }
var currentTeam = null;    // { id, name, members }
var missions = [];         // Loaded from PostgreSQL via /api/missions + /puzzles
var sessionsByMission = {}; // missionId -> MissionSessionResponse

var selectedMissionIndex = null;
var selectedStageIndex = 0;
var currentSession = null;
var diaryNotes = [];
var seconds = 0;

// -------------------- Persistence helpers --------------------
function saveLocalState() {
    if (currentUser) {
        localStorage.setItem("escapehub_user", JSON.stringify(currentUser));
    } else {
        localStorage.removeItem("escapehub_user");
    }
    if (currentTeam) {
        localStorage.setItem("escapehub_team", JSON.stringify(currentTeam));
    } else {
        localStorage.removeItem("escapehub_team");
    }
}

function restoreLocalState() {
    try {
        var savedUser = localStorage.getItem("escapehub_user");
        var savedTeam = localStorage.getItem("escapehub_team");
        if (savedUser) currentUser = JSON.parse(savedUser);
        if (savedTeam) currentTeam = JSON.parse(savedTeam);
    } catch (e) {
        currentUser = null;
        currentTeam = null;
    }
    renderUserState();
    renderTeamState();
}

function renderUserState() {
    var el = document.getElementById("currentUser");
    if (currentUser) {
        el.textContent = "Logged in as: " + currentUser.username + " (ID: " + currentUser.id + ")";
    } else {
        el.textContent = "Not logged in";
    }
}

function renderTeamState() {
    var teamNameEl = document.getElementById("teamName");
    var membersEl = document.getElementById("teamMembers");
    if (currentTeam) {
        teamNameEl.textContent = currentTeam.name + " (Team ID: " + currentTeam.id + ")";
        var members = (currentTeam.members || []).map(function (m) {
            return m.username + " (" + m.role + ")";
        });
        membersEl.textContent = members.length > 0 ? "Members: " + members.join(", ") : "";
    } else {
        teamNameEl.textContent = "You are not in a team yet.";
        membersEl.textContent = "";
    }
}

async function extractErrorMessage(response, fallback) {
    try {
        var data = await response.json();
        return data.messege || data.message || data.error || fallback;
    } catch (e) {
        return fallback + " (HTTP " + response.status + ")";
    }
}

// -------------------- Navigation --------------------
function showPage(pageId) {
    var pages = document.getElementsByClassName("page");
    for (var i = 0; i < pages.length; i++) {
        pages[i].classList.add("hidden");
    }
    document.getElementById(pageId).classList.remove("hidden");

    if (pageId === "missionsPage") {
        loadMissions();
    } else if (pageId === "progressPage") {
        updateProgress();
    } else if (pageId === "gamePage" && currentTeam) {
        loadDiary();
    }
}

// -------------------- Auth (POST /api/auth/login & /register) --------------------
async function login(event) {
    event.preventDefault();
    var username = document.getElementById("loginUsername").value.trim();
    var password = document.getElementById("loginPassword").value;

    if (!username || !password) {
        showMessage("loginMessage", "Please fill in all fields.");
        return;
    }

    try {
        var res = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: username, pass: password })
        });

        if (!res.ok) {
            var errMsg = await extractErrorMessage(res, "Login failed.");
            showMessage("loginMessage", errMsg);
            return;
        }

        var data = await res.json();
        currentUser = {
            id: data.userId,
            username: data.username || username,
            email: data.email
        };
        currentTeam = null;
        currentSession = null;
        saveLocalState();
        renderUserState();
        renderTeamState();
        showMessage("loginMessage", "Login successful! Welcome, " + currentUser.username + ".");
        document.getElementById("loginPassword").value = "";
    } catch (e) {
        showMessage("loginMessage", "Could not connect to backend.");
    }
}

async function register(event) {
    event.preventDefault();
    var username = document.getElementById("registerUsername").value.trim();
    var password = document.getElementById("registerPassword").value;

    if (!username || !password) {
        showMessage("loginMessage", "Please fill in all fields.");
        return;
    }

    try {
        var res = await fetch("/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: username, pass: password })
        });

        if (!res.ok) {
            var errMsg = await extractErrorMessage(res, "Registration failed.");
            showMessage("loginMessage", errMsg);
            return;
        }

        var data = await res.json();
        currentUser = {
            id: data.userId,
            username: data.username || username,
            email: data.email
        };
        currentTeam = null;
        currentSession = null;
        saveLocalState();
        renderUserState();
        renderTeamState();
        showMessage("loginMessage", "Account created and saved to database! Logged in as " + currentUser.username + ".");
        document.getElementById("registerUsername").value = "";
        document.getElementById("registerPassword").value = "";
    } catch (e) {
        showMessage("loginMessage", "Could not connect to backend.");
    }
}

// -------------------- Teams (POST /api/teams/create & /join-by-name) --------------------
async function createTeam() {
    if (!currentUser) {
        showMessage("teamMessage", "Please log in or register first.");
        return;
    }
    var name = document.getElementById("newTeamName").value.trim();
    if (!name) {
        showMessage("teamMessage", "Please enter a team name.");
        return;
    }

    try {
        var res = await fetch("/api/teams/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: name, creatorId: currentUser.id })
        });

        if (!res.ok) {
            var errMsg = await extractErrorMessage(res, "Could not create team.");
            showMessage("teamMessage", errMsg);
            return;
        }

        currentTeam = await res.json();
        currentSession = null;
        sessionsByMission = {};
        saveLocalState();
        renderTeamState();
        loadDiary();
        showMessage("teamMessage", "Team '" + currentTeam.name + "' created and saved!");
        document.getElementById("newTeamName").value = "";
    } catch (e) {
        showMessage("teamMessage", "Could not connect to backend.");
    }
}

async function joinTeam() {
    if (!currentUser) {
        showMessage("teamMessage", "Please log in or register first.");
        return;
    }
    var name = document.getElementById("joinTeamName").value.trim();
    if (!name) {
        showMessage("teamMessage", "Please enter a team name.");
        return;
    }

    try {
        var res = await fetch("/api/teams/join-by-name", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ teamName: name, userId: currentUser.id })
        });

        if (!res.ok) {
            var errMsg = await extractErrorMessage(res, "Could not join team.");
            showMessage("teamMessage", errMsg);
            return;
        }

        currentTeam = await res.json();
        currentSession = null;
        sessionsByMission = {};
        saveLocalState();
        renderTeamState();
        loadDiary();
        showMessage("teamMessage", "Joined team '" + currentTeam.name + "'!");
        document.getElementById("joinTeamName").value = "";
    } catch (e) {
        showMessage("teamMessage", "Could not connect to backend.");
    }
}

// -------------------- Missions & Puzzles (GET /api/missions & /{id}/puzzles) --------------------
async function loadMissions() {
    var list = document.getElementById("missionList");
    list.innerHTML = "<p>Loading missions from database...</p>";

    try {
        var res = await fetch("/api/missions");
        if (!res.ok) throw new Error("Failed to load missions");
        var missionData = await res.json();

        var loaded = [];
        for (var i = 0; i < missionData.length; i++) {
            var m = missionData[i];
            var pRes = await fetch("/api/missions/" + m.id + "/puzzles");
            var puzzles = pRes.ok ? await pRes.json() : [];
            loaded.push({
                id: m.id,
                name: m.title,
                description: m.description || "",
                stages: puzzles
            });
        }
        missions = loaded;
        renderMissionList();
        updateProgress();
    } catch (e) {
        list.innerHTML = "<p>Could not load missions from backend.</p>";
    }
}

function renderMissionList() {
    var list = document.getElementById("missionList");
    list.innerHTML = "";

    if (missions.length === 0) {
        list.innerHTML = "<p>No missions found in database.</p>";
        return;
    }

    for (var i = 0; i < missions.length; i++) {
        var mission = missions[i];
        var session = sessionsByMission[mission.id];
        var solvedCount = session && session.solvedPuzzles ? session.solvedPuzzles.length : 0;
        var totalStages = mission.stages.length;

        var panel = document.createElement("div");
        panel.className = "panel mission";

        var title = document.createElement("h3");
        title.textContent = mission.name;

        var description = document.createElement("p");
        description.textContent = mission.description;

        var progress = document.createElement("p");
        progress.textContent = "Stages: " + solvedCount + "/" + totalStages +
            (session && session.status === "COMPLETED" ? " (Completed)" : "");

        var button = document.createElement("button");
        button.className = "main-button";
        button.textContent = session ? "Resume Mission" : "Start Mission";

        button.onclick = (function (idx) {
            return function () { selectMission(idx); };
        })(i);

        panel.appendChild(title);
        panel.appendChild(description);
        panel.appendChild(progress);
        panel.appendChild(button);
        list.appendChild(panel);
    }
}

// -------------------- Session Start & Gameplay --------------------
async function selectMission(index) {
    if (!currentUser) {
        alert("Please log in or register first (Account tab).");
        showPage("loginPage");
        return;
    }
    if (!currentTeam) {
        alert("Please create or join a team first (Teams tab).");
        showPage("teamsPage");
        return;
    }

    var mission = missions[index];
    try {
        var res = await fetch("/api/sessions/start", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ teamId: currentTeam.id, missionId: mission.id })
        });

        if (!res.ok) {
            var errMsg = await extractErrorMessage(res, "Could not start mission session.");
            alert(errMsg);
            return;
        }

        currentSession = await res.json();
        sessionsByMission[mission.id] = currentSession;
        selectedMissionIndex = index;

        syncTimerFromSession(currentSession);

        document.getElementById("selectedMission").textContent =
            mission.name + " (Session #" + currentSession.id + ")";
        document.getElementById("answerInput").value = "";
        showMessage("answerMessage", "");

        loadStages();
        loadDiary();
        updateProgress();
        renderMissionList();
        showPage("gamePage");
        updateTimer();

        // Open first unlocked & unsolved stage (or stage 0)
        var firstOpen = 0;
        for (var i = 0; i < mission.stages.length; i++) {
            if (isStageUnlocked(mission.stages[i]) && !isStageCompleted(mission.stages[i])) {
                firstOpen = i;
                break;
            }
        }
        if (mission.stages.length > 0) {
            openStage(firstOpen);
        }
    } catch (e) {
        alert("Could not connect to backend to start session.");
    }
}

function syncTimerFromSession(session) {
    if (!session) {
        seconds = 0;
        return;
    }
    if (session.status === "COMPLETED" && session.completionTimeSeconds != null) {
        seconds = session.completionTimeSeconds;
        return;
    }
    if (session.startedAt) {
        var startedMs = new Date(session.startedAt).getTime();
        if (!isNaN(startedMs)) {
            var diff = Math.floor((Date.now() - startedMs) / 1000);
            seconds = diff > 0 ? diff : 0;
            return;
        }
    }
    seconds = 0;
}

function isStageCompleted(stage) {
    if (!currentSession || !currentSession.solvedPuzzles) return false;
    return currentSession.solvedPuzzles.indexOf(stage.code) !== -1;
}

function isStageUnlocked(stage) {
    if (isStageCompleted(stage)) return true;
    if (!stage.requiredClues || stage.requiredClues.length === 0) return true;
    var evidence = (currentSession && currentSession.unlockedEvidence) ? currentSession.unlockedEvidence : [];
    for (var i = 0; i < stage.requiredClues.length; i++) {
        if (evidence.indexOf(stage.requiredClues[i]) === -1) {
            return false;
        }
    }
    return true;
}

function loadStages() {
    var list = document.getElementById("stageList");
    list.innerHTML = "";
    if (selectedMissionIndex === null) return;

    var stages = missions[selectedMissionIndex].stages;
    for (var i = 0; i < stages.length; i++) {
        var stage = stages[i];
        var completed = isStageCompleted(stage);
        var unlocked = isStageUnlocked(stage);

        var button = document.createElement("button");
        button.className = "stage-button";

        if (completed) {
            button.textContent = "Stage " + (i + 1) + ": " + stage.title + " - Completed";
            button.classList.add("completed");
        } else if (unlocked) {
            button.textContent = "Stage " + (i + 1) + ": " + stage.title + " - Open";
        } else {
            button.textContent = "Stage " + (i + 1) + ": " + stage.title + " - Locked";
            button.disabled = true;
        }

        button.onclick = (function (idx) {
            return function () { openStage(idx); };
        })(i);

        list.appendChild(button);
    }
}

function openStage(index) {
    var mission = missions[selectedMissionIndex];
    var stage = mission.stages[index];

    if (!isStageUnlocked(stage)) {
        showMessage("answerMessage", "This stage is locked. Complete the required stages first.");
        return;
    }

    selectedStageIndex = index;

    document.getElementById("stageContent").classList.remove("hidden");
    document.getElementById("stageTitle").textContent =
        "Stage " + (index + 1) + " — " + stage.title + " [" + stage.type + "]";
    document.getElementById("stageNumber").textContent =
        (index + 1) + " / " + mission.stages.length;
    document.getElementById("stageDescription").textContent = stage.prompt;

    document.getElementById("answerInput").value = "";
    showMessage("answerMessage", isStageCompleted(stage) ? "Stage already completed!" : "");
    showMessage("clueMessage", "");

    loadClues();
}

function loadClues() {
    var list = document.getElementById("clueList");
    list.innerHTML = "";

    var stage = missions[selectedMissionIndex].stages[selectedStageIndex];
    var allClues = (stage.clues || []).slice();

    // Also include unlocked evidence gathered from previous stages
    var unlockedEv = (currentSession && currentSession.unlockedEvidence) ? currentSession.unlockedEvidence : [];
    var evMap = stage.evidenceMap || {};
    for (var k = 0; k < unlockedEv.length; k++) {
        var evKey = unlockedEv[k];
        var evText = evMap[evKey] ? evMap[evKey] : ("Unlocked evidence: " + evKey);
        if (allClues.indexOf(evText) === -1) {
            allClues.push("[Unlocked] " + evText);
        }
    }

    stage._displayClues = allClues;
    stage.revealedClues = 0;

    for (var i = 0; i < allClues.length; i++) {
        var clue = document.createElement("p");
        clue.textContent = "Clue " + (i + 1) + ": Hidden (click Reveal Hint)";
        clue.id = "clue" + i;
        list.appendChild(clue);
    }
}

function showNextClue() {
    if (selectedMissionIndex === null) return;
    var stage = missions[selectedMissionIndex].stages[selectedStageIndex];
    var clues = stage._displayClues || [];

    if (stage.revealedClues >= clues.length) {
        showMessage("clueMessage", "No more clues available.");
        return;
    }

    var idx = stage.revealedClues;
    document.getElementById("clue" + idx).textContent = "Clue " + (idx + 1) + ": " + clues[idx];
    stage.revealedClues++;
    showMessage("clueMessage", "Clue revealed.");
}

// -------------------- Submit Answer (POST /api/sessions/{sessionId}/puzzles/{puzzleId}/attempt) --------------------
async function submitAnswer() {
    if (selectedMissionIndex === null || !currentSession) {
        showMessage("answerMessage", "Please select a mission first.");
        return;
    }

    var answer = document.getElementById("answerInput").value.trim();
    if (!answer) {
        showMessage("answerMessage", "Please enter an answer.");
        return;
    }

    var mission = missions[selectedMissionIndex];
    var stage = mission.stages[selectedStageIndex];

    try {
        var res = await fetch(
            "/api/sessions/" + currentSession.id + "/puzzles/" + stage.id + "/attempt",
            {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    userId: currentUser ? currentUser.id : null,
                    answer: answer
                })
            }
        );

        if (res.status === 403) {
            var lockMsg = await extractErrorMessage(res, "This puzzle is locked.");
            showMessage("answerMessage", lockMsg);
            return;
        }

        if (!res.ok) {
            var errMsg = await extractErrorMessage(res, "Attempt failed.");
            showMessage("answerMessage", errMsg);
            return;
        }

        var outcome = await res.json();

        if (outcome.solved) {
            // Refresh session state from PostgreSQL
            var sRes = await fetch("/api/sessions/" + currentSession.id);
            if (sRes.ok) {
                currentSession = await sRes.json();
                sessionsByMission[mission.id] = currentSession;
                syncTimerFromSession(currentSession);
            }

            if (outcome.missionComplete) {
                showMessage("answerMessage", "Mission completed! Well done! Time: " + document.getElementById("timer").textContent);
            } else if (outcome.alreadySolved) {
                showMessage("answerMessage", "This stage was already solved.");
            } else {
                var rewardText = "";
                if (outcome.revealedClue && stage.evidenceMap && stage.evidenceMap[outcome.revealedClue]) {
                    rewardText = " Unlocked: " + stage.evidenceMap[outcome.revealedClue];
                }
                showMessage("answerMessage", "Correct answer!" + rewardText);
            }

            loadStages();
            loadClues();
            renderMissionList();
            updateProgress();
            loadDiary(); // Refresh diary to show auto-logged clue!
        } else {
            if (stage.type === "COLLABORATIVE") {
                showMessage("answerMessage", "Submitted! Collaborative puzzles require 2 different team members to submit the correct answer.");
            } else {
                showMessage("answerMessage", "Incorrect answer. Try again.");
            }
        }
    } catch (e) {
        showMessage("answerMessage", "Could not connect to backend.");
    }
}

// -------------------- Shared Diary (GET/POST/DELETE /api/diary) --------------------
async function loadDiary() {
    if (!currentTeam) {
        diaryNotes = [];
        displayDiary();
        return;
    }

    try {
        var res = await fetch("/api/diary/team/" + currentTeam.id);
        if (res.ok) {
            diaryNotes = await res.json();
            displayDiary();
        }
    } catch (e) {
        // Keep current list on transient error
    }
}

async function addNote() {
    var text = document.getElementById("diaryInput").value.trim();
    if (!text) return;

    if (!currentTeam || !currentUser) {
        alert("Please log in and join a team before adding diary notes.");
        return;
    }

    try {
        var res = await fetch("/api/diary", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                teamId: currentTeam.id,
                authorName: currentUser.username,
                content: text
            })
        });

        if (res.ok) {
            document.getElementById("diaryInput").value = "";
            loadDiary();
        }
    } catch (e) {
        alert("Could not save diary note to backend.");
    }
}

async function deleteNote(noteId) {
    try {
        var res = await fetch("/api/diary/" + noteId, { method: "DELETE" });
        if (res.ok) {
            loadDiary();
        }
    } catch (e) {
        alert("Could not delete diary note.");
    }
}

function displayDiary() {
    var list = document.getElementById("diaryList");
    list.innerHTML = "";

    if (!currentTeam) {
        list.innerHTML = "<li>Join or create a team to use the shared diary.</li>";
        return;
    }

    if (diaryNotes.length === 0) {
        list.innerHTML = "<li>No notes yet.</li>";
        return;
    }

    for (var i = 0; i < diaryNotes.length; i++) {
        var entry = diaryNotes[i];
        var item = document.createElement("li");

        var timeStr = entry.createdAt
            ? new Date(entry.createdAt).toLocaleTimeString()
            : "";

        var span = document.createElement("span");
        span.textContent = entry.content + " — " + entry.authorName + (timeStr ? " (" + timeStr + ") " : " ");

        var delBtn = document.createElement("button");
        delBtn.textContent = "Delete";
        delBtn.className = "secondary-button";
        delBtn.style.marginLeft = "8px";
        delBtn.onclick = (function (id) {
            return function () { deleteNote(id); };
        })(entry.id);

        item.appendChild(span);
        item.appendChild(delBtn);
        list.appendChild(item);
    }
}

// -------------------- Progress --------------------
function updateProgress() {
    var total = 0;
    var completed = 0;
    var container = document.getElementById("missionProgress");
    container.innerHTML = "";

    for (var i = 0; i < missions.length; i++) {
        var mission = missions[i];
        var stageCount = mission.stages.length;
        var session = sessionsByMission[mission.id];
        var missionCompleted = session && session.solvedPuzzles ? session.solvedPuzzles.length : 0;

        total += stageCount;
        completed += missionCompleted;

        var p = document.createElement("p");
        p.textContent = mission.name + ": " + missionCompleted + "/" + stageCount + " stages completed" +
            (session && session.status === "COMPLETED" ? " (ESCAPED!)" : "");
        container.appendChild(p);
    }

    document.getElementById("progressText").textContent =
        completed + " of " + total + " stages completed";

    var pct = total > 0 ? Math.round((completed / total) * 100) : 0;
    document.getElementById("progressBar").style.width = pct + "%";
}

// -------------------- Timer --------------------
function updateTimer() {
    var minutes = Math.floor(seconds / 60);
    var remainingSeconds = seconds % 60;
    var mm = minutes < 10 ? "0" + minutes : "" + minutes;
    var ss = remainingSeconds < 10 ? "0" + remainingSeconds : "" + remainingSeconds;
    document.getElementById("timer").textContent = mm + ":" + ss;
}

setInterval(function () {
    var gameVisible = !document.getElementById("gamePage").classList.contains("hidden");
    var isActive = currentSession && currentSession.status !== "COMPLETED";
    if (gameVisible && selectedMissionIndex !== null && isActive) {
        seconds++;
        updateTimer();
    }
}, 1000);

// -------------------- Backend Search & Status --------------------
function searchAPI() {
    var searchText = document.getElementById("searchInput").value.trim();
    var result = document.getElementById("searchResult");

    if (!searchText) {
        result.textContent = "Please enter something to search.";
        return;
    }

    result.textContent = "Searching...";

    fetch("/api/search?query=" + encodeURIComponent(searchText))
        .then(function (response) {
            if (!response.ok) throw new Error("Search failed");
            return response.json();
        })
        .then(function (data) {
            result.textContent = data.message || "Search completed.";
        })
        .catch(function () {
            result.textContent = "Could not connect to the backend.";
        });
}

function checkBackend() {
    fetch("/api/test")
        .then(function (response) {
            if (!response.ok) throw new Error("Backend error");
            return response.json();
        })
        .then(function (data) {
            document.getElementById("backendStatus").textContent = data.message || "Backend connected";
            if (data.version) document.getElementById("version").textContent = data.version;
            if (data.branch) document.getElementById("commit").textContent = data.branch;
        })
        .catch(function () {
            document.getElementById("backendStatus").textContent = "Backend not connected";
        });
}

function showMessage(elementId, message) {
    document.getElementById(elementId).textContent = message;
}

// -------------------- Init --------------------
restoreLocalState();
loadMissions();
loadDiary();
checkBackend();