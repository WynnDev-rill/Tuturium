package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class ReaderWordSelectionTest {
    @Test fun tappingWordInsideSentenceFindsExactTtsOffset() {
        val sentence = "Mereka sedang membaca sebuah buku."
        val row = listOf(IndexedValue(7, Pass(sentence, 0, 0, 0)))
        val result = selectSpokenWord(sentence, row, sentence.indexOf("membaca") + 3)
        assertEquals(WordSelection(7, sentence.indexOf("membaca")), result)
    }

    @Test fun tappingPunctuationChoosesNearestSpeakableWord() {
        val sentence = "Satu, dua."
        val row = listOf(IndexedValue(1, Pass(sentence, 0, 0, 0)))
        assertEquals(WordSelection(1, 6), selectSpokenWord(sentence, row, 5))
        assertEquals(WordSelection(1, 0), selectSpokenWord(sentence, row, 4))
    }

    @Test fun tappingAWordInSecondPassKeepsPassAndLocalOffset() {
        val rowText = "Awal selesai. Selanjutnya berjalan."
        val second = rowText.indexOf("Selanjutnya")
        val passes = listOf(
            IndexedValue(4, Pass("Awal selesai.", 0, 0, 0)),
            IndexedValue(5, Pass("Selanjutnya berjalan.", 0, 0, second))
        )
        assertEquals(WordSelection(5, "Selanjutnya ".length),
            selectSpokenWord(rowText, passes, rowText.indexOf("berjalan") + 2))
    }

    @Test fun emptyAndNonverbalRowsCannotBePlayed() {
        assertNull(selectSpokenWord("", emptyList(), 0))
        assertNull(selectSpokenWord("  ... ", listOf(IndexedValue(0, Pass("...", 0, 0, 2))), 2))
    }

    @Test fun tappingAfterLastCharacterReturnsLastWord() {
        val text = "Kata terakhir."
        val row = listOf(IndexedValue(9, Pass(text, 0, 0, 0)))
        assertEquals(WordSelection(9, 5), selectSpokenWord(text, row, text.length + 20))
    }
}
