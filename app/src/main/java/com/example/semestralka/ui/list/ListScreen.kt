package com.example.semestralka.ui.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.semestralka.data.ProductData
import com.example.semestralka.ui.components.ProductCard
import com.example.semestralka.ui.theme.SemestralkaTheme
import kotlinx.coroutines.launch

@Composable
fun ListScreen(
    viewModel: ListViewModel,
    onEditProduct: (String) -> Unit,
    isShoppingList: Boolean = false,
    snackbarHostState: SnackbarHostState? = null
) {
    val scope = rememberCoroutineScope()
    var showExportConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadList()
    }

    val listState by viewModel.state.collectAsStateWithLifecycle()
    val selectedIdentifiers by viewModel.selectedIdentifiers.collectAsStateWithLifecycle()
    val expandedIdentifiers by viewModel.expandedIdentifiers.collectAsStateWithLifecycle()

    if (showExportConfirm) {
        AlertDialog(
            onDismissRequest = { showExportConfirm = false },
            title = { Text("Export to Storage") },
            text = { Text("Are you sure you want to export selected items to storage and delete them from the shopping list?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportConfirm = false
                        viewModel.exportToStorage {
                            scope.launch {
                                snackbarHostState?.showSnackbar("Selected items exported to Storage")
                            }
                        }
                    }
                ) { Text("Export") }
            },
            dismissButton = {
                TextButton(onClick = { showExportConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isShoppingList && selectedIdentifiers.isNotEmpty()) {
            Button(
                onClick = { showExportConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Export Selected (${selectedIdentifiers.size})")
            }
        }

        when (val state = listState) {
            ListState.Idle -> Unit
            ListState.Loading -> ListLoading(modifier = Modifier.weight(1f))
            is ListState.Loaded -> {
                ProductList(
                    products = state.products,
                    onEditProduct = onEditProduct,
                    isShoppingList = isShoppingList,
                    selectedIdentifiers = selectedIdentifiers,
                    onToggleSelection = { viewModel.toggleSelection(it) },
                    expandedIdentifiers = expandedIdentifiers,
                    onToggleExpanded = { viewModel.toggleExpanded(it) },
                    modifier = Modifier.weight(1f)
                )
            }
            ListState.Error -> ListError(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ListLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun ListError(modifier: Modifier = Modifier) {
    Text(
        text = "Error loading products",
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ProductList(
    products: List<ProductData>,
    onEditProduct: (String) -> Unit,
    isShoppingList: Boolean,
    selectedIdentifiers: Set<String>,
    onToggleSelection: (String) -> Unit,
    expandedIdentifiers: Set<String>,
    onToggleExpanded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("List is empty", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(products, key = { it.identifier }) { product ->
                ProductCard(
                    product = product,
                    onEdit = { onEditProduct(product.identifier) },
                    showCheckbox = isShoppingList,
                    selected = selectedIdentifiers.contains(product.identifier),
                    onSelectionChange = { onToggleSelection(product.identifier) },
                    expanded = expandedIdentifiers.contains(product.identifier),
                    onExpandChange = { onToggleExpanded(product.identifier) }
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Light Mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    showBackground = true,
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProductListPreview() {
    SemestralkaTheme {
        ProductList(
            products = emptyList(),
            onEditProduct = {},
            isShoppingList = true,
            selectedIdentifiers = emptySet(),
            onToggleSelection = {},
            expandedIdentifiers = emptySet(),
            onToggleExpanded = {}
        )
    }
}