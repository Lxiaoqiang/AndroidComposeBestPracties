
# MVI 与 Jetpack Compose：团队实用开发指南

鉴于目前ViewModel-Compose过程编码没有实际的案例（或许我看的文章少没找到），本文目的是为了分享个人在Android Compose中ViewModel到Compose这个过程的实际编码方案，以及多Compose嵌套过程事件传递定义。本片内容主要为了介绍结构化方面以及具体编码的实现，Demo代码做了一定的封装，限定了各类类型，更适合团队开发

1. 我将使用UI Intent定义从UI到ViewModel的意图，或者你可以叫它UI事件，如果你不喜欢Intent这个叫法，叫做UIEvent也合理
2. ViewModel到UI的数据流，分为两类，一类为常驻状态（Text中的值、显示在UI上的控件内容），另一类为非常驻状态（或者你可以叫它瞬时状态，比如：Toast、navigate）

## 1. 为什么我们需要 MVI？ (The "Why")

在开发复杂界面时，我们经常会遇到这些问题：

-   **状态混乱**：一个界面有多个 StateFlow，isLoading, isError, data 分散各处，很容易出现 isLoading 和 isError 同时为 true 这样的矛盾状态。
-   **逻辑分散**：ViewModel 中有各种 public 函数，UI 层可以直接调用，导致事件入口不统一，代码难以追踪和维护。
-   **难以测试**：因为状态和逻辑分散，编写单元测试变得非常复杂。

MVI (Model-View-Intent) 架构通过一个核心原则来解决这些问题：**单向数据流 (Unidirectional Data Flow)**。

数据永远在一个可预测的环路中流动： **State (状态) -> UI (界面) -> Intent (意图) -> State (新状态)**

-   State 驱动 UI 显示
-   UI 上的用户操作会生成 Intent
-   Intent 被 ViewModel 处理，生成新的 State

这个清晰的循环让我们的应用行为变得高度可预测，调试也更简单。

## 2. MVI 的三大核心组件 (The "What")

在我们的实践中，MVI 主要由三个关键部分组成，我们称之为屏幕的"契约" (Contract)。

### 2.1 UiState：UI 的唯一事实来源

UiState 是一个数据类或密封类，它代表了 UI 在任何特定时刻的完整样子。它是"单一事实来源" (Single Source of Truth)，UI 只需观察这一个对象即可完成渲染。

**示例：使用 sealed interface 对互斥状态建模**

当页面有加载、成功、失败等几种明确且不会同时出现的状态时，sealed interface 是最佳选择。

kotlin

```kotlin
// 定义屏幕的几种宏观状态
sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(val results: List<String>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
```

### 2.2 Intent (或 Event)：用户的所有操作

Intent 代表用户可能执行的所有操作。例如"点击搜索按钮"、"在输入框打字"。我们同样使用 sealed interface 来定义它们，这样所有可能的事件一目了然。

kotlin

```kotlin
// 定义所有用户可以发起的事件
sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object SearchClicked : SearchIntent
}
```

### 2.3 SideEffect：一次性的"副作用"（现在官网叫做效应）事件

这是 MVI 实践中最关键也最容易混淆的一点。副作用 (Side Effect) 是指那些一次性的、不应该在屏幕重建（如旋转）后重复执行的事件。

-   **典型例子**：显示 Toast、Snackbar、触发导航、弹出一个对话框
-   **错误实践**：把一个 Toast 消息放在 UiState 里。这会导致每次屏幕旋转，Toast 都会重新显示。

为了正确处理，我们需要一个专门的通道来发送这些一次性事件。

kotlin

```kotlin
// 定义所有一次性副作用事件
sealed interface SearchSideEffect {
    data class ShowToast(val message: String) : SearchSideEffect
    data class NavigateToDetails(val itemId: String) : SearchSideEffect
}
```

## 3. MVI 的实践流程 (The "How")

现在我们把这三个组件串联起来。

### 步骤 1：在 ViewModel 中实现 MVI 循环

ViewModel 是 MVI 的核心枢纽。它的职责非常清晰：接收 Intent，处理业务逻辑，然后更新 UiState 或发送 SideEffect。

kotlin

