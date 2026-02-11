//SettingsScreen.kt
package com.noobexon.xposedfakelocation.manager.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.noobexon.xposedfakelocation.R

// Dimension constants
private object Dimensions {
    val SPACING_EXTRA_SMALL = 4.dp
    val SPACING_SMALL = 8.dp
    val SPACING_MEDIUM = 16.dp
    val SPACING_LARGE = 24.dp
    val CARD_CORNER_RADIUS = 12.dp
    val CARD_ELEVATION = 2.dp
    val CATEGORY_SPACING = 32.dp
}

// Setting definitions to reduce duplication
private object SettingDefinitions {
    // Define setting categories
    fun getCategories(context: android.content.Context) = mapOf(
        context.getString(R.string.location) to listOf(
            context.getString(R.string.randomize_nearby_location),
            context.getString(R.string.custom_horizontal_accuracy),
            context.getString(R.string.custom_vertical_accuracy)
        ),
        context.getString(R.string.altitude) to listOf(
            context.getString(R.string.custom_altitude),
            context.getString(R.string.custom_msl),
            context.getString(R.string.custom_msl_accuracy)
        ),
        context.getString(R.string.movement) to listOf(
            context.getString(R.string.custom_speed),
            context.getString(R.string.custom_speed_accuracy)
        )
    )

