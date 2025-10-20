
# Android Compose MVI 最佳编码实践

Android Compose 的官方文档和业内实践在 MVI（Model-View-Intent）模式的实际代码实现方面存在一些缺失，导致开发者在构建复杂应用时可能面临高内聚性不足、状态和事件流向不清晰等问题。本文旨在分享 Android Compose 中 MVI 的最佳编码实践，以实现不同层面的高内聚、清晰的状态和事件流向、明确的关键点以及统一的编码风格。

我们将从以下几个关键实现点进行深入探讨：

- 限定事件类型：统一事件类型，聚合所有事件。
- 统一 ViewModel 中的事件入口：统一事件调用的入口，便于查找逻辑。
- 使用唯一 UI 状态对象更新 UI：状态对外唯一，作为唯一可信数据来源，避免分散的状态导致容易出错。
- 深层嵌套 Compose 函数下的事件传递最佳方案：使用统一函数，增加或删除事件功能时不必沿链路修改。

## 1. 限定事件的类型

为了更好地管理 UI 事件和瞬时状态，我们首先定义基类来限定它们的类型。

### 1.1 定义 UI 事件基类，统一入口事件类型

所有来自 UI 层的事件都应该实现此接口，从而实现事件类型的统一。
```html
/**
 * The basic event defined that from ui layer
 */
interface BaseUIEvent
```

### 1.2 定义非常驻 UI 的状态或瞬时状态

这类状态通常表示一次性、非持久性的 UI 行为，例如 Toast 消息或导航事件。
```html
/**
 * The basic state defined that from viewmodel to ui layer
 * */
interface BaseActionState
```

## 2. 统一 ViewModel 中的事件入口

统一 ViewModel 中的事件入口可以显著提高代码的可维护性和可读性。

### 2.1 使用事件基类限定

所有 ViewModel 都应该继承 BaseViewModel，并通过 onEvent 方法处理来自 UI 层的事件。
```html
/**
 * The base class for all ViewModels.
 * define how to handle events from the UI layer
 */
abstract class BaseViewModel<E: BaseUIEvent>: ViewModel() {

	/**
	 * Events that triggers actions in the UI.
	 * All of the events should be use this.
	 */
	abstract fun onEvent(event: E)
}
```
### 2.2 扩展 BaseViewModel，增加 BaseActionState 在 ViewModel 中的限定

为了更好地处理瞬时状态，我们引入 BaseActionStateViewModel，它提供了向 UI 层发送 BaseActionState 的机制。
```html
/**
 * Provide the way that the action-state to the UI layer.
 *
 */
abstract class BaseActionStateViewModel<E: BaseUIEvent, A: BaseActionState>: BaseViewModel<E>() {

	/**
	 * Events that triggers a specific action in the UI.
	 * all of the action-state trigger should be use this.
	 * eg: 
	 *	LaunchedEffect(Unit) {
	 * 		viewModel.uiActionState.collect {
	 * 			when (it) {
	 * 				is ToastActionState -> {
	 * 					Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
	 * 				}
	 * 			}
	 * 		}
	 * }
	 */
	private val _uiActionState = MutableSharedFlow<BaseActionState>()
	val uiActionState: SharedFlow<BaseActionState> = _uiActionState

	/**
	 * send the action-state to UI
	 * @param state the action-state
	 */
	fun sendActionState(state: A) {
		viewModelScope.launch {
			_uiActionState.emit(state)
		}
	}
}
```

## 3. 使用唯一 UI 状态对象更新 UI

为了确保数据来源的唯一性和可信度，我们建议只使用一个唯一的 UI 状态对象来更新 UI。在复杂场景下，这个 UI 状态对象可以进一步细分，以实现更精细的粒度控制。

