
// Current user and team
var currentUser = "";
var currentTeam = "";

// Timer
var seconds = 0;

// Current game
var selectedMission = null;
var selectedStage = 0;

// Temporary diary
var diaryNotes = [];

// Mock mission data
var missions = [
    {
        name: "Mission 1",
        description: "Placeholder description for Mission 1.",
        stages: [
            {
                description: "Placeholder challenge for Stage 1.",
                answer: "a",
                clues: ["1st letter of alphabet."],
                unlocked: true,
                completed: false
            },
            {
                description: "Placeholder challenge for Stage 2.",
                answer: "b",
                clues: ["2nd letter of alphabet"],
                unlocked: false,
                completed: false
            },
            {
                description: "Placeholder challenge for Stage 3.",
                answer: "c",
                clues: ["3d letter of alphabet."],
                unlocked: false,
                completed: false
            }
        ]
    },
    {
        name: "Mission 2",
        description: "Placeholder description for Mission 2.",
        stages: [
            {

                description: "Placeholder challenge for Stage 1.",
                answer: "a",
                clues: ["1st letter of alphabet."],
                unlocked: true,
                completed: false
            },
            {
                description: "Placeholder challenge for Stage 2.",
                answer: "b",
                clues: ["2nd letter of alphabet"],
                unlocked: false,
                completed: false
            },
            {
                description: "Placeholder challenge for Stage 3.",
                answer: "c",
                clues: ["3d letter of alphabet."],
                unlocked: false,
                completed: false
            }
        ]
    },
    {
        name: "Mission 3",
        description: "Placeholder description for Mission 3.",
        stages: [
            {
                description: "Placeholder challenge for Stage 1.",
                answer: "a",
                clues: ["1st letter of alphabet."],
                unlocked: true,
                completed: false
            },
            {
                description: "Placeholder challenge for Stage 2.",
                answer: "b",
                clues: ["2nd letter of alphabet"],
                unlocked: false,
                completed: false
            },
            {
                description: "Placeholder challenge for Stage 3.",
                answer: "c",
                clues: ["3d letter of alphabet."],
                unlocked: false,
                completed: false

            }
        ]
    }
];

// Show one page
function showPage(pageId) {

    var pages = document.getElementsByClassName("page");

    for (var i = 0; i < pages.length; i++) {
        pages[i].classList.add("hidden");
    }

    document.getElementById(pageId).classList.remove("hidden");

    if (pageId === "progressPage") {
        updateProgress();
    }
}

// Login demo
function login(event) {

    event.preventDefault();

    var username = document.getElementById("loginUsername").value;
    var password = document.getElementById("loginPassword").value;

    if (username.trim() === "" || password === "") {
        showMessage("loginMessage", "Please fill in all fields.");
        return;
    }

    currentUser = username;

    document.getElementById("currentUser").textContent =
        "Logged in as: " + currentUser;

    showMessage("loginMessage",
        "Demo login successful. No account was saved.");

}

// Register demo
function register(event) {

    event.preventDefault();

    var username = document.getElementById("registerUsername").value;
    var password = document.getElementById("registerPassword").value;

    if (username.trim() === "" || password === "") {
        showMessage("loginMessage", "Please fill in all fields.");
        return;
    }

    currentUser = username;

    document.getElementById("currentUser").textContent =
        "Logged in as: " + currentUser;

    showMessage("loginMessage",
        "Demo registration successful. No account was saved.");

}

// Create a team
function createTeam() {

    var name = document.getElementById("newTeamName").value.trim();

    if (name === "") {
        showMessage("teamMessage", "Please enter a team name.");
        return;
    }

    currentTeam = name;

    document.getElementById("teamName").textContent = currentTeam;
    document.getElementById("teamMembers").textContent =
        currentUser ? "Members: " + currentUser : "Members: Demo player";

    showMessage("teamMessage", "Team created in the demo.");
    document.getElementById("newTeamName").value = "";
}

// Join a team
function joinTeam() {

    var name = document.getElementById("joinTeamName").value.trim();

    if (name === "") {
        showMessage("teamMessage", "Please enter a team name.");
        return;
    }

    currentTeam = name;

    document.getElementById("teamName").textContent = currentTeam;
    document.getElementById("teamMembers").textContent =
        currentUser ? "Members: " + currentUser : "Members: Demo player";

    showMessage("teamMessage", "You joined this team in the demo.");
    document.getElementById("joinTeamName").value = "";
}

