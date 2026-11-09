
(() => {
    const PAGE_SIZE = 10;

    let allProblems = [];
    let currentPage = 1;
    let selectedProblem = null;

    const $ = (id) => document.getElementById(id);

    const list = $("libraryProblemList");
    const searchInput = $("librarySearch");
    const difficultyFilter = $("libraryDifficulty");

    function safeText(value, fallback = "—") {
        return value === null || value === undefined || value === ""
            ? fallback
            : String(value);
    }

    function formatDifficulty(value) {
        return safeText(value, "Unknown").toUpperCase();
    }

    function difficultyClass(value) {
        const difficulty = String(value || "").toLowerCase();

        if (difficulty === "easy") return "difficulty-easy";
        if (difficulty === "medium") return "difficulty-medium";
        if (difficulty === "hard") return "difficulty-hard";

        return "";
    }

    function makeElement(tag, className, text) {
        const element = document.createElement(tag);

        if (className) element.className = className;
        if (text !== undefined) element.textContent = text;

        return element;
    }

    function getFilteredProblems() {
        const query = searchInput.value.trim().toLowerCase();
        const difficulty = difficultyFilter.value;

        return allProblems.filter((problem) => {
            const title = String(problem.title || "").toLowerCase();
            const platform = String(problem.platform || "").toLowerCase();

            const matchesSearch =
                !query ||
                title.includes(query) ||
                platform.includes(query);

            const matchesDifficulty =
                !difficulty ||
                formatDifficulty(problem.difficulty) === difficulty;

            return matchesSearch && matchesDifficulty;
        });
    }

    function renderProblemList() {
        const filtered = getFilteredProblems();
        const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));

        currentPage = Math.min(currentPage, totalPages);

        const start = (currentPage - 1) * PAGE_SIZE;
        const pageProblems = filtered.slice(start, start + PAGE_SIZE);

        list.replaceChildren();
        $("libraryTotal").textContent =
            `${filtered.length} problem${filtered.length === 1 ? "" : "s"}`;

        $("libraryPageInfo").textContent =
            `Page ${currentPage} of ${totalPages}`;

        $("libraryPrev").disabled = currentPage <= 1;
        $("libraryNext").disabled = currentPage >= totalPages;

        if (pageProblems.length === 0) {
            list.append(
                makeElement("p", "empty-state", "No matching problems found.")
            );
            return;
        }

        pageProblems.forEach((problem) => {
            const button = makeElement("button", "library-problem-item");
            button.type = "button";

            if (selectedProblem?.id === problem.id) {
                button.classList.add("is-selected");
                button.setAttribute("aria-pressed", "true");
            } else {
                button.setAttribute("aria-pressed", "false");
            }

            button.append(
                makeElement(
                    "span",
                    "library-problem-title",
                    safeText(problem.title, "Untitled problem")
                )
            );

            const metadata = makeElement("span", "library-problem-meta");

            metadata.append(
                makeElement("span", "", safeText(problem.platform, "Platform not set")),
                makeElement(
                    "span",
                    difficultyClass(problem.difficulty),
                    formatDifficulty(problem.difficulty)
                )
            );

            button.append(metadata);

            button.addEventListener("click", () => selectProblem(problem));
            list.append(button);
        });
    }

    async function loadProblems() {
        list.replaceChildren(
            makeElement("p", "empty-state", "Loading problems...")
        );

        try {
            const response = await fetch("/api/problems");

            if (!response.ok) {
                throw new Error(`Could not load problems (${response.status}).`);
            }

            const data = await response.json();
            allProblems = Array.isArray(data) ? data : [];

            // Keep the selected problem in sync with the latest server data.
            if (selectedProblem) {
                selectedProblem =
                    allProblems.find((p) => p.id === selectedProblem.id) || null;
            }

            renderProblemList();

            if (selectedProblem) {
                renderSelectedProblem();
            } else {
                showEmptyDetail();
            }
        } catch (error) {
            list.replaceChildren(
                makeElement("p", "empty-state", error.message)
            );
        }
    }

    function showEmptyDetail() {
        $("libraryEmptyDetail").hidden = false;
        $("librarySelectedDetail").hidden = true;
    }

    function selectProblem(problem) {
        selectedProblem = problem;
        renderProblemList();
        renderSelectedProblem();

        // The timer listens for this event and loads the selected problem.
        window.dispatchEvent(
            new CustomEvent("recallcode:select-problem", {
                detail: { problem }
            })
        );
    }

    function renderSelectedProblem() {
        if (!selectedProblem) {
            showEmptyDetail();
            return;
        }

        $("libraryEmptyDetail").hidden = true;
        $("librarySelectedDetail").hidden = false;

        $("detailProblemTitle").textContent =
            safeText(selectedProblem.title, "Untitled problem");

        $("detailPlatform").textContent =
            safeText(selectedProblem.platform, "Platform not set");

        $("detailDifficulty").textContent =
            formatDifficulty(selectedProblem.difficulty);

        const link = $("detailProblemLink");
        const url = String(selectedProblem.problemUrl || "");

        // Only expose HTTP/HTTPS links in the UI.
        try {
            const parsed = new URL(url);

            if (parsed.protocol === "http:" || parsed.protocol === "https:") {
                link.href = parsed.href;
                link.hidden = false;
            } else {
                link.removeAttribute("href");
                link.hidden = true;
            }
        } catch {
            link.removeAttribute("href");
            link.hidden = true;
        }

        $("detailAttemptCount").textContent = "…";
        $("detailBestTime").textContent = "…";
        $("libraryAttemptHistory").replaceChildren(
            makeElement("p", "empty-state", "Loading attempt history...")
        );

        loadAttemptHistory(selectedProblem.id);
    }

    function formatDate(value) {
        if (!value) return "Date unavailable";

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return String(value);
        }

        return date.toLocaleString();
    }

    function renderAttemptHistory(attempts, problemId) {
        // Ignore a slow response if the user selected a different problem.
        if (selectedProblem?.id !== problemId) return;

        const history = $("libraryAttemptHistory");
        history.replaceChildren();

        $("detailAttemptCount").textContent = String(attempts.length);

        const solvedTimes = attempts
            .filter((attempt) =>
                String(attempt.outcome || "").toUpperCase() === "SOLVED"
            )
            .map((attempt) => Number(attempt.timeTakenMinutes))
            .filter((minutes) => Number.isFinite(minutes) && minutes >= 0);

        $("detailBestTime").textContent = solvedTimes.length
            ? `${Math.min(...solvedTimes)} min`
            : "—";

        // Newest attempts first.
        attempts.sort((a, b) => {
            const dateA = new Date(a.attemptedAt || 0).getTime();
            const dateB = new Date(b.attemptedAt || 0).getTime();
            return dateB - dateA;
        });

        if (attempts.length === 0) {
            history.append(
                makeElement("p", "empty-state", "No attempts yet. Start practising!")
            );
            return;
        }

        attempts.forEach((attempt) => {
            const card = makeElement("article", "attempt-card");
            const top = makeElement("div", "attempt-card-top");

            const outcome = String(attempt.outcome || "Unknown")
                .replaceAll("_", " ");

            const outcomeClass =
                outcome.toUpperCase() === "SOLVED"
                    ? "difficulty-easy"
                    : outcome.toUpperCase() === "PARTIALLY SOLVED"
                        ? "difficulty-medium"
                        : "difficulty-hard";

            top.append(
                makeElement("span", `attempt-outcome ${outcomeClass}`, outcome),
                makeElement(
                    "span",
                    "attempt-duration",
                    `${safeText(attempt.timeTakenMinutes, "—")} min`
                )
            );

            card.append(
                top,
                makeElement("div", "attempt-date", formatDate(attempt.attemptedAt))
            );

            if (attempt.notes) {
                card.append(makeElement("p", "attempt-notes", attempt.notes));
            }

            history.append(card);
        });
    }

    async function loadAttemptHistory(problemId) {
        try {
            const response = await fetch(
                `/api/problems/${encodeURIComponent(problemId)}/attempts`
            );

            if (!response.ok) {
                throw new Error(
                    `Could not load attempt history (${response.status}).`
                );
            }

            const attempts = await response.json();

            renderAttemptHistory(
                Array.isArray(attempts) ? attempts : [],
                problemId
            );
        } catch (error) {
            if (selectedProblem?.id !== problemId) return;

            $("libraryAttemptHistory").replaceChildren(
                makeElement("p", "empty-state", error.message)
            );
            $("detailAttemptCount").textContent = "—";
            $("detailBestTime").textContent = "—";
        }
    }

    searchInput.addEventListener("input", () => {
        currentPage = 1;
        renderProblemList();
    });

    difficultyFilter.addEventListener("change", () => {
        currentPage = 1;
        renderProblemList();
    });

    $("libraryPrev").addEventListener("click", () => {
        if (currentPage > 1) {
            currentPage--;
            renderProblemList();
        }
    });

    $("libraryNext").addEventListener("click", () => {
        const totalPages = Math.max(
            1,
            Math.ceil(getFilteredProblems().length / PAGE_SIZE)
        );

        if (currentPage < totalPages) {
            currentPage++;
            renderProblemList();
        }
    });

    $("practiceSelectedProblem").addEventListener("click", () => {
        if (!selectedProblem) return;

        window.dispatchEvent(
            new CustomEvent("recallcode:select-problem", {
                detail: { problem: selectedProblem }
            })
        );

        $("practiceSession").scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    });


    // Select a problem recommended by the adaptive revision queue.
window.addEventListener("recallcode:select-problem", event => {
    const problem = event.detail?.problem;

    if (!problem) return;

    const selected = allProblems.find(
        p => String(p.id) === String(problem.id ?? problem.problemId)
    );

    if (!selected) return;

    selectedProblem = selected;
    renderProblemList();
    renderSelectedProblem();

    $("practiceSession").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
});
    // Refresh the library after an attempt is saved.
    window.addEventListener("recallcode:attempt-saved", () => {
        loadProblems();
    });

    // Make it possible for other dashboard scripts to refresh the library.
    window.loadProblemLibrary = loadProblems;

    loadProblems();
})();