    // Define all settings with their parameters
    @Composable
    fun getSettings(viewModel: SettingsViewModel): List<SettingData> = listOf(
        // Randomize Nearby Location
        DoubleSettingData(
            title = stringResource(R.string.randomize_nearby_location),
            description = stringResource(R.string.randomize_nearby_location_description),
            useValueState = viewModel.useRandomize.collectAsState(),
            valueState = viewModel.randomizeRadius.collectAsState(),
            setUseValue = viewModel::setUseRandomize,
            setValue = viewModel::setRandomizeRadius,
            label = stringResource(R.string.randomization_radius),
            unit = stringResource(R.string.meters),
            minValue = 0f,
            maxValue = 2000f,
            step = 0.1f
        ),
        // Custom Horizontal Accuracy
        DoubleSettingData(
            title = stringResource(R.string.custom_horizontal_accuracy),
            description = stringResource(R.string.custom_horizontal_accuracy_description),
            useValueState = viewModel.useAccuracy.collectAsState(),
            valueState = viewModel.accuracy.collectAsState(),
            setUseValue = viewModel::setUseAccuracy,
            setValue = viewModel::setAccuracy,
            label = stringResource(R.string.horizontal_accuracy),
            unit = stringResource(R.string.meters),
            minValue = 0f,
            maxValue = 100f,
            step = 1f
        ),
        // Custom Vertical Accuracy
        FloatSettingData(
            title = stringResource(R.string.custom_vertical_accuracy),
            description = stringResource(R.string.custom_vertical_accuracy_description),
            useValueState = viewModel.useVerticalAccuracy.collectAsState(),
            valueState = viewModel.verticalAccuracy.collectAsState(),
            setUseValue = viewModel::setUseVerticalAccuracy,
            setValue = viewModel::setVerticalAccuracy,
            label = stringResource(R.string.vertical_accuracy),
            unit = stringResource(R.string.meters),
            minValue = 0f,
            maxValue = 100f,
            step = 1f
        ),
        // Custom Altitude
        DoubleSettingData(
            title = stringResource(R.string.custom_altitude),
            description = stringResource(R.string.custom_altitude_description),
            useValueState = viewModel.useAltitude.collectAsState(),
            valueState = viewModel.altitude.collectAsState(),
            setUseValue = viewModel::setUseAltitude,
            setValue = viewModel::setAltitude,
            label = stringResource(R.string.altitude),
            unit = stringResource(R.string.meters),
            minValue = 0f,
            maxValue = 2000f,
            step = 0.5f
        ),
        // Custom MSL
        DoubleSettingData(
            title = stringResource(R.string.custom_msl),
            description = stringResource(R.string.custom_msl_description),
            useValueState = viewModel.useMeanSeaLevel.collectAsState(),
            valueState = viewModel.meanSeaLevel.collectAsState(),
            setUseValue = viewModel::setUseMeanSeaLevel,
            setValue = viewModel::setMeanSeaLevel,
            label = stringResource(R.string.msl),
            unit = stringResource(R.string.meters),
            minValue = -400f,
            maxValue = 2000f,
            step = 0.5f
        ),
        // Custom MSL Accuracy
        FloatSettingData(
            title = stringResource(R.string.custom_msl_accuracy),
            description = stringResource(R.string.custom_msl_accuracy_description),
            useValueState = viewModel.useMeanSeaLevelAccuracy.collectAsState(),
            valueState = viewModel.meanSeaLevelAccuracy.collectAsState(),
            setUseValue = viewModel::setUseMeanSeaLevelAccuracy,
            setValue = viewModel::setMeanSeaLevelAccuracy,
            label = stringResource(R.string.msl_accuracy),
            unit = stringResource(R.string.meters),
            minValue = 0f,
            maxValue = 100f,
            step = 1f
        ),
        // Custom Speed
        FloatSettingData(
            title = stringResource(R.string.custom_speed),
            description = stringResource(R.string.custom_speed_description),
            useValueState = viewModel.useSpeed.collectAsState(),
            valueState = viewModel.speed.collectAsState(),
            setUseValue = viewModel::setUseSpeed,
            setValue = viewModel::setSpeed,
            label = stringResource(R.string.speed),
            unit = stringResource(R.string.meters_per_second),
            minValue = 0f,
            maxValue = 30f,
            step = 0.1f
        ),
        // Custom Speed Accuracy
        FloatSettingData(
            title = stringResource(R.string.custom_speed_accuracy),
            description = stringResource(R.string.custom_speed_accuracy_description),
            useValueState = viewModel.useSpeedAccuracy.collectAsState(),
            valueState = viewModel.speedAccuracy.collectAsState(),
            setUseValue = viewModel::setUseSpeedAccuracy,
            setValue = viewModel::setSpeedAccuracy,
            label = stringResource(R.string.speed_accuracy),
            unit = stringResource(R.string.meters_per_second),
            minValue = 0f,
            maxValue = 100f,
            step = 1f
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel ()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Get settings from the definition object
    val allSettings = SettingDefinitions.getSettings(settingsViewModel)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusManager.clearFocus() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimensions.SPACING_MEDIUM)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))

                // Display settings by category
                SettingDefinitions.getCategories(context).forEach { (category, settingsInCategory) ->
                    CategoryHeader(category)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimensions.SPACING_SMALL),
                        shape = RoundedCornerShape(Dimensions.CARD_CORNER_RADIUS),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION)
                    ) {
                        Column(modifier = Modifier.padding(Dimensions.SPACING_SMALL)) {
                            settingsInCategory.forEach { settingTitle ->
                                val setting = allSettings.find { it.title == settingTitle }
                                setting?.let {
                                    when (setting) {
                                        is DoubleSettingData -> {
                                            DoubleSettingComposable(setting)
                                        }
                                        is FloatSettingData -> {
                                            FloatSettingComposable(setting)
                                        }
                                    }
                                    if (settingTitle != settingsInCategory.last()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = Dimensions.SPACING_SMALL),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))
                }

                // Add space at the bottom of the list
                Spacer(modifier = Modifier.height(Dimensions.SPACING_LARGE))
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimensions.SPACING_SMALL)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(
            modifier = Modifier
                .weight(2f)
                .padding(start = Dimensions.SPACING_MEDIUM),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun DoubleSettingItem(
    title: String,
    description: String,
    useValue: Boolean,
    onUseValueChange: (Boolean) -> Unit,
    value: Double,
    onValueChange: (Double) -> Unit,
    label: String,
    unit: String,
    minValue: Float,
    maxValue: Float,
    step: Float
) {
    SettingItem(
        title = title,
        description = description,
        useValue = useValue,
        onUseValueChange = onUseValueChange,
        value = value,
        onValueChange = onValueChange,
        label = label,
        unit = unit,
        minValue = minValue,
        maxValue = maxValue,
        step = step,
        valueFormatter = { "%.2f".format(it) },
        parseValue = { it.toDouble() }
    )
}

@Composable
fun FloatSettingItem(
    title: String,
    description: String,
    useValue: Boolean,
    onUseValueChange: (Boolean) -> Unit,
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    unit: String,
    minValue: Float,
    maxValue: Float,
    step: Float
) {
    SettingItem(
        title = title,
        description = description,
        useValue = useValue,
        onUseValueChange = onUseValueChange,
        value = value,
        onValueChange = onValueChange,
        label = label,
        unit = unit,
        minValue = minValue,
        maxValue = maxValue,
        step = step,
        valueFormatter = { "%.2f".format(it) },
        parseValue = { it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Number> SettingItem(
    title: String,
    description: String,
    useValue: Boolean,
    onUseValueChange: (Boolean) -> Unit,
    value: T,
    onValueChange: (T) -> Unit,
    label: String,
    unit: String,
    minValue: Float,
    maxValue: Float,
    step: Float,
    valueFormatter: (T) -> String,
    parseValue: (Float) -> T
) {
    var showTooltip by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(Dimensions.SPACING_SMALL)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = { showTooltip = !showTooltip },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = stringResource(R.string.more_info_about, title),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (showTooltip) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimensions.SPACING_EXTRA_SMALL)
                    )
                }
            }

            Switch(
                checked = useValue,
                onCheckedChange = onUseValueChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.semantics {
                    contentDescription = if (useValue) "Disable $title" else "Enable $title"
                }
            )
        }

        if (useValue) {
            Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))

            var sliderValue by remember { mutableFloatStateOf(value.toFloat()) }
            var showExactValue by remember { mutableStateOf(false) }

            LaunchedEffect(value) {
                if (sliderValue != value.toFloat()) {
                    sliderValue = value.toFloat()
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SPACING_SMALL),
                modifier = Modifier.fillMaxWidth()
            ) {
                val displayText = "$label: ${valueFormatter(parseValue(sliderValue))} $unit"
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showExactValue = !showExactValue }
                )

                // Add +/- buttons for precise adjustment
                OutlinedIconButton(
                    onClick = {
                        val newValue = (sliderValue - step).coerceAtLeast(minValue)
                        sliderValue = newValue
                        onValueChange(parseValue(newValue))
                    },
                    enabled = sliderValue > minValue,
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "−",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                OutlinedIconButton(
                    onClick = {
                        val newValue = (sliderValue + step).coerceAtMost(maxValue)
                        sliderValue = newValue
                        onValueChange(parseValue(newValue))
                    },
                    enabled = sliderValue < maxValue,
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "+",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            // Min and max value labels
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.SPACING_SMALL)
            ) {
                Text(
                    text = "${minValue.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${maxValue.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                },
                onValueChangeFinished = {
                    onValueChange(parseValue(sliderValue))
                },
                valueRange = minValue..maxValue,
                steps = ((maxValue - minValue) / step).toInt() - 1,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Adjust $title value"
                    }
            )
        }
    }
}

