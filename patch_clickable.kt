    fun verifyTapSelection() {
        val currentWord = currentSelection.map { (r, c) -> grid[r][c] }.joinToString("")
        val wordUpper = currentWord.uppercase()
        val wordReversed = wordUpper.reversed()
        val foundWord = when {
            targetWords.contains(wordUpper) && !foundWords.contains(wordUpper) -> wordUpper
            targetWords.contains(wordReversed) && !foundWords.contains(wordReversed) -> wordReversed
            else -> null
        }

        if (foundWord != null) {
            // Found a new word!
            foundWords.add(foundWord)
            solvedCoordinates.addAll(currentSelection)
            currentSelection.clear()
            score += 150
            coinsEarned += 25

            // check if overall game finished
            if (foundWords.size == targetWords.size) {
                score += 300 // completion bonus
                coinsEarned += 50
                gameOver = true
            }
        }
    }
