package fr.cph.chicago.core.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Switch
import androidx.compose.material.SwitchColors
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// FIXME: this does not exist (yet?) in material3
@Composable
fun SwitchMaterial3(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Switch(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.secondary,
            checkedTrackColor = MaterialTheme.colorScheme.secondary,
            checkedTrackAlpha = 0.54f,
            uncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant,
            uncheckedTrackColor = MaterialTheme.colorScheme.onSurface,
            uncheckedTrackAlpha = 0.38f,
            disabledCheckedThumbColor = MaterialTheme.colorScheme.secondary
                .copy(alpha = ContentAlpha.disabled)
                .compositeOver(MaterialTheme.colorScheme.surface),
            disabledCheckedTrackColor = MaterialTheme.colorScheme.secondary
                .copy(alpha = ContentAlpha.disabled)
                .compositeOver(MaterialTheme.colorScheme.surface),
            disabledUncheckedThumbColor = MaterialTheme.colorScheme.secondary
                .copy(alpha = ContentAlpha.disabled)
                .compositeOver(MaterialTheme.colorScheme.surface),
            disabledUncheckedTrackColor = MaterialTheme.colorScheme.secondary
                .copy(alpha = ContentAlpha.disabled)
                .compositeOver(MaterialTheme.colorScheme.surface)
        )
    )
}


// FIXME: this does not exist yet in material3
@Preview
@Composable
fun ChipMaterial3(
    modifier: Modifier = Modifier,
    text: String = "Filter",
    isSelected: Boolean = true,
    onClick: () -> Unit = {},
) {
    val backgroundColor: Color
    val border: BorderStroke
    if (isSelected) {
        backgroundColor = MaterialTheme.colorScheme.primaryContainer
        border = BorderStroke(0.dp, MaterialTheme.colorScheme.secondaryContainer)
    } else {
        backgroundColor = Color.Transparent
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground)
    }
    Surface(
        modifier = modifier,
        border = border,
        shape = RoundedCornerShape(8.0.dp),
    ) {
        Row(modifier = Modifier
            .clickable { onClick() }
            .background(backgroundColor)) {
            Row(
                modifier = Modifier.padding(start = 5.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.padding(end = 5.dp),
                    imageVector = Icons.Filled.Check,
                    contentDescription = "ticked",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

// FIXME: TextField is not implemented yet in material3. This should be replaced by the official implementation when available
@Composable
fun TextFieldMaterial3(modifier: Modifier = Modifier, text: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
    Surface(
        modifier = modifier
            .padding(horizontal = 25.dp)
            .fillMaxWidth()
            .height(50.dp)
            .fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(20.0.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                modifier = Modifier.padding(start = 15.dp),
                imageVector = Icons.Filled.Search,
                contentDescription = "Icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
            )
            TextField(
                value = text,
                onValueChange = onValueChange,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,//LocalContentColor.current.copy(LocalContentAlpha.current),
                    //disabledTextColor = textColor.copy(ContentAlpha.disabled),
                    backgroundColor = Color.Transparent,//MaterialTheme.colorScheme.onSurface.copy(alpha = BackgroundOpacity),
                    cursorColor = MaterialTheme.colorScheme.onSecondaryContainer,//MaterialTheme.colorScheme.primary,
                    errorCursorColor = MaterialTheme.colorScheme.error,
                    focusedIndicatorColor = Color.Transparent,//MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                    unfocusedIndicatorColor = Color.Transparent,//MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.UnfocusedIndicatorLineOpacity),
                    //disabledIndicatorColor = unfocusedIndicatorColor.copy(alpha = ContentAlpha.disabled),
                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                    leadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                    //disabledLeadingIconColor = leadingIconColor.copy(alpha = ContentAlpha.disabled),
                    //errorLeadingIconColor = leadingIconColor,
                    trailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                    //disabledTrailingIconColor = trailingIconColor.copy(alpha = ContentAlpha.disabled),
                    errorTrailingIconColor = MaterialTheme.colorScheme.error,

                    focusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(ContentAlpha.medium),
                    //disabledLabelColor = unfocusedLabelColor.copy(ContentAlpha.disabled),
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    placeholderColor = MaterialTheme.colorScheme.onSurface.copy(ContentAlpha.medium),
                    //disabledPlaceholderColor = placeholderColor.copy(ContentAlpha.disabled)
                )
            )
        }
    }
}
