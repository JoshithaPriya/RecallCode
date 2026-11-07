
document.addEventListener("DOMContentLoaded", () => {
    loadDashboardSummary();
    loadRevisionQueue();
});

async function loadDashboardSummary() {
    try {
        const response = await fetch("/api/dashboard/summary");

        if (!response.ok) {
            throw new Error("Could not load dashboard summary");
        }

        const data = await response.json();

        document.getElementById("totalProblems").textContent =
            data.totalProblems ?? 0;

        document.getElementById("solvedAttempts").textContent =
            data.solvedAttempts ?? 0;

        document.getElementById("totalAttempts").textContent =
            data.totalAttempts ?? 0;

        const average = Number(data.averageTimeMinutes ?? 0);
        document.getElementById("averageTime").textContent =
            `${average.toFixed(1)} min`;
    } catch (error) {
        console.error("Dashboard summary error:", error);
    }
}

async function loadRevisionQueue() {
    const list = document.getElementById("revisionList");
    const count = document.getElementById("revisionCount");

    try {
        const response = await fetch("/api/revisions");

        if (!response.ok) {
            throw new Error("Could not load revision queue");
        }

        const problems = await response.json();
        count.textContent = `${problems.length} due`;

        if (problems.length === 0) {
            list.innerHTML =
                '<p class="empty-state">You have no revisions due. Nice work!</p>';
            return;
        }

        list.replaceChildren();

        problems.slice(0, 4).forEach(problem => {
            const item = document.createElement("div");
            item.className = "revision-item";

            const details = document.createElement("div");
            const name = document.createElement("strong");
            const metadata = document.createElement("small");
            const button = document.createElement("button");

            name.textContent = problem.title;
            metadata.textContent =
                `${problem.platform || "Other"} · ${problem.difficulty || "Unspecified"}`;

            button.className = "revision-action";
            button.type = "button";
            button.textContent = "Practice";
            button.addEventListener("click", () => {
                // The practice-session section will handle this in Part 2.
                window.dispatchEvent(new CustomEvent("recallcode:select-problem", {
                    detail: { problem }
                }));
            });

            details.append(name, metadata);
            item.append(details, button);
            list.appendChild(item);
        });

        if (problems.length > 4) {
            const more = document.createElement("p");
            more.className = "empty-state";
            more.textContent = `+ ${problems.length - 4} more due for revision`;
            list.appendChild(more);
        }
    } catch (error) {
        console.error("Revision queue error:", error);
        list.innerHTML =
            '<p class="empty-state">Unable to load revisions. Check the backend.</p>';
        count.textContent = "Unavailable";
    }
}
