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
            // Fix: FontFamily doesn't have a .name property. Check equality instead.
            spanJson.put("fontFamily", if (range.item.fontFamily == FontFamily.Serif) "Serif" else "Default")
            spanJson.put("fontSize", range.item.fontSize.value)
            spanJson.put("fontStyle", if (range.item.fontStyle == FontStyle.Italic) "Italic" else "Normal")
            spanJson.put("fontWeight", if (range.item.fontWeight == FontWeight.Bold) "Bold" else "Normal")
            spans.put(spanJson)
        }
        json.put("spans", spans)

        // Save ParagraphStyles (for Arabic RTL alignment)
        val paragraphs = JSONArray()
        annotatedString.paragraphStyles.forEach { range ->
            val pJson = JSONObject()
            pJson.put("start", range.start)
            pJson.put("end", range.end)
            pJson.put("textAlign", if (range.item.textAlign == TextAlign.End) "End" else "Start")
            pJson.put("textDirection", if (range.item.textDirection == TextDirection.Rtl) "Rtl" else "Ltr")
            paragraphs.put(pJson)
        }
        json.put("paragraphs", paragraphs)

        return json.toString()
    }

    fun deserialize(jsonString: String): AnnotatedString {
        try {
            val json = JSONObject(jsonString)
            val text = json.getString("text")
            val builder = AnnotatedString.Builder(text)
            
            // Restore Spans
            val spans = json.optJSONArray("spans")
            if (spans != null) {
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
            }

            // Restore Paragraphs
            val paragraphs = json.optJSONArray("paragraphs")
            if (paragraphs != null) {
                for (i in 0 until paragraphs.length()) {
                    val p = paragraphs.getJSONObject(i)
                    val style = ParagraphStyle(
                        textAlign = if (p.optString("textAlign") == "End") TextAlign.End else TextAlign.Start,
                        textDirection = if (p.optString("textDirection") == "Rtl") TextDirection.Rtl else TextDirection.Ltr
                    )
                    builder.addStyle(style, p.getInt("start"), p.getInt("end"))
                }
            }

            return builder.toAnnotatedString()
        } catch (e: Exception) {
            return AnnotatedString("")
        }
    }
}
