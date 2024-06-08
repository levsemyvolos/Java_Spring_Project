document.addEventListener('DOMContentLoaded', () => {
    const path = window.location.pathname;
    if (path.endsWith('/learn')) {
        initLearnPage();
    } else if (path.endsWith('/result')) {
        initResultPage();
    }
});

function initLearnPage() {
    const sentenceContainer = document.getElementById('sentenceContainer');
    const translationContainer = document.getElementById('translation');
    const synonymsContainer = document.getElementById('synonyms');
    const counterContainer = document.getElementById('counter');
    const nextButton = document.getElementById('nextButton');
    const accountButton = document.getElementById('accountButton');

    let cards = [];
    let currentCardIndex = 0;
    let answers = {};

    accountButton.addEventListener('click', () => {
        // Redirect to account page (not implemented)
        alert('Redirect to account page');
    });

    nextButton.addEventListener('click', () => {
        if (currentCardIndex < cards.length) {
            showCard(cards[currentCardIndex]);
        } else {
            submitAnswers();
        }
    });

    fetch('/api/learn/get-cards')
        .then(response => response.json())
        .then(data => {
            cards = data;
            showCard(cards[currentCardIndex]);
        })
        .catch(error => console.error('Error fetching cards:', error));

    function showCard(card) {
        sentenceContainer.innerHTML = card.sentence.replace(card.word, `<input type="text" id="answerInput">`);
        translationContainer.textContent = card.translation;
        synonymsContainer.textContent = card.synonyms;
        counterContainer.textContent = `${currentCardIndex + 1}/${cards.length}`;

        const answerInput = document.getElementById('answerInput');
        answerInput.focus();

        answerInput.addEventListener('keydown', (event) => {
            if (event.key === 'Enter') {
                checkAnswer(card, answerInput.value);
            }
        });
    }

    function checkAnswer(card, userAnswer) {
        if (userAnswer.trim().toLowerCase() === card.word.toLowerCase()) {
            answers[card.cardId] = true;
            sentenceContainer.innerHTML = card.sentence.replace(card.word, `<span class="correct">${card.word}</span>`);
            currentCardIndex++;
        } else {
            answers[card.cardId] = false;
            sentenceContainer.innerHTML = card.sentence.replace(card.word, `<span class="incorrect">${card.word}</span>`);
        }
        setTimeout(() => {
            if (currentCardIndex < cards.length) {
                showCard(cards[currentCardIndex]);
            } else {
                submitAnswers();
            }
        }, 2000);
    }

    function submitAnswers() {
        fetch('/api/learn/answer', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(answers)
        })
            .then(response => response.json())
            .then(() => {
                window.location.href = '/result';
            })
            .catch(error => console.error('Error submitting answers:', error));
    }
}

function initResultPage() {
    const resultContainer = document.getElementById('resultContainer');
    const accountButton = document.getElementById('accountButton');

    accountButton.addEventListener('click', () => {
        // Redirect to account page (not implemented)
        alert('Redirect to account page');
    });

    fetch('/api/learn/answer')
        .then(response => response.json())
        .then(data => {
            data.forEach(result => {
                const resultDiv = document.createElement('div');
                resultDiv.innerHTML = `${result.word} - ${result.translation}`;
                resultDiv.classList.add(result.isCorrect ? 'correct' : 'incorrect');
                resultContainer.appendChild(resultDiv);
            });
        })
        .catch(error => console.error('Error fetching results:', error));
}
