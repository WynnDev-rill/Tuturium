package id.tuturium.app

import android.os.Build
import android.view.View
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView

/** The popup and sheet have their own windows. Request smooth motion on those surfaces. */
@Composable
private fun MenuFrameRate() {
    val view = LocalView.current
    DisposableEffect(view) {
        if (Build.VERSION.SDK_INT >= 35) {
            val root = view.rootView
            val previous = root.requestedFrameRate
            root.requestedFrameRate = View.REQUESTED_FRAME_RATE_CATEGORY_HIGH
            onDispose { root.requestedFrameRate = previous }
        } else onDispose {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TuturiumSheet(
    onDismissRequest: () -> Unit,
    containerColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, containerColor = containerColor,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        MenuFrameRate()
        content()
    }
}

@Composable
internal fun TuturiumMenu(
    expanded: Boolean, onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MenuDefaults.shape,
    containerColor: Color = MenuDefaults.containerColor,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(expanded, onDismissRequest, modifier, shape = shape, containerColor = containerColor) {
        MenuFrameRate()
        content()
    }
}

@Composable
internal fun TuturiumDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        MenuFrameRate()
        confirmButton()
    }, dismissButton = dismissButton, title = title, text = text, containerColor = containerColor)
}