// Show missions
function loadMissions() {

    var list = document.getElementById("missionList");
    list.innerHTML = "";

    for (var i = 0; i < missions.length; i++) {

        var mission = missions[i];
        var completed = 0;

        for (var j = 0; j < mission.stages.length; j++) {
            if (mission.stages[j].completed) {
                completed++;
            }
        }

        var panel = document.createElement("div");
        panel.className = "panel mission";

        var title = document.createElement("h3");
        title.textContent = mission.name;

        var description = document.createElement("p");
        description.textContent = mission.description;

        var progress = document.createElement("p");
        progress.textContent = "Progress: " + completed + "/3 stages";

        var button = document.createElement("button");
        button.className = "main-button";
        button.textContent = "Select Mission";

        button.onclick = function(index) {
            return function() {
                selectMission(index);
            };
        }(i);

        panel.appendChild(title);
        panel.appendChild(description);
        panel.appendChild(progress);
        panel.appendChild(button);

        list.appendChild(panel);
    }
}

// Select a mission
function selectMission(index) {

    selectedMission = index;
    selectedStage = 0;
    seconds = 0;

    document.getElementById("selectedMission").textContent =
        missions[index].name;

    document.getElementById("answerInput").value = "";
    showMessage("answerMessage", "");

    loadStages();
    showPage("gamePage");
    updateTimer();
    openStage(0);
}

// Show stages
function loadStages() {

    var list = document.getElementById("stageList");
    list.innerHTML = "";

    var stages = missions[selectedMission].stages;

    for (var i = 0; i < stages.length; i++) {

        var button = document.createElement("button");
        button.className = "stage-button";

        if (stages[i].completed) {
            button.textContent = "Stage " + (i + 1) + " - Completed";
            button.classList.add("completed");
        } else if (stages[i].unlocked) {
            button.textContent = "Stage " + (i + 1) + " - Open";
        } else {
            button.textContent = "Stage " + (i + 1) + " - Locked";
            button.disabled = true;
        }

        button.onclick = function(index) {
            return function() {
                openStage(index);
            };
        }(i);

        list.appendChild(button);
    }
}

// Open a stage
function openStage(index) {

    var stage = missions[selectedMission].stages[index];

    if (!stage.unlocked) {
        showMessage("answerMessage",
            "This stage is locked. Complete the previous stage first.");
        return;
    }

    selectedStage = index;

    document.getElementById("stageContent").classList.remove("hidden");
    document.getElementById("stageTitle").textContent =
        "Stage " + (index + 1);

    document.getElementById("stageNumber").textContent =
        (index + 1) + " / 3";

    document.getElementById("stageDescription").textContent =
        stage.description;

    document.getElementById("answerInput").value = "";

    showMessage("answerMessage", "");
    showMessage("clueMessage", "");

    loadClues();
}

// Show clues
function loadClues() {

    var list = document.getElementById("clueList");
    list.innerHTML = "";

    var stage = missions[selectedMission].stages[selectedStage];

    for (var i = 0; i < stage.clues.length; i++) {

        var clue = document.createElement("p");
        clue.textContent = "Clue " + (i + 1) + ": Hidden";
        clue.id = "clue" + i;

        list.appendChild(clue);
    }

    stage.revealedClues = 0;
}

// Reveal next clue
function showNextClue() {

    var stage = missions[selectedMission].stages[selectedStage];

    if (stage.revealedClues >= stage.clues.length) {
        showMessage("clueMessage", "No more clues available.");
        return;
    }

    var index = stage.revealedClues;

    document.getElementById("clue" + index).textContent =
        "Clue " + (index + 1) + ": " + stage.clues[index];

    stage.revealedClues++;

    showMessage("clueMessage", "Clue revealed.");
}

// Submit an answer
function submitAnswer() {

    if (selectedMission === null) {
        showMessage("answerMessage", "Please select a mission first.");
        return;
    }

    var answer = document.getElementById("answerInput").value.trim();

    if (answer === "") {
        showMessage("answerMessage", "Please enter an answer.");
        return;
    }

    var stage = missions[selectedMission].stages[selectedStage];

    if (answer.toLowerCase() === stage.answer.toLowerCase()) {

        stage.completed = true;

        if (selectedStage < 2) {
            missions[selectedMission].stages[selectedStage + 1].unlocked = true;

            showMessage("answerMessage",
                "Correct answer! The next stage is now unlocked.");
        } else {
            showMessage("answerMessage",
                "Mission completed! Well done.");
        }

        loadStages();
        updateProgress();
        loadMissions();

    } else {

        showMessage("answerMessage",
            "Incorrect answer. Try again.");

    }
}

