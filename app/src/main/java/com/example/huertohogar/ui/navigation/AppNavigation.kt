package com.example.huertohogar.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.huertohogar.data.model.CartItem
import com.example.huertohogar.data.model.Product
import com.example.huertohogar.ui.viewmodel.CartViewModel
import com.example.huertohogar.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

// --- Sealed class to define navigation routes for type safety ---
sealed class AppScreen(val route: String) {
    object Login : AppScreen("login")
    object Main : AppScreen("main")
    object ProductList : AppScreen("product_list")
    object ShoppingCart : AppScreen("shopping_cart")
}

// --- Main Navigation Composable ---
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Shared ViewModels scoped to the navigation graph
    val productViewModel: ProductViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()

    NavHost(navController = navController, startDestination = AppScreen.Login.route) {
        composable(AppScreen.Login.route) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(AppScreen.Main.route) {
                    popUpTo(AppScreen.Login.route) { inclusive = true }
                }
            })
        }
        composable(AppScreen.Main.route) {
            MainScaffold(
                productViewModel = productViewModel,
                cartViewModel = cartViewModel
            )
        }
    }
}


// --- Main Scaffold with Drawer and TopBar ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val cartItems by cartViewModel.cartItems.collectAsState()

    // Get current route to update the title
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentTitle = when (currentRoute) {
        AppScreen.ProductList.route -> "Catálogo de Productos"
        AppScreen.ShoppingCart.route -> "Carro de Compras"
        else -> "Mi Tienda"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController = navController, closeDrawer = {
                scope.launch { drawerState.close() }
            })
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentTitle) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        // Cart action with badge
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge { Text("${cartItems.sumOf { it.quantity }}") }
                                }
                            }
                        ) {
                            IconButton(onClick = { navController.navigate(AppScreen.ShoppingCart.route) }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Shopping Cart")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                // Internal navigation for the main content area
                NavHost(navController = navController, startDestination = AppScreen.ProductList.route) {
                    composable(AppScreen.ProductList.route) {
                        ProductListScreen(
                            productViewModel = productViewModel,
                            onProductClick = { product ->
                                cartViewModel.addProductToCart(product)
                            }
                        )
                    }
                    composable(AppScreen.ShoppingCart.route) {
                        ShoppingCartScreen(cartViewModel = cartViewModel)
                    }
                }
            }
        }
    }
}


// --- Drawer Content ---
@Composable
fun DrawerContent(navController: NavController, closeDrawer: () -> Unit) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Mi Tienda", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
            NavigationDrawerItem(
                label = { Text("Productos") },
                selected = navController.currentDestination?.route == AppScreen.ProductList.route,
                onClick = {
                    navController.navigate(AppScreen.ProductList.route)
                    closeDrawer()
                }
            )
            NavigationDrawerItem(
                label = { Text("Mi Carro") },
                selected = navController.currentDestination?.route == AppScreen.ShoppingCart.route,
                onClick = {
                    navController.navigate(AppScreen.ShoppingCart.route)
                    closeDrawer()
                }
            )
        }
    }
}


// --- Login Screen ---
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Bienvenido", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)

            // Placeholder for an app logo
            // Image(painterResource(id = R.drawable.ic_logo), contentDescription = "Logo", modifier = Modifier.size(120.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    onLoginSuccess()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ingresar", modifier = Modifier.padding(8.dp))
            }
        }
    }
}

// --- Product List Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    productViewModel: ProductViewModel,
    onProductClick: (Product) -> Unit
) {
    val searchQuery by productViewModel.searchQuery.collectAsState()
    val products by productViewModel.filteredProducts.collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { productViewModel.onSearchQueryChanged(it) },
                label = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Product List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = {
                            onProductClick(product)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "${product.name} añadido al carro",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$${"%.2f".format(product.price)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Button(onClick = onAddToCart) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Add to Cart")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir")
                    }
                }
            }
        }
    }
}

// --- Shopping Cart Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingCartScreen(cartViewModel: CartViewModel) {
    val cartItems by cartViewModel.cartItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Tu carro de compras está vacío",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val totalPrice = cartItems.sumOf { it.product.price * it.quantity }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems, key = { it.product.id }) { item ->
                    // =================== SECCIÓN CORREGIDA ===================
                    val dismissState = rememberDismissState(
                        confirmValueChange = {
                            if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                                cartViewModel.removeProductFromCart(item)
                                return@rememberDismissState true
                            }
                            false
                        },
                        // Positional threshold specifies the fraction of the width that needs to be swiped
                        positionalThreshold = { 150.dp.toPx() }
                    )

                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
                        background = {
                            val color by animateColorAsState(
                                when (dismissState.targetValue) {
                                    DismissValue.DismissedToEnd -> Color.Red.copy(alpha = 0.8f)
                                    DismissValue.DismissedToStart -> Color.Red.copy(alpha = 0.8f)
                                    else -> Color.Transparent
                                }
                            )
                            val alignment = when (dismissState.dismissDirection) {
                                DismissDirection.StartToEnd -> Alignment.CenterStart
                                DismissDirection.EndToStart -> Alignment.CenterEnd
                                null -> Alignment.Center
                            }
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color, shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = alignment
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Icon",
                                    tint = Color.White
                                )
                            }
                        },
                        dismissContent = {
                            CartItemCard(
                                item = item,
                                onQuantityChange = { newQuantity ->
                                    cartViewModel.updateQuantity(item, newQuantity)
                                }
                            )
                        }
                    )
                    // ================= FIN DE SECCIÓN CORREGIDA ================
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Totals and Checkout Button
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Total: $${"%.2f".format(totalPrice)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { cartViewModel.checkout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Enviar Venta", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItem, onQuantityChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = item.product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$${"%.2f".format(item.product.price)}", color = Color.Gray)
            }
            // Quantity controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onQuantityChange(item.quantity - 1) },
                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                ) { Icon(Icons.Default.Remove, contentDescription = "Decrease quantity") }

                Text(
                    "${item.quantity}",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { onQuantityChange(item.quantity + 1) },
                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                ) { Icon(Icons.Default.Add, contentDescription = "Increase quantity") }
            }
        }
    }
}

