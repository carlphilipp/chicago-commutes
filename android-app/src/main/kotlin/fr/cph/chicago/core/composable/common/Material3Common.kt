package fr.cph.chicago.core.composable.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

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