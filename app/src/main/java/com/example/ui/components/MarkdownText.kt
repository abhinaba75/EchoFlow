package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val blocks = parseMarkdownBlocks(text)
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Header -> {
                    Text(
                        text = parseInlineStyles(block.text, textColor),
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = textColor)
                            2 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = textColor)
                            else -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textColor)
                        },
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockItem(code = block.code, language = block.language)
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            style = style.copy(fontWeight = FontWeight.Bold, color = textColor),
                            modifier = Modifier.width(16.dp)
                        )
                        SelectionContainer {
                            Text(
                                text = parseInlineStyles(block.text, textColor),
                                style = style.copy(color = textColor),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    SelectionContainer {
                        Text(
                            text = parseInlineStyles(block.text, textColor),
                            style = style.copy(color = textColor),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Header(val text: String, val level: Int) : MarkdownBlock()
    data class CodeBlock(val code: String, val language: String?) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

/**
 * Parses markdown text into blocks (Headers, CodeBlocks, Bullet Items, and Paragraphs)
 */
fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = text.split("\n")
    var insideCodeBlock = false
    val codeAccumulator = StringBuilder()
    var codeLanguage: String? = null

    for (line in lines) {
        if (line.trim().startsWith("```")) {
            if (insideCodeBlock) {
                // Code block closed
                blocks.add(MarkdownBlock.CodeBlock(codeAccumulator.toString().trimEnd(), codeLanguage))
                codeAccumulator.setLength(0)
                codeLanguage = null
                insideCodeBlock = false
            } else {
                // Code block opened
                insideCodeBlock = true
                val lang = line.trim().substring(3).trim()
                codeLanguage = lang.ifEmpty { null }
            }
            continue
        }

        if (insideCodeBlock) {
            codeAccumulator.append(line).append("\n")
            continue
        }

        val trimmed = line.trim()
        when {
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Header(trimmed.substring(2).trim(), 1))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Header(trimmed.substring(3).trim(), 2))
            }
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Header(trimmed.substring(4).trim(), 3))
            }
            trimmed.startsWith("* ") -> {
                blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2).trim()))
            }
            trimmed.startsWith("- ") -> {
                blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2).trim()))
            }
            trimmed.isEmpty() -> {
                // Skip plain double newlines to manage layout spacing dynamically
            }
            else -> {
                blocks.add(MarkdownBlock.Paragraph(line))
            }
        }
    }

    if (insideCodeBlock && codeAccumulator.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(codeAccumulator.toString().trimEnd(), codeLanguage))
    }

    return blocks
}

/**
 * Parses inline markdown components like **bold** and `code` into AnnotatedStrings
 */
fun parseInlineStyles(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val boldStart = text.indexOf("**", cursor)
            val codeStart = text.indexOf("`", cursor)

            // Find closest marker
            val nextMarker = when {
                boldStart != -1 && codeStart != -1 -> minOf(boldStart, codeStart)
                boldStart != -1 -> boldStart
                codeStart != -1 -> codeStart
                else -> -1
            }

            if (nextMarker == -1) {
                // No more tokens, append remaining substring
                append(text.substring(cursor))
                break
            }

            // Append pre-marker body
            append(text.substring(cursor, nextMarker))

            if (nextMarker == boldStart) {
                val boldEnd = text.indexOf("**", boldStart + 2)
                if (boldEnd != -1) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(boldStart + 2, boldEnd))
                    }
                    cursor = boldEnd + 2
                } else {
                    // Unclosed bold, write literal
                    append("**")
                    cursor = boldStart + 2
                }
            } else {
                val codeEnd = text.indexOf("`", codeStart + 1)
                if (codeEnd != -1) {
                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color.Gray.copy(alpha = 0.15f),
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(text.substring(codeStart + 1, codeEnd))
                    }
                    cursor = codeEnd + 1
                } else {
                    // Unclosed inline code, write literal
                    append("`")
                    cursor = codeStart + 1
                }
            }
        }
    }
}

@Composable
fun CodeBlockItem(code: String, language: String?) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (language ?: "code").uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Copied Code", code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied into clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                SelectionContainer {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }
    }
}