```kotlin
class SearchViewModel : ViewModel() {

    // 1. UiState: 使用 StateFlow 暴露给 UI
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Success(emptyList()))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // 2. SideEffect: 使用 Channel 来确保事件只被消费一次
    private val _sideEffectChannel = Channel<SearchSideEffect>()
    val sideEffects: Flow<SearchSideEffect> = _sideEffectChannel.receiveAsFlow()

    // 3. Intent: 提供唯一的事件入口函数
    fun onEvent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.SearchClicked -> {
                //... 执行搜索逻辑...
                // 搜索成功后更新 UiState
                _uiState.value = SearchUiState.Success(listOf("Result 1", "Result 2"))
                
                // 或者搜索失败时发送一个 SideEffect
                viewModelScope.launch {
                    _sideEffectChannel.send(SearchSideEffect.ShowToast("Search Failed!"))
                }
            }
            is SearchIntent.QueryChanged -> {
                //... 更新搜索框的文字状态...
            }
        }
    }
}
```

**关键点：**

-   只暴露一个 StateFlow<UiState>
-   使用 Channel 发送副作用，因为它能保证事件只被一个订阅者消费一次
-   只有一个公共函数 onEvent() 来接收所有 Intent，这让逻辑入口非常清晰

### 步骤 2：在 Composable 中消费状态和事件

UI 层（Composable）变得非常"被动"，它只做两件事：根据 UiState 渲染自己，以及将用户操作包装成 Intent 发送给 ViewModel。

```kotlin
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    // 1. 订阅 UiState
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 2. 订阅 SideEffect
    // 使用 LaunchedEffect 来确保只在 Composable 在屏幕上时监听
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is SearchSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SearchSideEffect.NavigateToDetails -> {
                    // navController.navigate(...)
                }
            }
        }
    }

    // 3. 根据 UiState 渲染 UI，并发送 Intent
    Column {
        //... 其他 UI...
        Button(
            onClick = {
                // 将用户点击包装成 Intent 发送出去
                viewModel.onEvent(SearchIntent.SearchClicked)
            }
        ) {
            Text("Search")
        }

        when (val state = uiState) {
            is SearchUiState.Loading -> CircularProgressIndicator()
            is SearchUiState.Success -> {
                LazyColumn {
                    items(state.results) { result -> Text(result) }
                }
            }
            is SearchUiState.Error -> Text(state.message, color = Color.Red)
        }
    }
}
```

## 4. 嵌套Compose函数中的事件传递
定义页面Intent，所有嵌套不管层级深度，传递统一的HomeIntent，如果后续修改，只需要增加调用点和响应点代码即可，可避免申明具体事件类型，而导致每次需求修改都需要修改整个路径
```kotlin
typealias homeUIIntent = (HomeIntent) -> Unit
```
向更深处传递Intent
```kotlin
@Composable  
fun HomePage(  
    uiState: HomeUIState,  
    homeUIIntent: homeUIIntent  
) {  
    Column(  
        modifier = Modifier  
            .fillMaxSize()  
            .padding(16.dp)  
    ) {  
  // Search Input Box  
  OutlinedTextField(  
            value = uiState.searchWord,  
            onValueChange = {  
	          //trigger ui intent
			  homeUIIntent(HomeIntent.SearchIntent(it))  
            },  
            label = { Text("Search") },  
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },  
            modifier = Modifier.fillMaxWidth()  
        )  
  
        Spacer(modifier = Modifier.height(16.dp))  
  
        when (uiState) {  
            is HomeUIState.DataState -> {  
	            //homeUIIntent
                HasDataComponent(uiState, homeUIIntent)  
            }  
  
            is HomeUIState.NoDataState -> {  
                NoDataComponent(uiState)  
            }  
        }  
    }  
}
```

## 5. 总结：我们的 MVI 实践清单

为了确保团队实践的一致性，我们可以遵循以下简单的"Do's & Don'ts"。

### Do's (要做):

-   **单一 UiState**：每个屏幕只用一个 StateFlow 暴露 UiState
-   **密封类契约**：为 UiState, Intent, SideEffect 使用 sealed interface，让屏幕能力一目了然
-   **Channel 处理副作用**：用 Channel 来处理导航、Toast 等一次性事件
-   **统一事件入口**：ViewModel 只提供一个 onEvent(intent) 方法
-   **统一UI意图**：定义统一的Intent，使用typealias修饰

### Don'ts (不要做):

-   **不要在 UiState 中放一次性事件**：绝对不要把 Toast 消息或导航事件放在 UiState 里
-   **不要在 ViewModel 中暴露多个 StateFlow**：这会破坏单一数据源原则，导致状态不一致
-   **不要在 UI 层直接调用业务逻辑**：所有操作都应通过发送 Intent 来触发

通过遵循这套简单的 MVI 实践，我们可以构建出更健壮、可维护性更高、也更容易测试的 Jetpack Compose 应用。