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

package com.skydoves.nowinandroid.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private object TestFirstTopLevelKey : NavKey
private object TestSecondTopLevelKey : NavKey
private object TestThirdTopLevelKey : NavKey
private object TestKeyFirst : NavKey
private object TestKeySecond : NavKey

class NavigatorTest {

    private lateinit var navigationState: NavigationState
    private lateinit var navigator: Navigator

    @BeforeTest
    fun setup() {
        val startKey = TestFirstTopLevelKey
        val topLevelStack = NavBackStack<NavKey>(startKey)
        val topLevelKeys = listOf(startKey, TestSecondTopLevelKey, TestThirdTopLevelKey)
        val subStacks = topLevelKeys.associateWith { key -> NavBackStack(key) }

        navigationState = NavigationState(
            startKey = startKey,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
        )
        navigator = Navigator(navigationState)
    }

    @Test
    fun startKey() {
        assertEquals(TestFirstTopLevelKey, navigationState.startKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun navigate() {
        navigator.navigate(TestKeyFirst)

        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
        assertEquals(TestKeyFirst, navigationState.subStacks[TestFirstTopLevelKey]?.last())
    }

    @Test
    fun navigateTopLevel() {
        navigator.navigate(TestSecondTopLevelKey)

        assertEquals(TestSecondTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun navigateSingleTop() {
        navigator.navigate(TestKeyFirst)
        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )

        navigator.navigate(TestKeyFirst)
        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )
    }

    @Test
    fun navigateTopLevelSingleTopClearsItsSubStack() {
        navigator.navigate(TestSecondTopLevelKey)
        navigator.navigate(TestKeyFirst)
        assertEquals(
            listOf(TestSecondTopLevelKey, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )

        navigator.navigate(TestSecondTopLevelKey)
        assertEquals(listOf(TestSecondTopLevelKey), navigationState.currentSubStack.toList())
    }

    @Test
    fun subStack() {
        navigator.navigate(TestKeyFirst)
        assertEquals(TestKeyFirst, navigationState.currentKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)

        navigator.navigate(TestKeySecond)
        assertEquals(TestKeySecond, navigationState.currentKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun multiStackKeepsEachTopLevelStackIntact() {
        navigator.navigate(TestKeyFirst)
        assertEquals(TestKeyFirst, navigationState.currentKey)

        navigator.navigate(TestSecondTopLevelKey)
        assertEquals(TestSecondTopLevelKey, navigationState.currentKey)

        navigator.navigate(TestKeySecond)
        assertEquals(TestKeySecond, navigationState.currentKey)
        assertEquals(TestSecondTopLevelKey, navigationState.currentTopLevelKey)

        // Returning to the first tab restores where it was left.
        navigator.navigate(TestFirstTopLevelKey)
        assertEquals(TestKeyFirst, navigationState.currentKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun popOneNonTopLevel() {
        navigator.navigate(TestKeyFirst)
        navigator.navigate(TestKeySecond)
        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeyFirst, TestKeySecond),
            navigationState.currentSubStack.toList(),
        )

        navigator.goBack()

        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )
        assertEquals(TestKeyFirst, navigationState.currentKey)
    }

    @Test
    fun popOneTopLevel() {
        navigator.navigate(TestKeyFirst)
        navigator.navigate(TestSecondTopLevelKey)
        assertEquals(listOf(TestSecondTopLevelKey), navigationState.currentSubStack.toList())

        navigator.goBack()

        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )
        assertEquals(TestKeyFirst, navigationState.currentKey)
    }

    @Test
    fun popMultipleNonTopLevel() {
        navigator.navigate(TestKeyFirst)
        navigator.navigate(TestKeySecond)

        navigator.goBack()
        navigator.goBack()

        assertEquals(listOf(TestFirstTopLevelKey), navigationState.currentSubStack.toList())
        assertEquals(TestFirstTopLevelKey, navigationState.currentKey)
    }

    @Test
    fun popMultipleTopLevel() {
        navigator.navigate(TestSecondTopLevelKey)
        navigator.navigate(TestKeyFirst)
        navigator.navigate(TestThirdTopLevelKey)
        navigator.navigate(TestKeySecond)
        assertEquals(
            listOf(TestThirdTopLevelKey, TestKeySecond),
            navigationState.currentSubStack.toList(),
        )

        repeat(4) { navigator.goBack() }

        assertEquals(listOf(TestFirstTopLevelKey), navigationState.currentSubStack.toList())
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun throwOnEmptyBackStack() {
        assertFailsWith<IllegalStateException> {
            navigator.goBack()
        }
    }
}
