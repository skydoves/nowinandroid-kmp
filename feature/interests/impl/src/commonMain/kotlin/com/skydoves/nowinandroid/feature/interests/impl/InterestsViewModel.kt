/*
 * Designed and developed by 2026 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.nowinandroid.feature.interests.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydoves.nowinandroid.core.data.repository.UserDataRepository
import com.skydoves.nowinandroid.core.domain.GetFollowableTopicsUseCase
import com.skydoves.nowinandroid.core.domain.TopicSortField
import com.skydoves.nowinandroid.core.model.data.FollowableTopic
import com.skydoves.nowinandroid.feature.interests.api.navigation.InterestsNavKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@AssistedInject
class InterestsViewModel(
    val userDataRepository: UserDataRepository,
    getFollowableTopics: GetFollowableTopicsUseCase,
    // TODO: see comment below
    @Assisted val key: InterestsNavKey,
) : ViewModel() {

    // TODO: this should no longer be necessary, the currently selected topic should be
    //  available through the navigation state.
    // The Android original kept this in a `SavedStateHandle`, which needs saved-state-aware
    // `CreationExtras` that the Navigation 3 ViewModel decorator does not supply on every target.
    // The selection is re-seeded from the nav key on restore, so nothing is lost.
    private val selectedTopicId = MutableStateFlow(key.initialTopicId)

    val uiState: StateFlow<InterestsUiState> = combine(
        selectedTopicId,
        getFollowableTopics(sortBy = TopicSortField.NAME),
        InterestsUiState::Interests,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InterestsUiState.Loading,
    )

    fun followTopic(followedTopicId: String, followed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTopicIdFollowed(followedTopicId, followed)
        }
    }

    fun onTopicClick(topicId: String?) {
        // TODO: This should modify the navigation state directly rather than just updating the
        //  savedStateHandle
        selectedTopicId.value = topicId
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(key: InterestsNavKey): InterestsViewModel
    }
}

sealed interface InterestsUiState {
    data object Loading : InterestsUiState

    data class Interests(val selectedTopicId: String?, val topics: List<FollowableTopic>) : InterestsUiState

    data object Empty : InterestsUiState
}
