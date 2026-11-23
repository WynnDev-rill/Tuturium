package id.tuturium.app

import android.content.Context

/** Names and memberships are local preferences; documents remain in the Library database. */
internal object FolderStore {
    private const val FILE = "library_folders"
    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun names(context: Context): List<String> =
        prefs(context).getStringSet("names", emptySet()).orEmpty().sortedBy { it.lowercase() }

    fun add(context: Context, name: String): Boolean {
        val safe = name.trim().take(60)
        if (safe.isBlank() || names(context).any { it.equals(safe, true) }) return false
        return prefs(context).edit().putStringSet("names", (names(context) + safe).toSet()).commit()
    }

    fun folderOf(context: Context, bookId: String): String? =
        prefs(context).getString("book:$bookId", null)

    fun assign(context: Context, bookId: String, folder: String?) {
        if (folder != null && folder !in names(context)) return
        val editor = prefs(context).edit()
        if (folder == null) editor.remove("book:$bookId")
        else editor.putString("book:$bookId", folder)
        editor.apply()
    }
}
