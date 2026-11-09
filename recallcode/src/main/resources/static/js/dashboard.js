
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
        const response = await fetch("/api/recommendations");

        if (!response.ok) {
            throw new Error("Could not load recommendations");
        }

        const data = await response.json();
        const recommendations = data.recommendations ?? [];

        count.textContent = `${recommendations.length} recommendations`;
        list.replaceChildren();

        if (recommendations.length === 0) {
            list.innerHTML =
                '<p class="empty-state">No recommendations right now. Keep practising!</p>';
            return;
        }

        recommendations.slice(0, 4).forEach(recommendation => {
            const item = document.createElement("div");
            item.className = "revision-item";

            const details = document.createElement("div");
            details.className = "revision-details";

            const name = document.createElement("strong");
            name.textContent = recommendation.problemTitle;

            const type = document.createElement("small");
            type.textContent = recommendation.recommendationType.replaceAll("_", " ");

            const concept = document.createElement("small");
            concept.textContent =
                `Concept: ${recommendation.relevantConcept || "Not available"}`;

            const metadata = document.createElement("small");
            metadata.textContent =
                `${recommendation.difficulty || "Unspecified"} · ` +
                `Mastery: ${(recommendation.masteryProbability * 100).toFixed(1)}%`;

            const reason = document.createElement("p");
            reason.className = "recommendation-reason";
            reason.textContent = recommendation.reason || "";

            const priority = document.createElement("small");
            priority.textContent =
                `Priority: ${(recommendation.priorityScore * 100).toFixed(1)}%`;

            const revisionDate = document.createElement("small");
            revisionDate.textContent = recommendation.nextRevisionAt
                ? `Next revision: ${new Date(recommendation.nextRevisionAt).toLocaleDateString()}`
                : "New problem · No revision scheduled yet";

            const button = document.createElement("button");
            button.className = "revision-action";
            button.type = "button";
            button.textContent = "Practice";

            button.addEventListener("click", () => {
                window.dispatchEvent(
                    new CustomEvent("recallcode:select-problem", {
                        detail: {
                            problem: {
                                id: recommendation.problemId,
                                problemId: recommendation.problemId,
                                title: recommendation.problemTitle,
                                difficulty: recommendation.difficulty
                            }
                        }
                    })
                );
            });

            details.append(
                name, type, concept, metadata,
                reason, priority, revisionDate
            );

            item.append(details, button);
            list.appendChild(item);
        });

        if (recommendations.length > 4) {
            const more = document.createElement("p");
            more.className = "empty-state";
            more.textContent =
                `+ ${recommendations.length - 4} more recommendations`;
            list.appendChild(more);
        }

    } catch (error) {
        console.error("Recommendation queue error:", error);

        list.innerHTML =
            '<p class="empty-state">Unable to load recommendations.</p>';

        count.textContent = "Unavailable";
    }
}
