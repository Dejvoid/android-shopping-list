@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.semestralka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.Room
import com.example.semestralka.data.AppDatabase
import com.example.semestralka.data.ProductDataDao
import com.example.semestralka.data.storage.LocalStorageProductList
import com.example.semestralka.ui.list.ListScreen
import com.example.semestralka.ui.list.ListViewModel
import com.example.semestralka.ui.product.DetailScreen
import com.example.semestralka.ui.product.DetailViewModel
import com.example.semestralka.ui.theme.SemestralkaTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "product-db"
        ).build()

        val dao = db.productDataDao()

        enableEdgeToEdge()
        setContent {
            SemestralkaTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val isShopping = navBackStackEntry?.toRoute<List>()?.isShoppingList ?: false
                
                val topBarTitle = when {
                    currentDestination?.hasRoute<List>() == true -> {
                        if (isShopping) stringResource(R.string.drawer_shopping_list) 
                        else stringResource(R.string.drawer_storage_list)
                    }
                    currentDestination?.hasRoute<Detail>() == true -> stringResource(R.string.detail_title)
                    else -> stringResource(R.string.app_name)
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text(
                                text = stringResource(R.string.drawer_title), 
                                modifier = Modifier.padding(16.dp)
                            )
                            HorizontalDivider()
                            NavigationDrawerItem( // Storage List
                                label = { Text(text = stringResource(R.string.drawer_storage_list)) },
                                selected = currentDestination?.hasRoute<List>() == true && !isShopping,
                                onClick = {
                                    navController.navigate(List(isShoppingList = false)) {
                                        popUpTo<List> { inclusive = true }
                                    }
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem( // Shopping List
                                label = { Text(text = stringResource(R.string.drawer_shopping_list)) },
                                selected = currentDestination?.hasRoute<List>() == true && isShopping,
                                onClick = {
                                    navController.navigate(List(isShoppingList = true)) {
                                        popUpTo<List> { inclusive = true }
                                    }
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text(topBarTitle) },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch {
                                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = stringResource(R.string.menu_content_description)
                                        )
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            BottomAppBar(
                                actions = { },
                                floatingActionButton = {
                                    if (currentDestination?.hasRoute<Detail>() == false) {
                                        FloatingActionButton(
                                            onClick = {
                                                navController.navigate(Detail(null, isShoppingList = isShopping))
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add, 
                                                contentDescription = stringResource(R.string.detail_save_add)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        MainContent(
                            dao = dao,
                            navController = navController,
                            snackbarHostState = snackbarHostState,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent(
    dao: ProductDataDao,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = List(isShoppingList = false),
        modifier = modifier
    ) {
        composable<Detail> { backStackEntry ->
            val args: Detail = backStackEntry.toRoute()
            val storage = remember(args.isShoppingList) {
                LocalStorageProductList(dao, args.isShoppingList)
            }
            val detailViewModel: DetailViewModel = viewModel(
                key = "detail_${args.isShoppingList}", 
                factory = DetailViewModel.provideFactory(storage)
            )
            DetailScreen(
                viewModel = detailViewModel,
                identifier = args.identifier,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<List> { backStackEntry ->
            val args: List = backStackEntry.toRoute()
            val storage = remember(args.isShoppingList) {
                LocalStorageProductList(dao, args.isShoppingList)
            }
            val listViewModel: ListViewModel = viewModel(
                key = "list_${args.isShoppingList}",
                factory = ListViewModel.provideFactory(storage)
            )
            ListScreen(
                viewModel = listViewModel,
                onEditProduct = { identifier ->
                    navController.navigate(Detail(identifier, args.isShoppingList))
                },
                isShoppingList = args.isShoppingList,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@Serializable data class Detail(val identifier: String?, val isShoppingList: Boolean)
@Serializable data class List(val isShoppingList: Boolean)
