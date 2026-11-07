package com.ringgitwise.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ringgitwise.app.data.*

private data class CategoryPreset(
    val name: String,
    val iconKey: String,
    val colorHex: String
)

private val CATEGORY_PRESETS = listOf(
    CategoryPreset("Pets", "pets", "#F59E0B"),
    CategoryPreset("Gym & Sports", "fitness", "#10B981"),
    CategoryPreset("Gaming", "gaming", "#A856EA"),
    CategoryPreset("Movies & Fun", "movie", "#F43F5E"),
    CategoryPreset("Education", "school", "#38BDF8"),
    CategoryPreset("Beauty & Barber", "beauty", "#EC4899"),
    CategoryPreset("Gifts & Charity", "gift", "#F97316"),
    CategoryPreset("Gadgets & Tech", "tech", "#06B6D4"),
    CategoryPreset("Travel & Holiday", "flight", "#14B8A6"),
    CategoryPreset("Home & Living", "home", "#84CC16"),
    CategoryPreset("Work & Office", "work", "#6366F1"),
    CategoryPreset("Subscriptions", "bills", "#EAB308")
)

fun categoryIconByKey(key: String): ImageVector = when (key.lowercase()) {
    "tag" -> Icons.Default.Label
    "restaurant" -> Icons.Default.Restaurant
    "cafe" -> Icons.Default.LocalCafe
    "fastfood" -> Icons.Default.Fastfood
    "groceries" -> Icons.Default.ShoppingCart
    "shopping" -> Icons.Default.ShoppingBag
    "car" -> Icons.Default.DirectionsCar
    "fuel" -> Icons.Default.LocalGasStation
    "flight" -> Icons.Default.Flight
    "home" -> Icons.Default.Home
    "bills" -> Icons.Default.ReceiptLong
    "health" -> Icons.Default.MedicalServices
    "fitness" -> Icons.Default.FitnessCenter
    "gaming" -> Icons.Default.SportsEsports
    "movie" -> Icons.Default.Movie
    "school" -> Icons.Default.School
    "work" -> Icons.Default.Work
    "pets" -> Icons.Default.Pets
    "gift" -> Icons.Default.CardGiftcard
    "beauty" -> Icons.Default.Spa
    "tech" -> Icons.Default.Devices
    "savings" -> Icons.Default.Savings
    "charity" -> Icons.Default.VolunteerActivism
    "payments" -> Icons.Default.Payments
    else -> Icons.Default.Label
}

