package id.tuturium.app

import android.content.Context

/** Names and memberships are local preferences; documents remain in the Library database. */
internal object FolderStore {
    private const val FILE = "library_folders"
    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun names(context: Context): List<String> =
        prefs(context).getStringSet("names", emptySet()).orEmpty().sortedBy { it.lowercase() }

    fun parentOf(context: Context, name: String): String? {
        val p = prefs(context)
        return p.getString("parent:$name", null)
            ?.takeIf { it != name && it in p.getStringSet("names", emptySet()).orEmpty() }
    }

    fun canMove(context: Context, name: String, destination: String?): Boolean {
        val names = prefs(context).getStringSet("names", emptySet()).orEmpty()
        if (name !in names || (destination != null && destination !in names)) return false
        val visited = mutableSetOf(name)
        var ancestor = destination
        while (ancestor != null) {
            if (!visited.add(ancestor)) return false
            ancestor = parentOf(context, ancestor)
        }
        return true
    }

    fun move(context: Context, name: String, destination: String?): Boolean {
        if (!canMove(context, name, destination)) return false
        val edit = prefs(context).edit()
        if (destination == null) edit.remove("parent:$name")
        else edit.putString("parent:$name", destination)
        val saved = edit.commit()
        if (saved) TuturiumCloud.markDirty(context)
        return saved
    }

    fun add(context: Context, name: String, parent: String? = null): Boolean {
        val safe = name.trim().take(60)
        if (safe.isBlank() || names(context).any { it.equals(safe, true) }) return false
        if (parent != null && parent !in names(context)) return false
        val edit = prefs(context).edit().putStringSet("names", (names(context) + safe).toSet())
        if (parent != null) edit.putString("parent:$safe", parent)
        val saved = edit.commit()
        if (saved) TuturiumCloud.markDirty(context)
        return saved
    }

    fun isPinned(context: Context, name: String): Boolean =
        name in prefs(context).getStringSet("pinned_folders", emptySet()).orEmpty()

    fun togglePinned(context: Context, name: String) {
        val pinned = prefs(context).getStringSet("pinned_folders", emptySet()).orEmpty().toMutableSet()
        if (!pinned.add(name)) pinned.remove(name)
        prefs(context).edit().putStringSet("pinned_folders", pinned).apply()
        TuturiumCloud.markDirty(context)
    }

    fun rename(context: Context, current: String, replacement: String): Boolean {
        val safe = replacement.trim().take(60)
        if (safe.isBlank() || current !in names(context) ||
            names(context).any { it != current && it.equals(safe, true) }) return false
        val p = prefs(context)
        val existing = names(context)
        val editor = p.edit()
        editor.putStringSet("names", (existing - current + safe).toSet())
        p.all.keys.filter { (it.startsWith("book:") || it.startsWith("parent:")) &&
            p.getString(it, null) == current }.forEach { editor.putString(it, safe) }
        if (current != safe) {
            editor.remove("parent:$current")
            parentOf(context, current)?.let { editor.putString("parent:$safe", it) }
        }
        val pins = p.getStringSet("pinned_folders", emptySet()).orEmpty()
        if (current in pins) editor.putStringSet("pinned_folders", pins - current + safe)
        val saved = editor.commit()
        if (saved) TuturiumCloud.markDirty(context)
        return saved
    }

    fun remove(context: Context, name: String) {
        val p = prefs(context)
        val editor = p.edit().putStringSet("names", (names(context) - name).toSet())
        val parent = parentOf(context, name)
        editor.remove("parent:$name")
        p.all.keys.filter { (it.startsWith("book:") || it.startsWith("parent:")) &&
            p.getString(it, null) == name }.forEach {
            if (parent == null) editor.remove(it) else editor.putString(it, parent)
        }
        editor.putStringSet("pinned_folders",
            p.getStringSet("pinned_folders", emptySet()).orEmpty() - name)
        editor.apply()
        TuturiumCloud.markDirty(context)
    }

    fun folderOf(context: Context, bookId: String): String? {
        val preferences = prefs(context)
        val names = preferences.getStringSet("names", emptySet()).orEmpty()
        return preferences.getString("book:$bookId", null)?.takeIf { it in names }
    }

    fun assign(context: Context, bookId: String, folder: String?) {
        if (folder != null && folder !in names(context)) return
        val editor = prefs(context).edit()
        if (folder == null) editor.remove("book:$bookId")
        else editor.putString("book:$bookId", folder)
        editor.apply()
        TuturiumCloud.markDirty(context)
    }
}
