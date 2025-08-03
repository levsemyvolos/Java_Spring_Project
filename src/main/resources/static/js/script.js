document.addEventListener('DOMContentLoaded', initLearnPage);

function initLearnPage() {
    const sentenceContainer = document.getElementById('sentence');
    const translationContainer = document.getElementById('translation');
    const synonymsContainer = document.getElementById('synonyms');
    const counterContainer = document.getElementById('cardCounter');
    const nextButton = document.getElementById('nextButton');
    const lastAnsweredContainer = document.getElementById('lastAnswered');
    const learningPage = document.getElementById('learningPage');
    const resultPage = document.getElementById('resultPage');
    const resultContainer = document.getElementById('resultContainer');

    let cards = [];
    let currentCardIndex = 0;
    let answers = {};

    fetch('/api/learn/get-cards')
        .then(response => response.json())
        .then(data => {
            cards = data;
            if (cards.length > 0) {
                cards.forEach(card => {
                    answers[card.cardId] = true;
                });
                updateUI();
            } else {
                sentenceContainer.textContent = "No words to learn.";
            }
        })
        .catch(error => {
            console.error('Error fetching cards:', error);
            sentenceContainer.textContent = "Error loading words.";
        });

    function updateUI() {
        const card = cards[currentCardIndex];

        // Remove asterisks from the sentence
        const cleanedSentence = card.sentence.replace(/\*/g, '');

        sentenceContainer.innerHTML = cleanedSentence.replace(
            card.word,
            `<input type="text" id="answerInput" class="form-control d-inline" autocomplete="off">`
        );
        translationContainer.textContent = `Translation: ${card.translation}`;
        synonymsContainer.textContent = `Synonyms: ${card.synonyms}`;
        counterContainer.textContent = `${currentCardIndex + 1}/${cards.length}`;

        lastAnsweredContainer.textContent = card.lastAnsweredFormatted === "Новое слово"
            ? "New word"
            : `Last answered: ${card.lastAnsweredFormatted}`;

        let inputField = document.getElementById("answerInput");
        inputField.style.backgroundColor = "";
        inputField.value = "";
        inputField.placeholder = "";
        adjustInputWidth(inputField, card.word);
        inputField.addEventListener("keyup", function (event) {
            if (event.key === "Enter") {
                checkAnswer(card, inputField.value);
            }
        });
        inputField.focus();
    }

    function checkAnswer(card, userAnswer) {
        if (userAnswer.trim().toLowerCase() === card.word.toLowerCase()) {
            showIntermediateStage(card);
        } else {
            answers[card.cardId] = false;
            let inputField = document.getElementById("answerInput");
            inputField.classList.add("alert-danger");
            inputField.value = '';
            inputField.placeholder = card.word;
            inputField.focus();
        }
    }

    function showIntermediateStage(card) {
        document.getElementById("answerInput").outerHTML = `<span class="correct">${card.word}</span>`;
        lastAnsweredContainer.textContent = `Next repetition: ${card.dueFormattedTrue}`;
        nextButton.disabled = false;
        nextButton.addEventListener('click', nextCard, { once: true });
    }

    function nextCard() {
        currentCardIndex++;
        if (currentCardIndex < cards.length) {
            updateUI();
        } else {
            showResults();
        }
        nextButton.disabled = false;
    }

    function showResults() {
        learningPage.style.display = 'none';
        resultPage.style.display = 'block';

        resultContainer.innerHTML = '';
        for (const cardId in answers) {
            const isCorrect = answers[cardId];
            const card = cards.find(c => c.cardId === parseInt(cardId));
            const resultDiv = document.createElement('div');
            resultDiv.textContent = `${card.word} - ${card.translation}`;
            resultDiv.classList.add(isCorrect ? 'correct' : 'incorrect');
            resultContainer.appendChild(resultDiv);
        }

        submitAnswers();
    }

    function submitAnswers() {
        fetch('/api/learn/answer', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(answers)
        })
            .catch(error => console.error('Error submitting answers:', error));
    }

    function adjustInputWidth(input, word) {
        const tempSpan = document.createElement("span");
        tempSpan.style.visibility = "hidden";
        tempSpan.style.whiteSpace = "nowrap";
        tempSpan.style.fontSize = getComputedStyle(input).fontSize;
        tempSpan.textContent = word;
        document.body.appendChild(tempSpan);
        const width = tempSpan.offsetWidth;
        document.body.removeChild(tempSpan);
        input.style.width = `${width}px`;
    }
}