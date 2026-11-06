package com.fpt.enterpriseapp.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object FileUtil {
    fun exportCsv(context: Context, fileName: String, headers: List<String>, rows: List<List<String>>) {
        try {
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            val writer = OutputStreamWriter(outputStream, "UTF-8")

            // Thêm BOM để Excel đọc đúng tiếng Việt
            writer.write("\uFEFF")

            // Ghi header
            writer.write(headers.joinToString(",") { "\"$it\"" } + "\n")

            // Ghi data
            rows.forEach { row ->
                writer.write(row.joinToString(",") { "\"$it\"" } + "\n")
            }
            writer.close()
            outputStream.close()

            // Mở share sheet
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Chia sẻ báo cáo"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}