例如，HomeViewModelState 可以转换为 HomeUIState，其中 HomeUIState 进一步细分为 HasResultState 和 NoResultState，以应对不同的 UI 展示情况。
```kotin
private val viewModelState = MutableStateFlow(
	HomeViewModelState(
		isLoading = false
	)
)

//expose to UI
val uiState = viewModelState
	.map(HomeViewModelState::toUiState)
	.stateIn(
		viewModelScope,
		SharingStarted.Eagerly,
		viewModelState.value.toUiState()
	)
例如，HomeViewModelState 可以转换为 HomeUIState，其中 HomeUIState 进一步细分为 HasResultState 和 NoResultState，以应对不同的 UI 展示情况。
/**
 * The State for ViewModel
 */
private data class MediaViewModelState(
	val isLoading: Boolean,
	val errorMessage: String = "",
	val searchInput: String = "",
	val searchItems: List<SearchItem> = emptyList(),
	val selectedSource: MusicSource = MusicSource.QQMusic
) {
	/**
	 * convert state to uiState
	 */
	fun toUiState(): MusicUIState =
		if (searchItems.isEmpty()) {
			MusicUIState.NoResultState(
				isLoading = isLoading,
				errorMessage = errorMessage,
				searchInput = searchInput,
				selectedSource = selectedSource
			)
		} else {
			MusicUIState.HasResultState(
				isLoading = isLoading,
				errorMessage = errorMessage,
				searchInput = searchInput,
				songList = searchItems,
				selectedSource = selectedSource
			)
		}
}


/**
 * The state for ui
 */
sealed interface MediaUIState {
	val isLoading: Boolean
	val errorMessage: String
	val searchInput: String
	val selectedSource: MusicSource


	/**
	 * The State with data
	 */
	data class HasResultState(
		override val isLoading: Boolean,
		override val errorMessage: String,
		override val searchInput: String,
		override val selectedSource: MusicSource,
		val songList: List<SearchItem>
	) : MediaUIState 

	/**
	 * The State without data
	 */
	data class NoResultState(
		override val isLoading: Boolean,
		override val errorMessage: String,
		override val searchInput: String,
		override val selectedSource: MusicSource
	) : MediaUIState 
}
```

## 4. 深层嵌套 Compose 函数下的事件传递最佳方案

在深层嵌套的 Compose 函数中，事件传递常常成为一个痛点。通过定义统一的事件函数类型，我们可以避免层层传递事件，简化代码维护。
```html
//定义事件函数
typealias onHomeUIEvent = (PlayerScreenUIEvent) -> Unit

@Composable
fun HomeScreen() {
	val uiState by viewModel.uiState.collectAsStateWithLifecycle()
	HomePage(uiState) {
		//内部的统一出口，对应也是调用到ViewModel中的唯一入口
		viewModel.onEvent(it)
	}
}

@Composable
fun HomePage(
	uiState: HomeUIState,
	onEvent: onHomeUIEvent //UI事件上传使用统一的Event
) {
	when {
		uiState is HomeUIState.HasResultState -> {
			HasDataPage(uiState) {
				onEvent(it)
			}
		}

		uiState is HomeUIState.NoResultState -> {
			NoDataPage(uiState) {
				onEvent(it)
			}
		}
	}
}


@Composable
fun HasDataPage(
	uiState: HomeUIState.HasResultState, //使用更加细化的HasResultState
	onEvent: onHomeUIEvent //UI事件上传使用统一的Event
) {
	//可能有更深的层级，依次传递即可
}

@Composable
fun NoDataPage(
	uiState: HomeUIState.NoResultState, //使用更加细化的NoResultState
	onEvent: onHomeUIEvent //UI事件上传使用统一的Event
) {
	Button(
		onClick = {
			//UI事件发起
			onEvent(HomeViewModelEvent.Login)
		}
	)
}
在 ViewModel 中，事件的处理逻辑如下：
/**
 * The Events define.
 * is UI to ViewModel event
 */
sealed interface MediaUIEvent {

	data class SearchEvent(val keyword: String) : MediaUIEvent 
}

class MediaViewModel(): ViewModel() {

	private val viewModelState = MutableStateFlow(
		HomeViewModelState( 
			isLoading = false
		)
	)

	val uiState = viewModelState
		.map(HomeViewModelState::toUiState)
		.stateIn(
			viewModelScope,
			SharingStarted.Eagerly,
			viewModelState.value.toUiState()
		)


	fun onEvent(event: MediaUIEvent) { 
		when (event) {
			is MediaUIEvent.SearchEvent -> {
				// Assuming searchBySource and event.type are defined elsewhere
				// searchBySource(event.keyword, event.type)
			}
		}
	}
}
```

## 总结

在 Android Compose MVI 架构中，ViewModel 扮演着 UI 状态管理和 UI 事件处理的核心角色，职责划分清晰，行为明确，处理点集中。

- **ViewModelEvent (或 MediaUIEvent)**: 用于定义和传递从 UI 到 ViewModel 的事件。
- **ViewModelState**: 用于 ViewModel 内部的状态分离和管理。ViewModelState 会被转换为 UIState，用于刷新和更新 UI。
- **UIState**: 用于 UI 状态更新。UIState 的细分可以实现更精细的粒度控制和更细化的模型，这在逻辑复杂的场景下尤为重要。

需要注意的是，UIState 可以分为两种：

- **常驻 UI 的状态**：例如文本值、列表数据等，它们持续存在于 UI 上。
- **非常驻 UI 的状态（瞬时状态）**：例如 Toast 消息、Snackbar 提示或导航行为等，它们是短暂的、一次性的 UI 响应。这些瞬时状态通常通过 SharedFlow 或类似机制传递，并在 UI 层进行消费。
