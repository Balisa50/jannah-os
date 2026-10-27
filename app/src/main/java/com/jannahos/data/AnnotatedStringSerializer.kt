package com.jannahos.data

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

object AnnotatedStringSerializer {
    fun serialize(annotatedString: AnnotatedString): String {
        val json = JSONObject()
        json.put("text", annotatedString.text)
        
        val spans = JSONArray()
        annotatedString.spanStyles.forEach { range ->
            val spanJson = JSONObject()
            spanJson.put("start", range.start)
            spanJson.put("end", range.end)
            spanJson.put("fontFamily", range.item.fontFamily?.name ?: "Default")
            spanJson.put("fontSize", range.item.fontSize.value)
            spanJson.put("fontStyle", if (range.item.fontStyle == FontStyle.Italic) "Italic" else "Normal")
            spanJson.put("fontWeight", if (range.item.fontWeight == FontWeight.Bold) "Bold" else "Normal")
            spans.put(spanJson)
        }
        json.put("spans", spans)
        return json.toString()
    }

    fun deserialize(jsonString: String): AnnotatedString {
        try {
            val json = JSONObject(jsonString)
            val text = json.getString("text")
            val builder = AnnotatedString.Builder(text)
            
            val spans = json.getJSONArray("spans")
            for (i in 0 until spans.length()) {
                val span = spans.getJSONObject(i)
                val style = SpanStyle(
                    fontFamily = if (span.optString("fontFamily") == "Serif") FontFamily.Serif else FontFamily.Default,
                    fontSize = span.optDouble("fontSize", 17.0).sp,
                    fontStyle = if (span.optString("fontStyle") == "Italic") FontStyle.Italic else FontStyle.Normal,
                    fontWeight = if (span.optString("fontWeight") == "Bold") FontWeight.Bold else FontWeight.Normal
                )
                builder.addStyle(style, span.getInt("start"), span.getInt("end"))
            }
            return builder.toAnnotatedString()
        } catch (e: Exception) {
            return AnnotatedString("")
        }
    }
}