@Composable
fun CategoriesScreen(
    customCategories: List<CustomCategory>,
    transactions: List<Transaction>,
    onUpsert: (CustomCategory, String?) -> Unit,
    onDelete: (String) -> Unit
) {
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CustomCategory?>(null) }
    var deletingTarget by remember { mutableStateOf<CustomCategory?>(null) }

    val txCountByCategory = remember(transactions) {
        transactions.groupingBy { it.category.trim().lowercase() }.eachCount()
    }
    val spendByCategory = remember(transactions) {
        transactions.filter { it.type != "credit" && it.amount > 0 }
            .groupBy { it.category.trim().lowercase() }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Space.lg, end = Space.lg, top = Space.sm, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(Space.md)
        ) {
            item {
                AppCard(container = MaterialTheme.colorScheme.surfaceContainerHigh, contentPadding = PaddingValues(Space.xl)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                        IconBadge(Icons.Default.Category, MaterialTheme.colorScheme.primary, size = 48.dp, iconSize = IconSize.lg)
                        Column(Modifier.weight(1f)) {
                            Text("Expense categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "${customCategories.size} custom • ${DEFAULT_CATEGORIES.size} built-in",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(
                            onClick = { creating = true },
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(IconSize.sm))
                            Spacer(Modifier.width(4.dp))
                            Text("New")
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Custom categories (${customCategories.size})",
                    actionLabel = "Add custom",
                    onAction = { creating = true }
                )
            }

            if (customCategories.isEmpty()) {
                item {
                    AppCard {
                        EmptyState(
                            icon = Icons.Default.AddCircleOutline,
                            title = "No custom categories yet",
                            message = "Create your own categories like Pets, Gym, Gaming, or Laundry with custom icons and colours.",
                            actionLabel = "Create custom category",
                            onAction = { creating = true }
                        )
                    }
                }
            } else {
                items(customCategories, key = { it.id }) { cat ->
                    val color = parseHex(cat.colorHex)
                    val icon = categoryIconByKey(cat.iconKey)
                    val count = txCountByCategory[cat.name.trim().lowercase()] ?: 0
                    val spent = spendByCategory[cat.name.trim().lowercase()] ?: 0.0

                    AppCard(
                        onClick = { editing = cat },
                        contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.md)
                        ) {
                            IconBadge(icon, color, size = 44.dp, iconSize = IconSize.md)
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        cat.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = color.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            "Custom",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = color,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    if (count > 0) "$count ${if (count == 1) "entry" else "entries"} • ${formatRMExact(spent)}"
                                    else "Tap to edit icon or colour",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { editing = cat }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit ${cat.name}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { deletingTarget = cat }) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete ${cat.name}", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader("Built-in categories (${DEFAULT_CATEGORIES.size})")
            }

            items(DEFAULT_CATEGORIES, key = { it.id }) { defCat ->
                val count = txCountByCategory[defCat.name.trim().lowercase()] ?: 0
                val spent = spendByCategory[defCat.name.trim().lowercase()] ?: 0.0
                AppCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.md)
                    ) {
                        IconBadge(getCategoryIcon(defCat.name), defCat.color, size = 40.dp, iconSize = IconSize.md)
                        Column(Modifier.weight(1f)) {
                            Text(defCat.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (count > 0) "$count ${if (count == 1) "entry" else "entries"} • ${formatRMExact(spent)}"
                                else "Default category",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { creating = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("New category") },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Space.lg)
                .navigationBarsPadding()
        )
    }

    if (creating || editing != null) {
        val currentEditing = editing
        CustomCategorySheet(
            initial = currentEditing,
            onDismiss = { creating = false; editing = null },
            onSave = { updated ->
                onUpsert(updated, currentEditing?.name)
                creating = false
                editing = null
            },
            onDelete = if (currentEditing != null) {
                {
                    onDelete(currentEditing.id)
                    creating = false
                    editing = null
                }
            } else null
        )
    }

    if (deletingTarget != null) {
        val target = deletingTarget!!
        AlertDialog(
            onDismissRequest = { deletingTarget = null },
            title = { Text("Delete \"${target.name}\"?") },
            text = { Text("Existing expenses tagged with \"${target.name}\" will keep their category label, and \"${target.name}\" will be removed from your custom picker.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(target.id)
                        deletingTarget = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deletingTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomCategorySheet(
    initial: CustomCategory? = null,
    onDismiss: () -> Unit,
    onSave: (CustomCategory) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var colorHex by remember { mutableStateOf(initial?.colorHex ?: CATEGORY_COLORS.first()) }
    var iconKey by remember { mutableStateOf(initial?.iconKey ?: "tag") }
    var triedSave by remember { mutableStateOf(false) }

    val cleanName = name.trim()
    val isBuiltInDuplicate = DEFAULT_CATEGORIES.any { it.name.equals(cleanName, ignoreCase = true) }
    val nameError = cleanName.isEmpty() || isBuiltInDuplicate
    val previewColor = parseHex(colorHex)
    val previewIcon = categoryIconByKey(iconKey)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = Radius.hero, topEnd = Radius.hero)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.xl)
                .padding(bottom = Space.xl),
            verticalArrangement = Arrangement.spacedBy(Space.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (initial == null) "New custom category" else "Edit category",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                // Live preview chip
                Surface(
                    shape = CircleShape,
                    color = previewColor.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, previewColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(previewIcon, contentDescription = null, tint = previewColor, modifier = Modifier.size(16.dp))
                        Text(
                            cleanName.ifEmpty { "Preview" },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = previewColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (initial == null) {
                Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                    Text(
                        "Quick ideas",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Space.sm)
                    ) {
                        CATEGORY_PRESETS.forEach { preset ->
                            val presetColor = parseHex(preset.colorHex)
                            SuggestionChip(
                                onClick = {
                                    name = preset.name
                                    iconKey = preset.iconKey
                                    colorHex = preset.colorHex
                                },
                                icon = {
                                    Icon(
                                        categoryIconByKey(preset.iconKey),
                                        contentDescription = null,
                                        tint = presetColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(preset.name) },
                                shape = CircleShape
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category name") },
                placeholder = { Text("e.g. Pets, Gym & Sports, Laundry") },
                leadingIcon = {
                    Icon(previewIcon, contentDescription = null, tint = previewColor)
                },
                isError = triedSave && nameError,
                supportingText = if (triedSave && nameError) {
                    {
                        Text(
                            if (cleanName.isEmpty()) "Please enter a category name"
                            else "\"$cleanName\" is already a built-in category"
                        )
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                Text("Colour", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Space.md),
                    verticalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    CATEGORY_COLORS.forEach { hex ->
                        val c = parseHex(hex)
                        val selected = colorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(c)
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable(role = Role.RadioButton) { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(IconSize.md)
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                Text("Icon", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    verticalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    CATEGORY_ICON_KEYS.forEach { key ->
                        val selected = iconKey == key
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (selected) previewColor.copy(alpha = 0.22f)
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .then(
                                    if (selected) Modifier.border(2.dp, previewColor, RoundedCornerShape(14.dp))
                                    else Modifier
                                )
                                .clickable(role = Role.RadioButton) { iconKey = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                categoryIconByKey(key),
                                contentDescription = key,
                                tint = if (selected) previewColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(IconSize.md)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    triedSave = true
                    if (!nameError) {
                        val base = initial ?: CustomCategory(name = cleanName)
                        onSave(
                            base.copy(
                                name = cleanName,
                                colorHex = colorHex,
                                iconKey = iconKey
                            )
                        )
                    }
                },
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (initial == null) "Create category" else "Save changes", fontWeight = FontWeight.Bold)
            }

            if (onDelete != null) {
                OutlinedButton(
                    onClick = onDelete,
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delete category")
                }
            }
        }
    }
}
