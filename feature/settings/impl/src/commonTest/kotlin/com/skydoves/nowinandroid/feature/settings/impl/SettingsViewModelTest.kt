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

package com.skydoves.nowinandroid.feature.settings.impl

import app.cash.turbine.test
import com.skydoves.nowinandroid.core.model.data.DarkThemeConfig
import com.skydoves.nowinandroid.core.model.data.ThemeBrand
import com.skydoves.nowinandroid.core.testing.repository.TestUserDataRepository
import com.skydoves.nowinandroid.core.testing.repository.emptyUserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsViewModelTest {

    private val userDataRepository = TestUserDataRepository()
    private lateinit var viewModel: SettingsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(kotlinx.coroutines.test.UnconfinedTestDispatcher())
        viewModel = SettingsViewModel(userDataRepository)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(SettingsUiState.Loading, viewModel.settingsUiState.value)
    }

    @Test
    fun stateIsSuccessAfterUserDataLoads() = runTest {
        viewModel.settingsUiState.test {
            assertEquals(SettingsUiState.Loading, awaitItem())
            userDataRepository.setUserData(emptyUserData)

            assertEquals(
                SettingsUiState.Success(
                    UserEditableSettings(
                        brand = ThemeBrand.DEFAULT,
                        useDynamicColor = false,
                        darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                    ),
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun changingTheThemeUpdatesTheState() = runTest {
        userDataRepository.setUserData(emptyUserData)

        viewModel.updateThemeBrand(ThemeBrand.ANDROID)
        viewModel.updateDarkThemeConfig(DarkThemeConfig.DARK)
        viewModel.updateDynamicColorPreference(true)

        val userData = userDataRepository.getCurrentUserData()
        assertEquals(ThemeBrand.ANDROID, userData.themeBrand)
        assertEquals(DarkThemeConfig.DARK, userData.darkThemeConfig)
        assertEquals(true, userData.useDynamicColor)
    }
}