// Add a diary note
function addNote() {

    var text = document.getElementById("diaryInput").value.trim();

    if (text === "") {
        return;
    }

    var note = {
        text: text,
        author: currentUser || "Demo player",
        time: new Date().toLocaleTimeString()
    };

    diaryNotes.push(note);

    document.getElementById("diaryInput").value = "";

    displayDiary();
}

// Display diary notes
function displayDiary() {

    var list = document.getElementById("diaryList");
    list.innerHTML = "";

    for (var i = 0; i < diaryNotes.length; i++) {

        var item = document.createElement("li");

        item.textContent =
            diaryNotes[i].text + " — " +
            diaryNotes[i].author + " (" +
            diaryNotes[i].time + ")";

        list.appendChild(item);
    }
}

// Update progress
function updateProgress() {

    var total = 0;
    var completed = 0;

    for (var i = 0; i < missions.length; i++) {

        var mission = missions[i];
        var missionCompleted = 0;

        total += mission.stages.length;

        for (var j = 0; j < mission.stages.length; j++) {
            if (mission.stages[j].completed) {
                completed++;
                missionCompleted++;
            }
        }

        document.getElementById("missionProgress").innerHTML =
            document.getElementById("missionProgress").innerHTML || "";

        var progressItem = document.getElementById("mission-" + i);

        if (!progressItem) {
            progressItem = document.createElement("p");
            progressItem.id = "mission-" + i;
            document.getElementById("missionProgress").appendChild(progressItem);
        }

        progressItem.textContent =
            mission.name + ": " + missionCompleted + "/3 stages completed";
    }

    document.getElementById("progressText").textContent =
        completed + " of " + total + " stages completed";

    document.getElementById("progressBar").style.width =
        (completed / total * 100) + "%";
}

// Timer
function updateTimer() {

    var minutes = Math.floor(seconds / 60);
    var remainingSeconds = seconds % 60;

    if (minutes < 10) {
        minutes = "0" + minutes;
    }

    if (remainingSeconds < 10) {
        remainingSeconds = "0" + remainingSeconds;
    }

    document.getElementById("timer").textContent =
        minutes + ":" + remainingSeconds;
}

setInterval(function() {

    if (!document.getElementById("gamePage").classList.contains("hidden")
        && selectedMission !== null) {

        seconds++;
        updateTimer();
    }

}, 1000);

// Search using the backend
function searchAPI() {

    var searchText = document.getElementById("searchInput").value.trim();
    var result = document.getElementById("searchResult");

    if (searchText === "") {
        result.textContent = "Please enter something to search.";
        return;
    }

    result.textContent = "Searching...";

    fetch("/api/search?query=" + encodeURIComponent(searchText))
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Search failed");
            }

            return response.json();
        })
        .then(function(data) {
            result.textContent = data.message || "Search completed.";
        })
        .catch(function() {
            result.textContent = "Could not connect to the backend.";
        });
}

// Check backend connection
function checkBackend() {

    fetch("/api/test")
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Backend error");
            }

            return response.json();
        })
        .then(function(data) {
            document.getElementById("backendStatus").textContent =
                data.message || "Backend connected";
        })
        .catch(function() {
            document.getElementById("backendStatus").textContent =
                "Backend not connected";
        });
}

// Get Git information
function getBackendInfo() {

    fetch("/actuator/info")
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Backend error");
            }

            return response.json();
        })
        .then(function(data) {

            document.getElementById("version").textContent =
                data.app.version || "Unknown";

            document.getElementById("commit").textContent =
                data.app.commit || "Unknown";
        })
        .catch(function() {

            document.getElementById("version").textContent =
                "Unavailable";

            document.getElementById("commit").textContent =
                "Unavailable";
        });
}

// Show a message
function showMessage(elementId, message) {
    document.getElementById(elementId).textContent = message;
}

// Start the page
loadMissions();
displayDiary();
updateProgress();
checkBackend();
getBackendInfo();
