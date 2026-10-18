package app.calavera.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.File
import java.util.UUID

/** Portraits for the ofrenda, copied into the app so they stay even if the gallery forgets them. */
object Photos {
    private const val MAX = 900

    fun dir(context: Context): File = File(context.filesDir, "photos").apply { mkdirs() }

    fun file(context: Context, name: String): File = File(dir(context), name)

    /** Copies a picked picture in, scaled down to a portrait's size. Returns its file name. */
    fun import(context: Context, uri: Uri): String? = runCatching {
        val bmp = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { d, info, _ ->
                val s = info.size
                val k = maxOf(1, maxOf(s.width, s.height) / MAX)
                d.setTargetSize(s.width / k, s.height / k)
                d.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return null
        }
        val k = MAX.toFloat() / maxOf(bmp.width, bmp.height)
        val scaled = if (k < 1f) Bitmap.createScaledBitmap(bmp, (bmp.width * k).toInt(), (bmp.height * k).toInt(), true) else bmp
        val name = UUID.randomUUID().toString() + ".jpg"
        file(context, name).outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        name
    }.getOrNull()

    fun delete(context: Context, name: String) {
        if (name.isNotEmpty()) file(context, name).delete()
    }
}
