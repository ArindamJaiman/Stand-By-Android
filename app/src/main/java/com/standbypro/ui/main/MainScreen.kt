package com.standbypro.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.standbypro.ui.StandByViewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: StandByViewModel = viewModel(),
    onExit: () -> Unit = {}
) {
    StandByScreen(
        viewModel = viewModel,
        onExit = onExit,
        modifier = modifier
    )
}
