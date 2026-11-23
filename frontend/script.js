
// Show one page and hide the others
function showPage(pageId) {
    var pages = document.getElementsByClassName("page");

    for (var i = 0; i < pages.length; i++) {
        pages[i].classList.add("hidden");
    }

    document.getElementById(pageId).classList.remove("hidden");
}

// Demo login form
function login(event) {
    event.preventDefault();

    var username = document.getElementById("loginUsername").value;
    document.getElementById("loginMessage").textContent =
        "Login form submitted for " + username + ". Backend login is not connected yet.";
}

// Demo register form
function register(event) {
    event.preventDefault();

    var username = document.getElementById("registerUsername").value;
    document.getElementById("loginMessage").textContent =
        "Registration form submitted for " + username + ". Backend registration is not connected yet.";
}

// Create a team (temporary frontend demo)
function createTeam() {
    var teamName = document.getElementById("newTeamName").value;

    if (teamName === "") {
        document.getElementById("teamMessage").textContent = "Please enter a team name.";
        return;
    }

    document.getElementById("teamName").textContent = teamName;
    document.getElementById("teamMessage").textContent = "Team created for this demo.";
}

// Join a team (temporary frontend demo)
function joinTeam() {
    var teamName = document.getElementById("joinTeamName").value;

    if (teamName === "") {
        document.getElementById("teamMessage").textContent = "Please enter a team name.";
        return;
    }

    document.getElementById("teamName").textContent = teamName;
    document.getElementById("teamMessage").textContent = "You joined this team in the demo.";
}

// Select a mission and start the timer
function selectMission(missionName) {
    document.getElementById("selectedMission").textContent = missionName;
    document.getElementById("answerMessage").textContent = "";
    document.getElementById("answerInput").value = "";

    seconds = 0;
    updateTimer();
    showPage("gamePage");
}

// Submit an answer (placeholder)
function submitAnswer() {
    var answer = document.getElementById("answerInput").value;

    if (answer === "") {
        document.getElementById("answerMessage").textContent = "Please enter an answer.";
    } else {
        document.getElementById("answerMessage").textContent =
            "Answer submitted. Answer checking will be connected to the backend later.";
    }
}

// Add a note to the diary (temporary, stored only in this page)
function addNote() {
    var noteText = document.getElementById("diaryInput").value;

    if (noteText === "") {
        return;
    }

    var note = document.createElement("li");
    note.textContent = noteText;

    document.getElementById("diaryList").appendChild(note);
    document.getElementById("diaryInput").value = "";
}

// Timer
var seconds = 0;

function updateTimer() {
    var minutes = Math.floor(seconds / 60);
    var remainingSeconds = seconds % 60;

    if (minutes < 10) {
        minutes = "0" + minutes;
    }

    if (remainingSeconds < 10) {
        remainingSeconds = "0" + remainingSeconds;
    }

    document.getElementById("timer").textContent = minutes + ":" + remainingSeconds;
}

setInterval(function() {
    if (!document.getElementById("gamePage").classList.contains("hidden")) {
        seconds++;
        updateTimer();
    }
}, 1000);


// ====================================
// BACKEND COMPATIBILITY
// ====================================

var backendURL = "http://localhost:8080";

// Check if the backend is running
function checkBackend() {
    fetch(backendURL + "/api/test")
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Backend error");
            }

            return response.json();
        })
        .then(function(data) {
            console.log("Backend connected!");
            console.log(data);
        })
        .catch(function() {
            console.log("Backend is not connected.");
        });
}

// Search using the backend
function searchAPI() {
    var searchText = document.getElementById("searchInput").value;
    var searchResult = document.getElementById("searchResult");

    if (searchText === "") {
        searchResult.textContent = "Please enter something to search.";
        return;
    }

    searchResult.textContent = "Searching...";

    fetch(backendURL + "/api/search?query=" + encodeURIComponent(searchText))
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Search failed");
            }

            return response.json();
        })
        .then(function(data) {
            searchResult.textContent = data.message || "Search completed.";
        })
        .catch(function() {
            searchResult.textContent = "Could not connect to the backend.";
        });
}

// Get version information from the backend
function getBackendInfo() {
    fetch(backendURL + "/actuator/info")
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Backend is not available");
            }

            return response.json();
        })
        .then(function(data) {
            if (data.app) {
                document.getElementById("version").textContent = data.app.version || "Unknown";
                document.getElementById("commit").textContent = data.app.commit || "Unknown";
            } else {
                document.getElementById("version").textContent = "Connected";
                document.getElementById("commit").textContent = "Not provided";
            }
        })
        .catch(function() {
            document.getElementById("version").textContent = "Backend not connected";
            document.getElementById("commit").textContent = "Backend not connected";
        });
}

// Start backend checks when the page loads
checkBackend();
getBackendInfo();