sealed class SettingData {
    abstract val title: String
    abstract val description: String
    abstract val useValueState: State<Boolean>
    abstract val setUseValue: (Boolean) -> Unit
    abstract val label: String
    abstract val unit: String
    abstract val minValue: Float
    abstract val maxValue: Float
    abstract val step: Float
}

data class DoubleSettingData(
    override val title: String,
    override val description: String,
    override val useValueState: State<Boolean>,
    val valueState: State<Double>,
    override val setUseValue: (Boolean) -> Unit,
    val setValue: (Double) -> Unit,
    override val label: String,
    override val unit: String,
    override val minValue: Float,
    override val maxValue: Float,
    override val step: Float
) : SettingData()

data class FloatSettingData(
    override val title: String,
    override val description: String,
    override val useValueState: State<Boolean>,
    val valueState: State<Float>,
    override val setUseValue: (Boolean) -> Unit,
    val setValue: (Float) -> Unit,
    override val label: String,
    override val unit: String,
    override val minValue: Float,
    override val maxValue: Float,
    override val step: Float
) : SettingData()

@Composable
fun DoubleSettingComposable(
    setting: DoubleSettingData
) {
    DoubleSettingItem(
        title = setting.title,
        description = setting.description,
        useValue = setting.useValueState.value,
        onUseValueChange = setting.setUseValue,
        value = setting.valueState.value,
        onValueChange = setting.setValue,
        label = setting.label,
        unit = setting.unit,
        minValue = setting.minValue,
        maxValue = setting.maxValue,
        step = setting.step
    )
}

@Composable
fun FloatSettingComposable(
    setting: FloatSettingData
) {
    FloatSettingItem(
        title = setting.title,
        description = setting.description,
        useValue = setting.useValueState.value,
        onUseValueChange = setting.setUseValue,
        value = setting.valueState.value,
        onValueChange = setting.setValue,
        label = setting.label,
        unit = setting.unit,
        minValue = setting.minValue,
        maxValue = setting.maxValue,
        step = setting.step
    )
}